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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.kuksa.appstore.client.MessageFeignClient;
import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.AssignedResult;
import org.eclipse.kuksa.appstore.model.Rule;
import org.eclipse.kuksa.appstore.model.RuleMain;
import org.eclipse.kuksa.appstore.model.TargetByData;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.service.AppService;
import org.eclipse.kuksa.appstore.service.UserService;
import org.eclipse.kuksa.appstore.ui.component.NavHeader;
import org.eclipse.kuksa.appstore.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

@SpringView(name = AppView.VIEW_NAME)
public class AppView extends CustomComponent implements View {

	public static final String VIEW_NAME = "app";

	public static final String TITLE_NAME = "App";

	private String imgPath = "img";
	CustomLayout appslayout;
	VerticalLayout mainlayout;
	private final MessageFeignClient messageFeignClient;
	App currentapp;
	@Autowired
	UserService userManagerService;
	@Autowired
	AppService appManagerService;

	@Autowired
	public AppView(MessageFeignClient messageFeignClient) {
		this.messageFeignClient = messageFeignClient;
	}

	@PostConstruct
	public void init() {

		if (VaadinSession.getCurrent().getAttribute("app") != null) {
			currentapp = appManagerService
					.findById(Long.parseLong(VaadinSession.getCurrent().getAttribute("app").toString()));
		} else {

			new Notification("No Selected App", "You have to select an app to show it's details.",
					Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
			Page.getCurrent().setUriFragment("!" + AppsListView.VIEW_NAME);
		}

		User currentUser = userManagerService
				.findByUserName(VaadinSession.getCurrent().getAttribute("user").toString());
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME + "-" + currentapp.getName());
		mainlayout = new VerticalLayout();
		appslayout = new CustomLayout("my-layout");

		mainlayout.addComponent(new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString()));

		Image image = new Image();
		File new_file = new File(Utils.getImageFilePath() + currentapp.getId() + ".png");
		if (new_file.exists()) {
			image.setSource(new FileResource(new_file));
		} else {

			image.setSource(new ThemeResource(imgPath + "/noimage.png"));
		}

		appslayout.addComponent(image, "appimage");

		appslayout.addComponent(new Label(currentapp.getName()), "appname");
		ComboBox<String> comboBox = new ComboBox<>("Select A Device");

		List<String> listOfTargets = new ArrayList<>();

		String dis = VaadinSession.getCurrent().getAttribute("user").toString();
		dis = "" + "*" + dis + "*";
		dis = "description==" + dis;

		List<TargetByData> deviceList = messageFeignClient.getTargetsByDes(dis, "name:ASC").getContent();

		for (TargetByData targetByData : deviceList) {
			listOfTargets.add(targetByData.getControllerId());
		}

		comboBox.setItems(listOfTargets);
		comboBox.setPlaceholder("No device selected");
		comboBox.setEmptySelectionAllowed(false);
		comboBox.addValueChangeListener(event -> {
			/*
			 * if (event.getSource().isEmpty()) {
			 * 
			 * Notification.show("No artist selected"); } else if (event.getOldValue() ==
			 * null) { Notification.show("Selected artist: " + event.getValue()); }else {
			 * Notification.show( "Selected artist: " + event.getValue() +
			 * "\nThe old selection was: " + event.getOldValue()); }
			 */
		});
		comboBox.setWidth("300");
		appslayout.addComponent(comboBox, "appselectdevice");
		Button install_app = new Button("Install App");

		install_app.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub

				if (comboBox.getValue() != null) {
					String queryname = "name==" + currentapp.getHawkbitname();
					Rule ruleNew = new Rule();
					ruleNew.setForcetime("1530893371603");
					ruleNew.setId(messageFeignClient.getDistributionByName(queryname).getContent().get(0).getId());
					ruleNew.setType("timeforced");
					RuleMain rulemain = new RuleMain();
					rulemain.setDuration("00:10:00");
					rulemain.setSchedule("0 37 8 22 6 ? 2019");
					rulemain.setTimezone("+00:00");
					ruleNew.setMaintenanceWindow(rulemain);

					try {

						AssignedResult response = messageFeignClient.sendApptoDevice(comboBox.getSelectedItem().get(),
								ruleNew);
						if (response.getAssigned() > 0) {


							appManagerService.incrementAppDownloadCount(currentapp.getId());
							
							List<User> list = currentapp.getUsers();
							list.add(currentUser);
							currentapp.setUsers(list);
							appManagerService.updateApp(currentapp);


							new Notification("Succes Update Action",
									"The updating action has been sent to Hawkbit for selected device.",
									Notification.Type.TRAY_NOTIFICATION).show(com.vaadin.server.Page.getCurrent());

						}else if (response.getAlreadyAssigned() > 0) {
							new Notification("Already Assigned Update Action",
									"The updating action is already assigned for selected device.",
									Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());

						} else {
							new Notification("Fail Update Action",
									"The updating action hasnt been sent to Hawkbit for selected device.",
									Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());

						}
					} catch (Exception e) {
						// TODO: handle exception
						new Notification("Fail Update Action",
								"The updating action hasnt been sent to Hawkbit for selected device.",
								Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());

					}

				} else {
					new Notification("Select a Device", "You have to select a device!", Notification.Type.ERROR_MESSAGE)
							.show(com.vaadin.server.Page.getCurrent());

				}
			}
		});

		install_app.setWidth("300");
		appslayout.addComponent(install_app, "appinstall");

		Label desclabel = new Label(currentapp.getDescription());
		desclabel.setStyleName("alnleft");
		desclabel.setWidth(100.0f, Unit.PERCENTAGE);
		appslayout.addComponent(desclabel, "appdesc");

		Label publishdatelabel = new Label(currentapp.getPublishdate().toLocaleString());
		publishdatelabel.setStyleName("alnleft");
		publishdatelabel.setWidth(100.0f, Unit.PERCENTAGE);
		appslayout.addComponent(publishdatelabel, "apppublishdate");

		Label versionlabel = new Label(currentapp.getVersion());
		versionlabel.setStyleName("alnleft");
		versionlabel.setWidth(100.0f, Unit.PERCENTAGE);
		appslayout.addComponent(versionlabel, "appversion");

		Label ownerlabel = new Label(currentapp.getOwner());
		ownerlabel.setStyleName("alnleft");
		ownerlabel.setWidth(100.0f, Unit.PERCENTAGE);
		appslayout.addComponent(ownerlabel, "appowner");

		Label countlabel = new Label(Integer.toString(currentapp.getDownloadcount()));
		countlabel.setStyleName("alnleft");
		countlabel.setWidth(100.0f, Unit.PERCENTAGE);
		appslayout.addComponent(countlabel, "appcount");

		mainlayout.addComponent(appslayout);
		setCompositionRoot(mainlayout);

	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

}