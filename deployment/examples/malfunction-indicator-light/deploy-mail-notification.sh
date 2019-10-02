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

SENDER_MAIL=$1
SENDER_NAME=$2
SMTP_HOST=$3
SMTP_PORT=$4
SMTP_TRANSPORT=$5
SMTP_USERNAME=$6
SMTP_PASSWORD=$7

DOCKER_REGISTRY_SERVER=$8
DOCKER_REGISTRY_USERNAME=$9
DOCKER_REGISTRY_PASSWORD=${10}
DOCKER_REGISTRY_EMAIL=${11}

#the docker registry secret is created by this script
DOCKER_REGISTRY_SECRET="docker-secret"
NAMESPACE=extensions
DOCKER_IMAGE_NAME="mail-notification"
VERSION="0.1.0"

TARGET_DIR=$SCRIPTPATH/mail-notification/target
CONFIGMAP_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/mail-notification/mail-notification-configmap.yaml
CONFIGMAP_DESCRIPTOR=$TARGET_DIR/mail-notification-configmap.yaml
SECRET_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/mail-notification/mail-notification-secret.yaml
SECRET_DESCRIPTOR=$TARGET_DIR/mail-notification-secret.yaml
DEPLOYMENT_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/mail-notification/mail-notification-deployment.yaml
DEPLOYMENT_DESCRIPTOR=$TARGET_DIR/mail-notification-deployment.yaml
SERVICE_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/mail-notification/mail-notification-service.yaml
SERVICE_DESCRIPTOR=$TARGET_DIR/mail-notification-service.yaml

. $SCRIPTPATH/../../kubernetes/includes/namespace/createOrReuseNamespace.inc
. $SCRIPTPATH/../../kubernetes/includes/secret/createOrReuseDockerRegistrySecret.inc

echo
echo "##############################################################"
echo "##############################################################"
echo "###### Eclipse Kuksa Cloud mail notification deployment ######"
echo "##############################################################"
echo "##############################################################"

echo
echo "########## Initialization ##########"

cd $SCRIPTPATH

echo 
echo "##### Customize configuration #####"
mkdir -p $TARGET_DIR

cp $CONFIGMAP_DESCRIPTOR_TEMPLATE $CONFIGMAP_DESCRIPTOR
sed -i "s/<SENDER_MAIL_PLACEHOLDER>/$SENDER_MAIL/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<SENDER_NAME_PLACEHOLDER>/$SENDER_NAME/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<SMTP_HOST_PLACEHOLDER>/$SMTP_HOST/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<SMTP_PORT_PLACEHOLDER>/$SMTP_PORT/" $CONFIGMAP_DESCRIPTOR
sed -i "s/<SMTP_TRANSPORT_PLACEHOLDER>/$SMTP_TRANSPORT/" $CONFIGMAP_DESCRIPTOR

cp $SECRET_DESCRIPTOR_TEMPLATE $SECRET_DESCRIPTOR
SMTP_USERNAME_BASE64="$(echo $SMTP_USERNAME | base64 --wrap=0)"
SMTP_PASSWORD_BASE64="$(echo $SMTP_PASSWORD | base64 --wrap=0)"
sed -i "s/<SMTP_USERNAME_PLACEHOLDER>/$SMTP_USERNAME_BASE64/" $SECRET_DESCRIPTOR
sed -i "s/<SMTP_PASSWORD_PLACEHOLDER>/$SMTP_PASSWORD_BASE64/" $SECRET_DESCRIPTOR

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
kubectl apply -f $DEPLOYMENT_DESCRIPTOR -n $NAMESPACE
kubectl apply -f $SECRET_DESCRIPTOR -n $NAMESPACE
kubectl apply -f $CONFIGMAP_DESCRIPTOR -n $NAMESPACE
kubectl apply -f $SERVICE_DESCRIPTOR -n $NAMESPACE

echo
echo  "########## Final cleanup ##########"

cd $SCRIPTPATH
ls -lah

rm -rf $TARGET_DIR
