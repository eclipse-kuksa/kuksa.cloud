/*
 * ******************************************************************************
 * Copyright (c) 2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * *****************************************************************************
 */
package org.eclipse.kuksa.influxdb.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class Controller {

    /* default std out logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    /* jackson node factory to create new nodes */
    private static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;

    /* name of the database to query on influxDB */
    private final String dbName;

    /* URL of the influxDB deployment */
    private final URL url;

    /**
     * Creates a new instance of the REST controller based on the parameters passed to Spring Boot
     * via the application.properties or as arguments to the Java runtime.
     *
     * @param dbName name of the database to query
     * @param url    url of the influxDB deployment to connect to
     * @param port   port to connect to
     */
    public Controller(@Value("${influxDB.db.name}") String dbName,
                      @Value("${influxDB.url}") String url,
                      @Value("${influxDB.port}") int port) throws MalformedURLException {
        Objects.requireNonNull(dbName);
        Objects.requireNonNull(url);

        this.dbName = dbName;
        this.url = new URL(url + ':' + port);
    }

    /**
     * Returns a list with the IDs of all available devices to query.
     *
     * @return list of device IDs
     */
    @GetMapping("/devices")
    public ResponseEntity<List<String>> getDevices() {
        JsonNode response;
        try {
            response = queryInfluxDB("SHOW SERIES ON " + dbName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(e.getMessage()));
        }

        List<String> result = new ArrayList<>();
        try {
            ArrayNode values = (ArrayNode) getFirstSeries(response).get("values");
            for (JsonNode value : values) {
                ArrayNode valueArray = (ArrayNode) value;
                if (valueArray.size() > 0 && valueArray.get(0).isTextual()) {
                    String device = valueArray.get(0).asText();
                    if (!StringUtils.isEmpty(device) && !device.contains(",")) {
                        result.add(device);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Returns a list of measurements using the device id and the optional
     * time restrictions. If none provided the latest value for each column
     * within the last 24hours is returned.
     * Time format is defined in https://www.ietf.org/rfc/rfc3339.txt.
     * If {@code at} is provided, from and to are ignored.
     *
     * @param id   device id to query
     * @param from starting point in time
     * @param to   end point in time
     * @param at   certain point in time
     * @return list of measurements
     */
    @GetMapping("/devices/{id}")
    public ResponseEntity<List<JsonNode>> getMeasurements(@PathVariable String id,
                                                          @RequestParam(required = false) String from,
                                                          @RequestParam(required = false) String to,
                                                          @RequestParam(required = false) String at) {
        try {
            List<JsonNode> results = queryRoots(id, from, to, at);
            if (results == null) {
                results = Collections.emptyList();
            }

            return ResponseEntity.ok(results);
        } catch (NullPointerException n) {
            return ResponseEntity.ok(Collections.emptyList());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(JsonNodeFactory.instance.textNode(e.getMessage())));
        }
    }

    /**
     * Returns the first series of the first result received from
     * influxDB.
     *
     * @param root root node received from the query
     * @return first series node
     */
    private static JsonNode getFirstSeries(JsonNode root) {
        return root.get("results").get(0).get("series").get(0);
    }

    /**
     * Checks whether there are time query parameters are set or not.
     * If none are set the query requires the latest data.
     *
     * @param from starting point of the time query
     * @param to   end point of the time query
     * @param at   exact point in time to query for
     * @return true if none of the parameters is set, false otherwise
     */
    private static boolean requiresLatestObject(String from, String to, String at) {
        return from == null && to == null && at == null;
    }

    private JsonNode buildLatestTree(JsonNode root) {
        ObjectNode result = FACTORY.objectNode();
        JsonNode series = getFirstSeries(root);
        ArrayNode columns = (ArrayNode) series.get("columns");
        String[][] columnSplits = new String[columns.size()][];
        for (int i = 0; i < columns.size(); i++) {
            columnSplits[i] = columns.get(i).asText().toLowerCase().split("\\.");
        }
        ArrayNode values = (ArrayNode) series.get("values");
        for (int i = 1; i < columnSplits.length; i++) {
            for (JsonNode measurementRow : values) {
                if (measurementRow != null && measurementRow.isArray()) {
                    ArrayNode measurementArray = (ArrayNode) measurementRow;
                    JsonNode measurement = measurementArray.get(i);

                    if (measurement != null && !StringUtils.isEmpty(measurement.asText())
                            && !measurement.asText().equalsIgnoreCase("null")) {
                        buildUpTree(result, columnSplits[i], measurement.asText());
                        break;
                    }
                }
            }
        }

        return result;
    }

    private List<JsonNode> buildTrees(JsonNode root) {
        JsonNode series = getFirstSeries(root);
        ArrayNode columns = (ArrayNode) series.get("columns");
        String[][] columnSplits = new String[columns.size()][];
        for (int i = 0; i < columns.size(); i++) {
            columnSplits[i] = columns.get(i).asText().toLowerCase().split("\\.");
        }
        List<JsonNode> result = new ArrayList<>();
        ArrayNode values = (ArrayNode) series.get("values");
        for (JsonNode measurementRow : values) {
            if (measurementRow != null && measurementRow.isArray()) {
                ArrayNode measurementArray = (ArrayNode) measurementRow;
                if (measurementArray.size() > 0) {
                    ObjectNode timeNode = FACTORY.objectNode();
                    timeNode.set("time", FACTORY.textNode(measurementArray.get(0).asText()));

                    for (int i = 1; i < measurementRow.size(); i++) {
                        buildUpTree(timeNode, columnSplits[i], measurementRow.get(i).asText());
                    }
                    result.add(timeNode);
                }
            }
        }
        return result;
    }

    /**
     * Builds up a tree based on the given hierarchy of names.
     * If a name already exists the function traverses the existing tree.
     * Otherwise the new required node gets created. The leaf node will
     * contain the given value.
     *
     * @param node      node to start iterating
     * @param hierarchy hierarchy of names
     * @param value     value to set for leaf
     */
    private static void buildUpTree(ObjectNode node, String[] hierarchy, String value) {
        for (int i = 0; i < hierarchy.length - 1; i++) {
            String step = hierarchy[i];
            JsonNode child = node.get(step);
            if (child != null && child.isObject()) {
                node = (ObjectNode) child;
            } else {
                ObjectNode next = FACTORY.objectNode();
                node.set(step, next);
                if (child != null) {
                    next.set("_value", child);
                }

                node = next;
            }
        }

        node.set(hierarchy[hierarchy.length - 1], createNewNode(value));
    }

    /**
     * Creates a new json node depending on the content of the given string.
     * If the string is a boolean, integer or floating point number the function
     * return the matching node type. Otherwise string.
     *
     * @param s string to create a json node for
     * @return json node with the content of {@code s}
     */
    private static JsonNode createNewNode(String s) {
        if (StringUtils.isEmpty(s)) {
            return FACTORY.nullNode();
        }

        // boolean
        if (s.equalsIgnoreCase("true")) {
            return FACTORY.booleanNode(true);
        }
        if (s.equalsIgnoreCase("false")) {
            return FACTORY.booleanNode(false);
        }

        // integers
        try {
            long value = Long.parseLong(s);
            return FACTORY.numberNode(value);
        } catch (NumberFormatException e) {
            //ignore
        }

        // floating point
        try {
            double value = Double.parseDouble(s);
            return FACTORY.numberNode(value);
        } catch (NumberFormatException e) {
            //ignore
        }

        // text
        return FACTORY.textNode(s);
    }

    private List<JsonNode> queryRoots(String id, String from, String to, String at) throws IOException {
        JsonNode query = querySeries(id, from, to, at);
        if (query == null) {
            return Collections.emptyList();
        }

        if (requiresLatestObject(from, to, at)) {
            return Collections.singletonList(buildLatestTree(query));
        } else {
            return buildTrees(query);
        }
    }

    /**
     * Queries the database for a specific table in a time descending order.
     * Time specifications are optional and require timestamps as defined in https://www.ietf.org/rfc/rfc3339.txt.
     * You can either specify an exact point in time, a time interval or all data.
     * If an exact point is set via {@code at} it is preferred over time interval.
     * For a time interval set {@code from} or {@code to} or both of them.
     * Otherwise queries all available data ignoring timestamps.
     * Returns the first series returned by influxDB.
     *
     * @param table table to query data from
     * @param from  starting point in time
     * @param to    end point in time
     * @param at    exact point in time
     * @return first series of the received result
     */
    private JsonNode querySeries(String table, String from, String to, String at) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM \"");
        builder.append(table);
        builder.append('\"');

        // query the last day for latest build
        if (requiresLatestObject(from, to, at)) {
            ZonedDateTime time = LocalDateTime.now().atZone(ZoneId.systemDefault()).minusDays(1);
            from = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        }

        builder.append(" WHERE");

        if (at != null) {
            builder.append(" time = \'");
            builder.append(at);
            builder.append('\'');
        } else {
            if (from != null) {
                builder.append(" time >= \'");
                builder.append(from);
                builder.append('\'');
                if (to != null) {
                    builder.append(" AND");
                }
            }
            if (to != null) {
                builder.append(" time <= \'");
                builder.append(to);
                builder.append('\'');
            }
        }
        builder.append(" ORDER BY time DESC");

        // send the created query to the influxDB
        return queryInfluxDB(builder.toString());
    }

    /**
     * Sends the given query to the influxDB using the {@code url} and {@code port} of this instance.
     *
     * @param query query string to send to influxDB
     * @return the query response
     * @throws IOException thrown if query to influx db fails
     */
    private JsonNode queryInfluxDB(String query) throws IOException {
        URL buildURL = generateQueryUrl(url, createParameters(query));
        String json = HttpQuery.get(buildURL);

        return new ObjectMapper().readTree(json);
    }

    /**
     * Returns a map of parameters for the query consisting of
     * the database to query and the query string.
     *
     * @param query query string for influxDB
     * @return map of parameters for the query
     */
    private Map<String, String> createParameters(String query) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("db", dbName);
        parameters.put("q", query);

        return parameters;
    }

    /**
     * Generated a query url based on the given base url together with the
     * parameters.
     *
     * @param url        base url
     * @param parameters query parameters to add after the base url
     * @return generated query url
     * @throws MalformedURLException thrown if invalid url
     */
    private static URL generateQueryUrl(URL url, Map<String, String> parameters) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder.append("/query?");

        Iterator<String> keyIterator = parameters.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            builder.append(key);
            builder.append('=');
            builder.append(parameters.get(key));
            if (keyIterator.hasNext()) {
                builder.append('&');
            }
        }

        return new URL(url, builder.toString());
    }
}
