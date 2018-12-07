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
package org.eclipse.kuksa.appstore.controller;

import java.util.List;

import javax.validation.Valid;

import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.Result;
import org.eclipse.kuksa.appstore.service.AppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@ApiOperation(notes = "Deletes the App specified by the appId parameter.", value = "Deleting an App", nickname = "deleteApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@DeleteMapping("/app/{appId}")
	public ResponseEntity<?> deleteApp(@PathVariable String appId) throws NotFoundException {
		LOG.debug("[deleteApp]: Delete App request is received. appId: {}", appId);
		appService.deleteApp(appId);

		LOG.debug("[deleteApp] Delete App is processed successfully. appId: {}", appId);
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

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

	@ApiOperation(notes = "Returns user's purchased apps.", value = "Getting User's Purchased Apps", nickname = "getPurchasedAppsbyUserId", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/app/purchased/user/{userId}")
	public ResponseEntity<?> getPurchasedAppsbyUserId(@PathVariable Long userId, Pageable pageable)
			throws NotFoundException {

		Page<App> apps = appService.findByNameStartsWithIgnoreCaseAndInstalledusersId("", userId, pageable);

		LOG.debug("[getUserApssbyUserId]: getUserApssbyUserId request is processed successfully. Device: {}", userId);

		return new ResponseEntity<>(apps, HttpStatus.OK);

	}

	@ApiOperation(notes = "Returns User's installed Apps by userId and name parameter. The name parameter ensures to search by App Name.", value = "Getting User's installed Apps.", nickname = "getUserApssbyUserId", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/app/installed/user/{userId}")
	public ResponseEntity<?> getUserApssbyUserId(@PathVariable Long userId, Pageable pageable)
			throws NotFoundException {

		Page<App> apps = appService.findByInstalledusersId(userId, pageable);

		LOG.debug("[getUserApssbyUserId]: getUserApssbyUserId request is processed successfully. userId: {}", userId);

		return new ResponseEntity<>(apps, HttpStatus.OK);

	}

	@ApiOperation(notes = "Returns User's installed Apps by userId and name parameter. The name parameter ensures to search by App Name.", value = "Getting User's installed Apps by name.", nickname = "getUserApssbyUserIdAndName", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/app/installed/user/{userId}/{name}")
	public ResponseEntity<?> getUserApssbyUserIdAndName(@PathVariable Long userId, @PathVariable String name,
			Pageable pageable) throws NotFoundException {

		Page<App> apps = appService.findByNameStartsWithIgnoreCaseAndInstalledusersId(name, userId, pageable);

		LOG.debug(
				"[getUserApssbyUserIdAndName]: getUserApssbyUserIdAndName request is processed successfully. userId: {}",
				userId);

		return new ResponseEntity<>(apps, HttpStatus.OK);

	}

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

	@ApiOperation(notes = "This process is used to purchase an App specified by userId and appId.", value = "Purchase an App", nickname = "purchaseApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PostMapping(value = "/app/{appId}/purchase/{userId}")
	public ResponseEntity<?> purchaseApp(@PathVariable Long userId, @PathVariable Long appId)
			throws NotFoundException, BadRequestException {

		LOG.debug("[purchaseApp]: purchaseApp request is processed successfully. purchaseApp: {}", appId);
		return new ResponseEntity<>(appService.purchaseApp(userId, appId), HttpStatus.OK);
	}
}
