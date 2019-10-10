# create ingress ip
resource "azurerm_public_ip" "ingress_ip" {
  name                = "kuksa${var.INSTANCE_NAME}${local.ENVIRONMENT}ip"
  location            = "${azurerm_resource_group.main.location}"
  resource_group_name = "${azurerm_resource_group.main.name}"

  allocation_method   = "Static"
  domain_name_label   = "kuksa-${var.INSTANCE_NAME}-${local.ENVIRONMENT}"

  tags = {
    project = "kuksa"
    instance = "${var.INSTANCE_NAME}"
    environment = "${local.ENVIRONMENT}"
  }
}

# ingress
resource "helm_release" "ingress" {
  name      = "ingress"
  chart     = "stable/nginx-ingress"
  namespace = "kube-system"
  timeout   = 1800

  set {
    name  = "controller.service.loadBalancerIP"
    value = "${azurerm_public_ip.ingress_ip.ip_address}"
  }
  set {
    name = "controller.service.annotations.\"service\\.beta\\.kubernetes\\.io/azure-load-balancer-resource-group\""
    value = "${azurerm_resource_group.main.name}"
  }
  set {
    name  = "rbac.create"
    value = "false"
  }
}
