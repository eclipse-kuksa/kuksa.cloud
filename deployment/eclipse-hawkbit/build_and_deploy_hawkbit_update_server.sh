#!/bin/bash
#
# ******************************************************************************
# Copyright (c) 2019 Bosch Software Innovations GmbH [and others].
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/index.php
#
# *****************************************************************************
#

# Make the script fail if a command fails
set -e

SCRIPTPATH=$(dirname "$(readlink -f "$0")")

# The version to check out - the latest version for which deployment files have been released in
# https://github.com/eclipse/hawkbit/tree/master/hawkbit-runtime/docker
HAWKBIT_MAJOR_VERSION=$1
HAWKBIT_USERNAME=$2
HAWKBIT_PASSWORD=$3
AZURE_DOCKER_REGISTRY_SECRET=$4
AZURE_DOCKER_REGISTRY_SERVER=$5
AZURE_DOCKER_REGISTRY_USERNAME=$6
AZURE_DOCKER_REGISTRY_PASSWORD=$7
AZURE_DOCKER_REGISTRY_EMAIL=$8

HAWKBIT_VERSION="$HAWKBIT_MAJOR_VERSION"-mysql

NAMESPACE=hawkbit

DOCKER_IMAGE_NAME=hawkbit-update-server
DOCKER_MAJOR_IMAGE_VERSION="$HAWKBIT_MAJOR_VERSION"-kuksa
DOCKER_IMAGE_VERSION="$HAWKBIT_VERSION"-kuksa

. $SCRIPTPATH/../kubernetes/includes/allIncludes.inc

echo
echo "##########################################################################"
echo "##########################################################################"
echo "############### Eclipse hawkBit $HAWKBIT_MAJOR_VERSION build and deployment ###############"
echo "##########################################################################"
echo "##########################################################################"

echo
echo "########## Initialization ##########"

cd $SCRIPTPATH

echo 
echo "##### Check out repository #####"
git clone https://github.com/eclipse/hawkbit.git
cd $SCRIPTPATH/hawkbit
# Get the deployment files from master - they are assumed to be unchanged by later commits
git checkout master

echo
echo "##### Create intermediate directories  #####"
mkdir $SCRIPTPATH/hawkbit-target
mkdir $SCRIPTPATH/hawkbit-target/$HAWKBIT_MAJOR_VERSION
mkdir $SCRIPTPATH/hawkbit-target/$HAWKBIT_VERSION

