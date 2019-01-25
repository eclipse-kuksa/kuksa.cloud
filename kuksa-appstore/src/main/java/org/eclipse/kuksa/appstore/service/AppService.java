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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.kuksa.appstore.client.HawkbitFeignClient;
import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.model.hawkbit.AssignedResult;
import org.eclipse.kuksa.appstore.model.hawkbit.Distribution;
import org.eclipse.kuksa.appstore.model.hawkbit.DistributionResult;
import org.eclipse.kuksa.appstore.model.hawkbit.Result;
import org.eclipse.kuksa.appstore.model.hawkbit.Rule;
import org.eclipse.kuksa.appstore.model.hawkbit.RuleMain;
import org.eclipse.kuksa.appstore.model.hawkbit.SoftwareModule;
import org.eclipse.kuksa.appstore.model.hawkbit.SoftwareModuleResult;
import org.eclipse.kuksa.appstore.model.hawkbit.Target;
import org.eclipse.kuksa.appstore.repo.AppCategoryRepository;
import org.eclipse.kuksa.appstore.repo.AppRepository;
import org.eclipse.kuksa.appstore.repo.UserRepository;
import org.eclipse.kuksa.appstore.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;

import feign.Response;

@Service
public class AppService {
	@Autowired
	AppRepository appRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	AppCategoryRepository appCategoryRepository;
	@Autowired
	HawkbitFeignClient hawkbitFeignClient;
	@Autowired
	UserService userService;

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
		return createApptoHawkbit(app);

	}

	public Result<?> createApptoHawkbit(App app) throws BadRequestException {
		App currentApp = appRepository.findByName(app.getName());
		List<SoftwareModule> responsesoftwareModule;
		try {

			List<SoftwareModule> softwareModuleList = new ArrayList<>();
			softwareModuleList.add(new SoftwareModule(app.getName(), app.getDescription(), app.getVersion(),
					"application", app.getOwner()));
			responsesoftwareModule = hawkbitFeignClient.createSoftwaremodules(softwareModuleList);
		} catch (Exception e) {
			appRepository.delete(currentApp);
			throw new BadRequestException("Hawkbit connection error. Check your Hawkbit's IP in the propreties file!");
		}
		if (responsesoftwareModule.size() > 0) {

			return Result.success(HttpStatus.CREATED, appRepository.findByName(app.getName()));
		} else {
			appRepository.delete(currentApp);
			throw new BadRequestException("App could not be saved to Hawkbit and Appstore!");
		}

	}

	public App findById(Long id) {

		return appRepository.findById(id);

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

		} else if (!currentApp.getName().equals(app.getName())
				&& appRepository.findByNameIgnoreCase(app.getName()) != null) {

			throw new AlreadyExistException("New App name already exist. New name: " + app.getName());

		} else if (app.getAppcategory() == null) {

			throw new BadRequestException("App Category is mandatory field!");

		} else if (app.getAppcategory() != null
				&& appCategoryRepository.findById(app.getAppcategory().getId()) == null) {

			throw new BadRequestException("App Category should exist!");
		}
		app.setId(currentApp.getId());

		if (!currentApp.getDescription().equals(app.getDescription())
				|| !currentApp.getOwner().equals(app.getOwner())) {

			SoftwareModuleResult softwareModuleResult;
			try {
				softwareModuleResult = hawkbitFeignClient
						.getSoftwaremoduleByName(Utils.createFIQLEqual("name", currentApp.getName()));
			} catch (Exception e) {
				throw new BadRequestException(
						"Hawkbit connection error. Check your Hawkbit's IP in the propreties file!");
			}

			if (softwareModuleResult.getSize() > 0) {
				Integer softawareModuleId = Utils.getExistsSoftwareModule(softwareModuleResult.getContent());

				Response responseSoftwareModule = hawkbitFeignClient.updateSoftwareModule(softawareModuleId.toString(),
						new SoftwareModule(app.getDescription(), app.getOwner()));

				if (responseSoftwareModule.status() == HttpStatus.OK.value()) {
					appRepository.save(app);
				}
				return Result.success(HttpStatus.OK, app);

			} else {
				throw new BadRequestException("App not found on Hawkbit!");
			}
		} else {
			appRepository.save(app);
			return Result.success(HttpStatus.OK, app);
		}

	}

	public void deleteApp(String appId) throws NotFoundException {
		App currentApp = appRepository.findById(Long.parseLong(appId));
		if (currentApp == null) {
			throw new NotFoundException("App not found. appId: " + appId);
		} else {

			SoftwareModuleResult softwareModuleResult;
			try {
				softwareModuleResult = hawkbitFeignClient
						.getSoftwaremoduleByName(Utils.createFIQLEqual("name", currentApp.getName()));
			} catch (Exception e) {
				throw new NotFoundException(
						"Hawkbit connection error. Check your Hawkbit's IP in the propreties file!");
			}

			if (softwareModuleResult.getSize() > 0) {
				Integer softawareModuleId = Utils.getExistsSoftwareModule(softwareModuleResult.getContent());

				Response responseSoftwareModule = hawkbitFeignClient
						.deletesoftwareModuleById(softawareModuleId.toString());

				if (responseSoftwareModule.status() == HttpStatus.OK.value()) {

					appRepository.delete(currentApp);

				} else {
					throw new NotFoundException(responseSoftwareModule.body().toString());
				}
			} else {
				throw new NotFoundException("Software Module not found on Hawkbit. . Software:"
						+ Utils.createSoftwareName(currentApp.getId()));
			}

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

	public Result<?> InstallApp(String targetDeviceName, Long userId, Long appId)
			throws NotFoundException, BadRequestException, AlreadyExistException {

		App currentApp = appRepository.findById(appId);
		if (currentApp == null) {
			throw new NotFoundException("App not found. appId: " + appId);
		}
		User currentUser = userRepository.findById(userId);
		if (currentUser == null) {
			throw new NotFoundException("User not found. userId: " + userId);
		}
		List<String> listOfTargets = getListOfTargets();
		boolean isOwner = userService.isUsersAppOwner(currentUser.getId().toString(), currentApp.getId().toString(),
				getListOfOem(listOfTargets));

		if (!isOwner) {
			throw new BadRequestException("This User is not owner of this app!");

		}
		if (targetDeviceName == null) {

			throw new BadRequestException("targetDeviceName should not be empty!");
		}
		SoftwareModuleResult currentSoftwareModuleResult = hawkbitFeignClient
				.getSoftwaremoduleByName("name==" + currentApp.getName());
		DistributionResult lastDistributionResult = hawkbitFeignClient
				.getDistributionByName("name==" + targetDeviceName);
		String version = null;

		if (lastDistributionResult.getSize() > 0) {
			version = lastDistributionResult.getContent().get(lastDistributionResult.getContent().size() - 1)
					.getVersion();
		} else {
			version = "0";
		}

		int newVersion = Integer.parseInt(version) + 1;
		boolean isAlreadyAssigned = false;

		Rule ruleNew = new Rule();
		ruleNew.setForcetime("1530893371603");
		ruleNew.setType("timeforced");
		RuleMain rulemain = new RuleMain();
		rulemain.setDuration("00:10:00");
		rulemain.setSchedule("0 37 8 22 6 ? 2019");
		rulemain.setTimezone("+00:00");
		ruleNew.setMaintenanceWindow(rulemain);

		List<SoftwareModule> softwareModules = new ArrayList<SoftwareModule>();
		if (lastDistributionResult.getSize() > 0) {
			softwareModules = lastDistributionResult.getContent().get(lastDistributionResult.getSize() - 1)
					.getModules();

			if (!Utils.isAppAlreadyInstalled(currentSoftwareModuleResult.getContent().get(0), softwareModules)) {

				softwareModules.addAll(currentSoftwareModuleResult.getContent());
			} else {
				isAlreadyAssigned = true;
			}
		} else {
			softwareModules.addAll(currentSoftwareModuleResult.getContent());
		}
		if (isAlreadyAssigned == false) {
			Distribution newDistribution = new Distribution(targetDeviceName, Integer.toString(newVersion));
			newDistribution.setName(targetDeviceName);
			newDistribution.setDescription(currentApp.getDescription());
			newDistribution.setType("app");
			newDistribution.setModules(softwareModules);
			Response responseCreateDistribution = hawkbitFeignClient
					.createDistributionSets(Arrays.asList(newDistribution));
			if (responseCreateDistribution.status() != HttpStatus.CREATED.value()) {
				throw new BadRequestException("Fail Creating Distribution");

			}
			DistributionResult newDistributionResult = hawkbitFeignClient
					.getDistributionByName(Utils.createFIQLEqual("name", targetDeviceName));
			if (newDistributionResult.getSize() > 0) {

				ruleNew.setId(newDistributionResult.getContent().get(newDistributionResult.getSize() - 1).getId());
			} else {

				throw new BadRequestException("This app not found on Hawkbit!");
			}
		} else {

			ruleNew.setId(lastDistributionResult.getContent().get(lastDistributionResult.getSize() - 1).getId());

		}

		AssignedResult response = hawkbitFeignClient.sendApptoDevice(targetDeviceName, ruleNew);
		if (response.getAssigned() > 0) {

			List<User> list = currentApp.getInstalledusers();
			if (!Utils.isUserAlreadyOwner(currentUser, list)) {

				currentApp = incrementAppDownloadCount(currentApp);
				list.add(currentUser);

				currentApp.setInstalledusers(list);
				updateApp(currentApp.getId().toString(), currentApp);
			}
			return Result.success(HttpStatus.OK);
		} else if (response.getAlreadyAssigned() > 0) {

			throw new BadRequestException("The updating action is already assigned for selected device.");

		} else {
			throw new BadRequestException("The updating action hasnt been sent to Hawkbit for selected device.");

		}

	}

	public List<String> getListOfOem(List<String> listOfTargets) throws BadRequestException {
		List<String> listOfOem = new ArrayList<>();
		for (int i = 0; i < listOfTargets.size(); i++) {

			try {
				String deviceName = listOfTargets.get(i);
				int index = deviceName.indexOf("_");
				String oem = deviceName.substring(0, index);
				listOfOem.add(oem);
			} catch (Exception e) {
				throw new BadRequestException(
						"The names of device/target are invalid!, The names of device/target should be like this format OEM_TARGETNAME .");

			}
		}
		return listOfOem;
	}

	public List<String> getListOfTargets() throws BadRequestException {
		List<String> listOfTargets = new ArrayList<>();

		String dis = VaadinSession.getCurrent().getAttribute("user").toString();
		dis = "" + "*" + dis + "*";
		dis = "description==" + dis;
		List<Target> deviceList = new ArrayList<>();
		try {
			deviceList = hawkbitFeignClient.getTargetsByDes(dis, "name:ASC").getContent();
		} catch (Exception e) {
			throw new BadRequestException(
					"Not Found Hawkbit Instance, Make sure that you connect any Hawkbit instance with this AppStore!");
		}
		for (Target target : deviceList) {
			listOfTargets.add(target.getControllerId());
		}
		return listOfTargets;

	}
}
