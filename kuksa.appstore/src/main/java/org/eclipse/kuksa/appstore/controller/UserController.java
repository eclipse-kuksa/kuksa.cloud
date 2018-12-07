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
import org.eclipse.kuksa.appstore.model.Result;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.service.AppService;
import org.eclipse.kuksa.appstore.service.UserService;
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
@Api(value = "/api/1.0", description = "User API", tags = "User", consumes = "application/json")
public class UserController {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	@Autowired
	UserService userService;
	@Autowired
	AppService appService;

	@ApiOperation(notes = "Returns the User specified by the userId parameter. The response includes all details about the User.", value = "Getting an User", nickname = "getUserbyId", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/user/{userId}")
	public ResponseEntity<?> getUserbyId(@PathVariable String userId) throws NotFoundException {

		User user = userService.findById(userId);
		if (user != null) {
			LOG.debug("[getUserbyId]: getUserbyId request is processed successfully. Device: {}", userId);
			return new ResponseEntity<>(user, HttpStatus.OK);
		} else {
			LOG.debug("[getUserbyId]: getUserbyId request is received. Device: {}", userId);
			throw new NotFoundException("User not found!");
		}

	}

	@ApiOperation(notes = "Creates an User defined in the request JSON body. Id field should not implemented in post request JSON body because of that it is already given by server.", value = "Create an User", nickname = "createUser", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PostMapping("/user")
	public ResponseEntity<?> createUser(@Valid @RequestBody User user)
			throws AlreadyExistException, BadRequestException {
		Result<?> response = userService.createUser(user.getUsername(), user.getPassword(), user.getUserType(),
				user.getOem(), user.getMembers());
		if (response.isSuccess()) {
			LOG.debug("[createUser]: createUser request is processed successfully. user: {}", user);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.CREATED);
		} else {
			LOG.debug("[createUser]: createUser request is received. user: {}", user);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(notes = "Updates the User identified by the appId parameter and the JSON body.", value = "Updating an User", nickname = "updateUser", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PutMapping("/user/{userId}")
	public ResponseEntity<?> updateUser(@PathVariable String userId, @Valid @RequestBody User user)
			throws AlreadyExistException, BadRequestException, NotFoundException {

		Result<?> response = userService.updateUser(userId, user);
		if (response.isSuccess()) {
			LOG.debug("[updateUser]: updateUser request is processed successfully. user: {}", user);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
		} else {
			LOG.debug("[updateUser]: updateUser request is received. user: {}", user);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(notes = "Deletes the User specified by the userId path parameter.", value = "Deleting an User", nickname = "deleteUser", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@DeleteMapping("/user/{userId}")
	public ResponseEntity<?> deleteUser(@PathVariable String userId) throws NotFoundException {
		LOG.debug("[deleteUser]: Delete User request is received. userId: {}", userId);
		userService.deleteUser(userId);

		LOG.debug("[deleteUser] Delete User is processed successfully. userId: {}", userId);
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	@ApiOperation(notes = "Returns all Users.", value = "Getting all User", nickname = "getAllUser", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/user")
	public ResponseEntity<?> getAllUser(Pageable pageable) throws NotFoundException {

		Page<User> users = userService.findAll(pageable);

		LOG.debug("[getUserbyId]: getUserbyId request is processed successfully.");
		return new ResponseEntity<>(users, HttpStatus.OK);

	}

	@ApiOperation(notes = "This process is used to validate a given user.", value = "Validating an User", nickname = "validationUser", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PostMapping("/user/validation")
	public ResponseEntity<?> validationUser(@RequestBody User user) throws NotFoundException, BadRequestException {

		User currentUser = userService.findByUserNameAndPassword(user.getUsername(), user.getPassword());
		if (currentUser != null) {
			LOG.debug("[validationUser]: validationUser request is processed successfully. user: {}", currentUser);
			return new ResponseEntity<>(currentUser, HttpStatus.OK);
		} else {
			LOG.debug("[validationUser]: validationUser request is received. user: {}", currentUser);
			throw new NotFoundException("User not found!");
		}
	}

}
