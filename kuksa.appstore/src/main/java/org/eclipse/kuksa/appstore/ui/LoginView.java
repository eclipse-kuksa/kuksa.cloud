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
import org.eclipse.kuksa.appstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;

@SpringView(name = LoginView.VIEW_NAME)
public class LoginView extends CustomComponent implements View {

	public static final String VIEW_NAME = "login";
	public static final String TITLE_NAME = "Login";
	private String imgPath = "img";
	private Image imageLogo;

	// private Navigator navigator;
	private TextField username = new TextField();
	private PasswordField password = new PasswordField();
	private Button login = new Button("Login");

	@Autowired
	UserService userService;

	@Autowired
	public LoginView() {
		
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME);

		CustomLayout sample = new CustomLayout("login-layout");

		imageLogo = new Image();
		imageLogo.setWidth("200");
		imageLogo.setHeight("200");
		imageLogo.setSource(new ThemeResource(imgPath + "/kuksalogo.png"));
		sample.addComponent(imageLogo, "logo");

		username.setWidth(100.0f, Unit.PERCENTAGE);
		sample.addComponent(username, "username");

		password.setWidth(100.0f, Unit.PERCENTAGE);
		sample.addComponent(password, "password");

		login.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		sample.addComponent(login, "okbutton");

		login.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				
				
				User loggedUser = userService.findByUserNameAndPassword(username.getValue(), password.getValue());
				
				if (loggedUser != null) {
					
					VaadinSession.getCurrent().setAttribute("user", loggedUser.getUserName());
					VaadinSession.getCurrent().setAttribute("isCurrentUserAdmin", loggedUser.getAdminuser());
					
					if (loggedUser.getAdminuser() == true) {
						
						Page.getCurrent().setUriFragment("!" + AppEditView.VIEW_NAME);
						
					} else {
						
						Page.getCurrent().setUriFragment("!" + AppsListView.VIEW_NAME);
						
					}

				} else {

					new Notification("Login Failed", "Invalid username or password!", Notification.Type.ERROR_MESSAGE)
							.show(Page.getCurrent());
				}

			}
		});

		setCompositionRoot(sample);
	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
