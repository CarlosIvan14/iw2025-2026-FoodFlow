package pos.ui;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    public MainLayout() {
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

        list.add(itemLink("Menú Digital", "/"));
        list.add(itemLink("Pedidos", "/ordenes"));
        list.add(itemLink("Mesas", "/mesas"));
        list.add(itemLink("Cocina", "/cocina"));
        list.add(itemLink("Caja", "/admin/caja"));
        list.add(itemLink("Inventario", "/admin/inventario"));
        list.add(itemLink("Analytics", "admin/analytics"));
        list.add(itemLink("Login", "/login"));
        list.add(itemLink("Registro", "/register"));
        list.add(itemLink("Reportes", "/reports"));


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
