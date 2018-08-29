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

import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.Usersapps;
import org.eclipse.kuksa.appstore.repo.UsersAppsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsersAppsService {

	@Autowired
	UsersAppsRepository installedAppsRepository;

	public List<Usersapps> findByUserid(Long id) {
		return installedAppsRepository.findByUserid(id);
	}

	public List<Long> findAppidByUserid(Long id) {
		return installedAppsRepository.findAppidByUserid(id);
	}
	public void addInstalledApp(Usersapps object) {

		installedAppsRepository.save(object);

	}

}
