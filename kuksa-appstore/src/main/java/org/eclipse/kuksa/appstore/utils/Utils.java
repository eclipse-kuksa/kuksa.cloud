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

public class Utils {

	private static final String IMAGE_FILE_PATH = System.getProperty("user.dir")+File.separator+"imgs" +File.separator+ "app";

	public static String getImageFilePath() {
		return IMAGE_FILE_PATH;
	}
	
	private static final String IMAGE_FOLDER_PATH = System.getProperty("user.dir")+File.separator+"imgs";

	public static String getImageFolderPath() {
		return IMAGE_FOLDER_PATH;
	}

}
