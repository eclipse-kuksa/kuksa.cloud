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
package org.eclipse.kuksa.appstore.repo;

import java.util.List;

import org.eclipse.kuksa.appstore.model.App;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppRepository extends CrudRepository<App, String> {

	List<App> findByNameStartsWithIgnoreCase(String name);
	
	Page<App> findByNameStartsWithIgnoreCase(String name,Pageable pageable);

	List<App> findAll();

	App findById(Long id);
	
	App findByName(String name);	

	Page<App> findAll(Pageable pageable);

	Page<App> findByIdIn(List<Long> id,Pageable pageable);
	
	Page<App> findByIdInAndNameStartsWithIgnoreCase(List<Long> id,String name,Pageable pageable);
}
