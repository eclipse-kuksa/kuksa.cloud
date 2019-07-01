#
# ******************************************************************************
# Copyright (c) 2019 Bosch Software Innovations GmbH [and others].
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/index.php
#
# *****************************************************************************
#

function createOrReuseNamespace() {
	local NAMESPACE=$1
	
	if [[ "" == "$(kubectl get namespace $NAMESPACE --ignore-not-found=true)" ]]; then
		kubectl create namespace $NAMESPACE
	else
		echo "The namespace $NAMESPACE exists already and will be re-used."
	fi
}