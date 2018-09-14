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

import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class HonoConnectorTest {

    @Test
    public void testJSONParsing() throws IOException {

        String content = "{\"temp\": 5}";

        // parse JSON
        Map<String, String> entries =
                new ObjectMapper().readValue( content, Map.class);

        System.out.print(entries);
    }

}