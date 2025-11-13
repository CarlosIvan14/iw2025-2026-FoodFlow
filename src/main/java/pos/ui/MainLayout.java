package pos.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    //----------------------------------------------------------
    // HEADER (NAVBAR) — Glass flotante, sin título
    //----------------------------------------------------------
    private void createHeader() {

        var toggle = new DrawerToggle();     // botón hamburguesa
        var right = new Span();              // placeholder para usuario/logout

        var header = new HorizontalLayout(toggle, right);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // ❗ SIN BORDES ni fondo feo de Vaadin por defecto
        header.removeClassName(LumoUtility.Border.BOTTOM);
        header.removeClassName(LumoUtility.BorderColor.CONTRAST_10);

        // Estilo flotante glass
        header.addClassName("floating-navbar");

        addToNavbar(header);
    }


    //----------------------------------------------------------
    // DRAWER (MENÚ LATERAL)
    //----------------------------------------------------------
    private void createDrawer() {
        var nav = new Nav();
        var list = new UnorderedList();

        list.getStyle()
                .set("list-style", "none")
                .set("padding", "0")
                .set("margin", "0");

        list.add(itemLink("Dashboard Mesas", "/mesas"));
        list.add(itemLink("Pedidos (Mesero)", "/ordenes/nueva"));
        list.add(itemLink("Cocina", "/cocina"));
        list.add(itemLink("Menú Digital", "/"));
        list.add(itemLink("Caja", "/admin/caja"));
        list.add(itemLink("Inventario", "/admin/inventario"));
        list.add(itemLink("Reportes", "/reports"));
        list.add(itemLink("Login", "/login"));
        list.add(itemLink("Registro", "/register"));

        nav.add(list);
        nav.getStyle().set("padding", "var(--lumo-space-m)");

        addToDrawer(nav);
    }


    //----------------------------------------------------------
    // UTILIDAD PARA CREAR LINKS DEL DRAWER
    //----------------------------------------------------------
    private ListItem itemLink(String text, String href) {

        var a = new Anchor(href, text);
        a.getStyle()
                .set("display", "block")
                .set("padding", "0.5rem 0.75rem")
                .set("border-radius", "0.5rem")
                .set("text-decoration", "none");

        a.addClassNames(LumoUtility.TextColor.BODY);

        var li = new ListItem(a);
        li.getStyle().set("margin-bottom", "0.25rem");

        return li;
    }
}
