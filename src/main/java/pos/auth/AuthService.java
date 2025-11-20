package pos.auth;

import com.vaadin.flow.server.VaadinSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pos.repository.UserRepository;
import pos.domain.User;

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

  public void logout(){
    VaadinSession.getCurrent().close();
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

  public static class AuthException extends Exception {
      public AuthException(String message) {
          super(message);
      }
  }
}
