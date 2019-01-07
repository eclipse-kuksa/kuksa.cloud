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

## General information

This project contains a Spring-Boot Application that connects a running Hono instance with a running InfluxDB instance
so that messages received by Hono can be stored in InfluxDB. This is especially usefull if one wants
to easily create a visualization of some measurements eg with Grafana.

To start the application run

`mvn spring-boot:run`

from the command line.

To configure the connection details you can either change the file

`src/main/resources/application.properties`

or provide environment variables for the configuration parameters (its standard Spring behavior but currently untested).

## Configuration

This section describes how to configure the hono-influxdb-connector to operate on the idial.institute hono instance.

**Install dependencies**

```
apt install maven
```

**Modify properties**

Edit `src/main/resources/application.properties`

and change the content to

<!--- TODO to be changed to Azure Subscription --->

```
influxdb.url=http://idial.institute:8086
influxdb.db.name=devices
qpid.router.host=idial.institute
qpid.router.port=15671
hono.tenant.id=DEFAULT_TENANT
hono.user=user1@HONO
hono.password=pw
hono.trustedStorePath=trusted-certs.pem
hono.reconnectAttempts=100
```

Depending on the used Eclipse Hono version, it may be further necessary to replace the `src/main/resources/trusted-certs.pem` with a suitable certificate (cf. https://www.eclipse.org/hono/download/)

**Start the consumer**

Enter `mvn spring-boot:run`

or (if you prefer to execute the consumer as background task)

`screen mvn spring-boot:run`

## Known issues

- The connector reconnects to Hono as often as defined in the `hono.reconnectAttempts` defined in the properties.
Still though sometimes the connector is unable to reconnect.
In this case the connector has to be restarted (there is currently no way to reconnect it otherwise).
