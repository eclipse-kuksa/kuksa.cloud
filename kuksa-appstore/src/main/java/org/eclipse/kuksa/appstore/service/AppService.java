/*******************************************************************************
 * Copyright (C) 2018-2019 Netas Telekomunikasyon A.S. [and others]
 *  
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *  
 * SPDX-License-Identifier: EPL-2.0
 *  
 * Contributors:
 * Adem Kose, Fatih Ayvaz and Ilker Kuzu (Netas Telekomunikasyon A.S.) - Initial functionality
 * Philipp Heisig (Dortmund University of Applied Sciences and Arts) 
 * Johannes Kristan (Bosch Software Innovation)
 ******************************************************************************/
package org.eclipse.kuksa.appstore.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.kuksa.appstore.client.HawkbitFeignClient;
import org.eclipse.kuksa.appstore.client.HawkbitMultiPartFileFeignClient;
import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.Oem;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.model.hawkbit.Artifact;
import org.eclipse.kuksa.appstore.model.hawkbit.AssignedResult;
import org.eclipse.kuksa.appstore.model.hawkbit.Distribution;
import org.eclipse.kuksa.appstore.model.hawkbit.DistributionResult;
import org.eclipse.kuksa.appstore.model.hawkbit.Result;
import org.eclipse.kuksa.appstore.model.hawkbit.Rule;
import org.eclipse.kuksa.appstore.model.hawkbit.RuleMain;
import org.eclipse.kuksa.appstore.model.hawkbit.SoftwareModule;
import org.eclipse.kuksa.appstore.model.hawkbit.SoftwareModuleResult;
import org.eclipse.kuksa.appstore.model.hawkbit.Target;
import org.eclipse.kuksa.appstore.model.hawkbit.upload.ArtifactFile;
import org.eclipse.kuksa.appstore.repo.AppCategoryRepository;
import org.eclipse.kuksa.appstore.repo.AppRepository;
import org.eclipse.kuksa.appstore.repo.OemRepository;
import org.eclipse.kuksa.appstore.repo.UserRepository;
import org.eclipse.kuksa.appstore.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import feign.Response;

@Service
public class AppService {
	@Autowired
	AppRepository appRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	OemRepository oemRepository;
	@Autowired
	AppCategoryRepository appCategoryRepository;
	@Autowired
	HawkbitFeignClient hawkbitFeignClient;
	@Autowired
	HawkbitMultiPartFileFeignClient hawkbitMultiPartFileFeignClient;
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
						.getSoftwaremoduleByName(Utils.createFIQLEqual("name", currentApp.getName()) + ";"
								+ Utils.createFIQLEqual("version", currentApp.getVersion()));
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
						.getSoftwaremoduleByName(Utils.createFIQLEqual("name", currentApp.getName()) + ";"
								+ Utils.createFIQLEqual("version", currentApp.getVersion()));
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

	public Result<?> uploadArtifactWithAppId(Long appId, String fileName, byte[] b)
			throws BadRequestException, NotFoundException {
		String softwareModuleId = getSoftwareModuleName(appId);
		return uploadArtifactToHawkbit(softwareModuleId, fileName, b);
	}

	public Result<?> uploadArtifactWithSoftwareModuleId(String softwareModuleId, String fileName, byte[] b)
			throws BadRequestException {
		return uploadArtifactToHawkbit(softwareModuleId, fileName, b);
	}

