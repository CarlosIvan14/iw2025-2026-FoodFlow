package pos.auth;

import com.vaadin.flow.server.VaadinSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pos.repository.UserRepository;
import pos.domain.User;
import pos.ui.Broadcaster;

import java.io.Serializable;

@Service
@RequiredArgsConstructor
public class AuthService {
  public record UserSession(Long userId, String username, String role) implements Serializable {}

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void authenticate(String email, String password) throws AuthException {
      User user = userRepository.findByEmail(email)
              .orElseThrow(() -> new AuthException("Usuario no encontrado"));

      if (!passwordEncoder.matches(password, user.getPassword())) {
          throw new AuthException("Contraseña incorrecta");
      }

      if (!user.getActive()) {
          throw new AuthException("Usuario inactivo");
      }

      login(user.getId(), user.getName(), user.getRole().name());
  }

  private void login(Long userId, String username, String role){
    VaadinSession.getCurrent().setAttribute(UserSession.class, new UserSession(userId, username, role));
  }

  public boolean isAuthenticated(){
    return VaadinSession.getCurrent().getAttribute(UserSession.class) != null;
  }

  public String currentRole(){
    var us = VaadinSession.getCurrent().getAttribute(UserSession.class);
    return us != null ? us.role() : null;
  }

  public String currentUser(){
    var us = VaadinSession.getCurrent().getAttribute(UserSession.class);
    return us != null ? us.username() : null;
  }

  public Long currentUserId(){
      var us = VaadinSession.getCurrent().getAttribute(UserSession.class);
      return us != null ? us.userId() : null;
  }

  /**
   * Sincroniza la sesión actual con los datos de la BD.
   * Verifica si el rol ha cambiado y actualiza la sesión si es necesario.
   * Útil para reflejar cambios en tiempo real (ej: cambio de rol en admin panel).
   */
  public void syncSessionWithDatabase() {
    try {
      Long userId = currentUserId();
      if (userId == null) return; // Sin sesión activa

      var userInDb = userRepository.findById(userId).orElse(null);
      if (userInDb == null) return; // Usuario no encontrado

      String currentRole = currentRole();
      String dbRole = userInDb.getRole().name();

      // Si el rol cambió, actualizar la sesión
      if (!dbRole.equals(currentRole)) {
        login(userId, currentUser(), dbRole);
        // Notificar para que se refresque el drawer/menú
        try { Broadcaster.broadcast(); } catch (Exception ignored) {}
      }
    } catch (Exception e) {
      System.err.println("Error sincronizando sesión: " + e.getMessage());
    }
  }

  public static class AuthException extends Exception {
      public AuthException(String message) {
          super(message);
      }
  }

  public void logout(){
      VaadinSession.getCurrent().setAttribute(UserSession.class, null);

      // Notify layouts to refresh drawer
      try { Broadcaster.broadcast(); } catch (Exception ignored) {}

      VaadinSession.getCurrent().close();

      com.vaadin.flow.component.UI.getCurrent().getPage().setLocation("/login");
  }

}
