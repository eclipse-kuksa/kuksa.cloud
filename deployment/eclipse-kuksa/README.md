<!--
******************************************************************************
Copyright (c) 2019 Bosch Software Innovations GmbH [and others].
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php
***************************************************************************** 
-->

# Eclipse Kuksa Deployment

---

## Deploy Traccar Server

* Script:
  * `deploy_traccar.sh`
* Purpose:
  * Deploys a Docker image of [Traccar server](https://github.com/traccar/traccar) on a Kubernetes cluster
* Stages:
  * Deployment script conversion
    * Replace relevant parts in the Kubernetes resource files in a new `target` folder
  * Deployment
    * Create the `kuksa` namespace, or re-use the existing one
    * Deploy the Kubernetes resources
  * Final cleanup
    * Remove the `target` folder