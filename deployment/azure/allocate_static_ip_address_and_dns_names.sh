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

RESOURCE_GROUP_NAME=$1
CLUSTER_NAME=$2
ZONE_NAME=$3
ZONE_RESOURCE_GROUP_NAME=${4:-$RESOURCE_GROUP_NAME} # optional, only set if zone is defined in a different resource group 

. $SCRIPTPATH/../kubernetes/includes/allIncludes.inc

GATEWAY_NAME="gateway"
GATEWAY_IP_ADDRESS="" # will be set in allocateStaticIpAddressForGateway

function allocateStaticIpAddressForGateway() {
	local SERVICE_NAME="$GATEWAY_NAME"
	local ADDRESS_NAME="$SERVICE_NAME-$CLUSTER_NAME-staticIp" # used to identify the address
	local NODE_RESOURCE_GROUP=`az aks show --resource-group $RESOURCE_GROUP_NAME --name $CLUSTER_NAME --query nodeResourceGroup --output tsv`
	
	# Create the static IP address
	local ADDRESS_JSON=`az network public-ip create \
	    --resource-group $NODE_RESOURCE_GROUP \
	    --name $ADDRESS_NAME \
	    --allocation-method static`
	local STATIC_IP_ADDRESS=`echo $ADDRESS_JSON | jq .publicIp.ipAddress | tr -d '"'`

	echo "Created static IP address $STATIC_IP_ADDRESS for $SERVICE_NAME"
	
	GATEWAY_IP_ADDRESS=$STATIC_IP_ADDRESS
}

function allocateDomainNameForIpAddress() {
	local SERVICE_NAME="$1"
	local RECORD_SET_NAME="$SERVICE_NAME.$CLUSTER_NAME"
	az network dns record-set a add-record \
		--ipv4-address $GATEWAY_IP_ADDRESS \
		--record-set-name $RECORD_SET_NAME \
		--resource-group $ZONE_RESOURCE_GROUP_NAME \
		--zone-name $ZONE_NAME \
		--output none
	FULLY_QUALIFIED_DOMAIN_NAME="$RECORD_SET_NAME.$ZONE_NAME"
	
	echo "Created DNS A record $FULLY_QUALIFIED_DOMAIN_NAME for $GATEWAY_IP_ADDRESS"
}

echo
echo "################################################################"
echo "################################################################"
echo "######### Allocate static IP address and DNS records ###########"
echo "################################################################"
echo "################################################################"

echo
echo "########## Resource names ##########"
echo "Resource group:"
echo $RESOURCE_GROUP_NAME
echo
echo "Cluster:"
echo $CLUSTER_NAME

echo
echo "########## Get static IP address for gateway ##########"
allocateStaticIpAddressForGateway

echo
echo "########## Create DNS records ##########"
# The gateway address is required because TLS termination is performed
# using a single certificate that uses gateway as common name and all other
# names as subject alt names (because Ambassador is not able to handle
# multiple certificates on one port that is used for multiple host names).
allocateDomainNameForIpAddress "gateway"
allocateDomainNameForIpAddress "grafana"
allocateDomainNameForIpAddress "hono-adapter-amqp-vertx"
allocateDomainNameForIpAddress "hono-adapter-http-vertx"
allocateDomainNameForIpAddress "hono-adapter-kura"
allocateDomainNameForIpAddress "hono-adapter-mqtt-vertx"
allocateDomainNameForIpAddress "hono-dispatch-router"
allocateDomainNameForIpAddress "hono-dispatch-router-ext"
allocateDomainNameForIpAddress "hono-service-device-registry"
allocateDomainNameForIpAddress "hono-service-messaging"
allocateDomainNameForIpAddress "influxdb"

allocateDomainNameForIpAddress "hawkbit"

allocateDomainNameForIpAddress "kuksa-appstore"
allocateDomainNameForIpAddress "traccar"

allocateDomainNameForIpAddress "influxdb-rest-service"
allocateDomainNameForIpAddress "mail-notification"

