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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppRepository extends CrudRepository<App, String> {

	List<App> findByNameStartsWithIgnoreCase(String name);

	Page<App> findByNameStartsWithIgnoreCase(String name, Pageable pageable);

	List<App> findAll();

	App findById(Long id);

	App findByName(String name);

	App findByNameIgnoreCase(String name);

	Page<App> findAll(Pageable pageable);

	Page<App> findByIdIn(List<Long> id, Pageable pageable);

	Page<App> findByNameStartsWithIgnoreCaseAndInstalledusersUsername(String Name, String username, Pageable pageable);

	Page<App> findByNameStartsWithIgnoreCaseAndInstalledusersId(String appname, Long userid, Pageable pageable);

	Page<App> findByInstalledusersId(Long userid, Pageable pageable);

	Page<App> findByAppcategoryId(Long id, Pageable pageable);

	Page<App> findByNameStartsWithIgnoreCaseAndAppcategoryId(String name, Long id, Pageable pageable);

	@Query(nativeQuery = true, value = "select * from app where id in ( "
			+ " WITH RECURSIVE T(N) AS ( " +
				"select member from members  where member = ?1 " +
					"UNION ALL " +
				"SELECT user FROM members  ,T where member = N " +
				") " +
				"select USERAPPS.appid from T inner join USERAPPS on T.N=USERAPPS.userid " +
					"UNION " +
				"select USERAPPS.appid from  USERAPPS where USERAPPS.userid = ?1 " +
					"UNION " +
				"select USERAPPS.appid from USERAPPS where USERAPPS.userid in ( select id from USER where USER.OEM_ID in (select ID from OEM where name in ?2) ) )")
	Page<App> findUsersApps(String userId, List<String> oemList, Pageable pageable);
}
