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
import pos.domain.OrderItem;
import pos.domain.Product;
import pos.domain.TableSpot;
import pos.service.MenuService;
import pos.service.OrderService;
import pos.service.TableService;
import pos.ui.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Crear Orden")
@Route(value="ordenes/nueva", layout = MainLayout.class)
public class CrearOrdenView extends VerticalLayout implements RouteGuard {
  private final List<OrderItem> items = new ArrayList<>();

  public CrearOrdenView(TableService tables, MenuService menu, OrderService orders, AuthService auth){
    var h = new H2("Crear Orden (Mesero)");
    var mesa = new ComboBox<TableSpot>("Mesa");
    mesa.setItems(tables.all());
    mesa.setItemLabelGenerator(TableSpot::name);

    var grid = new Grid<>(Product.class, false);
    grid.addColumn(Product::name).setHeader("Producto");
    grid.addColumn(Product::price).setHeader("Precio");
    grid.addComponentColumn(p -> {
      var qty = new IntegerField(); qty.setMin(1); qty.setValue(1); qty.setWidth("90px");
      var note = new TextField(); note.setPlaceholder("Nota opcional");
      var add = new Button("Añadir", e -> {
        items.add(new OrderItem(p.id(), p.name(), qty.getValue(), p.price(), note.getValue()));
        Notification.show("Añadido "+p.name());
      });
      return new HorizontalLayout(qty, note, add);
    });
    grid.setItems(menu.list());

    var btnCrear = new Button("Crear Pedido", e -> {
      if (mesa.getValue()==null){ Notification.show("Selecciona mesa"); return; }
      if (items.isEmpty()){ Notification.show("Sin productos"); return; }
      var o = orders.createTableOrder(mesa.getValue().id(), items, auth.currentUser());
      Notification.show("Creado pedido #"+o.getId()+" total $"+o.total());
      items.clear();
    });

    var btnDividir = new Button("Dividir cuenta (50/50 demo)", e -> {
      double total = items.stream().mapToDouble(i -> i.unitPrice()*i.qty()).sum();
      Notification.show("Dos cuentas de: $"+(total/2.0));
    });

    add(h, mesa, grid, new HorizontalLayout(btnCrear, btnDividir));
  }
}
