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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class App {

	public App(Long id, String name, String hawkbitname, String description, String version, String owner, int downloadcount,
			Date publishdate) {
		super();
		this.id = id;
		this.name = name;
		this.hawkbitname = hawkbitname;
		this.description = description;
		this.version = version;
		this.owner = owner;
		this.downloadcount=downloadcount;
		this.publishdate = publishdate;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "hawkbitname")
	private String hawkbitname;

	@Column(name = "description")
	private String description;

	@Column(name = "version")
	private String version;

	@Column(name = "owner")
	private String owner;

	@Column(name = "downloadcount")
	private int downloadcount;

	@Column(name = "publishdate")
	private Date publishdate;

	
	
	public App() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getHawkbitname() {
		return hawkbitname;
	}

	public void setHawkbitname(String hawkbitname) {

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isWhitespace(c)) {
				c = '_';
			}
			sb.append(c);
		}

		System.out.println("App Name:     " + name);
		System.out.println("Hawkbit Name:     " + sb);
		this.hawkbitname = sb.toString();

	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Date getPublishdate() {
		return publishdate;
	}

	public void setPublishdate(Date publishdate) {
		this.publishdate = publishdate;
	}

	public int getDownloadcount() {
		return downloadcount;
	}

	public void setDownloadcount(int downloadcount) {
		this.downloadcount = downloadcount;
	}

	@Override
	public String toString() {
		return "App [id=" + id + ", name=" + name + ", description=" + description + ", version=" + version + ", owner="
				+ owner + ", publishdate=" + publishdate + "]";
	}

}
