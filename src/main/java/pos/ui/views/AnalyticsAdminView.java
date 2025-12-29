package pos.ui.views;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import elemental.json.JsonArray;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.domain.OrderStatus;
import pos.domain.Product;
import pos.service.MenuService;
import pos.service.OrderService;
import pos.ui.MainLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Analytics")
@Route(value = "admin/analytics", layout = MainLayout.class)
@pos.auth.RequiredRoles(pos.domain.Role.ADMIN)
@NpmPackage(value = "chart.js", version = "4.4.0")
@JsModule("./charts-setup.js")
public class AnalyticsAdminView extends VerticalLayout implements RouteGuard {

  private final OrderService orderService;
  private final MenuService menuService;

  private DatePicker startDatePicker;
  private DatePicker endDatePicker;
  private ComboBox<Product> productFilter;
  private Grid<Order> salesGrid;

  public AnalyticsAdminView(OrderService orderService, MenuService menuService) {
    this.orderService = orderService;
    this.menuService = menuService;

    addClassName("analytics-view");
    setSizeFull();
    setSpacing(true);
    setPadding(true);

    var title = new H2("Análisis de Negocio");
    add(title);

    // Tabs
    var tabCharts = new Tab("Gráficos");
    var tabHistory = new Tab("Historial de Ventas");
    var tabs = new Tabs(tabCharts, tabHistory);
    tabs.setWidthFull();

    var chartsContent = buildChartsContent();
    var historyContent = buildHistoryContent();

    var tabContent = new Div(chartsContent, historyContent);
    tabContent.setWidthFull();

    chartsContent.setVisible(true);
    historyContent.setVisible(false);

    tabs.addSelectedChangeListener(e -> {
      boolean charts = e.getSelectedTab() == tabCharts;
      chartsContent.setVisible(charts);
      historyContent.setVisible(!charts);

      // Si vuelves al tab de gráficos, re-render por si el canvas cambió tamaño
      if (charts) {
        renderCharts();
      }
    });

    add(tabs, tabContent);
  }

  private VerticalLayout buildChartsContent() {
    var container = new VerticalLayout();
    container.setWidthFull();
    container.setAlignItems(Alignment.CENTER);

    var chartsDiv = new Div();
    chartsDiv.setWidthFull();
    chartsDiv.setMaxWidth("1000px");
    chartsDiv.getStyle().set("display", "flex");
    chartsDiv.getStyle().set("flex-wrap", "wrap");
    chartsDiv.getStyle().set("gap", "20px");
    chartsDiv.getStyle().set("justify-content", "center");
    chartsDiv.getStyle().set("align-items", "center");

    HtmlComponent salesCanvas = new HtmlComponent("canvas");
    salesCanvas.setId("salesChart");
    salesCanvas.getStyle().set("width", "480px");
    salesCanvas.getStyle().set("height", "320px");
    salesCanvas.getStyle().set("max-width", "100%");
    salesCanvas.getElement().setAttribute("width", "480");
    salesCanvas.getElement().setAttribute("height", "320");


    HtmlComponent rolesCanvas = new HtmlComponent("canvas");
    rolesCanvas.setId("rolesChart");
    rolesCanvas.getStyle().set("width", "360px");
    rolesCanvas.getStyle().set("height", "320px");
    rolesCanvas.getStyle().set("max-width", "100%");
    rolesCanvas.getElement().setAttribute("width", "360");
    rolesCanvas.getElement().setAttribute("height", "320");

    chartsDiv.add(salesCanvas, rolesCanvas);
    container.add(chartsDiv);

    // Render inicial al attach
    container.addAttachListener(e -> renderCharts());

    return container;
  }

