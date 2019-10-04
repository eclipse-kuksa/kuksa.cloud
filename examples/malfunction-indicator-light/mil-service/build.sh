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

VERSION="$(gradle properties -q | grep 'version:' | awk '{print $2}')"

echo
echo "##############################################################"
echo "##############################################################"
echo "########## Eclipse Kuksa Cloud mil-service build #############"
echo "##############################################################"
echo "##############################################################"


echo
echo "##### Build with Gradle #####"
gradle clean bootJar

cp build/libs/indicator-light-service-${VERSION}.jar indicator-light-service.jar
