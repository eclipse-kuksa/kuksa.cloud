/*
 *********************************************************************
 * Copyright (c) 2021 Bosch.IO GmbH [and others]
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.kuksa.honoConnector;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.proton.ProtonClientOptions;
import org.eclipse.hono.application.client.DownstreamMessage;
import org.eclipse.hono.application.client.MessageConsumer;
import org.eclipse.hono.application.client.MessageContext;
import org.eclipse.hono.application.client.amqp.AmqpApplicationClient;
import org.eclipse.hono.application.client.amqp.ProtonBasedApplicationClient;
import org.eclipse.hono.client.DisconnectListener;
import org.eclipse.hono.client.HonoConnection;
import org.eclipse.hono.config.ClientConfigProperties;
import org.eclipse.kuksa.honoConnector.config.ConnectionConfig;
import org.eclipse.kuksa.honoConnector.influxdb.InfluxDBClient;
import org.eclipse.kuksa.honoConnector.message.MessageDTO;
import org.eclipse.kuksa.honoConnector.message.MessageHandler;
import org.slf4j.Logger;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;

public class HonoInfluxConnection {

    /** standard logger for information and error output */
    private static final Logger LOGGER = getLogger(HonoInfluxConnection.class);

    private final String tenantId;
    
    /** vertx instance opened to connect to Hono Messaging, needs to be closed */
    private final Vertx vertx;

    /** client used to connect to the Hono Messaging Service to receive new messages */
    private final ProtonBasedApplicationClient client;

    /** properties for connection to Hono which is used to create the Hono client */
    private final ClientConfigProperties honoConfig;

    /** connection options for the connection to the Hono Messaging Service */
    private final ProtonClientOptions options;

    /** message handler forwarding the messages to their final destination */
    private final MessageHandler messageHandlerInflux;

    /** name of the database in the InfluxDb where incoming Hono messages should be written to */
    private final String dbName;

    public HonoInfluxConnection(final ClientConfigProperties honoConfig,
    		final String influxURL, final ConnectionConfig config) throws MalformedURLException {
    	this(honoConfig, influxURL, config.getTenantId(), config.getInfluxDatabaseName());
    }
    
    public HonoInfluxConnection(final ClientConfigProperties honoConfig,
    		final String influxURL, final String tenantId, final String dbName) throws MalformedURLException {
    	this.tenantId = tenantId;
    	this.dbName = dbName;
    	this.honoConfig = honoConfig;
        vertx = Vertx.vertx();

        HonoConnection honoConnection = HonoConnection.newConnection(vertx, honoConfig);
        client = new ProtonBasedApplicationClient(honoConnection);

        options = new ProtonClientOptions();
        options.setConnectTimeout(10000);

        messageHandlerInflux = new InfluxDBClient(influxURL, dbName);
    }

    /**
     * Connects to the Hono based on the options defined in {@link #options}.
     */
	public void connectToHono() {
        try {

            // Handler to be registered for disconnect case
            // which tries to reconnect if link is closed by Hono.
            final DisconnectListener<HonoConnection> closeHandler = unused -> {
                // The close handler is invoked when the Hono dispatch router
                // closes the telemetry receiver link.
                LOGGER.info("receiver link was closed.");
                reconnect();
            };

            client.start()
                    .onSuccess(v -> {
                            final AmqpApplicationClient ac = client;
                            ac.addDisconnectListener(closeHandler);
                            ac.addReconnectListener(c -> LOGGER.info("reconnected to Hono"));
                    })
                    .compose(v -> CompositeFuture.all(createEventConsumer(), createTelemetryConsumer()))

                    //.onSuccess(v -> LOGGER.info("Connected to Hono at {}:{} for tenantId {} and Influx database {}", honoConnection.getConfig().getHost(),
                    //        honoConnection.getConfig().getPort(), tenantId, dbName))
                    .onSuccess(v -> LOGGER.info("Connected to Hono at {}:{} for tenantId {}, with user {} and Influx database {}",
                            honoConfig.getHost(), honoConfig.getPort(), honoConfig.getUsername(), tenantId, dbName))
                    .onFailure(cause -> {
                        LOGGER.error("Failed to create message consumers to Hono at {}:{} " +
                                "for tenantId {}, with user {} and Influx database {} with error {}",
                                honoConfig.getHost(), honoConfig.getPort(),
                                honoConfig.getUsername(), tenantId, dbName, cause.toString());
                        disconnect();}
                        );
        } catch (Exception e) {
            LOGGER.error("Error during connection to Hono at {}:{} " +
                            "for tenantId {}, with user {} and Influx database {} with exception {}",
                    honoConfig.getHost(), honoConfig.getPort(),
                    honoConfig.getUsername(), tenantId, dbName, e.toString());
        }
	}

    private Future<MessageConsumer> createEventConsumer() {
        return client.createEventConsumer(tenantId,
                msg -> {handleEventMessage(msg);},
                cause -> {LOGGER.error("The event consumer was closed by remote:", cause);});
    }

    private Future<MessageConsumer> createTelemetryConsumer() {
        return client.createTelemetryConsumer(tenantId,
                msg -> {handleTelemetryMessage(msg);},
                cause -> {LOGGER.error("The telemetry consumer was closed by remote:", cause);});
    }

    /**
     * Reconnects to Hono using the {@link #connectToHono()} function.
     */
    private void reconnect() {
        LOGGER.info("Reconnecting to Hono for tenantId {}", tenantId);
        connectToHono();
    }

    /**
     * Handles the incoming telemetry message and forwards it using the {@code messageHandler}.
     * If the message is {@code null} it will drop the message.
     *
     * @param msg message to forward
     */
    private void handleTelemetryMessage(final DownstreamMessage<? extends MessageContext> msg) {
        if (msg == null) {
            LOGGER.debug("The received telemetry message from Hono is null.");
            return;
        }

        vertx.executeBlocking(future -> {
            final MessageDTO messageDTO = createMessageDTO(msg);
            LOGGER.info(messageDTO.toString());

            // forward created message dto
            messageHandlerInflux.processTelemetry(messageDTO);
            future.complete(messageDTO);
        }, res -> {
            if (res.succeeded()) {
                LOGGER.debug("Wrote telemetry message: {}", res.result());
            } else if (res.failed())  {
                LOGGER.debug("Writing failed due to {} ", res.cause());
            }
        });
    }

    /**
     * Handles the incoming event message and forwards it using the {@code messageHandler}.
     * If the message is {@code null} it will drop the message.
     *
     * @param msg message to forward
     */
    private void handleEventMessage(final DownstreamMessage<? extends MessageContext> msg) {
        if (msg == null) {
            LOGGER.debug("The received event message from Hono is null.");
            return;
        }

        vertx.executeBlocking(future -> {
            final MessageDTO messageDTO = createMessageDTO(msg);
            LOGGER.info(messageDTO.toString());

            // forward created message dto
            messageHandlerInflux.processEvent(messageDTO);
            future.complete(messageDTO);
        }, res -> {
            if (res.succeeded()) {
                LOGGER.debug("Wrote event event message: {}", res.result());
            } else if (res.failed())  {
                LOGGER.debug("Writing failed due to {} ", res.cause());
            }
        });
    }


    /**
     * Creates a new dto of the given message object.
     *
     * @param msg message to transform
     * @return the dto of the input
     */
    private static MessageDTO createMessageDTO(final DownstreamMessage<? extends MessageContext> msg) {
        msg.getDeviceId();

        final String deviceId = msg.getDeviceId();
        JsonObject json = new JsonObject();
        try {
            json = msg.getPayload().toJsonObject();
        } catch (DecodeException e) {
            LOGGER.warn("Failed to parse the message body to JSON due to.", e);
        }

        return new MessageDTO(deviceId, json.getMap());
    }

    /**
     * Disconnects from Hono and the message handler.
     */
	public void disconnect() {
        LOGGER.warn("Hono connector for tenantId {} is closing connections to Hono and InfluxDB.", tenantId);
        messageHandlerInflux.close();
        vertx.close();
        LOGGER.info("Hono connector for tenantId {} closed all connections.", tenantId);
	}
	
	public static Function<ConnectionConfig, HonoInfluxConnection> toConnection(final ClientConfigProperties honoConfig,
    		final String influxURL) {
		return connectionConfig -> {
			try {
				return new HonoInfluxConnection(honoConfig, influxURL, connectionConfig);
			} catch (final MalformedURLException e) {
				throw new UncheckedIOException(e);
			}
		};
	}
}
