
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

import org.eclipse.kuksa.appstore.model.Oem;
import org.eclipse.kuksa.appstore.service.OemService;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.Binder;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringComponent
@UIScope
public class OemEditor extends VerticalLayout implements View {

	@Autowired
	OemService oemService;
	public Oem oem;
	public TextField name = new TextField("Oem Name");
	public Button save = new Button("Save", FontAwesome.SAVE);
	public Button cancel = new Button("Cancel", FontAwesome.CLOSE);
	public Button delete = new Button("Delete", FontAwesome.TRASH_O);
	HorizontalLayout hlayout = new HorizontalLayout();
	VerticalLayout vlayout = new VerticalLayout();
	Binder<Oem> binder = new Binder<>(Oem.class);

	@Autowired
	public OemEditor() {

		name.setWidth("200px");

		vlayout.addComponents(name);
		hlayout.addComponent(vlayout);

		vlayout = new VerticalLayout();
		hlayout.addComponent(vlayout);

		vlayout = new VerticalLayout();
		vlayout.addComponents(save, delete, cancel);
		hlayout.addComponent(vlayout);

		vlayout = new VerticalLayout();
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

	public final void editOem(Oem s) {
		if (s == null) {
			setVisible(false);
			return;
		}
		final boolean persisted = s.getId() != null;
		if (persisted) {
			// Find fresh entity for editing
			oem = oemService.findById(s.getId());
		} else {
			oem = s;
		}
		delete.setVisible(persisted);
		cancel.setVisible(true);
		binder.setBean(oem);

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
