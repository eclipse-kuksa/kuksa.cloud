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

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.Oem;
import org.eclipse.kuksa.appstore.model.Result;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.model.UserType;
import org.eclipse.kuksa.appstore.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vaadin.spring.annotation.SpringComponent;

@SpringComponent
@Service
public class UserService {

	@Autowired
	UserRepository userRepository;

	public Result<?> createUser(String username, String password, UserType usertype, Oem oem, Set<User> members)
			throws AlreadyExistException, BadRequestException {

		User newUser = new User(null, username, password, usertype, oem, members);
		if (username == null || password == null || username.equals("") || password.equals("") || username.contains(" ")
				|| password.contains(" ")) {

			throw new BadRequestException("Username and password are mandatory field!");

		} else if (userRepository.findByUsername(username) != null) {
			throw new AlreadyExistException("User name already exist. username: " + username);
		} /*
			 * else if (usertype == UserType.GroupAdmin && oem == null) { throw new
			 * BadRequestException("Oem should be given. "); }
			 */ else {
			userRepository.save(newUser);
		}
		return Result.success(HttpStatus.CREATED, newUser);

	}

	public void updateUser(User userObject) {

		userRepository.save(userObject);

	}

	public Result<?> updateUser(String userId, User userObject)
			throws NotFoundException, BadRequestException, AlreadyExistException {
		User currentUser = userRepository.findById(Long.parseLong(userId));
		if (currentUser == null) {
			throw new NotFoundException("User not found. userId: " + userId);
		} else if (userObject.getUsername() == null || userObject.getUsername().equals("")) {

			throw new BadRequestException("Username is mandatory field!");

		} else if (userObject.getPassword() == null || userObject.getPassword().equals("")) {

			throw new BadRequestException("Password is mandatory field!");

		} else if (userObject.getUsername().contains(" ") || userObject.getPassword().contains(" ")) {

			throw new BadRequestException("Username or password should not contains space character!");

		} /*
			 * else if (userObject.getUserType() == UserType.GroupAdmin &&
			 * userObject.getOem() == null) { throw new
			 * BadRequestException("Oem should be given. "); }
			 */ else if (!currentUser.getUsername().equals(userObject.getUsername())) {
			if (userRepository.findByUsername(userObject.getUsername()) != null) {
				throw new AlreadyExistException(
						"New User name already exist. New username: " + userObject.getUsername());
			}
		}
		userRepository.save(userObject);
		return Result.success(HttpStatus.OK, userObject);

	}

	public void deleteAllMembers(String userId) {

		userRepository.deleteAllMembers(userId);

	}

	public void deleteUser(User userObject) {

		userRepository.delete(userObject);

	}

	public void deleteUser(String userId) throws NotFoundException {
		User currentUser = userRepository.findById(Long.parseLong(userId));
		if (currentUser == null) {
			throw new NotFoundException("User not found. userId: " + userId);
		} else {
			userRepository.delete(currentUser);
		}
	}

	public User findByUserNameAndPassword(String username, String password) {

		return userRepository.findByUsernameAndPassword(username, password);

	}

	public User findById(String id) {

		return userRepository.findById(Long.parseLong(id));

	}

	public User findByUserName(String userName) {

		return userRepository.findByUsername(userName);

	}

	public List<User> findAll() {

		return userRepository.findAll();

	}

	public Page<User> findAll(Pageable pageable) {

		return userRepository.findAll(pageable);

	}

	public List<User> findByUserNameStartsWithIgnoreCase(String username) {

		return userRepository.findByUsernameStartsWithIgnoreCase(username);

	}

	public List<User> findByIdNotIn(List<Long> notInList) {

		return userRepository.findByIdNotIn(notInList);

	}

	public boolean isUsersAppOwner(String userId, String appId, List<String> oemList) {

		int count = userRepository.isUsersAppOwner(userId, appId, oemList);
		if (count > 0) {
			return true;
		}
		return false;
	}
}
