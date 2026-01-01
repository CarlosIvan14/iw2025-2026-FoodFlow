package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.domain.OrderStatus;
import pos.ui.MainLayout;
import pos.service.OrderService;

import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

@PageTitle("Cocina")
@Route(value = "cocina", layout = MainLayout.class)
@pos.auth.RequiredRoles(pos.domain.Role.COCINERO)
@CssImport("./styles/cocina.css")
public class CocinaView extends VerticalLayout implements RouteGuard {

  private final Div list = new Div();

  public CocinaView(OrderService orders) {
    addClassName("cocina-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);

    var title = new H2("Cocina");
    title.addClassName("cocina-title");

    list.addClassName("cocina-list");
    list.setWidthFull();

    add(title, list);

    refresh(orders);
  }

  private void refresh(OrderService orders) {
    list.removeAll();

    var queue = orders.kitchenQueueWithItems();
    if (queue == null || queue.isEmpty()) {
      var empty = new Div(new Span("No hay pedidos en cola."));
      empty.addClassName("cocina-empty");
      list.add(empty);
      return;
    }

    for (Order o : queue) {
      // Mostrar solo PENDING e IN_PREPARATION en cocina
      if (o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.IN_PREPARATION) {
        list.add(orderRow(o, orders));
      }
    }
  }

  private Div orderRow(Order o, OrderService orders) {
    var card = new Div();
    card.addClassName("cocina-card");

    // TOP: título + botones
    var leftTop = new Div();
    leftTop.addClassName("cocina-card-top-left");

    var id = new Span("Pedido #" + safe(o.getId()));
    id.addClassName("cocina-order-id");

    var meta = new Span(buildMeta(o));
    meta.addClassName("cocina-meta");

    leftTop.add(id, meta);

    var buttonsContainer = new HorizontalLayout();
    buttonsContainer.setSpacing(true);
    buttonsContainer.setAlignItems(Alignment.CENTER);

    // Botón "En Preparación" solo si está PENDING
    if (o.getStatus() == OrderStatus.PENDING) {
      var btnPrepare = new Button("En Preparación");
      btnPrepare.addClassName("cocina-btn-prep");
      btnPrepare.addClassName("cocina-btn-cafe");
      btnPrepare.addClickListener(e -> {
        try {
          orders.updateStatus(o.getId(), OrderStatus.IN_PREPARATION);
          Notification.show("Orden en preparación", 2500, Notification.Position.TOP_CENTER)
              .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
          refresh(orders);
        } catch (Exception ex) {
          Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
              .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
      });
      buttonsContainer.add(btnPrepare);
    }

    // Botón "Listo" si está EN_PREPARATION
    if (o.getStatus() == OrderStatus.IN_PREPARATION) {
      var btnListo = new Button("LISTO");
      btnListo.addClassName("cocina-btn-big");
      btnListo.addClassName("cocina-btn-verde");
      btnListo.addClickListener(e -> {
        try {
          orders.updateStatus(o.getId(), OrderStatus.LISTO);
          Notification.show("Orden enviada a Caja", 2500, Notification.Position.TOP_CENTER)
              .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
          refresh(orders);
        } catch (Exception ex) {
          Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
              .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
      });
      buttonsContainer.add(btnListo);
    }

    var top = new HorizontalLayout(leftTop, buttonsContainer);
    top.addClassName("cocina-card-top");
    top.setWidthFull();
    top.setAlignItems(Alignment.CENTER);
    top.setJustifyContentMode(JustifyContentMode.BETWEEN);

    // BODY: items + notas
    var body = new Div();
    body.addClassName("cocina-card-body");

    var itemsBlock = buildItemsBlock(o);
    body.add(itemsBlock);

    // Footer: total / estado (grande y claro)
    var footer = new Div();
    footer.addClassName("cocina-card-footer");

    var status = new Span("Estado: " + safe(o.getStatus()));
    status.addClassName("cocina-status");

    var total = new Span("Total: €" + String.format("%.2f", o.getTotal()));
    total.addClassName("cocina-total");

    footer.add(status, total);

    card.add(top, body, footer);
    return card;
  }

  private String buildMeta(Order o) {
    String mesa = (o.getTableId() == null) ? "—" : String.valueOf(o.getTableId());
    String fecha = "—";
    try {
      if (o.getCreatedAt() != null) {
        fecha = o.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
      }
    } catch (Exception ignored) {}

    return "Mesa: " + mesa + " · Creado: " + fecha;
  }

  private Div buildItemsBlock(Order o) {
    var wrap = new Div();
    wrap.addClassName("cocina-items");

    var title = new Span("A preparar");
    title.addClassName("cocina-items-title");
    wrap.add(title);

    Collection<?> items = tryGetItems(o);
    if (items == null || items.isEmpty()) {
      var no = new Span("No se pudieron cargar los items del pedido (verifica que Order exponga getItems()).");
      no.addClassName("cocina-items-empty");
      wrap.add(no);
      return wrap;
    }

    var ul = new Div();
    ul.addClassName("cocina-items-list");

    for (Object it : items) {
      ul.add(renderItemLine(it));
    }

    wrap.add(ul);
    return wrap;
  }

  private Div renderItemLine(Object item) {
    var row = new Div();
    row.addClassName("cocina-item-row");

    String name = firstNonBlank(
        tryInvokeToString(item, "getProductName"),
        tryInvokeToString(item, "getName"),
        tryInvokeToString(item, "getTitle"),
        "Item"
    );

    String qty = firstNonBlank(
        tryInvokeToString(item, "getQuantity"),
        tryInvokeToString(item, "getQty"),
        "1"
    );

    String note = firstNonBlank(
        tryInvokeToString(item, "getComment"),
        tryInvokeToString(item, "getNote"),
        tryInvokeToString(item, "getNotes"),
        ""
    );

    var left = new Div(new Span(name), new Span("x" + qty));
    left.addClassName("cocina-item-left");

    row.add(left);

    if (note != null && !note.isBlank()) {
      var n = new Div(new Span("Nota: " + note));
      n.addClassName("cocina-item-note");
      row.add(n);
    }

    return row;
  }

  @SuppressWarnings("unchecked")
  private Collection<?> tryGetItems(Order o) {
    Object v;

    v = tryInvoke(o, "getItems");
    if (v instanceof Collection) return (Collection<?>) v;

    v = tryInvoke(o, "getOrderItems");
    if (v instanceof Collection) return (Collection<?>) v;

    v = tryInvoke(o, "getLines");
    if (v instanceof Collection) return (Collection<?>) v;

    return null;
  }

  private Object tryInvoke(Object target, String methodName) {
    try {
      Method m = target.getClass().getMethod(methodName);
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

  private String safe(Object v) {
    return v == null ? "—" : String.valueOf(v);
  }
}
