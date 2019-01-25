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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.model.UserType;
import org.eclipse.kuksa.appstore.service.OemService;
import org.eclipse.kuksa.appstore.service.UserService;

import com.vaadin.data.Binder;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ItemCaptionGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.GridRowDragger;
import com.vaadin.ui.themes.ValoTheme;

@SpringComponent
@UIScope
public class UserEditor extends VerticalLayout implements View {
	private UserService userService;
	private OemService oemService;
	public User user;
	public TextField username = new TextField("Username");
	public TextField password = new TextField("Password");
	public ComboBox<String> comboBoxUserType = new ComboBox<>("User Type");
	public ComboBox<String> comboBoxOem = new ComboBox<>("Select an Oem");
	/* Action buttons */
	public Button save = new Button("Save", FontAwesome.SAVE);
	public Button delete = new Button("Delete", FontAwesome.TRASH_O);
	VerticalLayout mainLayout = new VerticalLayout();
	HorizontalLayout vlayout = new HorizontalLayout();
	Binder<User> binder = new Binder<>(User.class);
	Grid<User> left = new Grid<>(User.class);
	Grid<User> right = new Grid<>(User.class);

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setOemService(OemService oemService) {
		this.oemService = oemService;
	}

	public UserEditor() {
		comboBoxOem.setEmptySelectionAllowed(true);
		comboBoxOem.setTextInputAllowed(true);
		comboBoxUserType.setEmptySelectionAllowed(false);
		comboBoxUserType.setTextInputAllowed(false);
		// upload
		vlayout.addComponents(username, password, comboBoxUserType, comboBoxOem);
		mainLayout.addComponent(vlayout);

		vlayout = new HorizontalLayout();
		vlayout.addComponents(left, right);

		mainLayout.addComponent(vlayout);

		vlayout = new HorizontalLayout();
		vlayout.addComponents(save, delete);
		mainLayout.addComponent(vlayout);

		addComponents(mainLayout);

		binder.forField(comboBoxUserType).bind(User -> {
			if (User.getUserType() != null) {
				// if there is any row and it is not empty
				return User.getUserType().toString();
			}
			return null; // if address is null, return empty string
		}, (User, usertype) -> {
			UserType newUserType = UserType.valueOf(usertype);
			User.setUserType(newUserType);
		});
		binder.forField(comboBoxOem).bind(User -> {
			if (User.getOem() != null) {
				// if there is any row and it is not empty
				return User.getOem().getId().toString();
			}
			return null; // if address is null, return empty string
		}, (User, oem) -> {
			if (oem != null) {
				User.setOem(oemService.findById(Long.parseLong(oem)));
			} else {
				User.setOem(null);
			}
		});
		binder.bindInstanceFields(this);

		// Configure and style components
		setSpacing(true);
		save.setStyleName(ValoTheme.BUTTON_PRIMARY);
		save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		delete.setStyleName(ValoTheme.BUTTON_DANGER);

		setVisible(false);

		comboBoxOem.setVisible(false);
		left.setVisible(false);
		right.setVisible(false);
		comboBoxUserType.addValueChangeListener(event -> {

			if (event.getValue().equals(UserType.GroupAdmin.toString())) {

				comboBoxOem.setVisible(true);
				left.setVisible(true);
				right.setVisible(true);

			} else {

				comboBoxOem.setVisible(false);
				left.setVisible(false);
				right.setVisible(false);

			}

		});
		ItemCaptionGenerator<String> icg = new ItemCaptionGenerator<String>() {
			@Override
			public String apply(String item) {
				return oemService.findById(Long.parseLong(item)).getName();
			}
		};
		comboBoxOem.setItemCaptionGenerator(icg);

		// Create a sub-window and set the content

		left.setColumns("id", "username");
		left.setCaption("Users");

		right.setColumns("id", "username");
		right.setCaption("Members");

		left.setWidth(300, Sizeable.Unit.PIXELS);
		right.setWidth(300, Sizeable.Unit.PIXELS);

		// enable row dnd from left to right and handle drops
		GridRowDragger<User> leftToRight = new GridRowDragger<>(left, right);

		// enable row dnd from right to left and handle drops
		GridRowDragger<User> rightToLeft = new GridRowDragger<>(right, left);

		// don't allow drops to left when it is the source
		leftToRight.getGridDragSource()
				.addDragStartListener(event -> rightToLeft.getGridDropTarget().setDropEffect(DropEffect.MOVE));
		leftToRight.getGridDragSource()
				.addDragEndListener(event -> rightToLeft.getGridDropTarget().setDropEffect(DropEffect.MOVE));

		// don't allow drops to right when it is the source
		rightToLeft.getGridDragSource()
				.addDragStartListener(event -> leftToRight.getGridDropTarget().setDropEffect(DropEffect.MOVE));
		rightToLeft.getGridDragSource()
				.addDragEndListener(event -> leftToRight.getGridDropTarget().setDropEffect(DropEffect.MOVE));

	}

	public interface ChangeHandler {

		void onChange();
	}

	public final void editUser(User u) {
		if (u == null) {
			setVisible(false);
			return;
		}
		final boolean persisted = u.getId() != null;
		if (persisted) {
			// Find fresh entity for editing
			user = userService.findById(Long.toString(u.getId()));
		} else {
			user = u;
		}
		delete.setVisible(persisted);
		binder.setBean(user);

		setVisible(true);
		save.focus();
		username.selectAll();

		comboBoxUserType.setItems(UserType.Normal.toString(), UserType.SystemAdmin.toString(),
				UserType.GroupAdmin.toString());

		List<String> oemlist = oemService.getAllId();
		comboBoxOem.setItems(oemlist);

		fillGrids(user);
	}

	public void fillGrids(User user) {

		List<User> rightList = new ArrayList<User>();

		if (user.getMembers() != null) {
			rightList.addAll(user.getMembers());
		}

		List<Long> notInList = new ArrayList<Long>();
		if (user.getId() != null) {
			notInList.add(user.getId());
		}
		for (int i = 0; i < rightList.size(); i++) {
			notInList.add(rightList.get(i).getId());
		}

		if (notInList.size() != 0) {
			left.setItems(userService.findByIdNotIn(notInList));
		} else {
			left.setItems(userService.findAll());
		}
		right.setItems(rightList);
	}

	public void setChangeHandler(ChangeHandler h) {
		save.addClickListener(e -> h.onChange());
		delete.addClickListener(e -> h.onChange());
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

}
