package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.ui.MainLayout;
import pos.service.OrderService;

@PageTitle("Cocina")
@Route(value = "cocina", layout = MainLayout.class)
public class CocinaView extends VerticalLayout implements RouteGuard {

  public CocinaView(OrderService orders) {
    addClassName("cocina-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.START);

    var title = new H2("Cocina");
    title.addClassName("cocina-title");

    var grid = new Grid<>(Order.class, false);
    grid.addClassName("cocina-grid");
    grid.addColumn(Order::getId).setHeader("#");
    grid.addColumn(o -> o.getTableId() == null ? "" : o.getTableId()).setHeader("Mesa");
    grid.addColumn(Order::getCreatedAt).setHeader("Creado");
    grid.addColumn(Order::getStatus).setHeader("Estado");
    grid.addColumn(Order::getTotal).setHeader("Total");

    grid.addComponentColumn(o -> {
      var btn = new Button("LISTO", e -> {
        orders.updateStatus(o.getId(), pos.domain.OrderStatus.LISTO);

        grid.setItems(orders.kitchenQueue());

        // Feedback opcional
        com.vaadin.flow.component.notification.Notification.show("Orden enviada a Caja");
      });
      btn.addClassName("cocina-btn");
      return btn;
    }).setHeader("Acción");

    grid.setItems(orders.kitchenQueue());
    add(title, grid);
  }
}
