// src/main/frontend/charts-setup.js

import Chart from 'chart.js/auto';

window.renderPOSCharts = function (idSales, labelsSales, dataSales, idRoles, labelsRoles, dataRoles) {

    // 1. Gráfico de Barras (Vendas Semanal)
    const ctxSales = document.getElementById(idSales);
    if (ctxSales) {
        // Destruir gráfico antigo se existir para não sobrepor
        if (ctxSales.chart) ctxSales.chart.destroy();

        ctxSales.chart = new Chart(ctxSales, {
            type: 'bar',
            data: {
                labels: labelsSales,
                datasets: [{
                    label: 'Vendas ($)',
                    data: dataSales,
                    backgroundColor: 'rgba(54, 162, 235, 0.6)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                scales: { y: { beginAtZero: true } }
            }
        });
    }

    // 2. Gráfico de Pizza (Pedidos por Função/Categoria)
    const ctxRoles = document.getElementById(idRoles);
    if (ctxRoles) {
        if (ctxRoles.chart) ctxRoles.chart.destroy();

        ctxRoles.chart = new Chart(ctxRoles, {
            type: 'doughnut',
            data: {
                labels: labelsRoles,
                datasets: [{
                    label: 'Actividad',
                    data: dataRoles,
                    backgroundColor: [
                        '#FF6384',
                        '#36A2EB',
                        '#FFCE56',
                        '#4BC0C0'
                    ],
                    hoverOffset: 4
                }]
            }
        });
    }
}