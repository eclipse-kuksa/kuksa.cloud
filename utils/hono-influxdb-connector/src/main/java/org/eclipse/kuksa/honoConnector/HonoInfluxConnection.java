/*********************************************************************
 * Copyright (c) 2019, 2020 Bosch.IO GmbH [and others]
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
import org.eclipse.hono.application.client.ApplicationClient;
import org.eclipse.hono.application.client.DownstreamMessage;
import org.eclipse.hono.application.client.MessageConsumer;
import org.eclipse.hono.application.client.MessageContext;
import org.eclipse.hono.application.client.amqp.AmqpApplicationClient;
import org.eclipse.hono.application.client.amqp.ProtonBasedApplicationClient;
import org.eclipse.hono.client.HonoConnection;
import org.eclipse.hono.config.ClientConfigProperties;
import org.eclipse.kuksa.honoConnector.config.ConnectionConfig;
import org.eclipse.kuksa.honoConnector.influxdb.InfluxDBClient;
import org.eclipse.kuksa.honoConnector.message.MessageDTO;
import org.eclipse.kuksa.honoConnector.message.MessageHandler;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;

public class HonoInfluxConnection {

    /** standard logger for information and error output */
    private static final Logger LOGGER = getLogger(HonoInfluxConnection.class);

    private final String tenantId;
    
    /** vertx instance opened to connect to Hono Messaging, needs to be closed */
    private final Vertx vertx;

    private final ApplicationClient<? extends MessageContext> client;

    /** connection used to connect to the Hono Messaging Service to receive new messages */
    private final HonoConnection honoConnection;

    /** message handler forwarding the messages to their final destination */
    private final MessageHandler messageHandler;

    private MessageConsumer eventConsumer;

    private MessageConsumer telemetryConsumer;
    
    public HonoInfluxConnection(final ClientConfigProperties honoConfig,
    		final String influxURL, final ConnectionConfig config) throws MalformedURLException {
    	this(honoConfig, influxURL, config.getTenantId(), config.getInfluxDatabaseName());
    }
    
    public HonoInfluxConnection(final ClientConfigProperties honoConfig,
    		final String influxURL, final String tenantId, final String dbName) throws MalformedURLException {
    	this.tenantId = tenantId;
        vertx = Vertx.vertx();

        honoConnection = HonoConnection.newConnection(vertx, honoConfig);
        client = new ProtonBasedApplicationClient(honoConnection);

        messageHandler = new InfluxDBClient(influxURL, dbName);

        LOGGER.debug("Initiated Hono-InfluxDb Connection");
    }


    public void consumeData() {

        final CountDownLatch latch = new CountDownLatch(1);

        LOGGER.info("Connect to Hono at host {}:{}, with user: {}", honoConnection.getConfig().getHost(),
                honoConnection.getConfig().getPort(), honoConnection.getConfig().getUsername());

        final Future<CompositeFuture> startFuture = client.start()
                .onSuccess(v -> {
                    final AmqpApplicationClient ac = (AmqpApplicationClient) client;
                    ac.addDisconnectListener(c -> LOGGER.info("lost connection"));
                    ac.addReconnectListener(c -> LOGGER.info("reconnected"));
                })
                .compose(v -> CompositeFuture.all(createEventConsumer(tenantId), createTelemetryConsumer(tenantId)))
                .onSuccess(v -> LOGGER.info("Consumer ready for telemetry and event messages."))
                .onFailure(cause -> LOGGER.error("consumer failed to start"))
                .onComplete(ar -> latch.countDown());

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final List<Future> closeFutures = new ArrayList<>();
        if (startFuture.succeeded()) {
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            closeFutures.add(eventConsumer.close());
            closeFutures.add(telemetryConsumer.close());
            closeFutures.add(client.stop());
        }

        CompositeFuture.join(closeFutures).onComplete(ar -> vertx.close());

    }

    private Future<MessageConsumer> createEventConsumer(String tenantId) {
        return client.createEventConsumer(tenantId,
                msg -> {handleEventMessage(msg);},
                cause -> {LOGGER.error("The event consumer was closed by remote:", cause);})
        .onSuccess(consumer -> this.eventConsumer = consumer);
    }

    private Future<MessageConsumer> createTelemetryConsumer(String tenantId) {
        return client.createTelemetryConsumer(tenantId,
                msg -> {handleTelemetryMessage(msg);},
                cause -> {LOGGER.error("The telemetry consumer was closed by remote:", cause);})
        .onSuccess(telemetryConsumer -> this.telemetryConsumer = telemetryConsumer);
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

        final MessageDTO messageDTO = createMessageDTO(msg);
        LOGGER.info(messageDTO.toString());

        // forward created message dto
        messageHandler.processTelemetry(messageDTO);
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

        final MessageDTO messageDTO = createMessageDTO(msg);
        LOGGER.info(messageDTO.toString());

        // forward created message dto
        messageHandler.processEvent(messageDTO);
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
