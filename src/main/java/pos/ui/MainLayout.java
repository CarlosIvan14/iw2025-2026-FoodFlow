package pos.ui;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import pos.auth.AuthService;

public class MainLayout extends AppLayout {

    private final AuthService authService;

    public MainLayout(AuthService authService) {
        this.authService = authService;

        setPrimarySection(Section.DRAWER);   // 🌟 sin navbar fija

        var toggle = new DrawerToggle();
        toggle.addClassName("floating-toggle");
        addToNavbar(toggle);                // 🌟 solo el botón, sin barra

        createDrawer();
    }

    private void createDrawer() {
        var nav = new Nav();
        var list = new UnorderedList();

        list.getStyle().set("list-style", "none").set("padding", "0");

        // Mostrar opciones según rol
        String role = authService.currentRole();

        // Menú siempre disponible para usuarios autenticados
        if (authService.isAuthenticated()) {
            list.add(itemLink("Menú Digital", "/"));
        }

        if (hasRole(role, "MESERO") || hasRole(role, "ADMIN")) {
            list.add(itemLink("Pedidos", "/ordenes"));
            list.add(itemLink("Mesas", "/mesas"));
        }

        if (hasRole(role, "COCINERO") || hasRole(role, "ADMIN")) {
            list.add(itemLink("Cocina", "/cocina"));
        }

        if (hasRole(role, "CAJERO") || hasRole(role, "ADMIN")) {
            list.add(itemLink("Caja", "/admin/caja"));
        }

        if (hasRole(role, "ADMIN")) {
            list.add(itemLink("Productos", "/admin/productos"));
            list.add(itemLink("Ingredientes", "/admin/ingredientes"));
            list.add(itemLink("Analytics", "/admin/analytics"));
        }

        // Opciones para cliente (si existe rol CLIENT o CLIENTE)
        if (hasRole(role, "CLIENT") || hasRole(role, "CLIENTE")) {
            list.add(itemLink("Mis Pedidos", "/mis-pedidos"));
        }
        //list.add(itemLink("Analytics", "/admin/analytics"));
        //list.add(itemLink("Login", "/login"));
        //list.add(itemLink("Registro", "/register"));
        //list.add(itemLink("Reportes", "/reports"));

        // Separador visual
        var separador = new ListItem();
        separador.getStyle()
                .set("border-top", "1px solid #e0e0e0")
                .set("margin-top", "0.5rem")
                .set("margin-bottom", "0.5rem");
        list.add(separador);

        // Botão de logout como item do menu
        list.add(itemLogout());

        nav.add(list);
        nav.getStyle().set("padding", "1rem");

        var footer = createFooter();

        addToDrawer(nav, footer);
    }

    private ListItem itemLink(String text, String href) {
        var a = new Anchor(href, text);
        a.getStyle().set("text-decoration", "none");
        a.addClassNames(LumoUtility.TextColor.BODY);

        var li = new ListItem(a);
        li.getStyle().set("margin-bottom", "0.3rem");

        return li;
    }

    private ListItem itemLogout() {
        Button logoutBtn = new Button("Salir", new Icon(VaadinIcon.SIGN_OUT));
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutBtn.addClickListener(e -> authService.logout());
        logoutBtn.getStyle()
                .set("width", "100%")
                .set("justify-content", "flex-start")
                .set("padding-left", "0")
                .set("color", "var(--lumo-body-text-color)");

        var li = new ListItem(logoutBtn);
        li.getStyle().set("margin-bottom", "0.3rem");

        return li;
    }

    private boolean hasRole(String role, String expected) {
        if (role == null) return false;
        return role.equalsIgnoreCase(expected);
    }

    private Footer createFooter() {
        var footer = new Footer();

        // Estilo básico para ficar centralizado e com letra menor
        footer.getStyle().set("padding", "20px");
        footer.getStyle().set("text-align", "center");
        footer.getStyle().set("font-size", "0.85rem");
        footer.getStyle().set("color", "gray");
        footer.getStyle().set("border-top", "1px solid #eee"); // Linha separadora

        // Criando os textos
        var nome = new Div(new Text("FoodFlow Systems"));
        nome.getStyle().set("font-weight", "bold");

        var email = new Div(new Text("suporte@foodflow.com"));
        var contato = new Div(new Text("+55 11 9999-9999"));

        // Adicionando tudo ao footer
        footer.add(nome, email, contato);

        return footer;
    }
}