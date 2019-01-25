/*******************************************************************************
 * Copyright (C) 2018 Netas Telekomunikasyon A.S.
 *  
 *  This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *  
 * SPDX-License-Identifier: EPL-2.0
 *  
 * Contributors:
 * Adem Kose, Fatih Ayvaz and Ilker Kuzu (Netas Telekomunikasyon A.S.) - Initial functionality
 ******************************************************************************/
package org.eclipse.kuksa.appstore.model.hawkbit;

import java.util.List;

public class Distribution {

	String createdBy;
	String createdAt;
	String lastModifiedBy;
	String lastModifiedAt;
	String name;
	String description;
	String version;
	List<SoftwareModule> modules;
	boolean requiredMigrationStep;
	String type;
	boolean complete;
	boolean deleted;
	Integer id;

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getLastModifiedAt() {
		return lastModifiedAt;
	}

	public void setLastModifiedAt(String lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<SoftwareModule> getModules() {
		return modules;
	}

	public void setModules(List<SoftwareModule> modules) {
		this.modules = modules;
	}

	public boolean getRequiredMigrationStep() {
		return requiredMigrationStep;
	}

	public void setRequiredMigrationStep(boolean requiredMigrationStep) {
		this.requiredMigrationStep = requiredMigrationStep;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean getComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Distribution [createdBy=" + createdBy + ", createdAt=" + createdAt + ", lastModifiedBy="
				+ lastModifiedBy + ", lastModifiedAt=" + lastModifiedAt + ", name=" + name + ", description="
				+ description + ", version=" + version + ", modules=" + modules + ", requiredMigrationStep="
				+ requiredMigrationStep + ", type=" + type + ", complete=" + complete + ", deleted=" + deleted + ", id="
				+ id + "]";
	}

	public Distribution(String name, String description, String version, List<SoftwareModule> modules,
			boolean requiredMigrationStep, String type) {
		this.name = name;
		this.description = description;
		this.version = version;
		this.modules = modules;
		this.requiredMigrationStep = requiredMigrationStep;
		this.type = type;
	}

	public Distribution(String description, String version) {
		this.description = description;
		this.version = version;
	}

	public Distribution() {
		super();
	}
}
