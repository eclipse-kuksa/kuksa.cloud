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

import javax.transaction.Transactional;

import org.eclipse.kuksa.appstore.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

	User findByUsernameAndPassword(String username, String password);

	List<User> findByUsernameStartsWithIgnoreCase(String name);

	User findByUsername(String username);

	Page<User> findAll(Pageable pageable);

	User findById(Long id);

	List<User> findAll();

	List<User> findByIdNotIn(List<Long> notInList);

	@Modifying
	@Transactional
	@Query(nativeQuery = true, value = "delete from MEMBERS where user=?1")
	void deleteAllMembers(String userId);

	List<User> findByOemIdIn(List<Long> oemIdList);

}
