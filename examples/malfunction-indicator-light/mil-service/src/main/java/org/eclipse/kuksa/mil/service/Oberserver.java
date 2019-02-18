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
package org.eclipse.kuksa.mil.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.maps.GeoApiContext;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;

@Component
public class Oberserver implements ApplicationRunner {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final int SLEEP_TIME = 5000;

    private static final int SLEEP_TIME_AFTER_SENT = 3600000;

    private static final Logger LOGGER = LoggerFactory.getLogger(Oberserver.class);

    private static final String SUBJECT = "no mails while driving...";

    private static final String MAPS_QUERY = "Werkstatt";

    private final URL influxUrl;

    private final URL mailUrl;

    private final String receiverAddress;

    private final String mapsKey;

    Oberserver(@Value("${influxDB.rest.url}") String influxUrl,
               @Value("${influxDB.rest.p}") int influxPort,
               @Value("${influxDB.rest.deviceID}") int deviceID,
               @Value("${mail.url}") String mailUrl,
               @Value("${mail.p}") int mailPort,
               @Value("${receiver.mail}") String receiverAddress,
               @Value("${maps.api.key}") String mapsKey) throws MalformedURLException {
        Objects.requireNonNull(influxUrl);
        Objects.requireNonNull(mailUrl);
        Objects.requireNonNull(receiverAddress);
        Objects.requireNonNull(mapsKey);

        this.influxUrl = new URL("http", influxUrl, influxPort, "/devices/" + deviceID);
        this.mailUrl = new URL("http", mailUrl, mailPort, "/send");

        this.receiverAddress = receiverAddress;
        this.mapsKey = mapsKey;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean status = true;
        int counter = 0;

        while (status) {
            try {
                String json = query(influxUrl);
                JsonNode root = parseJson(json);
                if (!root.isNull() && isMilActive(root)) {
                    StringBuilder sb = new StringBuilder("<h1>Malfunction Indicator Light</h1>");

                    // check the current coordinates
                    checkCoordinates(sb, root);

                    Email email = new Email(Collections.singletonList(receiverAddress), SUBJECT, sb.toString());
                    boolean success;
                    try (Response response = sendEmail(mailUrl, email)) {
                        success = response != null && response.isSuccessful();
                    }
                    if (success) {
                        LOGGER.info("Successfully sent an email to " + receiverAddress);
                        // wait a bit
                        Thread.sleep(SLEEP_TIME_AFTER_SENT);
                        counter++;
                        if (counter >= 10) {
                            status = false;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(SLEEP_TIME);
        }

        LOGGER.info("Shutting down...");
    }

    private void checkCoordinates(StringBuilder sb, JsonNode root) {
        JsonNode coordinates = getCoordinateNode(root);
        if (!validCoordinates(coordinates)) {
            LOGGER.warn("There are no coordinates available to use.");
            sb.append("<p>no coordinates available... still, see a garage soon.</p>");
            return;
        }

        String longString = coordinates.get("longitude").asText();
        String latString = coordinates.get("latitude").asText();
        double longitude, latitude;
        try {
            longitude = Double.parseDouble(longString);
            latitude = Double.parseDouble(latString);
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid longitude {} and latitude {}", longString, latString);
            sb.append("<p>invalid coordinates... still, see a garage soon.</p>");
            return;
        }

        sb.append(findClosest(longitude, latitude));
    }

    /**
     * Parses the JSON string to a node object. Returns {@link com.fasterxml.jackson.databind.node.NullNode}
     * if it fails to parse the string.
     *
     * @param json json string to parse
     * @return object node built from the string
     */
    private static JsonNode parseJson(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            LOGGER.warn("Failed to parse json string: {}", json);
            e.printStackTrace();
        }

        return JsonNodeFactory.instance.nullNode();
    }

    private String findClosest(double longitude, double latitude) {
        GeoApiContext context = new GeoApiContext.Builder().apiKey(mapsKey).build();
        LatLng coordinates = new LatLng(latitude, longitude);
        NearbySearchRequest request = PlacesApi.nearbySearchQuery(context, coordinates);
        try {
            PlacesSearchResponse result = request.keyword(MAPS_QUERY).radius(10000).await();

            if (result != null && result.results != null && result.results.length > 0) {
                return "<p>See your next garage " + result.results[0].name + " at " +
                        result.results[0].vicinity + "</p>";
            }
        } catch (InterruptedException | ApiException | IOException e) {
            LOGGER.warn("Failed to query Google Maps for garages.");
            e.printStackTrace();
        }

        return "<p>no garages near you...</p>";
    }

    private static boolean validCoordinates(JsonNode coordinates) {
        return coordinates != null && coordinates.get("latitude") != null && coordinates.get("longitude") != null;
    }

    private static JsonNode getCoordinateNode(JsonNode root) {
        try {
            if (root != null && root.isArray()) {
                ArrayNode arrayNode = (ArrayNode) root;
                if (arrayNode.size() > 0) {
                    JsonNode signal = arrayNode.get(0).get("signal");
                    if (signal != null) {
                        JsonNode cabin = signal.get("cabin");
                        if (cabin != null) {
                            JsonNode infotainment = cabin.get("infotainment");
                            if (infotainment != null) {
                                JsonNode navigation = infotainment.get("navigation");
                                if (navigation != null) {
                                    return navigation.get("currentLocation");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to parse json for location {}", e.getMessage());
        }

        return null;
    }

    private static Response sendEmail(URL url, Email email) {
        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(email);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.Builder().url(url).post(body).build();
        OkHttpClient client = new OkHttpClient();

        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            LOGGER.error("Failed to send the email notification.");
            e.printStackTrace();
        }

        return null;
    }

    private static boolean isMilActive(JsonNode root) {
        if (root != null && root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            if (arrayNode.size() > 0) {
                JsonNode signal = arrayNode.get(0).get("signal");
                if (signal != null) {
                    JsonNode obd = signal.get("obd");
                    if (obd != null) {
                        JsonNode status = obd.get("status");
                        if (status != null) {
                            JsonNode mil = status.get("mil");
                            return mil.asBoolean(false);
                        }
                    }
                }
            }
        }

        return false;
    }

    private static String query(URL url) {
        Request request = new Request.Builder().url(url).build();

        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(request).execute()) {
            if (response != null && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to query {}", url);
            e.printStackTrace();
        }

        return null;
    }
}
