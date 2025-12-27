package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import pos.domain.OrderItem;
import pos.domain.Product;
import pos.domain.TableSpot;
import pos.service.MenuService;
import pos.service.OrderService;
import pos.service.TableService;
import pos.ui.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Crear Orden")
@Route(value = "ordenes", layout = MainLayout.class)
public class CrearOrdenView extends VerticalLayout implements RouteGuard {

  // Lista temporária em memória antes de salvar no banco
  public List<OrderItem> items = new ArrayList<>();

  public CrearOrdenView(TableService tables, MenuService menu, OrderService orders, AuthService auth) {
    addClassName("orden-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);
    setAlignItems(Alignment.CENTER); // Centraliza visualmente

    // --- Título ---
    var title = new H2("Crear Pedido");
    title.addClassName("orden-title");

    // --- Selección de mesa ---
    var tableSelect = new ComboBox<TableSpot>("Mesa");
    tableSelect.setItems(tables.all()); // Certifique-se que tables.all() retorna List<TableSpot>
    tableSelect.setItemLabelGenerator(TableSpot::getCode);
    tableSelect.addClassName("orden-combobox");
    tableSelect.setWidth("300px");

    // --- Tabla de productos (Carrinho) ---
    var grid = new Grid<>(OrderItem.class, false);
    grid.addClassName("orden-grid");
    grid.setHeight("400px");
    grid.setWidthFull();

    // 1. Nome do Produto (Snapshot)
    grid.addColumn(OrderItem::getProductName)
            .setHeader("Producto")
            .setAutoWidth(true)
            .setFlexGrow(1);

    // 2. Quantidade EDITÁVEL
    grid.addComponentColumn(item -> {
      IntegerField qtyField = new IntegerField();
      qtyField.setValue(item.getQuantity()); // Ajustado de getQty para getQuantity
      qtyField.setMin(1);
      qtyField.setMax(999);
      qtyField.setWidth("100px");
      qtyField.setStepButtonsVisible(true);

      qtyField.addValueChangeListener(e -> {
        if (e.getValue() != null && e.getValue() > 0) {
          item.setQuantity(e.getValue()); // Atualiza o objeto na lista
          grid.getDataProvider().refreshItem(item); // Atualiza a linha visualmente (para recalcular subtotal)
        }
      });
      return qtyField;
    }).setHeader("Cantidad").setWidth("140px").setFlexGrow(0);

    // 3. Preço Unitário (Snapshot)
    grid.addColumn(item -> String.format("€ %.2f", item.getUnitPrice()))
            .setHeader("Precio U.")
            .setWidth("120px")
            .setFlexGrow(0);

    // 4. Subtotal Calculado (Usando o método helper da Entidade)
    grid.addColumn(item -> String.format("€ %.2f", item.getTotal()))
            .setHeader("Subtotal")
            .setWidth("120px")
            .setFlexGrow(0);

    // 5. Botão de DELETAR
    grid.addComponentColumn(item -> {
      Button btnDelete = new Button(new Icon(VaadinIcon.TRASH));
      btnDelete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

      btnDelete.addClickListener(e -> {
        items.remove(item);
        grid.getDataProvider().refreshAll(); // Atualiza a grid inteira
        Notification.show("Producto eliminado", 2000, Notification.Position.BOTTOM_START);
      });
      return btnDelete;
    }).setHeader("Eliminar").setWidth("100px").setFlexGrow(0);


    // --- Área de Adicionar Produto ---
    var productSelect = new ComboBox<Product>("Producto");
    productSelect.setItems(menu.list());
    productSelect.setItemLabelGenerator(Product::getName);
    productSelect.setWidth("300px");

    var qty = new IntegerField("Cantidad");
    qty.setMin(1);
    qty.setValue(1);
    qty.setWidth("120px");
    qty.setStepButtonsVisible(true);

    var note = new TextField("Nota");
    note.setPlaceholder("Sin cebolla...");
    note.setWidth("200px");

    var btnAdd = new Button("Agregar", new Icon(VaadinIcon.PLUS));
    btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    // Lógica de Adicionar ao Carrinho
    btnAdd.addClickListener(e -> {
      Product p = productSelect.getValue();
      Integer q = qty.getValue();

      if (p != null && q != null && q > 0) {
        // AQUI ESTÁ A MUDANÇA PRINCIPAL:
        // Criamos o OrderItem preenchendo tanto a Referência quanto o Snapshot
        var item = OrderItem.builder()
                .product(p)               // 1. Referência FK (para estoque e relatórios)
                .productName(p.getName()) // 2. Snapshot Nome
                .unitPrice(p.getPrice())  // 3. Snapshot Preço
                .quantity(q)
                .comment(note.getValue())
                .build();

        items.add(item);
        grid.setItems(items); // Recarrega a grid com a nova lista

        Notification.show("Agregado: " + p.getName(), 2000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Resetar campos
        productSelect.clear();
        qty.setValue(1);
        note.clear();
        productSelect.focus(); // Foco de volta para agilizar digitação
      } else {
        Notification.show("Selecciona un producto válido", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });

    var addProductLayout = new HorizontalLayout(productSelect, qty, note, btnAdd);
    addProductLayout.setAlignItems(Alignment.BASELINE); // Alinha na base do texto
    addProductLayout.setWidthFull();

    // --- Botões Finais ---
    var btnCreate = new Button("Confirmar Pedido", new Icon(VaadinIcon.CHECK_CIRCLE));
    btnCreate.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
    btnCreate.setWidth("200px");

    btnCreate.addClickListener(e -> {
      if (tableSelect.getValue() == null) {
        Notification.show("⚠ Selecciona una mesa", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        return;
      }
      if (items.isEmpty()) {
        Notification.show("⚠ Agrega productos al Pedido", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        return;
      }

      try {
        // Chama o serviço passando a lista de itens montados
        orders.createTableOrder(tableSelect.getValue().getId(), items, auth.currentUserId());

        Notification.show("Pedido creadao para la mesa " + tableSelect.getValue().getCode(),
                        4000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Limpa a tela
        items.clear();
        grid.setItems(items);
        tableSelect.clear();

      } catch (Exception ex) {
        Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        ex.printStackTrace();
      }
    });

    var footer = new HorizontalLayout(btnCreate);
    footer.setWidthFull();
    footer.setJustifyContentMode(JustifyContentMode.END); // Botões à direita

    add(title, tableSelect, addProductLayout, grid, footer);
  }
}