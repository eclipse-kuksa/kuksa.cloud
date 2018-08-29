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
package org.eclipse.kuksa.appstore.model;

public class Rule {

	RuleMain maintenanceWindow;
	String forcetime;
	String id;
	String type;

	@Override
	public String toString() {
		return "RuleResource [maintenanceWindow=" + maintenanceWindow + ", forcetime=" + forcetime + ", id=" + id
				+ ", type=" + type + "]";
	}

	public RuleMain getMaintenanceWindow() {
		return maintenanceWindow;
	}

	public void setMaintenanceWindow(RuleMain maintenanceWindow) {
		this.maintenanceWindow = maintenanceWindow;
	}

	public String getForcetime() {
		return forcetime;
	}

	public void setForcetime(String forcetime) {
		this.forcetime = forcetime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
