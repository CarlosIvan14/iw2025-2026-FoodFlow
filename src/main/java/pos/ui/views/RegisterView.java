package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import pos.auth.AuthService;
import pos.domain.Role;
import pos.domain.User;
import pos.service.UserService;

@PageTitle("Registro")
@Route(value = "register")
@AnonymousAllowed
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final AuthService authService;
    private final Binder<User> binder = new Binder<>(User.class);

    public RegisterView(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2("Crear Cuenta");

        TextField name = new TextField("Nombre Completo");
        EmailField email = new EmailField("Correo Electrónico");
        PasswordField password = new PasswordField("Contraseña");
        PasswordField confirmPassword = new PasswordField("Confirmar Contraseña");

        // Configurar validaciones
        binder.forField(name)
                .asRequired("El nombre es obligatorio")
                .bind(User::getName, User::setName);

        binder.forField(email)
                .asRequired("El correo es obligatorio")
                .withValidator(e -> e.contains("@"), "Correo inválido")
                .bind(User::getEmail, User::setEmail);

        binder.forField(password)
                .asRequired("La contraseña es obligatoria")
                .withValidator(p -> p.length() >= 6, "Mínimo 6 caracteres")
                .bind(User::getPassword, User::setPassword);

        // Binder no soporta confirmación directa fácilmente, lo validamos al guardar
        
        Button registerButton = new Button("Registrarse", event -> {
            if (binder.writeBeanIfValid(new User())) {
                if (!password.getValue().equals(confirmPassword.getValue())) {
                    Notification.show("Las contraseñas no coinciden", 3000, Notification.Position.MIDDLE);
                    return;
                }

                try {
                    User newUser = new User();
                    binder.writeBean(newUser);
                    newUser.setRole(Role.CLIENT); // Por defecto CLIENT
                    newUser.setActive(true);
                    
                    userService.create(newUser);
                    
                    Notification.show("Cuenta creada con éxito. Por favor inicia sesión.");
                    getUI().ifPresent(ui -> ui.navigate(LoginView.class));
                } catch (Exception e) {
                    Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                }
            }
        });
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        FormLayout form = new FormLayout();
        form.add(name, email, password, confirmPassword);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.setMaxWidth("400px");

        add(title, form, registerButton, new RouterLink("¿Ya tienes cuenta? Inicia sesión", LoginView.class));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authService.isAuthenticated()) {
            event.forwardTo("");
        }
    }
}

