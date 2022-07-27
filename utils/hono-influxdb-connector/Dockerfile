# Copyright Bosch Software Innovations GmbH [and others], 2019. Part of the Eclipse Kuksa Project.
#
# All rights reserved. This configuration file is provided to you under the
# terms and conditions of the Eclipse Distribution License v1.0 which
# accompanies this distribution, and is available at
# http://www.eclipse.org/org/documents/edl-v10.php

#######################################################
##################### Stage 1 #########################
#######################################################

From gradle:7.4.2-jdk11-alpine AS BUILD
RUN mkdir -p /home/gradle/src/
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

#######################################################
##################### Stage 2 #########################
#######################################################

FROM gcr.io/distroless/java:11
#COPY --from=BUILD build/libs/hono-influxdb-connector-*.jar /hono-influxdb-connector.jar
COPY --from=BUILD /home/gradle/src/build/libs/*-0.2.0.jar /hono-influxdb-connector.jar
ENTRYPOINT ["java","-jar", "hono-influxdb-connector.jar"]



