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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.repo.AppRepository;
import org.eclipse.kuksa.appstore.repo.UserRepository;
import org.eclipse.kuksa.appstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.Binder;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringComponent
@UIScope
public class UserEditor extends VerticalLayout implements View {	

	@Autowired
	UserService userManagerService;

	public User user;
	// id,name,description,version,type,publishDate
	/* Fields to edit properties in Student entity */
	public TextField username = new TextField("Username");
	public TextField password = new TextField("Password");
	public CheckBox adminuser = new CheckBox("Admin Access");
	// DateField birthDate = new DateField("Birth date");

	/* Action buttons */
	public Button save = new Button("Save", FontAwesome.SAVE);
	public Button cancel = new Button("Cancel");
	public Button delete = new Button("Delete", FontAwesome.TRASH_O);
	HorizontalLayout hlayout = new HorizontalLayout();
	VerticalLayout vlayout = new VerticalLayout();
	Binder<User> binder = new Binder<>(User.class);

	@Autowired
	public UserEditor() {

		// upload
		// actions.addComponents(upload,appimage);
		vlayout.addComponents(username, password);
		hlayout.addComponent(vlayout);

		vlayout = new VerticalLayout();
		vlayout.addComponents(adminuser);
		hlayout.addComponent(vlayout);

		vlayout = new VerticalLayout();
		vlayout.addComponents(save, cancel, delete);
		hlayout.addComponent(vlayout);

		// hlayout.addComponents();
		addComponents(hlayout);
		// bind using naming convention
		binder.bindInstanceFields(this);

		// Configure and style components
		setSpacing(true);
		// actions.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		save.setStyleName(ValoTheme.BUTTON_PRIMARY);
		save.setClickShortcut(ShortcutAction.KeyCode.ENTER);

		setVisible(false);
	}

	public interface ChangeHandler {

		void onChange();
	}

	public final void editStudent(User s) {
		if (s == null) {
			setVisible(false);
			return;
		}
		final boolean persisted = s.getId() != null;
		if (persisted) {
			// Find fresh entity for editing
			user = userManagerService.findById(Long.toString(s.getId()));
		} else {
			user = new User(null, "", "", false);
		}
		cancel.setVisible(persisted);
		// Bind student properties to similarly named fields
		// Could also use annotation or "manual binding" or programmatically
		// moving values from fields to entities before saving
		binder.setBean(user);

		setVisible(true);

		// A hack to ensure the whole form is visible
		save.focus();
		// Select all text in firstName field automatically
		username.selectAll();
	}

	public void setChangeHandler(ChangeHandler h) {
		// ChangeHandler is notified when either save or delete
		// is clicked
		save.addClickListener(e -> h.onChange());
		delete.addClickListener(e -> h.onChange());
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

}
