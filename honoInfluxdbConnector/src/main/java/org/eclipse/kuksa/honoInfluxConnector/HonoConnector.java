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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClientOptions;
import io.vertx.proton.ProtonConnection;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
import org.eclipse.hono.client.HonoClient;
import org.eclipse.hono.client.MessageConsumer;
import org.eclipse.hono.client.impl.HonoClientImpl;
import org.eclipse.hono.connection.ConnectionFactoryImpl;
import org.eclipse.hono.util.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component
public class HonoConnector implements ApplicationRunner {

    /* standard logger for information and error output */
    private static final Logger LOGGER = LoggerFactory.getLogger(HonoConnector.class);

    /* connection options for the connection to the Hono Messaging Service */
    private final ProtonClientOptions options;

    //TODO is this field necessary?
    private final String honoTenantId;

    /* vertx instance opened to connect to Hono Messaging, needs to be closed */
    private final Vertx vertx = Vertx.vertx();

    /* client used to connect to the Hono Messaging Service to receive new messages */
    private final HonoClient honoClient;

    /* message handler forwarding the messages to their final destination */
    private MessageHandler messageHandler;

    /* handler for a completed connect to Hono Messaging */
    private final Handler<AsyncResult<MessageConsumer>> completeHandler;

    /* handler of the outcome of a connection attempt to Hono Messaging */
    private final Handler<AsyncResult<org.eclipse.hono.client.HonoClient>> connectionHandler;

    /* handler for a disconnect from the Hono Messaging */
    private final Handler<ProtonConnection> disconnectHandler;

    /* current number of reconnects so far */
    private int reconnectCount;

    @Autowired
    private ApplicationContext appContext;

    /**
     * Creates a new client to connect to Hono Messaging and forward the received messages to a
     * message handler of choice.
     *
     * @param qpidRouterHost       url of the dispatch router to connect to
     * @param qpidRouterPort       port of the dispatch router to use
     * @param honoUser             user to authorize with Hono Messaging
     * @param honoPassword         password to authorize with Hono Messaging
     * @param honoTrustedStorePath path to the certificate file used to connect to Hono Messaging
     * @param reconnectAttempts    maximum number of reconnects
     * @param honoTenantId         tenant id
     */
    public HonoConnector(@Value("${qpid.router.host}") final String qpidRouterHost,
                         @Value("${qpid.router.port}") final int qpidRouterPort,
                         @Value("${hono.user}") final String honoUser,
                         @Value("${hono.password}") final String honoPassword,
                         @Value("${hono.trustedStorePath}") final String honoTrustedStorePath,
                         @Value("${hono.reconnectAttempts}") final int reconnectAttempts,
                         @Value("${hono.tenant.id}") final String honoTenantId) {
        honoClient = new HonoClientImpl(vertx,
                ConnectionFactoryImpl.ConnectionFactoryBuilder.newBuilder()
                        .vertx(vertx)
                        .host(qpidRouterHost)
                        .port(qpidRouterPort)
                        .user(honoUser)
                        .password(honoPassword)
                        .trustStorePath(honoTrustedStorePath)
                        .disableHostnameVerification()
                        .build());
        options = new ProtonClientOptions();
        options.setReconnectAttempts(reconnectAttempts);
        options.setConnectTimeout(10000);
        reconnectCount = 0;
        this.honoTenantId = honoTenantId;

        // log information of the outcome of the telemetry consumer creation
        completeHandler = x -> {
            if (x.succeeded()) {
                LOGGER.info("Created a telemetry consumer for {}", honoTenantId);
            } else {
                LOGGER.error("Failed to create a telemetry consumer. Reason: {}", x.cause());
                x.cause().printStackTrace();
            }
        };

        // on connection established create a new telemetry consumer to handle incoming messages
        connectionHandler = x -> {
            if (x.succeeded()) {
                LOGGER.info("Connected to Hono Messaging at {}:{}", qpidRouterHost, qpidRouterPort);
                honoClient.createTelemetryConsumer(honoTenantId, this::handleTelemetryMessage, completeHandler);
            } else {
                LOGGER.error("unable to connect to the hono messaging service. Reason: {}", x.cause());
                x.cause().printStackTrace();
            }
        };

        // on disconnect try to reconnect if there are still reconnects available
        disconnectHandler = x -> reconnect();
    }

    /**
     * Reconnect to the hono messaging service.
     * If the number of reconnects exceeds the number of reconnects defined in the application.properties by the
     * parameter 'hono.reconnectAttempts' the connector won't reconnect but shutdown instead.
     */
    private void reconnect() {
        reconnectCount++;

        if (reconnectCount <= options.getReconnectAttempts()) {
            LOGGER.info("Reconnecting to the Hono Messaging Service...");
            connectToHonoMessaging();
        } else {
            LOGGER.info("Number of reconnects exceeds the user defined threshold of {} reconnects.", options.getReconnectAttempts());
            shutdown();
        }
    }

    /**
     * Shuts down the connector and calls Spring Boot to terminate.
     */
    private void shutdown() {
        vertx.close();
        LOGGER.info("Shutting connector down...");

        SpringApplication.exit(appContext, () -> 0);
    }

    /**
     * Sets the message handler for incoming messages. null values will be ignored.
     *
     * @param messageHandler new message handler to use
     */
    @Autowired
    public void setMessageHandler(final MessageHandler messageHandler) {
        if (messageHandler != null) {
            this.messageHandler = messageHandler;
        }
    }

    @Override
    public void run(final ApplicationArguments applicationArguments) {
        // start with the initial connect to Hono
        connectToHonoMessaging();
    }

    /**
     * Connects to Hono Messaging using the options and event handler initialized in the constructor.
     * In case of a exception the client tries the next reconnect.
     */
    private void connectToHonoMessaging() {
        try {
            honoClient.connect(options, connectionHandler, disconnectHandler);
        } catch (Exception e) {
            LOGGER.error("Exception caught during connection attempt to Hono. Reason: {}", e.getMessage());
            e.printStackTrace();
            reconnect();
        }
    }

    /**
     * Handles the incoming message and forwards it using the {@code messageHandler}.
     *
     * @param msg message to forward
     */
    private void handleTelemetryMessage(final Message msg) {
        final Section body = msg.getBody();

        if (!(body instanceof Data)) {
            return;
        }

        MessageDTO messageDTO = createMessageDTO(msg);
        LOGGER.info(messageDTO.toString());

        messageHandler.process(messageDTO);
    }

    /**
     * Creates a new dto of the given message object.
     *
     * @param msg message to transform
     * @return the dto of the input
     */
    private static MessageDTO createMessageDTO(final Message msg) {
        final String deviceId = MessageHelper.getDeviceId(msg);

        String content = ((Data) msg.getBody()).getValue().toString();

        Map<String, Object> entries = null;
        try {
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
            };
            entries = new ObjectMapper().readValue(content, typeRef);
        } catch (IOException e) {
            LOGGER.error("Unable to parse message {}.", content);
        }

        return new MessageDTO(deviceId, entries);
    }
}
