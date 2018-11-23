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

	public Result<?> createApp(App app) throws AlreadyExistException, BadRequestException {

		if (app.getName() == null || app.getVersion() == null || app.getName().equals("") || app.getVersion().equals("")
				|| app.getName().contains(" ") || app.getVersion().contains(" ")) {

			throw new BadRequestException("Name and Version are mandatory field!");

		} else if (appRepository.findByNameIgnoreCase(app.getName()) != null) {
			throw new AlreadyExistException("App name already exist. name: " + app.getName());
		} else {
			appRepository.save(app);
		}
		return Result.success(HttpStatus.CREATED, app);

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
		} else if (app.getName() == null || app.getVersion() == null || app.getName().equals("")
				|| app.getVersion().equals("") || app.getName().contains(" ") || app.getVersion().contains(" ")) {

			throw new BadRequestException("App name and version are mandatory field!");

		} else if (!currentApp.getName().equals(app.getName())) {
			if (appRepository.findByNameIgnoreCase(app.getName()) != null) {
				throw new AlreadyExistException("New App name already exist. New name: " + app.getName());
			}
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

	public void incrementAppDownloadCount(Long appid) {

		App app = appRepository.findById(appid);
		app.setDownloadcount(app.getDownloadcount() + 1);
		appRepository.save(app);

	}

	public App incrementAppDownloadCount(App app) {

		app.setDownloadcount(app.getDownloadcount() + 1);

		return app;

	}

	public Page<App> findByNameStartsWithIgnoreCaseAndInstalledusersUserName(String appname, String username,
			Pageable pageable) {

		return appRepository.findByNameStartsWithIgnoreCaseAndInstalledusersUsername(appname, username, pageable);

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

	public Result<?> purchaseApp(Long userId, Long appId) throws NotFoundException {
		List<User> ownerUserList;
		App currentApp = appRepository.findById(appId);
		if (currentApp == null) {
			throw new NotFoundException("App not found. appId: " + appId);
		}
		ownerUserList = currentApp.getOwnerusers();
		ownerUserList.add(userRepository.findById(userId));
		currentApp.setOwnerusers(ownerUserList);
		appRepository.save(currentApp);
		return Result.success(HttpStatus.OK, currentApp);
	}
}
