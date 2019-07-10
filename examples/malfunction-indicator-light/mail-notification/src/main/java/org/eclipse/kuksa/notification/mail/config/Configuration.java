/*
 * ******************************************************************************
 * Copyright (c) 2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * *****************************************************************************
 */
package org.eclipse.kuksa.notification.mail.config;

import org.springframework.stereotype.Component;

@Component
public class Configuration {

    private final SmtpConfig smtpConfig;

    private final SenderConfig senderConfig;

    public Configuration(SmtpConfig smtpConfig, SenderConfig senderConfig) {
        this.smtpConfig = smtpConfig;
        this.senderConfig = senderConfig;
    }

    public SmtpConfig getSmtpConfig() {
        return smtpConfig;
    }

    public SenderConfig getSenderConfig() {
        return senderConfig;
    }
}
