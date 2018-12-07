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

	User currentUser;
	CustomLayout appslayout;
	VerticalLayout mainlayout;
	@Autowired
	MessageFeignClient messageFeignClient;
	App currentApp;
	@Autowired
	UserService userService;
	@Autowired
	AppService appService;

	@PostConstruct
	public void init() {
		if (VaadinSession.getCurrent().getAttribute("app") != null) {
			currentApp = appService.findById(Long.parseLong(VaadinSession.getCurrent().getAttribute("app").toString()));
		} else {

			new Notification("No Selected App", "You have to select an app to show it's details.",
					Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
			Page.getCurrent().setUriFragment("!" + AppsListView.VIEW_NAME);
		}

		currentUser = userService.findByUserName(VaadinSession.getCurrent().getAttribute("user").toString());
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME + "-" + currentApp.getName());
		mainlayout = new VerticalLayout();
		appslayout = new CustomLayout("my-layout");

		mainlayout.addComponent(new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString()));

		Image image = new Image();
		File new_file = new File(Utils.getImageFilePath() + currentApp.getId() + ".png");
		if (new_file.exists()) {
			image.setSource(new FileResource(new_file));
		} else {

			image.setSource(new ThemeResource(imgPath + "/noimage.png"));
		}

		appslayout.addComponent(image, "appimage");

		appslayout.addComponent(new Label(currentApp.getName()), "appname");

		createInstallButtonorBuyButton();

		Label categorylabel = new Label(currentApp.getAppcategory().getName());
		categorylabel.setStyleName("alnleft");
		categorylabel.setWidth(100.0f, Unit.PERCENTAGE);
		appslayout.addComponent(categorylabel, "appcategory");

		Label desclabel = new Label(currentApp.getDescription());
		desclabel.setStyleName("alnleft");
		desclabel.setWidth(100.0f, Unit.PERCENTAGE);
		appslayout.addComponent(desclabel, "appdesc");

		Label publishdatelabel = new Label(currentApp.getPublishdate().toLocaleString());
		publishdatelabel.setStyleName("alnleft");
		publishdatelabel.setWidth(100.0f, Unit.PERCENTAGE);
		appslayout.addComponent(publishdatelabel, "apppublishdate");

		Label versionlabel = new Label(currentApp.getVersion());
		versionlabel.setStyleName("alnleft");
		versionlabel.setWidth(100.0f, Unit.PERCENTAGE);
		appslayout.addComponent(versionlabel, "appversion");

		Label ownerlabel = new Label(currentApp.getOwner());
		ownerlabel.setStyleName("alnleft");
		ownerlabel.setWidth(100.0f, Unit.PERCENTAGE);
		appslayout.addComponent(ownerlabel, "appowner");

		Label countlabel = new Label(Integer.toString(currentApp.getDownloadcount()));
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
	private void createInstallButtonorBuyButton() {

		List<String> listOfTargets = getListOfTargets();
		boolean isOwner = userService.isUsersAppOwner(currentUser.getId().toString(), currentApp.getId().toString(),
				getListOfOem(listOfTargets));

		if (isOwner) {
			ComboBox<String> comboBoxDevice;
			comboBoxDevice = new ComboBox<>("Select A Device");
			comboBoxDevice.setItems(listOfTargets);
			comboBoxDevice.setPlaceholder("No device selected");
			comboBoxDevice.setEmptySelectionAllowed(false);

			comboBoxDevice.setWidth("300");
			appslayout.addComponent(comboBoxDevice, "appselectdevice");

			Button install_app = new Button("Install App");

			install_app.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					// TODO Auto-generated method stub

					if (comboBoxDevice.getValue() != null) {
						String queryname = "name==" + currentApp.getName();
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

							AssignedResult response = messageFeignClient
									.sendApptoDevice(comboBoxDevice.getSelectedItem().get(), ruleNew);
							if (response.getAssigned() > 0) {

								currentApp = appService.incrementAppDownloadCount(currentApp);

								List<User> list = currentApp.getInstalledusers();
								list.add(currentUser);
								currentApp.setInstalledusers(list);
								appService.updateApp(currentApp);

								new Notification("Succes Update Action",
										"The updating action has been sent to Hawkbit for selected device.",
										Notification.Type.TRAY_NOTIFICATION).show(com.vaadin.server.Page.getCurrent());

							} else if (response.getAlreadyAssigned() > 0) {
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
						new Notification("Select a Device", "You have to select a device!",
								Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());

					}
				}
			});

			install_app.setWidth("300");
			appslayout.addComponent(install_app, "appinstall");
		} else {

			Button install_app = new Button("Purchase this App");

			install_app.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					// TODO Auto-generated method stub

					new Notification("This app is purchased.", "Operation Success", Notification.Type.WARNING_MESSAGE)
							.show(com.vaadin.server.Page.getCurrent());
					App currentapp = appService.findById(currentApp.getId());

					List<User> list = currentapp.getOwnerusers();
					list.add(userService.findById(currentUser.getId().toString()));
					currentapp.setOwnerusers(list);
					appService.updateApp(currentapp);
					Page.getCurrent().reload();

				}
			});

			install_app.setWidth("300");
			appslayout.addComponent(install_app, "appinstall");

		}

	}
	private List<String> getListOfOem(List<String> listOfTargets) {
		List<String> listOfOem = new ArrayList<>();
		for (int i = 0; i < listOfTargets.size(); i++) {
			String deviceName = listOfTargets.get(i);
			int index = deviceName.indexOf("_");
			String oem = deviceName.substring(0, index);
			listOfOem.add(oem);
		}
		return listOfOem;
	}
	public List<String> getListOfTargets() {
		List<String> listOfTargets = new ArrayList<>();

		String dis = VaadinSession.getCurrent().getAttribute("user").toString();
		dis = "" + "*" + dis + "*";
		dis = "description==" + dis;
		List<TargetByData> deviceList = new ArrayList<>();
		try {
			deviceList = messageFeignClient.getTargetsByDes(dis, "name:ASC").getContent();
		} catch (Exception e) {
			new Notification("Not Found Hawkbit Instance",
					"Make sure that you connect any Hawkbit instance with this AppStore!",
					Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());
		}
		for (TargetByData targetByData : deviceList) {
			listOfTargets.add(targetByData.getControllerId());
		}
		return listOfTargets;

	}
}
