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
The scripts in this directory and it's subdirectories help to setup a deployment of the Kuksa cloud. These scripts assume a running Kubernetes cluster which can be configured using `kubectl`. More information regarding the parameters of the scripts can be found within the respective script file.

## Structure
The deployment scripts are divided into the following parts:
  1. **Eclipse hawkBit** enables the deployment of the corresponding software update components, in partilucar the update server.  
  **Note that this step requires the installation of the command line tool kompose. Installation instructions can be found at http://kompose.io/**
  2. **Eclipse Hono** enables the deployment of a messaging infrastructure. 
  3. **Kubernetes** provides functions for the Kubernetes deployment of the Kuksa cloud. 
  4. **Utils** scripts that are included by other parts of the deployment infrastructure (e.g. handling static IP-addresses for the services). It is possible to set static IP-addresses and DNS entries for deployed services. For more details on that configuration see the `Readme.md` file in the `utils` directory.
