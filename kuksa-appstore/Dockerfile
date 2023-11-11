######################################################################
######################################################################
# Copyright (c) 2019 Bosch Software Innovations GmbH [and others]
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
########################################################################

#######################################################
##################### Stage 1 #########################
#######################################################

FROM maven:3.6-jdk-11 as build

COPY pom.xml /tmp/
COPY build.sh /tmp/
COPY src /tmp/src/

WORKDIR /tmp/

RUN chmod +x build.sh
RUN bash build.sh

#######################################################
##################### Stage 2 #########################
#######################################################

FROM openjdk:8-jre-alpine

LABEL maintainer="The Eclipse Kuksa project"
LABEL project="Eclipse Kuksa"

EXPOSE 8080

COPY --from=build /tmp/target/*.jar /kuksa-appstore.jar

ENTRYPOINT ["java", "-jar", "/kuksa-appstore.jar"]

