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
echo "Init Device"

#insert the device id (e.g. 4711)
DEVICE_ID=${1:-4711}
AUTH_ID="$DEVICE_ID"_auth

#insert the password you want to use for the device credentials
PWD=${2:-"verysecret"}

#insert the address of the Hono Device Registry (e.g. "http://127.0.0.1:8080")
HONO_DEVICE_REGISTRY_ADDRESS=${3:-http://127.0.0.1:8080}

#add device
echo
echo "Add Device"
curl -X POST -i -H 'Content-Type: application/json' -d '{"device-id": "'$DEVICE_ID'"}' $HONO_DEVICE_REGISTRY_ADDRESS/registration/DEFAULT_TENANT

# Add credentials
echo
echo "Add credentials"
PWD_HASH=$(echo -n $PWD | openssl dgst -binary -sha512 | base64 -w 0)
curl -X POST -i -H 'Content-Type: application/json' -d '{"device-id": "'$DEVICE_ID'", "type": "hashed-password", "auth-id": "'$AUTH_ID'", "secrets": [{"pwd-hash":  "'$PWD_HASH'", "hash-function": "sha-512", "not-after": "2020-03-31T00:00:00+01:00"}]}' $HONO_DEVICE_REGISTRY_ADDRESS/credentials/DEFAULT_TENANT

#get device information
echo
echo "Get device information"
curl -X GET -i $HONO_DEVICE_REGISTRY_ADDRESS/registration/DEFAULT_TENANT/$DEVICE_ID
echo
