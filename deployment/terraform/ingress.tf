# create ingress ip
resource "azurerm_public_ip" "ingress_ip" {
  name                = "${var.INSTANCE_NAME}${local.ENVIRONMENT}ip"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  allocation_method   = "Static"
  domain_name_label   = "${var.INSTANCE_NAME}${local.ENVIRONMENT}"

  tags = {
    project = "kuksa"
    instance = var.INSTANCE_NAME
    environment = local.ENVIRONMENT
  }
}
