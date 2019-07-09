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
AMBASSADOR_CHART_VERSION=2.0.1
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
echo "########## Install Ambassador ##########"

INSTALLED_AMBASSADOR_DEPLOYMENT="$(kubectl get deployments ambassador -o yaml --ignore-not-found=true)"

if [[ "" != $INSTALLED_AMBASSADOR_DEPLOYMENT ]]; then
	INSTALLED_AMBASSADOR_CHART_VERSION="$(kubectl get deployments ambassador -o yaml --ignore-not-found=true \
		| yq r - metadata.labels \
		| grep helm.sh/chart \
		| sed -e 's/helm.sh\/chart: ambassador-//')"
	echo "Version of Ambassador chart: $INSTALLED_AMBASSADOR_CHART_VERSION"
else
	INSTALLED_AMBASSADOR_CHART_VERSION=""
	echo "Did not find an Ambassador deployment."
fi

if [[ "$INSTALLED_AMBASSADOR_CHART_VERSION" == "$AMBASSADOR_CHART_VERSION" ]]; then
	echo "Ambassador is up-to-date"
elif [[ "" != "$INSTALLED_AMBASSADOR_CHART_VERSION" ]]; then
	echo "Upgrading Ambassador using local tiller ..."
	tiller --storage=secret &
	export HELM_HOST=:44134
	helm repo update \
	&& helm upgrade --wait --timeout 600 ambassador \
	    --set service.http.enabled=false \
	    --set service.https.port=8443 \
	    --set service.htpss.targetPort=8443 \
		--set service.loadBalancerIP=$GATEWAY_IP_ADDRESS \
		--version $AMBASSADOR_CHART_VERSION \
		stable/ambassador \
	&& kill "$(jobs -p %tiller)"
else
	echo "Installing Ambassador using local tiller ..."
	tiller --storage=secret &
	export HELM_HOST=:44134
	helm repo update \
	&& helm install \
		--wait \
		--timeout 600 \
	    --name ambassador \
	    --set service.http.enabled=false \
	    --set service.https.port=8443 \
	    --set service.htpss.targetPort=8443 \
		--set service.loadBalancerIP=$GATEWAY_IP_ADDRESS \
		--version $AMBASSADOR_CHART_VERSION \
		stable/ambassador \
	&& kill "$(jobs -p %tiller)"
fi

echo
echo "########## Deploy Ambassador service ##########"

kubectl apply -f $RESOURCE_DESCRIPTOR

echo
echo "########## Clean-up ##########"

echo "### Delete customized resource descriptors ###"
rm -rf $TARGET_DIR
