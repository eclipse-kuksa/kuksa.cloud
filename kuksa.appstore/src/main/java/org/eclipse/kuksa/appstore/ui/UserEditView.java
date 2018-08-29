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

import org.eclipse.kuksa.appstore.model.User;
import org.eclipse.kuksa.appstore.service.UserService;
import org.eclipse.kuksa.appstore.ui.component.NavHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.vaadin.data.ValueProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.HtmlRenderer;

import elemental.json.JsonArray;

@SpringView(name = UserEditView.VIEW_NAME)
public class UserEditView extends CustomComponent implements View {

	public static final String VIEW_NAME = "userlist";
	public static final String TITLE_NAME = "User List";
	private final UserEditor userEditor;

	final Grid<User> grid;

	final TextField searchText;

	private final Button addNewBtn;
	private final PopupView popup;

	@Autowired
	UserService userManagerService;

	@Autowired
	public UserEditView(UserEditor userEditor) {
		com.vaadin.server.Page.getCurrent().setTitle(TITLE_NAME);

		this.userEditor = userEditor;
		this.grid = new Grid<>(User.class);
		this.searchText = new TextField();
		this.addNewBtn = new Button("New User", FontAwesome.PLUS);
		addNewBtn.setStyleName("v-button-primary");

		VerticalLayout popupContent = new VerticalLayout();
		popupContent.addComponent(userEditor);
		popup = new PopupView(null, popupContent);

		// build layout
		HorizontalLayout actions = new HorizontalLayout(searchText, popup, addNewBtn);
		actions.setStyleName("v-actions");

		HorizontalLayout gridLayout = new HorizontalLayout();
		gridLayout.addComponent(grid);
		gridLayout.setWidth("100%");
		gridLayout.setStyleName("v-mainGrid");

		VerticalLayout mainLayout = new VerticalLayout(new NavHeader().create(VIEW_NAME,
				VaadinSession.getCurrent().getAttribute("isCurrentUserAdmin").toString()), actions, gridLayout);
		grid.setHeight(500, Unit.PIXELS);
		grid.setWidth("100%");
		grid.setColumns("id", "userName", "password", "adminuser");

		JavaScript.getCurrent().addFunction("executeOnServer", new JavaScriptFunction() {
			@Override
			public void call(JsonArray jsonArray) {
				// read parameters from jsonArray
				User item = userManagerService.findById(jsonArray.get(0).toJson().replaceAll("\"", "").toString());

				grid.select(item);

				popup.setPopupVisible(true);
			}
		});
		ValueProvider<User, String> vp = new ValueProvider() {
			@Override
			public Object apply(Object o) {
				User bean = (User) o;
				// getIcon() returns the FontAwesome HTML. Feel free to add parameters to the
				// call
				String s = "<html>\r\n" + "  <head>\r\n"
						+ "    <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\">\r\n"
						+ "        \r\n" + "  </head>\r\n" + "  <body  > <div onclick=\"executeOnServer('"
						+ bean.getId() + "');\">\r\n" + "    \r\n"
						+ "    <span class=\"glyphicon glyphicon-pencil\"  ></span>  <span> Edit</span> </div>  </body>\r\n"
						+ "</html>\r\n" + "";
				return s;
			}
		};
		grid.addColumn(vp, new HtmlRenderer()).setCaption("Edit").setMinimumWidth(100);

		// Connect selected Student to editor or hide if none is selected
		grid.asSingleSelect().addValueChangeListener(e -> {
			userEditor.editStudent(e.getValue());
		});

		// Instantiate and edit new Student the new button is clicked

		addNewBtn.addClickListener(e -> {
			popup.setPopupVisible(true);
			userEditor.editStudent(new org.eclipse.kuksa.appstore.model.User(null, "", "", false));
		});

		userEditor.save.addClickListener(e -> {

			try {
				userManagerService.updateUser(userEditor.user);
			} catch (Exception e1) {

				new Notification("Error", "Wrong Email!!", Notification.Type.ERROR_MESSAGE)
						.show(com.vaadin.server.Page.getCurrent());
				System.out.println("Exception thrown  :" + e1);
			}

			System.out.println(userEditor.user.getId());
			listUsers(null);
			userEditor.setVisible(false);
		});

		userEditor.delete.addClickListener(e -> {
			userManagerService.deleteUser(userEditor.user);
			listUsers(null);
			userEditor.setVisible(false);
		});

		userEditor.cancel.addClickListener(e -> {
			userEditor.editStudent(userEditor.user);
			listUsers(null);
			userEditor.setVisible(false);
		});

		// Listen changes made by the filter textbox, refresh data from backend
		searchText.setPlaceholder("Search by Username");
		searchText.addValueChangeListener(event -> {
			userEditor.setVisible(false);
			listUsers(event.getValue());
		});
		
		setCompositionRoot(mainLayout);
	}

	@PostConstruct
	public void init() {
		listUsers(null);
	}

	public void listUsers(String searchText) {
		if (StringUtils.isEmpty(searchText)) {
			grid.setItems(userManagerService.findAll());
		} else {
			grid.setItems(userManagerService.findByUserNameStartsWithIgnoreCase(searchText));
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
