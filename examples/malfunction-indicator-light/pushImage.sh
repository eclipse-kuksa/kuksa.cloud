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

echo
echo "##############################################################"
echo "##############################################################"
echo "### Eclipse Kuksa malfunction-indicator-light Images push ####"
echo "##############################################################"
echo "##############################################################"

cd influxdb-rest
. ./pushImage.sh $DOCKER_REGISTRY_SERVER $DOCKER_REGISTRY_USERNAME $DOCKER_REGISTRY_PASSWORD $VERSION
cd ../mail-notification
. ./pushImage.sh $DOCKER_REGISTRY_SERVER $DOCKER_REGISTRY_USERNAME $DOCKER_REGISTRY_PASSWORD $VERSION
cd ../mil-service
. ./pushImage.sh $DOCKER_REGISTRY_SERVER $DOCKER_REGISTRY_USERNAME $DOCKER_REGISTRY_PASSWORD $VERSION

