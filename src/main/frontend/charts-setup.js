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
  productData,
  selectedProductName = null
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
        productCanvasId, title2, productLabels, productData,
        selectedProductName
      )
    );
    return;
  }

  destroyIfExists("salesByDate");
  destroyIfExists("salesByProduct");

  // Colores para la gráfica de pastel
  const colors = [
    "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
    "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B195", "#A3E4D7"
  ];

  const productLabelsArray = Array.from(productLabels);
  const productDataArray = Array.from(productData);
  
  // Generar colores con énfasis en el producto seleccionado
  const backgroundColor = productLabelsArray.map((label, idx) => {
    if (selectedProductName && label === selectedProductName) {
      return colors[idx % colors.length]; // Color normal para resaltado
    }
    return colors[idx % colors.length];
  });

  const borderColor = productLabelsArray.map((label, idx) => {
    if (selectedProductName && label === selectedProductName) {
      return "#000"; // Borde negro para resaltar
    }
    return "#fff";
  });

  const borderWidth = productLabelsArray.map((label) => {
    if (selectedProductName && label === selectedProductName) {
      return 3; // Borde más grueso para resaltar
    }
    return 1;
  });

  window.__posCharts.salesByDate = new Chart(dateCanvas, {
    type: "line",
    data: {
      labels: Array.from(dateLabels),
      datasets: [{ 
        label: title1, 
        data: Array.from(dateData),
        borderColor: "#4ECDC4",
        backgroundColor: "rgba(78, 205, 196, 0.1)",
        tension: 0.4,
        fill: true
      }],
    },
    options: { 
      responsive: true, 
      maintainAspectRatio: false,
      plugins: {
        legend: { display: true }
      }
    },
  });

  window.__posCharts.salesByProduct = new Chart(prodCanvas, {
    type: "doughnut",
    data: {
      labels: productLabelsArray,
      datasets: [{
        label: title2,
        data: productDataArray,
        backgroundColor: backgroundColor,
        borderColor: borderColor,
        borderWidth: borderWidth
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { 
          position: "right",
          labels: {
            padding: 15,
            font: { size: 12 }
          }
        },
        tooltip: {
          callbacks: {
            label: function(context) {
              const label = context.label || '';
              const value = context.parsed || 0;
              return label + ': ' + Math.round(value) + ' unidades';
            }
          }
        }
      },
    },
  });
};

