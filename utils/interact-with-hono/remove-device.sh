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
echo "Remove Device:"

#insert the device id (e.g. 4711)
DEVICE_ID=${1:-4711}
AUTH_ID="$DEVICE_ID"_auth

#insert the address of the Hono Device Registry (e.g. "http://127.0.0.1:8080")
HONO_DEVICE_REGISTRY_ADDRESS=${2:-http://127.0.0.1:8080}

#remove credentials
echo "Remove Credentials:"
curl -X DELETE -i -H 'Content-Type: application/json' -d '{"type": "hashed-password", "auth-id": "'$AUTH_ID'"}' $HONO_DEVICE_REGISTRY_ADDRESS/credentials/DEFAULT_TENANT/$DEVICE_ID

#remove/deregister the device
echo "Deregister the device:"
curl -X DELETE -i $HONO_DEVICE_REGISTRY_ADDRESS/registration/DEFAULT_TENANT/$DEVICE_ID
echo