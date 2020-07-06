resource "azuread_application" "aks" {
  name = "${var.INSTANCE_NAME}-${local.ENVIRONMENT}-aks"
}

# service principal for aks
resource "azuread_service_principal" "aks" {
  application_id = azuread_application.aks.application_id
}

resource "random_string" "aks-principal-secret" {
  length  = 30
  special = true
}

resource "azuread_service_principal_password" "aks" {
  service_principal_id = azuread_service_principal.aks.id
  value                = random_string.aks-principal-secret.result
  end_date             = "2100-01-01T00:00:00Z"
}

resource "azurerm_role_assignment" "aks-network-contributor" {
  scope                = azurerm_resource_group.main.id
  role_definition_name = "Network Contributor"
  principal_id         = azuread_service_principal.aks.id
}

# kubernetes cluster
resource "azurerm_kubernetes_cluster" "main" {
  name                = "${var.INSTANCE_NAME}${local.ENVIRONMENT}aks"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  depends_on = [
    azurerm_role_assignment.aks-network-contributor,
    azurerm_public_ip.ingress_ip
  ]

  dns_prefix         = "${var.INSTANCE_NAME}${local.ENVIRONMENT}"

  default_node_pool {
    name            = "default"
    node_count      = 1
    vm_size         = "Standard_D3_v2"
    os_disk_size_gb = 30
  }

  service_principal {
    client_id     = azuread_application.aks.application_id
    client_secret = azuread_service_principal_password.aks.value
  }

  tags = {
    project = "example"
    instance = "${var.INSTANCE_NAME}"
    environment = "${local.ENVIRONMENT}"
  }
}