echo
echo "##### Copy docker data  #####"
cp $SCRIPTPATH/hawkbit/hawkbit-runtime/docker/$HAWKBIT_MAJOR_VERSION/* $SCRIPTPATH/hawkbit-target/$HAWKBIT_MAJOR_VERSION
cp $SCRIPTPATH/hawkbit/hawkbit-runtime/docker/$HAWKBIT_VERSION/* $SCRIPTPATH/hawkbit-target/$HAWKBIT_VERSION
cp $SCRIPTPATH/hawkbit/hawkbit-runtime/docker/docker-compose.yml $SCRIPTPATH/hawkbit-target/

echo "##### Checkout matching hawkbit version #####"
cd $SCRIPTPATH/hawkbit
# Get the tagged sources of the desired version
git checkout tags/$HAWKBIT_MAJOR_VERSION

echo
echo "########## Build and push docker image  ##########"

echo
echo "##### Replace credentials #####"
cd $SCRIPTPATH/hawkbit/hawkbit-runtime/hawkbit-update-server/src/main/resources/

sed -i 's/security.user.name=admin/security.user.name='"$HAWKBIT_USERNAME"'/' application.properties
sed -i 's/security.user.password=admin/security.user.password='"$HAWKBIT_PASSWORD"'/' application.properties

sed -i 's/hawkbit.server.ui.demo.user=admin/hawkbit.server.ui.demo.user='"$HAWKBIT_USERNAME"'/' application.properties
sed -i 's/hawkbit.server.ui.demo.password=admin/hawkbit.server.ui.demo.password='"$HAWKBIT_PASSWORD"'/' application.properties

echo
cat application.properties

cd $SCRIPTPATH/hawkbit/hawkbit-autoconfigure/src/main/resources/

sed -i 's/security.user.name=admin/security.user.name='"$HAWKBIT_USERNAME"'/' hawkbit-security-defaults.properties
sed -i 's/security.user.password=admin/security.user.password='"$HAWKBIT_PASSWORD"'/' hawkbit-security-defaults.properties

echo
cat hawkbit-security-defaults.properties

echo
echo "#### Maven build #####"
cd $SCRIPTPATH/hawkbit
mvn clean install -DskipTests
ls -lah $SCRIPTPATH/hawkbit/hawkbit-runtime/hawkbit-update-server/target/
cp $SCRIPTPATH/hawkbit/hawkbit-runtime/hawkbit-update-server/target/hawkbit-update-server-*-SNAPSHOT.jar $SCRIPTPATH/hawkbit-target/$HAWKBIT_MAJOR_VERSION/hawkbit-update-server.jar
cp $SCRIPTPATH/hawkbit/hawkbit-runtime/hawkbit-update-server/target/hawkbit-update-server-*-SNAPSHOT.jar $SCRIPTPATH/hawkbit-target/$HAWKBIT_VERSION/hawkbit-update-server.jar

echo
echo "##### Modify major docker file #####"
cd $SCRIPTPATH/hawkbit-target/$HAWKBIT_MAJOR_VERSION

sed -i 's/apk del build-dependencies/apk del build-dependencies\n\nCOPY hawkbit-update-server.jar $HAWKBIT_HOME/' Dockerfile
sed -i '/\&\& wget/d' Dockerfile
sed -i '/\&\& gpg --batch/d' Dockerfile

echo
cat Dockerfile

echo
echo "##### Build major docker image #####"
docker build -t $DOCKER_IMAGE_NAME:$DOCKER_MAJOR_IMAGE_VERSION .
docker images

echo
echo "##### Push major docker image to Azure docker registry #####"
docker login \
	--username $AZURE_DOCKER_REGISTRY_USERNAME \
	--password $AZURE_DOCKER_REGISTRY_PASSWORD \
	$AZURE_DOCKER_REGISTRY_SERVER
docker tag $DOCKER_IMAGE_NAME:$DOCKER_MAJOR_IMAGE_VERSION $AZURE_DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:$DOCKER_MAJOR_IMAGE_VERSION
docker push $AZURE_DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:$DOCKER_MAJOR_IMAGE_VERSION

echo
echo "##### Modify docker file #####"
cd $SCRIPTPATH/hawkbit-target/$HAWKBIT_VERSION

sed -i 's#FROM hawkbit/hawkbit-update-server:'"$HAWKBIT_MAJOR_VERSION"'#FROM '"$AZURE_DOCKER_REGISTRY_SERVER"'/'"$DOCKER_IMAGE_NAME"':'"$DOCKER_MAJOR_IMAGE_VERSION"'#' Dockerfile 

echo
cat Dockerfile

echo
echo "##### Build docker image #####"
docker build -t $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION .
docker images

echo
echo "##### Push docker image to Azure docker registry #####"
docker login \
	--username $AZURE_DOCKER_REGISTRY_USERNAME \
	--password $AZURE_DOCKER_REGISTRY_PASSWORD \
	$AZURE_DOCKER_REGISTRY_SERVER
docker tag $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION $AZURE_DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION
docker push $AZURE_DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION

echo
echo "########## Create Kubernetes deployment scripts  ##########"
cd $SCRIPTPATH/hawkbit-target

echo
echo "##### Convert docker compose file to kubernetes resource files #####"

# Note that the following command requires the installation of the command line tool kompose.
# Installation instructions can be found at http://kompose.io/
kompose convert

echo
echo "##### Replace relevant parts in kubernetes resource files #####"

sed -i 's#image: hawkbit/hawkbit-update-server:latest-mysql#image: '"$AZURE_DOCKER_REGISTRY_SERVER"'/hawkbit-update-server:'"$DOCKER_IMAGE_VERSION"'#' hawkbit-deployment.yaml
sed -i 's/resources: {}/resources: {}\n      imagePullSecrets:\n      - name: '"$AZURE_DOCKER_REGISTRY_SECRET"'/' hawkbit-deployment.yaml
cat hawkbit-deployment.yaml

echo
echo "########## Deployment ##########"

echo
echo "##### Create namespace #####"
createOrReuseNamespace $NAMESPACE

echo
echo "##### Create secret #####"
createOrReuseDockerRegistrySecret \
	$AZURE_DOCKER_REGISTRY_SECRET \
	$NAMESPACE \
	$AZURE_DOCKER_REGISTRY_SERVER \
	$AZURE_DOCKER_REGISTRY_USERNAME \
	$AZURE_DOCKER_REGISTRY_PASSWORD \
	$AZURE_DOCKER_REGISTRY_EMAIL

echo
echo "##### Deploy kubernetes resources #####"
cd $SCRIPTPATH/hawkbit-target
kubectl apply -f hawkbit-deployment.yaml -n $NAMESPACE
kubectl apply -f hawkbit-service.yaml -n $NAMESPACE
kubectl apply -f mysql-deployment.yaml -n $NAMESPACE
kubectl apply -f mysql-service.yaml -n $NAMESPACE
kubectl apply -f rabbitmq-deployment.yaml -n $NAMESPACE
kubectl apply -f rabbitmq-service.yaml -n $NAMESPACE

echo
echo  "########## Final cleanup ##########"
cd $SCRIPTPATH
ls -lah

echo
echo "##### Delete hawkbit directory #####"
rm -rf hawkbit
ls -lah

echo
echo "##### Delete intermediate diretories #####"
rm -rf hawkbit-target
ls -lah

