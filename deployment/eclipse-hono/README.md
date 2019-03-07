<!--
******************************************************************************
Copyright (c) 2019 Bosch Software Innovations GmbH.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php
***************************************************************************** 
-->
# Eclipse Hono Deployment

## Eclipse Hono 0.8

* Relevant script:
  * `deploy_hono_0.8.sh`
* Purpose:
  * Deploy version 0.8 of Eclipse Hono based on the Docker images
  * The deployment is conducted in the Kubernetes namespace `hono`.
* Options:
  * If a file `../exportStaticIpAddresses.inc` is available relative to the script, these static IP addresses will be configured for the services. See `../utils/README.md` if you want to create the file manually.
* Steps:
  * Initialization
    * Download the deployment scripts for version 0.8 of Eclipse Hono from https://download.eclipse.org/hono/eclipse-hono-deploy-0.8.tar.gz
    * Uncompress the binaries
  * Replace the relevant parts in the Kubernetes deployment configuration files
    * Convert all Kubernetes services of type `NodePort` to type `LoadBalancer`, to make them accessible outside the Kubernetes cluster.
    * If static IP addresses are available in a file `../utils/exportStaticIpAddresses.inc`, they are configured for the load balancer services.
    * Change memory limits of the `hono-service-device-registry` and the `hono-service-auth` deployments.
      * These deploymentes experienced frequent restarts of the corresponding pods.
      * The issue is described at https://github.com/eclipse/hono/issues/817
      * Therefore the corresponding memory limits are increased to 512 MiB.
    * Change routing pattern in qpid dispatch router
      * The vanilla version of version >= 0.7 of Eclipse Hono deploys a messaging network based on the Qpid Dispatch Router and the Artemis Broker.
      * Per default the Qpid dispatch router is configured to the routing pattern `balanced`.
      * This leads to the problem that multiple consumers receive messages sent via Eclipse Hono in a round robin manner.
      * To enable all consumers to receive all messages, the routing pattern is changed to `multicast`.
      * See https://qpid.apache.org/releases/qpid-dispatch-1.0.1/book/index.html#_message_routing for more information
  * Deployment of Kubernetes resources
  * Final cleanup
    * Delete the compressed binaries
    * Delete the uncompressed and modified binaries  
* Notes:
  * The scripts from Eclipse Hono for the Kubernetes deployment reuse parts of the deployment for OpenShift. Because of that, errors stating "no matches for kind "Route" in version "v1" " are an expected behavior.



### Undeployment

* Relevant script:
  * `undeploy_hono_0.8.sh`
* Purpose:
  * Undeploy the Eclipse Hono resources from the Kubernetes cluster
* Options:
  * None
* Steps:
  * Initialization
    * Download version 0.8 of the Eclipse Hono binaries from https://download.eclipse.org/hono/eclipse-hono-deploy-0.8.tar.gz
    * Uncompress the binaries
  * Undeployment of Kubernetes resources
    * Usage of the undeploy script that is provided by the Eclipse Hono team
    * **Note that the undeploy script deletes the namesspace `hono` including ALL resources it comprises.**
  * Final cleanup
    * Delete the compressed binaries
    * Delete the uncompressed binaries