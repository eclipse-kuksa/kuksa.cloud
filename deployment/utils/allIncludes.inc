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

# Sources all includes, relative to SCRIPTPATH

. $SCRIPTPATH/../utils/ipAddress/configureStaticIpAddress.inc
. $SCRIPTPATH/../utils/ipAddress/getDnsNameVariable.inc
. $SCRIPTPATH/../utils/ipAddress/getIpAddressesFile.inc
. $SCRIPTPATH/../utils/ipAddress/getIpAddressVariable.inc
. $SCRIPTPATH/../utils/ipAddress/writeDnsName.inc
. $SCRIPTPATH/../utils/ipAddress/writeIpAddress.inc

. $SCRIPTPATH/../kubernetes/namespace/createOrReuseNamespace.inc

. $SCRIPTPATH/../kubernetes/secret/createOrReuseDockerRegistrySecret.inc
