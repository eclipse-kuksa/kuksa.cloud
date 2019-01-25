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
package org.eclipse.kuksa.appstore.utils;

import java.io.File;
import java.util.List;

import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.model.hawkbit.SoftwareModule;

public class Utils {

	private static final String IMAGE_FILE_PATH = System.getProperty("user.dir") + File.separator + "imgs"
			+ File.separator + "app";

	public static String getImageFilePath() {
		return IMAGE_FILE_PATH;
	}

	private static final String IMAGE_FOLDER_PATH = System.getProperty("user.dir") + File.separator + "imgs";

	public static String getImageFolderPath() {
		return IMAGE_FOLDER_PATH;
	}

	public static String getFileExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // empty extension
		}
		return name.substring(lastIndexOf);
	}

	public static Integer getExistsSoftwareModule(List<SoftwareModule> listsoftwareModules) {
		for (SoftwareModule softwareModule : listsoftwareModules) {
			if (softwareModule.isDeleted() == false) {
				return softwareModule.getId();
			}
		}
		return null;
	}

	public static String createFIQLEqual(String fieldName, String value) {
		return fieldName + "==" + value;
	}

	public static String createDistributionName(Long appId) {
		return "distribution" + appId;
	}

	public static String createSoftwareName(Long appId) {
		return "software" + appId;
	}

	public static boolean isAppAlreadyInstalled(SoftwareModule softwareModule,
			List<SoftwareModule> softwareModuleList) {

		for (SoftwareModule indexSoftwareModule : softwareModuleList) {
			if (indexSoftwareModule.getId().equals(softwareModule.getId())) {
				return true;
			}
		}
		return false;
	}
	public static boolean isUserAlreadyOwner(User user,
			List<User> ownerList) {

		for (User indexUser : ownerList) {
			if (indexUser.getId().equals(user.getId())) {
				return true;
			}
		}
		return false;
	}
}
