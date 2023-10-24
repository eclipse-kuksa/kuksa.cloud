<!--
******************************************************************************
Copyright (c) 2018 Dortmund University of Applied Sciences and Arts

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php

Contributors:
    Robert Hoettger - initial readme files added
*****************************************************************************
-->

# THIS REPOSITORY HAS BEEN ARCHIVED, see [issue 187](https://github.com/eclipse/kuksa.cloud/issues/187) for details.


# Eclipse Kuksa-Cloud
[![Eclipse Public License 2.0](https://img.shields.io/badge/license-EPL--2.0-green.svg "Eclipse Public License 2.0")](LICENSE)

![Eclipse Kuksa](logos/kuksa.png "Eclipse Kuksa Logo")

## Content

* [HonoInfluxDBConnector](utils/hono-influxdb-connector/README.md): A Spring-Boot application that connects a running Eclipse Hono instance with a running InfluxDB instance so that messages received by Hono can be stored in InfluxDB. This is especially useful if one wants to easily create a visualization of some measurements eg with Grafana.
* [AppStore](kuksa-appstore/README.md): A Spring-Boot application that connects a running Eclipse HawkBit instance and provides a GUI to manage vehicle (and eventually cloud) applications
* [Deployment](deployment/README.md) instructions and scripts to setup the overall cloud architecture

More information on deployment / usage is given in each modules' subdirectories.

## License

Licensed under the [EPL-2.0](LICENSE) license.
