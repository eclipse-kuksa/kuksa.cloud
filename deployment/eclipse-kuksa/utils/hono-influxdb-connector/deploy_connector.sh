#!/bin/bash
######################################################################
# Copyright (c) 2019 Bosch Software Innovations GmbH [and others]
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
########################################################################

# Make the script fail if a command fails
set -e

SCRIPTPATH=$(dirname "$(readlink -f "$0")")

INFLUXDB_CLUSTER_INTERNAL_URL=${1:-http://localhost:8086} # ClusterIP of influxdb service

QPID_DISPATCH_ROUTER_HOST=${2:-localhost} # ClusterIP of hono-dispatch-router-ext service
QPID_DISPATCH_ROUTER_PORT=${3:-15671}
HONO_USER=${4:-""}
HONO_PASSWORD=${5:-""}
TRUST_STORE_PATH=${6:-""}
VERIFY_HONO_HOSTNAME=${7:-true}

# Use parameter expansion to convert the string DEFAULT_TENANT:devices,otherTenant:nameOfOtherInfluxDatabase
# into an array with 2 elements: DEFAULT_TENANT:devices and otherTenant:nameOfOtherInfluxDatabase
# See also Section ${parameter/pattern/string} in
# https://www.gnu.org/software/bash/manual/html_node/Shell-Parameter-Expansion.html
CONNECTIONS=(${8//,/ })

DOCKER_REGISTRY_SECRET=${9:-""}
DOCKER_REGISTRY_SERVER=${10:-localhost}
DOCKER_REGISTRY_USERNAME=${11:-""}
DOCKER_REGISTRY_PASSWORD=${12:-pw}

NAMESPACE=hono
DOCKER_IMAGE_NAME=hono-influxdb-connector

VERSION="0.2.0"
DOCKER_IMAGE_VERSION="$VERSION"

SOURCE_DESCRIPTORS=$SCRIPTPATH/kubernetes
TARGET_DIR=$SCRIPTPATH/target
RESOURCE_DESCRIPTORS=$TARGET_DIR/descriptors
CONFIGMAP_DESCRIPTOR=$RESOURCE_DESCRIPTORS/configmap.yaml
DEPLOYMENT_DESCRIPTOR=$RESOURCE_DESCRIPTORS/deployment.yaml
SECRET_DESCRIPTOR=$RESOURCE_DESCRIPTORS/secret.yaml

function createOrReuseDockerRegistrySecret() {
	local NAME_OF_SECRET=$1
	local NAMESPACE=$2
	local DOCKER_REGISTRY_SERVER=$3
	local DOCKER_REGISTRY_USERNAME=$4
	local DOCKER_REGISTRY_PASSWORD=$5
	
	if [[ "" == "$(kubectl get secret $NAME_OF_SECRET --namespace $NAMESPACE --ignore-not-found=true)" ]]; then
		kubectl create secret docker-registry $NAME_OF_SECRET \
			--docker-server=$DOCKER_REGISTRY_SERVER \
			--docker-username=$DOCKER_REGISTRY_USERNAME \
			--docker-password=$DOCKER_REGISTRY_PASSWORD \
			--namespace=$NAMESPACE
	else
		echo "The secret $NAME_OF_SECRET exists already and will be re-used."
	fi
}

echo
echo "##########################################################################"
echo "##########################################################################"
echo "############### Eclipse Hono InfluxDB Connector deployment ###############"
echo "##########################################################################"
echo "##########################################################################"

echo
echo "########## Initialization ##########"

cd $SCRIPTPATH
mkdir -p $TARGET_DIR

echo
echo "########## Deploy project ##########"

echo
echo "##### Customize resource descriptors #####"

mkdir -p $RESOURCE_DESCRIPTORS
cp $SOURCE_DESCRIPTORS/*.yaml $RESOURCE_DESCRIPTORS/

echo "# Customize $CONFIGMAP_DESCRIPTOR #"
sed -i 's#INFLUXDB_URL: ""#INFLUXDB_URL: '"$INFLUXDB_CLUSTER_INTERNAL_URL"'#' $CONFIGMAP_DESCRIPTOR

sed -i 's/QPID_ROUTER_HOST: ""/QPID_ROUTER_HOST: '"$QPID_DISPATCH_ROUTER_HOST"'/' $CONFIGMAP_DESCRIPTOR
sed -i 's/QPID_ROUTER_PORT: "5671"/QPID_ROUTER_PORT: "'"$QPID_DISPATCH_ROUTER_PORT"'"/' $CONFIGMAP_DESCRIPTOR

sed -i 's/HONO_VERIFYHOSTNAME: "true"/HONO_VERIFYHOSTNAME: "'"$VERIFY_HONO_HOSTNAME"'"/' $CONFIGMAP_DESCRIPTOR

echo "# Customize $SECRET_DESCRIPTOR"
HONO_USER="$(echo -n $HONO_USER | base64)"
HONO_PASSWORD="$(echo -n $HONO_PASSWORD | base64)"
sed -i 's/HONO_USER: /HONO_USER: "'"$HONO_USER"'"/' $SECRET_DESCRIPTOR
sed -i 's/HONO_PASSWORD: /HONO_PASSWORD: "'"$HONO_PASSWORD"'"/' $SECRET_DESCRIPTOR

CONNECTIONS_DATA=""
for i in "${!CONNECTIONS[@]}"; do
	CONNECTION=${CONNECTIONS[$i]}
	TENANT_AND_DATABASE_NAME=(${CONNECTION//:/ })
	TENANT=${TENANT_AND_DATABASE_NAME[0]}
	DATABASE_NAME=${TENANT_AND_DATABASE_NAME[1]}
	CONNECTIONS_DATA="$CONNECTIONS_DATA\n  HONO_CONNECTIONS_${i}_TENANTID: $TENANT"
	CONNECTIONS_DATA="$CONNECTIONS_DATA\n  HONO_CONNECTIONS_${i}_INFLUXDATABASENAME: $DATABASE_NAME"
done
sed -i "s/^data:/data:$CONNECTIONS_DATA/" $CONFIGMAP_DESCRIPTOR

echo "# Customize $DEPLOYMENT_DESCRIPTOR #"
sed -i 's/<VERSION>/'"$VERSION"'/' $DEPLOYMENT_DESCRIPTOR
sed -i 's/image: <DOCKER_IMAGE>:<DOCKER_VERSION>/image: '"$DOCKER_REGISTRY_SERVER"'\/'"$DOCKER_IMAGE_NAME"':'"$DOCKER_IMAGE_VERSION"'/' $DEPLOYMENT_DESCRIPTOR
sed -i 's/name: docker-secret/name: '"$DOCKER_REGISTRY_SECRET"'/' $DEPLOYMENT_DESCRIPTOR

echo
echo "##### Create docker registry secret #####"
createOrReuseDockerRegistrySecret \
	$DOCKER_REGISTRY_SECRET \
	$NAMESPACE \
	$DOCKER_REGISTRY_SERVER \
	$DOCKER_REGISTRY_USERNAME \
	$DOCKER_REGISTRY_PASSWORD
	
echo
echo "##### Create configuration secret if it does not exist #####"
CONF_SECRET_NAME=hono-influxdb-connector-conf
if [[ "" == "$(kubectl get secret --namespace hono --ignore-not-found $CONF_SECRET_NAME)" ]]; then
	kubectl create secret generic $CONF_SECRET_NAME \
	  --from-file=trusted-certs.pem=$TRUST_STORE_PATH \
	  --namespace $NAMESPACE
fi

echo
echo "##### Deploy docker image #####"
kubectl apply -f $CONFIGMAP_DESCRIPTOR -n $NAMESPACE
kubectl apply -f $DEPLOYMENT_DESCRIPTOR -n $NAMESPACE
kubectl apply -f $SECRET_DESCRIPTOR -n $NAMESPACE

echo
echo "########## Final cleanup ##########"
cd $SCRIPTPATH

echo
echo "##### Remove deployment target folder #####"
rm -rf $TARGET_DIR

