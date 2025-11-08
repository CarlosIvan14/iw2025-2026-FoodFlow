package pos.auth;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;
import java.io.Serializable;

@Service
public class AuthService {
  public record UserSession(String username, String role) implements Serializable {}

  public void login(String username, String role){
    VaadinSession.getCurrent().setAttribute(UserSession.class, new UserSession(username, role));
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
}
