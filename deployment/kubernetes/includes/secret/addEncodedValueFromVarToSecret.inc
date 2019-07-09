#
# ******************************************************************************
# Copyright (c) 2019 Bosch Software Innovations GmbH [and others].
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/index.php
#
# *****************************************************************************
#

function addEncodedValueFromVarToSecret() {
	local VARIABLE_NAME=$1
	local SECRETS_DESCRIPTOR=$2
	
	# Create the variable name dynamically and safely as described in
	# https://stackoverflow.com/a/9715377
	VALUE=${!VARIABLE_NAME}
	
	ENCODED_VALUE=`echo "$VALUE" | base64 --wrap=0` # do not wrap lines
	echo "Adding $VARIABLE_NAME ..."
	sed -i 's/'"$VARIABLE_NAME"': .\+/'"$VARIABLE_NAME"': '"$ENCODED_VALUE"'/' $SECRETS_DESCRIPTOR 		
}