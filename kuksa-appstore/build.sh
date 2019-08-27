#!/bin/bash

######################################################################
# Copyright (c) 2019 Bosch Software Innovations GmbH [and others]
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
########################################################################

# Make the script fail if a command fails
set -e

echo
echo "##############################################################"
echo "##############################################################"
echo "############ Eclipse Kuksa Cloud app-store build #############"
echo "##############################################################"
echo "##############################################################"

echo
echo "########## Initialization ##########"

echo
echo "##### Build app-store with Maven #####"

mvn clean verify

# Needs the xq command, see https://github.com/kislyuk/xq
VERSION="$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)"
cp target/kuksa.appstore-$VERSION.jar kuksa-appstore.jar