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

# See https://hub.helm.sh/charts?q=cert-manager for versions and repositories
CERT_MANAGER_VERSION=0.6.2
CERT_MANAGER_CHART_VERSION=0.6.6 # In the stable repository, chart and app version diverge
CERT_MANAGER_HELM_REPOSITORY="stable"
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
echo "####################################################################################################"
echo "####################################################################################################"
echo "###############     Install Cert-Manager and Get Certificates from Let's Encrypt     ###############"
echo "####################################################################################################"
echo "####################################################################################################"
echo
echo "########## Initialization ##########"

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

echo "### Install cert-manager ###"
INSTALLED_CERT_MANAGER_CHART_VERSION="$(kubectl get deployments -n cert-manager cert-manager -o yaml --ignore-not-found=true \
	| yq .metadata.labels.chart \
	| tr -d '\"' \
	| sed -e 's/cert-manager-v//')"

if [[ "$INSTALLED_CERT_MANAGER_CHART_VERSION" == "$CERT_MANAGER_CHART_VERSION" ]]; then
	echo "cert-manager is up-to-date"
elif [[ "" != "$INSTALLED_CERT_MANAGER_CHART_VERSION" ]]; then
	# See
	# * https://helm.sh/docs/using_helm/#installing-helm
	# * https://docs.helm.sh/using_helm/#securing-your-helm-installation
	# * https://docs.cert-manager.io/en/latest/tasks/upgrading/index.html
	# Note that tiller is started, sent to the background and eventually terminated
	echo "Upgrading cert-manager using local tiller ..."
	tiller --storage=secret &
	export HELM_HOST=:44134
	kubectl apply \
		     -f https://raw.githubusercontent.com/jetstack/cert-manager/release-$CERT_MANAGER_VERSION/deploy/manifests/00-crds.yaml \
	    && echo "Waiting for custom resource definitions to be available ..." \
	    && until [[ "5" == "$(kubectl get CustomResourceDefinitions | grep 'certmanager.k8s.io' | wc -l)" ]]; do echo "Waiting ..."; sleep 3; done \
		&& helm repo update \
		&& kubectl label namespace cert-manager certmanager.k8s.io/disable-validation=true --overwrite=true \
		&& helm upgrade \
			--wait \
			--timeout 600 \
			--version v$CERT_MANAGER_CHART_VERSION \
			cert-manager \
			$CERT_MANAGER_HELM_REPOSITORY/cert-manager \
		&& kill "$(jobs -p %tiller)"
else
	# See
	# * https://helm.sh/docs/using_helm/#installing-helm
	# * https://docs.helm.sh/using_helm/#securing-your-helm-installation
	# * https://docs.cert-manager.io/en/latest/getting-started/install.html
	# Note that tiller is started, sent to the background and eventually terminated
	echo "Installing cert-manager using local tiller ..."
	tiller --storage=secret &
	export HELM_HOST=:44134
	helm init \
			--client-only \
			--override 'spec.template.spec.containers[0].command'='{/tiller,--storage=secret}' \
		&& kubectl apply \
			-f https://raw.githubusercontent.com/jetstack/cert-manager/v$CERT_MANAGER_VERSION/deploy/manifests/00-crds.yaml \
	    && echo "Waiting for custom resource definitions to be available ..." \
		&& until [[ "5" == "$(kubectl get CustomResourceDefinitions | grep 'certmanager.k8s.io' | wc -l)" ]]; do echo "Waiting ..."; sleep 3; done \
		&& createOrReuseNamespace cert-manager \
		&& kubectl label namespace cert-manager certmanager.k8s.io/disable-validation=true --overwrite=true \
		&& helm repo add jetstack https://charts.jetstack.io \
		&& helm repo update \
		&& helm install \
			--wait \
			--timeout 600 \
		    --name cert-manager \
		    --namespace cert-manager \
		    --version v$CERT_MANAGER_CHART_VERSION \
		    $CERT_MANAGER_HELM_REPOSITORY/cert-manager \
		&& kill "$(jobs -p %tiller)"
fi

echo "### Apply cluster issuer resource descriptors ###"
kubectl apply -f $CLUSTER_ISSUER_RESOURCES/

echo "### Apply certificate resource descriptors ###"
kubectl apply -f $CERT_RESOURCES/

echo
echo "########## Clean-up ##########"

echo "### Delete customized resource descriptors ###"
rm -rf $RESOURCE_DESCRIPTORS
