$(document).ready(function() {
    // Get CSRF token for AJAX requests
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    
    // Set up AJAX to send CSRF token with all requests
    $.ajaxSetup({
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        }
    });
    
    // Initialize charts
    let categoryChart = null;
    let monthlyChart = null;
    
    // Populate year filter with last 5 years
    const currentYear = new Date().getFullYear();
    for (let i = 0; i < 5; i++) {
        const year = currentYear - i;
        $("#yearFilter").append(`<option value="${year}">${year}</option>`);
    }
    
    // Load initial data
    loadCategoryData();
    loadMonthlyData();
    loadTopExpenses();
    
    // Event listeners for filters
    $("#periodFilter").change(function() {
        const period = $(this).val();
        if (period === "custom") {
            $("#customDateRange").removeClass("d-none");
        } else {
            $("#customDateRange").addClass("d-none");
            loadCategoryData();
            loadTopExpenses();
        }
    });
    
    $("#startDate, #endDate").change(function() {
        if ($("#periodFilter").val() === "custom" && 
            $("#startDate").val() && $("#endDate").val()) {
            loadCategoryData();
            loadTopExpenses();
        }
    });
    
    $("#yearFilter").change(function() {
        loadMonthlyData();
    });
    
    // Function to load category data for pie chart
    function loadCategoryData() {
        const period = $("#periodFilter").val();
        let params = { period: period };
        
        if (period === "custom") {
            params.startDate = $("#startDate").val();
            params.endDate = $("#endDate").val();
            
            if (!params.startDate || !params.endDate) {
                return; // Don't proceed if dates aren't selected
            }
        }
        
        $.ajax({
            url: "/analytics/category-data",
            type: "GET",
            data: params,
            success: function(data) {
                renderCategoryChart(data);
            },
            error: function(xhr, status, error) {
                console.error("Error loading category data:", error);
                $("#noCategoryData").removeClass("d-none");
            }
        });
    }
    
    // Function to render the category pie chart
    function renderCategoryChart(data) {
        const categories = Array.from(data.categories);
        const amounts = Array.from(data.amounts);
        
        if (categories.length === 0) {
            $("#noCategoryData").removeClass("d-none");
            return;
        }
        
        $("#noCategoryData").addClass("d-none");
        
        // Generate random colors for each category
        const backgroundColors = categories.map(() => getRandomColor());
        
        const ctx = document.getElementById('categoryChart').getContext('2d');
        
        if (categoryChart) {
            categoryChart.destroy();
        }
        
        categoryChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: categories,
                datasets: [{
                    data: amounts,
                    backgroundColor: backgroundColors,
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right',
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.raw || 0;
                                const total = context.chart.data.datasets[0].data.reduce((a, b) => a + parseFloat(b), 0);
                                const percentage = Math.round((value * 100) / total);
                                return `${label}: ${value} (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });
    }
    
    // Function to load monthly data for line chart
    function loadMonthlyData() {
        const year = $("#yearFilter").val();
        
        $.ajax({
            url: "/analytics/monthly-data",
            type: "GET",
            data: { year: year },
            success: function(data) {
                renderMonthlyChart(data);
            },
            error: function(xhr, status, error) {
                console.error("Error loading monthly data:", error);
                $("#noMonthlyData").removeClass("d-none");
            }
        });
    }
    
    // Function to render the monthly line chart
    function renderMonthlyChart(data) {
        const months = Array.from(data.months);
        const amounts = Array.from(data.amounts);
        
        if (months.length === 0) {
            $("#noMonthlyData").removeClass("d-none");
            return;
        }
        
        $("#noMonthlyData").addClass("d-none");
        
        // Convert month numbers to month names
        const monthNames = months.map(month => {
            return new Date(2000, month - 1, 1).toLocaleString('default', { month: 'short' });
        });
        
        const ctx = document.getElementById('monthlyChart').getContext('2d');
        
        if (monthlyChart) {
            monthlyChart.destroy();
        }
        
        monthlyChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: monthNames,
                datasets: [{
                    label: 'Monthly Expenses',
                    data: amounts,
                    borderColor: 'rgba(75, 192, 192, 1)',
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return value.toLocaleString();
                            }
                        }
                    }
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return `Expenses: ${parseFloat(context.raw).toLocaleString()}`;
                            }
                        }
                    }
                }
            }
        });
    }
    
    // Function to load top expenses
    function loadTopExpenses() {
        const period = $("#periodFilter").val();
        let params = { period: period };
        
        if (period === "custom") {
            params.startDate = $("#startDate").val();
            params.endDate = $("#endDate").val();
            
            if (!params.startDate || !params.endDate) {
                return; // Don't proceed if dates aren't selected
            }
        }
        
        $.ajax({
            url: "/expense-view/data",
            type: "GET",
            data: params,
            success: function(data) {
                renderTopExpenses(data);
            },
            error: function(xhr, status, error) {
                console.error("Error loading expenses data:", error);
                $("#noExpensesData").removeClass("d-none");
            }
        });
    }
    
    // Function to render top expenses table
    function renderTopExpenses(data) {
        const expenses = data.expenses || [];
        
        if (expenses.length === 0) {
            $("#noExpensesData").removeClass("d-none");
            return;
        }
        
        $("#noExpensesData").addClass("d-none");
        
        // Sort expenses by amount (descending) and take top 10
        const topExpenses = expenses
            .sort((a, b) => parseFloat(b.amount) - parseFloat(a.amount))
            .slice(0, 10);
        
        // Clear existing table rows
        $("#topExpensesTable").empty();
        
        // Add new rows
        topExpenses.forEach(expense => {
            // Get category name from category object
            const categoryName = expense.category && expense.category.categoryName ? expense.category.categoryName : 'Uncategorized';
            
            const row = `
                <tr>
                    <td>${formatDate(expense.date)}</td>
                    <td>${expense.description}</td>
                    <td>${categoryName}</td>
                    <td class="text-end text-danger">${formatCurrency(expense.amount, expense.currency)}</td>
                </tr>
            `;
            $("#topExpensesTable").append(row);
        });
    }
    
    // Helper function to format date
    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString();
    }
    
    // Helper function to format currency
    function formatCurrency(amount, currency) {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency || 'USD'
        }).format(amount);
    }
    
    // Helper function to generate random colors
    function getRandomColor() {
        const letters = '0123456789ABCDEF';
        let color = '#';
        for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        return color;
    }
});
