package pos.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
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

  private void createHeader() {
    var toggle = new DrawerToggle();

    var title = new H1("POS · Restaurante");
    title.getStyle().set("font-size", "1.1rem");
    title.getStyle().set("margin", "0");

    var right = new Span(); // placeholder usuario/logout

    var header = new HorizontalLayout(toggle, title, right);
    header.setWidthFull();
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.setJustifyContentMode(JustifyContentMode.BETWEEN);
    header.addClassNames(
        LumoUtility.Padding.MEDIUM,
        LumoUtility.Border.BOTTOM,
        LumoUtility.BorderColor.CONTRAST_10);

    addToNavbar(header);
  }

  private void createDrawer() {
    var nav = new Nav();

    var list = new UnorderedList();
    list.getStyle().set("list-style", "none").set("padding", "0").set("margin", "0");

    // RUTAS QUE SÍ EXISTEN (según el mensaje de Vaadin)
    list.add(itemLink("Dashboard Mesas", "/mesas"));
    list.add(itemLink("Pedidos (Mesero)", "/ordenes/nueva"));
    list.add(itemLink("Cocina", "/cocina"));
    list.add(itemLink("Menú Digital", "/"));                 // raíz
    list.add(itemLink("Caja", "/admin/caja"));
    list.add(itemLink("Inventario", "/admin/inventario"));
    list.add(itemLink("Reportes", "/reports"));       // <-- antes apuntaba a /reports
    list.add(itemLink("Login", "/login"));
    list.add(itemLink("Registro", "/register"));

    nav.add(list);
    nav.getStyle().set("padding", "var(--lumo-space-m)");

    addToDrawer(nav);
  }

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
