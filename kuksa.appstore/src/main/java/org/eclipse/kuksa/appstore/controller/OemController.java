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
import org.eclipse.kuksa.appstore.model.Oem;
import org.eclipse.kuksa.appstore.model.Result;
import org.eclipse.kuksa.appstore.service.OemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@RestController
@RequestMapping("/api/1.0")
@Api(value = "/api/1.0", description = "OEM API Rest Controller", tags = "OEM API", consumes = "application/json")
public class OemController {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	OemService oemService;

	@ApiOperation(notes = "This process is used to get an OEM. Id parameter should given in get operation.", value = "Getting an OEM", nickname = "getOEM", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/oems/{oemId}")
	public ResponseEntity<?> getOembyId(@PathVariable Long oemId) throws NotFoundException {

		Oem oem = oemService.findById(oemId);
		if (oem != null) {
			LOG.debug("[getOembyId]: getOembyId request is processed successfully. Oem: {}", oemId);
			return new ResponseEntity<>(oem, HttpStatus.OK);
		} else {
			LOG.debug("[getOembyId]: getOembyId request is received. Oem: {}", oemId);
			throw new NotFoundException("User not found!");
		}

	}

	@ApiOperation(notes = "This process is used to create an oem with OEM model. Id parameter should not implemented in post operation because of that it is already given by server.", value = "Create an User", nickname = "createUser", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PostMapping("/oem")
	public ResponseEntity<?> createOem(@Valid @RequestBody Oem oem) throws AlreadyExistException, BadRequestException {
		Result<?> response = oemService.createOem(oem);
		if (response.isSuccess()) {
			LOG.debug("[createOem]: createOem request is processed successfully. oem: {}", oem);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
		} else {
			LOG.debug("[createOem]: createOem request is received. oem: {}", oem);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}
}