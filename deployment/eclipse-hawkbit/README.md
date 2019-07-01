<!--
******************************************************************************
Copyright (c) 2019 Bosch Software Innovations GmbH [and others].
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
* Stages:
  * Initialization
    * Checkout of the Eclipse hawkBit github repository
  * Deployment script conversion
    * Convert Docker Compose files
      * At the point of creating the script, Eclipse hawkBit does not provide Kubernetes deployment files.
      * To enable a Kubernetes deployment the Docker Compose files are translated into Kubernetes resource files.
      * **Note that this step requires the installation of the command line tool kompose. Installation instructions can be found at http://kompose.io/**
  * Deployment
    * Create the `hawkbit` namespace
    * Deploy the Kubernetes resources
      * In this regard, the Kubernetes resource files created above are used.
  * Final cleanup
    * Delete the `hawkbit` directory

---

## Build and Deploy Update Server from Source

* Relevant script:
  * `build_and_deploy_hawkbit_update_server.sh`
* Purpose:
  * Build and deploy a version of the Eclipse hawkBit Update Server based on the source files from the repository. The version is specified in the script and shall be the latest version.
  * This deployment file is restricted to a specific version, since there are differences regarding file paths between the different tags.
  * The original motivation for building the Update Server from source was the replacement of the default credentials.
  * **Note that the execution of the script requires a custom Docker registry server.**
  * The Kubernetes resources are deployed to the namespace `hawkbit`.
* Options:
  * `HAWKBIT_MAJOR_VERSION`: The version to build. The latest version for which deployment files have been released in a directory at https://github.com/eclipse/hawkbit/tree/master/hawkbit-runtime/docker, e.g. `0.2.5`.
  * `HAWKBIT_USERNAME`: New username for logging in to sign in to the hawkBit Update Server.
  * `HAWKBIT_PASSWORD`: New password for logging in to sign in to the hawkBit Update Server.
  * `AZURE_DOCKER_REGISTRY_SECRET`: Name of the secret to access to custom Docker registry. **Note that the secret is created during the build and deploy process within the namespace `hawkbit`.**
  * `AZURE_DOCKER_REGISTRY_SERVER`: Address of the Docker registry server, preferrably running on Microsoft Azure.
  * `AZURE_DOCKER_REGISTRY_USERNAME`: Username to sign in to the Docker registry.
  * `AZURE_DOCKER_REGISTRY_PASSWORD`: Password to sign in to the Docker registry.
  * `AZURE_DOCKER_REGISTRY_EMAIL`: Email to sign in to the Docker registry.
* Stages:
  * Initialization
    * Clone the Eclipse hawkBit git repository from https://github.com/eclipse/hawkbit.git
    * Create intermediate directories for the targets
    * Copy Docker deployment files to the corresponding target directories
    * Checkout version 0.2.2 to prepare the build process
  * Build and push the Docker image to the custom Docker registry
    * Replace the old username and password with the new credentials
      * **This might be a place to debug**
    * Build the project using Maven
    * Modify the Dockerfile of version 0.2.2
      * Copy the `.jar` file created as a result of the Maven build
      * Delete the retrieval of the pre-built
    * Build the Docker image for version 0.2.2
    * Push the Docker image to Docker registry running on Microsoft Azure
    * Deployment script conversion
      * Convert Docker Compose files
        * At the point of creating the script, Eclipse hawkBit does not provide Kubernetes deployment files.
        * To enable a Kubernetes deployment the Docker Compose files are translated into Kubernetes resource files.
        * **Note that this step requires the installation of the command line tool kompose. Installation instructions can be found at http://kompose.io/**
      * Deployment
        * Create the `hawkbit` namespace
        * Create Docker registry secret
        * Deploy the Kubernetes resources
          * In this regard, the Kubernetes resource files created above are used.
      * Final cleanup
        * Delete the `hawkbit` directory
        * Delete the target directories created during initialization
