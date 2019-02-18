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
package org.eclipse.kuksa.mil.service;

import java.util.List;

public class Email {

    private final List<String> to;

    private final String subject;

    private final String body;

    Email(List<String> to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public List<String> getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
