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

## Build Eclipse Kuksa Appstore

* Script:
  * `build_kuksa_appstore.sh`
* Purpose:
  * Build a Docker image for the Eclipse Kuksa Appstore and push it to a Docker registry
* Options:
  * `DOCKER_REGISTRY_SERVER`: Address of the Docker registry server, e.g. running on Microsoft Azure.
  * `DOCKER_REGISTRY_USERNAME`: Username to sign in to the Docker registry.
  * `DOCKER_REGISTRY_PASSWORD`: Password to sign in to the Docker registry.
  * `DOCKER_REGISTRY_EMAIL`: Email to sign in to the Docker registry.
* Stages:
  * Clone the Kuksa Git repository
  * Build with Maven
  * Build Docker image
  * Push Docker image to Docker registry
  * Final cleanup
    * Remove the clones Git repository


## Deploy Eclipse Kuksa Appstore

* Script:
  * `deploy_kuksa_appstore.sh`
* Purpose:
  * Deploys a Docker image for the Eclipse Kuksa Appstore on a Kubernetes cluster
* Options:
  * `APPSTORE_USERNAME`: The username to set for the admin user of the appstore
  * `APPSTORE_PASSWORD`: The password to set for the admin user of the appstore
  * `HAWKBIT_HOST`: The (cluster-internal) hostname of the hawkbit server to use in the appstore 
  * `HAWKBIT_PORT`: The (cluster-internal) port number of the hawkbit server to use in the appstore
  * `HAWKBIT_USERNAME`: The username to be used by the appstore to authenticate with
    hawkbit.
  * `HAWKBIT_PASSWORD`: The password to be used by the appstore to authenticate with 
    hawkbit.
  * `DOCKER_REGISTRY_SERVER`: Address of the Docker registry server, e.g. running on Microsoft Azure.
  * `DOCKER_REGISTRY_USERNAME`: Username to sign in to the Docker registry.
  * `DOCKER_REGISTRY_PASSWORD`: Password to sign in to the Docker registry.
  * `DOCKER_REGISTRY_EMAIL`: Email to sign in to the Docker registry.
  * `DOCKER_REGISTRY_SECRET`: Name of the secret to access to custom Docker registry. **Note that the secret is created during the deploy process within the namespace `extension`.**
  
* Notes:
  * Production mode is enabled for Vaadin.
  * H2 console is disabled.
  * The H2 database is persisted via a persistent volume claim.
  * The service uses a ClusterIP so it is only available behind the Ambassador gateway.

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
    * Create Docker registry secret, or re-use the existing one
    * Deploy the Kubernetes resources
  * Final cleanup
    * Remove the `target` folder