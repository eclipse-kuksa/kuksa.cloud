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
import org.eclipse.kuksa.appstore.model.AppCategory;
import org.eclipse.kuksa.appstore.model.hawkbit.Result;
import org.eclipse.kuksa.appstore.service.AppCategoryService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@RestController
@RequestMapping("/api/1.0")
@Api(value = "/api/1.0", description = "AppCategory API", tags = "AppCategory", consumes = "application/json")
public class AppCategoryController {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	@Autowired
	AppCategoryService appCategoryService;

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@ApiOperation(notes = "Returns an App Category specified by appCategoryId.", value = "Getting an App Category", nickname = "getAppCategorybyId", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/appcategory", params="id")
	public ResponseEntity<?> getAppCategorybyId(@RequestParam("id") String id) throws NotFoundException {

		AppCategory appCategory = appCategoryService.findById(Long.parseLong(id));
		if (appCategory != null) {
			LOG.debug("[getAppCategorybyId]: getAppCategorybyId request is processed successfully. id: {}", id);
			return new ResponseEntity<>(appCategory, HttpStatus.OK);
		} else {
			LOG.debug("[getAppCategorybyId]: getAppCategorybyId request is received. id: {}", id);
			throw new NotFoundException("AppCategory not found!");
		}

	}

	@Secured("ROLE_ADMIN")
	@ApiOperation(notes = "Creates an App Category defined in the request JSON body. Id field should not implemented in post request JSON body because of that it is already given by server.", value = "Creating an App Category", nickname = "createAppCategory", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PostMapping("/appcategory")
	public ResponseEntity<?> createAppCategory(@Valid @RequestBody AppCategory appCategory) throws AlreadyExistException, BadRequestException {
		Result<?> response = appCategoryService.createAppCategory(appCategory);
		if (response.isSuccess()) {
			LOG.debug("[createAppCategory]: createAppCategory request is processed successfully. appCategory: {}", appCategory);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.CREATED);
		} else {
			LOG.debug("[createAppCategory]: createAppCategory request is received. appCategory: {}", appCategory);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}

	@Secured("ROLE_ADMIN")
	@ApiOperation(notes = "Updates an App Category defined in the request JSON body.", value = "Updating an App Category", nickname = "updateAppCategory", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PutMapping("/appcategory/{appCategoryId}")
	public ResponseEntity<?> updateAppCategory(@PathVariable String appCategoryId, @Valid @RequestBody AppCategory appCategory)
			throws AlreadyExistException, BadRequestException, NotFoundException {

		Result<?> response = appCategoryService.updateAppCategory(appCategoryId, appCategory);
		if (response.isSuccess()) {
			LOG.debug("[updateAppCategory]: updateAppCategory request is processed successfully. appCategory: {}", appCategory);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
		} else {
			LOG.debug("[updateApp]: updateAppCategory request is received. appCategory: {}", appCategory);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}

	@Secured("ROLE_ADMIN")
	@ApiOperation(notes = "Deletes an app category specified by appCategoryId parameter.", value = "Deleting an App Category", nickname = "deleteAppCategory", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@DeleteMapping("/appcategory/{appCategoryId}")
	public ResponseEntity<?> deleteAppCategory(@PathVariable String appCategoryId) throws NotFoundException, BadRequestException {
		LOG.debug("[deleteAppCategory]: Delete App Category request is received. appId: {}", appCategoryId);
		appCategoryService.deleteAppCategory(appCategoryId);

		LOG.debug("[deleteAppCategory] Delete App Category is processed successfully. appId: {}", appCategoryId);
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@ApiOperation(notes = "Returns all app category.", value = "Getting all App Category", nickname = "getAllAppCategory", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/appcategory")
	public ResponseEntity<?> getAllAppCategory(Pageable pageable) throws NotFoundException {

		Page<AppCategory> appCategories = appCategoryService.findAll(pageable);
		if (appCategories.getTotalElements() > 0) {
			LOG.debug("[getAllAppCategory]: getAllAppCategory request is processed successfully.");
			return new ResponseEntity<>(appCategories, HttpStatus.OK);
		} else {
			LOG.debug("[getAllAppCategory]: getAllAppCategory request is received.");
			throw new NotFoundException("App Categories not found!");
		}
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@ApiOperation(notes = "Returns an App Category specified by appCategoryName.", value = "Getting an App Category", nickname = "getAppCategorybyName", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/appcategory", params="name")
	public ResponseEntity<?> getAppCategoryByName(@RequestParam("name") String name) throws NotFoundException {

		AppCategory appCategory = appCategoryService.findByName(name);
		if (appCategory != null) {
			LOG.debug("[getAppCategoryByName]: getAppCategoryByName request is processed successfully. name: {}", name);
			return new ResponseEntity<>(appCategory, HttpStatus.OK);
		} else {
			LOG.debug("[getAppCategoryByName]: getAppCategoryByName request is received. name: {}", name);
			throw new NotFoundException("AppCategory not found!");
		}

	}
}
