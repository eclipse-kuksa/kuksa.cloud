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
 * ******************************************************************************
 */

package org.eclipse.kuksa.honoConnector.message;

/**
 * Interface to wrap the message processing of incoming messages from Hono.
 */
public interface MessageHandler {

    /**
     * Processes the incoming message dto received from Hono.
     *
     * @param msg message dto to process
     */
    @Deprecated
    void process(final MessageDTO msg);

    /**
     * Processes the incoming telemetry message dto received from Hono.
     *
     * @param msg message dto to process
     */
    void processTelemetry(final MessageDTO msg);

    /**
     * Processes the incoming event message dto received from Hono.
     * @param msg
     */
    void processEvent(final MessageDTO msg);


    /**
     * Closes the message handler. Needs to be called before exit.
     */
    void close();
}
