package pos.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import pos.auth.AuthService;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.domain.OrderItem;
import pos.domain.OrderStatus;
import pos.domain.Product;
import pos.domain.TableSpot;
import pos.service.MenuService;
import pos.service.OrderService;
import pos.service.TableService;
import pos.ui.MainLayout;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Crear Orden")
@Route(value = "ordenes", layout = MainLayout.class)
@pos.auth.RequiredRoles(pos.domain.Role.MESERO)
public class CrearOrdenView extends VerticalLayout implements RouteGuard {

  // Carrito en memoria
  private final List<OrderItem> items = new ArrayList<>();

  // Estado UI
  private TableSpot selectedTable = null;

  // UI refs para refrescar
  private final Div tableCanvas = new Div();
  private final Div productsList = new Div();
  private final Div cartList = new Div();
  private final Span cartTotal = new Span("€ 0.00");
  private final Span selectedTableLabel = new Span("Sin mesa seleccionada");

  // Para resaltar la mesa seleccionada
  private final Map<Long, Button> tableButtonsById = new HashMap<>();

  public CrearOrdenView(TableService tables, MenuService menu, OrderService orders, AuthService auth) {
    addClassName("ue-order-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);

    // Recuperar carrito desde sesión si existe (flujo cliente desde MenuView)
    var sessionCart = (List<OrderItem>) VaadinSession.getCurrent().getAttribute("clientCart");
    if (sessionCart != null && !sessionCart.isEmpty()) {
      items.addAll(sessionCart);
      VaadinSession.getCurrent().setAttribute("clientCart", null);
    }

    // Header
    var header = buildHeader();

    // Layout principal
    var content = new HorizontalLayout();
    content.addClassName("ue-order-content");
    content.setSizeFull();
    content.setSpacing(true);

    // Columna izquierda: mesas + productos
    var left = new VerticalLayout();
    left.addClassName("ue-left");
    left.setPadding(false);
    left.setSpacing(true);
    left.setSizeFull();

    var tablesSection = buildTablesSection(tables, orders);
    var productsSection = buildProductsSection(menu);

    left.add(tablesSection, productsSection);

    // Columna derecha: carrito (más angosto por CSS)
    var right = buildCartSection(orders, auth);

    content.add(left, right);
    content.setFlexGrow(1, left);
    content.setFlexGrow(0, right);

    add(header, content);

    // Primer render
    refreshTableMap(tables, orders);
    refreshProducts(menu.list());
    refreshCart();
  }

  /* =========================
     HEADER
     ========================= */
  private Component buildHeader() {
    var title = new H2("Crear Pedido");
    title.addClassName("ue-title");

    var hint = new Span("Selecciona una mesa y agrega productos al carrito.");
    hint.addClassName("ue-subtitle");

    var wrap = new Div(title, hint);
    wrap.addClassName("ue-header");
    return wrap;
  }

  /* =========================
     SECTION: MESAS (GRID 4 COL)
     ========================= */
  private Component buildTablesSection(TableService tables, OrderService orders) {
    var section = new Div();
    section.addClassName("ue-section");

    var top = new Div();
    top.addClassName("ue-section-top");

    var h = new H3("Mesa");
    h.addClassName("ue-section-title");

    selectedTableLabel.addClassName("ue-table-selected");
    selectedTableLabel.getStyle().set("flex", "1");
    selectedTableLabel.getStyle().set("min-width", "160px");

    var refreshBtn = new Button(new Icon(VaadinIcon.REFRESH));
    refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    refreshBtn.setAriaLabel("Refrescar mesas");
    refreshBtn.addClickListener(e -> refreshTableMap(tables, orders));

    top.add(h, selectedTableLabel, refreshBtn);

    // IMPORTANTE: ahora es grid responsive 4 columnas (CSS)
    tableCanvas.addClassName("ue-table-grid");

    var canvasWrap = new Div(tableCanvas);
    canvasWrap.addClassName("ue-mesas-wrap");

    section.add(top, canvasWrap);
    return section;
  }

  private void refreshTableMap(TableService tables, OrderService orders) {
    tableCanvas.removeAll();
    tableButtonsById.clear();

    List<TableSpot> all = tables.all();
    List<TableSpot> sortedTables = sortTablesByOrderPriority(all, orders);

    for (var t : sortedTables) {
      var btn = createSelectableTableButton(t, orders);

      if (selectedTable != null && Objects.equals(selectedTable.getId(), t.getId())) {
        btn.addClassName("mesa-selected");
      }

      tableButtonsById.put(t.getId(), btn);
      tableCanvas.add(btn);
    }

    updateSelectedTableLabel();
  }

  private Button createSelectableTableButton(TableSpot t, OrderService orders) {
    var btn = new Button();
    btn.addClassName("mesa-btn");

    String statusClass = getTableStatusClass(t, orders);
    String statusBadge = getTableStatusBadge(t, orders);

    btn.addClassName(statusClass);

    btn.getElement().setProperty("innerHTML",
        "<img src='icons/mesa.png' class='mesa-icon'>" +
            "<span class='mesa-label'>" + t.getCode() + "</span>" +
            statusBadge
    );

    btn.addClickListener(e -> {
      // desmarcar anterior
      if (selectedTable != null && tableButtonsById.containsKey(selectedTable.getId())) {
        tableButtonsById.get(selectedTable.getId()).removeClassName("mesa-selected");
      }

      selectedTable = t;
      btn.addClassName("mesa-selected");
      updateSelectedTableLabel();

      Notification.show("Mesa seleccionada: " + t.getCode(), 1200, Notification.Position.BOTTOM_START)
          .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    });

    return btn;
  }

  private void updateSelectedTableLabel() {
    if (selectedTable == null) {
      selectedTableLabel.setText("Sin mesa seleccionada");
      selectedTableLabel.getElement().getThemeList().remove("badge");
    } else {
      selectedTableLabel.setText("Asignando a: " + selectedTable.getCode());
      selectedTableLabel.getElement().getThemeList().add("badge");
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
    if (activeOrders.isEmpty()) return "";

    long listoCount = activeOrders.stream().filter(o -> o.getStatus() == OrderStatus.LISTO).count();
    long preparationCount = activeOrders.stream().filter(o -> o.getStatus() == OrderStatus.IN_PREPARATION).count();
    long pendingCount = activeOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
    long pagadoCount = activeOrders.stream().filter(o -> o.getStatus() == OrderStatus.PAGADO).count();

    String badgeText = "";
    String badgeClass = "";

    if (listoCount > 0) { badgeText = "✓ LISTO"; badgeClass = "badge-listo"; }
    else if (preparationCount > 0) { badgeText = "🍳 COCINA"; badgeClass = "badge-cocina"; }
    else if (pendingCount > 0) { badgeText = "⏱ PEND"; badgeClass = "badge-pendiente"; }
    else if (pagadoCount > 0) { badgeText = "💳 PAG"; badgeClass = "badge-pagado"; }

    if (badgeText.isEmpty()) return "";
    return "<span class='mesa-badge " + badgeClass + "'>" + badgeText + "</span>";
  }

  /* =========================
     SECTION: PRODUCTOS (CARD + SEARCH + SCROLL)
     ========================= */
  private Component buildProductsSection(MenuService menu) {
    var section = new Div();
    section.addClassName("ue-section");

    var top = new Div();
    top.addClassName("ue-section-top");

    var h = new H3("Productos");
    h.addClassName("ue-section-title");

    top.add(h);

    var search = new TextField();
    search.setPlaceholder("Buscar por nombre...");
    search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    search.addClassName("ue-search");
    search.setWidthFull();
    search.setClearButtonVisible(true);

    productsList.addClassName("ue-products");

    var scroller = new Scroller(productsList);
    scroller.addClassName("ue-products-scroller");
    scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

    search.addValueChangeListener(e -> {
      String q = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
      List<Product> all = menu.list();

      // SOLO por nombre (como pediste)
      List<Product> filtered = all.stream()
          .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(q))
          .collect(Collectors.toList());

      refreshProducts(filtered);
    });

    var wrap = new VerticalLayout(top, search, scroller);
    wrap.setPadding(false);
    wrap.setSpacing(true);
    wrap.addClassName("ue-products-wrap");

    section.add(wrap);
    return section;
  }

  private void refreshProducts(List<Product> products) {
    productsList.removeAll();

    products.sort(Comparator.comparing(p -> Optional.ofNullable(p.getName()).orElse("")));

    for (var p : products) {
      productsList.add(productCard(p));
    }
  }

  private Component productCard(Product p) {
    var card = new Div();
    card.addClassName("ue-product-card");

    var img = new Image(resolveProductImage(p), Optional.ofNullable(p.getName()).orElse("Producto"));
    img.addClassName("ue-product-img");

    var name = new Div(Optional.ofNullable(p.getName()).orElse("Producto"));
    name.addClassName("ue-product-name");

    var desc = new Div(Optional.ofNullable(p.getDescription()).orElse(""));
    desc.addClassName("ue-product-desc");

    var price = new Div(String.format("€ %.2f", p.getPrice()));
    price.addClassName("ue-product-price");

    var info = new Div(name, desc, price);
    info.addClassName("ue-product-info");

    // botón +1 (solo agrega 1)
    var addBtn = new Button(new Icon(VaadinIcon.PLUS));
    addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    addBtn.addClassName("ue-product-add");

    addBtn.addClickListener(e -> {
      addOrIncrement(p, 1, null);
      Notification.show("Agregado: " + p.getName(), 1200, Notification.Position.BOTTOM_START)
          .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    });

    card.add(img, info, addBtn);
    return card;
  }

  private void addOrIncrement(Product p, int qty, String note) {
    for (OrderItem it : items) {
      if (it.getProduct() != null && it.getProduct().getId() != null
          && p.getId() != null && Objects.equals(it.getProduct().getId(), p.getId())) {

        it.setQuantity(it.getQuantity() + qty); // +1 siempre
        if (note != null && !note.isBlank()) it.setComment(note);
        refreshCart();
        return;
      }
    }

    var item = OrderItem.builder()
        .product(p)
        .productName(p.getName())
        .unitPrice(p.getPrice())
        .quantity(qty)
        .comment(note)
        .build();

    items.add(item);
    refreshCart();
  }

  /* =========================
     SECTION: CARRITO (20% EN DESKTOP)
     ========================= */
  private Component buildCartSection(OrderService orders, AuthService auth) {
    var right = new VerticalLayout();
    right.addClassName("ue-right");
    right.setPadding(false);
    right.setSpacing(true);
    right.setHeightFull();

    var header = new Div();
    header.addClassName("ue-cart-header");

    var h = new H3("Carrito");
    h.addClassName("ue-section-title");
    h.getStyle().set("margin", "0");

    header.add(h);

    cartList.addClassName("ue-cart-list");

    var scroller = new Scroller(cartList);
    scroller.addClassName("ue-cart-scroller");
    scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

    // Totales
    var totals = new Div();
    totals.addClassName("ue-cart-totals");

    var totalRow = new Div(new Span("Total"), cartTotal);
    totalRow.addClassName("ue-cart-total-row");

    totals.add(totalRow);

    // Botones
    var btnCreate = new Button("Confirmar Pedido", new Icon(VaadinIcon.CHECK_CIRCLE));
    btnCreate.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
    btnCreate.setWidthFull();

    var btnCancel = new Button("Cancelar", new Icon(VaadinIcon.CLOSE_CIRCLE));
    btnCancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
    btnCancel.setWidthFull();

    btnCancel.addClickListener(e -> {
      items.clear();
      refreshCart();
      Notification.show("Carrito cancelado", 1500, Notification.Position.BOTTOM_START);
    });

    btnCreate.addClickListener(e -> {
      if (selectedTable == null) {
        Notification.show("⚠ Selecciona una mesa", 2500, Notification.Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
        return;
      }
      if (items.isEmpty()) {
        Notification.show("⚠ Agrega productos", 2500, Notification.Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
        return;
      }

      try {
        orders.createTableOrder(selectedTable.getId(), items, auth.currentUserId());

        Notification.show("Pedido creado para " + selectedTable.getCode(), 3000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        items.clear();
        refreshCart();

      } catch (Exception ex) {
        Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
        ex.printStackTrace();
      }
    });

    var actions = new VerticalLayout(btnCreate, btnCancel);
    actions.setPadding(false);
    actions.setSpacing(true);
    actions.addClassName("ue-cart-actions");

    right.add(header, scroller, totals, actions);
    right.setFlexGrow(1, scroller);

    return right;
  }

  private void refreshCart() {
    cartList.removeAll();

    if (items.isEmpty()) {
      var empty = new Div(new Span("Tu carrito está vacío."));
      empty.addClassName("ue-cart-empty");
      cartList.add(empty);
      cartTotal.setText("€ 0.00");
      return;
    }

    for (OrderItem it : items) {
      cartList.add(cartRow(it));
    }

    double total = items.stream()
        .mapToDouble(item -> {
          BigDecimal t = item.getTotal();
          return t == null ? 0.0 : t.doubleValue();
        })
        .sum();

    cartTotal.setText(String.format("€ %.2f", total));
  }

  private Component cartRow(OrderItem item) {
    var row = new Div();
    row.addClassName("ue-cart-row");

    var name = new Div(Optional.ofNullable(item.getProductName()).orElse("Producto"));
    name.addClassName("ue-cart-name");

    var unit = new Div(String.format("€ %.2f", item.getUnitPrice()));
    unit.addClassName("ue-cart-unit");

    var left = new Div(name, unit);
    left.addClassName("ue-cart-left");

    var qty = new IntegerField();
    qty.setMin(1);
    qty.setMax(999);
    qty.setStepButtonsVisible(true);
    qty.setValue(item.getQuantity());
    qty.addClassName("ue-cart-qty");

    qty.addValueChangeListener(e -> {
      if (e.getValue() == null || e.getValue() < 1) {
        qty.setValue(1);
        return;
      }
      item.setQuantity(e.getValue());
      refreshCart();
    });

    var subtotal = new Div(String.format("€ %.2f", item.getTotal()));
    subtotal.addClassName("ue-cart-subtotal");

    var del = new Button(new Icon(VaadinIcon.TRASH));
    del.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
    del.addClassName("ue-cart-del");

    del.addClickListener(e -> {
      items.remove(item);
      refreshCart();
      Notification.show("Producto eliminado", 1200, Notification.Position.BOTTOM_START);
    });

    var right = new Div(qty, subtotal, del);
    right.addClassName("ue-cart-right");

    row.add(left, right);
    return row;
  }

  /* =========================
     Helpers
     ========================= */
  private String resolveProductImage(Product p) {
    List<String> candidates = List.of("getImageUrl", "getImage", "getPhotoUrl", "getImgUrl");

    for (String m : candidates) {
      try {
        Method method = p.getClass().getMethod(m);
        Object v = method.invoke(p);
        if (v != null) {
          String s = v.toString().trim();
          if (!s.isBlank()) return s;
        }
      } catch (Exception ignored) {}
    }

    return "images/placeholder-food.png";
  }
}
