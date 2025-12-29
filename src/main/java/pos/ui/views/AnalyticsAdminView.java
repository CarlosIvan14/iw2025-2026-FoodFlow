// src/main/java/pos/ui/views/AnalyticsAdminView.java
package pos.ui.views;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import elemental.json.JsonArray;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.domain.Product;
import pos.service.MenuService;
import pos.service.OrderService;
import pos.ui.MainLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Analytics")
@Route(value = "admin/analytics", layout = MainLayout.class)
@pos.auth.RequiredRoles(pos.domain.Role.ADMIN)
@NpmPackage(value = "chart.js", version = "4.4.0")
@JsModule("./charts-setup.js")
public class AnalyticsAdminView extends VerticalLayout implements RouteGuard, AfterNavigationObserver {

  private static final String CANVAS_DATE = "salesByDateChart";
  private static final String CANVAS_PRODUCT = "salesByProductChart";

  private final OrderService orderService;
  private final MenuService menuService;

  private DatePicker startDatePicker;
  private DatePicker endDatePicker;
  private ComboBox<Product> productFilter;

  public AnalyticsAdminView(OrderService orderService, MenuService menuService) {
    this.orderService = orderService;
    this.menuService = menuService;

    addClassName("analytics-view");
    setSizeFull();
    setSpacing(true);
    setPadding(true);

    add(new H2("Análisis de Ventas"));

    add(buildFilters(), buildChartsContent());
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    // MUCHÍSIMO más estable que addAttachListener
    renderChartsDeferred();
  }

  private VerticalLayout buildFilters() {
    var container = new VerticalLayout();
    container.setWidthFull();
    container.setPadding(false);
    container.setSpacing(true);

    var header = new H3("Filtros");

    var row = new HorizontalLayout();
    row.setWidthFull();
    row.setSpacing(true);
    row.getStyle().set("flex-wrap", "wrap");
    row.getStyle().set("align-items", "end");
    row.getStyle().set("gap", "10px");

    startDatePicker = new DatePicker("Desde");
    startDatePicker.setValue(LocalDate.now().minusMonths(1));

    endDatePicker = new DatePicker("Hasta");
    endDatePicker.setValue(LocalDate.now());

    productFilter = new ComboBox<>("Producto (opcional)");
    productFilter.setItems(menuService.list());
    productFilter.setItemLabelGenerator(Product::getName);
    productFilter.setClearButtonVisible(true);

    var btnApply = new Button("Aplicar", e -> renderChartsDeferred());
    btnApply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    var btnReset = new Button("Limpiar", e -> {
      startDatePicker.setValue(LocalDate.now().minusMonths(1));
      endDatePicker.setValue(LocalDate.now());
      productFilter.clear();
      renderChartsDeferred();
    });
    btnReset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    row.add(startDatePicker, endDatePicker, productFilter, btnApply, btnReset);
    container.add(header, row);
    return container;
  }

  private VerticalLayout buildChartsContent() {
    var container = new VerticalLayout();
    container.setWidthFull();
    container.setAlignItems(Alignment.CENTER);

    var chartsDiv = new Div();
    chartsDiv.setWidthFull();
    chartsDiv.setMaxWidth("1100px");
    chartsDiv.getStyle().set("display", "flex");
    chartsDiv.getStyle().set("flex-wrap", "wrap");
    chartsDiv.getStyle().set("gap", "20px");
    chartsDiv.getStyle().set("justify-content", "center");
    chartsDiv.getStyle().set("align-items", "center");

    // Wrapper 1 (fecha)
    var wrap1 = new Div();
    wrap1.setId("wrapSalesByDate");
    wrap1.getStyle().set("width", "520px");
    wrap1.getStyle().set("height", "320px");
    wrap1.getStyle().set("max-width", "100%");
    wrap1.getStyle().set("position", "relative");

    HtmlComponent c1 = new HtmlComponent("canvas");
    c1.setId("salesByDateChart");
    c1.getStyle().set("width", "100%");
    c1.getStyle().set("height", "100%");
    wrap1.add(c1);

    // Wrapper 2 (producto)
    var wrap2 = new Div();
    wrap2.setId("wrapSalesByProduct");
    wrap2.getStyle().set("width", "520px");
    wrap2.getStyle().set("height", "320px");
    wrap2.getStyle().set("max-width", "100%");
    wrap2.getStyle().set("position", "relative");

    HtmlComponent c2 = new HtmlComponent("canvas");
    c2.setId("salesByProductChart");
    c2.getStyle().set("width", "100%");
    c2.getStyle().set("height", "100%");
    wrap2.add(c2);

    chartsDiv.add(wrap1, wrap2);
    container.add(chartsDiv);
    return container;
  }


