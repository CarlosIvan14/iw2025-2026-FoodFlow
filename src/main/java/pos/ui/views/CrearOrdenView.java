package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import pos.auth.RouteGuard;
import pos.domain.Product;
import pos.domain.TableSpot;
import pos.ui.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Crear Orden")
@Route(value = "ordenes/crear", layout = MainLayout.class)
public class CrearOrdenView extends VerticalLayout implements RouteGuard {

  private final List<OrderItem> items = new ArrayList<>();

  public CrearOrdenView(TableService tables, MenuService menu, OrderService orders, AuthService auth) {
    addClassName("orden-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);
    getStyle().set("align-items", "center");

    // --- Título ---
    var title = new H2("Crear Orden (Mesero)");
    title.addClassName("orden-title");

    // --- Selección de mesa ---
    var mesa = new ComboBox<TableSpot>("Mesa");
    mesa.setItems(tables.all());
    mesa.setItemLabelGenerator(TableSpot::name);
    mesa.addClassName("orden-combobox");

    // --- Tabla de productos ---
    var grid = new Grid<>(Product.class, false);
    grid.addClassName("orden-grid");
    grid.addColumn(Product::name).setHeader("Producto").setAutoWidth(true);
    grid.addColumn(Product::price).setHeader("Precio").setAutoWidth(true);

    grid.addComponentColumn(p -> {
      var qty = new IntegerField();
      qty.setMin(1);
      qty.setValue(1);
      qty.setWidth("80px");
      qty.addClassName("orden-cantidad");

      var note = new TextField();
      note.setPlaceholder("Nota opcional");
      note.addClassName("orden-nota");

      var add = new Button("Añadir", e -> {
        items.add(new OrderItem(p.id(), p.name(), qty.getValue(), p.price(), note.getValue()));
        Notification.show("Añadido " + p.name());
      });
      add.addClassName("orden-add-btn");

      var hl = new HorizontalLayout(qty, note, add);
      hl.addClassName("orden-add-row");
      return hl;
    });

    grid.setItems(menu.list());

    // --- Botones principales ---
    var btnCrear = new Button("Crear Pedido", e -> {
      if (mesa.getValue() == null) {
        Notification.show("Selecciona mesa");
        return;
      }
      if (items.isEmpty()) {
        Notification.show("Sin productos");
        return;
      }
      var o = orders.createTableOrder(mesa.getValue().id(), items, auth.currentUser());
      Notification.show("Creado pedido #" + o.getId() + " total $" + o.total());
      items.clear();
    });
    btnCrear.addClassName("orden-crear-btn");

    var btnDividir = new Button("Dividir cuenta (50/50 demo)", e -> {
      double total = items.stream().mapToDouble(i -> i.unitPrice() * i.qty()).sum();
      Notification.show("Dos cuentas de: $" + (total / 2.0));
    });
    btnDividir.addClassName("orden-dividir-btn");

    var buttons = new HorizontalLayout(btnCrear, btnDividir);
    buttons.addClassName("orden-buttons");

    add(title, mesa, grid, buttons);
  }
}
