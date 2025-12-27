package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import pos.auth.RouteGuard;
import pos.domain.Ingredient;
import pos.service.IngredientService;
import pos.ui.MainLayout;

@PageTitle("Ingredientes")
@Route(value = "admin/ingredientes", layout = MainLayout.class)
public class AdminIngredientView extends VerticalLayout implements RouteGuard {

    private final IngredientService service;
    private final Grid<Ingredient> grid;

    public AdminIngredientView(IngredientService service) {
        this.service = service;

        addClassName("ingredients-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        // Cabeçalho
        var title = new H2("Gestión de Ingredientes");
        title.addClassName("ingredients-title");

        var addBtn = new Button("Nuevo Ingrediente", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> showDialog(null));

        var header = new Div(title, addBtn);
        header.addClassName("ingredients-header");
        header.getStyle().set("display", "flex");
        header.getStyle().set("justify-content", "space-between");
        header.getStyle().set("align-items", "center");
        header.getStyle().set("width", "100%");

        // Grid
        grid = new Grid<>(Ingredient.class, false);
        grid.addClassName("ingredients-grid");
        grid.addColumn(Ingredient::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Ingredient::getNombre).setHeader("Nombre");
        grid.addColumn(Ingredient::getUnidad).setHeader("Unidad").setAutoWidth(true);
        grid.addColumn(Ingredient::getStockActual).setHeader("Stock").setAutoWidth(true);
        grid.addColumn(i -> "€ " + i.getCostoUnitario()).setHeader("Costo Unit.").setAutoWidth(true);

        // Coluna de Ações
        grid.addComponentColumn(ingredient -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> showDialog(ingredient));

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> showDeleteConfirmation(ingredient));

            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Acciones");

        updateGrid();
        add(header, grid);
    }

    private void updateGrid() {
        grid.setItems(service.list());
    }

    private void showDialog(Ingredient ingredientToEdit) {
        Dialog dialog = new Dialog();
        boolean isEdit = ingredientToEdit != null;
        dialog.setHeaderTitle(isEdit ? "Editar Ingrediente" : "Nuevo Ingrediente");

        // Campos
        TextField nameField = new TextField("Nombre");
        nameField.setPlaceholder("Ej: Harina, Tomate...");

        ComboBox<String> unitField = new ComboBox<>("Unidad");
        unitField.setItems("kg", "gr", "L", "ml", "unidad", "paquete");

        NumberField stockField = new NumberField("Stock Actual");
        BigDecimalField costField = new BigDecimalField("Costo Unitario (€)");

        // Preencher se for edição
        if (isEdit) {
            nameField.setValue(ingredientToEdit.getNombre());
            unitField.setValue(ingredientToEdit.getUnidad());
            stockField.setValue(ingredientToEdit.getStockActual());
            costField.setValue(ingredientToEdit.getCostoUnitario());
        } else {
            stockField.setValue(0.0);
        }

        // Botões
        Button saveBtn = new Button("Guardar", e -> {
            if (nameField.isEmpty() || unitField.isEmpty() || stockField.isEmpty() || costField.isEmpty()) {
                showNotification("Todos los campos son obligatorios", true);
                return;
            }

            Ingredient ing = Ingredient.builder()
                    .id(isEdit ? ingredientToEdit.getId() : null)
                    .nombre(nameField.getValue())
                    .unidad(unitField.getValue())
                    .stockActual(stockField.getValue())
                    .costoUnitario(costField.getValue())
                    .build();

            try {
                service.save(ing);
                updateGrid();
                dialog.close();
                showNotification(isEdit ? "Ingrediente actualizado correctamente" : "Ingrediente creado correctamente", false);
            } catch (ObjectOptimisticLockingFailureException ex) {
                showNotification("Error: Alguien modificó este dato. Actualice la página.", true);
            } catch (Exception ex) {
                showNotification("Error al guardar: " + ex.getMessage(), true);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());

        VerticalLayout layout = new VerticalLayout(nameField, unitField, stockField, costField);
        HorizontalLayout buttons = new HorizontalLayout(saveBtn, cancelBtn);

        dialog.add(layout, buttons);
        dialog.open();
    }

    // Diálogo de confirmação para exclusão
    private void showDeleteConfirmation(Ingredient ingredient) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirmar eliminación");
        dialog.add("¿Estás seguro de que deseas eliminar '" + ingredient.getNombre() + "'?");

        Button confirmBtn = new Button("Eliminar", e -> {
            try {
                service.delete(ingredient.getId());
                updateGrid();
                showNotification("Ingrediente eliminado", false);
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