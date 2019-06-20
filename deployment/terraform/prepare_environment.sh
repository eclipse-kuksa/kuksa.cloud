#!/bin/bash

# assert sourcing only
[[ "${BASH_SOURCE[0]}" = "$0" ]] && echo "Usage: source $0" && exit 1

# check if prereqs are available
if command -v docker > /dev/null;
then
  echo "Docker found. Continue..."
else
  echo "Docker not found. Please install Docker!"
  return
fi
if command -v helm > /dev/null;
then
  echo "Helm found. Continue..."
else
  echo "Helm not found. Please install Helm!"
  return
fi
if command -v kubectl > /dev/null;
then
  echo "Kubectl found. Continue..."
else
  echo "Kubectl not found. Please install Kubectl!"
  return
fi
if command -v terraform > /dev/null;
then
  echo "Terraform found. Continue..."
else
  echo "Terraform not found. Please install Terraform!"
  return
fi

# project root path
if [ -n "$ZSH_VERSION" ]; then
  PROJECT_ROOT="$( cd "$( dirname "${(%):-%x}" )" >/dev/null && pwd )"
elif [ -n "$BASH_VERSION" ]; then
  PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
else
  PROJECT_ROOT="$( cd "$( dirname "$0" )" >/dev/null && pwd )"
fi

# use this kubernetes and helm configuration
mkdir -p $PROJECT_ROOT/.kube
mkdir -p $PROJECT_ROOT/.helm
export KUBE_CONFIG="$PROJECT_ROOT/.kube/config"
export HELM_HOME="$PROJECT_ROOT/.helm"

echo "KUBECONFIG=$KUBE_CONFIG"
echo "HELM_HOME=$HELM_HOME"

# export basic paths to terraform
export TF_VAR_K8S_KUBE_CONFIG="$KUBE_CONFIG"
export TF_VAR_K8S_HELM_HOME="$HELM_HOME"
