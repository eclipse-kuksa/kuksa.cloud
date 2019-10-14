locals {
  keycloak_chart_dir = "${path.root}/charts/keycloak-configuration/"
}

#
# Creates a Keycloak authorization server.
# References: https://github.com/helm/charts/tree/master/stable/keycloak
#
resource "helm_release" "keycloak-configuration" {
  name    = "keycloak-configuration"
  chart   = "${local.keycloak_chart_dir}"
  timeout = 1800
  version = "0.1.0"

  set {
    name  = "Registry.Endpoint"
    value = "${azurerm_container_registry.main.login_server}"
  }

  set {
    name  = "Registry.Username"
    value = "${azurerm_container_registry.main.admin_username}"
  }

  set {
    name  = "Registry.Password"
    value = "${azurerm_container_registry.main.admin_password}"
  }

  set {
    name  = "Fqdn"
    value = "${azurerm_public_ip.ingress_ip.fqdn}"
  }

  set {
    name = "trigger1"
    value = "${sha1(file(format("%s/%s", local.keycloak_chart_dir, "templates/configmap.yaml")))}"
  }
  set {
    name = "trigger2"
    value = "${sha1(file(format("%s/%s", local.keycloak_chart_dir, "kuksa-realm.json")))}"
  }
}

data "helm_repository" "codecentric" {
    name = "codecentric"
    url = "https://codecentric.github.io/helm-charts"
}

resource "helm_release" "keycloak" {
  repository = "${data.helm_repository.codecentric.metadata.0.name}"
  name    = "keycloak"
  chart   = "keycloak"
  timeout = 1800
  version = "5.1.7"
  depends_on = [ "helm_release.keycloak-configuration" ]

  values  = [
    "${file("keycloak-values.yaml")}",
    "keycloak:\n  ingress:\n    hosts:\n    - ${azurerm_public_ip.ingress_ip.fqdn}\n    tls:\n      - hosts:\n        - ${azurerm_public_ip.ingress_ip.fqdn}\n        secretName: kuksa-secret"
  ]
}
