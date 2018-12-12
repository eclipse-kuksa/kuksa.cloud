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
import org.eclipse.kuksa.appstore.model.Oem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
public interface OemRepository extends CrudRepository<Oem, String> {
	Oem findById(Long id);
	Oem findByName(String name);
	Oem findByNameIgnoreCase(String name);
	List<Oem> findByNameStartsWithIgnoreCase(String name);
	List<Oem> findAll();
	Page<Oem> findAll(Pageable pageable);
	@Query(nativeQuery = true, value = "select o.id from oem o")
	List<BigInteger> getAllId();
}
