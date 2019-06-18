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

import javax.annotation.PostConstruct;

import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.service.AppService;
import org.eclipse.kuksa.appstore.service.UserService;
import org.eclipse.kuksa.appstore.ui.component.AppGridView;
import org.eclipse.kuksa.appstore.ui.component.NavHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vaadin.addon.pagination.Pagination;
import com.vaadin.addon.pagination.PaginationChangeListener;
import com.vaadin.addon.pagination.PaginationResource;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

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
	public PurchasedAppsListView() {
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME);
		appslayout = new CssLayout();
		mainlayout = new VerticalLayout();
	}

	@PostConstruct
	public void init() {
		int currentpage = 1;
		int limit = 6;
		int total;
		Page<App> appsList = findOwnApps(currentpage - 1, limit);
		total = (int) appsList.getTotalElements();
		appslayout = AppGridView.crateAppGridView(appsList, 2);

		navHeaderLayout = new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString());
		mainlayout.addComponent(navHeaderLayout);

		// mainlayout.addComponent(actions);
		mainlayout.addComponent(appslayout);
		paginationComponent = createPaginationComponent(total, currentpage, limit);
		mainlayout.addComponent(paginationComponent);
		setCompositionRoot(mainlayout);
	}

	public void listApps(String text) {

		int currentpage = 1;
		int limit = 6;
		int total;
		Page<App> appsList = findOwnApps(currentpage - 1, limit);
		total = (int) appsList.getTotalElements();
		CssLayout appslayoutnew = new CssLayout();
		appslayoutnew = AppGridView.crateAppGridView(appsList, 2);
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
				appslayoutnew = AppGridView.crateAppGridView(appsList, 2);
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
		currentUser = userService.findByUserName(VaadinSession.getCurrent().getAttribute("user").toString());
		Page<App> apps;
		try {
			apps = appService.findUsersApps(currentUser.getId().toString(),
					appService.getListOfOem(appService.getListOfTargets(currentUser.getId())), pageable);
		} catch (BadRequestException e) {
			new Notification(e.getMessage(), Notification.Type.ERROR_MESSAGE).show(com.vaadin.server.Page.getCurrent());
			return null;
		}

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

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

}
