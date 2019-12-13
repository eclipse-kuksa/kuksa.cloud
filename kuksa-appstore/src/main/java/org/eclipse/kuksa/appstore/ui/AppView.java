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

import org.eclipse.kuksa.appstore.client.HawkbitFeignClient;
import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.Oem;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.model.hawkbit.Result;
import org.eclipse.kuksa.appstore.service.AppService;
import org.eclipse.kuksa.appstore.service.UserService;
import org.eclipse.kuksa.appstore.ui.component.NavHeader;
import org.eclipse.kuksa.appstore.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

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
import com.vaadin.ui.Window;

@Secured({ "ROLE_USER", "ROLE_ADMIN" })
@SpringView(name = AppView.VIEW_NAME)
public class AppView extends CustomComponent implements View {

	public static final String VIEW_NAME = "app";

	public static final String TITLE_NAME = "App";

	private String imgPath = "img";
	final Window permissionConfirmWindow = new Window("Permission Confirm");

	User currentUser;
	CustomLayout appslayout;
	VerticalLayout mainlayout;
	@Autowired
	HawkbitFeignClient hawkbitFeignClient;
	App currentApp;
	@Autowired
	UserService userService;
	@Autowired
	AppService appService;
	Button purchase_install;
	Button uninstallButton;
	private PermissionConfirm permissionConfirm;
	ComboBox<String> comboBoxDevice;

	@Autowired
	public AppView(PermissionConfirm permissionConfirm) {
		this.permissionConfirm = permissionConfirm;

	}

	@PostConstruct
	public void init() {
		if (VaadinSession.getCurrent().getAttribute("app") != null) {
			currentApp = appService.findById(Long.parseLong(VaadinSession.getCurrent().getAttribute("app").toString()));
		} else {

			new Notification("No Selected App", "You have to select an app to show it's details.",
					Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
			Page.getCurrent().setUriFragment("!" + AppsListView.VIEW_NAME);
		}

		VerticalLayout popupContent = new VerticalLayout();
		popupContent.addComponent(permissionConfirm);
		permissionConfirmWindow.setContent(popupContent);
		permissionConfirmWindow.center();
		permissionConfirmWindow.setModal(true);
		permissionConfirmWindow.setResizable(false);

		currentUser = userService.findByUserName(VaadinSession.getCurrent().getAttribute("user").toString());
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME + "-" + currentApp.getName());
		mainlayout = new VerticalLayout();
		appslayout = new CustomLayout("my-layout");

		mainlayout.addComponent(new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString()));

		Image image = new Image();
		image.setWidth("300");
		image.setHeight("200");
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

		permissionConfirm.confirm.addClickListener(eventConfirm -> {
			VaadinUI.getCurrent().removeWindow(permissionConfirmWindow);
			try {
				Result<?> result = appService.InstallApp(comboBoxDevice.getSelectedItem().get(), currentUser.getId(),
						currentApp.getId());

				if (result.isSuccess()) {
					new Notification("Succes Install Action",
							"The installing action has been sent to Hawkbit for selected device.",
							Notification.Type.TRAY_NOTIFICATION).show(com.vaadin.server.Page.getCurrent());

				} else {
					new Notification("Fail Update Action", result.getErrorMessage(), Notification.Type.ERROR_MESSAGE)
							.show(com.vaadin.server.Page.getCurrent());

				}
			} catch (NotFoundException e) {
				new Notification(e.getMessage(), Notification.Type.ERROR_MESSAGE)
						.show(com.vaadin.server.Page.getCurrent());

			} catch (BadRequestException e) {
				new Notification(e.getMessage(), Notification.Type.ERROR_MESSAGE)
						.show(com.vaadin.server.Page.getCurrent());

			} catch (AlreadyExistException e) {
				new Notification(e.getMessage(), Notification.Type.ERROR_MESSAGE)
						.show(com.vaadin.server.Page.getCurrent());
			}

		});

