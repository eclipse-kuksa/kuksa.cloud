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

import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.model.UserType;
import org.eclipse.kuksa.appstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
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

@SpringView(name = SignUpView.VIEW_NAME)
public class SignUpView extends CustomComponent implements View {

	public static final String VIEW_NAME = "signup";
	public static final String TITLE_NAME = "SignUp";
	private String imgPath = "img";
	private Image imageLogo;

	private TextField username = new TextField();
	private PasswordField password = new PasswordField();
	private Button signup = new Button("SignUp Me");

	@Autowired
	UserService UserService;

	public SignUpView() {

		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME);

		CustomLayout sample = new CustomLayout("signup-layout");

		imageLogo = new Image();
		imageLogo.setWidth("200");
		imageLogo.setHeight("200");
		imageLogo.setSource(new ThemeResource(imgPath + "/kuksalogo.png"));
		sample.addComponent(imageLogo, "logo");

		username.setWidth(100.0f, Unit.PERCENTAGE);
		sample.addComponent(username, "username");

		password.setWidth(100.0f, Unit.PERCENTAGE);
		sample.addComponent(password, "password");

		signup.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		sample.addComponent(signup, "signbutton");

		signup.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {

				try {

					UserService.createUser(username.getValue(), password.getValue(), UserType.Normal, null, null);

					new Notification("Succes Sign Up", "Welcome to Kuksa Appstore", Notification.Type.TRAY_NOTIFICATION)
							.show(com.vaadin.server.Page.getCurrent());
					Page.getCurrent().setUriFragment("!" + LoginView.VIEW_NAME);

				} catch (AlreadyExistException e) {

					new Notification("The User Name already exists.", "Please Enter Diffirent User Name",
							Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());

				} catch (BadRequestException e) {

					new Notification("Fill the Form", "Please Enter User Name,Password",
							Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());

				}

			}

		});

		setCompositionRoot(sample);
	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
