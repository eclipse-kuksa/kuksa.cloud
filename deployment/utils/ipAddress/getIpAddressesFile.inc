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

# Creates the name of the file that contains static IP addresses
# based on SCRIPTPATH.

function getIpAddressesFile() {
	echo $(readlink -f $SCRIPTPATH/../exportStaticIpAddresses.inc)
}