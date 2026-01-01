package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.AuthService;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.domain.OrderItem;
import pos.domain.OrderStatus;
import pos.service.OrderService;
import pos.ui.MainLayout;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@PageTitle("Mis Pedidos")
@Route(value = "pedidos", layout = MainLayout.class)
@CssImport("./styles/cliente-pedidos.css")
@pos.auth.RequiredRoles(pos.domain.Role.CLIENTE)
public class ClientePedidosView extends VerticalLayout implements RouteGuard {

  private final OrderService orderService;
  private final AuthService authService;
  private final Div ordersList = new Div();

  public ClientePedidosView(OrderService orderService, AuthService authService) {
    this.orderService = orderService;
    this.authService = authService;

    addClassName("cliente-pedidos-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);

    // Header
    var header = new Div();
    header.addClassName("pedidos-header");

    var title = new H2("Mis Pedidos");
    title.addClassName("pedidos-title");

    var refreshBtn = new Button(new Icon(VaadinIcon.REFRESH));
    refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    refreshBtn.setAriaLabel("Actualizar pedidos");
    refreshBtn.addClickListener(e -> refreshOrders());

    header.add(title, refreshBtn);
    header.getStyle().set("display", "flex").set("justify-content", "space-between").set("align-items", "center");

    // Lista de pedidos
    ordersList.addClassName("pedidos-list");

    var scroller = new Scroller(ordersList);
    scroller.addClassName("pedidos-scroller");
    scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

    add(header, scroller);
    setFlexGrow(1, scroller);

    // Cargar pedidos del cliente
    refreshOrders();
  }

  private void refreshOrders() {
    ordersList.removeAll();

    try {
      Long clientId = authService.currentUserId();
      List<Order> clientOrders = orderService.findOrdersByUserIdWithItems(clientId.toString());

      if (clientOrders == null || clientOrders.isEmpty()) {
        var empty = new Div(new Span("No tienes pedidos aún."));
        empty.addClassName("pedidos-empty");
        ordersList.add(empty);
        return;
      }

      // Convertir a lista mutable y ordenar
      java.util.List<Order> mutableOrders = new java.util.ArrayList<>(clientOrders);
      mutableOrders.sort((o1, o2) -> {
        if (o1.getCreatedAt() == null && o2.getCreatedAt() == null) return 0;
        if (o1.getCreatedAt() == null) return 1;
        if (o2.getCreatedAt() == null) return -1;
        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
      });

      for (Order order : mutableOrders) {
        ordersList.add(buildOrderCard(order));
      }

    } catch (Exception e) {
      var error = new Div(new Span("Error al cargar pedidos: " + e.getMessage()));
      error.addClassName("pedidos-error");
      ordersList.add(error);
      e.printStackTrace();
    }
  }

  private com.vaadin.flow.component.Component buildOrderCard(Order order) {
    var card = new Div();
    card.addClassName("pedido-card");

    // Encabezado: número y estado
    var headerDiv = new Div();
    headerDiv.addClassName("pedido-card-header");

    var orderNum = new Span("Pedido #" + order.getId());
    orderNum.addClassName("pedido-number");

    var statusBadge = createStatusBadge(order.getStatus());

    var headerContent = new Div(orderNum, statusBadge);
    headerContent.getStyle().set("display", "flex").set("justify-content", "space-between").set("align-items", "center");

    headerDiv.add(headerContent);

    // Fecha y hora
    var dateDiv = new Div();
    dateDiv.addClassName("pedido-date");

    String dateStr = "Fecha desconocida";
    if (order.getCreatedAt() != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      dateStr = order.getCreatedAt().format(formatter);
    }

    dateDiv.add(new Span(dateStr));

    // Items del pedido
    var itemsDiv = new Div();
    itemsDiv.addClassName("pedido-items");

    try {
      var items = getOrderItems(order);
      if (items != null && !items.isEmpty()) {
        for (OrderItem item : items) {
          var itemRow = new Div();
          itemRow.addClassName("pedido-item-row");

          var itemName = new Span(Optional.ofNullable(item.getProductName()).orElse("Producto"));
          itemName.addClassName("pedido-item-name");

          var itemQty = new Span("x" + item.getQuantity());
          itemQty.addClassName("pedido-item-qty");

          var itemPrice = new Span(String.format("€ %.2f", item.getTotal()));
          itemPrice.addClassName("pedido-item-price");

          itemRow.add(itemName, itemQty, itemPrice);
          itemsDiv.add(itemRow);
        }
      }
    } catch (Exception e) {
      System.err.println("Error reading order items: " + e.getMessage());
    }

    // Total
    var totalDiv = new Div();
    totalDiv.addClassName("pedido-total");

    var totalLabel = new Span("Total:");
    totalLabel.addClassName("pedido-total-label");

    var totalAmount = new Span(String.format("€ %.2f", order.getTotal()));
    totalAmount.addClassName("pedido-total-amount");

    totalDiv.add(totalLabel, totalAmount);

    // Contenedor principal del card
    var content = new VerticalLayout(headerDiv, dateDiv, itemsDiv, totalDiv);
    content.setPadding(false);
    content.setSpacing(true);
    content.addClassName("pedido-card-content");

    card.add(content);
    return card;
  }

  private com.vaadin.flow.component.Component createStatusBadge(OrderStatus status) {
    var badge = new Span();
    badge.addClassName("status-badge");

    if (status == null) {
      badge.setText("Desconocido");
      badge.addClassName("status-unknown");
    } else {
      switch (status) {
        case PENDING:
          badge.setText("⏱ Pendiente");
          badge.addClassName("status-pending");
          break;
        case IN_PREPARATION:
          badge.setText("🍳 En Preparación");
          badge.addClassName("status-in-preparation");
          break;
        case LISTO:
          badge.setText("✓ Listo");
          badge.addClassName("status-listo");
          break;
        case PAGADO:
          badge.setText("💳 Pagado");
          badge.addClassName("status-pagado");
          break;
        case CANCELED:
          badge.setText("✕ Cancelado");
          badge.addClassName("status-canceled");
          break;
        default:
          badge.setText(status.name());
      }
    }

    return badge;
  }

  @SuppressWarnings("unchecked")
  private java.util.List<OrderItem> getOrderItems(Order order) {
    try {
      var m = order.getClass().getMethod("getItems");
      return (java.util.List<OrderItem>) m.invoke(order);
    } catch (Exception e1) {
      try {
        var m = order.getClass().getMethod("getOrderItems");
        return (java.util.List<OrderItem>) m.invoke(order);
      } catch (Exception e2) {
        try {
          var m = order.getClass().getMethod("getLines");
          return (java.util.List<OrderItem>) m.invoke(order);
        } catch (Exception e3) {
          return null;
        }
      }
    }
  }
}
