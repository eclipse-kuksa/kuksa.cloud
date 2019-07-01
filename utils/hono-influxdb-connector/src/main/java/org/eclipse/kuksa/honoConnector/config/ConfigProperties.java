/*********************************************************************
 * Copyright (c) 2019 Bosch Software Innovations GmbH [and others]
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.kuksa.honoConnector.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="hono")
public class ConfigProperties {

	private List<ConnectionConfig> connections;

	public List<ConnectionConfig> getConnections() {
		return connections;
	}

	public void setConnections(final List<ConnectionConfig> connections) {
		this.connections = connections;
	}
	
}
