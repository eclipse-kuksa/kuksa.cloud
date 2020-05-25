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

package org.eclipse.kuksa.honoConnector.influxdb;

import org.eclipse.kuksa.honoConnector.message.MessageDTO;
import org.eclipse.kuksa.honoConnector.message.MessageHandler;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBException;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Client that connects to an instance of influxDB and forwards message dtos
 * as defined in {@link MessageHandler}. Transforms the message to a measurement
 * point to write to influxDB.
 */
public class InfluxDBClient implements MessageHandler {

    /* standard logger for logging information and errors */
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBClient.class);

    /* influxDB connection wrapper */
    private final InfluxDB influxDB;

    /* name of the database to write to */
    private final String dbName;

    /**
     * Creates a new connector to an influxDB database to write forwarded messages to.
     *
     * @param influxURL url of the influxDB deployment
     * @param dbName    name of the database
     * @throws MalformedURLException throws exception if the given url is in the wrong format
     */
    public InfluxDBClient(final String influxURL,
                          final String dbName) throws MalformedURLException {
        this.dbName = dbName;
        // check the given url string
        URL url = new URL(influxURL);
        influxDB = InfluxDBFactory.connect(url.toString());
        LOGGER.info("Connected to InfluxDB at {} with database name {}", url.toString(), dbName);

        // consumer for possible exceptions when writing
        BiConsumer<Iterable<Point>, Throwable> consumer = (points, throwable) -> {
            if (throwable instanceof InfluxDBException.DatabaseNotFoundException) {
                if (!createDatabase()) {
                    LOGGER.error("Failed to create the database {}. Will drop pending points.", dbName);
                    return;
                }
                for (Point point : points) {
                    writePoint(point);
                }
            }
        };
        ThreadFactory factory = Executors.defaultThreadFactory();
        influxDB.enableBatch(10, 100, TimeUnit.MILLISECONDS, factory, consumer);
    }

    /**
     * Creates a new database in the influxdb instance {@link #influxDB} with the name
     * {@link #dbName}. If the database is already present the function simply return true.
     *
     * @return true if database is created, false if not
     */
    private boolean createDatabase() {
        String query = String.format("CREATE DATABASE \"%s\"", dbName);
        QueryResult result = influxDB.query(new Query(query, dbName, true));
        if (result.hasError()) {
            LOGGER.error("Creating database {} resulted in error: {}", dbName, result.getError());
        } else {
            LOGGER.info("Database {} now exists and has been created if it did not exist before", dbName);
        }
        return result != null && !result.hasError();
    }

    /**
     * Transforms the body of the message to a database entry and writes it to the database
     * associated with the instance by {@link #influxDB}.
     * Will drop empty messages and not write them to the database.
     *
     * @param msg message dto to process
     */
    @Override
    public void process(final MessageDTO msg) {
        // check for empty messages and just drop them
        if (msg == null) {
            return;
        }
        final Map<String, Object> entries = msg.getEntries();
        if (entries == null || entries.isEmpty()) {
            return;
        }

        //check for attribute named 'time' in message and use it as timestamp for the InfluxDb
        long timestamp;
        Object timeObject = entries.get(MessageDTO.TIMESTAMP_ATTRIBUTE_NAME);
        if (timeObject instanceof Long) {
            timestamp = (Long) timeObject;
            LOGGER.debug("Used timestamp: {} from message with sender: {}", timestamp, msg.getDeviceID());
        } else {
            timestamp = System.currentTimeMillis();
            LOGGER.debug("Generated timestamp: {} for message with sender: {}", timestamp, msg.getDeviceID());
        }

        final Point point = createPoint(timestamp, msg.getDeviceID(), entries);
		writePoint(point);
    }

	public static Point createPoint(long timestampMs, final String deviceID, final Map<String, Object> entries) {
		final Point.Builder pointBuilder = Point.measurement(deviceID)
                .time(timestampMs, TimeUnit.MILLISECONDS);
		entries.forEach(addFields(pointBuilder));
        return pointBuilder.build();
	}

	private static BiConsumer<String, Object> addFields(final Point.Builder pointBuilder) {
		return (key, value) -> {
			if(value instanceof Boolean) {
				pointBuilder.addField(key, (Boolean)value);
			} else if(value instanceof Number) {
				pointBuilder.addField(key, ((Number)value).doubleValue());
			} else {
				pointBuilder.addField(key, value.toString());
			}
		};
	}

    /**
     * Writes a single measurement point to the influxDB.
     *
     * @param point measurement point to write to the database
     */
    private void writePoint(final Point point) {
        influxDB.write(dbName, "autogen", point);
    }

    @Override
    public void close() {
        influxDB.close();
    }
}
