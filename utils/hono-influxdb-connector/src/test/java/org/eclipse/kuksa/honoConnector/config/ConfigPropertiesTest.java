/*********************************************************************
 * Copyright (c) 2019 Bosch Software Innovations GmbH [and others]
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.kuksa.honoConnector.config;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.eclipse.kuksa.honoConnector.config.ConfigProperties;
import org.eclipse.kuksa.honoConnector.config.ConnectionConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest()
@EnableConfigurationProperties(ConfigProperties.class)
public class ConfigPropertiesTest {

	@Autowired
	private ConfigProperties configProperties;
	
	@Test
	public void configuredConnectionsAreCorrectlyParsed() {
		assertThat(configProperties.getConnections(), equalTo(asList(new ConnectionConfig("DEFAULT_TENANT", "devices"))));
	}
	
}
