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
import org.eclipse.kuksa.appstore.model.TargetByData;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.service.AppService;
import org.eclipse.kuksa.appstore.service.UserService;
import org.eclipse.kuksa.appstore.ui.component.NavHeader;
import org.eclipse.kuksa.appstore.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vaadin.addon.pagination.Pagination;
import com.vaadin.addon.pagination.PaginationChangeListener;
import com.vaadin.addon.pagination.PaginationResource;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringView(name = PurchasedAppsListView.VIEW_NAME)
public class PurchasedAppsListView extends CustomComponent implements View {

	public static final String VIEW_NAME = "purchasedapps";

	public static final String TITLE_NAME = "Purchased Apps";
	User currentUser;
	VerticalLayout mainlayout;
	CssLayout appslayout;
	Component paginationComponent;
	HorizontalLayout navHeaderLayout;
	@Autowired
	UserService userService;
	@Autowired
	AppService appService;
	@Autowired
	MessageFeignClient messageFeignClient;
	@Autowired
	public PurchasedAppsListView() {
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME);
		appslayout = new CssLayout();
		mainlayout = new VerticalLayout();
	}

	@PostConstruct
	public void init() {
		currentUser = userService.findByUserName(VaadinSession.getCurrent().getAttribute("user").toString());
		int currentpage = 1;
		int limit = 6;
		int total;
		Page<App> appsList = findOwnApps(currentpage - 1, limit);
		total = (int) appsList.getTotalElements();
		appslayout = crateAppLayout(appsList);

		navHeaderLayout = new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString());
		mainlayout.addComponent(navHeaderLayout);

		//mainlayout.addComponent(actions);
		mainlayout.addComponent(appslayout);
		paginationComponent = createPaginationComponent(total, currentpage, limit);
		mainlayout.addComponent(paginationComponent);
		setCompositionRoot(mainlayout);
	}

	// basla
	public void listApps(String text) {

		int currentpage = 1;
		int limit = 6;
		int total;
		Page<App> appsList = findOwnApps(currentpage - 1, limit);
		total = (int) appsList.getTotalElements();
		CssLayout appslayoutnew = new CssLayout();
		appslayoutnew = crateAppLayout(appsList);
		mainlayout.removeAllComponents();
		mainlayout.addComponent(navHeaderLayout);
		mainlayout.addComponent(appslayoutnew);
		paginationComponent = createPaginationComponent(total, currentpage, limit);
		mainlayout.addComponent(paginationComponent);
		setCompositionRoot(mainlayout);

	}

	public Component createPaginationComponent(int total, int currentpage, int limit) {

		final Pagination pagination = createPagination(total, currentpage, limit);
		pagination.setItemsPerPageVisible(false);
		pagination.setEnabled(true);
		pagination.setResponsive(true);
		pagination.setTotalCount(total);
		pagination.setCurrentPage(currentpage);

		pagination.addPageChangeListener(new PaginationChangeListener() {
			@Override
			public void changed(PaginationResource event) {
				Page<App> appsList = findOwnApps(event.pageIndex(), event.limit());
				CssLayout appslayoutnew = new CssLayout();
				appslayoutnew = crateAppLayout(appsList);
				mainlayout.removeAllComponents();
				mainlayout.addComponent(navHeaderLayout);
				mainlayout.addComponent(appslayoutnew);
				mainlayout.addComponent(paginationComponent);
				setCompositionRoot(mainlayout);

			}
		});
		final VerticalLayout layout = createContent(pagination);
		return layout;
	}

	public Page<App> findOwnApps(int page, int size) {
		Pageable pageable = new PageRequest(page, size);
		Page<App> apps = appService.findUsersApps(currentUser.getId().toString(), getListOfOem(getListOfTargets()), pageable);

		return apps;
	}

	private Pagination createPagination(long total, int page, int limit) {
		final PaginationResource paginationResource = PaginationResource.newBuilder().setTotal(total).setPage(page)
				.setLimit(limit).build();
		final Pagination pagination = new Pagination(paginationResource);
		pagination.setItemsPerPage(10, 20, 50, 100);
		return pagination;
	}

	private VerticalLayout createContent(Pagination pagination) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.addComponents(pagination);
		return layout;
	}

	private CssLayout crateAppLayout(Page<App> appsList) {
		CssLayout mainlayout = new CssLayout();
		VerticalLayout vlayout = new VerticalLayout();
		HorizontalLayout hlayout;

		List<App> listsApp = appsList.getContent();

		int intFetchSize = 2;
		int noOfRec = listsApp.size();
		int rows = noOfRec / intFetchSize;
		if (noOfRec % intFetchSize != 0) {
			rows++;
		}
		for (int i = 0; i < rows; i++) {
			hlayout = new HorizontalLayout();
			for (int j = 0; j < intFetchSize; j++) {

				int index = (i * intFetchSize) + (j);
				if (index < listsApp.size()) {
					hlayout.addComponent(createAppItem(listsApp.get(index)));
				} else {
					break;
				}

			}
			vlayout.addComponent(hlayout);
		}
		mainlayout.addComponent(vlayout);

		mainlayout.setSizeFull();

		mainlayout.addStyleName("v-scrollable");
		mainlayout.addStyleName("h-scrollable");
		mainlayout.setHeight("100%");

		return mainlayout;

	}

	private VerticalLayout createAppItem(App app) {
		VerticalLayout vinsidelayout = new VerticalLayout();

		String imageName = "app" + app.getId() + ".png";

		Image image = new Image();
		File new_file = new File(Utils.getImageFolderPath() + File.separator + imageName);
		image.setSource(new FileResource(new_file));
		image.setWidth("300");
		image.setHeight("200");
		image.addClickListener(e -> {
			VaadinSession.getCurrent().setAttribute("app", app.getId());
			com.vaadin.server.Page.getCurrent().setUriFragment("!" + AppView.VIEW_NAME);
		});
		Label namelabel = new Label(app.getName());
		namelabel.setWidth("300");
		namelabel.addStyleName(ValoTheme.LABEL_BOLD);
		Label versionlabel = new Label(app.getVersion() + "    /    " + app.getOwner());
		versionlabel.setWidth("300");

		vinsidelayout.addComponent(image);
		vinsidelayout.addComponent(namelabel);
		vinsidelayout.addComponent(versionlabel);

		return vinsidelayout;

	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

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
