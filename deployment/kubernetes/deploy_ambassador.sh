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

CLUSTER_NAME=$1
DNS_ZONE_NAME=$2
GATEWAY_IP_ADDRESS=$3

# The chart version from https://hub.helm.sh/charts/stable/ambassador
# Note: the chart version does not match the app version
RESOURCE_DESCRIPTOR_TEMPLATE=$SCRIPTPATH/ambassador/ambassador-service.yaml
TARGET_DIR=$SCRIPTPATH/target
RESOURCE_DESCRIPTOR=$TARGET_DIR/ambassador-service.yaml

. $SCRIPTPATH/../kubernetes/includes/allIncludes.inc

echo
echo "########################################################################"
echo "########################################################################"
echo "###############     Install and configure Ambassador     ###############"
echo "########################################################################"
echo "########################################################################"

echo
echo "########## Initialization ##########"

echo "The gateway will use the static IP address $GATEWAY_IP_ADDRESS"

echo
echo "########## Create resource descriptors ##########"
mkdir -p $TARGET_DIR
cp $RESOURCE_DESCRIPTOR_TEMPLATE $RESOURCE_DESCRIPTOR
sed -i "s/<CLUSTER_NAME>/$CLUSTER_NAME/" $RESOURCE_DESCRIPTOR
sed -i "s/<DNS_ZONE_NAME>/$DNS_ZONE_NAME/" $RESOURCE_DESCRIPTOR
sed -i "s/<GATEWAY_IP_ADDRESS>/$GATEWAY_IP_ADDRESS/" $RESOURCE_DESCRIPTOR

echo
echo "########## Install/Update Ambassador - manually - ##########"
echo "See https://www.getambassador.io/user-guide/install"
echo " or https://www.getambassador.io/reference/upgrading"

echo
echo "########## Deploy Ambassador service ##########"

kubectl apply -f $RESOURCE_DESCRIPTOR

echo
echo "########## Clean-up ##########"

echo "### Delete customized resource descriptors ###"
rm -rf $TARGET_DIR
