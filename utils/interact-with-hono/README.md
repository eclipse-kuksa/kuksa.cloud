<!--
******************************************************************************
Copyright (c) 2019 Bosch Software Innovations GmbH.

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php

***************************************************************************** 
-->
# Scripts for the interaction with Hono

These scripts are intended to provide help for the basic interaction with the Hono. A good introduction for the interaction with Hono can be found under https://www.eclipse.org/hono/getting-started/ . All scripts except the receive-telemetry.sh use of the Hono HTTP adapter and require an installation of cURL (https://curl.haxx.se/). 

This directory contains the following scripts:
* init-device.sh: Used to register a new device with hono. This includes setting credentials for the communication and must be done before sending data to the Hono protocol adapter.
* remove-device.sh: Removes a device from Hono.
* send-telemetry.sh: Send telemetry data to Hono.
* receive-telemetry.sh: Receive telemetry data from the Hono dispatch router. To run this script a installation of of the cli-proton-python receiver is required. For further information on how to install that library please visit https://cli-proton-python.readthedocs.io/en/latest/