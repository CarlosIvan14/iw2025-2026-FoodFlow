package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
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
import java.math.BigDecimal;

@PageTitle("Inventario")
@Route(value = "admin/inventario", layout = MainLayout.class)
public class AdminInventarioView extends VerticalLayout implements RouteGuard {

  public AdminInventarioView(ProductService productService) {
    addClassName("inventario-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.START);

    var title = new H2("Inventario");
    title.addClassName("inventario-title");

    var addBtn = new Button("Agregar Producto");
    addBtn.addClickListener(e -> showAddProductDialog(productService));

    var header = new Div(title, addBtn);
    header.addClassName("inventario-header");
    header.getStyle().set("display", "flex");
    header.getStyle().set("justify-content", "space-between");
    header.getStyle().set("align-items", "center");
    header.getStyle().set("width", "100%");

    var grid = new Grid<>(Product.class, false);
    grid.addClassName("inventario-grid");
    grid.addColumn(Product::getId).setHeader("ID").setAutoWidth(true);
    grid.addColumn(Product::getName).setHeader("Producto");
    grid.addColumn(Product::getPrice).setHeader("Precio");
    grid.addColumn(Product::getCategory).setHeader("Categoría").setAutoWidth(true);
    grid.addColumn(Product::getStock).setHeader("Stock").setAutoWidth(true);

    grid.setItems(productService.list());

    add(header, grid);
  }

  private void showAddProductDialog(ProductService productService) {
    var dialog = new Dialog();
    dialog.setHeaderTitle("Agregar Nuevo Producto");

    var nameField = new TextField("Nombre");
    var priceField = new BigDecimalField("Precio");
    var categoryField = new TextField("Categoría");
    var stockField = new IntegerField("Stock Inicial");
    stockField.setValue(0);

    var saveBtn = new Button("Guardar", e -> {
      if (nameField.isEmpty() || priceField.isEmpty() || categoryField.isEmpty()) return;

      var p = Product.builder()
          .name(nameField.getValue())
          .price(priceField.getValue())
          .category(categoryField.getValue())
          .stock(stockField.getValue())
          .build();

      productService.create(p);
      dialog.close();
      com.vaadin.flow.component.UI.getCurrent().getPage().reload();
    });

    var layout = new VerticalLayout(nameField, priceField, categoryField, stockField, saveBtn);
    dialog.add(layout);
    dialog.open();
  }
}
