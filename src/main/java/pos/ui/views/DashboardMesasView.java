package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.domain.TableSpot;
import pos.domain.OrderStatus;
import pos.ui.MainLayout;
import pos.service.TableService;
import pos.service.OrderService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Mesas")
@Route(value = "mesas", layout = MainLayout.class)
@pos.auth.RequiredRoles(pos.domain.Role.MESERO)
public class DashboardMesasView extends VerticalLayout implements RouteGuard {

  private final Div canvas = new Div();

  public DashboardMesasView(TableService tables, OrderService orders) {
    addClassName("mesas-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);

    // Header
    var title = new H2("Mapa de Mesas");
    title.addClassName("mesas-title");

    var addBtn = new Button("Agregar Mesa");
    addBtn.addClassName("mesas-add-btn");
    addBtn.addClickListener(e -> showAddTableDialog(tables));

    var header = new HorizontalLayout(title, addBtn);
    header.addClassName("mesas-header");
    header.setWidthFull();
    header.setAlignItems(Alignment.CENTER);
    header.setJustifyContentMode(JustifyContentMode.BETWEEN);

    // Canvas (GRID)
    canvas.addClassName("mesas-canvas");
    canvas.addClassName("mesas-grid"); // <- selector importante para CSS
    canvas.setWidthFull();

    add(header, canvas);

    refresh(tables, orders);
  }

  private void refresh(TableService tables, OrderService orders) {
    canvas.removeAll();

    List<TableSpot> all = tables.all();
    List<TableSpot> sorted = sortTablesByOrderPriority(all, orders);

    for (var t : sorted) {
      canvas.add(createTableButton(t, orders));
    }
  }

  private List<TableSpot> sortTablesByOrderPriority(List<TableSpot> tables, OrderService orders) {
    return tables.stream()
        .sorted(Comparator.comparingInt(t -> getTablePriority(t, orders)))
        .collect(Collectors.toList());
  }

  private int getTablePriority(TableSpot table, OrderService orders) {
    List<Order> activeOrders = orders.findActiveOrdersByTable(table.getId());
    if (activeOrders.isEmpty()) return 5;

    boolean hasListo = activeOrders.stream().anyMatch(o -> o.getStatus() == OrderStatus.LISTO);
    boolean hasInPreparation = activeOrders.stream().anyMatch(o -> o.getStatus() == OrderStatus.IN_PREPARATION);
    boolean hasPending = activeOrders.stream().anyMatch(o -> o.getStatus() == OrderStatus.PENDING);
    boolean hasPagado = activeOrders.stream().anyMatch(o -> o.getStatus() == OrderStatus.PAGADO);

    if (hasListo) return 0;
    if (hasInPreparation) return 1;
    if (hasPending) return 2;
    if (hasPagado) return 3;
    return 4;
  }

  private Button createTableButton(TableSpot t, OrderService orders) {
    var btn = new Button();
    btn.addClassName("mesa-btn");
    btn.addClassName("mesa-card"); // <- para que el CSS le de formato bonito

    String statusClass = getTableStatusClass(t, orders);
    String statusBadge = getTableStatusBadge(t, orders);
    btn.addClassName(statusClass);

    btn.getElement().setProperty("innerHTML",
        "<div class='mesa-inner'>" +
            "<img src='icons/mesa.png' class='mesa-icon' alt='mesa'/>" +
            "<div class='mesa-texts'>" +
              "<span class='mesa-label'>" + t.getCode() + "</span>" +
              statusBadge +
            "</div>" +
        "</div>"
    );

    btn.addClickListener(e -> showOrdersFor(t, orders));
    return btn;
  }

  private String getTableStatusClass(TableSpot table, OrderService orders) {
    List<Order> activeOrders = orders.findActiveOrdersByTable(table.getId());
    if (activeOrders.isEmpty()) return "mesa-libre";

    boolean hasListo = activeOrders.stream().anyMatch(o -> o.getStatus() == OrderStatus.LISTO);
    boolean hasInPreparation = activeOrders.stream().anyMatch(o -> o.getStatus() == OrderStatus.IN_PREPARATION);
    boolean hasPending = activeOrders.stream().anyMatch(o -> o.getStatus() == OrderStatus.PENDING);
    boolean hasPagado = activeOrders.stream().anyMatch(o -> o.getStatus() == OrderStatus.PAGADO);

    if (hasListo) return "mesa-lista";
    if (hasInPreparation) return "mesa-cocina";
    if (hasPending) return "mesa-pendiente";
    if (hasPagado) return "mesa-pagado";
    return "mesa-ocupada";
  }

  private String getTableStatusBadge(TableSpot table, OrderService orders) {
    List<Order> activeOrders = orders.findActiveOrdersByTable(table.getId());
    if (activeOrders.isEmpty()) return "<span class='mesa-badge badge-libre'>LIBRE</span>";

    long listoCount = activeOrders.stream().filter(o -> o.getStatus() == OrderStatus.LISTO).count();
    long preparationCount = activeOrders.stream().filter(o -> o.getStatus() == OrderStatus.IN_PREPARATION).count();
    long pendingCount = activeOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
    long pagadoCount = activeOrders.stream().filter(o -> o.getStatus() == OrderStatus.PAGADO).count();

    String badgeText = "";
    String badgeClass = "";

    if (listoCount > 0) { badgeText = "✓ LISTO"; badgeClass = "badge-listo"; }
    else if (preparationCount > 0) { badgeText = "🍳 COCINA"; badgeClass = "badge-cocina"; }
    else if (pendingCount > 0) { badgeText = "⏱ PENDIENTE"; badgeClass = "badge-pendiente"; }
    else if (pagadoCount > 0) { badgeText = "💳 PAGADO"; badgeClass = "badge-pagado"; }
    else { badgeText = "OCUPADA"; badgeClass = "badge-ocupada"; }

    return "<span class='mesa-badge " + badgeClass + "'>" + badgeText + "</span>";
  }

  private void showAddTableDialog(TableService tables) {
    var dialog = new Dialog();
    dialog.setHeaderTitle("Agregar Nueva Mesa");

    var codeField = new com.vaadin.flow.component.textfield.TextField("Código (ej. M1)");
    var capacityField = new com.vaadin.flow.component.textfield.IntegerField("Capacidad");
    capacityField.setValue(4);

    var saveBtn = new Button("Guardar", e -> {
      if (codeField.isEmpty() || capacityField.isEmpty()) return;

      var t = pos.domain.TableSpot.builder()
          .code(codeField.getValue())
          .capacity(capacityField.getValue())
          .x(0)
          .y(0)
          .state(pos.domain.TableState.FREE)
          .build();

      tables.save(t);
      dialog.close();
      com.vaadin.flow.component.UI.getCurrent().getPage().reload();
    });

    saveBtn.addClassName("mesas-save-btn");
    var layout = new VerticalLayout(codeField, capacityField, saveBtn);
    dialog.add(layout);
    dialog.open();
  }

  private void showOrdersFor(TableSpot t, OrderService orders) {
    var dialog = new Dialog();
    dialog.setHeaderTitle("Pedidos de " + t.getCode());

    var wrap = new VerticalLayout();
    wrap.setPadding(false);
    wrap.setSpacing(true);
    wrap.setWidthFull();

    var activeOrders = orders.findActiveOrdersByTable(t.getId());

    if (activeOrders.isEmpty()) {
      wrap.add(new Span("Sin pedidos abiertos"));
    } else {
      for (Order o : activeOrders) {
        wrap.add(orderCard(o));
      }
    }

    dialog.add(wrap);
    dialog.setWidth("720px");
    dialog.open();
  }

  private Div orderCard(Order o) {
    var card = new Div();
    card.addClassName("pedido-card");

    String statusEmoji = getStatusEmoji(o.getStatus());

    card.add(new Span(
        statusEmoji + " Pedido #" + o.getId() +
        " | Estado: " + getStatusText(o.getStatus()) +
        " | Total: €" + String.format("%.2f", o.getTotal())
    ));

    return card;
  }

  private String getStatusEmoji(OrderStatus status) {
    switch (status) {
      case PENDING: return "⏱";
      case IN_PREPARATION: return "🍳";
      case LISTO: return "✓";
      case PAGADO: return "💳";
      case ON_THE_WAY: return "🚗";
      case DELIVERED: return "✔";
      case CANCELED: return "✗";
      default: return "•";
    }
  }

  private String getStatusText(OrderStatus status) {
    switch (status) {
      case PENDING: return "Pendiente";
      case IN_PREPARATION: return "En Preparación";
      case LISTO: return "Listo";
      case PAGADO: return "Pagado";
      case ON_THE_WAY: return "En Camino";
      case DELIVERED: return "Entregado";
      case CANCELED: return "Cancelado";
      default: return status.toString();
    }
  }
}
