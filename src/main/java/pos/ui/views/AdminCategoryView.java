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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import pos.auth.RouteGuard;
import pos.domain.Category;
import pos.service.CategoryService;
import pos.ui.MainLayout;

@PageTitle("Categorías")
@Route(value = "admin/categorias", layout = MainLayout.class)
@pos.auth.RequiredRoles(pos.domain.Role.ADMIN)
public class AdminCategoryView extends VerticalLayout implements RouteGuard {

    private final CategoryService service;
    private final Grid<Category> grid;

    public AdminCategoryView(CategoryService service) {
        this.service = service;

        addClassName("categories-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        var title = new H2("Administrar Categorías de Productos");
        title.addClassName("categories-title");

        var addBtn = new Button("Nueva Categoría", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> showDialog(null));

        var header = new Div(title, addBtn);
        header.addClassName("categories-header");
        header.getStyle().set("display", "flex");
        header.getStyle().set("justify-content", "space-between");
        header.getStyle().set("align-items", "center");
        header.getStyle().set("width", "100%");

        grid = new Grid<>(Category.class, false);
        grid.addClassName("categories-grid");
        grid.addColumn(Category::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Category::getNombre).setHeader("Nombre");
        grid.addColumn(Category::getDescripcion).setHeader("Descripción");

        grid.addComponentColumn(category -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> showDialog(category));

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> showDeleteConfirmation(category));

            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Acciones");

        updateGrid();
        add(header, grid);
    }

    private void updateGrid() {
        grid.setItems(service.list());
    }

    private void showDialog(Category categoryToEdit) {
        Dialog dialog = new Dialog();
        boolean isEdit = categoryToEdit != null;
        dialog.setHeaderTitle(isEdit ? "Editar Categoría" : "Nueva Categoría");

        TextField nameField = new TextField("Nombre");
        nameField.setPlaceholder("Ej: Bebidas, Postres...");

        TextField descriptionField = new TextField("Descripción");
        descriptionField.setPlaceholder("Breve descripción de la categoría");

        if (isEdit) {
            nameField.setValue(categoryToEdit.getNombre());
            descriptionField.setValue(categoryToEdit.getDescripcion() != null ? categoryToEdit.getDescripcion() : "");
        }

        Button saveBtn = new Button("Guardar", e -> {
            if (nameField.isEmpty()) {
                showNotification("El nombre es obligatorio", true);
                return;
            }

            Category cat = Category.builder()
                    .id(isEdit ? categoryToEdit.getId() : null)
                    .nombre(nameField.getValue())
                    .descripcion(descriptionField.getValue())
                    .build();

            try {
                if (isEdit) {
                    service.update(categoryToEdit.getId(), cat);
                    showNotification("Categoría actualizada correctamente", false);
                } else {
                    service.create(cat);
                    showNotification("Categoría creada correctamente", false);
                }
                updateGrid();
                dialog.close();
            } catch (ObjectOptimisticLockingFailureException ex) {
                showNotification("Error: Alguien modificó este dato. Actualice la página.", true);
            } catch (Exception ex) {
                showNotification("Error al guardar: " + ex.getMessage(), true);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());

        VerticalLayout layout = new VerticalLayout(nameField, descriptionField);
        HorizontalLayout buttons = new HorizontalLayout(saveBtn, cancelBtn);

        dialog.add(layout, buttons);
        dialog.open();
    }

    private void showDeleteConfirmation(Category category) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirmar eliminación");
        dialog.add("¿Estás seguro de que deseas eliminar '" + category.getNombre() + "'?");

        Button confirmBtn = new Button("Eliminar", e -> {
            try {
                service.delete(category.getId());
                updateGrid();
                showNotification("Categoría eliminada", false);
                dialog.close();
            } catch (Exception ex) {
                showNotification("Error al eliminar: " + ex.getMessage(), true);
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
