<!--
******************************************************************************
Copyright (c) 2021 Bosch.IO GmbH [and others].
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php
***************************************************************************** 
-->
# Azure specifics 

This directory contains files that are specific for deploying an instance of the Eclipse Kuksa.Cloud to an Azure Kubernetes Service (AKS) in Azure. In general, the aim during the development of the Eclipse Kuksa.Cloud is to be deployable in most Kubernetes environments. However, the contents of this directory can be used when one plans to use AKS anyway.  

There are the following sub-folders: 

* **Terraform**: Terraform templates that can be used to provision the required infrastructure in Azure including an AKS. 
* **AKS Scripts**: A set of further scripts that can be applied to an AKS after it has been provisioned (e.g., through Terraform). The scripts are mostly needed to ease the procurment process of new certificates through Let's Encrypt. 

