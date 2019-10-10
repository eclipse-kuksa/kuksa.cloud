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

INFLUXDB_URL=$1
INFLUXDB_PORT=$2
INFLUXDB_DB_NAME=$3

DOCKER_REGISTRY_SERVER=$4
DOCKER_REGISTRY_USERNAME=$5
DOCKER_REGISTRY_PASSWORD=$6
DOCKER_REGISTRY_EMAIL=$7

#the docker registry secret is created by this script
DOCKER_REGISTRY_SECRET="docker-secret"
NAMESPACE=extensions
DOCKER_IMAGE_NAME="influxdb-rest"
VERSION="0.1.0"

TARGET_DIR=$SCRIPTPATH/influxdb-rest/target
CONFIGMAP_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/influxdb-rest/influxdb-rest-configmap.yaml
CONFIGMAP_DESCRIPTOR=$TARGET_DIR/influxdb-rest-configmap.yaml
DEPLOYMENT_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/influxdb-rest/influxdb-rest-deployment.yaml
DEPLOYMENT_DESCRIPTOR=$TARGET_DIR/influxdb-rest-deployment.yaml
SERVICE_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/influxdb-rest/influxdb-rest-service.yaml
SERVICE_DESCRIPTOR=$TARGET_DIR/influxdb-rest-service.yaml

. $SCRIPTPATH/../../kubernetes/includes/namespace/createOrReuseNamespace.inc
. $SCRIPTPATH/../../kubernetes/includes/secret/createOrReuseDockerRegistrySecret.inc

echo
echo "##############################################################"
echo "##############################################################"
echo "######## Eclipse Kuksa Cloud influxdb rest deployment ########"
echo "##############################################################"
echo "##############################################################"

echo
echo "########## Initialization ##########"

cd $SCRIPTPATH

echo 
echo "##### Customize configuration #####"
mkdir -p $TARGET_DIR

cp $CONFIGMAP_DESCRIPTOR_TEMPLATE $CONFIGMAP_DESCRIPTOR
sed -i "s/<INFLUXDB_URL_PLACEHOLDER>/$INFLUXDB_URL/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<INFLUXDB_PORT_PLACEHOLDER>/$INFLUXDB_PORT/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<INFLUXDB_DB_NAME_PLACEHOLDER>/$INFLUXDB_DB_NAME/" $CONFIGMAP_DESCRIPTOR

cp $DEPLOYMENT_DESCRIPTOR_TEMPLATE $DEPLOYMENT_DESCRIPTOR
sed -i "s/<VERSION>/$VERSION/" $DEPLOYMENT_DESCRIPTOR
sed -i "s/<DOCKER_REGISTRY_SERVER>/$DOCKER_REGISTRY_SERVER/" $DEPLOYMENT_DESCRIPTOR
sed -i "s/<DOCKER_IMAGE_NAME>/$DOCKER_IMAGE_NAME/" $DEPLOYMENT_DESCRIPTOR
sed -i "s/<DOCKER_REGISTRY_SECRET>/$DOCKER_REGISTRY_SECRET/" $DEPLOYMENT_DESCRIPTOR

cp $SERVICE_DESCRIPTOR_TEMPLATE $SERVICE_DESCRIPTOR

echo
echo "##### Create namespace #####"
createOrReuseNamespace $NAMESPACE

echo
echo "##### Create secret #####"
createOrReuseDockerRegistrySecret \
	$DOCKER_REGISTRY_SECRET \
	$NAMESPACE \
	$DOCKER_REGISTRY_SERVER \
	$DOCKER_REGISTRY_USERNAME \
	$DOCKER_REGISTRY_PASSWORD \
	$DOCKER_REGISTRY_EMAIL

echo
echo "##### Deploy docker image #####"
kubectl apply -f $CONFIGMAP_DESCRIPTOR -n $NAMESPACE
kubectl apply -f $DEPLOYMENT_DESCRIPTOR -n $NAMESPACE
kubectl apply -f $SERVICE_DESCRIPTOR -n $NAMESPACE

echo
echo  "########## Final cleanup ##########"

cd $SCRIPTPATH
ls -lah

rm -rf $TARGET_DIR
