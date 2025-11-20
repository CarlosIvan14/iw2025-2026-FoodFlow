package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.domain.TableSpot;
import pos.ui.MainLayout;
import com.vaadin.flow.component.html.Image;
import pos.service.TableService;
import pos.service.OrderService;
import pos.service.MenuService;
import pos.domain.OrderItem;

import java.util.List;

@PageTitle("Mesas")
@Route(value = "mesas", layout = MainLayout.class)
public class DashboardMesasView extends VerticalLayout implements RouteGuard {

  public DashboardMesasView(TableService tables, OrderService orders) {
    addClassName("mesas-view");
    setSizeFull();

    var title = new H2("Mapa de Mesas");
    title.addClassName("mesas-title");

    var canvas = new Div();
    canvas.addClassName("mesas-canvas");

    add(title, canvas);

    List<TableSpot> all = tables.all();

  

    for (var t : all) {
    var btn = new Button();
    btn.addClassName("mesa-btn");
    btn.getElement().setProperty("innerHTML",
        "<img src='icons/mesa.png' class='mesa-icon'>" +
        "<span class='mesa-label'>" + t.getCode() + "</span>"
    );
    btn.getStyle().set("left", t.getX() + "px");
    btn.getStyle().set("top", t.getY() + "px");
    btn.addClickListener(e -> showOrdersFor(t, orders));
    canvas.add(btn);
    }

  }

  private void showOrdersFor(TableSpot t, OrderService orders) {
    var dialog = new Dialog();
    dialog.setHeaderTitle("Pedidos de " + t.getCode());

    var wrap = new VerticalLayout();
    orders.all().stream()
        .filter(o -> t.getId().equals(o.getTableId()))
        .forEach(o -> wrap.add(orderCard(o, orders)));

    if (wrap.getComponentCount() == 0)
      wrap.add(new Span("Sin pedidos abiertos"));

    dialog.add(wrap);
    dialog.setWidth("600px");
    dialog.open();
  }

  private Div orderCard(Order o, OrderService orders) {
    var card = new Div();
    card.addClassName("pedido-card");
    card.add(new Span("Pedido #" + o.getId() +
        " | Estado: " + o.getStatus() +
        " | Total: $" + o.getTotal()));

    var btnPrep = new Button("Preparando", e -> {
      orders.updateStatus(o.getId(), pos.domain.OrderStatus.IN_PREPARATION);
      card.getElement().callJsFunction("remove");
    });

    var btnCerrar = new Button("Cerrar", e -> {
      orders.updateStatus(o.getId(), pos.domain.OrderStatus.DELIVERED);
      card.getElement().callJsFunction("remove");
    });

    card.add(btnPrep, btnCerrar);
    return card;
  }
}
