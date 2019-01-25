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
package org.eclipse.kuksa.appstore.ui.component;

import java.util.List;

import org.eclipse.kuksa.appstore.model.App;
import org.springframework.data.domain.Page;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public class AppGridView {
	public static CssLayout crateAppGridView(Page<App> appsList, int intFetchSize) {
		CssLayout mainlayout = new CssLayout();
		VerticalLayout vlayout = new VerticalLayout();
		HorizontalLayout hlayout;

		List<App> listsApp = appsList.getContent();

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
					hlayout.addComponent(AppViewBox.createAppViewBox(listsApp.get(index)));
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

}
