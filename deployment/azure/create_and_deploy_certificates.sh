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
AZURE_SUBSCRIPTION_ID=$3
AZURE_AD_TENANT_ID=$4
APP_ID_OF_SERVICE_PRINCIPAL_WITH_ACCESS_TO_AZURE_DNS=$5
NAME_OF_SECRET_OF_SERVICE_PRINCIPAL_WITH_ACCESS_TO_AZURE_DNS=$6
DNS_ZONE_NAME=$7
EMAIL_ADDRESS_FOR_LETS_ENCRYPT=$8
LETS_ENCRYPT_ENVIRONMENT=${9:-prod} # either prod or staging
ZONE_RESOURCE_GROUP_NAME=${10:-$RESOURCE_GROUP_NAME} # optional, only set if zone is defined in a different resource group

NAMESPACE_TO_STORE_CERTIFICATES_IN="default"
NAME_OF_CLUSTER_ISSUER="letsencrypt-$LETS_ENCRYPT_ENVIRONMENT-dns01"

. $SCRIPTPATH/../kubernetes/includes/allIncludes.inc

function customizeClusterIssuer() {
	local TEMPLATE_FILE=$1
	local RESOURCE_TEMPLATE=$CLUSTER_ISSUER_TEMPLATES/$TEMPLATE_FILE
	local CUSTOM_RESOURCE=$CLUSTER_ISSUER_RESOURCES/$TEMPLATE_FILE
	
	echo "Creating resource descriptor $CUSTOM_RESOURCE ..."
	cp $RESOURCE_TEMPLATE $CUSTOM_RESOURCE
	sed -i "s/<EMAIL_ADDRESS_FOR_LETS_ENCRYPT>/$EMAIL_ADDRESS_FOR_LETS_ENCRYPT/" $CUSTOM_RESOURCE
	sed -i "s/<AZURE_SUBSCRIPTION_ID>/$AZURE_SUBSCRIPTION_ID/" $CUSTOM_RESOURCE
	sed -i "s/<AZURE_AD_TENANT_ID>/$AZURE_AD_TENANT_ID/" $CUSTOM_RESOURCE
	sed -i "s/<ZONE_RESOURCE_GROUP_NAME>/$ZONE_RESOURCE_GROUP_NAME/" $CUSTOM_RESOURCE
	sed -i "s/<APP_ID_OF_SERVICE_PRINCIPAL_WITH_ACCESS_TO_AZURE_DNS>/$APP_ID_OF_SERVICE_PRINCIPAL_WITH_ACCESS_TO_AZURE_DNS/" $CUSTOM_RESOURCE
	sed -i "s/<NAME_OF_SECRET_OF_SERVICE_PRINCIPAL_WITH_ACCESS_TO_AZURE_DNS>/$NAME_OF_SECRET_OF_SERVICE_PRINCIPAL_WITH_ACCESS_TO_AZURE_DNS/" $CUSTOM_RESOURCE
	sed -i "s/<DNS_ZONE_NAME>/$DNS_ZONE_NAME/" $CUSTOM_RESOURCE
}

function createCertificateDescriptor() {
	local SIMPLE_NAME=$1
	local NAMESPACE=$2
	local TARGET_FILE=$SIMPLE_NAME-certificate.yaml
	local TEMPLATE_FILE=certificate.yaml
	local RESOURCE_TEMPLATE=$CERT_TEMPLATES/$TEMPLATE_FILE
	local CUSTOM_RESOURCE=$CERT_RESOURCES/$TARGET_FILE
	
	echo "Creating resource descriptor $CUSTOM_RESOURCE ..."
	cp $RESOURCE_TEMPLATE $CUSTOM_RESOURCE
	
	sed -i "s/<SIMPLE_NAME>/$SIMPLE_NAME/" $CUSTOM_RESOURCE
	sed -i "s/<CLUSTER_NAME>/$CLUSTER_NAME/" $CUSTOM_RESOURCE
	sed -i "s/<DNS_ZONE_NAME>/$DNS_ZONE_NAME/" $CUSTOM_RESOURCE
	sed -i "s/<NAMESPACE>/$NAMESPACE/" $CUSTOM_RESOURCE
	sed -i "s/<NAME_OF_CLUSTER_ISSUER>/$NAME_OF_CLUSTER_ISSUER/" $CUSTOM_RESOURCE
}

echo
echo "##############################################################################################"
echo "##############################################################################################"
echo "###############     Update Certificates and Cert Issuers for Let's Encrypt     ###############"
echo "##############################################################################################"
echo "##############################################################################################"
echo
echo "########## Initialization ##########"
echo
echo "# To install cert-manager"
echo "# see https://cert-manager.io/docs/installation/kubernetes/#installing-with-helm"
echo
echo "# To upgrade cert-manager"
echo "# see https://cert-manager.io/docs/installation/upgrading/"

RESOURCE_TEMPLATES=$SCRIPTPATH/resource-descriptor-templates
CLUSTER_ISSUER_TEMPLATES=$RESOURCE_TEMPLATES/cluster-issuers
CERT_TEMPLATES=$RESOURCE_TEMPLATES/certificates

RESOURCE_DESCRIPTORS=$SCRIPTPATH/resource-descriptors
CLUSTER_ISSUER_RESOURCES=$RESOURCE_DESCRIPTORS/cluster-issuers
CERT_RESOURCES=$RESOURCE_DESCRIPTORS/certificates

echo "### Create resource descriptors ###"
mkdir -p $CLUSTER_ISSUER_RESOURCES
customizeClusterIssuer "letsencrypt-prod-clusterissuer-dns01.yaml"
customizeClusterIssuer "letsencrypt-staging-clusterissuer-dns01.yaml"

mkdir -p $CERT_RESOURCES
createCertificateDescriptor "gateway" $NAMESPACE_TO_STORE_CERTIFICATES_IN

echo "### Apply cluster issuer resource descriptors ###"
kubectl apply -f $CLUSTER_ISSUER_RESOURCES/

echo "### Apply certificate resource descriptors ###"
kubectl apply -f $CERT_RESOURCES/

echo
echo "########## Clean-up ##########"

echo "### Delete customized resource descriptors ###"
rm -rf $RESOURCE_DESCRIPTORS
