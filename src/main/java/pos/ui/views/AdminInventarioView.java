package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.domain.Product;
import pos.ui.MainLayout;
import pos.service.ProductService;

@PageTitle("Productos")
@Route(value = "admin/productos", layout = MainLayout.class)
public class AdminInventarioView extends VerticalLayout implements RouteGuard {

  private final ProductService productService;
  private final Grid<Product> grid;

  public AdminInventarioView(ProductService productService) {
    this.productService = productService; // Guardamos o service para usar nos métodos

    addClassName("inventario-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.START);

    var title = new H2("Gestión de Productos");
    title.addClassName("inventario-title");

    // Botão de Adicionar (Passamos 'null' para indicar que é um novo produto)
    var addBtn = new Button("Agregar Producto", VaadinIcon.PLUS.create());
    addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    addBtn.addClickListener(e -> showProductDialog(null));

    var header = new Div(title, addBtn);
    header.addClassName("inventario-header");
    header.getStyle().set("display", "flex");
    header.getStyle().set("justify-content", "space-between");
    header.getStyle().set("align-items", "center");
    header.getStyle().set("width", "100%");

    // Configuração da Grid
    grid = new Grid<>(Product.class, false);
    grid.addClassName("inventario-grid");
    grid.addColumn(Product::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
    grid.addColumn(Product::getName).setHeader("Producto");
    grid.addColumn(Product::getPrice).setHeader("Precio");
    grid.addColumn(Product::getCategory).setHeader("Categoría").setAutoWidth(true);
    grid.addColumn(Product::getStock).setHeader("Stock").setAutoWidth(true);

    // --- NOVA COLUNA DE AÇÕES (EDITAR E DELETAR) ---
    grid.addComponentColumn(product -> {

      // Botão Editar
      Button editBtn = new Button(VaadinIcon.EDIT.create());
      editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      editBtn.addClickListener(e -> showProductDialog(product));

      // Botão Deletar
      Button deleteBtn = new Button(VaadinIcon.TRASH.create());
      deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
      deleteBtn.addClickListener(e -> showDeleteConfirmation(product));

      return new HorizontalLayout(editBtn, deleteBtn);
    }).setHeader("Acciones");

    updateGrid(); // Carrega os dados iniciais

    add(header, grid);
  }

  // Método auxiliar para atualizar a lista sem recarregar a página
  private void updateGrid() {
    grid.setItems(productService.list());
  }

  // Lógica unificada para Criar e Editar
  private void showProductDialog(Product productToEdit) {
    Dialog dialog = new Dialog();
    boolean isEditMode = productToEdit != null;

    dialog.setHeaderTitle(isEditMode ? "Editar Producto" : "Agregar Nuevo Producto");

    var nameField = new TextField("Nombre");
    var priceField = new BigDecimalField("Precio");
    var categoryField = new TextField("Categoría");
    var stockField = new IntegerField("Stock");

    // Se for edição, preenchemos os campos com os dados atuais
    if (isEditMode) {
      nameField.setValue(productToEdit.getName());
      priceField.setValue(productToEdit.getPrice());
      categoryField.setValue(productToEdit.getCategory());
      stockField.setValue(productToEdit.getStock());
    } else {
      stockField.setValue(0); // Valor padrão para novos
    }

    var saveBtn = new Button("Guardar", e -> {
      if (nameField.isEmpty() || priceField.isEmpty() || categoryField.isEmpty()) {
        Notification.show("Por favor complete todos los campos mandatory.");
        return;
      }

      // Cria o objeto (ou usa o builder para atualizar os dados)
      var p = Product.builder()
              .name(nameField.getValue())
              .price(priceField.getValue())
              .category(categoryField.getValue())
              .stock(stockField.getValue())
              .build();

      try {
        if (isEditMode) {
          // Se for edição, chamamos o update passando o ID original
          productService.update(productToEdit.getId(), p);
          showNotification("Producto actualizado correctamente", false);
        } else {
          // Se for novo, chamamos o create
          productService.create(p);
          showNotification("Producto creado correctamente", false);
        }

        updateGrid(); // Atualiza a tabela
        dialog.close();

      } catch (Exception ex) {
        showNotification("Error al guardar: " + ex.getMessage(), true);
      }
    });
    saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    var cancelBtn = new Button("Cancelar", e -> dialog.close());

    var layout = new VerticalLayout(nameField, priceField, categoryField, stockField);
    var buttons = new HorizontalLayout(saveBtn, cancelBtn);

    dialog.add(layout, buttons);
    dialog.open();
  }

  // Diálogo de confirmação para exclusão
  private void showDeleteConfirmation(Product product) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Confirmar eliminación");
    dialog.add("¿Estás seguro de que deseas eliminar '" + product.getName() + "'?");

    Button confirmBtn = new Button("Eliminar", e -> {
      try {
        productService.delete(product.getId());
        updateGrid();
        showNotification("Producto eliminado", false);
        dialog.close();
      } catch (Exception ex) {
        showNotification("Error al eliminar", true);
      }
    });
    confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

    Button cancelBtn = new Button("Cancelar", e -> dialog.close());

    dialog.getFooter().add(cancelBtn, confirmBtn);
    dialog.open();
  }

  private void showNotification(String text, boolean isError) {
    Notification notification = Notification.show(text);
    notification.addThemeVariants(isError ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_SUCCESS);
  }
}