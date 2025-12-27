package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.domain.Product;
import pos.service.MenuService;
import pos.ui.MainLayout;

@PageTitle("Menú Digital")
@Route(value = "", layout = MainLayout.class)
public class MenuView extends VerticalLayout {

  private Grid<Product> grid;
  private final MenuService menuService;

  public MenuView(MenuService menu) {
    this.menuService = menu;
    addClassName("menu-view");
    setSizeFull();
    setPadding(false);
    setSpacing(false);

    add(createHeader(), createContent());
  }

  private VerticalLayout createHeader() {
    var headerLayout = new VerticalLayout();
    headerLayout.addClassName("menu-header-section");
    headerLayout.setPadding(true);
    headerLayout.setSpacing(true);
    headerLayout.setWidth("100%");

    // Título con ícono
    var titleLayout = new HorizontalLayout();
    titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
    titleLayout.setSpacing(true);

    var icon = new Icon(VaadinIcon.BOOK);
    icon.setSize("32px");
    icon.addClassName("title-icon");

    var title = new H2("Menú Digital");
    title.addClassName("menu-title");
    title.getStyle().set("margin", "0");

    titleLayout.add(icon, title);

    // Subtítulo
    var subtitle = new Span("Explore nuestro catálogo de productos");
    subtitle.addClassName("menu-subtitle");

    headerLayout.add(titleLayout, subtitle);
    return headerLayout;
  }

  private VerticalLayout createContent() {
    var contentLayout = new VerticalLayout();
    contentLayout.addClassName("menu-content");
    contentLayout.setSizeFull();
    contentLayout.setPadding(true);
    contentLayout.setSpacing(true);

    contentLayout.add(
            createFilterSection(),
            createGrid(),
            createActionButton()
    );

    return contentLayout;
  }

  private HorizontalLayout createFilterSection() {
    var filterLayout = new HorizontalLayout();
    filterLayout.addClassName("filter-section");
    filterLayout.setWidthFull();
    filterLayout.setAlignItems(FlexComponent.Alignment.END);
    filterLayout.setSpacing(true);

    // Campo de búsqueda con ícono
    var searchField = new TextField();
    searchField.setPlaceholder("Buscar por categoría...");
    searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
    searchField.addClassName("search-field");
    searchField.setWidth("300px");

    // Botón de filtrar
    var filterBtn = new Button("Filtrar");
    filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    filterBtn.setIcon(new Icon(VaadinIcon.FILTER));
    filterBtn.addClassName("filter-btn");

    // Botón de limpiar
    var clearBtn = new Button("Limpiar");
    clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    clearBtn.setIcon(new Icon(VaadinIcon.CLOSE_SMALL));
    clearBtn.addClassName("clear-btn");

    filterBtn.addClickListener(e -> {
      String category = searchField.getValue();
      if (category == null || category.isBlank()) {
        grid.setItems(menuService.list());
      } else {
        grid.setItems(menuService.byCategory(category));
      }
    });

    clearBtn.addClickListener(e -> {
      searchField.clear();
      grid.setItems(menuService.list());
    });

    filterLayout.add(searchField, filterBtn, clearBtn);
    return filterLayout;
  }

  private Grid<Product> createGrid() {
    grid = new Grid<>(Product.class, false);
    grid.addClassName("menu-grid");
    grid.addThemeVariants(
            GridVariant.LUMO_NO_BORDER,
            GridVariant.LUMO_ROW_STRIPES,
            GridVariant.LUMO_WRAP_CELL_CONTENT
    );
    grid.setHeight("100%");

    // Columna de Producto con badge de categoría
    grid.addColumn(new ComponentRenderer<>(product -> {
              var layout = new VerticalLayout();
              layout.setPadding(false);
              layout.setSpacing(false);

              var name = new Span(product.getName());
              name.addClassName("product-name");
              name.getStyle()
                      .set("font-weight", "600")
                      .set("font-size", "1rem");

              var categoryBadge = new Span(product.getCategory());
              categoryBadge.addClassName("category-badge");
              categoryBadge.getStyle()
                      .set("background-color", "var(--lumo-contrast-10pct)")
                      .set("color", "var(--lumo-secondary-text-color)")
                      .set("padding", "2px 8px")
                      .set("border-radius", "12px")
                      .set("font-size", "0.75rem")
                      .set("display", "inline-block")
                      .set("margin-top", "4px");

              layout.add(name, categoryBadge);
              return layout;
            }))
            .setHeader("Productos")
            .setAutoWidth(true)
            .setFlexGrow(1);

    // Columna de Precio con formato
    grid.addColumn(new ComponentRenderer<>(product -> {
              var priceLayout = new HorizontalLayout();
              priceLayout.setAlignItems(FlexComponent.Alignment.CENTER);
              priceLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
              priceLayout.setWidthFull();
              priceLayout.setSpacing(false);

              var priceSpan = new Span(String.format("€%.2f", product.getPrice()));
              priceSpan.addClassName("product-price");
              priceSpan.getStyle()
                      .set("font-weight", "700")
                      .set("font-size", "1.1rem")
                      .set("color", "var(--lumo-primary-text-color)");

              priceLayout.add(priceSpan);
              return priceLayout;
            }))
            .setHeader("Precio")
            .setAutoWidth(true)
            .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

    grid.setItems(menuService.list());
    return grid;
  }

  private HorizontalLayout createActionButton() {
    var actionLayout = new HorizontalLayout();
    actionLayout.addClassName("action-section");
    actionLayout.setWidthFull();
    actionLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
    actionLayout.setPadding(true);

    var orderBtn = new Button("Realizar Pedido");
    orderBtn.addThemeVariants(
            ButtonVariant.LUMO_PRIMARY,
            ButtonVariant.LUMO_LARGE
    );
    orderBtn.setIcon(new Icon(VaadinIcon.CART));
    orderBtn.addClassName("order-btn");
    orderBtn.getStyle()
            .set("border-radius", "8px")
            .set("padding", "12px 24px");

    orderBtn.addClickListener(e ->
            getUI().ifPresent(ui -> ui.navigate("ordenes"))
    );

    actionLayout.add(orderBtn);
    return actionLayout;
  }
}