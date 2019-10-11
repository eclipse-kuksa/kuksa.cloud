locals {
  ENVIRONMENT = "${terraform.workspace}"
}

variable "INSTANCE_NAME" {
  description = "DNS-compatible Instance Name"
}

variable "REGION" {
  description = "Azure Region"
}

variable "SUBSCRIPTION_ID" {
  description = "Azure Subscription ID"
}

variable "K8S_KUBE_CONFIG" {
  description = "Path to Kube Config File (can be created through prepare environment)"
}

variable "K8S_HELM_HOME" {
  description = "Path to Helm Home Directory (e.g. under $HOME/.helm) and can be created through prepare environment.sh"
}
