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

import org.eclipse.kuksa.appstore.model.Usersapps;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;;

@Repository
public interface UsersAppsRepository extends CrudRepository<Usersapps, String> {

	List<Usersapps> findByUserid(Long id);
	@Query("SELECT r.appid FROM Usersapps r where r.userid = :userid")     
	List<Long> findAppidByUserid(@Param("userid") Long id);
}
