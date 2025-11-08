package pos.ui.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.JsModule;

import pos.ui.MainLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "reports", layout = MainLayout.class)
@RouteAlias(value = "reports", layout = MainLayout.class)
@PageTitle("Análisis de negocio")
@NpmPackage(value = "chart.js", version = "4.4.4")
@JsModule("./charts-setup.js")
public class AnalyticsAdminView extends Composite<VerticalLayout> {

  @Override
  protected VerticalLayout initContent() {
    VerticalLayout root = new VerticalLayout();
    root.addClassNames(LumoUtility.Padding.MEDIUM);
    root.setSizeFull();

    root.add(new H2("Análisis de negocio (mock)"));

    // === Gráficas ===
    HtmlComponent salesCanvas = new HtmlComponent("canvas");
    salesCanvas.getElement().setProperty("id", "salesChart");
    salesCanvas.getElement().getStyle().set("max-width", "900px").set("height", "360px");
    root.add(salesCanvas);

    HtmlComponent roleCanvas = new HtmlComponent("canvas");
    roleCanvas.getElement().setProperty("id", "rolesChart");
    roleCanvas.getElement().getStyle().set("max-width", "900px").set("height", "360px");
    root.add(roleCanvas);

    // === Datos mock ===
    List<String> labels = Arrays.asList("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom");
    List<Integer> ventas = Arrays.stream(new int[]{18, 22, 30, 27, 35, 42, 25})
                                 .boxed().collect(Collectors.toList());
    List<String> roleLabels = Arrays.asList("Mesero", "Cocina", "Caja", "Admin");
    List<Integer> roleData = Arrays.stream(new int[]{120, 80, 60, 15})
                                   .boxed().collect(Collectors.toList());

    // === Convertir a Serializable (necesario para executeJs) ===
    Serializable labelsS = new ArrayList<>(labels);
    Serializable ventasS = new ArrayList<>(ventas);
    Serializable roleLabelsS = new ArrayList<>(roleLabels);
    Serializable roleDataS = new ArrayList<>(roleData);

    // === Ejecutar el JS para renderizar las gráficas ===
    UI.getCurrent().getPage().executeJs(
      "window.renderPOSCharts($0, $1, $2, $3, $4, $5)",
      "salesChart",
      labelsS,
      ventasS,
      "rolesChart",
      roleLabelsS,
      roleDataS
    );

    return root;
  }
}
