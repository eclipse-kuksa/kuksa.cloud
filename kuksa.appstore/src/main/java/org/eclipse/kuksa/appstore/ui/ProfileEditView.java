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

import javax.annotation.PostConstruct;

import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.Result;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.service.UserService;
import org.eclipse.kuksa.appstore.ui.component.NavHeader;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;

@SpringView(name = ProfileEditView.VIEW_NAME)
public class ProfileEditView extends CustomComponent implements View {

	public static final String VIEW_NAME = "profile";
	public static final String TITLE_NAME = "My Profile";
	private PasswordField currentPassword = new PasswordField("Current Password");
	private PasswordField newPassword = new PasswordField("New Password");
	private PasswordField repeatNewPassword = new PasswordField("Repeat New Password");
	private Button save = new Button("Save");
	@Autowired
	UserService userService;

	@Autowired
	public ProfileEditView(UserEditor userEditor) {
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME);
	}

	@PostConstruct
	public void init() {

		VerticalLayout mainLayout = new VerticalLayout(new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString()));
		currentPassword.setPlaceholder("Current Password");
		newPassword.setPlaceholder("New Password");
		repeatNewPassword.setPlaceholder("Repeat New Password");
		Panel panel = new Panel("Change Password");
		panel.setSizeUndefined(); // Shrink to fit content
		mainLayout.addComponent(panel);
		// Create the content
		FormLayout content = new FormLayout();
		content.addStyleName("mypanelcontent");
		content.addComponent(currentPassword);
		content.addComponent(newPassword);
		content.addComponent(repeatNewPassword);
		content.addComponent(save);
		content.setSizeUndefined(); // Shrink to fit
		content.setMargin(true);
		panel.setContent(content);
		setCompositionRoot(mainLayout);

		save.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				User currentUser = userService
						.findByUserName(VaadinSession.getCurrent().getAttribute("user").toString());

				if (!currentPassword.getValue().equals(currentUser.getPassword())) {

					new Notification("Wrong Password!", "Current Password Wrong!", Notification.Type.ERROR_MESSAGE)
							.show(Page.getCurrent());
				} else if (!newPassword.getValue().equals(repeatNewPassword.getValue())) {
					new Notification("Conflict Password!", "Repeat Password is not equal with New Password!",
							Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
				} else if (newPassword.getValue().equals(currentPassword.getValue())) {
					new Notification("Same Password!", "New Password is equal with Old Password!",
							Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
				} else {
					currentUser.setPassword(newPassword.getValue());
					try {
						Result<?> result = userService.updateUser(currentUser.getId().toString(), currentUser);
					 if(result.isSuccess()) {
						 new Notification("Succes Updating", "Your Password has been updated",
									Notification.Type.HUMANIZED_MESSAGE).show(Page.getCurrent());
					 }
					} catch (NotFoundException | BadRequestException | AlreadyExistException e) {
						// TODO Auto-generated catch block
						new Notification("Error", e.getMessage(), Notification.Type.ERROR_MESSAGE)
								.show(Page.getCurrent());
					}
				}
			}
		});
	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
