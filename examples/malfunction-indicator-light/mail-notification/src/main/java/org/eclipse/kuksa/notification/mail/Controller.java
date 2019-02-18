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

import org.eclipse.kuksa.notification.mail.config.Configuration;
import org.eclipse.kuksa.notification.mail.entity.Mail;
import org.eclipse.kuksa.notification.mail.smtp.SmtpSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.ConnectException;

@RestController
public class Controller {

    /* default std out logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    /* email sender to process incoming messages */
    private final MailSender sender;

    /**
     * Creates a new REST controller for the mail notification service.
     *
     * @param config configuration to use to connect
     * @throws ConnectException thrown if test connection fails
     */
    public Controller(Configuration config) throws ConnectException {
        // create an smtp sender
        sender = new SmtpSender(
                config.getSmtpConfig().getTransport(),
                config.getSmtpConfig().getHost(),
                config.getSmtpConfig().getPort(),
                config.getSmtpConfig().getUsername(),
                config.getSmtpConfig().getPassword(),
                config.getSenderConfig().getMail(),
                config.getSenderConfig().getName()
        );

        // test the connection to verify the settings
        if (!sender.testConnection()) {
            throw new ConnectException("Failed to connect to smtp server.");
        }
    }

    /**
     * Sends an email from the address given in
     * {@link Controller#Controller(Configuration)}
     * using the mail passed in the body of the POST call.
     *
     * @param mail mail object to send
     * @return response of the request
     */
    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestBody Mail mail) {
        if (mail == null) {
            return ResponseEntity.badRequest().body("No email to send received.");
        }
        if (mail.getTo() == null || mail.getTo().isEmpty()) {
            return ResponseEntity.badRequest().body("The email needs at least one receiver.");
        }

        try {
            sender.sendEmail(mail);

            String message = "Successful sent email to " + mail.getTo().toString();
            LOGGER.info(message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
