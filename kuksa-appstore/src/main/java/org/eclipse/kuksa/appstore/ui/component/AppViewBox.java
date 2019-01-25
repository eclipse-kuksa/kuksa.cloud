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

import java.io.File;

import org.eclipse.kuksa.appstore.model.App;
import org.eclipse.kuksa.appstore.ui.AppView;
import org.eclipse.kuksa.appstore.utils.Utils;

import com.vaadin.server.FileResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class AppViewBox {

	public static VerticalLayout createAppViewBox(App app) {
		VerticalLayout vinsidelayout = new VerticalLayout();

		String imageName = "app" + app.getId() + ".png";

		Image image = new Image();
		File new_file = new File(Utils.getImageFolderPath() + File.separator + imageName);
		if (new_file.exists()) {
			image.setSource(new FileResource(new_file));
		} else {

			image.setSource(new ThemeResource("img/noimage.png"));
		}
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
}
