#
# ******************************************************************************
# Copyright (c) 2019 Bosch Software Innovations GmbH.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/index.php
#
# *****************************************************************************
#
#!/bin/bash
echo Send Telemetry

#insert the device id
DEVICE_ID=${1:-4711}
AUTHZ_ID="$DEVICE_ID"_auth

#insert password
AUTH_PWD=${2:-"verysecret"}

#insert address of hono HTTP adapter (e.g. 127.0.0.1:8080)
HONO_ADAPTER_HTTP_VERTX_ADDRESS=${3:-"127.0.0.1:8080"}

curl -X POST -i -u $AUTHZ_ID@DEFAULT_TENANT:$AUTH_PWD -H 'Content-Type: application/json' --data-binary '{"signal": 23}' http://$HONO_ADAPTER_HTTP_VERTX_ADDRESS/telemetry
echo