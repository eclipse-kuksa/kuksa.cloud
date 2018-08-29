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

public class AssignedResult {

	int assigned;
	int alreadyAssigned;
	int total;

	public int getAssigned() {
		return assigned;
	}

	public void setAssigned(int assigned) {
		this.assigned = assigned;
	}

	public int getAlreadyAssigned() {
		return alreadyAssigned;
	}

	public void setAlreadyAssigned(int alreadyAssigned) {
		this.alreadyAssigned = alreadyAssigned;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	@Override
	public String toString() {
		return "AssignedResult [assigned=" + assigned + ", alreadyAssigned=" + alreadyAssigned + ", total=" + total
				+ "]";
	}
}
