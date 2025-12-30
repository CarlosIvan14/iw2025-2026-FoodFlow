import Chart from "chart.js/auto";

window.__posCharts = window.__posCharts || {};

const destroyIfExists = (key) => {
  if (window.__posCharts[key]) {
    window.__posCharts[key].destroy();
    window.__posCharts[key] = null;
  }
};

window.renderSalesCharts = (
  dateCanvasId,
  title1,
  dateLabels,
  dateData,
  productCanvasId,
  title2,
  productLabels,
  productData
) => {
  const dateCanvas = document.getElementById(dateCanvasId);
  const prodCanvas = document.getElementById(productCanvasId);
  if (!dateCanvas || !prodCanvas) return;

  const r1 = dateCanvas.getBoundingClientRect();
  const r2 = prodCanvas.getBoundingClientRect();
  if (r1.width === 0 || r2.width === 0) {
    requestAnimationFrame(() =>
      window.renderSalesCharts(
        dateCanvasId, title1, dateLabels, dateData,
        productCanvasId, title2, productLabels, productData
      )
    );
    return;
  }

  destroyIfExists("salesByDate");
  destroyIfExists("salesByProduct");

  window.__posCharts.salesByDate = new Chart(dateCanvas, {
    type: "line",
    data: {
      labels: Array.from(dateLabels),
      datasets: [{ label: title1, data: Array.from(dateData) }],
    },
    options: { responsive: true, maintainAspectRatio: false },
  });

  window.__posCharts.salesByProduct = new Chart(prodCanvas, {
    type: "bar",
    data: {
      labels: Array.from(productLabels),
      datasets: [{ label: title2, data: Array.from(productData) }],
    },
    options: { responsive: true, maintainAspectRatio: false },
  });
};
