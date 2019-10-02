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
  description = "Path to Kube Config File"
}

variable "K8S_HELM_HOME" {
  description = "Path to Helm Home Directory"
}
