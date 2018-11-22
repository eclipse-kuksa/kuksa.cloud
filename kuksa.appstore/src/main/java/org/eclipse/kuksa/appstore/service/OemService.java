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
package org.eclipse.kuksa.appstore.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.Oem;
import org.eclipse.kuksa.appstore.model.Result;
import org.eclipse.kuksa.appstore.repo.OemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OemService {
	@Autowired
	OemRepository oemRepository;

	public Result<?> createOem(Oem oem) throws AlreadyExistException, BadRequestException {

		if (oem.getName() == null || oem.getName().equals("") || oem.getName().contains(" ")) {

			throw new BadRequestException("Name is mandatory field!");

		} else if (oemRepository.findByNameIgnoreCase(oem.getName()) != null) {
			throw new AlreadyExistException("Oem name already exist. name: " + oem.getName());
		} else {
			oemRepository.save(oem);
		}
		return Result.success(HttpStatus.CREATED, oem);

	}

	public Oem findById(Long id) {

		return oemRepository.findById(id);

	}

	public Oem findByName(String name) {

		return oemRepository.findByNameIgnoreCase(name);

	}

	public void updateOem(Oem oem) {

		oemRepository.save(oem);

	}

	/*
	 * public Result<?> updateOem(String oemId, Oem oem) throws NotFoundException,
	 * BadRequestException, AlreadyExistException {
	 * 
	 * Oem currentOem = OemRepository.findById(Long.parseLong(oemId)); if
	 * (currentOem == null) { throw new NotFoundException("Oem not found. appId: " +
	 * oemId); } else if (oem.getName() == null || oem.getName().equals("") ||
	 * oem.getName().contains(" ")) {
	 * 
	 * throw new BadRequestException("Oem name and version are mandatory field!");
	 * 
	 * } else if (!currentOem.getName().equals(oem.getName())) { if
	 * (OemRepository.findByName(oem.getName()) != null) { throw new
	 * AlreadyExistException( "New Oem name already exist. New name: " +
	 * oem.getName()); } } oem.setId(currentOem.getId()); OemRepository.save(oem);
	 * return Result.success(HttpStatus.OK, oem);
	 * 
	 * }
	 */
	public void deleteOem(Oem oem) {

		oemRepository.delete(oem);

	}

	public void deleteOem(String oemId) throws NotFoundException {
		Oem currentOem = oemRepository.findById(Long.parseLong(oemId));
		if (currentOem == null) {
			throw new NotFoundException("Oem not found. userId: " + oemId);
		} else {
			oemRepository.delete(currentOem);
		}
	}

	public List<Oem> findAll() {

		return oemRepository.findAll();
	}

	/*
	 * public Page<Oem> findAll(Pageable pageable) {
	 * 
	 * return OemRepository.findAll(pageable); }
	 */
	public List<String> getAllId() {

		List<String> list = new ArrayList<>();
		for (BigInteger component : oemRepository.getAllId()) {
			list.add(component.toString());
		}
		return list;
	}
}
