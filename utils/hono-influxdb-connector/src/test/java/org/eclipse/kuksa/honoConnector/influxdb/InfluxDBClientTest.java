/*
 * ******************************************************************************
 * Copyright (c) 2019 Bosch Software Innovations GmbH [and others]
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 * *****************************************************************************
 */

package org.eclipse.kuksa.honoConnector.influxdb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import java.util.HashMap;
import java.util.Map;

import org.influxdb.dto.Point;
import org.junit.Test;

public class InfluxDBClientTest {

	@Test
	public void stringIsCorrectlyEncoded() {
		final long timestampMs = System.currentTimeMillis();
		final Point point = InfluxDBClient.createPoint(timestampMs, "deviceID", map("key", "valueString"));
		
		assertThat(point.lineProtocol(), equalTo("deviceID key=\"valueString\" "+timestampMs+"000000"));
	}

	@Test
	public void booleanIsCorrectlyEncoded() {
		final long timestampMs = System.currentTimeMillis();
		final Point point = InfluxDBClient.createPoint(timestampMs, "deviceID", map("key", true));
		
		assertThat(point.lineProtocol(), equalTo("deviceID key=true "+timestampMs+"000000"));
	}

	/**
	 * At times clients send data as integer initially (e.g. due to rounding) and as double later.
	 * This causes problems with InfluxDB indexing. Hence, all numbers should be encoded as double.
	 */
	@Test
	public void integerIsEncodedAsDouble() {
		final long timestampMs = System.currentTimeMillis();
		final Point point = InfluxDBClient.createPoint(timestampMs, "deviceID", map("key", 123));
		
		assertThat(point.lineProtocol(), equalTo("deviceID key=123.0 "+timestampMs+"000000"));
	}
	
	@Test
	public void doubleIsCorrectlyEncoded() {
		final long timestampMs = System.currentTimeMillis();
		final Point point = InfluxDBClient.createPoint(timestampMs, "deviceID", map("key", 1.23));
		
		assertThat(point.lineProtocol(), equalTo("deviceID key=1.23 "+timestampMs+"000000"));
	}
	
	@Test
	public void miscIsEncodedAsString() {
		final long timestampMs = System.currentTimeMillis();
		final Point point = InfluxDBClient.createPoint(timestampMs, "deviceID", map("key", new SomeClass()));
		
		assertThat(point.lineProtocol(), equalTo("deviceID key=\"SomeClass as a string\" "+timestampMs+"000000"));
	}
	
	protected Map<String,Object> map(final String key, final Object value) {
		final Map<String,Object> entries = new HashMap<>();
		entries.put(key, value);
		return entries;
	}
	
	private static class SomeClass {
		@Override
		public String toString() {
			return "SomeClass as a string";
		}
	}
}
