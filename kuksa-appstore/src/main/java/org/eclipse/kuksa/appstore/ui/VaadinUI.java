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
package org.eclipse.kuksa.appstore.ui;

import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.model.UserType;
import org.eclipse.kuksa.appstore.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Theme;
import com.vaadin.server.Page;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.Page.UriFragmentChangedListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.SpringViewDisplay;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI
@SpringViewDisplay
@Theme("mytheme")
public class VaadinUI extends UI {

	@Autowired
	private final UserRepository userRepository;

	@Autowired
	public VaadinUI(UserRepository userRepository) {

		this.userRepository = userRepository;

	}

	@Override
	protected void init(VaadinRequest request) {
		// build layout

		VerticalLayout mainLayout = new VerticalLayout();

		LoginView loginView = new LoginView();

		mainLayout.addComponent(loginView);

		mainLayout.setComponentAlignment(loginView, Alignment.MIDDLE_CENTER);
		mainLayout.setSizeFull();
		setContent(mainLayout);

		Page.getCurrent().addUriFragmentChangedListener(new UriFragmentChangedListener() {

			@Override
			public void uriFragmentChanged(UriFragmentChangedEvent event) {
				router(event.getUriFragment());
			}
		});

		router("");
	}

	private void router(String route) {
		if (getSession().getAttribute("user") != null) {
			User loggedUser = userRepository.findByUsername(getSession().getAttribute("user").toString());

			getNavigator().addView(AppEditView.VIEW_NAME, AppEditView.class);
			getNavigator().addView(AppsListView.VIEW_NAME, AppsListView.class);
			getNavigator().addView(AppView.VIEW_NAME, AppView.class);
			getNavigator().addView(InstalledAppsListView.VIEW_NAME, InstalledAppsListView.class);
			getNavigator().addView(UserEditView.VIEW_NAME, UserEditView.class);
			getNavigator().addView(SignUpView.VIEW_NAME, SignUpView.class);
			getNavigator().addView(ProfileEditView.VIEW_NAME, ProfileEditView.class);
			getNavigator().addView(PurchasedAppsListView.VIEW_NAME, PurchasedAppsListView.class);
			if (route.equals("!appedit")) {
				if (loggedUser.getUserType() == UserType.SystemAdmin) {

					Page.getCurrent().setUriFragment("!" + AppEditView.VIEW_NAME);
				} else {

					Page.getCurrent().setUriFragment("!" + AppsListView.VIEW_NAME);
				}

			} else if (route.equals("!applist")) {
				Page.getCurrent().setUriFragment("!" + AppsListView.VIEW_NAME);
			} else if (route.equals("!app")) {
				Page.getCurrent().setUriFragment("!" + AppView.VIEW_NAME);
			} else if (route.equals("!installedapps")) {
				Page.getCurrent().setUriFragment("!" + InstalledAppsListView.VIEW_NAME);
			} else if (route.equals("!purchasedapps")) {
				Page.getCurrent().setUriFragment("!" + PurchasedAppsListView.VIEW_NAME);
			} else if (route.equals("!profile")) {
				Page.getCurrent().setUriFragment("!" + ProfileEditView.VIEW_NAME);
			} else if (route.equals("!useredit")) {

				if (loggedUser.getUserType() == UserType.SystemAdmin) {

					Page.getCurrent().setUriFragment("!" + UserEditView.VIEW_NAME);
				} else {

					Page.getCurrent().setUriFragment("!" + AppsListView.VIEW_NAME);
				}

			} else {

				String gotopage;

				gotopage = Page.getCurrent().getUriFragment();

				if (gotopage == null) {
					gotopage = "!applist";

				}
				Page.getCurrent().setUriFragment(gotopage);

			}

		} else {
			if (route.equals("!signup")) {
				Page.getCurrent().setUriFragment("!" + SignUpView.VIEW_NAME);
			} else {
				Page.getCurrent().setUriFragment("!" + LoginView.VIEW_NAME);
			}
		}
	}

}
