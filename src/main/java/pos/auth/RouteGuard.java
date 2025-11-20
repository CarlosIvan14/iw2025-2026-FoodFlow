package pos.auth;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinSession;

public interface RouteGuard extends BeforeEnterObserver {
  @Override
  default void beforeEnter(BeforeEnterEvent event) {
    var us = VaadinSession.getCurrent().getAttribute(AuthService.UserSession.class);
    var path = event.getLocation().getPath();
    // P?blico: login, register, menu
    // Público: login, register, menu (root)
    if (path.isEmpty() || path.startsWith("login") || path.startsWith("register") || path.startsWith("menu")) return;
    if (us == null) event.rerouteTo("login");
  }
}
