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
package org.eclipse.kuksa.appstore.ui.component;

import org.eclipse.kuksa.appstore.model.UserType;
import org.eclipse.kuksa.appstore.ui.AppEditView;
import org.eclipse.kuksa.appstore.ui.AppsListView;
import org.eclipse.kuksa.appstore.ui.InstalledAppsListView;
import org.eclipse.kuksa.appstore.ui.ProfileEditView;
import org.eclipse.kuksa.appstore.ui.PurchasedAppsListView;
import org.eclipse.kuksa.appstore.ui.UserEditView;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;

public class NavHeader {
	private CustomLayout sample;
	private Image image;
	private String imgPath = "img";
	private String styleName = null;

	public HorizontalLayout create(String currentPage, String isCurrentUserAdmin) {
		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setStyleName("v-navHeader");

		// LOGO
		sample = new CustomLayout("navItem-layout");
		image = new Image();
		image.setStyleName("v-logo");
		image.addClickListener(e -> {
			Page.getCurrent().setUriFragment("!" + "appedit");
		});
		headerLayout.addComponent(image);
		// LOGO

		// App Edit
		if (isCurrentUserAdmin.equals(UserType.SystemAdmin.toString())) {
			if (!currentPage.equals(AppEditView.VIEW_NAME)) {
				styleName = "v-test";
			}
			LayoutClickListener listener = new LayoutClickListener() {

				@Override
				public void layoutClick(LayoutClickEvent event) {
					Page.getCurrent().setUriFragment("!" + AppEditView.VIEW_NAME);
				}
			};

			headerLayout.addComponent(createNavItem("appedit.png", styleName, "App Edit", listener));
		}
		// App Edit

		// Store
		styleName = null;
		if (!currentPage.equals(AppsListView.VIEW_NAME)) {
			styleName = "v-test";
		}
		LayoutClickListener listener = new LayoutClickListener() {

			@Override
			public void layoutClick(LayoutClickEvent event) {
				Page.getCurrent().setUriFragment("!" + AppsListView.VIEW_NAME);
			}
		};

		headerLayout.addComponent(createNavItem("showallaps.png", styleName, "Store", listener));
		// Store

		if (isCurrentUserAdmin.equals(UserType.SystemAdmin.toString())
				|| isCurrentUserAdmin.equals(UserType.GroupAdmin.toString())) {
			// User Edit
			styleName = null;
			if (!currentPage.equals(UserEditView.VIEW_NAME)) {
				styleName = "v-test";
			}
			listener = new LayoutClickListener() {

				@Override
				public void layoutClick(LayoutClickEvent event) {
					Page.getCurrent().setUriFragment("!" + UserEditView.VIEW_NAME);
				}
			};
			headerLayout.addComponent(createNavItem("useredit.png", styleName, "User Edit", listener));
			// User Edit
		}

		// Installed Apps
		styleName = null;
		if (!currentPage.equals(InstalledAppsListView.VIEW_NAME)) {
			styleName = "v-test";
		}
		listener = new LayoutClickListener() {

			@Override
			public void layoutClick(LayoutClickEvent event) {
				Page.getCurrent().setUriFragment("!" + InstalledAppsListView.VIEW_NAME);
			}
		};

		headerLayout.addComponent(createNavItem("installedapps.png", styleName, "Installed", listener));
		// Installed Apps

		// Purchased Apps
		styleName = null;
		if (!currentPage.equals(PurchasedAppsListView.VIEW_NAME)) {
			styleName = "v-test";
		}
		listener = new LayoutClickListener() {

			@Override
			public void layoutClick(LayoutClickEvent event) {
				Page.getCurrent().setUriFragment("!" + PurchasedAppsListView.VIEW_NAME);
			}
		};

		headerLayout.addComponent(createNavItem("purchased.png", styleName, "Purchased", listener));
		// Purchased Apps

		// Logout
		styleName = "v-test";
		listener = new LayoutClickListener() {

			@Override
			public void layoutClick(LayoutClickEvent event) {
				VaadinSession.getCurrent().close();
				Page.getCurrent().setLocation("sso/logout");
			}
		};
		headerLayout.addComponent(createNavItem("logout.png", styleName, "Logout", listener));
		// Logout

		// Profile
		styleName = "v-profilePhoto";
		listener = new LayoutClickListener() {

			@Override
			public void layoutClick(LayoutClickEvent event) {
				Page.getCurrent().setUriFragment("!" + ProfileEditView.VIEW_NAME);
			}
		};
		headerLayout.addComponent(createNavItem("user.png", styleName,
				VaadinSession.getCurrent().getAttribute("user").toString(), listener));

		// Profile
		return headerLayout;

	}

	public HorizontalLayout createNavItem(String imageResource, String styleName, String labelText,
			LayoutClickListener listener) {

		HorizontalLayout layout = new HorizontalLayout();
		sample = new CustomLayout("navItem-layout");
		image = new Image();
		image.setWidth("30");
		image.setHeight("30");
		image.setSource(new ThemeResource(imgPath + "/" + imageResource));
		sample.addComponent(image, "navImage");
		sample.addComponent(new Label(labelText), "navName");
		sample.setStyleName(styleName);
		layout.addComponent(sample);
		layout.addLayoutClickListener(listener);
		return layout;
	}

}
