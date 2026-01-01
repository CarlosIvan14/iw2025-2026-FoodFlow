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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
import java.util.Collection;
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

      // Ordenar por fecha descendente
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

    // TOP: número + estado + botones
    var top = new HorizontalLayout();
    top.addClassName("pedido-card-top");
    top.setWidthFull();
    top.setAlignItems(HorizontalLayout.Alignment.CENTER);
    top.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);

    // Izquierda: número y meta
    var leftTop = new Div();
    leftTop.addClassName("pedido-card-left");

    var orderNum = new Span("Pedido #" + order.getId());
    orderNum.addClassName("pedido-number");

    var meta = new Span(buildMeta(order));
    meta.addClassName("pedido-meta");

    leftTop.add(orderNum, meta);

    // Derecha: estado + botones
    var rightTop = new HorizontalLayout();
    rightTop.setSpacing(true);
    rightTop.setAlignItems(HorizontalLayout.Alignment.CENTER);

    var statusBadge = createStatusBadge(order.getStatus());
    rightTop.add(statusBadge);

    // Botón cancelar si está PENDING
    if (order.getStatus() == OrderStatus.PENDING) {
      var btnCancel = new Button("Cancelar Pedido");
      btnCancel.addClassName("pedido-btn-cancel");
      btnCancel.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
      btnCancel.addClickListener(e -> showCancelConfirmation(order));
      rightTop.add(btnCancel);
    }

    top.add(leftTop, rightTop);

    // BODY: items con fotos
    var body = new Div();
    body.addClassName("pedido-card-body");

    var itemsBlock = buildItemsBlock(order);
    body.add(itemsBlock);

    // FOOTER: total
    var footer = new Div();
    footer.addClassName("pedido-card-footer");

    var totalLabel = new Span("Total:");
    totalLabel.addClassName("pedido-total-label");

    var totalAmount = new Span(String.format("€ %.2f", order.getTotal()));
    totalAmount.addClassName("pedido-total-amount");

    footer.add(totalLabel, totalAmount);

    card.add(top, body, footer);
    return card;
  }

  private void showCancelConfirmation(Order order) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Cancelar Pedido");

    var confirmMsg = new Div(new Span("¿Estás seguro de que deseas cancelar el pedido #" + order.getId() + "?"));
    confirmMsg.addClassName("dialog-message");

    var btnConfirm = new Button("Sí, Cancelar", e -> {
      try {
        orderService.updateStatus(order.getId(), OrderStatus.CANCELED);
        Notification.show("Pedido cancelado exitosamente", 3000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        dialog.close();
        refreshOrders();
      } catch (Exception ex) {
        Notification.show("Error al cancelar: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });
    btnConfirm.addThemeVariants(ButtonVariant.LUMO_ERROR);

    var btnCancel = new Button("No, Mantener", e -> dialog.close());
    btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    dialog.add(confirmMsg);
    dialog.getFooter().add(btnCancel, btnConfirm);
    dialog.open();
  }

  private String buildMeta(Order order) {
    String fecha = "—";
    try {
      if (order.getCreatedAt() != null) {
        fecha = order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
      }
    } catch (Exception ignored) {}
    return "Creado: " + fecha;
  }

  private Div buildItemsBlock(Order order) {
    var wrap = new Div();
    wrap.addClassName("pedido-items-block");

    var title = new Span("Artículos");
    title.addClassName("pedido-items-title");
    wrap.add(title);

    Collection<?> items = tryGetItems(order);
    if (items == null || items.isEmpty()) {
      var no = new Span("No hay artículos en este pedido");
      no.addClassName("pedido-items-empty");
      wrap.add(no);
      return wrap;
    }

    var ul = new Div();
    ul.addClassName("pedido-items-list");

    for (Object it : items) {
      ul.add(renderItemLine(it));
    }

    wrap.add(ul);
    return wrap;
  }

  private Div renderItemLine(Object item) {
    var row = new Div();
    row.addClassName("pedido-item-row");

    // Información del item
    String name = firstNonBlank(
        tryInvokeToString(item, "getProductName"),
        tryInvokeToString(item, "getName"),
        "Artículo"
    );

    String qty = firstNonBlank(
        tryInvokeToString(item, "getQuantity"),
        "1"
    );

    String price = firstNonBlank(
        tryInvokeToString(item, "getTotal"),
        "0.00"
    );

    // Contenedor izquierdo con info
    var left = new Div();
    left.addClassName("pedido-item-left");

    var itemName = new Span(name);
    itemName.addClassName("pedido-item-name");

    var itemQty = new Span("x" + qty);
    itemQty.addClassName("pedido-item-qty");

    left.add(itemName, itemQty);

    // Precio a la derecha
    var itemPrice = new Span("€ " + String.format("%.2f", Double.parseDouble(price)));
    itemPrice.addClassName("pedido-item-price");

    row.add(left, itemPrice);
    return row;
  }

  @SuppressWarnings("unchecked")
  private Collection<?> tryGetItems(Order order) {
    Object v;

    v = tryInvoke(order, "getItems");
    if (v instanceof Collection) return (Collection<?>) v;

    v = tryInvoke(order, "getOrderItems");
    if (v instanceof Collection) return (Collection<?>) v;

    v = tryInvoke(order, "getLines");
    if (v instanceof Collection) return (Collection<?>) v;

    return null;
  }

  private Object tryInvoke(Object target, String methodName) {
    try {
      var m = target.getClass().getMethod(methodName);
      return m.invoke(target);
    } catch (Exception ignored) {
      return null;
    }
  }

  private String tryInvokeToString(Object target, String methodName) {
    Object v = tryInvoke(target, methodName);
    return v == null ? null : String.valueOf(v);
  }

  private String firstNonBlank(String... vals) {
    if (vals == null) return "";
    for (String v : vals) {
      if (v != null && !v.trim().isBlank()) return v.trim();
    }
    return "";
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
}
