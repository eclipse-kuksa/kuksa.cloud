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
import java.util.List;

import org.eclipse.kuksa.appstore.client.HawkbitFeignClient;
import org.eclipse.kuksa.appstore.client.HawkbitMultiPartFileFeignClient;
import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.hawkbit.SoftwareModuleResult;
import org.eclipse.kuksa.appstore.service.AppCategoryService;
import org.eclipse.kuksa.appstore.service.AppService;
import org.eclipse.kuksa.appstore.utils.Utils;

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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ItemCaptionGenerator;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SpringComponent
@UIScope
public class AppEditor extends VerticalLayout implements View {

	AppService appService;
	AppCategoryService appCategoryService;
	public App app;
	public TextField name = new TextField("App Name");
	public TextArea description = new TextArea("Description");
	public TextField version = new TextField("Version");
	public TextField owner = new TextField("Owner");
	public Button save = new Button("Save", FontAwesome.SAVE);
	public Button delete = new Button("Delete", FontAwesome.TRASH_O);
	public Button artifactDetailsBtn = new Button("Artifacts Details", FontAwesome.FILE);
	public ComboBox<String> categoryComboBox = new ComboBox<>("Select A Category");
	public Upload upload;
	public Embedded appimage;
	HorizontalLayout subHlayout = new HorizontalLayout();
	VerticalLayout mainVLayout = new VerticalLayout();
	Binder<App> binder = new Binder<>(App.class);
	HawkbitFeignClient hawkbitFeignClient;
	HawkbitMultiPartFileFeignClient hawkbitMultiPartFileFeignClient;
	final Window artifactFileEditorWindow = new Window("Artifact File");
	final ArtifactFileEditor artifactFileEditor = new ArtifactFileEditor();
	SoftwareModuleResult softwareModuleResult;

	public void setAppService(AppService appService) {
		this.appService = appService;
	}

	public void setAppCategoryService(AppCategoryService appCategoryService) {
		this.appCategoryService = appCategoryService;
	}

	public void setMessageFeignClient(HawkbitFeignClient hawkbitFeignClient) {
		this.hawkbitFeignClient = hawkbitFeignClient;
	}

	public void setHawkbitFeignClient(HawkbitMultiPartFileFeignClient hawkbitMultiPartFileFeignClient) {
		this.hawkbitMultiPartFileFeignClient = hawkbitMultiPartFileFeignClient;
	}

