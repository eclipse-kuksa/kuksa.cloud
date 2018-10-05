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
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import static springfox.documentation.builders.PathSelectors.regex;
@Configuration
@EnableSwagger2
public class SwaggerConfig {                                    
    @Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())  
          .select()                                  
          .apis(RequestHandlerSelectors.any())              
          .paths(postPaths())
          .build();                                           
    }
    private Predicate<String> postPaths() {
		return regex("/api/1.0.*");
	}
    private ApiInfo apiInfo() {
        return new ApiInfo(
          "Kuksa Appstore Rest API", 
          "This API is a RESTful API that enables to perform Create/Read/Update/Delete operations for provisioning applications and user of store.\r\n" + 
          "\r\n" + 
          "Based on the this API you can manage and monitor application update operations via HTTP/HTTPS.This API supports JSON payload with hypermedia as well as filtering, sorting and paging. Furthermore The API is protected and needs authentication and authorization based on the security concept.", 
          "1.0", 
          "Terms of service", 
          new Contact("Fatih Ayvaz", "www.netas.com.tr", "fayvaz@netas.com.tr"),          
          "License of API", "license", Collections.emptyList());
   }
}