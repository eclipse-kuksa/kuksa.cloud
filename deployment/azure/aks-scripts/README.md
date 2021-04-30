<!--
******************************************************************************
Copyright (c) 2021 Bosch.IO GmbH [and others].
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php
***************************************************************************** 
-->

# Microsoft Azure Deployment
Scripts to provision on an Azure Kubernetes Service to ease the certificate handling for the services in the cluster. 


## Allocate a static IP address and DNS names

* Script:
  * `allocate_static_ip_address_and_dns_names.sh`
* Purpose:
  * Get a static IP address for a gateway service to perform TLS termination
    and DNS names that point to it. The IP address and the DNS names are
    saved to `cloud-deployment/exportStaticIpAddresses.inc` to be used by deployment scripts.
    Static IPs remain stable across service re-deployments.
* Options:
  * `RESOURCE_GROUP_NAME`: Name of the resource group to create the static IP address 
    in. This should be the zone to which the AKS is deployed. Note that the resource
    group has to be created prior to executing the script.
  * `CLUSTER_NAME`: Name of the AKS cluster.
  * `ZONE_NAME`: Name of the DNS zone to add DNS names to via AzureDNS.
  * `ZONE_RESOURCE_GROUP_NAME`: Optional: Name of the resource group that contains 
    the DNS zone. It needs to be set if it differs from above `RESOURCE_GROUP_NAME`.
    If it is not given, the value of the `RESOURCE_GROUP_NAME` parameter will be used.
* Stages:
  * Allocate static IP addresses
    * Get static IP addresses via `az network public-ip create` - this is idempotent
  * Allocate DNS names
    * Add DNS record sets via `az network dns recprd-set a ad-record` this will fail 
      if the record already exists. Records will not be removed automatically.


## Create and deploy TLS certificates

* Script:
  * `create_and_deploy_certificates.sh`
* Purpose:
  * Install [cert-manager](https://cert-manager.io/docs/) for AzureDNS
  * Add certificate resources to cert-manager
  * Use [Let's encrypt](https://letsencrypt.org/) [dns01 challenge](https://cert-manager.io/docs/tutorials/acme/dns-validation/)
    to issue and automatically renew certificates via cert-manager
* Options:
  * `RESOURCE_GROUP_NAME`: Name of the resource group, to which the AKS is deployed. Note that the resource group has to create prior to executing the script.
  * `CLUSTER_NAME`: Name of the AKS cluster.
  * `AZURE_SUBSCRIPTION_ID`: The ID of the Azure subscription to use, as included in 
    `az account list`.
  * `AZURE_AD_TENANT_ID`: The ID of the tenant of the Azure subscription to use, as included in 
    `az account list`.
  * `APP_ID_OF_SERVICE_PRINCIPAL_WITH_ACCESS_TO_AZURE_DNS`: The App ID of a service 
    principal that is (only) able to access Azure DNS for the DNS Zone to be used for 
    the dns01 challenge. See https://docs.cert-manager.io/en/latest/tasks/acme/configuring-dns01/azuredns.html 
    on how to create it.
  * `NAME_OF_SECRET_OF_SERVICE_PRINCIPAL_WITH_ACCESS_TO_AZURE_DNS`: The variable name 
    says it. The secret should reside in the `cert-manager` namespace. See above link
    for instructions.
  * `DNS_ZONE_NAME`: The name of the DNS zone to use for the dns01 challenge and for 
    hosting the services. The name of the DNS zone is a domain name.
  * `EMAIL_ADDRESS_FOR_LETS_ENCRYPT`: The e-mail address to use when contacting Let's 
    encrypt.
  * `LETS_ENCRYPT_ENVIRONMENT`: The Let's encrypt environment to use - either `staging` 
    or `prod`. `prod` yielts certificates signed by a CA trusted by most browsers and 
    systems but has quotas that might be exceeded when trying a configuration. Use 
    the `staging` environment to try out configurations. Note that the staging environment 
    yielts certificates signed by a CA that is not trusted by browsers/systems. You 
    will need to provide the CA certificate of that CA to establish trust:
    https://letsencrypt.org/certs/fakelerootx1.pem.
  * `ZONE_RESOURCE_GROUP_NAME`: Optional: Name of the resource group that contains 
    the DNS zone. It needs to be set if it differs from above `RESOURCE_GROUP_NAME`.
    If it is not given, the value of the `RESOURCE_GROUP_NAME` parameter will be used.
* Stages:
  * Create resource descriptors for cert-manager cluster issuers and certificates
  * Install cert-manager using a tiller started by this script. If the script fails,
    tiller will not be stopped. You will need to find it using `ps aux | grep tiller` 
    and kill it manually.
  * Install cluster-issuers and certificates
  * Cert-manager will then start to issue the certificates.
  * Use `kubectl get certificates`, `kubectl get orders`, `kubectl get challenges`
    to list certificates, the orders created from certificates and the challenges 
    created from orders. `kubectl describe ...` lists events for certificates et 
    al. Use `kubectl logs -f ...` for the `cert-manager-...` pod in the `cert-manager
    namespace to see certificate issueing errors or progress.
  * Cert-manager will eventually create the secret mentioned in a certificate to
    store the issued certificate and private key.
* Troubleshooting:
  * When you switched a certificate from the staging to the prod cluster issuer, you
    need to remove the secret that was created for the staging certificate to trigger
    a in initial issuing of a production certificate.