	public AppEditor() {

		name.setWidth("220px");
		name.setPlaceholder("App Name");
		description.setWidth("450px");
		description.setHeight("180px");
		description.setPlaceholder("Description");
		version.setWidth("220px");
		version.setPlaceholder("Version");
		owner.setWidth("220px");
		owner.setPlaceholder("Owner");
		categoryComboBox.setWidth("220px");
		categoryComboBox.setEmptySelectionAllowed(false);
		appimage = new Embedded("App Image");
		appimage.setVisible(false);
		appimage.setWidth("75");
		appimage.setHeight("50");

		VerticalLayout popupContent = new VerticalLayout();
		popupContent.addComponent(artifactFileEditor);
		artifactFileEditorWindow.setContent(popupContent);
		artifactFileEditorWindow.center();
		artifactFileEditorWindow.setModal(true);
		artifactFileEditorWindow.setResizable(false);

		class ImageUploader implements Receiver, SucceededListener {
			public File file;

			public OutputStream receiveUpload(String filename, String mimeType) {

				FileOutputStream fos = null;
				try {

					file = new File(Utils.getImageFilePath() + app.getId() + ".png");

					fos = new FileOutputStream(file);

				} catch (final java.io.FileNotFoundException e) {
					new Notification("Could not open file<br/>", e.getMessage(), Notification.Type.ERROR_MESSAGE)
							.show(Page.getCurrent());
					return null;
				}
				return fos;
			}

			public void uploadSucceeded(SucceededEvent event) {
				appimage.setVisible(true);
				appimage.setSource(new FileResource(file));

			}
		}
		;
		ImageUploader receiver = new ImageUploader();

		upload = new Upload(null, receiver);
		upload.setButtonCaption("Add or Change Image");
		upload.addSucceededListener(receiver);

		ItemCaptionGenerator<String> icg = new ItemCaptionGenerator<String>() {
			@Override
			public String apply(String item) {
				return appCategoryService.findById(Long.parseLong(item)).getName();
			}
		};

		categoryComboBox.setPlaceholder("No category selected");
		categoryComboBox.setEmptySelectionAllowed(false);

		categoryComboBox.setItemCaptionGenerator(icg);
		subHlayout.addComponents(name, version);
		mainVLayout.addComponent(subHlayout);

		subHlayout = new HorizontalLayout();
		subHlayout.addComponents(owner, categoryComboBox);
		mainVLayout.addComponent(subHlayout);

		subHlayout = new HorizontalLayout();
		subHlayout.addComponents(description);
		mainVLayout.addComponent(subHlayout);

		subHlayout = new HorizontalLayout();
		subHlayout.addComponents(artifactDetailsBtn, upload);
		mainVLayout.addComponent(subHlayout);

		subHlayout = new HorizontalLayout();
		subHlayout.addComponents(appimage);
		mainVLayout.addComponent(subHlayout);

		subHlayout = new HorizontalLayout();
		subHlayout.addComponents(save, delete);
		mainVLayout.addComponent(subHlayout);

		addComponents(mainVLayout);

		binder.forField(categoryComboBox).bind(App -> {
			if (App.getAppcategory() != null) {
				// if there is any row and it is not empty
				return App.getAppcategory().getId().toString();
			}
			return null; // if address is null, return empty string
		}, (App, categoryId) -> {
			Long id = Long.parseLong(categoryId);
			App.setAppcategory(appCategoryService.findById(id));
		});

		binder.bindInstanceFields(this);

		setSpacing(true);
		save.setStyleName(ValoTheme.BUTTON_PRIMARY);
		save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		delete.setStyleName(ValoTheme.BUTTON_DANGER);
		artifactDetailsBtn.setStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);

		artifactDetailsBtn.addClickListener(e -> {

			artifactFileEditorWindow.center();
			VaadinUI.getCurrent().addWindow(artifactFileEditorWindow);
			artifactFileEditor.setmessageFeignClient(hawkbitFeignClient);
			artifactFileEditor.setHawkbitFeignClient(hawkbitMultiPartFileFeignClient);
			artifactFileEditor.editArtifactFile(Utils.getExistsSoftwareModule(softwareModuleResult.getContent()).toString());
		});

		setVisible(false);

	}

	public interface ChangeHandler {

		void onChange();
	}

	public final void editApp(App s) {
		if (s == null) {
			setVisible(false);
			return;
		}
		final boolean persisted = s.getId() != null;
		if (persisted) {
			// Find fresh entity for editing
			app = appService.findById(s.getId());
		} else {
			app = s;
		}
		delete.setVisible(persisted);
		upload.setVisible(persisted);
		appimage.setVisible(persisted);
		artifactDetailsBtn.setVisible(persisted);
		binder.setBean(app);

		setVisible(true);

		save.focus();
		name.selectAll();
		List<String> list = appCategoryService.getAllId();
		if (list.size() > 0) {
			categoryComboBox.setItems(list);
		} else {
			new Notification("Not Found Any App Category!", "Please add an App Category before you want to add an app!",
					Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
		}

		if (app.getName() != null && !app.getName().equals("")) {

			try {
				softwareModuleResult = hawkbitFeignClient
						.getSoftwaremoduleByName(Utils.createFIQLEqual("name", app.getName()));
			} catch (Exception e) {
				new Notification("Hawkbit connection error. Check your Hawkbit's IP in the propreties file!",
						Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
			}

		}
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
