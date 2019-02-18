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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URL;

class HttpQuery {

    /**
     * Sends a GET call to the given url and return the return body as
     * a string. If the request failed the return is null.
     *
     * @param url url to send get call to
     * @return message body as a string, null if failed
     * @throws IOException thrown if get call fails
     */
    static String get(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (response != null && response.body() != null) {
                return response.body().string();
            }
        }

        return null;
    }
}
