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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import pos.auth.RouteGuard;
import pos.domain.Role;
import pos.domain.User;
import pos.service.UserService;
import pos.ui.MainLayout;

import java.lang.reflect.Method;

@PageTitle("Usuarios")
@Route(value = "admin/usuarios", layout = MainLayout.class)
@pos.auth.RequiredRoles(pos.domain.Role.ADMIN)
public class AdminUserView extends VerticalLayout implements RouteGuard {

  private final UserService userService;
  private final Grid<User> grid;

  public AdminUserView(UserService userService) {
    this.userService = userService;

    addClassName("users-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);
    setAlignItems(Alignment.STRETCH);
    setJustifyContentMode(JustifyContentMode.START);

    // Encabezado
    var title = new H2("Gestión de Usuarios");
    title.addClassName("users-title");

    var addBtn = new Button("Nuevo Usuario", VaadinIcon.PLUS.create());
    addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    addBtn.addClickListener(e -> showUserDialog(null));

    var header = new Div(title, addBtn);
    header.addClassName("users-header");
    header.getStyle().set("display", "flex");
    header.getStyle().set("justify-content", "space-between");
    header.getStyle().set("align-items", "center");
    header.getStyle().set("width", "100%");

    // Grid de Usuarios
    grid = new Grid<>(User.class, false);
    grid.addClassName("users-grid");
    grid.setWidthFull();

    grid.addColumn(u -> safe(invokeAny(u, "getId")))
        .setHeader("ID").setAutoWidth(true).setFlexGrow(0);

    grid.addColumn(u -> safe(firstNonBlank(
            asString(invokeAny(u, "getNombre")),
            asString(invokeAny(u, "getName"))
        )))
        .setHeader("Nombre").setAutoWidth(true);

    grid.addColumn(u -> safe(asString(invokeAny(u, "getEmail"))))
        .setHeader("Email").setAutoWidth(true);

    grid.addColumn(u -> safe(asString(invokeAny(u, "getRole"))))
        .setHeader("Rol").setAutoWidth(true);

    grid.addColumn(u -> isActive(u) ? "✓ Activo" : "✗ Inactivo")
        .setHeader("Estado").setAutoWidth(true);

    // Columna de acciones
    grid.addComponentColumn(user -> {
      Button editBtn = new Button(VaadinIcon.EDIT.create());
      editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      editBtn.addClickListener(e -> showUserDialog(user));

      Button deleteBtn = new Button(VaadinIcon.TRASH.create());
      deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
      deleteBtn.addClickListener(e -> showDeleteConfirmation(user));

      var wrap = new HorizontalLayout(editBtn, deleteBtn);
      wrap.setPadding(false);
      wrap.setSpacing(true);
      return wrap;
    }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);

    updateGrid();
    add(header, grid);
  }

  private void updateGrid() {
    grid.setItems(userService.list());
  }

  private void showUserDialog(User userToEdit) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle(userToEdit == null ? "Nuevo Usuario" : "Editar Usuario");

    var form = new VerticalLayout();
    form.setSpacing(true);
    form.setPadding(false);
    form.setWidth("420px");

    var nameField = new TextField("Nombre");
    nameField.setWidthFull();

    var emailField = new EmailField("Email");
    emailField.setWidthFull();

    var passwordField = new PasswordField("Contraseña");
    passwordField.setWidthFull();

    var roleCombo = new ComboBox<Role>("Rol");
    roleCombo.setItems(Role.values());
    roleCombo.setItemLabelGenerator(Role::name);
    roleCombo.setWidthFull();

    if (userToEdit != null) {
      nameField.setValue(firstNonBlank(
          asString(invokeAny(userToEdit, "getNombre")),
          asString(invokeAny(userToEdit, "getName"))
      ));

      emailField.setValue(safe(asString(invokeAny(userToEdit, "getEmail"))));

      Object r = invokeAny(userToEdit, "getRole");
      if (r instanceof Role) roleCombo.setValue((Role) r);

      passwordField.setEnabled(true);
      passwordField.setPlaceholder("Dejar vacío para no cambiar");
    } else {
      passwordField.setPlaceholder("Requerida para nuevo usuario");
    }

    form.add(nameField, emailField, passwordField, roleCombo);
    dialog.add(form);

    var saveBtn = new Button("Guardar", e -> {
      if (nameField.isEmpty() || emailField.isEmpty() || roleCombo.isEmpty()) {
        showNotification("Por favor complete todos los campos requeridos.", true);
        return;
      }
      if (userToEdit == null && passwordField.isEmpty()) {
        showNotification("Contraseña requerida para nuevo usuario.", true);
        return;
      }

      try {
        User u = (userToEdit != null) ? userToEdit : new User();

        // set nombre/name
        setAny(u, "setNombre", nameField.getValue());
        setAny(u, "setName", nameField.getValue());

        // set email
        setAny(u, "setEmail", emailField.getValue());

        // set role
        setAny(u, "setRole", roleCombo.getValue());

        // password solo si viene
        if (!passwordField.isEmpty()) {
          String pw = passwordField.getValue();
          setAny(u, "setPassword", pw);
          setAny(u, "setContrasena", pw);
          setAny(u, "setContraseña", pw); // por si existe exactamente así
        }

        // activo por defecto al crear
        if (userToEdit == null) {
          setAny(u, "setActivo", true);
          setAny(u, "setActive", true);
          setAny(u, "setEnabled", true);
        }

        userService.save(u);

        updateGrid();
        showNotification("Usuario " + (userToEdit == null ? "creado" : "actualizado") + " exitosamente.", false);
        dialog.close();

      } catch (ObjectOptimisticLockingFailureException ex) {
        showNotification("El usuario fue modificado por otro usuario. Intente nuevamente.", true);
      } catch (Exception ex) {
        showNotification("Error: " + ex.getMessage(), true);
      }
    });
    saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    var cancelBtn = new Button("Cancelar", e -> dialog.close());
    cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    dialog.getFooter().add(cancelBtn, saveBtn);
    dialog.open();
  }

