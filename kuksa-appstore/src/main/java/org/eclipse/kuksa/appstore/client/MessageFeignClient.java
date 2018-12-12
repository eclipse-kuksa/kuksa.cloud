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

import org.eclipse.kuksa.appstore.configuration.FeignConfig;
import org.eclipse.kuksa.appstore.model.AssignedResult;
import org.eclipse.kuksa.appstore.model.DistributionResult;
import org.eclipse.kuksa.appstore.model.DistributionResultByName;
import org.eclipse.kuksa.appstore.model.Rule;
import org.eclipse.kuksa.appstore.model.TargetResult;
import org.eclipse.kuksa.appstore.model.TargetResultByData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hawkbit-get-targets", url = "http://${hawkbit.host}:${hawkbit.port}", configuration = FeignConfig.class)
public interface MessageFeignClient {

	@RequestMapping(method = RequestMethod.GET, value = "/rest/v1/targets")
	TargetResultByData getTargetsByDes(@RequestParam(value = "q") String key,
			@RequestParam(value = "sort") String sort);

	@RequestMapping(value = "/rest/v1/targets", method = RequestMethod.GET)
	TargetResult getTargets();

	@RequestMapping(value = "/rest/v1/distributionsets ", method = RequestMethod.GET)
	DistributionResult getDistribution();

	@RequestMapping(value = "/rest/v1/targets/{i}/assignedDS ", headers = {
			"Content-Type=application/json;charset=UTF-8" }, method = RequestMethod.POST)
	AssignedResult sendApptoDevice(@PathVariable("i") String i, Rule ruleinput);

	@RequestMapping(value = "/rest/v1/distributionsets", method = RequestMethod.GET)
	DistributionResultByName getDistributionByName(@RequestParam(value = "q") String key);

}
