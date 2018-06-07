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
 * *****************************************************************************
 */

package org.eclipse.kuksa.honoInfluxConnector;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClientOptions;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


@Component
public class HonoClient implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(HonoClient.class);

    @Value("${hono.tenant.id}")
    public String honoTenantId;

    private final Vertx vertx = Vertx.vertx();
    private final org.eclipse.hono.client.HonoClient honoClient;

    private final CountDownLatch latch;

    private MessageHandler messageHandler;

    public HonoClient(@Value("${qpid.router.host}") String qpidRouterHost,
                      @Value("${qpid.router.port}") int qpidRouterPort,
                      @Value("${hono.user}") String honoUser,
                      @Value("${hono.password}") String honoPassword,
                      @Value("${hono.trustedStorePath}") String honoTrustedStorePath) {
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
        latch = new CountDownLatch(1);
    }


    @Autowired
    public void setMessageHandler(MessageHandler messageHandler){
        this.messageHandler = messageHandler;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        System.out.println("Starting downstream consumer...");
        consumeTelemetryData();
        System.out.println("Finishing downstream consumer.");
    }

    private void consumeTelemetryData() throws Exception {
        final Future<MessageConsumer> consumerFuture = Future.future();

        consumerFuture.setHandler(result -> {
            if (!result.succeeded()) {
                System.err.println("honoClient could not create telemetry consumer : " + result.cause());
            }
            latch.countDown();
        });

        final Future<org.eclipse.hono.client.HonoClient> connectionTracker = Future.future();

        honoClient.connect(new ProtonClientOptions(), connectionTracker.completer());

        connectionTracker.compose(honoClient -> {
                    honoClient.createTelemetryConsumer(honoTenantId,
                            msg -> handleTelemetryMessage(msg), consumerFuture.completer());
                },
                consumerFuture);

        latch.await();

        if (consumerFuture.succeeded())
            System.in.read();
        vertx.close();
    }

    private void handleTelemetryMessage(final Message msg) {
        final Section body = msg.getBody();

        if (!(body instanceof Data))
            return;

        MessageDTO messageDTO = createMessageDTO(msg);
        logger.info(messageDTO.toString());

        messageHandler.process(messageDTO);
    }

    private MessageDTO createMessageDTO(final Message msg) {

        final String deviceId = MessageHelper.getDeviceId(msg);

        String content = ((Data) msg.getBody()).getValue().toString();

        Map<String, Object> entries = null;
        try {
            entries = new ObjectMapper().readValue(content, Map.class);
        } catch (IOException e) {
            logger.error("Unable to parse message {}.", content);
        }

        MessageDTO result = new MessageDTO(deviceId, entries);

        return result;
    }

}
