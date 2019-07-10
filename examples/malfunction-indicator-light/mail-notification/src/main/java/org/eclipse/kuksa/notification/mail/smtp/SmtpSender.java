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
package org.eclipse.kuksa.notification.mail.smtp;

import org.eclipse.kuksa.notification.mail.MailSender;
import org.eclipse.kuksa.notification.mail.entity.Mail;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.ServerConfig;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class SmtpSender implements MailSender {

    /* default std out logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpSender.class);

    /* smtp mail sender */
    private final Mailer mailer;

    /* mail address of the sender */
    private final String senderAddress;

    /* display name of the sender */
    private final String senderName;

    /**
     * Creates a new smtp sender instance.
     *
     * @param transport     transport strategy to connect
     * @param host          smtp host to connect to
     * @param port          port to use
     * @param user          user name to authenticate
     * @param password      password to authenticate
     * @param senderAddress email address of the sender
     * @param senderName    display name of the sender
     * @throws IllegalArgumentException thrown if strategy is out of scope
     */
    public SmtpSender(String transport,
                      String host,
                      int port,
                      String user,
                      String password,
                      String senderAddress,
                      String senderName) throws IllegalArgumentException {
        Objects.requireNonNull(transport);
        Objects.requireNonNull(host);
        Objects.requireNonNull(user);
        Objects.requireNonNull(password);
        Objects.requireNonNull(senderAddress);

        mailer = MailerBuilder
                .withTransportStrategy(getTransportStrategy(transport))
                .withSMTPServerHost(host)
                .withSMTPServerPort(port)
                .withSMTPServerUsername(user)
                .withSMTPServerPassword(password)
                .buildMailer();

        this.senderAddress = senderAddress;
        this.senderName = senderName != null ? senderName : senderAddress;
    }

    @Override
    public boolean testConnection() {
        ServerConfig config = mailer.getServerConfig();
        if (config == null) {
            LOGGER.error("The connection configuration is not set. Unable to test connection.");
            return false;
        }

        LOGGER.info("Testing to connect to {}:{}", config.getHost(), config.getPort());
        try {
            mailer.testConnection();
        } catch (Exception e) {
            LOGGER.error("Connection test failed.");
            e.printStackTrace();
            return false;
        }

        LOGGER.info("Connection test successful.");
        return true;
    }

    @Override
    public void sendEmail(Mail mail) {
        Email email = EmailBuilder.startingBlank()
                .from(senderName, senderAddress)
                .toMultiple(mail.getTo())
                .withSubject(mail.getSubject())
                .withHTMLText(mail.getBody())
                .buildEmail();

        mailer.sendMail(email, false);
    }

    /**
     * Returns the transport strategy enum for the given string.
     * Valid options are 'SMTP', 'SMTPS' and 'SMTP_TLS'.
     * String matching is case insensitive.
     *
     * @param s string to match to the enum
     * @return transport transfer enum matching the input
     * @throws IllegalArgumentException thrown if given string does not match any option
     */
    private static TransportStrategy getTransportStrategy(String s) throws IllegalArgumentException {
        switch (s.toUpperCase()) {
            case "SMTP":
                return TransportStrategy.SMTP;

            case "SMTPS":
                return TransportStrategy.SMTPS;

            case "SMTP_TLS":
                return TransportStrategy.SMTP_TLS;

            default:
                LOGGER.error("'{}' is an unsupported transfer protocol.", s);
                throw new IllegalArgumentException("Unsupported transfer protocol set. 'SMTP', 'SMTPS' and 'SMTP_TLS' available.");
        }
    }
}
