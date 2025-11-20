package pos.ui.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.domain.Product;
import pos.ui.MainLayout;

@PageTitle("Inventario")
@Route(value = "admin/inventario", layout = MainLayout.class)
public class AdminInventarioView extends VerticalLayout implements RouteGuard {

  public AdminInventarioView(MenuService menu) {
    addClassName("inventario-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.START);

    var title = new H2("Inventario (Mock)");
    title.addClassName("inventario-title");

    var grid = new Grid<>(Product.class, false);
    grid.addClassName("inventario-grid");
    grid.addColumn(Product::id).setHeader("ID").setAutoWidth(true);
    grid.addColumn(Product::name).setHeader("Nombre").setAutoWidth(true);
    grid.addColumn(Product::price).setHeader("Precio").setAutoWidth(true);
    grid.addColumn(Product::category).setHeader("Categoría").setAutoWidth(true);

    grid.setItems(menu.list());

    add(title, grid);
  }
}
