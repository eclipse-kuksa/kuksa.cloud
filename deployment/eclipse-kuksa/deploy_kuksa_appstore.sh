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

APPSTORE_USERNAME=$1
APPSTORE_PASSWORD=$2
HAWKBIT_URL=$3
HAWKBIT_USERNAME=$4
HAWKBIT_PASSWORD=$5
DOCKER_REGISTRY_SERVER=$6
DOCKER_REGISTRY_USERNAME=$7
DOCKER_REGISTRY_PASSWORD=$8
DOCKER_REGISTRY_EMAIL=${9}

VAADIN_PRODUCTION_MODE_ENABLED=true

#the docker registry secret is created by this script
DOCKER_REGISTRY_SECRET="docker-secret"
NAMESPACE=kuksa
DOCKER_IMAGE_NAME="kuksa-appstore"
VERSION="0.0.1-SNAPSHOT"

TARGET_DIR=$SCRIPTPATH/kuksa-appstore/target
APPLICATION_YML_TEMPLATE=$SCRIPTPATH/kuksa-appstore/application.yml
APPLICATION_YML=$TARGET_DIR/application.yml
PVC_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/kuksa-appstore/kuksa-appstore-pvc.yaml
PVC_DESCRIPTOR=$TARGET_DIR/kuksa-appstore-pvc.yaml
SECRET_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/kuksa-appstore/kuksa-appstore-secret.yaml
SECRET_DESCRIPTOR=$TARGET_DIR/kuksa-appstore-secret.yaml
DEPLOYMENT_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/kuksa-appstore/kuksa-appstore-deployment.yaml
DEPLOYMENT_DESCRIPTOR=$TARGET_DIR/kuksa-appstore-deployment.yaml
SERVICE_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/kuksa-appstore/kuksa-appstore-service.yaml
SERVICE_DESCRIPTOR=$TARGET_DIR/kuksa-appstore-service.yaml

. $SCRIPTPATH/../kubernetes/includes/allIncludes.inc

echo
echo "##############################################################"
echo "##############################################################"
echo "########## Eclipse Kuksa Cloud app-store deployment ##########"
echo "##############################################################"
echo "##############################################################"

echo
echo "########## Initialization ##########"

cd $SCRIPTPATH

echo 
echo "##### Customize configuration #####"
mkdir -p $TARGET_DIR
cp $APPLICATION_YML_TEMPLATE $APPLICATION_YML
sed -i "s/<APPSTORE_USERNAME>/$APPSTORE_USERNAME/" $TARGET_DIR/application.yml
sed -i "s/<APPSTORE_PASSWORD>/$APPSTORE_PASSWORD/" $TARGET_DIR/application.yml
sed -i "s#<HAWKBIT_URL>#$HAWKBIT_URL#" $TARGET_DIR/application.yml
sed -i "s/<HAWKBIT_USERNAME>/$HAWKBIT_USERNAME/" $TARGET_DIR/application.yml
sed -i "s/<HAWKBIT_PASSWORD>/$HAWKBIT_PASSWORD/" $TARGET_DIR/application.yml
sed -i "s/<VAADIN_PRODUCTION_MODE_ENABLED>/$VAADIN_PRODUCTION_MODE_ENABLED/" $TARGET_DIR/application.yml

cp $PVC_DESCRIPTOR_TEMPLATE $PVC_DESCRIPTOR
sed -i "s/<VERSION>/$VERSION/" $PVC_DESCRIPTOR 

cp $SECRET_DESCRIPTOR_TEMPLATE $SECRET_DESCRIPTOR
APPLICATION_YML_BASE64="$(cat $APPLICATION_YML | base64 --wrap=0)"
sed -i "s/<APPLICATION_YML_BASE64>/$APPLICATION_YML_BASE64/" $SECRET_DESCRIPTOR

cp $DEPLOYMENT_DESCRIPTOR_TEMPLATE $DEPLOYMENT_DESCRIPTOR
sed -i "s/<VERSION>/$VERSION/" $DEPLOYMENT_DESCRIPTOR
sed -i "s/<DOCKER_REGISTRY_SERVER>/$DOCKER_REGISTRY_SERVER/" $DEPLOYMENT_DESCRIPTOR
sed -i "s/<DOCKER_IMAGE_NAME>/$DOCKER_IMAGE_NAME/" $DEPLOYMENT_DESCRIPTOR
sed -i "s/<DOCKER_REGISTRY_SECRET>/$DOCKER_REGISTRY_SECRET/" $DEPLOYMENT_DESCRIPTOR

cp $SERVICE_DESCRIPTOR_TEMPLATE $SERVICE_DESCRIPTOR
sed -i "s/<VERSION>/$VERSION/" $SERVICE_DESCRIPTOR

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
kubectl apply -f $PVC_DESCRIPTOR -n $NAMESPACE
kubectl apply -f $SECRET_DESCRIPTOR -n $NAMESPACE
kubectl apply -f $DEPLOYMENT_DESCRIPTOR -n $NAMESPACE
kubectl apply -f $SERVICE_DESCRIPTOR -n $NAMESPACE

echo
echo  "########## Final cleanup ##########"

cd $SCRIPTPATH
ls -lah

rm -rf $TARGET_DIR
