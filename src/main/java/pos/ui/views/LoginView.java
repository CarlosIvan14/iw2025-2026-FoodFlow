package pos.ui.views;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import pos.auth.AuthService;
import pos.auth.RouteGuard;

@PageTitle("Login")
@Route(value = "login")
public class LoginView extends VerticalLayout implements RouteGuard {

    private final LoginForm login = new LoginForm();
    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;

        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.addLoginListener(e -> {
            try {
                authService.authenticate(e.getUsername(), e.getPassword());
                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (AuthService.AuthException ex) {
                login.setError(true);
            }
        });
        
        // Traducción básica al español
        login.setI18n(createSpanishI18n());

        add(login);
        add(new RouterLink("¿No tienes cuenta? Regístrate aquí", RegisterView.class));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authService.isAuthenticated()) {
            event.forwardTo("");
        }
    }
    
    private com.vaadin.flow.component.login.LoginI18n createSpanishI18n() {
        var i18n = com.vaadin.flow.component.login.LoginI18n.createDefault();
        
        i18n.getForm().setTitle("Iniciar Sesión");
        i18n.getForm().setUsername("Correo");
        i18n.getForm().setPassword("Contraseña");
        i18n.getForm().setSubmit("Entrar");
        i18n.getForm().setForgotPassword("Olvidé mi contraseña");
        i18n.getErrorMessage().setTitle("Error de acceso");
        i18n.getErrorMessage().setMessage("Correo o contraseña incorrectos");
        
        return i18n;
    }
}
