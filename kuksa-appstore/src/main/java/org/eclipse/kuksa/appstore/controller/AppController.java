/*******************************************************************************
 * Copyright (C) 2018-2019 Netas Telekomunikasyon A.S. [and others]
 *  
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *  
 * SPDX-License-Identifier: EPL-2.0
 *  
 * Contributors:
 * Adem Kose, Fatih Ayvaz and Ilker Kuzu (Netas Telekomunikasyon A.S.) - Initial functionality
 * Philipp Heisig (Dortmund University of Applied Sciences and Arts) 
 * Johannes Kristan (Bosch Software Innovation)
 ******************************************************************************/
package org.eclipse.kuksa.appstore.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.hawkbit.Result;
import org.eclipse.kuksa.appstore.service.AppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@RestController
@RequestMapping("/api/1.0")
@Api(value = "/api/1.0", description = "Applications API", tags = "Applications", consumes = "application/json")
public class AppController {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	@Autowired
	AppService appService;

	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@ApiOperation(notes = "Returns the App specified by the appId path parameter. The response includes all details about the App.", value = "Getting an App", nickname = "getAppbyId", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/app/{appId}")
	public ResponseEntity<?> getAppbyId(@PathVariable String appId) throws NotFoundException {

		App app = appService.findById(Long.parseLong(appId));
		if (app != null) {
			LOG.debug("[getAppbyId]: getAppbyId request is processed successfully. appId: {}", appId);
			return new ResponseEntity<>(app, HttpStatus.OK);
		} else {
			LOG.debug("[getAppbyId]: getAppbyId request is received. appId: {}", appId);
			throw new NotFoundException("App not found!");
		}

	}

	@Secured("ROLE_ADMIN")
	@ApiOperation(notes = "Creates an App defined in the request JSON body. Id field should not implemented in post request JSON body because of that it is already given by server.", value = "Creating an App", nickname = "createApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PostMapping("/app")
	public ResponseEntity<?> createApp(@Valid @RequestBody App app) throws AlreadyExistException, BadRequestException {
		Result<?> response = appService.createApp(app);
		if (response.isSuccess()) {
			LOG.debug("[createApp]: createApp request is processed successfully. app: {}", app);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
		} else {
			LOG.debug("[createApp]: createApp request is received. app: {}", app);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}

	@Secured("ROLE_ADMIN")
	@ApiOperation(notes = "Updates the App identified by the appId parameter and the JSON body.", value = "Updating an App", nickname = "updateApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PutMapping("/app/{appId}")
	public ResponseEntity<?> updateApp(@PathVariable String appId, @Valid @RequestBody App app)
			throws AlreadyExistException, BadRequestException, NotFoundException {

		Result<?> response = appService.updateApp(appId, app);
		if (response.isSuccess()) {
			LOG.debug("[updateApp]: updateApp request is processed successfully. app: {}", app);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
		} else {
			LOG.debug("[updateApp]: updateApp request is received. app: {}", app);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}

	@Secured("ROLE_ADMIN")
	@ApiOperation(notes = "Deletes the App specified by the appId parameter.", value = "Deleting an App", nickname = "deleteApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@DeleteMapping("/app/{appId}")
	public ResponseEntity<?> deleteApp(@PathVariable String appId) throws NotFoundException {
		LOG.debug("[deleteApp]: Delete App request is received. appId: {}", appId);
		appService.deleteApp(appId);

		LOG.debug("[deleteApp] Delete App is processed successfully. appId: {}", appId);
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@ApiOperation(notes = "Returns all Apps.", value = "Getting all App", nickname = "getAllApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/app")
	public ResponseEntity<?> getAllApp(Pageable pageable) throws NotFoundException {

		Page<App> apps = appService.findAll(pageable);
		if (apps.getTotalElements() > 0) {
			LOG.debug("[getAllApp]: getUserbyId request is processed successfully.");
			return new ResponseEntity<>(apps, HttpStatus.OK);
		} else {
			LOG.debug("[getAllApp]: getUserbyId request is received.");
			throw new NotFoundException("Apps not found!");
		}
	}

	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@ApiOperation(notes = "Returns user's purchased apps.", value = "Getting User's Purchased Apps", nickname = "getPurchasedAppsbyUserId", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/app/purchased/user/{userId}")
	public ResponseEntity<?> getPurchasedAppsbyUserId(@PathVariable Long userId, Pageable pageable)
			throws NotFoundException, BadRequestException {

		Page<App> apps = appService.findUsersApps(userId.toString(), appService.getListOfOem(appService.getListOfTargets(userId)), pageable);

		LOG.debug("[getPurchasedAppsbyUserId]: getPurchasedAppsbyUserId request is processed successfully. Device: {}", userId);

		return new ResponseEntity<>(apps, HttpStatus.OK);

	}

	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@ApiOperation(notes = "Returns User's installed Apps by userId and name parameter. The name parameter ensures to search by App Name.", value = "Getting User's installed Apps.", nickname = "getUserApssbyUserId", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/app/installed/user/{userId}")
	public ResponseEntity<?> getUsersInstalledAppsbyUserId(@PathVariable Long userId, Pageable pageable)
			throws NotFoundException {

		Page<App> apps = appService.findByInstalledusersId(userId, pageable);

		LOG.debug("[getUsersInstalledAppsbyUserId]: getUsersInstalledAppsbyUserId request is processed successfully. userId: {}", userId);

		return new ResponseEntity<>(apps, HttpStatus.OK);

	}

	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@ApiOperation(notes = "Returns User's installed Apps by userId and name parameter. The name parameter ensures to search by App Name.", value = "Getting User's installed Apps by name.", nickname = "getUserApssbyUserIdAndName", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/app/installed/user/{userId}/{name}")
	public ResponseEntity<?> getUsersInstalledAppsbyUserIdAndName(@PathVariable Long userId, @PathVariable String name,
			Pageable pageable) throws NotFoundException {

		Page<App> apps = appService.findByNameStartsWithIgnoreCaseAndInstalledusersId(name, userId, pageable);

		LOG.debug(
				"[getUsersInstalledAppsbyUserIdAndName]: getUsersInstalledAppsbyUserIdAndName request is processed successfully. userId: {}",
				userId);

		return new ResponseEntity<>(apps, HttpStatus.OK);

	}

	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@ApiOperation(notes = "Returns Apps specified by appCategoryId parameter.", value = "Getting Apps By App Category Id", nickname = "getApssbyAppCategoryId", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/app/category/{appCategoryId}")
	public ResponseEntity<?> getApssbyAppCategoryId(@PathVariable Long appCategoryId, Pageable pageable)
			throws NotFoundException {

		Page<App> apps = appService.findByAppcategoryId(appCategoryId, pageable);
		LOG.debug(
				"[getApssbyAppCategoryId]: getApssbyAppCategoryId request is processed successfully. appCategoryId: {}",
				appCategoryId);
		return new ResponseEntity<>(apps, HttpStatus.OK);

	}

	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@ApiOperation(notes = "Returns Apps specified by appCategoryId parameter and name parameter. The name parameter ensures to search by App Name.", value = "Getting Apps By App Category Id with Filter Text", nickname = "getApssbyNameAndAppcategoryId", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/app/category/{appCategoryId}/{name}")
	public ResponseEntity<?> getApssbyNameAndAppcategoryId(@PathVariable Long appCategoryId, @PathVariable String name,
			Pageable pageable) throws NotFoundException {

		Page<App> apps = appService.findByNameStartsWithIgnoreCaseAndAppcategoryId(name, appCategoryId, pageable);
		LOG.debug(
				"[getApssbyNameAndAppcategoryId]: getApssbyNameAndAppcategoryId request is processed successfully. appCategoryId: {}",
				appCategoryId);
		return new ResponseEntity<>(apps, HttpStatus.OK);

	}

	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@ApiOperation(notes = "This process is used to purchase an App specified by userId and appId.", value = "Purchase an App", nickname = "purchaseApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PostMapping(value = "/app/{appId}/purchase/{userId}")
	public ResponseEntity<?> purchaseApp(@PathVariable Long userId, @PathVariable Long appId)
			throws NotFoundException, BadRequestException {

		LOG.debug("[purchaseApp]: purchaseApp request is processed successfully. purchaseApp: {}", appId);
		return new ResponseEntity<>(appService.purchaseApp(userId, appId), HttpStatus.OK);
	}

	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@ApiOperation(notes = "This process is used to install an App specified by appId to the given device for user specified by userId .", value = "Install an App", nickname = "InstallApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PostMapping(value = "/app/{appId}/install/{userId}")
	public ResponseEntity<?> InstallApp(@PathVariable Long userId, @PathVariable Long appId,
			@RequestBody String deviceName) throws NotFoundException, BadRequestException, AlreadyExistException {

		LOG.debug("[InstallApp]: InstallApp request is processed successfully. InstallApp: {}", appId, userId,
				deviceName);
		return new ResponseEntity<>(appService.InstallApp(deviceName, userId, appId), HttpStatus.OK);
	}

	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@ApiOperation(notes = "This process is used to uninstall an App specified by appId from the given device for user specified by userId.", value = "Uninstall an App", nickname = "UninstallApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@DeleteMapping(value = "/app/{appId}/uninstall/{userId}")
	public ResponseEntity<?> UninstallApp(@PathVariable Long userId, @PathVariable Long appId,
			@RequestBody String deviceName) throws NotFoundException, BadRequestException, AlreadyExistException {

		LOG.debug("[UninstallApp]: UninstallApp request is processed successfully. UninstallApp: {}", appId, userId,
				deviceName);
		List<Long> appIds = new ArrayList<Long>();
		appIds.add(appId);
		return new ResponseEntity<>(appService.UninstallMultiApp(deviceName, userId, appIds), HttpStatus.OK);
	}
	
	@Secured("ROLE_ADMIN")
	@ApiOperation(notes = "Get all artifacts to given app identified by its name and version.", value = "Get Artifacts for an App", nickname = "getArtifacts", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping("/app/{appId}/artifacts")
	public ResponseEntity<?> getArtifacts(@PathVariable Long appId)
			throws AlreadyExistException, BadRequestException, IOException, NotFoundException {
		Result<?> response = appService.getArtifactsWithAppId(appId);
		if (response.isSuccess()) {
			LOG.debug("[getArtifacts]: getArtifacts request is processed successfully for app: {}", appId);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
		} else {
			LOG.debug("[getArtifacts]: getArtifacts request is received for app: {}", appId);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@Secured("ROLE_ADMIN")
	@ApiOperation(notes = "Uploads an artifact to given app identified by its name and version. ", value = "Upload an Artifact to an App", nickname = "uploadArtifact", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PostMapping("/app/{appId}/artifact")
	public ResponseEntity<?> uploadArtifact(@Valid @RequestPart MultipartFile file, @PathVariable Long appId)
			throws AlreadyExistException, BadRequestException, IOException, NotFoundException {
		Result<?> response = appService.uploadArtifactWithAppId(appId, file.getOriginalFilename(), file.getBytes());
		if (response.isSuccess()) {
			LOG.debug("[uploadArtifact]: uploadArtifact request is processed successfully with artifact: {}",
					file.getOriginalFilename());
			return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
		} else {
			LOG.debug("[uploadArtifact]: uploadArtifact request is received. artifact: {}", file.getOriginalFilename());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@Secured("ROLE_ADMIN")
	@ApiOperation(notes = "Delete an artifact to given app identified by its name and version.", value = "Delete Artifact for an App", nickname = "deleteArtifact", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@DeleteMapping("/app/{appId}/artifact/{artifactId}")
	public ResponseEntity<?> deleteArtifact(@PathVariable Long appId, @PathVariable String artifactId)
			throws AlreadyExistException, BadRequestException, IOException, NotFoundException {
		Result<?> response = appService.deleteArtifactWithAppId(appId, artifactId);
		if (response.isSuccess()) {
			LOG.debug("[uploadArtifact]: uploadArtifact request is processed successfully with artifact: {}", appId);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
		} else {
			LOG.debug("[uploadArtifact]: uploadArtifact request is received. artifact: {}", appId);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}
}
