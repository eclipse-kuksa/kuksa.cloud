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

INFLUXDB_REST_URL=$1
INFLUXDB_REST_PORT=$2
INFLUXDB_REST_DEVICEID=$3
MAIL_URL=$4
MAIL_PORT=$5
RECEIVER_MAIL=$6
MAPS_API_KEY=$7

DOCKER_REGISTRY_SERVER=$8
DOCKER_REGISTRY_USERNAME=$9
DOCKER_REGISTRY_PASSWORD=${10}
DOCKER_REGISTRY_EMAIL=${11}

#the docker registry secret is created by this script
DOCKER_REGISTRY_SECRET="docker-secret"
NAMESPACE=extensions
DOCKER_IMAGE_NAME="indicator-light-service"
VERSION="0.1.0"

TARGET_DIR=$SCRIPTPATH/mil-service/target
CONFIGMAP_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/mil-service/mil-service-configmap.yaml
CONFIGMAP_DESCRIPTOR=$TARGET_DIR/mil-service-configmap.yaml
DEPLOYMENT_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/mil-service/mil-service-deployment.yaml
DEPLOYMENT_DESCRIPTOR=$TARGET_DIR/mil-service-deployment.yaml

. $SCRIPTPATH/../../kubernetes/includes/namespace/createOrReuseNamespace.inc
. $SCRIPTPATH/../../kubernetes/includes/secret/createOrReuseDockerRegistrySecret.inc

echo
echo "##############################################################"
echo "##############################################################"
echo "######### Eclipse Kuksa Cloud mil service deployment #########"
echo "##############################################################"
echo "##############################################################"

echo
echo "########## Initialization ##########"

cd $SCRIPTPATH

echo 
echo "##### Customize configuration #####"
mkdir -p $TARGET_DIR

cp $CONFIGMAP_DESCRIPTOR_TEMPLATE $CONFIGMAP_DESCRIPTOR
sed -i "s/<INFLUXDB_REST_URL_PLACEHOLDER>/$INFLUXDB_REST_URL/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<INFLUXDB_REST_P_PLACEHOLDER>/$INFLUXDB_REST_PORT/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<INFLUXDB_REST_DEVICEID_PLACEHOLDER>/$INFLUXDB_REST_DEVICEID/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<MAIL_URL_PLACEHOLDER>/$MAIL_URL/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<MAIL_P_PLACEHOLDER>/$MAIL_PORT/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<RECEIVER_MAIL_PLACEHOLDER>/$RECEIVER_MAIL/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<MAPS_API_KEY_PLACEHOLDER>/$MAPS_API_KEY/" $CONFIGMAP_DESCRIPTOR

cp $DEPLOYMENT_DESCRIPTOR_TEMPLATE $DEPLOYMENT_DESCRIPTOR
sed -i "s/<VERSION>/$VERSION/" $DEPLOYMENT_DESCRIPTOR
sed -i "s/<DOCKER_REGISTRY_SERVER>/$DOCKER_REGISTRY_SERVER/" $DEPLOYMENT_DESCRIPTOR
sed -i "s/<DOCKER_IMAGE_NAME>/$DOCKER_IMAGE_NAME/" $DEPLOYMENT_DESCRIPTOR
sed -i "s/<DOCKER_REGISTRY_SECRET>/$DOCKER_REGISTRY_SECRET/" $DEPLOYMENT_DESCRIPTOR

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

echo
echo  "########## Final cleanup ##########"

cd $SCRIPTPATH
ls -lah

rm -rf $TARGET_DIR
