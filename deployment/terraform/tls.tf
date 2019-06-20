locals {
  letsencrypt_chart_dir = "${path.root}/charts/letsencrypt/"
}

# cert-manager
resource "helm_release" "cert-manager" {
  name      = "cert-manager"
  chart     = "stable/cert-manager"
  version   = "v0.5.2"
  namespace = "kube-system"
  timeout   = 1800
  depends_on = [ "helm_release.ingress" ]

  set {
    name  = "ingressShim.defaultIssuerName"
    value = "letsencrypt"
  }
  set {
    name  = "ingressShim.defaultIssuerKind"
    value = "ClusterIssuer"
  }
}

# letsencrypt
resource "helm_release" "letsencrypt" {
  name      = "letsencrypt"
  chart     = "${local.letsencrypt_chart_dir}"
  namespace = "kube-system"
  timeout   = 1800
  depends_on = [ "helm_release.cert-manager" ]

  set {
    name  = "ENVIRONMENT"
    value = "${local.ENVIRONMENT}"
  }

  set {
    name = "trigger1"
    value = "${sha1(file(format("%s/%s", local.letsencrypt_chart_dir, "templates/clusterissuer.yaml")))}"
  }

  set {
    name = "trigger2"
    value = "${sha1(file(format("%s/%s", local.letsencrypt_chart_dir, "templates/certificate.yaml")))}"
  }
}
