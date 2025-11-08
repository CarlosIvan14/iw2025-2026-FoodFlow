package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.service.OrderService;
import pos.ui.MainLayout;

@PageTitle("Cocina")
@Route(value="cocina", layout = MainLayout.class)
public class CocinaView extends VerticalLayout implements RouteGuard {
  public CocinaView(OrderService orders){
    var h = new H2("Pedidos (Cocina)  ordenados por mesa y fecha");
    var grid = new Grid<>(Order.class, false);
    grid.addColumn(o -> o.getId()).setHeader("#");
    grid.addColumn(o -> o.getTableId()==null? "" : o.getTableId()).setHeader("Mesa");
    grid.addColumn(o -> o.getCreatedAt()).setHeader("Creado");
    grid.addColumn(o -> o.getStatus()).setHeader("Estado");
    grid.addColumn(o -> o.total()).setHeader("Total");
    grid.addComponentColumn(o -> new Button("A LISTO", e -> {
      orders.updateStatus(o.getId(), Order.Status.LISTO);
      grid.setItems(orders.kitchenQueue());
    })).setHeader("Acci?n");

    grid.setItems(orders.kitchenQueue());
    add(h, grid);
  }
}
