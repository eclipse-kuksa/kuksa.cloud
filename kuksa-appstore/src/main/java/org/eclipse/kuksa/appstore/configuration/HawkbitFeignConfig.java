/*******************************************************************************
 * Copyright (C) 2018 Netas Telekomunikasyon A.S.
 *  
 *  This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *  
 * SPDX-License-Identifier: EPL-2.0
 *  
 * Contributors:
 * Adem Kose, Fatih Ayvaz and Ilker Kuzu (Netas Telekomunikasyon A.S.) - Initial functionality
 ******************************************************************************/
package org.eclipse.kuksa.appstore.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.auth.BasicAuthRequestInterceptor;

public class HawkbitFeignConfig {

	@Value("${hawkbit.username}")
	private String username;

	@Value("${hawkbit.password}")
	private String password;

	@Bean
	public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
		return new BasicAuthRequestInterceptor(username, password);
	}

}
