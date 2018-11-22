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

import java.math.BigInteger;
import java.util.List;

import org.eclipse.kuksa.appstore.model.AppCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppCategoryRepository extends CrudRepository<AppCategory, String> {

	AppCategory findById(Long id);

	AppCategory findByName(String name);

	@Query(nativeQuery = true, value = "select u.id from app_category u")
	List<BigInteger> getAllId();

	List<AppCategory> findByNameStartsWithIgnoreCase(String name);

	List<AppCategory> findAll();

	Page<AppCategory> findAll(Pageable pageable);

}
