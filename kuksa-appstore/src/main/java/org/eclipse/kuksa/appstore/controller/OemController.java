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
import org.eclipse.kuksa.appstore.model.Oem;
import org.eclipse.kuksa.appstore.model.Result;
import org.eclipse.kuksa.appstore.service.OemService;
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
@Api(value = "/api/1.0", description = "OEM API", tags = "OEM", consumes = "application/json")
public class OemController {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	OemService oemService;

	@ApiOperation(notes = "Returns the OEM specified by the oemId parameter.", value = "Getting an OEM", nickname = "getOEM", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/oem/{oemId}")
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

	@ApiOperation(notes = "Creates an OEM defined in the request JSON body. Id field should not implemented in post request JSON body because of that it is already given by server.", value = "Create an Oem", nickname = "createOem", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PostMapping("/oem")
	public ResponseEntity<?> createOem(@Valid @RequestBody Oem oem) throws AlreadyExistException, BadRequestException {
		Result<?> response = oemService.createOem(oem);
		if (response.isSuccess()) {
			LOG.debug("[createOem]: createOem request is processed successfully. oem: {}", oem);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.CREATED);
		} else {
			LOG.debug("[createOem]: createOem request is received. oem: {}", oem);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(notes = "Updates an OEM defined in the request JSON body.", value = "Updating an OEM", nickname = "updateOEM", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@PutMapping("/oem/{oemId}")
	public ResponseEntity<?> updateOEM(@PathVariable Long oemId, @Valid @RequestBody Oem oem)
			throws AlreadyExistException, BadRequestException, NotFoundException {

		Result<?> response = oemService.updateOem(oemId, oem);
		if (response.isSuccess()) {
			LOG.debug("[updateOEM]: updateOEM request is processed successfully. oem: {}", oem);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
		} else {
			LOG.debug("[updateOEM]: updateOEM request is received. oem: {}", oem);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(notes = "Deletes an OEM specified by oemId parameter.", value = "Deleting an OEM", nickname = "deleteOEM", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@DeleteMapping("/oem/{oemId}")
	public ResponseEntity<?> deleteOEM(@PathVariable Long oemId) throws NotFoundException {
		LOG.debug("[deleteOEM]: Delete OEM request is received. oemId: {}", oemId);
		oemService.deleteOem(oemId);

		LOG.debug("[deleteOEM] Delete OEM is processed successfully. oemId: {}", oemId);
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	@ApiOperation(notes = "Returns all OEM.", value = "Getting all OEM", nickname = "getAllOEM", produces = "application/json", authorizations = @Authorization(value = "api_key"))
	@ApiImplicitParam(name = "Authorization", value = "Token Format: 'base64(username: password)'", required = true, dataType = "String", paramType = "Header", defaultValue = "Basic Token")
	@GetMapping(value = "/oem")
	public ResponseEntity<?> getAllOEM(Pageable pageable) throws NotFoundException {

		Page<Oem> oems = oemService.findAll(pageable);
		if (oems.getTotalElements() > 0) {
			LOG.debug("[getAllOEM]: getAllOEM request is processed successfully.");
			return new ResponseEntity<>(oems, HttpStatus.OK);
		} else {
			LOG.debug("[getAllOEM]: getAllOEM request is received.");
			throw new NotFoundException("OEMs not found!");
		}
	}
}