import {
  Chart,
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend,
  DoughnutController,
  ArcElement
} from "chart.js";

Chart.register(
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend,
  DoughnutController,
  ArcElement
);

const registry = {};
function mountChart(id, cfg) {
  const el = document.getElementById(id);
  if (!el) return;
  const ctx = el.getContext("2d");
  if (registry[id]) registry[id].destroy();
  registry[id] = new Chart(ctx, cfg);
}

window.renderPOSCharts = (salesId, labels, data, rolesId, roleLabels, roleData) => {
  // Gráfica de barras (ventas)
  mountChart(salesId, {
    type: "bar",
    data: {
      labels: labels,
      datasets: [{ label: "Ventas", data: data }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: { y: { beginAtZero: true } }
    }
  });

  // Gráfica de roles
  mountChart(rolesId, {
    type: "doughnut",
    data: {
      labels: roleLabels,
      datasets: [{ data: roleData }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { position: "bottom" } }
    }
  });
};
