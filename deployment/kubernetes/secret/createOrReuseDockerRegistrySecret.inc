#
# ******************************************************************************
# Copyright (c) 2019 Bosch Software Innovations GmbH.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/index.php
#
# *****************************************************************************
#
function createOrReuseDockerRegistrySecret() {
	local NAME_OF_SECRET=$1
	local NAMESPACE=$2
	local DOCKER_REGISTRY_SERVER=$3
	local DOCKER_REGISTRY_USERNAME=$4
	local DOCKER_REGISTRY_PASSWORD=$5
	local DOCKER_REGISTRY_EMAIL=$6
	
	if [[ "" == "$(kubectl get secret $NAME_OF_SECRET --namespace $NAMESPACE --ignore-not-found=true)" ]]; then
		kubectl create secret docker-registry $NAME_OF_SECRET \
			--docker-server=$DOCKER_REGISTRY_SERVER \
			--docker-username=$DOCKER_REGISTRY_USERNAME \
			--docker-password=$DOCKER_REGISTRY_PASSWORD \
			--docker-email=$DOCKER_REGISTRY_EMAIL \
			--namespace=$NAMESPACE
	else
		echo "The secret $NAME_OF_SECRET exists already and will be re-used."
	fi
}