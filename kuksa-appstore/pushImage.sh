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

DOCKER_REGISTRY_SERVER=$1
DOCKER_REGISTRY_USERNAME=$2
DOCKER_REGISTRY_PASSWORD=$3
VERSION=${4:-0.2.0}

DOCKER_IMAGE_NAME="kuksa-appstore"

echo
echo "##############################################################"
echo "##############################################################"
echo "#### Eclipse Kuksa Cloud app-store Docker build and push #####"
echo "##############################################################"
echo "##############################################################"

echo
echo "########## Build and push Docker image  ##########"

docker build \
	--tag $DOCKER_IMAGE_NAME:$VERSION .
docker tag $DOCKER_IMAGE_NAME:$VERSION $DOCKER_IMAGE_NAME:$VERSION

echo
echo "##### Push major docker image to Docker registry #####"
docker login \
	--username $DOCKER_REGISTRY_USERNAME \
	--password $DOCKER_REGISTRY_PASSWORD \
	$DOCKER_REGISTRY_SERVER
docker tag $DOCKER_IMAGE_NAME:$VERSION $DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:$VERSION
docker push $DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:$VERSION