		setCompositionRoot(mainlayout);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

	private void createInstallButtonorBuyButton() {

		List<String> listOfTargets;
		boolean isOwner;
		try {
			listOfTargets = appService.getListOfTargets(currentUser.getId());

			isOwner = userService.isUsersAppOwner(currentUser.getId().toString(), currentApp.getId(),
					appService.getListOfOem(listOfTargets));
		} catch (BadRequestException e1) {
			new Notification(e1.getMessage(), Notification.Type.ERROR_MESSAGE)
					.show(com.vaadin.server.Page.getCurrent());
			return;

		}

		if (isOwner) {

			comboBoxDevice = new ComboBox<>("Select A Device");
			comboBoxDevice.setItems(listOfTargets);
			comboBoxDevice.setPlaceholder("No device selected");
			comboBoxDevice.setEmptySelectionAllowed(false);

			comboBoxDevice.setWidth("300");
			appslayout.addComponent(comboBoxDevice, "appselectdevice");

			purchase_install = new Button("Install App");

			purchase_install.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {

					if (comboBoxDevice.getValue() != null) {

						permissionConfirmWindow.center();
						VaadinUI.getCurrent().addWindow(permissionConfirmWindow);

						if (permissionConfirm.listPermisson(currentApp.getId()) == 1) {
							VaadinUI.getCurrent().removeWindow(permissionConfirmWindow);
						}

					} else {
						new Notification("Select a Device", "You have to select a device!",
								Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());

					}
				}
			});

			uninstallButton = new Button("Uninstall App");

			uninstallButton.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {

					if (comboBoxDevice.getValue() != null) {

						try {
							List<Long> appIds = new ArrayList<Long>();
							appIds.add(currentApp.getId());
							Result<?> result = appService.UninstallMultiApp(comboBoxDevice.getSelectedItem().get(),
									currentUser.getId(), appIds);

							if (result.isSuccess()) {
								new Notification("Succes Uninstall Action",
										"The uninstalling action has been sent to Hawkbit for selected device.",
										Notification.Type.TRAY_NOTIFICATION).show(com.vaadin.server.Page.getCurrent());

							} else {
								new Notification("Fail Uninstall Action", result.getErrorMessage(),
										Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());

							}
						} catch (NotFoundException e) {
							new Notification(e.getMessage(), Notification.Type.ERROR_MESSAGE)
									.show(com.vaadin.server.Page.getCurrent());

						} catch (BadRequestException e) {
							new Notification(e.getMessage(), Notification.Type.ERROR_MESSAGE)
									.show(com.vaadin.server.Page.getCurrent());

						} catch (AlreadyExistException e) {
							new Notification(e.getMessage(), Notification.Type.ERROR_MESSAGE)
									.show(com.vaadin.server.Page.getCurrent());
						}

					} else {
						new Notification("Select a Device", "You have to select a device!",
								Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());

					}
				}
			});

			uninstallButton.setWidth("300");
			appslayout.addComponent(uninstallButton, "appuninstall");

		} else {

			purchase_install = new Button("Purchase this App");

			purchase_install.addClickListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					try {

						Result<?> result = appService.purchaseApp(currentUser.getId(), currentApp.getId());

						if (result.isSuccess()) {
							new Notification("This app is purchased.", "Operation Success",
									Notification.Type.WARNING_MESSAGE).show(com.vaadin.server.Page.getCurrent());
							Page.getCurrent().reload();
						} else {
							new Notification("This purchasing operation is failed.", result.getErrorMessage(),
									Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());

						}
					} catch (NotFoundException | BadRequestException e) {
						// TODO Auto-generated catch block
						new Notification("This purchasing operation is failed.", e.getMessage(),
								Notification.Type.WARNING_MESSAGE).show(com.vaadin.server.Page.getCurrent());
					}

				}
			});

		}

		purchase_install.setWidth("300");
		appslayout.addComponent(purchase_install, "appinstall");

	}

}
