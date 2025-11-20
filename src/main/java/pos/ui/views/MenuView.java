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
import pos.domain.Product;
import pos.ui.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Menú Digital")
@Route(value = "", layout = MainLayout.class)
public class MenuView extends VerticalLayout {

  private final List<OrderItem> cart = new ArrayList<>();
  private final Grid<Product> grid;

  public MenuView(MenuService menu, OrderService orders, AuthService auth) {
    addClassName("menu-view");
    setSizeFull();
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.START);
    setSpacing(true);
    setPadding(true);

    var title = new H2("Menú Digital");
    title.addClassName("menu-title");

    // ✅ Primero inicializamos el grid
    grid = new Grid<>(Product.class, false);
    grid.addClassName("menu-grid");
    grid.addColumn(Product::name).setHeader("Producto");
    grid.addColumn(Product::category).setHeader("Categoría");
    grid.addColumn(Product::price).setHeader("Precio");
    grid.addComponentColumn(p -> {
      var qty = new IntegerField();
      qty.setValue(1);
      qty.setMin(1);
      qty.setMax(20);
      qty.setWidth("80px");

      var addBtn = new Button("Añadir", ev -> {
        cart.add(new OrderItem(p.id(), p.name(), qty.getValue(), p.price(), ""));
        Notification.show(p.name() + " x" + qty.getValue() + " añadido al carrito");
      });
      addBtn.addClassName("menu-add-btn");

      return new HorizontalLayout(qty, addBtn);
    }).setHeader("Acción");
    grid.setItems(menu.list());

    // ✅ Luego creamos los controles que usan el grid
    var category = new TextField("Categoría (opcional)");
    category.addClassName("menu-input");

    var btnLoad = new Button("Cargar", e -> {
      if (category.getValue() == null || category.getValue().isBlank()) {
        grid.setItems(menu.list());
      } else {
        grid.setItems(menu.byCategory(category.getValue()));
      }
    });
    btnLoad.addClassName("menu-btn");

    var header = new HorizontalLayout(category, btnLoad);
    header.setAlignItems(Alignment.END);
    header.addClassName("menu-header");

    var delivery = new Checkbox("¿Entrega a domicilio?");
    delivery.addClassName("menu-checkbox");

    var address = new TextField("Dirección");
    var phone = new TextField("Teléfono");
    address.setVisible(false);
    phone.setVisible(false);
    delivery.addValueChangeListener(e -> {
      address.setVisible(e.getValue());
      phone.setVisible(e.getValue());
    });
    address.addClassName("menu-input");
    phone.addClassName("menu-input");

    var btnOrder = new Button("Realizar pedido", e -> {
      if (!auth.isAuthenticated()) {
        Notification.show("Inicia sesión para realizar pedidos");
        getUI().ifPresent(ui -> ui.navigate("login"));
        return;
      }
      if (cart.isEmpty()) {
        Notification.show("El carrito está vacío");
        return;
      }
      var o = orders.createCustomerOrder(
        delivery.getValue(), address.getValue(), phone.getValue(), cart, auth.currentUser()
      );
      Notification.show("Pedido realizado #" + o.getId() + " — Total: $" + o.total());
      cart.clear();
    });
    btnOrder.addClassName("menu-btn-primary");

    add(title, header, grid, delivery, address, phone, btnOrder);
  }
}
