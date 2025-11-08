package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.AuthService;
import pos.domain.OrderItem;
import pos.domain.Product;
import pos.service.MenuService;
import pos.service.OrderService;
import pos.ui.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("")
@Route(value="", layout = MainLayout.class)
public class MenuView extends VerticalLayout {
  private final List<OrderItem> cart = new ArrayList<>();

  public MenuView(MenuService menu, OrderService orders, AuthService auth){
    var h = new H2("Menú digital");
    var cat = new TextField("Categoría (opcional)");
    var btnLoad = new Button("Cargar", e -> grid.setItems(
      cat.getValue()==null || cat.getValue().isBlank()? menu.list() : menu.byCategory(cat.getValue())
    ));

    grid = new Grid<>(Product.class, false);
    grid.addColumn(Product::name).setHeader("Producto");
    grid.addColumn(Product::category).setHeader("Categor?a");
    grid.addColumn(Product::price).setHeader("Precio");
    grid.addComponentColumn(p -> {
      var qty = new IntegerField();
      qty.setValue(1); qty.setMin(1); qty.setMax(20); qty.setWidth("90px");
      var btn = new Button("Añadir", ev -> {
        cart.add(new OrderItem(p.id(), p.name(), qty.getValue(), p.price(), ""));
        Notification.show(p.name()+" x"+qty.getValue()+" a?adido");
      });
      return new HorizontalLayout(qty, btn);
    }).setHeader("Acci?n");

    grid.setItems(menu.list());

    var delivery = new Checkbox("?Entrega a domicilio?");
    var address = new TextField("Direcci?n");
    var phone = new TextField("Tel?fono");
    address.setVisible(false); phone.setVisible(false);
    delivery.addValueChangeListener(ev -> {
      address.setVisible(ev.getValue());
      phone.setVisible(ev.getValue());
    });

    var btnOrder = new Button("Realizar pedido", e -> {
      if(!auth.isAuthenticated()){
        Notification.show("Inicia sesi?n para pedir");
        getUI().ifPresent(ui -> ui.navigate("login"));
        return;
      }
      if (cart.isEmpty()){
        Notification.show("Carrito vac?o");
        return;
      }
      var o = orders.createCustomerOrder(delivery.getValue(), address.getValue(), phone.getValue(), cart, auth.currentUser());
      Notification.show("Pedido realizado #"+o.getId()+" total $" + o.total());
      cart.clear();
    });

    add(h, new HorizontalLayout(cat, btnLoad), grid, delivery, address, phone, btnOrder);
  }

  private Grid<Product> grid;
}
