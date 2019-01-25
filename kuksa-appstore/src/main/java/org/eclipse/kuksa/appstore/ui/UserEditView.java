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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.model.UserType;
import org.eclipse.kuksa.appstore.service.OemService;
import org.eclipse.kuksa.appstore.service.UserService;
import org.eclipse.kuksa.appstore.ui.component.NavHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ImageRenderer;

@SpringView(name = UserEditView.VIEW_NAME)
public class UserEditView extends CustomComponent implements View {

	public static final String VIEW_NAME = "userlist";
	public static final String TITLE_NAME = "User List";

	final UserEditor userEditor = new UserEditor();

	final Grid<User> grid;

	final TextField searchText;
	final Window userEditorWindow = new Window("User Editor");
	private final Button addNewBtn;
	@Autowired
	UserService userService;
	@Autowired
	OemService oemService;

	User currentUser;
	HorizontalLayout actions;

	@Autowired
	public UserEditView() {
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME);

		this.grid = new Grid<>(User.class);
		this.searchText = new TextField();
		this.addNewBtn = new Button("New User", FontAwesome.PLUS);
		addNewBtn.setStyleName("v-button-primary");

		VerticalLayout popupContent = new VerticalLayout();
		popupContent.addComponent(userEditor);
		userEditorWindow.setContent(popupContent);
		userEditorWindow.center();
		userEditorWindow.setModal(true);
		userEditorWindow.setResizable(false);

		actions = new HorizontalLayout(searchText, addNewBtn);
		actions.setStyleName("v-actions");

		HorizontalLayout gridLayout = new HorizontalLayout();
		gridLayout.addComponent(grid);
		gridLayout.setWidth("100%");
		gridLayout.setStyleName("v-mainGrid");

		VerticalLayout mainLayout = new VerticalLayout(new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString()), actions, gridLayout);
		grid.setWidth("100%");
		grid.setHeightMode(HeightMode.UNDEFINED);
		grid.setColumns("id", "username", "password", "userType");
		grid.getColumn("username").setCaption("User Name");
		grid.getColumn("password").setCaption("Password");
		grid.getColumn("userType").setCaption("User Type");
		addEditColumn("Edit");

		userEditor.comboBoxUserType.addValueChangeListener(event -> {

			userEditorWindow.center();
		});
		addNewBtn.addClickListener(e -> {
			userEditorWindow.center();
			VaadinUI.getCurrent().addWindow(userEditorWindow);
			userEditor.editUser(new org.eclipse.kuksa.appstore.model.User(null, "", "", UserType.Normal, null, null));
		});

		userEditor.save.addClickListener(e -> {
			if (userEditor.user.getUsername() != null && !userEditor.user.getUsername().isEmpty()
					&& userEditor.user.getPassword() != null && !userEditor.user.getPassword().isEmpty()) {
				if (userEditor.user.getUserType() != UserType.GroupAdmin && userEditor.user.getOem() != null) {

					userEditor.user.setOem(null);
				}
				if (userEditor.user.getUserType() != UserType.GroupAdmin && userEditor.user.getId() != null) {

					userService.deleteAllMembers(userEditor.user.getId().toString());
				}
				List<User> rightList = userEditor.right.getDataCommunicator().fetchItemsWithRange(0,
						userEditor.right.getDataCommunicator().getDataProviderSize());

				Set<User> newmemberList = new HashSet<User>();
				for (int i = 0; i < rightList.size(); i++) {
					newmemberList.add(userService.findById(rightList.get(i).getId().toString()));
				}
				userEditor.user.setMembers(newmemberList);

				if (userEditor.user.getId() != null) {

					try {
						userService.deleteAllMembers(userEditor.user.getId().toString());
						userService.updateUser(userEditor.user.getId().toString(), userEditor.user);
						listUsers(null);
						VaadinUI.getCurrent().removeWindow(userEditorWindow);
						new Notification("Succes Updating", "The User has been updated.",
								Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
					} catch (NotFoundException e1) {
						new Notification("NotFound Exist Exception", "User not found.", Notification.Type.ERROR_MESSAGE)
								.show(Page.getCurrent());
					} catch (BadRequestException e1) {
						new Notification("Bad Request Exception", e1.getMessage(), Notification.Type.ERROR_MESSAGE)
								.show(Page.getCurrent());
					} catch (AlreadyExistException e1) {
						new Notification("Already Exist Exception", "Username already exists.",
								Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
					}

				} else {
					try {
						userService.createUser(userEditor.user.getUsername(), userEditor.user.getPassword(),
								userEditor.user.getUserType(), userEditor.user.getOem(), userEditor.user.getMembers());
						listUsers(null);

						VaadinUI.getCurrent().removeWindow(userEditorWindow);
						new Notification("Succes Creating", "The User has been created.",
								Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
					} catch (AlreadyExistException e1) {
						new Notification("Already Exist Exception", e1.getMessage(), Notification.Type.ERROR_MESSAGE)
								.show(Page.getCurrent());
					} catch (BadRequestException e1) {
						new Notification("Bad Request Exception", e1.getMessage(), Notification.Type.ERROR_MESSAGE)
								.show(Page.getCurrent());
					}
				}
			} else {
				new Notification("Fill the mandatory fields", "Username and Password are mandatory fields.",
						Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
			}
		});

		userEditor.delete.addClickListener(e -> {
			userService.deleteUser(userEditor.user);
			listUsers(null);
			VaadinUI.getCurrent().removeWindow(userEditorWindow);
			new Notification("Succes Deleting", "The User has been deleted.", Notification.Type.TRAY_NOTIFICATION)
					.show(Page.getCurrent());
		});

		// Listen changes made by the filter textbox, refresh data from backend
		searchText.setPlaceholder("Search by Username");
		searchText.addValueChangeListener(event -> {
			listUsers(event.getValue());
		});

		setCompositionRoot(mainLayout);
	}

	private void addEditColumn(String caption) {
		ImageRenderer<User> renderer = new ImageRenderer<>();
		renderer.addClickListener(e -> iconClicked(e.getItem()));

		Grid.Column<User, ThemeResource> iconColumn = grid.addColumn(i -> new ThemeResource("img/edit.png"), renderer);
		iconColumn.setCaption(caption);
		iconColumn.setMaximumWidth(70);
		grid.addItemClickListener(e -> {
			if (e.getColumn().equals(iconColumn)) {
				iconClicked(e.getItem());
			}
		});
	}

	private void iconClicked(User user) {

		User item = userService.findById(user.getId().toString());
		userEditor.editUser(item);
		grid.select(item);
		userEditorWindow.center();
		VaadinUI.getCurrent().addWindow(userEditorWindow);
	}

	@PostConstruct
	public void init() {
		this.currentUser = userService.findByUserName(VaadinSession.getCurrent().getAttribute("user").toString());
		if (!this.currentUser.getUserType().equals(UserType.SystemAdmin)) {
			actions.setVisible(false);
		}
		listUsers(null);
		userEditor.setUserService(userService);
		userEditor.setOemService(oemService);
	}

	public void listUsers(String searchText) {
		if (currentUser.getUserType().equals(UserType.SystemAdmin)) {
			if (StringUtils.isEmpty(searchText)) {
				grid.setItems(userService.findAll());
			} else {
				grid.setItems(userService.findByUserNameStartsWithIgnoreCase(searchText));
			}
			userEditor.comboBoxUserType.setEnabled(true);
		} else {
			grid.setItems(userService.findByUserName(currentUser.getUsername()));
			userEditor.comboBoxUserType.setEnabled(false);
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
