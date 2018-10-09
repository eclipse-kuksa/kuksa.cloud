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
package org.eclipse.kuksa.appstore;

import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableAutoConfiguration
@ImportAutoConfiguration(FeignAutoConfiguration.class)
@EnableFeignClients
public class AppStoreApplication {
	
	private static final Logger LOG = LoggerFactory.getLogger(AppStoreApplication.class);
	
	@Autowired
	UserService userservice;
	
	@Value("${appstore.username}")
    private String username;
	
	@Value("${appstore.password}")
    private String password;
	
	public static void main(String[] args) {
		SpringApplication.run(AppStoreApplication.class, args);
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void EventListenerExecute() throws AlreadyExistException, BadRequestException {
		
		if(userservice.findByUserName(username)==null) {			
			userservice.createUser(username, password, true);
			LOG.debug("[EventListenerExecute]: The user is added. : {}", username);
		}else {
			LOG.debug("[EventListenerExecute]: The user already exists. : {}", username);			
		}
	}
}
