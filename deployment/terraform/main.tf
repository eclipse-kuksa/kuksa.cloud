terraform {
  backend "azurerm" {
    resource_group_name  = ""
    storage_account_name = ""
    container_name       = ""
    key                  = ""
    access_key           = ""
  }
}

# Configure the Microsoft Azure Provider
provider "azurerm" {
  subscription_id = "${var.SUBSCRIPTION_ID}"
}

provider "azuread" {
}

# helm provider
provider "helm" {
  debug = true
  home  = "${var.K8S_HELM_HOME}"
  kubernetes {
    config_path = "${local_file.kube_config.filename}"
  }
}

# Resource Group
resource "azurerm_resource_group" "main" {
  name     = "kuksa-${var.INSTANCE_NAME}-${local.ENVIRONMENT}"
  location = "${var.REGION}"

  tags = {
    project = "kuksa"
    instance = "${var.INSTANCE_NAME}"
    environment = "${local.ENVIRONMENT}"
  }
}

# container registry
resource "azurerm_container_registry" "main" {
  name                = "kuksa${var.INSTANCE_NAME}${local.ENVIRONMENT}cr"
  resource_group_name = "${azurerm_resource_group.main.name}"
  location            = "${azurerm_resource_group.main.location}"

  admin_enabled = true
  sku = "Basic"

  tags = {
    project = "kuksa"
    instance = "${var.INSTANCE_NAME}"
    environment = "${local.ENVIRONMENT}"
  }

  # perform docker login
  provisioner "local-exec" {
    command = "sleep 5 && echo '${azurerm_container_registry.main.admin_password}' | docker login '${azurerm_container_registry.main.login_server}' --username '${azurerm_container_registry.main.admin_username}' --password-stdin"
    interpreter = [ "bash", "-c" ]
  }
}
