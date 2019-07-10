######################################################################
# Copyright (c) 2019 Bosch Software Innovations GmbH [and others]
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
########################################################################

FROM openjdk:8-jre-alpine

LABEL maintainer="The Eclipse Kuksa project"
LABEL project="Eclipse Kuksa"

EXPOSE 8080

COPY kuksa-appstore.jar /kuksa-appstore.jar

ENTRYPOINT ["java", "-jar", "/kuksa-appstore.jar"]