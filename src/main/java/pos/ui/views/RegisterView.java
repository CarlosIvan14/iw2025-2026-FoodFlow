package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.AuthService;
import pos.auth.RouteGuard;
import pos.domain.Role;
import pos.ui.MainLayout;

@PageTitle("Registro")
@Route(value="register", layout = MainLayout.class)
public class RegisterView extends VerticalLayout implements RouteGuard {
  public RegisterView(AuthService auth){
    var h = new H2("Registro (dev) -> auto-login");
    var user = new TextField("Usuario");
    user.setValue("nuevo");
    var role = new ComboBox<Role>("Rol");
    role.setItems(Role.values());
    role.setValue(Role.CLIENTE);
    var btn = new Button("Registrarme", e -> {
      auth.login(user.getValue(), role.getValue().name());
      getUI().ifPresent(ui -> ui.navigate("menu"));
    });
    add(h, user, role, btn);
  }
}
