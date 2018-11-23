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

public enum UserType {
    Normal("Normal"), SystemAdmin("SystemAdmin"), GroupAdmin("GroupAdmin");

    private final String type;

    private UserType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public static UserType fromString(String text) {
        if (text != null) {
            for (UserType userType : UserType.values()) {
                if (text.equalsIgnoreCase(userType.type)) {
                    return userType;
                }
            }
        }
        return null;
    }
}