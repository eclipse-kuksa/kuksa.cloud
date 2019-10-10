/*
 * Keycloak
 */
output "KEYCLOAK_ENDPOINT" {
  value = "https://${azurerm_public_ip.ingress_ip.fqdn}/auth"
  description = "Keycloak endpoint"
}

/*
 * Kubernetes
 */
output "K8S_INGRESS_FQDN" {
  value = "${azurerm_public_ip.ingress_ip.fqdn}"
  description = "Kubernetes Ingress FQDN"
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