  private VerticalLayout buildHistoryContent() {
    var container = new VerticalLayout();
    container.setWidthFull();
    container.setPadding(false);
    container.setSpacing(true);

    // Filtros
    var filterHeader = new H3("Filtros");

    var filterLayout = new HorizontalLayout();
    filterLayout.setWidthFull();
    filterLayout.setSpacing(true);

    // wrap responsive (HorizontalLayout no tiene setWrappingEnabled)
    filterLayout.getStyle().set("flex-wrap", "wrap");
    filterLayout.getStyle().set("align-items", "end");
    filterLayout.getStyle().set("gap", "10px");


    startDatePicker = new DatePicker("Desde");
    startDatePicker.setValue(LocalDate.now().minusMonths(1));

    endDatePicker = new DatePicker("Hasta");
    endDatePicker.setValue(LocalDate.now());

    productFilter = new ComboBox<>("Producto");
    productFilter.setItems(menuService.list());
    productFilter.setItemLabelGenerator(Product::getName);
    productFilter.setClearButtonVisible(true);

    var filterBtn = new Button("Filtrar", e -> applyFilters());
    filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    var resetBtn = new Button("Limpiar", e -> resetFilters());
    resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    filterLayout.add(startDatePicker, endDatePicker, productFilter, filterBtn, resetBtn);

    // Grid historial
    salesGrid = new Grid<>(Order.class, false);
    salesGrid.addClassName("sales-history-grid");
    salesGrid.setWidthFull();

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    salesGrid.addColumn(Order::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);

    salesGrid.addColumn(o ->
        o.getCreatedAt() != null ? o.getCreatedAt().format(fmt) : "—"
    ).setHeader("Fecha").setAutoWidth(true);

    salesGrid.addColumn(Order::getStatus).setHeader("Estado").setAutoWidth(true);

    salesGrid.addColumn(o ->
        "€ " + String.format("%.2f", o.getTotal() == null ? 0.0 : o.getTotal())
    ).setHeader("Total").setAutoWidth(true);

    salesGrid.addComponentColumn(o -> {
      var itemsBtn = new Button("Ver Items");
      itemsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
      itemsBtn.addClickListener(e -> showOrderDetails(o));
      return itemsBtn;
    }).setHeader("Detalles").setAutoWidth(true);

    container.add(filterHeader, filterLayout, salesGrid);
    container.setFlexGrow(1, salesGrid);

    // inicial
    applyFilters();

    return container;
  }

  private void applyFilters() {
    LocalDate start = startDatePicker.getValue();
    LocalDate end = endDatePicker.getValue();
    Product selectedProduct = productFilter.getValue();

    List<Order> filtered = orderService.all().stream()
        .filter(o -> o.getStatus() == OrderStatus.PAGADO || o.getStatus() == OrderStatus.LISTO)
        .filter(o -> {
          if (o.getCreatedAt() == null) return false;
          LocalDate d = o.getCreatedAt().toLocalDate();
          return !d.isBefore(start) && !d.isAfter(end);
        })
        .filter(o -> {
          if (selectedProduct == null) return true;
          if (o.getItems() == null) return false;
          return o.getItems().stream().anyMatch(item ->
              item.getProduct() != null &&
              item.getProduct().getId() != null &&
              item.getProduct().getId().equals(selectedProduct.getId())
          );
        })
        .collect(Collectors.toList());

    salesGrid.setItems(filtered);
  }

  private void resetFilters() {
    startDatePicker.setValue(LocalDate.now().minusMonths(1));
    endDatePicker.setValue(LocalDate.now());
    productFilter.clear();
    applyFilters();
  }

  private void showOrderDetails(Order order) {
    var dialog = new com.vaadin.flow.component.dialog.Dialog();
    dialog.setHeaderTitle("Detalles del Pedido #" + order.getId());
    dialog.setMaxWidth("650px");

    var content = new VerticalLayout();
    content.setSpacing(true);

    if (order.getItems() != null && !order.getItems().isEmpty()) {
      order.getItems().forEach(item -> {
        var itemDiv = new Div();
        itemDiv.getStyle().set("padding", "8px");
        itemDiv.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        String line =
            item.getProductName() + " x" + item.getQuantity() +
            " @ €" + String.format("%.2f", item.getUnitPrice()) +
            " = €" + String.format("%.2f", item.getTotal());

        itemDiv.setText(line);
        content.add(itemDiv);
      });
    } else {
      content.add(new Div(new com.vaadin.flow.component.html.Span("Sin items registrados.")));
    }

    dialog.add(content);
    dialog.open();
  }

  private void renderCharts() {
    // Demo data (luego puedes reemplazar por datos reales)
    String[] daysRaw = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
    Integer[] salesRaw = {180, 220, 300, 270, 350, 420, 250};

    String[] rolesRaw = {"Comida", "Bebida", "Postre"};
    Integer[] roleDataRaw = {65, 25, 10};

    JsonArray daysJson = toJsonArray(daysRaw);
    JsonArray salesJson = toJsonArray(salesRaw);
    JsonArray rolesJson = toJsonArray(rolesRaw);
    JsonArray roleDataJson = toJsonArray(roleDataRaw);

    UI.getCurrent().getPage().executeJs(
        "window.renderPOSCharts($0, $1, $2, $3, $4, $5)",
        "salesChart",
        daysJson,
        salesJson,
        "rolesChart",
        rolesJson,
        roleDataJson
    );
  }

  private JsonArray toJsonArray(String[] data) {
    JsonArray array = Json.createArray();
    for (int i = 0; i < data.length; i++) {
      array.set(i, data[i]);
    }
    return array;
  }

  private JsonArray toJsonArray(Integer[] data) {
    JsonArray array = Json.createArray();
    for (int i = 0; i < data.length; i++) {
      array.set(i, data[i]);
    }
    return array;
  }
}
