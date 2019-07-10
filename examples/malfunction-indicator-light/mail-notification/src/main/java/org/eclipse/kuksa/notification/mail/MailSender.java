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
package org.eclipse.kuksa.notification.mail;

import org.eclipse.kuksa.notification.mail.entity.Mail;

public interface MailSender {

    /**
     * Tests the connection to make sure emails can be sent.
     *
     * @return true if connection is established, false on error
     */
    boolean testConnection();

    /**
     * Sends the email passed as argument to the list of receivers defined
     * in the {@link Mail#getTo()} list.
     *
     * @param mail mail object to send
     */
    void sendEmail(Mail mail);
}