  /**
   * Render robusto: espera a que el navegador haga layout (2 frames),
   * y además se ejecuta “beforeClientResponse” para asegurar que el DOM ya existe.
   */
  private void renderChartsDeferred() {
    UI ui = UI.getCurrent();
    if (ui == null) return;

    ui.beforeClientResponse(this, ctx -> renderCharts());
  }

  private void renderCharts() {
    LocalDate start = startDatePicker.getValue();
    LocalDate end = endDatePicker.getValue();
    Product selectedProduct = productFilter.getValue();

    if (start == null) start = LocalDate.now().minusMonths(1);
    if (end == null) end = LocalDate.now();
    if (end.isBefore(start)) {
      LocalDate tmp = start;
      start = end;
      end = tmp;
    }

    final LocalDate startF = start;
    final LocalDate endF = end;
    final Product selectedProductF = selectedProduct;

    // OJO: aquí usas tu método que ya trae items inicializados (evita lazy)
    List<Order> base = orderService.paidOrReadyWithItems(startF, endF);

    // Chart 1: ventas por día (total o por producto seleccionado)
    Map<LocalDate, Double> totalByDay = new TreeMap<>();

    for (Order o : base) {
      if (o.getCreatedAt() == null) continue;
      LocalDate day = o.getCreatedAt().toLocalDate();

      double amount = (selectedProductF == null)
          ? (o.getTotal() == null ? 0.0 : o.getTotal().doubleValue())
          : sumOrderForProduct(o, selectedProductF);

      totalByDay.merge(day, amount, Double::sum);
    }

    // Rellenar días faltantes para línea continua
    LocalDate cursor = startF;
    while (!cursor.isAfter(endF)) {
      totalByDay.putIfAbsent(cursor, 0.0);
      cursor = cursor.plusDays(1);
    }

    DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");
    String[] dateLabels = totalByDay.keySet().stream().map(d -> d.format(dayFmt)).toArray(String[]::new);
    Double[] dateValues = totalByDay.values().toArray(new Double[0]);

    // Chart 2: ventas por producto (top 10)
    Map<String, Double> totalByProduct = new HashMap<>();

    for (Order o : base) {
      if (o.getItems() == null) continue;

      o.getItems().forEach(it -> {
        String name = it.getProductName() != null ? it.getProductName() : "Producto";
        double lineTotal = it.getTotal() != null ? it.getTotal().doubleValue() : 0.0;
        totalByProduct.merge(name, lineTotal, Double::sum);
      });
    }

    List<Map.Entry<String, Double>> topProducts = totalByProduct.entrySet().stream()
        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
        .limit(10)
        .collect(Collectors.toList());

    String[] productLabels = topProducts.stream().map(Map.Entry::getKey).toArray(String[]::new);
    Double[] productValues = topProducts.stream().map(Map.Entry::getValue).toArray(Double[]::new);

    JsonArray dateLabelsJson = toJsonArray(dateLabels);
    JsonArray dateValuesJson = toJsonArray(dateValues);
    JsonArray prodLabelsJson = toJsonArray(productLabels);
    JsonArray prodValuesJson = toJsonArray(productValues);

    String title1 = (selectedProductF == null)
        ? "Ventas por día (Total)"
        : "Ventas por día (" + selectedProductF.getName() + ")";

    // Doble RAF en el cliente: evita que Chart.js calcule tamaño con 0px
    UI.getCurrent().getPage().executeJs("""
      (function(){
        const fn = window.renderSalesCharts;
        if (!fn) { console.warn("renderSalesCharts no está cargado aún"); return; }
        requestAnimationFrame(() => {
          requestAnimationFrame(() => {
            fn($0,$1,$2,$3,$4,$5,$6);
          });
        });
      })();
      """,
      CANVAS_DATE,
      title1,
      dateLabelsJson,
      dateValuesJson,
      CANVAS_PRODUCT,
      prodLabelsJson,
      prodValuesJson
    );
  }

  private double sumOrderForProduct(Order o, Product selectedProduct) {
    if (o.getItems() == null) return 0.0;

    Long selectedId = selectedProduct.getId();
    if (selectedId == null) return 0.0;

    return o.getItems().stream()
        .filter(it -> it.getProduct() != null && it.getProduct().getId() != null)
        .filter(it -> it.getProduct().getId().equals(selectedId))
        .mapToDouble(it -> it.getTotal() != null ? it.getTotal().doubleValue() : 0.0)
        .sum();
  }

  private JsonArray toJsonArray(String[] data) {
    JsonArray array = Json.createArray();
    for (int i = 0; i < data.length; i++) array.set(i, data[i]);
    return array;
  }

  private JsonArray toJsonArray(Double[] data) {
    JsonArray array = Json.createArray();
    for (int i = 0; i < data.length; i++) array.set(i, data[i] == null ? 0.0 : data[i]);
    return array;
  }
}
