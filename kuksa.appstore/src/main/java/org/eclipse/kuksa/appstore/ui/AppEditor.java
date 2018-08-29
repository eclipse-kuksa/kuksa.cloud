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
import org.eclipse.kuksa.appstore.service.AppService;
import org.eclipse.kuksa.appstore.utils.Utils;
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
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringComponent
@UIScope
public class AppEditor extends VerticalLayout implements View {
	
	@Autowired
	AppService appManagerService;
	public App app;
	public TextField name = new TextField("App Name");
	public TextArea description = new TextArea("Description");
	public TextField version = new TextField("Version");
	public TextField owner = new TextField("Owner");
	public Button save = new Button("Save", FontAwesome.SAVE);
	public Button cancel = new Button("Cancel",FontAwesome.CLOSE);
	public Button delete = new Button("Delete", FontAwesome.TRASH_O);
	public Upload upload;
	public Embedded appimage;
	HorizontalLayout hlayout = new HorizontalLayout();
	VerticalLayout vlayout = new VerticalLayout();
	Binder<App> binder = new Binder<>(App.class);

	@Autowired
	public AppEditor() {


		name.setWidth("200px");
		description.setWidth("600px");
		description.setHeight("180px");
		version.setWidth("200px");
		owner.setWidth("200px");
		appimage = new Embedded("App Image");
		appimage.setVisible(false);
		appimage.setWidth("75");
		appimage.setHeight("50");
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
				System.out.println(file.getAbsolutePath());

			}
		}
		;
		ImageUploader receiver = new ImageUploader();

		upload = new Upload(null, receiver);
		upload.setButtonCaption("Add or Change Image");
		upload.addSucceededListener(receiver);
		

		vlayout.addComponents(name,version, owner);
		hlayout.addComponent(vlayout);
		
		vlayout = new VerticalLayout();
		vlayout.addComponents(description);
		hlayout.addComponent(vlayout);

		vlayout = new VerticalLayout();
		vlayout.addComponents(save, delete,cancel);
		hlayout.addComponent(vlayout);

		vlayout = new VerticalLayout();
		vlayout.addComponents(upload, appimage);
		hlayout.addComponent(vlayout);

		addComponents(hlayout);
		binder.bindInstanceFields(this);

		setSpacing(true);
		save.setStyleName(ValoTheme.BUTTON_PRIMARY);
		save.setClickShortcut(ShortcutAction.KeyCode.ENTER);

		setVisible(false);
	}

	public interface ChangeHandler {

		void onChange();
	}

	public final void editStudent(App s) {
		if (s == null) {
			setVisible(false);
			return;
		}
		final boolean persisted = s.getId() != null;
		if (persisted) {
			// Find fresh entity for editing
			app = appManagerService.findById(s.getId());
		} else {
			app = s;
		}
		cancel.setVisible(persisted);
		upload.setVisible(persisted);
		appimage.setVisible(persisted);
		binder.setBean(app);

		setVisible(true);

		save.focus();
		name.selectAll();
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
