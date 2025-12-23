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
import pos.domain.OrderStatus;
import pos.ui.MainLayout;
import pos.service.TableService;
import pos.service.OrderService;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@PageTitle("Mesas")
@Route(value = "mesas", layout = MainLayout.class)
public class DashboardMesasView extends VerticalLayout implements RouteGuard {

  public DashboardMesasView(TableService tables, OrderService orders) {
    addClassName("mesas-view");
    setSizeFull();

    var title = new H2("Mapa de Mesas");
    title.addClassName("mesas-title");

    var addBtn = new Button("Agregar Mesa");
    addBtn.addClickListener(e -> showAddTableDialog(tables));

    var header = new Div(title, addBtn);
    header.addClassName("mesas-header");
    header.getStyle().set("display", "flex");
    header.getStyle().set("justify-content", "space-between");
    header.getStyle().set("align-items", "center");
    header.getStyle().set("width", "100%");

    var canvas = new Div();
    canvas.addClassName("mesas-canvas");

    add(header, canvas);

    // Obtener todas las mesas y ordenarlas por prioridad de estado
    List<TableSpot> all = tables.all();
    List<TableSpot> sortedTables = sortTablesByOrderPriority(all, orders);

    // Distribuir mesas en grid automáticamente
    int index = 0;
    int cols = 5; // 5 mesas por fila
    int spacing = 120; // Espacio entre mesas
    int startX = 50;
    int startY = 50;

    for (var t : sortedTables) {
      int row = index / cols;
      int col = index % cols;
      int x = startX + (col * spacing);
      int y = startY + (row * spacing);

      var btn = createTableButton(t, orders);
      // Sobrescribir posición solo visualmente
      btn.getStyle().set("left", x + "px");
      btn.getStyle().set("top", y + "px");
      canvas.add(btn);
      index++;
    }
  }

  private List<TableSpot> sortTablesByOrderPriority(List<TableSpot> tables, OrderService orders) {
    return tables.stream()
            .sorted(Comparator.comparingInt(t -> getTablePriority(t, orders)))
            .collect(Collectors.toList());
  }

  private int getTablePriority(TableSpot table, OrderService orders) {
    List<Order> activeOrders = orders.findActiveOrdersByTable(table.getId());

    if (activeOrders.isEmpty()) {
      return 5; // Sin pedidos - menor prioridad
    }

    // Determinar el estado más urgente (menor número = mayor prioridad)
    boolean hasListo = activeOrders.stream()
            .anyMatch(o -> o.getStatus() == OrderStatus.LISTO);
    boolean hasInPreparation = activeOrders.stream()
            .anyMatch(o -> o.getStatus() == OrderStatus.IN_PREPARATION);
    boolean hasPending = activeOrders.stream()
            .anyMatch(o -> o.getStatus() == OrderStatus.PENDING);
    boolean hasPagado = activeOrders.stream()
            .anyMatch(o -> o.getStatus() == OrderStatus.PAGADO);

    if (hasListo) return 0;           // LISTO (comida lista, esperando pago) - MÁXIMA PRIORIDAD
    if (hasInPreparation) return 1;   // EN PREPARACIÓN (en cocina)
    if (hasPending) return 2;         // PENDIENTE (recién ordenado)
    if (hasPagado) return 3;          // PAGADO (esperando que cliente se vaya)

    return 4; // Otros estados (DELIVERED, CANCELED, ON_THE_WAY)
  }

  private Button createTableButton(TableSpot t, OrderService orders) {
    var btn = new Button();
    btn.addClassName("mesa-btn");

    // Determinar estado y aplicar clase CSS
    String statusClass = getTableStatusClass(t, orders);
    String statusBadge = getTableStatusBadge(t, orders);

    btn.addClassName(statusClass);

    btn.getElement().setProperty("innerHTML",
            "<img src='icons/mesa.png' class='mesa-icon'>" +
                    "<span class='mesa-label'>" + t.getCode() + "</span>" +
                    statusBadge
    );

    // NO establecemos left/top aquí, se hace en el constructor
    btn.addClickListener(e -> showOrdersFor(t, orders));

    return btn;
  }

