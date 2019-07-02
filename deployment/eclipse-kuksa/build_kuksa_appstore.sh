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

DOCKER_REGISTRY_SECRET=$1
DOCKER_REGISTRY_SERVER=$2
DOCKER_REGISTRY_USERNAME=$3
DOCKER_REGISTRY_PASSWORD=$4
DOCKER_REGISTRY_EMAIL=$5

APPSTORE_PATH=$SCRIPTPATH/kuksa.cloud/kuksa-appstore
DOCKER_IMAGE_NAME="kuksa-appstore"

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
echo "##### Check out repository #####"
git clone https://github.com/eclipse/kuksa.cloud.git

echo
echo "########## Build and push docker image  ##########"

echo
echo "##### Build with Maven #####"

cd $APPSTORE_PATH
mvn clean verify

# Needs the xq command, see https://github.com/kislyuk/xq
VERSION="$(xq .project.version pom.xml | tr -d '"')"
cp $APPSTORE_PATH/target/kuksa.appstore-$VERSION.jar $APPSTORE_PATH/target/kuksa-appstore.jar

echo
echo "##### Build major docker image #####"
cp $SCRIPTPATH/kuksa-appstore/Dockerfile $APPSTORE_PATH/target/
docker build \
	--tag $DOCKER_IMAGE_NAME:$VERSION \
	$APPSTORE_PATH/target/
docker tag $DOCKER_IMAGE_NAME:$VERSION $DOCKER_IMAGE_NAME:latest

echo
echo "##### Push major docker image to Azure docker registry #####"
docker login \
	--username $DOCKER_REGISTRY_USERNAME \
	--password $DOCKER_REGISTRY_PASSWORD \
	$DOCKER_REGISTRY_SERVER
docker tag $DOCKER_IMAGE_NAME:$VERSION $DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:$VERSION
docker push $DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:$VERSION

echo
echo  "########## Final cleanup ##########"

cd $SCRIPTPATH
ls -lah

echo
echo "##### Delete kuksa.cloud folder #####"

rm -rf $SCRIPTPATH/kuksa.cloud
