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

public class ConnectionConfig {

	private String tenantId;
	private String influxDatabaseName;

	public ConnectionConfig() {
	}

	public ConnectionConfig(final String tenantId, final String influxDatabaseName) {
		this.tenantId = tenantId;
		this.influxDatabaseName = influxDatabaseName;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getInfluxDatabaseName() {
		return influxDatabaseName;
	}

	public void setInfluxDatabaseName(String influxDatabaseName) {
		this.influxDatabaseName = influxDatabaseName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((influxDatabaseName == null) ? 0 : influxDatabaseName.hashCode());
		result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConnectionConfig other = (ConnectionConfig) obj;
		if (influxDatabaseName == null) {
			if (other.influxDatabaseName != null)
				return false;
		} else if (!influxDatabaseName.equals(other.influxDatabaseName))
			return false;
		if (tenantId == null) {
			if (other.tenantId != null)
				return false;
		} else if (!tenantId.equals(other.tenantId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Connection [tenantId=" + tenantId + ", influxDatabaseName=" + influxDatabaseName + "]";
	}

}
