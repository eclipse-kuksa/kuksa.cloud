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

import org.eclipse.kuksa.appstore.model.App;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ImageRenderer;


@SpringView(name = AppEditView.VIEW_NAME)
public class AppEditView extends CustomComponent implements View {

	public static final String VIEW_NAME = "main";

	public static final String TITLE_NAME = "App Edit";
	private final AppEditor editor;

	final Grid<App> grid;

	final TextField searchText;

	private final PopupView popup;
	private final Button addNewBtn;
	@Autowired
	AppService appManagerService;

	@Autowired
	public AppEditView(AppEditor editor) {
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME);

		this.editor = editor;
		this.grid = new Grid<>(App.class);
		this.searchText = new TextField();
		this.addNewBtn = new Button("New App", FontAwesome.PLUS);
		addNewBtn.setStyleName("v-button-primary");

		VerticalLayout popupContent = new VerticalLayout();
		popupContent.addComponent(editor);
		popup = new PopupView(null, popupContent);

		HorizontalLayout actions = new HorizontalLayout(searchText, popup, addNewBtn);

		HorizontalLayout gridLayout = new HorizontalLayout();

		actions.setStyleName("v-actions");

		gridLayout.addComponent(grid);
		gridLayout.setWidth("100%");
		gridLayout.setStyleName("v-mainGrid");
		VerticalLayout mainLayout = new VerticalLayout(new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString()), actions, gridLayout);

		grid.setWidth("100%");

		grid.setColumns("id", "name", "description", "version", "owner");
		grid.getColumn("id").setMaximumWidth(150);
		grid.getColumn("name").setMaximumWidth(200);
		grid.getColumn("description").setMaximumWidth(700);
		grid.getColumn("version").setMaximumWidth(150);
		grid.getColumn("owner").setMaximumWidth(200);
		addEditColumn("Edit");
		
		grid.asSingleSelect().addValueChangeListener(e -> {
			editor.editStudent(e.getValue());

			editor.appimage.setVisible(true);
			try {
				new File(Utils.getImageFolderPath()).mkdirs();
				File new_file = new File(Utils.getImageFilePath() + e.getValue().getId() + ".png");

				if (new_file.exists()) {
					editor.appimage.setSource(new FileResource(new_file));
					editor.upload.setButtonCaption("Change Image");
				} else {
					editor.upload.setButtonCaption("Add Image");
					editor.appimage.setSource(new ThemeResource("img/noimage.png"));
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}

		});

		addNewBtn.addClickListener(e -> {
			popup.setPopupVisible(true);
			editor.editStudent(new App(null, "", "", "", "", "", 0, null));
		});

		editor.save.addClickListener(e -> {
			editor.app.setHawkbitname(editor.app.getName().replace(" ", "_"));
			editor.app.setPublishdate(new Timestamp(new Date().getTime()));
			appManagerService.updateApp(editor.app);
			System.out.println(editor.app.getId());
			listApps(null);
			editor.setVisible(false);
			new Notification("Succes Updating", "The App has been updated.", Notification.Type.TRAY_NOTIFICATION)
					.show(Page.getCurrent());
		});

		editor.delete.addClickListener(e -> {
			popup.setPopupVisible(false);
			appManagerService.deleteApp(editor.app);
			listApps(null);
			editor.setVisible(false);
			new Notification("Succes Deleting", "The App has been deleted.", Notification.Type.TRAY_NOTIFICATION)
					.show(Page.getCurrent());
		});

		editor.cancel.addClickListener(e -> {
			popup.setPopupVisible(false);
			editor.editStudent(editor.app);
			listApps(null);
			editor.setVisible(false);
		});

		searchText.setPlaceholder("Search by App Name");
		searchText.addValueChangeListener(event -> {
			editor.setVisible(false);
			listApps(event.getValue());
		});
		setCompositionRoot(mainLayout);

	}
	private void addEditColumn(String caption) {
        ImageRenderer<App> renderer = new ImageRenderer<>();
        renderer.addClickListener(e -> iconClicked(e.getItem()));

        Grid.Column<App, ThemeResource> iconColumn =
                grid.addColumn(i -> new ThemeResource("img/edit.png"), renderer);
        iconColumn.setCaption(caption);
        iconColumn.setMaximumWidth(70);
        grid.addItemClickListener(e -> {
            if (e.getColumn().equals(iconColumn)) {
                iconClicked(e.getItem());
            }
        });
    }

    private void iconClicked(App app) {
    	
        App item = appManagerService
				.findById(app.getId());
		grid.select(item);
		popup.setPopupVisible(true);
        
    }
	@PostConstruct
	public void init() {
		listApps(null);
	}

	void listApps(String searchText) {
		if (StringUtils.isEmpty(searchText)) {
			grid.setItems(appManagerService.findAll());
		} else {
			grid.setItems(appManagerService.findByNameStartsWithIgnoreCase(searchText));
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

}