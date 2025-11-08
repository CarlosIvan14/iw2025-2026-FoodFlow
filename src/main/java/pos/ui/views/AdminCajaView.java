package pos.ui.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.service.CashService;
import pos.ui.MainLayout;

@PageTitle("Caja")
@Route(value="admin/caja", layout = MainLayout.class)
public class AdminCajaView extends VerticalLayout implements RouteGuard {
  public AdminCajaView(CashService cash){
    var h = new H2("Corte de caja (mock)");
    var c = cash.closeDayMock();
    add(h, new Span("Entradas: $"+c.cashIn()), new Span("Salidas: $"+c.cashOut()), new Span("Balance: $"+c.balance()));
  }
}
