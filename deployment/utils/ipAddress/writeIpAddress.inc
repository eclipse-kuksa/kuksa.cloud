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

# Writes the static IP address of a service to the IP addresses file.
#
# Arguments
# 1. Service name
# 2. Static IP address

function writeIpAddress() {
	local SERVICE_NAME=$1
	local STATIC_IP_ADDRESS=$2
	
	local IP_ADDRESS_VARIABLE=`getIpAddressVariable $SERVICE_NAME`
	local IP_ADDRESSES_FILE=`getIpAddressesFile`
	
	# The first commands 1h;2,$H;$!d;g; read the entire file into memory,
	# to permit the following multi-line expression to operate on the
	# entire file, see https://unix.stackexchange.com/a/235016
	sed -i "1h;2,\$H;\$!d;g;s/export $IP_ADDRESS_VARIABLE='[0-9.]*'\n//" $IP_ADDRESSES_FILE
	echo "export $IP_ADDRESS_VARIABLE='$STATIC_IP_ADDRESS'" >> $IP_ADDRESSES_FILE
}