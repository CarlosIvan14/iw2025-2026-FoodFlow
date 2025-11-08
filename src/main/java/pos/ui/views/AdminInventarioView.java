package pos.ui.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.domain.Product;
import pos.service.MenuService;
import pos.ui.MainLayout;

@PageTitle("Inventario")
@Route(value="admin/inventario", layout = MainLayout.class)
public class AdminInventarioView extends VerticalLayout implements RouteGuard {
  public AdminInventarioView(MenuService menu){
    var h = new H2("Inventario (mock)");
    var grid = new Grid<>(Product.class);
    grid.setItems(menu.list());
    add(h, grid);
  }
}