	private Result<?> uploadArtifactToHawkbit(String softwareModuleId, String fileName, byte[] b)
			throws BadRequestException {
		try {
			ArtifactFile appArtifactFile = new ArtifactFile(fileName, fileName, "multipart/form-data", b);
			Response response = hawkbitMultiPartFileFeignClient.uploadFile(softwareModuleId, appArtifactFile);

			if (response.status() == HttpStatus.CREATED.value()) {
				return Result.success(HttpStatus.CREATED);
			} else {
				throw new BadRequestException("App could not be saved to Hawkbit and Appstore!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("Hawkbit connection error. Check your Hawkbit's IP in the propreties file!");
		}
	}

	public Result<?> deleteArtifactWithAppId(Long appId, String artifactId)
			throws BadRequestException, NotFoundException {
		String softwareModuleId = getSoftwareModuleName(appId);
		return deleteArtifactFromHawkbit(softwareModuleId, artifactId);
	}

	public Result<?> deleteArtifactWithSoftwareModuleId(String softwareModuleId, String artifactId)
			throws BadRequestException {
		return deleteArtifactFromHawkbit(softwareModuleId, artifactId);
	}

	private Result<?> deleteArtifactFromHawkbit(String softwareModuleId, String artifactId) throws BadRequestException {
		try {
			Response response = hawkbitFeignClient.deleteArtifactsBysoftwareModuleId(softwareModuleId, artifactId);
			if (response.status() == HttpStatus.OK.value()) {
				return Result.success(HttpStatus.OK);
			} else {
				throw new BadRequestException("App could not be deleted from Hawkbit and Appstore!");
			}
		} catch (Exception e) {
			throw new BadRequestException("Hawkbit connection error. Check your Hawkbit's IP in the propreties file!");
		}
	}

	public Result<?> getArtifactsWithAppId(Long appId) throws BadRequestException, NotFoundException {
		String softwareModuleId = getSoftwareModuleName(appId);
		List<Artifact> artifacts = hawkbitFeignClient.getArtifactsBysoftwareModuleId(softwareModuleId);
		if (!artifacts.isEmpty()) {
			return Result.success(HttpStatus.OK, artifacts);
		} else {
			return Result.fail(HttpStatus.NO_CONTENT);
		}
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
		List<String> listOfTargets = getListOfTargets(userId);
		boolean isOwner = userService.isUsersAppOwner(currentUser.getId().toString(), currentApp.getId(),
				getListOfOem(listOfTargets));

		if (!isOwner) {
			throw new BadRequestException("This User is not owner of this app!");
		}
		if (targetDeviceName == null) {
			throw new BadRequestException("targetDeviceName should not be empty!");
		}
		SoftwareModuleResult currentSoftwareModuleResult = hawkbitFeignClient
				.getSoftwaremoduleByName(Utils.createFIQLEqual("name", currentApp.getName()) + ";"
						+ Utils.createFIQLEqual("version", currentApp.getVersion()));
		DistributionResult lastDistributionResult = hawkbitFeignClient
				.getDistributionByName(Utils.createFIQLEqual("name", targetDeviceName), 1, "id:DESC");
		String version = null;

		if (lastDistributionResult.getSize() > 0) {
			version = lastDistributionResult.getContent().get(0).getVersion();
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

		if (newVersion > 1) {
			softwareModules = lastDistributionResult.getContent().get(lastDistributionResult.getSize() - 1)
					.getModules();

			if (softwareModules.size() == 1 && softwareModules.get(0).getName().equals(Utils.UNINSTALLED_ALL)) {
				softwareModules.remove(0);
			}

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
					.getDistributionByName(Utils.createFIQLEqual("name", targetDeviceName), 1, "id:DESC");
			if (newDistributionResult.getSize() > 0) {
				ruleNew.setId(newDistributionResult.getContent().get(0).getId());
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

	public Result<?> UninstallMultiApp(String targetDeviceName, Long userId, List<Long> appIds)
			throws NotFoundException, BadRequestException, AlreadyExistException {

		User currentUser = userRepository.findById(userId);
		if (currentUser == null) {
			throw new NotFoundException("User not found. userId: " + userId);
		}
		if (targetDeviceName == null) {
			throw new BadRequestException("targetDeviceName should not be empty!");
		}
		Rule ruleNew = new Rule();
		ruleNew.setForcetime("1530893371603");
		ruleNew.setType("timeforced");
		RuleMain rulemain = new RuleMain();
		rulemain.setDuration("00:10:00");
		rulemain.setSchedule("0 37 8 22 6 ? 2019");
		rulemain.setTimezone("+00:00");
		ruleNew.setMaintenanceWindow(rulemain);

		int newVersion = 0;

		List<SoftwareModule> softwareModules = new ArrayList<SoftwareModule>();

		DistributionResult lastDistributionResult = hawkbitFeignClient
				.getDistributionByName(Utils.createFIQLEqual("name", targetDeviceName), 1, "id:DESC");
		if (lastDistributionResult.getSize() == 0) {
			throw new BadRequestException("This application is already not installed to the selected device!");
		}
		softwareModules = lastDistributionResult.getContent().get(lastDistributionResult.getSize() - 1).getModules();
		String version = null;

		if (lastDistributionResult.getSize() > 0) {
			version = lastDistributionResult.getContent().get(0).getVersion();
		} else {
			throw new BadRequestException("Distribution not found for the TargetDevice!");
		}
		newVersion = Integer.parseInt(version) + 1;
		for (Long appId : appIds) {

			App currentApp = appRepository.findById(appId);
			if (currentApp == null) {
				throw new NotFoundException("App not found. appId: " + appId);
			}
			SoftwareModuleResult currentSoftwareModuleResult = hawkbitFeignClient
					.getSoftwaremoduleByName(Utils.createFIQLEqual("name", currentApp.getName()) + ";"
							+ Utils.createFIQLEqual("version", currentApp.getVersion()));

			boolean isAlreadyAssigned = false;

			isAlreadyAssigned = Utils.isAppAlreadyInstalled(currentSoftwareModuleResult.getContent().get(0),
					softwareModules);

			if (isAlreadyAssigned == true) {
				softwareModules = Utils.UninstallApp(currentSoftwareModuleResult.getContent().get(0), softwareModules);
			} else {
				throw new BadRequestException("This App is not installed right now on the device!");
			}
		}

		if (softwareModules.size() == 0) {

			// Check UNINSTALLED_ALL MODULE
			SoftwareModuleResult softwareModuleResult;
			try {
				softwareModuleResult = hawkbitFeignClient
						.getSoftwaremoduleByName(Utils.createFIQLEqual("name", Utils.UNINSTALLED_ALL));
			} catch (Exception e) {
				throw new BadRequestException(
						"Hawkbit connection error. Check your Hawkbit's IP in the propreties file!");
			}

			if (softwareModuleResult.getSize() == 0) {

				List<SoftwareModule> responsesoftwareModule;
				try {
					// Create UNINSTALLED_ALL MODULE
					List<SoftwareModule> softwareModuleList = new ArrayList<>();
					softwareModuleList.add(new SoftwareModule(Utils.UNINSTALLED_ALL,
							"This software module is a Dummy software module. It was created to assign empty distribution to the device. This situation is for uninstalling device's all apps.",
							"0", "application", "KUKSA_APPSTORE"));
					responsesoftwareModule = hawkbitFeignClient.createSoftwaremodules(softwareModuleList);
					// Create UNINSTALLED_ALL MODULE
				} catch (Exception e) {
					throw new BadRequestException(
							"Hawkbit connection error. Check your Hawkbit's IP in the propreties file!");
				}
				if (responsesoftwareModule.size() == 0) {
					throw new BadRequestException(
							Utils.UNINSTALLED_ALL + " Dummy App could not be saved to Hawkbit and Appstore!");
				} else {
					softwareModules.add(responsesoftwareModule.get(0));
				}

			} else {
				softwareModules.add(softwareModuleResult.getContent().get(0));
			}
			// Check UNINSTALLED_ALL MODULE
		}

		Distribution newDistribution = new Distribution(targetDeviceName, Integer.toString(newVersion));
		newDistribution.setName(targetDeviceName);
		newDistribution.setDescription("This distribution is created by Appstore for" + targetDeviceName);
		newDistribution.setType("app");
		newDistribution.setModules(softwareModules);
		Response responseCreateDistribution = hawkbitFeignClient.createDistributionSets(Arrays.asList(newDistribution));
		if (responseCreateDistribution.status() != HttpStatus.CREATED.value()) {
			throw new BadRequestException("Fail Creating Distribution");

		}
		DistributionResult newDistributionResult = hawkbitFeignClient
				.getDistributionByName(Utils.createFIQLEqual("name", targetDeviceName), 1, "id:DESC");
		if (newDistributionResult.getSize() > 0) {

			ruleNew.setId(newDistributionResult.getContent().get(0).getId());
		} else {

			throw new BadRequestException("This app not found on Hawkbit!");
		}

		AssignedResult response = hawkbitFeignClient.sendApptoDevice(targetDeviceName, ruleNew);
		if (response.getAssigned() > 0) {

			for (Long appId : appIds) {

				App currentApp = appRepository.findById(appId);

				List<User> list = currentApp.getInstalledusers();
				if (Utils.isUserAlreadyOwner(currentUser, list)) {

					currentApp.setInstalledusers(Utils.removeOwnerUser(currentUser, list));
					updateApp(currentApp.getId().toString(), currentApp);
				}
			}
			return Result.success(HttpStatus.OK);
		} else if (response.getAlreadyAssigned() > 0) {
			throw new BadRequestException("The updating action is already assigned for selected device.");
		} else {
			throw new BadRequestException("The updating action hasnt been sent to Hawkbit for selected device.");
		}
	}
	
	public String downloadPermissionArtifactFile(Long appId) throws BadRequestException, NotFoundException {
		App currentApp = appRepository.findById(appId);
		SoftwareModuleResult currentSoftwareModuleResult = hawkbitFeignClient
				.getSoftwaremoduleByName(Utils.createFIQLEqual("name", currentApp.getName()) + ";"
						+ Utils.createFIQLEqual("version", currentApp.getVersion()));

		List<Artifact> listArtifact = hawkbitFeignClient
				.getArtifactsBysoftwareModuleId(currentSoftwareModuleResult.getContent().get(0).getId().toString());

		String artifactId = null;

		for (Artifact artifact : listArtifact) {
			if (artifact.getProvidedFilename().toUpperCase().equals(Utils.PERMISSION)) {
				artifactId = artifact.getId();
				break;
			}
		}
		if (artifactId == null) {
			return Utils.NOT_FOUND;
		}
		String responseDownloadArtifactFile = hawkbitFeignClient
				.downloadArtifactFile(currentSoftwareModuleResult.getContent().get(0).getId().toString(), artifactId);
		return responseDownloadArtifactFile;
	}

	public App findById(Long id) {
		return appRepository.findById(id);
	}

	public App findByName(String name) {
		return appRepository.findByName(name);
	}

	public List<App> findAll() {
		return appRepository.findAll();
	}

	public List<App> findByNameStartsWithIgnoreCase(String name) {
		return appRepository.findByNameStartsWithIgnoreCase(name);
	}
	
	public App incrementAppDownloadCount(App app) {
		app.setDownloadcount(app.getDownloadcount() + 1);
		return app;
	}
	
	private String getSoftwareModuleName(Long id) throws BadRequestException, NotFoundException {
		App app = appRepository.findById(id);
		if (app != null) {
			try {
				SoftwareModuleResult softwareModuleResult = hawkbitFeignClient
						.getSoftwaremoduleByName(Utils.createFIQLEqual("name", app.getName()) + ";"
								+ Utils.createFIQLEqual("version", app.getVersion()));

				Integer softwareModuleId = Utils.getExistsSoftwareModule(softwareModuleResult.getContent());
				return softwareModuleId.toString();
			} catch (Exception e) {
				throw new NotFoundException(
						"Hawkbit connection error. Check your Hawkbit's IP in the propreties file!");
			}
		} else {
			throw new NotFoundException("App not found. appId: " + id);
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

	public List<String> getListOfTargets(Long userId) throws BadRequestException {
		List<String> listOfTargets = new ArrayList<>();

		String dis = userRepository.findById(userId).getUsername();
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

	public DistributionResult getDistributionOfTarget(String targetDeviceName) throws BadRequestException {
		DistributionResult lastDistributionResult = hawkbitFeignClient
				.getDistributionByName(Utils.createFIQLEqual("name", targetDeviceName), 1, "id:DESC");

		return lastDistributionResult;
	}
	
	public Page<App> findAll(Pageable pageable) {
		return appRepository.findAll(pageable);
	}

	public Page<App> findByNameStartsWithIgnoreCase(String name, Pageable pageable) {
		return appRepository.findByNameStartsWithIgnoreCase(name, pageable);
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
		return appRepository.findUsersApps(createUsersAppList(userId, oemList), pageable);
	}
	
	public List<Long> createUsersAppList(String userId, List<String> oemList) {
		List<Oem> listOem = new ArrayList<>();
		List<User> listUser = new ArrayList<>();
		if (oemList.size() > 0) {

			listOem = oemRepository.findIdByNameIn(oemList);

			List<Long> listOemId = new ArrayList<>();
			for (int i = 0; i < listOem.size(); i++) {
				listOemId.add(listOem.get(i).getId());
			}
			if (listOemId.size() > 0) {
				listUser = userRepository.findByOemIdIn(listOemId);
			}

		}
		List<App> listUsersApp = new ArrayList<>();
		List<Long> listUsersAppId = new ArrayList<>();
		for (int i = 0; i < listUser.size(); i++) {

			listUsersApp.addAll(listUser.get(i).getUserapps());
		}
		for (int i = 0; i < listUsersApp.size(); i++) {

			listUsersAppId.add(listUsersApp.get(i).getId());
		}
		List<Long> listUsersAppIdFromRelationQuery = new ArrayList<>();
		listUsersAppIdFromRelationQuery = appRepository.findUsersAppsIdFromUsersRelationship(userId);
		listUsersAppId.addAll(listUsersAppIdFromRelationQuery);
		return listUsersAppId;

	}
}