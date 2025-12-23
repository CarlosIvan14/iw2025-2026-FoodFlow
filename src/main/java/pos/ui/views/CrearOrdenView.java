package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.AuthService;
import pos.auth.RouteGuard;
import pos.domain.Product;
import pos.domain.TableSpot;
import pos.ui.MainLayout;
import pos.service.TableService;
import pos.service.MenuService;
import pos.service.OrderService;
import pos.domain.OrderItem;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Crear Orden")
@Route(value = "ordenes", layout = MainLayout.class)
public class CrearOrdenView extends VerticalLayout implements RouteGuard {

  public List<OrderItem> items = new ArrayList<>();

  public CrearOrdenView(TableService tables, MenuService menu, OrderService orders, AuthService auth) {
    addClassName("orden-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);
    getStyle().set("align-items", "center");

    // --- Título ---
    var title = new H2("Crear Orden (Mesero)");
    title.addClassName("orden-title");

    // --- Selección de mesa ---
    var tableSelect = new ComboBox<TableSpot>("Mesa");
    tableSelect.setItems(tables.all());
    tableSelect.setItemLabelGenerator(TableSpot::getCode);
    tableSelect.addClassName("orden-combobox");

    // --- Tabla de productos ---
    var grid = new Grid<>(OrderItem.class, false);
    grid.addClassName("orden-grid");
    grid.setHeight("400px");

    // 1. Nome do Produto
    grid.addColumn(OrderItem::getProductName)
            .setHeader("Producto")
            .setAutoWidth(true)
            .setFlexGrow(1);

    // 2. Quantidade EDITÁVEL com botões +/-
    grid.addComponentColumn(item -> {
              IntegerField qtyField = new IntegerField();
              qtyField.setValue(item.getQty());
              qtyField.setMin(1);
              qtyField.setMax(999);
              qtyField.setWidth("120px");
              qtyField.setStepButtonsVisible(true);
              qtyField.setStep(1);
              qtyField.addClassName("orden-qty-field");

              qtyField.addValueChangeListener(e -> {
                if (e.getValue() != null && e.getValue() > 0) {
                  item.setQty(e.getValue());
                  grid.getDataProvider().refreshItem(item);
                }
              });

              return qtyField;
            })
            .setHeader("Cantidad")
            .setWidth("140px")
            .setFlexGrow(0);

    // 3. Preço Unitário
    grid.addColumn(item -> String.format("€%.2f", item.getUnitPrice()))
            .setHeader("Precio U.")
            .setWidth("110px")
            .setFlexGrow(0);

    // 4. Subtotal Calculado
    grid.addColumn(item -> String.format("€%.2f",
                    item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQty()))))
            .setHeader("Subtotal")
            .setWidth("110px")
            .setFlexGrow(0);

    // 5. Botão de DELETAR
    grid.addComponentColumn(item -> {
              Button btnDelete = new Button(new Icon(VaadinIcon.TRASH));
              btnDelete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
              btnDelete.addClassName("orden-delete-btn");
              btnDelete.getElement().setAttribute("aria-label", "Eliminar producto");

              btnDelete.addClickListener(e -> {
                items.remove(item);
                grid.getDataProvider().refreshAll();
                Notification.show("Producto eliminado", 2000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
              });

              return btnDelete;
            })
            .setHeader("Eliminar")
            .setWidth("100px")
            .setFlexGrow(0);

    // Product selection and add to order
    var productSelect = new ComboBox<Product>("Producto");
    productSelect.setItems(menu.list());
    productSelect.setItemLabelGenerator(Product::getName);
    productSelect.setWidth("300px");
    productSelect.addClassName("orden-product-select");

    var qty = new IntegerField("Cantidad");
    qty.setMin(1);
    qty.setValue(1);
    qty.setWidth("100px");
    qty.setStepButtonsVisible(true);
    qty.addClassName("orden-cantidad");

    var note = new TextField("Nota");
    note.setPlaceholder("Nota opcional");
    note.setWidth("200px");
    note.addClassName("orden-nota");

    var btnAdd = new Button("Agregar", new Icon(VaadinIcon.PLUS));
    btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    btnAdd.addClassName("orden-add-btn");

    btnAdd.addClickListener(e -> {
      var p = productSelect.getValue();
      var q = qty.getValue();

      if (p != null && q != null && q > 0) {
        items.add(OrderItem.builder()
                .productId(p.getId())
                .productName(p.getName())
                .qty(q)
                .unitPrice(p.getPrice())
                .comment(note.getValue())
                .build());
        grid.setItems(items);

        Notification.show("✓ Agregado: " + p.getName(), 2000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        productSelect.clear();
        qty.setValue(1);
        note.clear();
      } else {
        Notification.show("Selecciona un producto y cantidad válida", 3000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });

    var addProductLayout = new HorizontalLayout(productSelect, qty, note, btnAdd);
    addProductLayout.addClassName("orden-add-product-layout");
    addProductLayout.setAlignItems(Alignment.END);
    addProductLayout.setWidthFull();
    addProductLayout.getStyle().set("flex-wrap", "wrap");

    // --- Botones principales ---
    var btnCreate = new Button("Crear Orden", new Icon(VaadinIcon.CHECK_CIRCLE));
    btnCreate.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
    btnCreate.addClassName("orden-crear-btn");

    btnCreate.addClickListener(e -> {
      if (tableSelect.getValue() == null) {
        Notification.show("⚠ Selecciona una mesa", 3000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        return;
      }

      if (items.isEmpty()) {
        Notification.show("⚠ Agrega productos", 3000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        return;
      }

      try {
        orders.createTableOrder(tableSelect.getValue().getId(), items, auth.currentUserId());

        Notification.show("✓ Orden creada exitosamente para la mesa " + tableSelect.getValue().getCode(),
                        4000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        items.clear();
        grid.setItems(items);
        tableSelect.clear();

      } catch (RuntimeException ex) {
        Notification.show("✗ " + ex.getMessage(), 5000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });

    var btnDividir = new Button("Dividir Cuenta", new Icon(VaadinIcon.SPLIT));
    btnDividir.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
    btnDividir.addClassName("orden-dividir-btn");

    btnDividir.addClickListener(e -> {
      if (items.isEmpty()) {
        Notification.show("No hay productos para dividir", 2000, Notification.Position.BOTTOM_START);
        return;
      }

      double total = items.stream()
              .mapToDouble(i -> i.getUnitPrice().doubleValue() * i.getQty())
              .sum();

      Notification.show(String.format("💰 Total: €%.2f → Dos cuentas de: €%.2f", total, total / 2.0),
                      4000, Notification.Position.BOTTOM_CENTER)
              .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    });

    var buttons = new HorizontalLayout(btnCreate, btnDividir);
    buttons.addClassName("orden-buttons");
    buttons.setSpacing(true);

    add(title, tableSelect, addProductLayout, grid, buttons);
  }
}