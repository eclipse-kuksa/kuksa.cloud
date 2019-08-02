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
import org.eclipse.kuksa.appstore.model.AppCategory;
import org.eclipse.kuksa.appstore.model.Oem;
import org.eclipse.kuksa.appstore.model.hawkbit.Result;
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

	public Result<?> updateOem(Long oemId, Oem oem)
			throws NotFoundException, BadRequestException, AlreadyExistException {

		Oem currentOEM = oemRepository.findById(oemId);
		if (currentOEM == null) {
			throw new NotFoundException(" currentOEM not found. appId: " + oemId);
		} else if (oem.getName() == null || oem.getName().equals("")
				|| oem.getName().contains(" ")) {

			throw new BadRequestException("OEM name is mandatory field!");

		} else if (!currentOEM.getName().equals(oem.getName())) {
			if (oemRepository.findByName(oem.getName()) != null) {
				throw new AlreadyExistException(
						"New Oem name already exist. New oem name: " + oem.getName());
			}
		}
		oem.setId(currentOEM.getId());
		oemRepository.save(oem);
		return Result.success(HttpStatus.OK, oem);

	}
	public void deleteOem(Oem oem) {

		oemRepository.delete(oem);

	}

	public void deleteOem(Long oemId) throws NotFoundException, BadRequestException {
		Oem currentOem = oemRepository.findById(oemId);
		if (currentOem == null) {
			throw new NotFoundException("Oem not found. userId: " + oemId);
		} else {
			try {
			oemRepository.delete(currentOem);} catch (Exception e) {
				throw new BadRequestException(
						"This OEM is being used by users. Please update the users's OEM before deleting.");
			}
		}
	}

	public List<Oem> findAll() {

		return oemRepository.findAll();
	}

	public Page<Oem> findAll(Pageable pageable) {

		return oemRepository.findAll(pageable);
	}

	public List<String> getAllId() {

		List<String> list = new ArrayList<>();
		for (Long component : oemRepository.getAllId()) {
			list.add(component.toString());
		}
		return list;
	}
}
