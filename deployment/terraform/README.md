# Description

With this part of kuksa.cloud project it is possible to deploy a Keycloak server to Azure cloud platform secured with a 
valid Let's Encrypt certificate without any manual steps. Please keep in mind that there is no claim to use this deployment
in a productive setup.

## Used Technologies  
- Terraform
- Azure
- AKS
- Helm
- Let’s Encrypt
- Keycloak

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

**deploy infrastructure**
```sh
source ./prepare_environment.sh
terraform workspace select <dev, stage or prod>
terraform init -upgrade
terraform plan
terraform apply
```

**open url (e.g. for Linux systems)**
```sh
xdg-open "$(terraform output KEYCLOAK_ENDPOINT)"
```

# FAQ

- Why I'm getting an invalid certificate with Common Name "Kubernetes Ingress Controller Fake Certificate"?

    The problem may caused by requesting too many certificates for exact set of domains. Let’s Encrypt provides rate 
    limits to ensure fair usage by as many people as possible (see https://letsencrypt.org/docs/rate-limits/). There is 
    a Duplicate Certificate limit of 5 certificates per week. Please try again later or use a different environment (aka 
    stage) for deployment.
