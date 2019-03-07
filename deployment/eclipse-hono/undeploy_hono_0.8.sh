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

echo
echo "###########################################################"
echo "###########################################################"
echo "#############      Eclipse Kuksa Cloud      ###############"
echo "############# Eclipse Hono $HONO_VERSION undeployment ###############"
echo "###########################################################"
echo "###########################################################"
echo
echo "########## Initialization ##########"

cd $SCRIPTPATH

echo
echo "##### Download Eclipse Hono binaries #####"
wget https://download.eclipse.org/hono/eclipse-hono-deploy-$HONO_VERSION.tar.gz


echo
echo "##### Uncompress the binaries #####"
tar -zxvf eclipse-hono-deploy-$HONO_VERSION.tar.gz

echo
echo  "########## Kubernetes undeployment ##########"

echo
cd $SCRIPTPATH/eclipse-hono-deploy-$HONO_VERSION/deploy/kubernetes
chmod +x kubernetes_undeploy.sh
./kubernetes_undeploy.sh

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

