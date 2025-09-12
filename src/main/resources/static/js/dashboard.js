$(document).ready(function() {
    const API_URL = '/api';
    let expenseSortOrder = 'desc';
    let incomeSortOrder = 'desc';

    function fetchDashboardData(period) {
        const expenseUrl = `${API_URL}/dashboard/summary?period=${period}&sortOrder=${expenseSortOrder}`;
        const incomeUrl = `${API_URL}/dashboard/summary?period=${period}&sortOrder=${incomeSortOrder}`;

        // Fetch for expenses
        $.get(expenseUrl, function(summary) {
            updateSummaryAndExpenses(summary);
        }).fail(function(jqXHR, textStatus, errorThrown) {
            alert("Error fetching dashboard expense data: " + textStatus + " - " + errorThrown);
            console.error("Dashboard Expense Error:", jqXHR.responseText);
        });

        // Fetch for income
        $.get(incomeUrl, function(summary) {
            updateIncome(summary);
        }).fail(function(jqXHR, textStatus, errorThrown) {
            alert("Error fetching dashboard income data: " + textStatus + " - " + errorThrown);
            console.error("Dashboard Income Error:", jqXHR.responseText);
        });
    }

    function updateSummaryAndExpenses(summary) {
        // Update summary cards
        $('#total-expense-summary').text(`₹${summary.totalExpense.toFixed(2)}`);
        updateNetTotal();

        // Populate expenses table
        const expensesTableBody = $('#dashboard-expenses-table');
        expensesTableBody.empty();
        summary.recentExpenses.forEach(expense => {
            const row = `<tr>
                <td>${expense.date}</td>
                <td>${expense.description}</td>
                <td>${expense.amount.toFixed(2)} ${expense.currency}</td>
                <td>${expense.categoryName}</td>
            </tr>`;
            expensesTableBody.append(row);
        });
    }

    function updateIncome(summary) {
        // Update summary cards
        $('#total-income-summary').text(`₹${summary.totalIncome.toFixed(2)}`);
        updateNetTotal();

        // Populate income table
        const incomeTableBody = $('#dashboard-income-table');
        incomeTableBody.empty();
        summary.recentIncomes.forEach(income => {
            const row = `<tr>
                <td>${income.date}</td>
                <td>${income.amount.toFixed(2)} ${income.currency}</td>
                <td>${income.account}</td>
            </tr>`;
            incomeTableBody.append(row);
        });
    }

    function updateNetTotal() {
        const totalIncome = parseFloat($('#total-income-summary').text().replace('₹', ''));
        const totalExpense = parseFloat($('#total-expense-summary').text().replace('₹', ''));
        const netTotal = totalIncome - totalExpense;
        const $netTotalSummary = $('#net-total-summary');
        const $netTotalCard = $netTotalSummary.closest('.card');

        $netTotalSummary.text(`₹${netTotal.toFixed(2)}`);

        // Update card color based on net total
        $netTotalCard.removeClass('bg-info bg-success bg-danger');
        if (netTotal > 0) {
            $netTotalCard.addClass('bg-success');
        } else if (netTotal < 0) {
            $netTotalCard.addClass('bg-danger');
        } else {
            $netTotalCard.addClass('bg-info');
        }
    }

    function updateSortIcon(headerId, sortOrder) {
        const icon = sortOrder === 'asc' ? 'bi-sort-up' : 'bi-sort-down';
        $(`#${headerId} .sort-icon`).removeClass('bi-sort-up bi-sort-down').addClass(icon);
    }

    $('#period-filter').on('change', function() {
        fetchDashboardData(this.value);
    });

    $('#dashboard-expense-date-sort').on('click', function() {
        expenseSortOrder = expenseSortOrder === 'asc' ? 'desc' : 'asc';
        updateSortIcon('dashboard-expense-date-sort', expenseSortOrder);
        fetchDashboardData($('#period-filter').val());
    });

    $('#dashboard-income-date-sort').on('click', function() {
        incomeSortOrder = incomeSortOrder === 'asc' ? 'desc' : 'asc';
        updateSortIcon('dashboard-income-date-sort', incomeSortOrder);
        fetchDashboardData($('#period-filter').val());
    });

    // Initial load
    fetchDashboardData('daily');
    updateSortIcon('dashboard-expense-date-sort', expenseSortOrder);
    updateSortIcon('dashboard-income-date-sort', incomeSortOrder);
    $('#dashboard-expense-date-sort .sort-icon').addClass('bi-sort-down');
    $('#dashboard-income-date-sort .sort-icon').addClass('bi-sort-down');
});
