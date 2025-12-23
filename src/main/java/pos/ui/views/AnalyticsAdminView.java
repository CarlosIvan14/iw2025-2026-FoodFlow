package pos.ui.views;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.Json;       // <--- IMPORTANTE
import elemental.json.JsonArray;  // <--- IMPORTANTE
import pos.auth.RouteGuard;
import pos.ui.MainLayout;

@PageTitle("Analytics")
@Route(value = "admin/analytics", layout = MainLayout.class)
@NpmPackage(value = "chart.js", version = "4.4.0")
@JsModule("./charts-setup.js")
public class AnalyticsAdminView extends VerticalLayout implements RouteGuard {

  public AnalyticsAdminView() {
    addClassName("analytics-view");
    setSizeFull();
    setAlignItems(Alignment.CENTER);

    var title = new H2("Análisis de Negocio");
    add(title);

    var chartsContainer = new Div();
    chartsContainer.setWidthFull();
    chartsContainer.setMaxWidth("800px");
    chartsContainer.getStyle().set("display", "flex");
    chartsContainer.getStyle().set("flex-wrap", "wrap");
    chartsContainer.getStyle().set("gap", "20px");
    chartsContainer.getStyle().set("justify-content", "center");

    HtmlComponent salesCanvas = new HtmlComponent("canvas");
    salesCanvas.setId("salesChart");
    salesCanvas.getStyle().set("max-width", "400px");
    salesCanvas.getStyle().set("max-height", "300px");

    HtmlComponent rolesCanvas = new HtmlComponent("canvas");
    rolesCanvas.setId("rolesChart");
    rolesCanvas.getStyle().set("max-width", "300px");
    rolesCanvas.getStyle().set("max-height", "300px");

    chartsContainer.add(salesCanvas, rolesCanvas);
    add(chartsContainer);

    addAttachListener(e -> renderCharts());
  }

  private void renderCharts() {
    // Dados brutos
    String[] daysRaw = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
    Integer[] salesRaw = {180, 220, 300, 270, 350, 420, 250};

    String[] rolesRaw = {"Comida", "Bebida", "Postre"};
    Integer[] roleDataRaw = {65, 25, 10};

    // 1. Converter para JsonArray (O formato nativo do Vaadin)
    // Isso resolve o erro "Can't encode class..."
    JsonArray daysJson = toJsonArray(daysRaw);
    JsonArray salesJson = toJsonArray(salesRaw);
    JsonArray rolesJson = toJsonArray(rolesRaw);
    JsonArray roleDataJson = toJsonArray(roleDataRaw);

    // 2. Enviar para o JS
    UI.getCurrent().getPage().executeJs(
            "window.renderPOSCharts($0, $1, $2, $3, $4, $5)",
            "salesChart",
            daysJson,      // Passando JsonArray
            salesJson,     // Passando JsonArray
            "rolesChart",
            rolesJson,     // Passando JsonArray
            roleDataJson   // Passando JsonArray
    );
  }

  // --- Métodos Auxiliares para Converter Dados em JSON Seguro ---

  // Converte Array de String para JsonArray
  private JsonArray toJsonArray(String[] data) {
    JsonArray array = Json.createArray();
    for (int i = 0; i < data.length; i++) {
      array.set(i, data[i]);
    }
    return array;
  }

  // Converte Array de Integer para JsonArray
  private JsonArray toJsonArray(Integer[] data) {
    JsonArray array = Json.createArray();
    for (int i = 0; i < data.length; i++) {
      array.set(i, data[i]);
    }
    return array;
  }
}