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

import java.util.List;

import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.Result;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.repo.AppCategoryRepository;
import org.eclipse.kuksa.appstore.repo.AppRepository;
import org.eclipse.kuksa.appstore.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AppService {
	@Autowired
	AppRepository appRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	AppCategoryRepository appCategoryRepository;

	public Result<?> createApp(App app) throws AlreadyExistException, BadRequestException {

		if (app.getName() == null || app.getName().equals("")) {

			throw new BadRequestException("Name is mandatory field!");

		} else if (app.getName().contains(" ")) {

			throw new BadRequestException("Name should not contain space character!");

		} else if (app.getVersion() == null || app.getVersion().equals("")) {

			throw new BadRequestException("Version is mandatory field!");

		} else if (app.getDescription() == null || app.getDescription().equals("")) {

			throw new BadRequestException("Description is mandatory field!");

		} else if (app.getOwner() == null || app.getOwner().equals("")) {

			throw new BadRequestException("Owner is mandatory field!");

		} else if (appRepository.findByNameIgnoreCase(app.getName()) != null) {

			throw new AlreadyExistException("App name already exist. name: " + app.getName());

		} else if (app.getAppcategory() == null) {

			throw new BadRequestException("App Category is mandatory field!");

		} else if (app.getAppcategory() != null
				&& appCategoryRepository.findById(app.getAppcategory().getId()) == null) {

			throw new BadRequestException("App Category should exist!");
		}
		appRepository.save(app);

		return Result.success(HttpStatus.CREATED, appRepository.findByName(app.getName()));

	}

	public App findById(Long id) {

		return appRepository.findById(id);

	}

	public void updateApp(App appObject) {

		appRepository.save(appObject);

	}

	public Result<?> updateApp(String appId, App app)
			throws NotFoundException, BadRequestException, AlreadyExistException {

		App currentApp = appRepository.findById(Long.parseLong(appId));

		if (currentApp == null) {
			throw new NotFoundException("App not found. appId: " + appId);
		} else if (app.getName() == null || app.getName().equals("")) {

			throw new BadRequestException("Name is mandatory field!");

		} else if (app.getName().contains(" ")) {

			throw new BadRequestException("Name should not contain space character!");

		} else if (app.getVersion() == null || app.getVersion().equals("")) {

			throw new BadRequestException("Version is mandatory field!");

		} else if (app.getDescription() == null || app.getDescription().equals("")) {

			throw new BadRequestException("Description is mandatory field!");

		} else if (app.getOwner() == null || app.getOwner().equals("")) {

			throw new BadRequestException("Owner is mandatory field!");

		} else if (!currentApp.getName().equals(app.getName()) && appRepository.findByNameIgnoreCase(app.getName()) != null) {

				throw new AlreadyExistException("New App name already exist. New name: " + app.getName());

		} else if (app.getAppcategory() == null) {

			throw new BadRequestException("App Category is mandatory field!");

		} else if (app.getAppcategory() != null
				&& appCategoryRepository.findById(app.getAppcategory().getId()) == null) {

			throw new BadRequestException("App Category should exist!");
		}
		app.setId(currentApp.getId());
		appRepository.save(app);
		return Result.success(HttpStatus.OK, app);

	}

	public void deleteApp(App appObject) {

		appRepository.delete(appObject);

	}

	public void deleteApp(String appId) throws NotFoundException {
		App currentApp = appRepository.findById(Long.parseLong(appId));
		if (currentApp == null) {
			throw new NotFoundException("App not found. userId: " + appId);
		} else {
			appRepository.delete(currentApp);
		}
	}

	public List<App> findAll() {

		return appRepository.findAll();

	}

	public List<App> findByNameStartsWithIgnoreCase(String name) {

		return appRepository.findByNameStartsWithIgnoreCase(name);

	}

	public Page<App> findAll(Pageable pageable) {

		return appRepository.findAll(pageable);

	}

	public Page<App> findByNameStartsWithIgnoreCase(String name, Pageable pageable) {

		return appRepository.findByNameStartsWithIgnoreCase(name, pageable);

	}

	public App incrementAppDownloadCount(App app) {

		app.setDownloadcount(app.getDownloadcount() + 1);

		return app;

	}

	public Page<App> findByNameStartsWithIgnoreCaseAndInstalledusersUserName(String appname, String username,
			Pageable pageable) {

		return appRepository.findByNameStartsWithIgnoreCaseAndInstalledusersUsername(appname, username, pageable);

	}

	public Page<App> findByInstalledusersId(Long userid, Pageable pageable) {

		return appRepository.findByInstalledusersId(userid, pageable);

	}

	public Page<App> findByNameStartsWithIgnoreCaseAndInstalledusersId(String appname, Long userid, Pageable pageable) {

		return appRepository.findByNameStartsWithIgnoreCaseAndInstalledusersId(appname, userid, pageable);

	}

	public Page<App> findByIdIn(List<Long> myappsid, Pageable pageable) {

		return appRepository.findByIdIn(myappsid, pageable);
	}

	public Page<App> findByAppcategoryId(Long id, Pageable pageable) {

		return appRepository.findByAppcategoryId(id, pageable);

	}

	public Page<App> findByNameStartsWithIgnoreCaseAndAppcategoryId(String name, Long id, Pageable pageable) {

		return appRepository.findByNameStartsWithIgnoreCaseAndAppcategoryId(name, id, pageable);

	}

	public Page<App> findUsersApps(String userId, List<String> oemList, Pageable pageable) {

		return appRepository.findUsersApps(userId, oemList, pageable);

	}

	public Result<?> purchaseApp(Long userId, Long appId) throws NotFoundException, BadRequestException {
		List<User> ownerUserList;
		App currentApp = appRepository.findById(appId);
		if (currentApp == null) {
			throw new NotFoundException("App not found. appId: " + appId);
		}
		User currentUser = userRepository.findById(userId);
		if (currentUser == null) {
			throw new NotFoundException("User not found. userId: " + userId);
		}
		if (currentApp.getOwnerusers().contains(currentUser)) {
			throw new BadRequestException("This User already purchased this app!");
		}
		ownerUserList = currentApp.getOwnerusers();
		ownerUserList.add(currentUser);
		currentApp.setOwnerusers(ownerUserList);
		appRepository.save(currentApp);
		return Result.success(HttpStatus.OK, currentApp);
	}
}
