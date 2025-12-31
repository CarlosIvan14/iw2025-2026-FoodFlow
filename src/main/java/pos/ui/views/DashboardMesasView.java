package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.auth.AuthService;
import pos.domain.Order;
import pos.domain.OrderStatus;
import pos.domain.TableSpot;
import pos.service.OrderService;
import pos.service.TableService;
import pos.ui.MainLayout;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Mesas")
@Route(value = "mesas", layout = MainLayout.class)
@pos.auth.RequiredRoles({pos.domain.Role.MESERO, pos.domain.Role.CAJERO, pos.domain.Role.COCINERO})
@CssImport("./styles/mesas.css")
public class DashboardMesasView extends VerticalLayout implements RouteGuard {

  private final Div canvas = new Div();
  private final AuthService authService;

  public DashboardMesasView(TableService tables, OrderService orders, AuthService authService) {
    this.authService = authService;
    addClassName("mesas-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);

    var title = new H2("Mapa de Mesas");
    title.addClassName("mesas-title");

    var header = new HorizontalLayout(title);
    header.addClassName("mesas-header");
    header.setWidthFull();
    header.setAlignItems(Alignment.CENTER);
    header.setJustifyContentMode(JustifyContentMode.BETWEEN);

    // Solo mostrar botón "Agregar Mesa" si el usuario es ADMIN
    try {
      String userRole = authService.currentRole();
      if (userRole != null && userRole.equals(pos.domain.Role.ADMIN.name())) {
        var addBtn = new Button("Agregar Mesa");
        addBtn.addClassName("mesas-add-btn");
        addBtn.addClickListener(e -> showAddTableDialog(tables));
        header.add(addBtn);
      }
    } catch (Exception e) {
      // Si hay error al verificar el rol, no mostrar el botón
      System.err.println("Error checking admin role: " + e.getMessage());
    }

    canvas.addClassName("mesas-canvas");
    canvas.addClassName("mesas-grid");
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
    btn.addClassName("mesa-card");

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

    String badgeText;
    String badgeClass;

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
    dialog.addClassName("pedidos-dialog");

    var closeBtn = new Button(new Icon(VaadinIcon.CLOSE));
    closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
    closeBtn.addClassName("pedidos-dialog-close");
    closeBtn.addClickListener(e -> dialog.close());
    dialog.getHeader().add(closeBtn);

    var wrap = new VerticalLayout();
    wrap.addClassName("pedidos-dialog-content");
    wrap.setPadding(false);
    wrap.setSpacing(false);
    wrap.setWidthFull();
    wrap.setMargin(false);

    var activeOrders = orders.findActiveOrdersByTable(t.getId());

    if (activeOrders.isEmpty()) {
      var emptyDiv = new Div();
      emptyDiv.addClassName("pedidos-empty");
      emptyDiv.add(new Span("Sin pedidos abiertos"));
      wrap.add(emptyDiv);
    } else {
      for (Order o : activeOrders) {
        wrap.add(orderCard(o, orders, dialog, t));
      }
    }

    dialog.add(wrap);
    dialog.setWidth("560px");      // 👈 fijo para que no se vea “gigante”
    dialog.setMaxWidth("92vw");    // responsive
    dialog.setModal(true);
    dialog.setCloseOnOutsideClick(true);
    dialog.open();
  }

  private Div orderCard(Order o, OrderService orders, Dialog parentDialog, TableSpot t) {
    var card = new Div();
    card.addClassName("pedido-card");

    String statusEmoji = getStatusEmoji(o.getStatus());

    var header = new Div();
    header.addClassName("pedido-card-header");

    var headerTitle = new Span(statusEmoji + " Pedido #" + o.getId());
    headerTitle.addClassName("pedido-card-title");

    var headerInfo = new Span(
        "Estado: " + getStatusText(o.getStatus()) + " | Total: €" + String.format("%.2f", o.getTotal())
    );
    headerInfo.addClassName("pedido-card-info");

    header.add(headerTitle, headerInfo);
    card.add(header);

    var actions = new Div();
    actions.addClassName("pedido-actions");

    // Ver más (AZUL + icono ojo)
    var viewBtn = new Button("Ver más", new Icon(VaadinIcon.EYE));
    viewBtn.addClassName("pedido-action-btn");
    viewBtn.addClassName("pedido-btn-view");
    viewBtn.getElement().setProperty("title", "Ver detalles");
    viewBtn.addClickListener(e -> showOrderDetails(o, orders, parentDialog));
    actions.add(viewBtn);

    // Editar / Agregar más (VERDE, icon-only)
    Button addMoreBtn = null;
    String userRole = authService.currentRole();
    if ((userRole == null || (!userRole.equals("CAJERO") && !userRole.equals("COCINERO")))
        && (o.getStatus() == OrderStatus.PENDING
            || o.getStatus() == OrderStatus.IN_PREPARATION
            || o.getStatus() == OrderStatus.LISTO)) {

      addMoreBtn = new Button(new Icon(VaadinIcon.EDIT));
      addMoreBtn.addClassName("pedido-action-btn");
      addMoreBtn.addClassName("pedido-btn-add");
      addMoreBtn.addClassName("pedido-btn-icon");
      addMoreBtn.getElement().setProperty("title", "Agregar más al pedido");
      addMoreBtn.addClickListener(e -> {
        com.vaadin.flow.server.VaadinSession.getCurrent().setAttribute("editingOrderId", o.getId());
        com.vaadin.flow.server.VaadinSession.getCurrent().setAttribute("editingTableId", t.getId());
        parentDialog.close();
        com.vaadin.flow.component.UI.getCurrent().navigate("ordenes");
      });
      actions.add(addMoreBtn);
    }

    // Cancelar (solo PENDING)
    if (o.getStatus() == OrderStatus.PENDING) {
      var cancelBtn = new Button("Cancelar", new Icon(VaadinIcon.TRASH));
      cancelBtn.addClassName("pedido-action-btn");
      cancelBtn.addClassName("pedido-btn-cancel");
      cancelBtn.getElement().setProperty("title", "Cancelar pedido");
      cancelBtn.addClickListener(e -> {
        try {
          orders.updateStatus(o.getId(), OrderStatus.CANCELED);
          com.vaadin.flow.component.UI.getCurrent().getPage().reload();
        } catch (Exception ex) {
          com.vaadin.flow.component.notification.Notification.show("Error: " + ex.getMessage(), 3000,
                  com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER)
              .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
        }
      });
      actions.add(cancelBtn);
    }

    // Pagado (solo LISTO)
    if (o.getStatus() == OrderStatus.LISTO
        && (userRole == null || (!userRole.equals("COCINERO")))) {
      var paidBtn = new Button("Pagado", new Icon(VaadinIcon.CREDIT_CARD));
      paidBtn.addClassName("pedido-action-btn");
      paidBtn.addClassName("pedido-btn-paid");
      paidBtn.getElement().setProperty("title", "Marcar como pagado");
      paidBtn.addClickListener(e -> {
        try {
          orders.updateStatus(o.getId(), OrderStatus.PAGADO);
          com.vaadin.flow.component.UI.getCurrent().getPage().reload();
        } catch (Exception ex) {
          com.vaadin.flow.component.notification.Notification.show("Error: " + ex.getMessage(), 3000,
                  com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER)
              .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
        }
      });
      actions.add(paidBtn);
    }

    card.add(actions);
    return card;
  }

  private void showOrderDetails(Order o, OrderService orders, Dialog parentDialog) {
    var detailDialog = new Dialog();
    detailDialog.setHeaderTitle("Detalles de Pedido #" + o.getId());

    var content = new VerticalLayout();
    content.setPadding(true);
    content.setSpacing(true);

    var infoDiv = new Div();
    infoDiv.addClassName("pedido-info");

    var statusText = new Div("Estado: " + getStatusText(o.getStatus()));
    var totalText = new Div("Total: €" + String.format("%.2f", o.getTotal()));
    var createdText = new Div("Creado: " + (o.getCreatedAt() != null ? o.getCreatedAt() : "—"));

    infoDiv.add(statusText, totalText, createdText);
    content.add(infoDiv);

    var itemsTitle = new H2("Items del Pedido");
    content.add(itemsTitle);

    var itemsList = new Div();
    itemsList.addClassName("pedido-items-list");

    try {
      Order fullOrder = orders.getOrderWithItemsInitialized(o.getId());
      var items = tryGetItems(fullOrder);

      if (items == null || items.isEmpty()) {
        itemsList.add(new Span("Sin items en este pedido"));
      } else {
        for (Object item : items) {
          var itemRow = buildOrderItemRow(item);
          itemsList.add(itemRow);
        }
      }
    } catch (Exception e) {
      var errorMsg = new Div();
      errorMsg.addClassName("pedido-items-error");
      errorMsg.add(new Span("No se pudieron cargar los items en este momento."));
      var info = new Div("Total del pedido: €" + String.format("%.2f", o.getTotal()));
      info.getStyle().set("margin-top", "8px");
      info.getStyle().set("font-weight", "bold");
      errorMsg.add(info);
      itemsList.add(errorMsg);
      e.printStackTrace();
    }

    content.add(itemsList);

    var closeBtn = new Button("Cerrar", e -> detailDialog.close());
    closeBtn.addClassName("pedido-detail-close-btn");
    content.add(closeBtn);

    detailDialog.add(content);
    detailDialog.setWidth("600px");
    detailDialog.setMaxHeight("80vh");
    detailDialog.open();
  }

  @SuppressWarnings("unchecked")
  private java.util.Collection<?> tryGetItems(Order o) {
    try {
      var m = o.getClass().getMethod("getItems");
      return (java.util.Collection<?>) m.invoke(o);
    } catch (Exception e1) {
      try {
        var m = o.getClass().getMethod("getOrderItems");
        return (java.util.Collection<?>) m.invoke(o);
      } catch (Exception e2) {
        try {
          var m = o.getClass().getMethod("getLines");
          return (java.util.Collection<?>) m.invoke(o);
        } catch (Exception e3) {
          return null;
        }
      }
    }
  }

  private Div buildOrderItemRow(Object item) {
    var row = new Div();
    row.addClassName("pedido-item-row");

    String name = tryInvokeToString(item, "getProductName");
    if (name == null || name.isBlank()) name = tryInvokeToString(item, "getName");
    if (name == null || name.isBlank()) name = "Item";

    String qty = tryInvokeToString(item, "getQuantity");
    if (qty == null || qty.isBlank()) qty = "1";

    String unitPrice = tryInvokeToString(item, "getUnitPrice");
    if (unitPrice == null || unitPrice.isBlank()) unitPrice = "0.00";

    var nameSpan = new Span(name);
    nameSpan.addClassName("item-name");

    var qtySpan = new Span("x" + qty);
    qtySpan.addClassName("item-qty");

    var priceSpan = new Span("€" + unitPrice);
    priceSpan.addClassName("item-price");

    row.add(nameSpan, qtySpan, priceSpan);
    return row;
  }

  private String tryInvokeToString(Object target, String methodName) {
    try {
      var m = target.getClass().getMethod(methodName);
      Object result = m.invoke(target);
      return result == null ? null : String.valueOf(result);
    } catch (Exception e) {
      return null;
    }
  }

  private String getStatusEmoji(OrderStatus status) {
    return switch (status) {
      case PENDING -> "⏱";
      case IN_PREPARATION -> "🍳";
      case LISTO -> "✓";
      case PAGADO -> "💳";
      case ON_THE_WAY -> "🚗";
      case DELIVERED -> "✔";
      case CANCELED -> "✗";
      default -> "•";
    };
  }

  private String getStatusText(OrderStatus status) {
    return switch (status) {
      case PENDING -> "Pendiente";
      case IN_PREPARATION -> "En Preparación";
      case LISTO -> "Listo";
      case PAGADO -> "Pagado";
      case ON_THE_WAY -> "En Camino";
      case DELIVERED -> "Entregado";
      case CANCELED -> "Cancelado";
      default -> status.toString();
    };
  }
}
