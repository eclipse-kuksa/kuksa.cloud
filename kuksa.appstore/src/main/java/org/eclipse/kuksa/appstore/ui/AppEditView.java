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
import java.sql.Timestamp;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.service.AppCategoryService;
import org.eclipse.kuksa.appstore.service.AppService;
import org.eclipse.kuksa.appstore.ui.component.NavHeader;
import org.eclipse.kuksa.appstore.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ImageRenderer;

@SpringView(name = AppEditView.VIEW_NAME)
public class AppEditView extends CustomComponent implements View {

	public static final String VIEW_NAME = "appedit";
	public static final String TITLE_NAME = "App Edit";
	final AppEditor appEditor = new AppEditor();

	final Grid<App> grid;

	final TextField searchText;

	final Window appEditorWindow = new Window("App Editor");
	private final Button addNewBtn;
	@Autowired
	AppService appService;
	@Autowired
	AppCategoryService appCategoryService;

	@Autowired
	public AppEditView() {
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME);

		this.grid = new Grid<>(App.class);
		this.searchText = new TextField();
		this.addNewBtn = new Button("New App", FontAwesome.PLUS);
		addNewBtn.setStyleName("v-button-primary");

		VerticalLayout popupContent = new VerticalLayout();
		popupContent.addComponent(appEditor);
		appEditorWindow.setContent(popupContent);
		appEditorWindow.center();
		appEditorWindow.setModal(true);
		appEditorWindow.setResizable(false);
		HorizontalLayout actions = new HorizontalLayout(searchText, addNewBtn);
		actions.setStyleName("v-actions");

		HorizontalLayout gridLayout = new HorizontalLayout();
		gridLayout.addComponent(grid);
		gridLayout.setWidth("100%");
		gridLayout.setStyleName("v-mainGrid");

		VerticalLayout mainLayout = new VerticalLayout(new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString()), actions, gridLayout);

		grid.setWidth("100%");
		grid.setColumns("id", "name", "version", "owner");
		grid.getColumn("id").setMaximumWidth(150);
		grid.getColumn("name").setMaximumWidth(350);
		grid.getColumn("version").setMaximumWidth(300);
		grid.getColumn("owner").setMaximumWidth(300);

		// Add generated full name column
		Column<App, String> categoryColumn = grid.addColumn(app -> app.getAppcategory().getName()).setMaximumWidth(300);
		categoryColumn.setCaption("Category");
		addEditColumn("Edit");
		grid.asSingleSelect().addValueChangeListener(e -> {
			appEditor.editApp(e.getValue());

			appEditor.appimage.setVisible(true);
			try {
				new File(Utils.getImageFolderPath()).mkdirs();
				File new_file = new File(Utils.getImageFilePath() + e.getValue().getId() + ".png");

				if (new_file.exists()) {
					appEditor.appimage.setSource(new FileResource(new_file));
					appEditor.upload.setButtonCaption("Change Image");
				} else {
					appEditor.upload.setButtonCaption("Add Image");
					appEditor.appimage.setSource(new ThemeResource("img/noimage.png"));
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}

		});

		addNewBtn.addClickListener(e -> {
			appEditorWindow.center();
			VaadinUI.getCurrent().addWindow(appEditorWindow);
			appEditor.editApp(new App(null, "", "", "", "", "", 0, null));
		});

		appEditor.save.addClickListener(e -> {
			if (appEditor.app.getAppcategory() != null && !appEditor.app.getName().isEmpty()
					&& appEditor.app.getName() != null) {

				if (appEditor.app.getId() != null) {

					appEditor.app.setHawkbitname(appEditor.app.getName().replace(" ", "_"));
					appEditor.app.setPublishdate(new Timestamp(new Date().getTime()));
					try {
						appService.updateApp(appEditor.app.getId().toString(), appEditor.app);
						listApps(null);
						VaadinUI.getCurrent().removeWindow(appEditorWindow);
						new Notification("Succes Updating", "The App has been updated.",
								Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
					} catch (NotFoundException e1) {
						new Notification("Not Found Exception", e1.getMessage(), Notification.Type.ERROR_MESSAGE)
								.show(Page.getCurrent());
					} catch (BadRequestException e1) {
						new Notification("Bad Request Exception", e1.getMessage(), Notification.Type.ERROR_MESSAGE)
								.show(Page.getCurrent());
					} catch (AlreadyExistException e1) {
						new Notification("Already Exist Exception", "App Name already exists.",
								Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
					}

				} else {
					appEditor.app.setHawkbitname(appEditor.app.getName().replace(" ", "_"));
					appEditor.app.setPublishdate(new Timestamp(new Date().getTime()));
					try {
						appService.createApp(appEditor.app);
						listApps(null);
						VaadinUI.getCurrent().removeWindow(appEditorWindow);
						new Notification("Succes Updating", "The App has been updated.",
								Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
					} catch (AlreadyExistException e1) {
						new Notification("Already Exist Exception", "App Name already exists.",
								Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
					} catch (BadRequestException e1) {
						new Notification("Bad Request Exception", e1.getMessage(), Notification.Type.ERROR_MESSAGE)
								.show(Page.getCurrent());
					}

				}
			} else {
				new Notification("Fill the mandatory fields", "App Category and Name are mandatory fields.",
						Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
			}
		});

		appEditor.delete.addClickListener(e -> {
			appService.deleteApp(appEditor.app);
			listApps(null);
			VaadinUI.getCurrent().removeWindow(appEditorWindow);
			new Notification("Succes Deleting", "The App has been deleted.", Notification.Type.TRAY_NOTIFICATION)
					.show(Page.getCurrent());
		});

		appEditor.cancel.addClickListener(e -> {
			appEditor.editApp(appEditor.app);
			listApps(null);
			VaadinUI.getCurrent().removeWindow(appEditorWindow);
		});

		searchText.setPlaceholder("Search by App Name");
		searchText.addValueChangeListener(event -> {
			VaadinUI.getCurrent().removeWindow(appEditorWindow);
			listApps(event.getValue());
		});
		setCompositionRoot(mainLayout);

	}

	private void addEditColumn(String caption) {
		ImageRenderer<App> renderer = new ImageRenderer<>();
		renderer.addClickListener(e -> iconClicked(e.getItem()));

		Grid.Column<App, ThemeResource> iconColumn = grid.addColumn(i -> new ThemeResource("img/edit.png"), renderer);
		iconColumn.setCaption(caption);
		iconColumn.setMaximumWidth(70);
		grid.addItemClickListener(e -> {
			if (e.getColumn().equals(iconColumn)) {
				iconClicked(e.getItem());
			}
		});
	}

	private void iconClicked(App app) {

		App item = appService.findById(app.getId());
		grid.select(item);
		appEditorWindow.center();
		VaadinUI.getCurrent().addWindow(appEditorWindow);

	}

	@PostConstruct
	public void init() {
		listApps(null);
		appEditor.setAppCategoryService(appCategoryService);
		appEditor.setAppService(appService);
	}

	void listApps(String searchText) {
		if (StringUtils.isEmpty(searchText)) {
			grid.setItems(appService.findAll());
		} else {
			grid.setItems(appService.findByNameStartsWithIgnoreCase(searchText));
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
