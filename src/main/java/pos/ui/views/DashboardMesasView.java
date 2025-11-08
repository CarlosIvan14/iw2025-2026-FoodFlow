package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.domain.TableSpot;
import pos.service.OrderService;
import pos.service.TableService;
import pos.ui.MainLayout;

import java.util.List;

@PageTitle("Dashboard Mesas")
@Route(value = "mesas", layout = MainLayout.class)
@CssImport(value = "./styles/mesas.css")
public class DashboardMesasView extends VerticalLayout implements RouteGuard {

  public DashboardMesasView(TableService tables, OrderService orders) {
    setSizeFull();
    var h = new H2("Mapa de Mesas");

    // Lienzo relativo para posicionar hijos en absoluto
    var canvas = new Div();
    canvas.getStyle().set("position", "relative");
    canvas.setWidth("500px");
    canvas.setHeight("300px");
    canvas.getStyle().set("background", "var(--lumo-base-color)");

    add(h, canvas);

    List<TableSpot> all = tables.all();
    for (var t : all) {
      var btn = new Button(t.name());
      btn.getStyle().set("position", "absolute");
      btn.getStyle().set("left", t.x() + "px");
      btn.getStyle().set("top", t.y() + "px");
      btn.addClickListener(e -> showOrdersFor(t, orders));
      canvas.add(btn);
    }
  }

  private void showOrdersFor(TableSpot t, OrderService orders) {
    var dialog = new com.vaadin.flow.component.dialog.Dialog();
    dialog.setHeaderTitle("Pedidos de " + t.name());
    var wrap = new VerticalLayout();
    orders.all().stream().filter(o -> t.id().equals(o.getTableId()))
        .forEach(o -> wrap.add(orderCard(o, orders)));
    if (wrap.getComponentCount() == 0) wrap.add(new Span("Sin pedidos abiertos"));
    dialog.add(wrap);
    dialog.setWidth("600px");
    dialog.open();
  }

  private Div orderCard(Order o, OrderService orders) {
    var card = new Div();
    card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
    card.getStyle().set("padding", "10px");
    card.getStyle().set("margin", "8px");
    card.getStyle().set("border-radius", "12px");
    card.add(new Span("Pedido #" + o.getId() + " | Estado: " + o.getStatus() + " | Total: $" + o.total()));

    var btnPrep = new Button("Preparando", e -> {
      orders.updateStatus(o.getId(), Order.Status.PREPARANDO);
      card.getElement().callJsFunction("remove");
    });
    var btnCerrar = new Button("Cerrar", e -> {
      orders.updateStatus(o.getId(), Order.Status.CERRADO);
      card.getElement().callJsFunction("remove");
    });
    var actions = new VerticalLayout(btnPrep, btnCerrar);
    actions.setPadding(false); actions.setSpacing(true);
    card.add(actions);
    return card;
  }
}