  private String getTableStatusClass(TableSpot table, OrderService orders) {
    List<Order> activeOrders = orders.findActiveOrdersByTable(table.getId());

    if (activeOrders.isEmpty()) {
      return "mesa-libre";
    }

    // Determinar el estado más urgente
    boolean hasListo = activeOrders.stream()
            .anyMatch(o -> o.getStatus() == OrderStatus.LISTO);
    boolean hasInPreparation = activeOrders.stream()
            .anyMatch(o -> o.getStatus() == OrderStatus.IN_PREPARATION);
    boolean hasPending = activeOrders.stream()
            .anyMatch(o -> o.getStatus() == OrderStatus.PENDING);
    boolean hasPagado = activeOrders.stream()
            .anyMatch(o -> o.getStatus() == OrderStatus.PAGADO);

    if (hasListo) return "mesa-lista";              // Verde pulsante
    if (hasInPreparation) return "mesa-cocina";     // Azul pulsante
    if (hasPending) return "mesa-pendiente";        // Naranja pulsante
    if (hasPagado) return "mesa-pagado";            // Púrpura

    return "mesa-ocupada"; // Default para otros estados
  }

  private String getTableStatusBadge(TableSpot table, OrderService orders) {
    List<Order> activeOrders = orders.findActiveOrdersByTable(table.getId());

    if (activeOrders.isEmpty()) {
      return "";
    }

    // Contar pedidos por estado
    long listoCount = activeOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.LISTO).count();
    long preparationCount = activeOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.IN_PREPARATION).count();
    long pendingCount = activeOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.PENDING).count();
    long pagadoCount = activeOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.PAGADO).count();

    String badgeText = "";
    String badgeClass = "";

    if (listoCount > 0) {
      badgeText = "✓ LISTO";
      badgeClass = "badge-listo";
    } else if (preparationCount > 0) {
      badgeText = "🍳 COCINA";
      badgeClass = "badge-cocina";
    } else if (pendingCount > 0) {
      badgeText = "⏱ PENDIENTE";
      badgeClass = "badge-pendiente";
    } else if (pagadoCount > 0) {
      badgeText = "💳 PAGADO";
      badgeClass = "badge-pagado";
    }

    if (badgeText.isEmpty()) {
      return "";
    }

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
              .x(50)
              .y(50)
              .state(pos.domain.TableState.FREE)
              .build();

      tables.save(t);
      dialog.close();
      com.vaadin.flow.component.UI.getCurrent().getPage().reload();
    });

    var layout = new VerticalLayout(codeField, capacityField, saveBtn);
    dialog.add(layout);
    dialog.open();
  }

  private void showOrdersFor(TableSpot t, OrderService orders) {
    var dialog = new Dialog();
    dialog.setHeaderTitle("Pedidos de " + t.getCode());

    var wrap = new VerticalLayout();

    var activeOrders = orders.findActiveOrdersByTable(t.getId());  // ← CORREGIDO: era "table.getId()"

    for (Order o : activeOrders) {
      wrap.add(orderCard(o));
    }

    if (activeOrders.isEmpty()) {
      wrap.add(new Span("Sin pedidos abiertos"));
    }

    dialog.add(wrap);
    dialog.setWidth("600px");
    dialog.open();
  }

  private Div orderCard(Order o) {
    var card = new Div();
    card.addClassName("pedido-card");

    String statusEmoji = getStatusEmoji(o.getStatus());

    card.add(new Span(statusEmoji + " Pedido #" + o.getId() +
            " | Estado: " + getStatusText(o.getStatus()) +
            " | Total: $" + String.format("%.2f", o.getTotal())));

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