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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@RestController
@RequestMapping("/api/1.0")
@Api(value = "/api/1.0", description = "App API Rest Controller", tags = "App API", consumes = "application/json")
public class AppController {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	@Autowired
	AppService appService;

	@ApiOperation(notes = "This process is used to get an app with Id of an app. Id parameter should given in get operation.", value = "Getting an App", nickname = "getAppbyId", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/apps/{appId}")
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

	@ApiOperation(notes = "This process is used to create app with app model. Id parameter should not implemented in post operation because of that it is already given by server", value = "Creating an App", nickname = "createApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
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

	@ApiOperation(notes = "This process is used to update an app with app model. Id parameter should given in put operation.", value = "Updating an App", nickname = "updateApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
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

	@ApiOperation(notes = "This process is used to delete an app with Id of an app. Id parameter should given in delete operation.", value = "Deleting an App", nickname = "deleteApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@DeleteMapping("/app/{appId}")
	public ResponseEntity<?> deleteApp(@PathVariable String appId) throws NotFoundException {
		LOG.debug("[deleteApp]: Delete App request is received. appId: {}", appId);
		appService.deleteApp(appId);

		LOG.debug("[deleteApp] Delete App is processed successfully. appId: {}", appId);
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	@ApiOperation(notes = "This process is used to get all app. You can use Pageable that ensures that You can get a page you want.", value = "Getting all App", nickname = "getAllApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/apps")
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
	
	@ApiOperation(notes = "This process is used to get User's app. You can use Pageable that ensures that You can get a page you want.", value = "Getting User's App", nickname = "getAllApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/apps/user/{userId}")
	public ResponseEntity<?> getUserApssbyUserId(@PathVariable Long userId, Pageable pageable)
			throws NotFoundException {

		Page<App> apps = appService.findByNameStartsWithIgnoreCaseAndUsersId("", userId, pageable);

		LOG.debug("[getUserApssbyUserId]: getUserApssbyUserId request is processed successfully. Device: {}", userId);

		return new ResponseEntity<>(apps, HttpStatus.OK);

	}
	
	@ApiOperation(notes = "This process is used to get User's app with filter text. You can use Pageable that ensures that You can get a page you want.", value = "Getting User's App with Filter Text", nickname = "getAllApp", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/apps/user/{userId}/{text}")
	public ResponseEntity<?> getUserApssbyUserIdAndText(@PathVariable Long userId, @PathVariable String text,
			Pageable pageable) throws NotFoundException {

		Page<App> apps = appService.findByNameStartsWithIgnoreCaseAndUsersId(text, userId, pageable);

		LOG.debug(
				"[getUserApssbyUserIdAndText]: getUserApssbyUserIdAndText request is processed successfully. Device: {}",
				userId);

		return new ResponseEntity<>(apps, HttpStatus.OK);

	}

}