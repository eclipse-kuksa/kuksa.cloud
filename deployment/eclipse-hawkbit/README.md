<!--
******************************************************************************
Copyright (c) 2019 Bosch Software Innovations GmbH.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php
***************************************************************************** 
-->
# Eclipse hawkBit Deployment

---

## Deploy from Docker Hub

* Relevant script:
  * `deploy_hawkbit_update_server.sh`
* Purpose:
  * Deploy the latest version of the Eclipse hawkBit update server that is available at Docker Hub (see https://hub.docker.com/r/hawkbit/hawkbit-update-server/).
  * The update server is deployed to the Kubernetes namespace `hawkbit`.
* Options:
  * Implicit: if a file `../exportStaticIpAddresses.inc` is available relative to the script, these static IP addresses will be configured for the services. See `../utils/README.md` if you want to create the file manually.
* The script performs the following steps:
  * Initialization
    * Checkout of the Eclipse hawkBit GitHub repository
  * Deployment script conversion
    * Convert Docker Compose files
      * At the point of creating the script, Eclipse hawkBit does not provide Kubernetes deployment files.
      * To enable a Kubernetes deployment the Docker Compose files are translated into Kubernetes resource files.
      * **Note that this step requires the installation of the command line tool kompose. Installation instructions can be found at http://kompose.io/**
    * Replace relevant parts in the Kubernetes resource files
      * Convert the hawkbit service into a LoadBalancer
      * Optionally configure static IP addresses for the LoadBalancer
  * Deployment
    * Create the `hawkbit` namespace
    * Deploy the Kubernetes resources
      * In this regard, the Kubernetes resource files created above are used.
  * Final cleanup
    * Delete the `hawkbit` directory