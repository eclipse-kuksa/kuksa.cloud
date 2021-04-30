<!--
******************************************************************************
Copyright (c) 2019 Bosch Software Innovations GmbH.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php
***************************************************************************** 
-->
# Kuksa Cloud Deployment

The scripts in this directory and it's subdirectories help to setup a deployment of the Kuksa cloud. These scripts
assume a running Kubernetes cluster which can be configured using `kubectl`. More information regarding the parameters
of the scripts can be found within the respective script file.

## Overall structure

The scripts have been written to deploy to a Kubernetes cluster on Microsoft Azure. They might be modified to deploy
to other Kubernetes services.

The deployment scripts are divided into the following groups:

### Infrastructure

These services are required to make the other services work

  1. [Azure](azure/README.md) for Azure-specific configuration that provides the basis of Kubernetes. It includes
     cert-manager that obtains TLS certificates.
### Services

  1. [Eclipse Hono](eclipse-hono/README.md) enables the deployment of a messaging infrastructure
  1. [Eclipse hawkBit](eclipse-hawkbit/README.md) enables the deployment of the corresponding software update
     components, in particular the update server
  1. [Kuksa Appstore](../kuksa-appstore/README.md) has to be
     [built](eclipse-kuksa/utils/README.md#build-eclipse-kuksa-appstore) before is can be
     [deployed](eclipse-kuksa/utils/README.md#deploy-eclipse-kuksa-appstore)
  1. [Hono-Influxdb-Connector](../utils/hono-influxdb-connector/README.md) sends Hono telemetry data and events to
     InfluxDB.
  1. [MIL example](../examples/malfunction-indicator-light/README.md) an exemplary malfunction-indicator-light use case
  1. [Traccar](eclipse-kuksa/README.md#deploy-traccar-server) deploys a service for tracking cars
