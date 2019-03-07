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

# Creates the name of the variable to read/write the DNS name of a service
# and writes it to DNS_NAME_VARIABLE.
#
# Arguments
# 1. Service name

function getDnsNameVariable() {
	local SERVICE_NAME=$1
	
	# Replace - by _ and convert to ALL_CAPS
	local ALL_CAPS_SERVICE_NAME=`echo $SERVICE_NAME | tr "-" "_" | tr [a-z] [A-Z]`
	echo "${ALL_CAPS_SERVICE_NAME}_FULLY_QUALIFIED_DNS_NAME"
}