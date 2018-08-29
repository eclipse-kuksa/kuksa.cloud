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

@RestController
@RequestMapping("/api/1.0")
public class AppController {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	@Autowired
	AppService appService;

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

	@PostMapping("/app")
	public ResponseEntity<?> createApp(@Valid @RequestBody App app)
			throws AlreadyExistException, BadRequestException {
		Result<?> response = appService.createApp(app);
		if (response.isSuccess()) {
			LOG.debug("[createApp]: createApp request is processed successfully. app: {}", app);
			return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
		} else {
			LOG.debug("[createApp]: createApp request is received. app: {}", app);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}
	}

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

	@DeleteMapping("/app/{appId}")
	public ResponseEntity<?> deleteApp(@PathVariable String appId) throws NotFoundException {
		LOG.debug("[deleteApp]: Delete App request is received. appId: {}", appId);
		appService.deleteApp(appId);

		LOG.debug("[deleteApp] Delete App is processed successfully. appId: {}", appId);
		return new ResponseEntity<>(null, HttpStatus.OK);
	}
	
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

}
