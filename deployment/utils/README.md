<!--
******************************************************************************
Copyright (c) 2019 Bosch Software Innovations GmbH.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php
***************************************************************************** 
-->
# Common installation functionality

## Structure

This directory contains functions in identically-named scripts to be sourced (i.e. included)
in other scripts that configure and deploy the Kuksa cloud services.

## Static IP addresses
The directory `ipAddress` contains functions to manage static IP addresses for the services.

The functions `writeIpAddress` and `writeDnsName` write to the file `../exportStaticIpAddresses.inc` which contains static IP addresses and DNS names that `configureStaticIpAddress` reads to configure a static IP address for a service in a kubernetes resource descriptor YAML file. Above scripts are the authoritative source for the file format of the IP addresses file.

Assuming a service named `hono-adapter-http-vertx`, the file `exportStaticIpAddresses.inc` would contain the following two lines:  

	export HONO_ADAPTER_HTTP_VERTX_IP_ADDRESS='1.2.3.4'
	export HONO_ADAPTER_HTTP_VERTX_FULLY_QUALIFIED_DNS_NAME='hono-adapter-http-vertx-a.b.c.com'

