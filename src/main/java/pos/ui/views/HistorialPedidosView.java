// src/main/java/pos/ui/views/HistorialPedidosView.java
package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.domain.OrderStatus;
import pos.service.OrderService;
import pos.ui.MainLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Historial de Pedidos")
@Route(value = "admin/historial-pedidos", layout = MainLayout.class)
@pos.auth.RequiredRoles(pos.domain.Role.ADMIN)
public class HistorialPedidosView extends VerticalLayout implements RouteGuard {

  private final OrderService orderService;

  private DatePicker startDatePicker;
  private DatePicker endDatePicker;
  private ComboBox<OrderStatus> statusFilter;

  private Grid<Order> grid;

  public HistorialPedidosView(OrderService orderService) {
    this.orderService = orderService;

    addClassName("historial-pedidos-view");
    setSizeFull();
    setSpacing(true);
    setPadding(true);

    add(new H2("Historial de Pedidos"));

    add(buildFilters());
    add(buildGrid());

    applyFilters();
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

    statusFilter = new ComboBox<>("Estado");
    statusFilter.setItems(OrderStatus.values());
    statusFilter.setClearButtonVisible(true);

    var btnApply = new Button("Filtrar", e -> applyFilters());
    btnApply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    var btnReset = new Button("Limpiar", e -> {
      startDatePicker.setValue(LocalDate.now().minusMonths(1));
      endDatePicker.setValue(LocalDate.now());
      statusFilter.clear();
      applyFilters();
    });
    btnReset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    row.add(startDatePicker, endDatePicker, statusFilter, btnApply, btnReset);
    container.add(header, row);
    return container;
  }

  private Div buildGrid() {
    var wrapper = new Div();
    wrapper.setSizeFull();

    grid = new Grid<>(Order.class, false);
    grid.setWidthFull();

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    grid.addColumn(Order::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);

    grid.addColumn(o -> o.getCreatedAt() != null ? o.getCreatedAt().format(fmt) : "—")
        .setHeader("Fecha").setAutoWidth(true);

    grid.addColumn(Order::getStatus).setHeader("Estado").setAutoWidth(true);

    // OJO: total es BigDecimal en tu dominio -> doubleValue()
    grid.addColumn(o -> "€ " + String.format("%.2f",
            o.getTotal() == null ? 0.0 : o.getTotal().doubleValue()))
        .setHeader("Total").setAutoWidth(true);

    grid.addColumn(o -> o.getTableId() != null ? ("Mesa " + o.getTableId()) : "Delivery")
        .setHeader("Origen").setAutoWidth(true);

    wrapper.add(grid);
    wrapper.getStyle().set("margin-top", "10px");
    wrapper.setHeightFull();

    setFlexGrow(1, wrapper);
    return wrapper;
  }

  private void applyFilters() {
    LocalDate start = startDatePicker.getValue();
    LocalDate end = endDatePicker.getValue();
    OrderStatus status = statusFilter.getValue();

    if (start == null) start = LocalDate.now().minusMonths(1);
    if (end == null) end = LocalDate.now();
    if (end.isBefore(start)) {
      LocalDate tmp = start;
      start = end;
      end = tmp;
    }

    final LocalDate startF = start;
    final LocalDate endF = end;
    final OrderStatus statusF = status;

    List<Order> filtered = orderService.all().stream()
        .filter(o -> o.getCreatedAt() != null)
        .filter(o -> {
          LocalDate d = o.getCreatedAt().toLocalDate();
          return !d.isBefore(startF) && !d.isAfter(endF);
        })
        .filter(o -> statusF == null || o.getStatus() == statusF)
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .collect(Collectors.toList());

    grid.setItems(filtered);
  }
}
