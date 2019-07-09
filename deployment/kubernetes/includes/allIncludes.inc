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

# Sources all includes, relative to SCRIPTPATH

. $SCRIPTPATH/../kubernetes/includes/namespace/createOrReuseNamespace.inc

. $SCRIPTPATH/../kubernetes/includes/secret/addEncodedValueFromVarToSecret.inc
. $SCRIPTPATH/../kubernetes/includes/secret/createOrReuseDockerRegistrySecret.inc
