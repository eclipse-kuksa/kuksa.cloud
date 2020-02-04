#!/bin/bash

# Make the script fail if a command fails
set -e

if [[ -z "$1" || -z "$2" ]]; then
	echo "usage: <hawkbit password> <load balancer IP>"
	exit 1
fi

HAWKBIT_PASSWORD="$1"
LOAD_BALANCER_IP="$2"
NAMESPACE=hawkbit

yq --yaml-roundtrip --in-place ".config.secrets.spring.security.user.password = \"{noop}$HAWKBIT_PASSWORD\"" helm-values.yaml
yq --yaml-roundtrip --in-place "[ .[] | select(.path == \"/spec/loadBalancerIP\").value = \"$LOAD_BALANCER_IP\" ]" overlays/kuksa/patches/add_load_balancer_ip.yaml 

helm repo add eclipse-iot https://eclipse.org/packages/charts
helm repo update

helm template eclipse-hawkbit eclipse-iot/hawkbit \
  --values helm-values.yaml \
  --output-dir base
exit 1
kubectl create namespace $NAMESPACE

# Optionally check customization using: kubectl kustomize overlays/kuksa

# Apply resource descriptors produced by helm
kubectl apply --namespace $NAMESPACE --filename base/hawkbit/ --recursive=true

# Perform modifications not supported by the Helm chart on
# selected resources:
# * use a LoadBalancer service with a static IP address
# (comment his line if the modification is not desired)
kubectl apply --namespace $NAMESPACE --kustomize=overlays/kuksa
