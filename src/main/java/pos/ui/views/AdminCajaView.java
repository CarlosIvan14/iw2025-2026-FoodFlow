package pos.ui.views;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.EmailField;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.domain.PaymentMethod;
import pos.service.CashService;
import pos.service.OrderService;
import pos.ui.MainLayout;

import java.math.BigDecimal;
import java.util.List;



@PageTitle("Caja")
@Route(value = "admin/caja", layout = MainLayout.class)
public class AdminCajaView extends VerticalLayout implements RouteGuard {

  private final OrderService orderService;
  private final FlexLayout cardsContainer;

  public AdminCajaView(CashService cash, OrderService orderService) {
    this.orderService = orderService;

    addClassName("caja-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);

    var title = new H2("Caja & Pagos");
    var subTitle = new H3("Mesas Listas para Cobrar");

    cardsContainer = new FlexLayout();
    cardsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
    cardsContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
    cardsContainer.getStyle().set("gap", "20px");
    cardsContainer.setWidthFull();

    refreshCards();

    add(title, subTitle, cardsContainer);
  }

  private void refreshCards() {
    cardsContainer.removeAll();
    List<Order> ordersToPay = orderService.readyToPayQueue();

    if (ordersToPay.isEmpty()) {
      Span noOrders = new Span("No hay mesas pendientes de cobro.");
      noOrders.getStyle().set("color", "gray");
      cardsContainer.add(noOrders);
      return;
    }

    for (Order order : ordersToPay) {
      cardsContainer.add(createTableCard(order));
    }
  }

  private Div createTableCard(Order order) {
    Div card = new Div();
    // Estilos CSS do Card (Mantidos iguais)
    card.getStyle().set("background-color", "white");
    card.getStyle().set("border-radius", "12px");
    card.getStyle().set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)");
    card.getStyle().set("padding", "20px");
    card.getStyle().set("width", "220px");
    card.getStyle().set("display", "flex");
    card.getStyle().set("flex-direction", "column");
    card.getStyle().set("align-items", "center");
    card.getStyle().set("gap", "10px");

    String mesaTexto = (order.getTableId() != null) ? "MESA " + order.getTableId() : "DELIVERY";
    H3 mesaTitle = new H3(mesaTexto);
    mesaTitle.getStyle().set("margin", "0");

    Span totalLabel = new Span("Total a Pagar");
    totalLabel.getStyle().set("font-size", "0.8rem");
    totalLabel.getStyle().set("color", "#666");

    Span totalValue = new Span("$" + order.getTotal());
    totalValue.getStyle().set("font-size", "1.8rem");
    totalValue.getStyle().set("font-weight", "bold");
    totalValue.getStyle().set("color", "#28a745");

    Button btnCobrar = new Button("Cobrar", new Icon(VaadinIcon.DOLLAR));
    btnCobrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    btnCobrar.setWidthFull();

    // AQUI ESTÁ A MUDANÇA: Abre o Dialog em vez de cobrar direto
    btnCobrar.addClickListener(e -> showPaymentDialog(order));

    card.add(mesaTitle, totalLabel, totalValue, btnCobrar);
    return card;
  }

  // --- NOVA FUNCIONALIDADE: DIÁLOGO DE PAGAMENTO ---
  private void showPaymentDialog(Order order) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Cobrar " + (order.getTableId() != null ? "Mesa " + order.getTableId() : "Orden"));

    // --- 1. DEFINIÇÃO DAS VARIÁVEIS VISUAIS (Tudo aqui no topo) ---

    // Total
    H2 totalDisplay = new H2("€ " + order.getTotal());
    totalDisplay.getStyle().set("color", "#28a745").set("align-self", "center");

    // Combo de Método
    ComboBox<PaymentMethod> methodSelect = new ComboBox<>("Método de Pago");
    methodSelect.setItems(PaymentMethod.values());
    methodSelect.setValue(PaymentMethod.CASH);
    methodSelect.setWidthFull();

    // Campos Numéricos
    BigDecimalField receivedField = new BigDecimalField("Monto Recibido (€)");
    receivedField.setValue(order.getTotal());
    receivedField.setWidthFull();
    receivedField.setClearButtonVisible(true);

    BigDecimalField tipField = new BigDecimalField("Propina / Gorjeta (€)");
    tipField.setValue(BigDecimal.ZERO);
    tipField.setWidthFull();

