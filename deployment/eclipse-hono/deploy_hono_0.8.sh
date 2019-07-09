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

# Make the script fail if a command fails
set -e

SCRIPTPATH=$(dirname "$(readlink -f "$0")")

CERTS_PATH=$1

HONO_VERSION=0.8
HONO_MIRROR="https://download.eclipse.org/hono/eclipse-hono-deploy-$HONO_VERSION.tar.gz"

. $SCRIPTPATH/../kubernetes/includes/allIncludes.inc

echo
echo "###########################################################"
echo "###########################################################"
echo "###############     Eclipse Kuksa Cloud     ###############"
echo "############### Eclipse Hono $HONO_VERSION deployment ###############"
echo "###########################################################"
echo "###########################################################"
echo
echo "########## Initialization ##########"

cd $SCRIPTPATH

echo
echo "##### Download Eclipse Hono binaries #####"
wget $HONO_MIRROR

echo
echo "##### Uncompress the binaries #####"
tar -zxvf eclipse-hono-deploy-$HONO_VERSION.tar.gz
HONO_PATH=$SCRIPTPATH/eclipse-hono-deploy-$HONO_VERSION
DEMO_CERTS_PATH=$HONO_PATH/config/hono-demo-certs-jar
KUBERNETES_PATH=$HONO_PATH/deploy/kubernetes
SPRING_CONFIG_PATH=$KUBERNETES_PATH
DESCRIPTORS_PATH=$HONO_PATH/deploy/resource-descriptors

echo "##### Add custom certificates #####"
if [[ "" != "$CERTS_PATH" ]]; then
	echo "Copying custom certificates from $CERTS_PATH to $DEMO_CERTS_PATH"
	cp $CERTS_PATH/* $DEMO_CERTS_PATH/
else
	echo "No custom certificates specified. Will use the default certificates."
fi

echo 
echo "########## Replace the relevant parts in the Kubernetes deployment configuration files from the Hono release ##########"

echo 
echo "##### NodePort to ClusterIP ######"
# When using an Istio gateway, the (unencrypted) services should not be available from outside.
# Hence, the service type is switched to ClusterIP.
# ClusterIP does not support nodePort specifications, so they are removed.

function configureClusterIP() {
	local FILE=$1

	sed -i 's/NodePort/ClusterIP/' $FILE
	# The first commands 1h;2,$H;$!d;g; read the entire file into memory,
	# to permit the following multi-line expression to operate on the
	# entire file, see https://unix.stackexchange.com/a/235016 
	# The final g matches globally, i.e. covers multiple non-overlapping occurrences
	sed -i "1h;2,\$H;\$!d;g;s# *nodePort: [0-9]* *\n##g" $FILE
}

configureClusterIP $DESCRIPTORS_PATH/hono-adapter-http/hono-adapter-http-vertx-svc.yml
configureClusterIP $DESCRIPTORS_PATH/hono-adapter-kura/hono-adapter-kura-svc.yaml
configureClusterIP $DESCRIPTORS_PATH/hono-service-device-registry/hono-service-device-registry-svc.yaml
configureClusterIP $DESCRIPTORS_PATH/dispatch-router/dispatch-router-ext-svc.yaml
configureClusterIP $DESCRIPTORS_PATH/hono-adapter-amqp/hono-adapter-amqp-vertx-svc.yaml
configureClusterIP $DESCRIPTORS_PATH/hono-service-messaging/hono-service-messaging-svc.yaml
configureClusterIP $DESCRIPTORS_PATH/hono-adapter-mqtt/hono-adapter-mqtt-vertx-svc.yaml
configureClusterIP $DESCRIPTORS_PATH/grafana/grafana-svc.yaml

echo
echo "##### Change memory limits #####"
cd $DESCRIPTORS_PATH/grafana
sed -i 's/memory: \"64Mi\"/memory: \"128Mi\"/' grafana-deployment.yaml

# See https://github.com/eclipse/hono/issues/817 for further information
cd $DESCRIPTORS_PATH/hono-service-device-registry
sed -i 's/memory: \"256Mi\"/memory: \"512Mi\"/' hono-service-device-registry-deployment.yaml

cat hono-service-device-registry-deployment.yaml

cd $DESCRIPTORS_PATH/hono-service-auth
sed -i 's/memory: \"196Mi\"/memory: \"512Mi\"/' hono-service-auth-deployment.yaml

cat  hono-service-auth-deployment.yaml

echo
echo "##### Change routing pattern in qpid dispatch router #####"

# See https://qpid.apache.org/releases/qpid-dispatch-1.0.1/book/index.html#_message_routing for more information
# See https://jira.apache.org/jira/browse/DISPATCH-744
cd $SCRIPTPATH/eclipse-hono-deploy-$HONO_VERSION/deploy/kubernetes/qpid
sed -i 's/\"distribution\": \"balanced\"/\"distribution\": \"multicast\"/' qdrouterd-with-broker.json

cat qdrouterd-with-broker.json

echo
echo  "########## Kubernetes deployment ##########"

echo
cd $KUBERNETES_PATH
chmod +x kubernetes_deploy.sh
./kubernetes_deploy.sh

echo
ls -lah

echo
echo  "########## Final cleanup ##########"

echo
echo  "##### Delete the compressed binaries #####"
cd $SCRIPTPATH
rm eclipse-hono-deploy-$HONO_VERSION.tar.gz

echo
ls -lah

echo
echo  "##### Delete the uncompressed binaries #####"
cd $SCRIPTPATH
rm -rf eclipse-hono-deploy-$HONO_VERSION

echo
ls -lah
