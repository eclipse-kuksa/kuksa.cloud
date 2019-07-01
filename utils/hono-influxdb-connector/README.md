<!--
******************************************************************************
Copyright (c) 2017 Bosch Software Innovations GmbH.

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php

Contributors:
     Johannes Kristan (Bosch Software Innovations GmbH) - initial API and functionality
*****************************************************************************
-->

# Hono-InfluxDB-Connector

This project contains a Spring-Boot Application that connects a running Hono instance with a running InfluxDB instance
so that messages received by Hono can be stored in InfluxDB. This is especially useful if one wants
to easily create a visualization of some measurements eg with Grafana.

## Build 

To build the Hono-InfluxDB-Connector navigate to its directory.

`cd utils/hono-influxdb-connector`

This project is build using Gradle and has been tested with [Gradle v5.0](https://github.com/gradle/gradle/releases/tag/v5.0.0).

To build with Gradle, create a Docker image and push the image to a Docker registry, 
run

```bash
./build.sh \
    <Hostname of the Docker registry> \
    <Username to log into the Docker registry> \
    <Password to log into the Docker registry>
```

## Deployment

To deploy in a Kubernetes cluster, run

```bash
./deploy.sh \
    <Cluster-internal URL of InfluxDB, typically http://influxdb.hono:8086> \
    <Cluster-internal hostname of QPID dispatch router, typically hono-dispatch-router-ext.hono> \
    <Cluster-internal port of QPID dispatch router, typically 5671> \
    <Hono user, typically consumer@HONO> \
    <Password of Hono user> \
    <Path to truststore, typically ./src/main/resources/trusted-certs.pem> \
    <Whether or not to verify the hostname for TLS connection to Hono, should be true> \
    <Connections between Hono and InfluxDB, e.g. DEFAULT_TENANT:devices,otherTenant:nameOfOtherInfluxDatabase> \
    <Name of the Kubernetes secret to store the Docker registry credentials in> \
    <Hostname of the Docker registry> \
    <Username to log into the Docker registry> \
    <Password to log into the Docker registry> \
    <E-mail address to log into the Docker registry>
```

## Configuration

To configure the connector either edit the [application.properties](src/main/resources/application.properties) file or set the respective environment variables.
If an environment variable is set it will overwrite the .properties value.
The table below lists all configuration parameter available.

|properties                              |environment variable                 |description                                                 |
|:---------------------------------------|:------------------------------------|:-----------------------------------------------------------|
|influxdb.url                            |INFLUXDB_URL                         |url of the influxDB instance to connect to                  |
|influxdb.db.name                        |INFLUXDB_DB_NAME                     |name fo the database to write to                            |
|qpid.router.host                        |QPID_ROUTER_HOST                     |url to the instance of the qpid dispatch router             |
|qpid.router.port                        |QPID_ROUTER_PORT                     |port to the instance of the qpid dispatch router            |
|hono.tenant.id                          |HONO_TENANT_ID                       |tenant id used by Hono                                      |
|hono.user                               |HONO_USER                            |username to authenticate with Hono                          |
|hono.password                           |HONO_PASSWORD                        |password to authenticate with Hono                          |
|hono.trustedStorePath                   |HONO_TRUSTEDSTOREPATH                |path to the .pem file to connect to Hono                    |
|hono.verifyHostname                     |HONO_VERIFYHOSTNAME                  |`true` to ensure that qpid.router.host is in TLS certificate|
|hono.connections\[0\]                   |-                                    |you may specify >= 1 connections (see below)                |
|hono.connections\[0\].tenantId          |HONO_CONNECTIONS_0_TENANTID          |tenant id used to read from Hono (array element)            |
|hono.connections\[0\].influxDatabaseName|HONO_CONNECTIONS_0_INFLUXDATABASENAME|name of the database to write to (array element)            |


The default Hono username, password and .pem file for the different Hono versions can be found on the [Hono website](https://www.eclipse.org/hono/).
