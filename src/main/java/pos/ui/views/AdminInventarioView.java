package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.domain.Product;
import pos.ui.MainLayout;
import pos.service.ProductService;
import pos.service.ImageUploadService;

@PageTitle("Productos")
@Route(value = "admin/productos", layout = MainLayout.class)
@pos.auth.RequiredRoles(pos.domain.Role.ADMIN)
public class AdminInventarioView extends VerticalLayout implements RouteGuard {

  private final ProductService productService;
  private final ImageUploadService imageUploadService;
  private final Grid<Product> grid;

  public AdminInventarioView(ProductService productService, ImageUploadService imageUploadService) {
    this.productService = productService;
    this.imageUploadService = imageUploadService;

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
    
    // Columna de imagen
    grid.addComponentColumn(product -> {
      if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
        var img = new Image(product.getImageUrl(), product.getName());
        img.setWidth("50px");
        img.setHeight("50px");
        img.getStyle().set("object-fit", "cover").set("border-radius", "4px");
        return img;
      }
      return new Div("Sin imagen");
    }).setHeader("Imagen").setAutoWidth(true);
    
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

    // Sección de imagen
    var imagePreview = new Image();
    imagePreview.setWidth("150px");
    imagePreview.setHeight("150px");
    imagePreview.getStyle().set("object-fit", "cover").set("border-radius", "8px");
    
    var imageSection = new Div();
    imageSection.addClassName("image-section");
    imageSection.getStyle().set("margin-bottom", "16px");
    
    // Upload de imagen
    MemoryBuffer buffer = new MemoryBuffer();
    Upload upload = new Upload(buffer);
    upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp", "image/gif");
    upload.setMaxFileSize(5 * 1024 * 1024); // 5MB máximo
    upload.setDropLabel(new com.vaadin.flow.component.html.Span("Arrastra una imagen aquí o haz clic para seleccionar"));
    
    final String[] uploadedImageUrl = {null};
    
    upload.addSucceededListener(event -> {
      try {
        String imageUrl = imageUploadService.uploadImage(buffer.getInputStream(), 
                                                          event.getFileName());
        uploadedImageUrl[0] = imageUrl;
        imagePreview.setSrc(imageUrl);
        Notification.show("Imagen cargada correctamente", 2000, Notification.Position.BOTTOM_START)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      } catch (Exception e) {
        Notification.show("Error al cargar imagen: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });
    
    upload.addFailedListener(event -> {
      Notification.show("Error: " + event.getReason().getMessage(), 3000, Notification.Position.TOP_CENTER)
          .addThemeVariants(NotificationVariant.LUMO_ERROR);
    });

    // Se for edição, preenchemos os campos com os dados atuais
    if (isEditMode) {
      nameField.setValue(productToEdit.getName());
      priceField.setValue(productToEdit.getPrice());
      categoryField.setValue(productToEdit.getCategory());
      stockField.setValue(productToEdit.getStock());
      
      if (productToEdit.getImageUrl() != null && !productToEdit.getImageUrl().isBlank()) {
        imagePreview.setSrc(productToEdit.getImageUrl());
        uploadedImageUrl[0] = productToEdit.getImageUrl();
      }
    } else {
      stockField.setValue(0); // Valor padrão para novos
      imagePreview.setSrc("images/placeholder-food.png");
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
              .imageUrl(uploadedImageUrl[0]) // Guardar URL de la imagen
              .build();

      try {
        if (isEditMode) {
          // Si hay imagen anterior diferente, eliminarla
          if (productToEdit.getImageUrl() != null && 
              !productToEdit.getImageUrl().equals(uploadedImageUrl[0])) {
            imageUploadService.deleteImage(productToEdit.getImageUrl());
          }
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

    // Preparar sección de imagen
    imageSection.add(imagePreview, upload);
    
    var layout = new VerticalLayout(imageSection, nameField, priceField, categoryField, stockField);
    layout.setPadding(true);
    layout.setSpacing(true);
    
    var buttons = new HorizontalLayout(saveBtn, cancelBtn);
    buttons.setSpacing(true);

    dialog.add(layout, buttons);
    dialog.setWidth("500px");
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