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

HONO_VERSION=0.8
HONO_MIRROR="https://download.eclipse.org/hono/eclipse-hono-deploy-$HONO_VERSION.tar.gz"

. $SCRIPTPATH/../utils/allIncludes.inc

echo
echo "###########################################################"
echo "###########################################################"
echo "##########          Eclipse Kuksa Cloud          ##########"
echo "########## Eclipse Hono $HONO_VERSION deployment ##########"
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

echo 
echo "########## Replace the relevant parts in the Kubernetes deployment configuration files from the Hono release ##########"

echo 
echo "##### NodePort to LoadBalancer ######"

echo $SCRIPTPATH
HONO_PATH=$SCRIPTPATH/eclipse-hono-deploy-$HONO_VERSION
DESCRIPTORS_PATH=$SCRIPTPATH/eclipse-hono-deploy-$HONO_VERSION/deploy/resource-descriptors

echo 
grep -rnw $HONO_PATH -e 'type: NodePort'

cd $DESCRIPTORS_PATH/hono-adapter-http
sed -i 's/NodePort/LoadBalancer/' hono-adapter-http-vertx-svc.yml

cd $DESCRIPTORS_PATH/hono-adapter-kura
sed -i 's/NodePort/LoadBalancer/' hono-adapter-kura-svc.yaml

cd $DESCRIPTORS_PATH/hono-service-device-registry
sed -i 's/NodePort/LoadBalancer/' hono-service-device-registry-svc.yaml

cd $DESCRIPTORS_PATH/dispatch-router
sed -i 's/NodePort/LoadBalancer/' dispatch-router-ext-svc.yaml

cd $DESCRIPTORS_PATH/hono-adapter-amqp
sed -i 's/NodePort/LoadBalancer/' hono-adapter-amqp-vertx-svc.yaml

cd $DESCRIPTORS_PATH/hono-service-messaging
sed -i 's/NodePort/LoadBalancer/' hono-service-messaging-svc.yaml

cd $DESCRIPTORS_PATH/hono-adapter-mqtt
sed -i 's/NodePort/LoadBalancer/' hono-adapter-mqtt-vertx-svc.yaml

cd $DESCRIPTORS_PATH/grafana
sed -i 's/NodePort/LoadBalancer/' grafana-svc.yaml

cd $DESCRIPTORS_PATH/influx
echo "  type: LoadBalancer" >> influxdb-svc.yaml

echo 
grep -rnw $SCRIPTPATH/eclipse-hono-deploy-$HONO_VERSION -e 'type: LoadBalancer'

echo 
echo "##### Configure static IP addresses ######"

IP_ADDRESSES_FILE=`getIpAddressesFile`
if [[ -f $IP_ADDRESSES_FILE ]]; then
	echo "Loading IP addresses from $IP_ADDRESSES_FILE ..."
	. $IP_ADDRESSES_FILE
	configureStaticIpAddress "$DESCRIPTORS_PATH/hono-adapter-http/hono-adapter-http-vertx-svc.yml" "hono-adapter-http-vertx"
	configureStaticIpAddress "$DESCRIPTORS_PATH/hono-adapter-kura/hono-adapter-kura-svc.yaml" "hono-adapter-kura"
	configureStaticIpAddress "$DESCRIPTORS_PATH/hono-service-device-registry/hono-service-device-registry-svc.yaml" "hono-service-device-registry"
	configureStaticIpAddress "$DESCRIPTORS_PATH/dispatch-router/dispatch-router-ext-svc.yaml" "hono-dispatch-router-ext"
	configureStaticIpAddress "$DESCRIPTORS_PATH/hono-adapter-amqp/hono-adapter-amqp-vertx-svc.yaml" "hono-adapter-amqp-vertx"
	configureStaticIpAddress "$DESCRIPTORS_PATH/hono-service-messaging/hono-service-messaging-svc.yaml" "hono-service-messaging"
	configureStaticIpAddress "$DESCRIPTORS_PATH/hono-adapter-mqtt/hono-adapter-mqtt-vertx-svc.yaml" "hono-adapter-mqtt-vertx"
	configureStaticIpAddress "$DESCRIPTORS_PATH/grafana/grafana-svc.yaml" "grafana"
	configureStaticIpAddress "$DESCRIPTORS_PATH/influx/influxdb-svc.yaml" "influxdb"
else
	echo "No static IP addresses will be configured because file is missing: $IP_ADDRESSES_FILE"
fi

echo
echo "##### Change memory limits #####"
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
cd $SCRIPTPATH/eclipse-hono-deploy-$HONO_VERSION/deploy/kubernetes
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
