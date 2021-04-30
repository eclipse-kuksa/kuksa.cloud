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