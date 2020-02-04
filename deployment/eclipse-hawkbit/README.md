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

## Deploy using Eclipse packages project

Install
[Eclipse hawkBit](https://www.eclipse.org/hawkbit/) on a
[Kubernetes](https://kubernetes.io/) cluster using 
the [Eclipse hawkBit Helm chart](https://github.com/eclipse/packages/tree/master/charts/hawkbit)
from the [Eclipse packages](https://github.com/eclipse/packages/) project.

### Prerequisites

* [Helm 3](https://github.com/helm/helm/releases)
* [kubectl >= 1.17.0](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
* [yq >= 2.10.0](https://github.com/kislyuk/yq) is required.

### Installing

To deploy [hawkBit](https://www.eclipse.org/hawkbit/) with a given admin password
and for a static IP address, run 

```bash
./deploy_hawkbit.sh <HAWKBIT_PASSWORD> <STATIC_IP_ADDRESS>
```

### Design decisions

* We use kustomize add a static IP address to the load balancer, because the
  helm chart lacks that option.
* The `--values hawkbit-values.yaml` option is used because the `{noop}`
  prefix of the password, which is required by Spring could not be set using
  the `--set` or `--set-string` options.
* We use the version of [kustomize that is integrated into kubectl](https://github.com/kubernetes-sigs/kustomize/#kubectl-integration)
