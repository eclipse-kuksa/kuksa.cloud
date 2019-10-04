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

DOCKER_REGISTRY_SERVER=$1
DOCKER_REGISTRY_USERNAME=$2
DOCKER_REGISTRY_PASSWORD=$3
VERSION=${4:-"0.1.0"}

DOCKER_IMAGE_NAME="influxdb-rest"

echo
echo "##############################################################"
echo "##############################################################"
echo "########### Eclipse Kuksa InfluxDb-Rest Image push ###########"
echo "##############################################################"
echo "##############################################################"

echo
echo "########## Initialization ##########"

cd $SCRIPTPATH

echo
echo "########## Build and push docker image  ##########"

echo
echo "##### Build docker image #####"
docker build --tag $DOCKER_IMAGE_NAME:$VERSION .
docker tag $DOCKER_IMAGE_NAME:$VERSION $DOCKER_IMAGE_NAME:latest

echo
echo "##### Push docker image to Docker registry #####"
docker login \
	--username $DOCKER_REGISTRY_USERNAME \
	--password $DOCKER_REGISTRY_PASSWORD \
	$DOCKER_REGISTRY_SERVER
docker tag $DOCKER_IMAGE_NAME:$VERSION $DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:$VERSION
docker push $DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:$VERSION
docker tag $DOCKER_IMAGE_NAME:latest $DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:latest
docker push $DOCKER_REGISTRY_SERVER/$DOCKER_IMAGE_NAME:latest

