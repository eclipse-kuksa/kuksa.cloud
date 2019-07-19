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

package org.eclipse.kuksa.honoConnector;

import static java.util.stream.Collectors.toList;
import static org.eclipse.kuksa.honoConnector.HonoInfluxConnection.toConnection;

import java.net.MalformedURLException;
import java.util.List;

import org.eclipse.hono.config.ClientConfigProperties;
import org.eclipse.kuksa.honoConnector.config.ConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Connects to Hono messaging and forwards received messages to InfluxDB.
 */
@Component
class HonoInfluxConnector implements ApplicationRunner {
    
    private final List<HonoInfluxConnection> connections;

    @Autowired
    public HonoInfluxConnector(@Value("${qpid.router.host}") final String qpidRouterHost,
                  @Value("${qpid.router.port}") final int qpidRouterPort,
                  @Value("${hono.user}") final String honoUser,
                  @Value("${hono.password}") final String honoPassword,
                  @Value("${hono.trustedStorePath}") final String honoTrustedStorePath,
                  @Value("${hono.verifyHostname}") final boolean verifyHostname,
                  @Value("${influxdb.url}") final String influxURL,
                  final ConfigProperties configProperties) throws MalformedURLException {
    	final ClientConfigProperties honoConfig1 = new ClientConfigProperties();
					honoConfig1.setHost(qpidRouterHost);
					honoConfig1.setPort(qpidRouterPort);
					honoConfig1.setUsername(honoUser);
					honoConfig1.setPassword(honoPassword);
					honoConfig1.setTrustStorePath(honoTrustedStorePath);
					honoConfig1.setHostnameVerificationRequired(verifyHostname);
		final ClientConfigProperties honoConfig = honoConfig1;
    	this.connections = connections(honoConfig, influxURL, configProperties);
    }

	private static List<HonoInfluxConnection> connections(final ClientConfigProperties honoConfig,
			final String influxURL,
			final ConfigProperties configProperties) {
		return configProperties.getConnections().stream()
				.map(toConnection(honoConfig, influxURL))
				.collect(toList());
	}

    @Override
    public void run(final ApplicationArguments applicationArguments) {
        // start with the initial connect to Hono
        connections.stream().forEach(HonoInfluxConnection::connectToHono);
    }
}
