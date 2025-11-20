package pos.ui.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.JsModule;
import pos.ui.MainLayout;
import pos.auth.RouteGuard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "admin/analytics", layout = MainLayout.class)
@PageTitle("Analytics")
@NpmPackage(value = "chart.js", version = "4.4.4")
@JsModule("./charts-setup.js")
public class AnalyticsAdminView extends VerticalLayout implements RouteGuard {

 @Override
  protected VerticalLayout initContent() {
    VerticalLayout root = new VerticalLayout();
    root.addClassName("analytics-view");
    root.setSizeFull();
    root.setAlignItems(VerticalLayout.Alignment.CENTER);
    root.setJustifyContentMode(VerticalLayout.JustifyContentMode.START);

    // === Título ===
    var title = new H2("Análisis de Negocio (Mock)");
    title.addClassName("analytics-title");
    root.add(title);

    // === Contenedor de tarjetas de gráficas ===
    var chartsContainer = new Div();
    chartsContainer.addClassName("analytics-charts");

    // === Gráfica de ventas ===
    HtmlComponent salesCanvas = new HtmlComponent("canvas");
    salesCanvas.getElement().setProperty("id", "salesChart");
    salesCanvas.addClassName("chart-canvas");
    chartsContainer.add(salesCanvas);

    // === Gráfica por roles ===
    HtmlComponent rolesCanvas = new HtmlComponent("canvas");
    rolesCanvas.getElement().setProperty("id", "rolesChart");
    rolesCanvas.addClassName("chart-canvas");
    chartsContainer.add(rolesCanvas);

    root.add(chartsContainer);

    // === Datos mock ===
    List<String> labels = Arrays.asList("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom");
    List<Integer> ventas = Arrays.stream(new int[]{18, 22, 30, 27, 35, 42, 25})
                                 .boxed().collect(Collectors.toList());
    List<String> roleLabels = Arrays.asList("Mesero", "Cocina", "Caja", "Admin");
    List<Integer> roleData = Arrays.stream(new int[]{120, 80, 60, 15})
                                   .boxed().collect(Collectors.toList());

    Serializable labelsS = new ArrayList<>(labels);
    Serializable ventasS = new ArrayList<>(ventas);
    Serializable roleLabelsS = new ArrayList<>(roleLabels);
    Serializable roleDataS = new ArrayList<>(roleData);

    // === Renderizar las gráficas con JS ===
    UI.getCurrent().getPage().executeJs(
      "window.renderPOSCharts($0, $1, $2, $3, $4, $5)",
      "salesChart", labelsS, ventasS,
      "rolesChart", roleLabelsS, roleDataS
    );

    return root;
  }
}
