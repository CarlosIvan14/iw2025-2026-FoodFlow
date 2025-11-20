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
        "<span class='mesa-label'>" + t.name() + "</span>"
    );
    btn.getStyle().set("left", t.x() + "px");
    btn.getStyle().set("top", t.y() + "px");
    btn.addClickListener(e -> showOrdersFor(t, orders));
    canvas.add(btn);
    }

  }

  private void showOrdersFor(TableSpot t, OrderService orders) {
    var dialog = new Dialog();
    dialog.setHeaderTitle("Pedidos de " + t.name());

    var wrap = new VerticalLayout();
    orders.all().stream()
        .filter(o -> t.id().equals(o.getTableId()))
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
        " | Total: $" + o.total()));

    var btnPrep = new Button("Preparando", e -> {
      orders.updateStatus(o.getId(), Order.Status.PREPARANDO);
      card.getElement().callJsFunction("remove");
    });

    var btnCerrar = new Button("Cerrar", e -> {
      orders.updateStatus(o.getId(), Order.Status.CERRADO);
      card.getElement().callJsFunction("remove");
    });

    card.add(btnPrep, btnCerrar);
    return card;
  }
}
