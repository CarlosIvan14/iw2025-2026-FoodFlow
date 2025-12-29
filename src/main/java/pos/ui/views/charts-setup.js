// src/main/frontend/charts-setup.js
import Chart from "chart.js/auto";

window.__posCharts = window.__posCharts || {};

window.renderPOSCharts = (salesId, labels, data, rolesId, roleLabels, roleData) => {
  // helpers
  const getCanvas = (id) => document.getElementById(id);
  const destroyIfExists = (key) => {
    if (window.__posCharts[key]) {
      window.__posCharts[key].destroy();
      window.__posCharts[key] = null;
    }
  };

  const salesCanvas = getCanvas(salesId);
  const rolesCanvas = getCanvas(rolesId);
  if (!salesCanvas || !rolesCanvas) return;

  // Si el canvas está oculto o sin tamaño, Chart.js no dibuja bien
  const salesRect = salesCanvas.getBoundingClientRect();
  const rolesRect = rolesCanvas.getBoundingClientRect();
  if (salesRect.width === 0 || rolesRect.width === 0) {
    requestAnimationFrame(() =>
      window.renderPOSCharts(salesId, labels, data, rolesId, roleLabels, roleData)
    );
    return;
  }

  destroyIfExists("sales");
  destroyIfExists("roles");

  window.__posCharts.sales = new Chart(salesCanvas, {
    type: "bar",
    data: {
      labels: Array.from(labels),
      datasets: [{ label: "Ventas", data: Array.from(data) }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
    },
  });

  window.__posCharts.roles = new Chart(rolesCanvas, {
    type: "doughnut",
    data: {
      labels: Array.from(roleLabels),
      datasets: [{ data: Array.from(roleData) }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
    },
  });
};
