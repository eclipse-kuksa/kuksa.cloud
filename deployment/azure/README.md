<!--
******************************************************************************
Copyright (c) 2019 Bosch Software Innovations GmbH [and others].
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php
***************************************************************************** 
-->

# Microsoft Azure Deployment
Scripts to provision on an Azure Kubernetes Service. 


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