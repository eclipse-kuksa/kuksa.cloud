terraform {
  backend "azurerm" {
    resource_group_name  = ""
    storage_account_name = ""
    container_name       = ""
    key                  = ""
  }
}

# Configure the Microsoft Azure Provider
provider "azurerm" {
  subscription_id = var.SUBSCRIPTION_ID
  features {}
}

provider "azuread" {
}

# Resource Group
resource "azurerm_resource_group" "main" {
  name     = "${var.INSTANCE_NAME}-${local.ENVIRONMENT}"
  location = var.REGION

  tags = {
    project = "kuksa"
    instance = var.INSTANCE_NAME
    environment = local.ENVIRONMENT
  }
}

# container registry
resource "azurerm_container_registry" "main" {
  name                = "kuksa${var.INSTANCE_NAME}${local.ENVIRONMENT}cr"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location

  admin_enabled = true
  sku = "Basic"

  tags = {
    project = "kuksa"
    instance = "${var.INSTANCE_NAME}"
    environment = "${local.ENVIRONMENT}"
  }
}
