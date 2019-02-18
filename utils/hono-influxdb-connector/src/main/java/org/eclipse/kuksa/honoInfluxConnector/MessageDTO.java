/*
 * ******************************************************************************
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 *  Contributors:
 *      Johannes Kristan (Bosch Software Innovations GmbH) - initial API and functionality
 *      Leon Graser (Bosch Software Innovations GmbH)
 * *****************************************************************************
 */

package org.eclipse.kuksa.honoInfluxConnector;

import java.util.Map;

class MessageDTO {

    private final String deviceID;

    private final Map<String, Object> entries;

    MessageDTO(String deviceID, Map<String, Object> entries) {
        this.deviceID = deviceID;
        this.entries = entries;
    }

    Map<String, Object> getEntries() {
        return entries;
    }

    String getDeviceID() {
        return deviceID;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Device: ").append(getDeviceID());

        for (Map.Entry<String, Object> entry : getEntries().entrySet()) {
            sb.append("\n")
                    .append(entry.getKey())
                    .append(" : ")
                    .append(entry.getValue());

        }

        return sb.toString();
    }
}
