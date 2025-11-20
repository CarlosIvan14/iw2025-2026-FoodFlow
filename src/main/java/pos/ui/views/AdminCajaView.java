package pos.ui.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.ui.MainLayout;

@PageTitle("Caja")
@Route(value = "admin/caja", layout = MainLayout.class)
public class AdminCajaView extends VerticalLayout implements RouteGuard {

  public AdminCajaView(CashService cash) {
    addClassName("caja-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.START);

    var title = new H2("Corte de Caja (Mock)");
    title.addClassName("caja-title");

    var card = new Div();
    card.addClassName("caja-card");

    var c = cash.closeDayMock();

    var entradas = new Span("Entradas: $" + c.cashIn());
    entradas.addClassName("caja-item");

    var salidas = new Span("Salidas: $" + c.cashOut());
    salidas.addClassName("caja-item");

    var balance = new Span("Balance: $" + c.balance());
    balance.addClassName("caja-balance");

    card.add(entradas, salidas, balance);
    add(title, card);
  }
}
