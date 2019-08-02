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
import org.eclipse.kuksa.appstore.model.hawkbit.Result;
import org.eclipse.kuksa.appstore.repo.AppCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AppCategoryService {
	@Autowired
	AppCategoryRepository appCategoryRepository;

	public Result<?> createAppCategory(AppCategory appCategory) throws AlreadyExistException, BadRequestException {

		if (appCategory.getName() == null || appCategory.getName().equals("") || appCategory.getName().contains(" ")) {

			throw new BadRequestException("Name is mandatory field!");

		} else if (appCategoryRepository.findByNameStartsWithIgnoreCase(appCategory.getName()).size() > 0) {
			throw new AlreadyExistException("AppCategory name already exist. name: " + appCategory.getName());
		} else {
			appCategoryRepository.save(appCategory);
		}
		return Result.success(HttpStatus.CREATED, appCategory);

	}

	public AppCategory findById(Long id) {

		return appCategoryRepository.findById(id);

	}

	public AppCategory findByName(String name) {

		return appCategoryRepository.findByName(name);

	}

	public void updateAppCategory(AppCategory appCategory) {

		appCategoryRepository.save(appCategory);

	}

	public Result<?> updateAppCategory(String appCategoryId, AppCategory appCategory)
			throws NotFoundException, BadRequestException, AlreadyExistException {

		AppCategory currentAppCategory = appCategoryRepository.findById(Long.parseLong(appCategoryId));
		if (currentAppCategory == null) {
			throw new NotFoundException("AppCategory not found. appId: " + appCategoryId);
		} else if (appCategory.getName() == null || appCategory.getName().equals("")
				|| appCategory.getName().contains(" ")) {

			throw new BadRequestException("AppCategory name is mandatory field!");

		} else if (!currentAppCategory.getName().equals(appCategory.getName())) {
			if (appCategoryRepository.findByName(appCategory.getName()) != null) {
				throw new AlreadyExistException(
						"New AppCategory name already exist. New name: " + appCategory.getName());
			}
		}
		appCategory.setId(currentAppCategory.getId());
		appCategoryRepository.save(appCategory);
		return Result.success(HttpStatus.OK, appCategory);

	}

	public void deleteAppCategory(AppCategory appCategory) {

		appCategoryRepository.delete(appCategory);

	}

	public void deleteAppCategory(String appCategoryId) throws NotFoundException, BadRequestException {
		AppCategory currentAppCategory = appCategoryRepository.findById(Long.parseLong(appCategoryId));
		if (currentAppCategory == null) {
			throw new NotFoundException("AppCategory not found. userId: " + appCategoryId);
		} else {
			try {
				appCategoryRepository.delete(currentAppCategory);
			} catch (Exception e) {
				throw new BadRequestException(
						"This category is being used by applications. Please update the applications's categories before deleting.");
			}
		}
	}

	public List<AppCategory> findAll() {

		return appCategoryRepository.findAll();
	}

	public Page<AppCategory> findAll(Pageable pageable) {

		return appCategoryRepository.findAll(pageable);
	}

	public List<String> getAllId() {

		List<String> list = new ArrayList<>();
		for (Long component : appCategoryRepository.getAllId()) {
			list.add(component.toString());
		}
		return list;
	}
}
