package org.eclipse.kuksa.appstore.ui;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
@SpringComponent
@UIScope
public class AccessDeniedView extends VerticalLayout implements View {
  private Label message;

  public AccessDeniedView() {
    setMargin(true);
    addComponent(message = new Label());
    message.setSizeUndefined();
    message.addStyleName(ValoTheme.LABEL_FAILURE);
  }

  @Override
  public void enter(ViewChangeListener.ViewChangeEvent event) {
    message.setValue(String.format("You do not have access to this view: %s", event.getViewName()));
  }
}
