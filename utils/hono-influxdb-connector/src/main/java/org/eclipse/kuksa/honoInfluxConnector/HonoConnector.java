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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.proton.ProtonClientOptions;
import org.apache.qpid.proton.message.Message;
import org.eclipse.hono.client.HonoClient;
import org.eclipse.hono.config.ClientConfigProperties;
import org.eclipse.hono.util.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;


@Component
public class HonoConnector implements ApplicationRunner {

    /* standard logger for information and error output */
    private static final Logger LOGGER = LoggerFactory.getLogger(HonoConnector.class);

    /* connection options for the connection to the Hono Messaging Service */
    private final ProtonClientOptions options;

    /* vertx instance opened to connect to Hono Messaging, needs to be closed */
    private final Vertx vertx;

    /* client used to connect to the Hono Messaging Service to receive new messages */
    private final HonoClient honoClient;

    /* message handler forwarding the messages to their final destination */
    private final MessageHandler messageHandler;

    /* handler of the outcome of a connection attempt to Hono Messaging */
    private final Handler<AsyncResult<HonoClient>> connectionHandler;

    private final Handler<Void> closeHandler;

    /* current number of reconnects so far */
    private int reconnectCount;

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
                         @Value("${hono.tenant.id}") final String honoTenantId,
                         @Value("${influxdb.url}") final String influxURL,
                         @Value("${influxdb.db.name}") final String dbName) throws MalformedURLException {
        vertx = Vertx.vertx();
        ClientConfigProperties config = new ClientConfigProperties();
        config.setHost(qpidRouterHost);
        config.setPort(qpidRouterPort);
        config.setUsername(honoUser);
        config.setPassword(honoPassword);
        config.setTrustStorePath(honoTrustedStorePath);
        config.setTlsEnabled(false);
        config.setReconnectAttempts(2);
        config.setHostnameVerificationRequired(false);

        honoClient = HonoClient.newClient(vertx, config);

        options = new ProtonClientOptions();
        options.setReconnectAttempts(reconnectAttempts);
        options.setConnectTimeout(10000);

        reconnectCount = 0;
        messageHandler = new InfluxDBClient(influxURL, dbName);

        closeHandler = x -> reconnect();

        // on connection established create a new telemetry consumer to handle incoming messages
        connectionHandler = x -> {
            if (x.succeeded()) {
                LOGGER.info("Connected to Hono at {}:{}", qpidRouterHost, qpidRouterPort);
                honoClient.createTelemetryConsumer(honoTenantId, this::handleTelemetryMessage, closeHandler);
            } else {
                LOGGER.error("Failed to connect to Hono.", x.cause());
                disconnect();
            }
        };
    }

    private void disconnect() {
        messageHandler.close();
        vertx.close();
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
            connectToHono();
        } else {
            LOGGER.info("Number of reconnects exceeds the user defined threshold of {} reconnects.", options.getReconnectAttempts());
            //honoClient.disconnect();
        }
    }

    @Override
    public void run(final ApplicationArguments applicationArguments) {
        // start with the initial connect to Hono
        connectToHono();
    }

    private void connectToHono() {
        try {
            Future<HonoClient> future = honoClient.connect(options);
            LOGGER.info("Started connection attempt to Hono");
            future.setHandler(connectionHandler);
        } catch (Exception e) {
            LOGGER.error("Failed to connect to Hono.", e);
        }
    }

    /**
     * Handles the incoming message and forwards it using the {@code messageHandler}.
     * If the message is {@code null} it will drop the message.
     *
     * @param msg message to forward
     */
    private void handleTelemetryMessage(final Message msg) {
        if (msg == null) {
            LOGGER.debug("The received message from Hono is null.");
            return;
        }

        MessageDTO messageDTO = createMessageDTO(msg);
        LOGGER.debug(messageDTO.toString());

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

        JsonObject json = new JsonObject();
        try {
            json = MessageHelper.getJsonPayload(msg);
        } catch (DecodeException e) {
            LOGGER.warn("Failed to parse the message body to JSON.", e);
        }

        //ObjectMapper mapper = new ObjectMapper();
        //MapType type = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);

        return new MessageDTO(deviceId, json.getMap());
    }
}
