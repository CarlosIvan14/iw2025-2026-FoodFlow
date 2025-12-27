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

        // Cabeçalho
        var title = new H2("Gestión de Ingredientes");
        var addBtn = new Button("Nuevo Ingrediente", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> showDialog(null));

        var header = new Div(title, addBtn);
        header.getStyle().set("display", "flex").set("justify-content", "space-between").set("width", "100%");

        // Grid
        grid = new Grid<>(Ingredient.class, false);
        grid.addColumn(Ingredient::getNombre).setHeader("Nombre");
        grid.addColumn(Ingredient::getUnidad).setHeader("Unidad").setAutoWidth(true);
        grid.addColumn(Ingredient::getStockActual).setHeader("Stock").setAutoWidth(true);
        grid.addColumn(i -> "€ " + i.getCostoUnitario()).setHeader("Costo Unit.");

        // Coluna de Ações
        grid.addComponentColumn(ingredient -> {
            Button edit = new Button(VaadinIcon.EDIT.create(), e -> showDialog(ingredient));
            edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button delete = new Button(VaadinIcon.TRASH.create(), e -> deleteIngredient(ingredient));
            delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

            return new HorizontalLayout(edit, delete);
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
        TextField nameField = new TextField("Nombre (Ej: Harina)");
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
        }

        // Botões
        Button saveBtn = new Button("Guardar", e -> {
            if (nameField.isEmpty() || unitField.isEmpty() || stockField.isEmpty() || costField.isEmpty()) {
                Notification.show("Todos los campos son obligatorios", 3000, Notification.Position.MIDDLE);
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
                Notification.show("Guardado correctamente", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (ObjectOptimisticLockingFailureException ex) {
                Notification.show("Error: Alguien modificó este dato. Actualice la página.", 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancelar", e -> dialog.close());

        VerticalLayout layout = new VerticalLayout(nameField, unitField, stockField, costField);
        dialog.add(layout);
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void deleteIngredient(Ingredient ingredient) {
        service.delete(ingredient.getId());
        updateGrid();
        Notification.show("Ingrediente eliminado").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}