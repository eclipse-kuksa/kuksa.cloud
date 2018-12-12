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
package org.eclipse.kuksa.appstore.model;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class User {

	public User(Long id, String username, String password, UserType userType, Oem oem, Set<User> members) {
		this.password = password;
		this.username = username;
		this.userType = userType;
		this.id = id;
		this.oem = oem;
		this.members = members;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@NotNull
	@Column(name = "user_name")
	private String username;

	@NotNull
	@Column(name = "password")
	private String password;

	@Enumerated(EnumType.STRING)
	@NotNull
	@Column(name = "usertype")
	private UserType userType;

	@ManyToMany(mappedBy = "installedusers", fetch = FetchType.EAGER)
	private List<App> installedapps;

	@ManyToMany(mappedBy = "ownerusers", fetch = FetchType.EAGER)
	private List<App> userapps;

	@Nullable
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "oem_id", nullable = true)
	private Oem oem;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinTable(name = "members", joinColumns = @JoinColumn(name = "user", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "member", referencedColumnName = "id"))
	@ElementCollection
	private Set<User> members;

	public User() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	@JsonIgnore
	public List<App> getInstalledapps() {
		return installedapps;
	}

	public void setInstalledapps(List<App> installedapps) {
		this.installedapps = installedapps;
	}

	@JsonIgnore
	public List<App> getUserapps() {
		return installedapps;
	}

	public void setUserapps(List<App> userapps) {
		this.userapps = userapps;
	}
	public Oem getOem() {
		return oem;
	}

	public void setOem(Oem oem) {
		this.oem = oem;
	}	

	public Set<User> getMembers() {
		return members;
	}

	public void setMembers(Set<User> members) {
		this.members = members;
	}

}
