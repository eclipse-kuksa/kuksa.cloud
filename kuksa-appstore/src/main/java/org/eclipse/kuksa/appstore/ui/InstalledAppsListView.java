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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.kuksa.appstore.exception.AlreadyExistException;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.exception.NotFoundException;
import org.eclipse.kuksa.appstore.model.hawkbit.DistributionResult;
import org.eclipse.kuksa.appstore.model.hawkbit.SoftwareModule;
import org.eclipse.kuksa.appstore.service.AppService;
import org.eclipse.kuksa.appstore.service.UserService;
import org.eclipse.kuksa.appstore.ui.component.NavHeader;
import org.eclipse.kuksa.appstore.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

import com.vaadin.addon.pagination.Pagination;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

@Secured({"ROLE_USER", "ROLE_ADMIN"})
@SpringView(name = InstalledAppsListView.VIEW_NAME)
public class InstalledAppsListView extends CustomComponent implements View {

	public static final String VIEW_NAME = "installedapps";

	public static final String TITLE_NAME = "Installed Apps";

	// final TextField searchText;

	private final Button uninstallAppBtn = new Button("Uninstall", FontAwesome.TRASH_O);
	private final Button selectAllAppBtn = new Button("Select All", FontAwesome.CHECK_SQUARE_O);
	private final Button deselectAllAppBtn = new Button("Deselect All", FontAwesome.SQUARE_O);
	ListSelect<String> sample;
	VerticalLayout mainlayout;
	VerticalLayout appslayout;
	HorizontalLayout navHeaderLayout;
	HorizontalLayout actions;
	@Autowired
	AppService appService;
	@Autowired
	UserService userService;
	List<String> data;
	ComboBox<String> comboBoxDevice;
	HorizontalLayout actionList;

	@Autowired
	public InstalledAppsListView() {
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME);
		appslayout = new VerticalLayout();
		mainlayout = new VerticalLayout();

		uninstallAppBtn.addClickListener(e -> {
			Set<String> selectedApps = sample.getSelectedItems();
			List<Long> appIds = new ArrayList<>();
			for (String appName : selectedApps) {
				appIds.add(appService.findByName(appName).getId());
			}
			if (appIds.size() != 0) {
				try {
					appService.UninstallMultiApp(
							comboBoxDevice.getSelectedItem().get().toString(), userService
									.findByUserName(VaadinSession.getCurrent().getAttribute("user").toString()).getId(),
							appIds);

					listInstalledApps(comboBoxDevice.getSelectedItem().get().toString());

					new Notification("Succes Unistalling", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());

				} catch (NotFoundException e1) {
					new Notification("Failed Unistalling", e1.getMessage(), Notification.Type.ERROR_MESSAGE)
							.show(Page.getCurrent());
				} catch (BadRequestException e1) {
					new Notification("Failed Unistalling", e1.getMessage(), Notification.Type.ERROR_MESSAGE)
							.show(Page.getCurrent());
				} catch (AlreadyExistException e1) {
					new Notification("Failed Unistalling", e1.getMessage(), Notification.Type.ERROR_MESSAGE)
							.show(Page.getCurrent());
				}
			} else {
				new Notification("Select at least one application to uninstall", Notification.Type.ERROR_MESSAGE)
						.show(Page.getCurrent());
			}

		});
		selectAllAppBtn.addClickListener(e -> {
			for (String app : data) {
				sample.select(app);
			}
		});
		deselectAllAppBtn.addClickListener(e -> {
			sample.deselectAll();
		});
	}

	@PostConstruct
	public void init() {
		navHeaderLayout = new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString());
		mainlayout.addComponent(navHeaderLayout);

		comboBoxDevice = new ComboBox<>();
		try {
			comboBoxDevice.setItems(appService.getListOfTargets(
					userService.findByUserName(VaadinSession.getCurrent().getAttribute("user").toString()).getId()));
		} catch (BadRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		comboBoxDevice.setPlaceholder("No device selected");
		comboBoxDevice.setEmptySelectionAllowed(false);

		comboBoxDevice.setWidth("300");

		HorizontalLayout actionBar = new HorizontalLayout();
		actionBar.addComponent(selectAllAppBtn);
		actionBar.addComponent(deselectAllAppBtn);
		actionBar.addComponent(uninstallAppBtn);
		appslayout.addComponent(actionBar);
		actionList = new HorizontalLayout();
		// actionList.addComponent(sample);
		actionList.setWidth(50.0f, Unit.PERCENTAGE);
		appslayout.addComponent(actionList);

		listInstalledApps(null);

		comboBoxDevice.addValueChangeListener(event -> {

			listInstalledApps(comboBoxDevice.getSelectedItem().get().toString());

		});

		Label selectDeviceLabel = new Label("Select a device");
		actions = new HorizontalLayout(selectDeviceLabel, comboBoxDevice);
		actions.setStyleName("v-actions");
		mainlayout.addComponent(actions);
		mainlayout.addComponent(appslayout);
		setCompositionRoot(mainlayout);
	}

	private VerticalLayout createContent(Pagination pagination) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.addComponents(pagination);
		return layout;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

	public void listInstalledApps(String targetDeviceName) {
		data = new ArrayList<>();
		DistributionResult distributionResult;
		try {
			if (targetDeviceName != null) {
				distributionResult = appService.getDistributionOfTarget(targetDeviceName);
				if (distributionResult.getSize() > 0) {

					List<SoftwareModule> softwareModules = distributionResult.getContent().get(0).getModules();

					for (SoftwareModule softwareModule : softwareModules) {
						if (!softwareModule.getName().equals(Utils.UNINSTALLED_ALL)) {
							data.add(softwareModule.getName());
						}
					}
					if (data.size() == 0) {
						new Notification("There is no installed application for this device.",
								Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
					}
				}
			}
			sample = new ListSelect<>("You can use Ctrl|Shift keys to select multi rows.", data);
			sample.setWidth(100.0f, Unit.PERCENTAGE);
			actionList.removeAllComponents();
			actionList.addComponent(sample);

		} catch (BadRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
