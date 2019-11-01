
/*******************************************************************************
 * Copyright (C) 2019 Netas Telekomunikasyon A.S.
 *  
 *  This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *  
 * SPDX-License-Identifier: EPL-2.0
 *  
 * Contributors:
 * Adem Kose (Netas Telekomunikasyon A.S.) - Initial functionality
 ******************************************************************************/
package org.eclipse.kuksa.appstore.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.kuksa.appstore.client.HawkbitFeignClient;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.Permission;
import org.eclipse.kuksa.appstore.model.hawkbit.Artifact;
import org.eclipse.kuksa.appstore.model.hawkbit.SoftwareModuleResult;
import org.eclipse.kuksa.appstore.repo.AppRepository;
import org.eclipse.kuksa.appstore.service.AppService;
import org.eclipse.kuksa.appstore.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringComponent
@UIScope
public class PermissionConfirm extends VerticalLayout implements View {

	public ListSelect permissionsList;
	public Button confirm = new Button("Confirm", FontAwesome.CHECK);
	@Autowired
	AppService appService;

	@Autowired
	public PermissionConfirm() {

		setVisible(false);
	}

	public interface ChangeHandler {

		void onChange();
	}

	public final void listPermisson(Long appId) {

		HorizontalLayout hlayout = new HorizontalLayout();
		VerticalLayout vlayout = new VerticalLayout();
		removeAllComponents();
		permissionsList = new ListSelect<>(
				"<b><font color=\"red\">This application wants the following permissions!</font></b>");
		String responseDownloadArtifactString;
		try {
			responseDownloadArtifactString = appService.downloadPermissionArtifactFile(appId);
			if (responseDownloadArtifactString != null) {
				try {
					permissionsList = new ListSelect<>(
							"<b><font color=\"red\">This application wants the following permissions!</font></b>",
							Utils.permissionArtifactStringToList(responseDownloadArtifactString));
				} catch (JsonParseException e) {
					new Notification("PERMISSIONFILE.JSON Json Parse Exception", e.getMessage(),
							Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
				} catch (JsonMappingException e) {
					new Notification("PERMISSIONFILE.JSON Json Mapping Exception", e.getMessage(),
							Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
				} catch (IOException e) {
					new Notification("PERMISSIONFILE.JSON IO Exception", e.getMessage(),
							Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
				}
			} else {
				new Notification("PERMISSIONFILE.JSON not found or empty.", Notification.Type.ERROR_MESSAGE)
						.show(Page.getCurrent());
			}
		} catch (BadRequestException e1) {
			new Notification("PERMISSIONFILE.JSON not found or empty.", Notification.Type.ERROR_MESSAGE)
					.show(Page.getCurrent());
		} catch (NotFoundException e1) {
			new Notification("PERMISSIONFILE.JSON not found or empty.", Notification.Type.ERROR_MESSAGE)
					.show(Page.getCurrent());
		}

		permissionsList.setRows(10);
		permissionsList.setWidth(100.0f, Unit.PERCENTAGE);
		permissionsList.setReadOnly(true);
		permissionsList.setCaptionAsHtml(true);

		hlayout.addComponents(permissionsList);
		vlayout.addComponents(hlayout, new Label("If you want to install this application, please confirm."));

		hlayout = new HorizontalLayout();
		hlayout.addComponents(confirm);
		vlayout.addComponent(hlayout);

		addComponents(vlayout);

		setSpacing(true);
		confirm.setStyleName(ValoTheme.BUTTON_PRIMARY);
		confirm.setClickShortcut(ShortcutAction.KeyCode.ENTER);

		setVisible(true);

		confirm.focus();

	}

	public void setChangeHandler(ChangeHandler h) {
		confirm.addClickListener(e -> h.onChange());
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}
}
