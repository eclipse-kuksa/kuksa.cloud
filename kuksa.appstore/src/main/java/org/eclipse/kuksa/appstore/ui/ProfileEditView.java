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
import org.eclipse.kuksa.appstore.model.AppCategory;
import org.eclipse.kuksa.appstore.model.Oem;
import org.eclipse.kuksa.appstore.model.Result;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.model.UserType;
import org.eclipse.kuksa.appstore.service.AppCategoryService;
import org.eclipse.kuksa.appstore.service.OemService;
import org.eclipse.kuksa.appstore.service.UserService;
import org.eclipse.kuksa.appstore.ui.component.NavHeader;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.EditorSaveEvent;
import com.vaadin.ui.renderers.TextRenderer;

@SpringView(name = ProfileEditView.VIEW_NAME)
public class ProfileEditView extends CustomComponent implements View {

	public static final String VIEW_NAME = "profile";
	public static final String TITLE_NAME = "My Profile";
	final Grid<AppCategory> gridAppCategory = new Grid<>();
	final Grid<Oem> gridOem = new Grid<>();
	private PasswordField currentPassword = new PasswordField("Current Password");
	private PasswordField newPassword = new PasswordField("New Password");
	private PasswordField repeatNewPassword = new PasswordField("Repeat New Password");
	private Button save = new Button("Save");
	@Autowired
	UserService userService;
	@Autowired
	AppCategoryService appCategoryService;
	@Autowired
	OemService oemService;
	private final AppCategoryEditor appCategoryEditor;
	private final PopupView popupCategory;
	private final Button addNewCategoryBtn = new Button("", FontAwesome.PLUS);
	private final Button deleteCategoryBtn = new Button("", FontAwesome.TRASH_O);

	private final OemEditor oemEditor;
	private final PopupView popupOem;
	private final Button addNewOemBtn = new Button("", FontAwesome.PLUS);
	private final Button deleteOemBtn = new Button("", FontAwesome.TRASH_O);

	@Autowired
	public ProfileEditView(AppCategoryEditor appCategoryEditor, OemEditor oemEditor) {
		this.appCategoryEditor = appCategoryEditor;
		this.oemEditor = oemEditor;
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME);

		VerticalLayout popupCategoryContent = new VerticalLayout();
		popupCategoryContent.addComponent(appCategoryEditor);
		popupCategory = new PopupView(null, popupCategoryContent);

		VerticalLayout popupOemContent = new VerticalLayout();
		popupOemContent.addComponent(oemEditor);
		popupOem = new PopupView(null, popupOemContent);

