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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HonoInfluxConnectorApplication {

	public static void main(String[] args) {

		SpringApplication.run(HonoInfluxConnectorApplication.class, args);
	}
}