  private void showDeleteConfirmation(User user) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Confirmar eliminación");

    String nombre = firstNonBlank(
        asString(invokeAny(user, "getNombre")),
        asString(invokeAny(user, "getName")),
        "este usuario"
    );

    dialog.add("¿Estás seguro de que deseas eliminar el usuario '" + nombre + "'?");

    var confirmBtn = new Button("Eliminar", e -> {
      try {
        Object id = invokeAny(user, "getId");
        if (id == null) {
          showNotification("No se pudo eliminar: ID nulo.", true);
          return;
        }
        userService.delete((Long) id);
        updateGrid();
        showNotification("Usuario eliminado.", false);
        dialog.close();
      } catch (ClassCastException cce) {
        showNotification("No se pudo eliminar: tipo de ID inesperado.", true);
      } catch (Exception ex) {
        showNotification("Error al eliminar: " + ex.getMessage(), true);
      }
    });
    confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

    var cancelBtn = new Button("Cancelar", e -> dialog.close());
    cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    dialog.getFooter().add(cancelBtn, confirmBtn);
    dialog.open();
  }

  private void showNotification(String text, boolean isError) {
    Notification n = Notification.show(text, 3500, Notification.Position.TOP_CENTER);
    n.addThemeVariants(isError ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_SUCCESS);
  }

  /* =======================
     Helpers (reflexión segura)
     ======================= */

  private boolean isActive(User u) {
    Object v = invokeAny(u, "getActivo");
    if (v instanceof Boolean) return (Boolean) v;
    v = invokeAny(u, "isActivo");
    if (v instanceof Boolean) return (Boolean) v;
    v = invokeAny(u, "getActive");
    if (v instanceof Boolean) return (Boolean) v;
    v = invokeAny(u, "isActive");
    if (v instanceof Boolean) return (Boolean) v;
    v = invokeAny(u, "isEnabled");
    if (v instanceof Boolean) return (Boolean) v;
    return true; // default
  }

  private Object invokeAny(Object target, String methodName) {
    try {
      Method m = target.getClass().getMethod(methodName);
      return m.invoke(target);
    } catch (Exception ignored) {
      return null;
    }
  }

  private void setAny(Object target, String methodName, Object arg) {
    try {
      for (Method m : target.getClass().getMethods()) {
        if (!m.getName().equals(methodName)) continue;
        if (m.getParameterCount() != 1) continue;
        Class<?> p = m.getParameterTypes()[0];
        if (arg == null || p.isAssignableFrom(arg.getClass()) ||
            (p == boolean.class && arg instanceof Boolean) ||
            (p == long.class && arg instanceof Long)) {
          m.invoke(target, arg);
          return;
        }
      }
    } catch (Exception ignored) {}
  }

  private String asString(Object v) {
    return v == null ? null : String.valueOf(v);
  }

  private String safe(String v) {
    return v == null ? "" : v;
  }

  private String firstNonBlank(String... vals) {
    if (vals == null) return "";
    for (String v : vals) {
      if (v != null && !v.trim().isBlank()) return v.trim();
    }
    return "";
  }

  private String safe(Object v) {
    return v == null ? "—" : String.valueOf(v);
  }
}
