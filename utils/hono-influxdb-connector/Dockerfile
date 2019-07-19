# Copyright Bosch Software Innovations GmbH [and others], 2019. Part of the Eclipse Kuksa Project.
#
# All rights reserved. This configuration file is provided to you under the
# terms and conditions of the Eclipse Distribution License v1.0 which
# accompanies this distribution, and is available at
# http://www.eclipse.org/org/documents/edl-v10.php
FROM openjdk:8-jre-alpine

ARG VERSION

COPY build/libs/hono-influxdb-connector-${VERSION}.jar /hono-influxdb-connector.jar

ENTRYPOINT ["java","-jar", "hono-influxdb-connector.jar"]
