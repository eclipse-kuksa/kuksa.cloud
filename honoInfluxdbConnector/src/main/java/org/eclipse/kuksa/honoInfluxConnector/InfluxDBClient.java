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

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
public class InfluxDBClient implements MessageHandler{

    private static final Logger logger = LoggerFactory.getLogger(InfluxDBClient.class);

    private InfluxDB influxDB;
    private BatchPoints batchPoints;


    public InfluxDBClient(@Value("${influxdb.url}") String influxURL,
                          @Value("${influxdb.db.name}") String dbName)
            throws MalformedURLException {


        // TODO test validity of URL
        influxDB = InfluxDBFactory.connect(influxURL);

        logger.info("will connect to InfluxDB server at {} and database {} ", influxURL, dbName );

        influxDB.createDatabase(dbName);
        influxDB.enableBatch(10,100, TimeUnit.MILLISECONDS);

        batchPoints = BatchPoints
                .database(dbName)
                .tag("async", "true")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .retentionPolicy("autogen")
                .build();
    }

    public void process(MessageDTO msg){
        Point.Builder pointBuilder = Point.measurement(msg.getDeviceID())
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        for(Map.Entry<String, Object> entry : msg.getEntries().entrySet()) {
            pointBuilder.addField(entry.getKey(), entry.getValue().toString());
        }
        Point point = pointBuilder.build();
        batchPoints.point(point);
        influxDB.write(batchPoints);
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        logger.info("will terminate.");
        influxDB.close();
    }
}
