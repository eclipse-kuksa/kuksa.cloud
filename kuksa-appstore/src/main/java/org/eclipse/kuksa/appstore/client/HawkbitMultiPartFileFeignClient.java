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
package org.eclipse.kuksa.appstore.client;

import org.eclipse.kuksa.appstore.configuration.HawkbitMultiPartFileFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import feign.Param;
import feign.Response;

@FeignClient(name = "hawkbit-upload-softwaremodules", url = "${hawkbit.url}", configuration = HawkbitMultiPartFileFeignConfig.class)
public interface HawkbitMultiPartFileFeignClient {

	@RequestMapping(value = "rest/v1/softwaremodules/{softwareModuleId}/artifacts", method = RequestMethod.POST)
	Response uploadFile(@PathVariable("softwareModuleId") String softwareModuleId, @Param("file") MultipartFile file);

}
