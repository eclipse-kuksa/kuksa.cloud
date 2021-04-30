<!--
******************************************************************************
Copyright (c) 2021 Bosch.IO GmbH [and others].
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php
***************************************************************************** 
-->
# Description

With this part of the Eclipse Kuksa.Cloud project it is possible to deploy the required infrastructure for the Kuksa.Cloud to Azure. Please keep in mind that there is no claim to use this deployment in a productive setup. 

## Used Technologies  
- Terraform
- Azure
- AKS

# How-to use

**clone kuksa.cloud repository**
```sh
git clone https://github.com/eclipse/kuksa.cloud.git
cd kuksa.cloud/deployment/terraform
```

**login to Azure and set subscription**
```sh
az login
az account set --subscription <ID>
```

**prepare infrastructure configuration**
```sh
cp ./terraform.tfvars.template ./terraform.tfvars
```
Edit ```./terraform.tfvars``` according your Azure subscription.   

**configure Terraform backend**

This deployment stores the state of in a storage in Azure. The storage is realized through a Terraform backend which needs to be configured in the `main.tf`. Nore information can be found under: https://www.terraform.io/docs/backends/types/azurerm.html. If you want to store the Terraform locally, you need to adapt the `main.tf`

**deploy infrastructure**
You may select to use Terraform workspace with: 
```terraform workspace select <dev, stage or prod>
```
In either case, one then can proceed running:
```sh
terraform init -upgrade
terraform plan
terraform apply
```

# Remark regarding Cloud provider support
One requirment for the development of the Kuksa cloud is too enable operators to choose the Cloud environment that they want to use. Because of that we try to stick to a Kubernetes cluster as the main requirement for running the Kuksa cloud. For more details see the other directories in the `deployment` directory (especially the deployment in `helm`). However, the provisioning of the required resources and infrastructure remains very specific to the used Cloud provider. The intention of this Terraform deployment is therefore to support the creation of this infrastrucutre in Azure. If you are using another Cloud you need to manually create the required infrastructure and access rights (Kubernetes cluster, container registry and an optional public IP-address) or adapt this Terraform deployment accordingly. We are of course happy for contributions to support further Cloud providers.

# FAQ

- Why I'm getting an invalid certificate with Common Name "Kubernetes Ingress Controller Fake Certificate"?

    The problem may caused by requesting too many certificates for exact set of domains. Let’s Encrypt provides rate 
    limits to ensure fair usage by as many people as possible (see https://letsencrypt.org/docs/rate-limits/). There is 
    a Duplicate Certificate limit of 5 certificates per week. Please try again later or use a different environment (aka 
    stage) for deployment.
