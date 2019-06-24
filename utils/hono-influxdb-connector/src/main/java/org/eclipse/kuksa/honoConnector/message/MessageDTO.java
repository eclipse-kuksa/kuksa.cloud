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

package org.eclipse.kuksa.honoConnector.message;

import java.util.Map;

/**
 * Data transfer object for a Hono message {@link org.apache.qpid.proton.message.Message}.
 */
public class MessageDTO {

    /* ID of the device sending the message */
    private final String deviceID;

    /* mapping of objects within the message body */
    private final Map<String, Object> entries;

    /**
     * Creates a new message dto with the given device ID and mapping of the
     * message content.
     *
     * @param deviceID device ID that sent the message
     * @param entries  mapping of strings to the respective objects of that message
     */
    public MessageDTO(String deviceID, Map<String, Object> entries) {
        this.deviceID = deviceID;
        this.entries = entries;
    }

    /**
     * Returns the mapping of keys to objects contained in this message.
     *
     * @return object mappings
     */
    public Map<String, Object> getEntries() {
        return entries;
    }

    /**
     * Returns the device ID that sent the message.
     *
     * @return device ID
     */
    public String getDeviceID() {
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