    BigDecimalField changeField = new BigDecimalField("Cambio / Troco (€)");
    changeField.setReadOnly(true);
    changeField.setWidthFull();
    changeField.setValue(BigDecimal.ZERO);

    // CAMPOS DE EMAIL (Definidos aqui para não dar erro de "symbol not found")
    Checkbox sendReceiptCheck = new Checkbox("¿Enviar recibo por correo?");

    EmailField emailField = new EmailField("Correo del Cliente");
    emailField.setPlaceholder("cliente@email.com");
    emailField.setWidthFull();
    emailField.setVisible(false); // Começa invisível
    emailField.setClearButtonVisible(true);
    emailField.setErrorMessage("Correo inválido");

    // --- 2. LÓGICA DE INTERFACE ---

    // Mostrar/Esconder email (Agora funciona porque emailField já existe)
    sendReceiptCheck.addValueChangeListener(e -> {
      emailField.setVisible(e.getValue());
      if (e.getValue()) {
        emailField.focus();
      }
    });

    // Calcular Troco
    receivedField.addValueChangeListener(e -> {
      BigDecimal received = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
      BigDecimal total = order.getTotal();

      if (received.compareTo(total) >= 0) {
        changeField.setValue(received.subtract(total));
        receivedField.setInvalid(false);
      } else {
        changeField.setValue(BigDecimal.ZERO);
        if (methodSelect.getValue() == PaymentMethod.CASH) {
          receivedField.setInvalid(true);
          receivedField.setErrorMessage("Monto insuficiente");
        }
      }
    });

    Button btnExact = new Button("Valor Exacto", e -> receivedField.setValue(order.getTotal()));
    btnExact.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

    // Mudar Dinheiro/Cartão
    methodSelect.addValueChangeListener(e -> {
      boolean isCash = e.getValue() == PaymentMethod.CASH;
      receivedField.setVisible(isCash);
      changeField.setVisible(isCash);
      btnExact.setVisible(isCash);
      if (!isCash) receivedField.setValue(order.getTotal());
    });

    // --- 3. BOTÕES E AÇÃO FINAL ---

    Button btnCancel = new Button("Cancelar", e -> dialog.close());

    Button btnConfirm = new Button("Confirmar Pago", new Icon(VaadinIcon.CHECK));
    btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
    btnConfirm.addClickShortcut(Key.ENTER);

    // A Lógica do Clique
    btnConfirm.addClickListener(e -> {
      BigDecimal received = receivedField.getValue();
      BigDecimal total = order.getTotal();

      // Validação de valor
      if (received == null || received.compareTo(total) < 0) {
        Notification.show("Monto insuficiente", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        return;
      }

      // Validação do Email e criação da variável 'emailToSend'
      String emailToSend = null; // Começa nulo

      if (sendReceiptCheck.getValue()) {
        // Se marcou o checkbox, validamos o campo
        if (emailField.isEmpty() || emailField.isInvalid()) {
          Notification.show("Por favor, escriba un correo válido", 3000, Notification.Position.MIDDLE)
                  .addThemeVariants(NotificationVariant.LUMO_ERROR);
          return; // Para tudo se o email estiver errado
        }
        // Se tudo ok, pega o valor
        emailToSend = emailField.getValue();
      }

      try {
        // Agora 'emailToSend' existe e pode ser passado
        orderService.processPayment(
                order.getId(),
                methodSelect.getValue(),
                received,
                tipField.getValue(),
                emailToSend
        );

        Notification.show("Pago realizado con éxito!", 3000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        dialog.close();
        refreshCards();

      } catch (Exception ex) {
        Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        ex.printStackTrace();
      }
    });

    // --- 4. MONTAGEM DO LAYOUT ---
    VerticalLayout layout = new VerticalLayout(
            totalDisplay,
            methodSelect,
            new HorizontalLayout(receivedField, btnExact),
            changeField,
            tipField,
            sendReceiptCheck, // Checkbox adicionado ao layout
            emailField        // Campo de Email adicionado ao layout
    );
    layout.setSpacing(true);
    layout.setPadding(false);

    dialog.add(layout);
    dialog.getFooter().add(btnCancel, btnConfirm);
    dialog.open();
  }
}