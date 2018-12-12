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

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class InfluxDBClient implements MessageHandler {

    /* standard logger for logging information and errors */
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBClient.class);

    /* influxDB connection wrapper */
    private final InfluxDB influxDB;

    /* name of the database to write to*/
    private final String dbName;

    /**
     * Creates a new connector to an influxDB database to write forwarded messages to.
     *
     * @param influxURL url of the influxDB deployment
     * @param dbName    name of the database
     * @throws MalformedURLException throws exception if the given url is in the wrong format
     */
    public InfluxDBClient(@Value("${influxdb.url}") final String influxURL,
                          @Value("${influxdb.db.name}") final String dbName) throws MalformedURLException {
        // check the given url string
        URL url = new URL(influxURL);
        influxDB = InfluxDBFactory.connect(url.toString());

        LOGGER.info("will connect to InfluxDB server at {} and database {} ", influxURL, dbName);

        influxDB.createDatabase(dbName);
        influxDB.enableBatch(10, 100, TimeUnit.MILLISECONDS);

        this.dbName = dbName;
    }

    /**
     * Transforms the body of the message to a database entry and writes it to the database
     * associated with the instance by {@code influxDB}.
     * Will drop empty messages and not write them to the database.
     *
     * @param msg message to write to process
     */
    public void process(MessageDTO msg) {
        Point.Builder pointBuilder = Point.measurement(msg.getDeviceID())
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        // check for empty messages and just drop them
        Map<String, Object> entries = msg.getEntries();
        if (entries == null || entries.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            pointBuilder.addField(entry.getKey(), entry.getValue().toString());
        }
        Point point = pointBuilder.build();

        influxDB.write(dbName, "autogen", point);
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        LOGGER.info("will terminate.");
        influxDB.close();
    }
}
