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

# Reads a static IP address from an environment variable following a convention
# and updates a service configuration file.
#
# Note that it is assumed that the YAML file describes a single service only;
# the service name is not used to select a service from the file.
#
# Arguments
# 1. Path to Service configuration YAML file
# 2. Service name
function configureStaticIpAddress() {
	local SERVICE_YML_FILE=$1
	local SERVICE_NAME=$2
	
	# Load the static IP address from the IP addresses file
	# Create the variable name dynamically and safely as described in
	# https://stackoverflow.com/a/9715377
	IP_ADDRESS_VARIABLE=`getIpAddressVariable $SERVICE_NAME`
	local IP_ADDRESS="${!IP_ADDRESS_VARIABLE}"
	
	# Update the config file.
	if grep -q loadBalancerIP "$SERVICE_YML_FILE"; then
		# loadBalancerIP is configured in file
		if [[ -n "$IP_ADDRESS" ]]; then
			# IP address is available
			echo "Updating static IP address for $SERVICE_NAME to $IP_ADDRESS ..."
			# The first commands 1h;2,$H;$!d;g; read the entire file into memory,
			# to permit the following multi-line expression to operate on the
			# entire file, see https://unix.stackexchange.com/a/235016
			sed -i "1h;2,\$H;\$!d;g;s/  type: LoadBalancer\n  loadBalancerIP: [0-9.]\+/  type: LoadBalancer\n  loadBalancerIP: $IP_ADDRESS/" $SERVICE_YML_FILE
		else
			# no IP address is available
			echo "Removing static IP address for $SERVICE_NAME"
			# The first commands 1h;2,$H;$!d;g; read the entire file into memory,
			# to permit the following multi-line expression to operate on the
			# entire file, see https://unix.stackexchange.com/a/235016
			sed -i "1h;2,\$H;\$!d;g;s/  type: LoadBalancer\n  loadBalancerIP: [0-9.]\+/  type: LoadBalancer/" $SERVICE_YML_FILE
		fi
	else
		# no loadBalancerIP is configured in file
		if [[ -n "$IP_ADDRESS" ]]; then
			# IP address is available
			echo "Adding static IP address for $SERVICE_NAME to $IP_ADDRESS"
			sed -i "s/  type: LoadBalancer/  type: LoadBalancer\n  loadBalancerIP: $IP_ADDRESS/" $SERVICE_YML_FILE
		else
			# no IP address is available
			echo "No static IP address configured for $SERVICE_NAME"
			# Ignore
		fi
	fi
}