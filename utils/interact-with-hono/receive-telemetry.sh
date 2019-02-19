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
#This script requires a working installation of the cli-proton-python receiver. 
#For further information on how to install that library please visit https://cli-proton-python.readthedocs.io/en/latest/)


#insert Hono client username (e.g. consumer@HONO)
HONO_CLIENT_USERNAME=${1:-"consumer@HONO"}

#insert Hono client password (e.g. verysecret)
HONO_CLIENT_PASSWORD=${2:-"verysecret"}

#insert the address of Hono dispatch router (hono-dispatch-router-ext, e.g. 127.0.0.1:5672) 
HONO_DISPATCH_ROUTER_ADDRESS=${3:-"127.0.0.1:5672"}


#insert number of telmetry messages to receive
NUMBER_OF_TELEMETRY_MESSAGE="1"
#insert logging format of the cli-proton-python-receiver (choices=['dict', 'body', 'upstream', 'none', 'interop', 'json'])
LOGGING_FORMAT="body"

cli-proton-python-receiver --broker-url $HONO_CLIENT_USERNAME:$HONO_CLIENT_PASSWORD@$HONO_DISPATCH_ROUTER_ADDRESS/telemetry/DEFAULT_TENANT --count $NUMBER_OF_TELEMETRY_MESSAGE --log-msgs $LOGGING_FORMAT
echo