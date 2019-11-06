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

import java.util.List;

import org.eclipse.kuksa.appstore.configuration.HawkbitFeignConfig;
import org.eclipse.kuksa.appstore.model.Permission;
import org.eclipse.kuksa.appstore.model.hawkbit.Artifact;
import org.eclipse.kuksa.appstore.model.hawkbit.AssignedResult;
import org.eclipse.kuksa.appstore.model.hawkbit.Distribution;
import org.eclipse.kuksa.appstore.model.hawkbit.DistributionResult;
import org.eclipse.kuksa.appstore.model.hawkbit.Rule;
import org.eclipse.kuksa.appstore.model.hawkbit.SoftwareModule;
import org.eclipse.kuksa.appstore.model.hawkbit.SoftwareModuleResult;
import org.eclipse.kuksa.appstore.model.hawkbit.TargetResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import feign.Response;

@FeignClient(name = "hawkbit-get-targets", url = "${hawkbit.url}", configuration = HawkbitFeignConfig.class)
public interface HawkbitFeignClient {

	@RequestMapping(method = RequestMethod.GET, value = "/rest/v1/targets")
	TargetResult getTargetsByDes(@RequestParam(value = "q") String key, @RequestParam(value = "sort") String sort);

	@RequestMapping(value = "/rest/v1/targets", method = RequestMethod.GET)
	TargetResult getTargets();

	@RequestMapping(value = "/rest/v1/distributionsets", method = RequestMethod.GET)
	DistributionResult getDistribution();

	@RequestMapping(value = "/rest/v1/targets/{i}/assignedDS", headers = {
			"Content-Type=application/json;charset=UTF-8" }, method = RequestMethod.POST)
	AssignedResult sendApptoDevice(@PathVariable("i") String i, Rule ruleinput);

	@RequestMapping(value = "/rest/v1/distributionsets", method = RequestMethod.GET)
	DistributionResult getDistributionByName(@RequestParam(value = "q") String key,
			@RequestParam(value = "limit") int limit, @RequestParam(value = "sort") String sort);

	@RequestMapping(method = RequestMethod.GET, value = "/rest/v1/softwaremodules")
	SoftwareModuleResult getSoftwaremoduleByName(@RequestParam(value = "q") String key);

	@RequestMapping(method = RequestMethod.POST, headers = {
			"Content-Type=application/hal+json;charset=UTF-8" }, value = "/rest/v1/softwaremodules")
	List<SoftwareModule> createSoftwaremodules(List<SoftwareModule> softwareModuleList);

	@RequestMapping(method = RequestMethod.DELETE, value = "rest/v1/softwaremodules/{softwareModuleId}")
	Response deletesoftwareModuleById(@PathVariable("softwareModuleId") String softwareModuleId);

	@RequestMapping(method = RequestMethod.GET, value = "rest/v1/softwaremodules/{softwareModuleId}/artifacts")
	List<Artifact> getArtifactsBysoftwareModuleId(@PathVariable("softwareModuleId") String softwareModuleId);

	@RequestMapping(method = RequestMethod.DELETE, value = "rest/v1/softwaremodules/{softwareModuleId}/artifacts/{artifactId}")
	Response deleteArtifactsBysoftwareModuleId(@PathVariable("softwareModuleId") String softwareModuleId,
			@PathVariable("artifactId") String artifactId);

	@RequestMapping(method = RequestMethod.DELETE, value = "rest/v1/distributionsets/{distributionSetId}")
	Response deleteDistribution(@PathVariable("distributionSetId") String distributionSetId);

	@RequestMapping(method = RequestMethod.POST, headers = {
			"Content-Type=application/hal+json;charset=UTF-8" }, value = "/rest/v1/distributionsets")
	Response createDistributionSets(List<Distribution> distributionList);

	@RequestMapping(method = RequestMethod.PUT, headers = {
			"Content-Type=application/json;charset=UTF-8" }, value = "/rest/v1/distributionsets/{distributionSetId}")
	Response updateDistributionSets(@PathVariable("distributionSetId") String distributionSetId,
			@RequestBody Distribution distribution);

	@RequestMapping(method = RequestMethod.PUT, headers = {
			"Content-Type=application/json;charset=UTF-8" }, value = "/rest/v1/softwaremodules/{softwareModuleId}")
	Response updateSoftwareModule(@PathVariable("softwareModuleId") String softwareModuleId,
			@RequestBody SoftwareModule softwareModule);

	@RequestMapping(method = RequestMethod.GET, headers = {
			"Content-Type=application/octet-stream;charset=UTF-8" }, value = "rest/v1/softwaremodules/{softwareModuleId}/artifacts/{artifactId}/download")
	String downloadArtifactFile(@PathVariable("softwareModuleId") String softwareModuleId,
			@PathVariable("artifactId") String artifactId);
}