		addNewCategoryBtn.addClickListener(e -> {
			popupCategory.setPopupVisible(true);
			appCategoryEditor.editAppCategory(new AppCategory());
		});
		deleteCategoryBtn.addClickListener(e -> {
			try {
				if (gridAppCategory.asSingleSelect().getValue() != null) {
					appCategoryService
							.deleteAppCategory(gridAppCategory.asSingleSelect().getValue().getId().toString());
					listAppCategories();
					new Notification("Succes Deleting", "The App Category has been deleted.",
							Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
				} else {
					new Notification("No selected row", "Make sure that You selected any row to delete",
							Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
				}
			} catch (NotFoundException e1) {
				// TODO Auto-generated catch block
				new Notification("Error", e1.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());

			}
		});

		appCategoryEditor.save.addClickListener(e -> {

			if (appCategoryEditor.appCategory.getName() != null && !appCategoryEditor.appCategory.getName().isEmpty()) {
				if (appCategoryEditor.appCategory.getId() != null) {
					appCategoryService.updateAppCategory(appCategoryEditor.appCategory);
					listAppCategories();
					appCategoryEditor.setVisible(false);
					popupCategory.setPopupVisible(false);
					new Notification("Succes Updating", "The App Category has been updated.",
							Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
				} else {
					try {
						appCategoryService.createAppCategory(appCategoryEditor.appCategory);
						listAppCategories();
						appCategoryEditor.setVisible(false);
						popupCategory.setPopupVisible(false);
						new Notification("Succes Creating", "The App Category has been created.",
								Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
					} catch (AlreadyExistException e1) {
						new Notification("Already Exist Exception", "App Category Name already exists.",
								Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
					} catch (BadRequestException e1) {
						new Notification("Bad Request Exception", e1.getMessage(), Notification.Type.ERROR_MESSAGE)
								.show(Page.getCurrent());
					}
				}

			} else {
				new Notification("Fill the mandatory fields", "App Category Name is mandatory fields.",
						Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
			}
		});

		appCategoryEditor.delete.addClickListener(e -> {
			appCategoryService.deleteAppCategory(appCategoryEditor.appCategory);
			listAppCategories();
			appCategoryEditor.setVisible(false);
			popupCategory.setPopupVisible(false);
			new Notification("Succes Deleting", "The App Category has been deleted.",
					Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
		});

		appCategoryEditor.cancel.addClickListener(e -> {
			listAppCategories();
			appCategoryEditor.setVisible(false);
			popupCategory.setPopupVisible(false);
		});

		// Oem

		addNewOemBtn.addClickListener(e -> {
			popupOem.setPopupVisible(true);
			oemEditor.editOem(new Oem());
		});
		deleteOemBtn.addClickListener(e -> {
			try {
				if (gridOem.asSingleSelect().getValue() != null) {
					oemService.deleteOem(gridOem.asSingleSelect().getValue().getId());
					listOems();
					new Notification("Succes Deleting", "The Oem has been deleted.",
							Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
				} else {
					new Notification("No selected row", "Make sure that You selected any row to delete",
							Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
				}
			} catch (NotFoundException e1) {
				// TODO Auto-generated catch block
				new Notification("Error", e1.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());

			}
		});

		oemEditor.save.addClickListener(e -> {

			if (oemEditor.oem.getName() != null && !oemEditor.oem.getName().isEmpty()) {
				if (oemEditor.oem.getId() != null) {
					oemService.updateOem(oemEditor.oem);
					listOems();
					oemEditor.setVisible(false);
					popupOem.setPopupVisible(false);
					new Notification("Succes Updating", "The Oem has been updated.",
							Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
				} else {
					try {
						oemService.createOem(oemEditor.oem);
						listOems();
						oemEditor.setVisible(false);
						popupOem.setPopupVisible(false);
						new Notification("Succes Creating", "The Oem has been created.",
								Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
					} catch (AlreadyExistException e1) {
						new Notification("Already Exist Exception", "Oem Name already exists.",
								Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
					} catch (BadRequestException e1) {
						new Notification("Bad Request Exception", e1.getMessage(), Notification.Type.ERROR_MESSAGE)
								.show(Page.getCurrent());
					}
				}

			} else {
				new Notification("Fill the mandatory fields", "Oem Name is mandatory fields.",
						Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
			}
		});

		oemEditor.delete.addClickListener(e -> {
			oemService.deleteOem(oemEditor.oem);
			listOems();
			oemEditor.setVisible(false);
			popupOem.setPopupVisible(false);
			new Notification("Succes Deleting", "The Oem has been deleted.", Notification.Type.TRAY_NOTIFICATION)
					.show(Page.getCurrent());
		});

		oemEditor.cancel.addClickListener(e -> {
			listOems();
			oemEditor.setVisible(false);
			popupOem.setPopupVisible(false);
		});

	}

	@PostConstruct
	public void init() {

		popupCategory.setHideOnMouseOut(false);
		popupOem.setHideOnMouseOut(false);
		VerticalLayout mainLayout = new VerticalLayout(new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString()));

		HorizontalLayout subHorizontalLayout = new HorizontalLayout();
		mainLayout.addComponent(subHorizontalLayout);
		setCompositionRoot(mainLayout);

		currentPassword.setPlaceholder("Current Password");
		newPassword.setPlaceholder("New Password");
		repeatNewPassword.setPlaceholder("Repeat New Password");
		Panel panelPassword = new Panel("Change Password");
		panelPassword.setSizeUndefined(); // Shrink to fit content
		// Create the content
		FormLayout content = new FormLayout();
		content.addComponent(currentPassword);
		content.addComponent(newPassword);
		content.addComponent(repeatNewPassword);
		content.addComponent(save);
		content.setSizeUndefined(); // Shrink to fit
		content.setMargin(true);
		panelPassword.setContent(content);
		mainLayout.addComponent(panelPassword);

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
						if (result.isSuccess()) {
							new Notification("Succes Updating", "Your Password has been updated",
									Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
						}
					} catch (NotFoundException | BadRequestException | AlreadyExistException e) {
						// TODO Auto-generated catch block
						new Notification("Error", e.getMessage(), Notification.Type.ERROR_MESSAGE)
								.show(Page.getCurrent());
					}
				}
			}
		});

		///// categoryPanel
		Panel categoryPanel = new Panel("Application Category");
		categoryPanel.setSizeUndefined(); // Shrink to fit content
		// Create the content
		VerticalLayout categoryLayout = new VerticalLayout();
		HorizontalLayout categoryActionsLayout = new HorizontalLayout();

		gridAppCategory.setSizeFull();
		gridAppCategory.setSelectionMode(SelectionMode.SINGLE);

		TextField categoryNameEditor = new TextField();

		gridAppCategory.addColumn(AppCategory::getId, new TextRenderer()).setCaption("Id").setExpandRatio(1);
		gridAppCategory.addColumn(AppCategory::getName).setEditorComponent(categoryNameEditor, AppCategory::setName)
				.setCaption("App Category Name").setEditable(true).setExpandRatio(3);

		gridAppCategory.getEditor().setEnabled(true);
		gridAppCategory.getEditor().addSaveListener(e -> modifyAppCategory(e));

		listAppCategories();

		categoryActionsLayout.addComponents(addNewCategoryBtn, deleteCategoryBtn);
		categoryLayout.addComponent(categoryActionsLayout);
		categoryLayout.addComponent(gridAppCategory);
		categoryPanel.setContent(categoryLayout);
		categoryActionsLayout.addComponent(popupCategory);
		if (VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString()
				.equals(UserType.SystemAdmin.toString())) {
			subHorizontalLayout.addComponent(categoryPanel);
		}
		//// categoryPanel

		///// oemPanel
		Panel oemPanel = new Panel("Oem");
		oemPanel.setSizeUndefined(); // Shrink to fit content
		// Create the content
		VerticalLayout oemLayout = new VerticalLayout();
		HorizontalLayout oemActionsLayout = new HorizontalLayout();

		gridOem.setSizeFull();
		gridOem.setSelectionMode(SelectionMode.SINGLE);

		TextField oemNameEditor = new TextField();

		gridOem.addColumn(Oem::getId, new TextRenderer()).setCaption("Id").setExpandRatio(1);
		gridOem.addColumn(Oem::getName).setEditorComponent(oemNameEditor, Oem::setName).setCaption("Oem Name")
				.setEditable(true).setExpandRatio(3);

		gridOem.getEditor().setEnabled(true);
		gridOem.getEditor().addSaveListener(e -> modifyOem(e));

		listOems();
		oemActionsLayout.addComponents(addNewOemBtn, deleteOemBtn);
		oemLayout.addComponent(oemActionsLayout);
		oemLayout.addComponent(gridOem);
		oemPanel.setContent(oemLayout);
		oemActionsLayout.addComponent(popupOem);
		if (VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString()
				.equals(UserType.SystemAdmin.toString())) {
			subHorizontalLayout.addComponent(oemPanel);
		}
		//// categoryPanel

	}

	private void modifyAppCategory(EditorSaveEvent<AppCategory> e) {

		if (appCategoryService.findByName(e.getBean().getName()) != null
				&& !appCategoryService.findByName(e.getBean().getName()).getId().equals(e.getBean().getId())) {
			Notification.show("AppCategory name already exists.", Notification.Type.ERROR_MESSAGE);
			listAppCategories();
		} else {
			appCategoryService.updateAppCategory(e.getBean());
			Notification.show("Selected row modified successfully...", Notification.Type.HUMANIZED_MESSAGE);
			listAppCategories();
		}
	}

	private void modifyOem(EditorSaveEvent<Oem> e) {

		if (oemService.findByName(e.getBean().getName()) != null
				&& !oemService.findByName(e.getBean().getName()).getId().equals(e.getBean().getId())) {
			Notification.show("Oem name already exists.", Notification.Type.ERROR_MESSAGE);
			listOems();
		} else {
			oemService.updateOem(e.getBean());
			Notification.show("Selected row modified successfully...", Notification.Type.HUMANIZED_MESSAGE);
			listOems();
		}
	}

	void listAppCategories() {

		gridAppCategory.setItems(appCategoryService.findAll());

	}

	void listOems() {

		gridOem.setItems(oemService.findAll());

	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
