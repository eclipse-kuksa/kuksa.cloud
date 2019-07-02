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

NAMESPACE=kuksa
DOCKER_IMAGE_NAME="traccar/traccar"
VERSION="4.5"

TARGET_DIR=$SCRIPTPATH/traccar/target
DEPLOYMENT_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/traccar/traccar-deployment.yaml
DEPLOYMENT_DESCRIPTOR=$TARGET_DIR/traccar-deployment.yaml
SERVICE_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/traccar/traccar-service.yaml
SERVICE_DESCRIPTOR=$TARGET_DIR/traccar-service.yaml

. $SCRIPTPATH/../kubernetes/includes/allIncludes.inc

echo
echo "##############################################################"
echo "##############################################################"
echo "############### Eclipse Kuksa Cloud deployment ###############"
echo "##############################################################"
echo "##############################################################"

echo
echo "########## Initialization ##########"

cd $SCRIPTPATH

echo 
echo "##### Customize configuration #####"
mkdir -p $TARGET_DIR
cp $DEPLOYMENT_DESCRIPTOR_TEMPLATE $DEPLOYMENT_DESCRIPTOR
sed -i "s/<VERSION>/$VERSION/" $DEPLOYMENT_DESCRIPTOR
sed -i "s#<DOCKER_IMAGE_NAME>#$DOCKER_IMAGE_NAME#" $DEPLOYMENT_DESCRIPTOR

cp $SERVICE_DESCRIPTOR_TEMPLATE $SERVICE_DESCRIPTOR
sed -i "s/<VERSION>/$VERSION/" $SERVICE_DESCRIPTOR

echo
echo "##### Create namespace #####"
createOrReuseNamespace $NAMESPACE

echo
echo "##### Deploy docker image #####"
kubectl apply -f $DEPLOYMENT_DESCRIPTOR -n $NAMESPACE
kubectl apply -f $SERVICE_DESCRIPTOR -n $NAMESPACE

echo
echo  "########## Final cleanup ##########"

cd $SCRIPTPATH
ls -lah

echo
echo "##### Delete kuksa folder #####"

rm -rf $TARGET_DIR
