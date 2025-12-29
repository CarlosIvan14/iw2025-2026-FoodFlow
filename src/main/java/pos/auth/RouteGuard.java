package pos.auth;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinSession;
import pos.domain.Role;
import java.util.Arrays;

public interface RouteGuard extends BeforeEnterObserver {
  @Override
  default void beforeEnter(BeforeEnterEvent event) {
    var us = VaadinSession.getCurrent().getAttribute(AuthService.UserSession.class);
    var path = event.getLocation().getPath();
    // Público: login, register
    if (path.startsWith("login") || path.startsWith("register")) return;
    if (us == null) { event.rerouteTo("login"); return; }

    // Si la vista declara @RequiredRoles, validar
    Class<?> target = event.getNavigationTarget();
    var ann = target.getAnnotation(RequiredRoles.class);
    if (ann != null) {
      String currentRole = us.role();
      if (currentRole == null) { event.rerouteTo("login"); return; }
      boolean ok = Arrays.stream(ann.value()).anyMatch(r -> r.name().equalsIgnoreCase(currentRole));
      if (!ok) {
        event.rerouteTo("login");
      }
    }
  }
}
