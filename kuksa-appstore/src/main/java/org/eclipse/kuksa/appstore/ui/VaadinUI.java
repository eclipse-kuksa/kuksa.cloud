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
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.adapters.springsecurity.account.KeycloakRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.vaadin.spring.security.VaadinSecurity;
import org.vaadin.spring.security.util.SecurityExceptionUtils;

import com.vaadin.annotations.Theme;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringNavigator;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI
@Theme("mytheme")
@SuppressWarnings("serial")
public class VaadinUI extends UI {
	@Autowired
	ApplicationContext applicationContext;
	@Autowired
	VaadinSecurity vaadinSecurity;
	@Autowired
	SpringViewProvider springViewProvider;
	@Autowired
	SpringNavigator navigator;
	@Autowired
	UserRepository userRepository;

	@Override
	protected void init(VaadinRequest request) {
		getPage().setTitle("Kuksa Appstore");

		setErrorHandler(new DefaultErrorHandler() {
			@Override
			public void error(com.vaadin.server.ErrorEvent event) {
				if (SecurityExceptionUtils.isAccessDeniedException(event.getThrowable())) {
					Notification.show("Sorry, you don't have access to do that.");
				} else {
					super.error(event);
				}
			}
		});
		VerticalLayout layout = new VerticalLayout();

		navigator.init(this, layout);

		springViewProvider.setAccessDeniedViewClass(AccessDeniedView.class);
		navigator.addProvider(springViewProvider);
		navigator.setErrorView(ErrorView.class);

		KeycloakPrincipal userPrincipal = (KeycloakPrincipal) vaadinSecurity.getAuthentication().getPrincipal();

		UserType usertype;
		if (vaadinSecurity.getAuthentication().getAuthorities().contains(new KeycloakRole("ROLE_ADMIN"))) {
			usertype = UserType.SystemAdmin;
		} else {
			usertype = UserType.Normal;
		}

		User loggedUser = userRepository.findByUsername(userPrincipal.getName());
		if (loggedUser != null) {
			loggedUser.setUserType(usertype);
		} else {
			User newUser = new User(null, userPrincipal.getName(), usertype, null, null);
			loggedUser = newUser;
		}
		userRepository.save(loggedUser);
		VaadinSession.getCurrent().setAttribute("user", loggedUser.getUsername());
		VaadinSession.getCurrent().setAttribute("isCurrentUserAdmin", loggedUser.getUserType());

		if (loggedUser.getUserType() == UserType.SystemAdmin) {

			navigator.navigateTo(AppEditView.VIEW_NAME);
		} else {

			navigator.navigateTo(AppsListView.VIEW_NAME);
		}

		setContent(layout);

	}

}
