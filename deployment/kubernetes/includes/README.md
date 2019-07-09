<!--
******************************************************************************
Copyright (c) 2019 Bosch Software Innovations GmbH [and others].
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v2.0
which accompanies this distribution, and is available at
https://www.eclipse.org/org/documents/epl-2.0/index.php
***************************************************************************** 
-->

# Kubernetes installation functionality

---

## Structure

This directory contains functions in identically-named scripts to be sourced (i.e. included)
in other scripts that configure and deploy Kubernetes services.

## Kubernetes

### namespace
The function `createOrReuseNamespace` creates a namespace if it does not already exist in the Kuberentes cluster that `kubectl` points to.

### secret
The function `createOrReuseDockerRegistrySecret' creates a new secret to access a Docker registry. This secret is needed to pull Docker images from an external Docker registry. The secret can the be used in the respective deployments.
