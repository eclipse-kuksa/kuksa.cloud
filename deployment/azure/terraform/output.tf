/*
 * Ingress IP
 */
output "K8S_INGRESS_IP" {
  value = "${azurerm_public_ip.ingress_ip.ip_address}"
  description = "Kubernetes Ingress IP"
}

/*
 * Container Registry
 */
output "CR_ENDPOINT" {
  value       = "${azurerm_container_registry.main.login_server}"
  description = "Container Registry Endpoint"
}

output "CR_ADMIN_USERNAME" {
  value       = "${azurerm_container_registry.main.admin_username}"
  description = "Container Registry Admin Username"
}

output "CR_ADMIN_PASSWORD" {
  value       = "${azurerm_container_registry.main.admin_password}"
  description = "Container Registry Admin Password"
  sensitive   = true
}

/*
 * Resource Group
 */
output "RESOURCE_GROUP" {
  value = azurerm_resource_group.main.name
  description = "The name of the resource group where Terraform provisioned the resource to."
}

/*
 * Kubernetes Cluster
 */
output "K8S_CLUSTER_NAME" {
    value = azurerm_kubernetes_cluster.main.name
    description = "The name of the created Kubernetes cluster. This might be needed to get the credentials with az aks get-credentials"
}