$(function () {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function(e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });
});

// Global Modal Variables
let addCategoryModal, addAccountModal, editExpenseModal, editIncomeModal, addExpenseModal, addIncomeModal;
let expenseSortOrder = 'desc';
let incomeSortOrder = 'desc';

//================================================
// EXPENSE FUNCTIONS
//================================================
function loadExpenses() {
    const category = $('#expense-category-filter').val();
    let period = $('#expense-period-filter').val();
    let startDate = '';
    let endDate = '';

    if (period === 'custom') {
        startDate = $('#expense-start-date').val();
        endDate = $('#expense-end-date').val();
    }

    const queryParams = new URLSearchParams({ category, period, startDate, endDate, sortOrder: expenseSortOrder });
    fetch(`/api/expenses?${queryParams.toString()}`)
        .then(response => response.json())
        .then(expenses => {
            const expensesTable = document.getElementById('expensesTable');
            expensesTable.innerHTML = '';
            expenses.forEach(expense => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${expense.date}</td>
                    <td>${expense.description}</td>
                    <td>${expense.amount.toFixed(2)}</td>
                    <td>${expense.currency}</td>
                    <td>${expense.categoryName}</td>
                    <td>${expense.paymentMode}</td>
                    <td>${expense.note || ''}</td>
                    <td>
                        <div class="btn-group" role="group">
                            <button class="btn btn-sm btn-warning" onclick="editExpense(${expense.id})"><i class="bi bi-pencil-fill"></i></button>
                            <button class="btn btn-sm btn-danger" data-bs-toggle="modal" data-bs-target="#deleteExpenseModal" data-bs-expense-id="${expense.id}"><i class="bi bi-trash-fill"></i></button>
                        </div>
                    </td>
                `;
                expensesTable.appendChild(row);
            });
        });
}

function addExpense() {
    // Validate date is not in the future
    if (!validateDate($('#date').val())) {
        return;
    }
    
    const expense = {
        description: $('#description').val(),
        amount: parseFloat($('#amount').val()),
        date: $('#date').val(),
        category: { categoryName: $('#category').val() },
        paymentMode: $('input[name="paymentMode"]:checked').val(),
        note: $('#note').val(),
        currency: $('#currency').val()
    };

    if (!expense.category.categoryName) {
        alert('Please select a category.');
        return;
    }

    $.ajax({
        url: '/api/expenses',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(expense),
        success: function() {
            loadExpenses();
            $('#expenseForm')[0].reset();
            addExpenseModal.hide();
        }
    });
}

function editExpense(id) {
    $.getJSON(`/api/expenses/${id}`, function(expense) {
        // Populate the form fields
        $('#editExpenseId').val(expense.id);
        $('#editDescription').val(expense.description);
        $('#editAmount').val(expense.amount);
        $('#editDate').val(expense.date);
        $('#editNote').val(expense.note);
        $('#editCurrency').val(expense.currency);
        
        // Set the category value in the pre-populated dropdown
        $('#editCategory').val(expense.categoryName);

        // Select the correct payment mode
        $(`input[name=editPaymentMode][value='${expense.paymentMode}']`).prop('checked', true);

        // Set max date to today
        setMaxDateToToday();
        
        // Show the modal
        editExpenseModal.show();
    });
}



function loadCategories(selectCategory) {
    $.getJSON('/api/categories', function(categories) {
        const $categorySelects = $('#category, #editCategory, #expense-category-filter');
        $categorySelects.html('<option value="">Select Category</option>'); // Clear existing options
        $.each(categories, function(index, category) {
            $categorySelects.append($('<option>', {
                value: category,
                text: category
            }));
        });
        // If a category should be pre-selected (e.g., after adding a new one)
        if (selectCategory) {
            $('#category').val(selectCategory);
        }
    });
}

//================================================
// ACCOUNT & INCOME FUNCTIONS
//================================================
function loadAccounts(selectedAccount) {
    const accountSelects = $('#incomeAccount, #income-account-filter');
    // Don't clear the incomeAccount dropdown if it's already populated by Thymeleaf
    const filterSelect = $('#income-account-filter');
    filterSelect.empty().append('<option value="">All</option>');

    $.get("/api/accounts", function(accounts) {
        accounts.forEach(account => {
            const isSelected = account === selectedAccount ? ' selected' : '';
            // Only populate the filter dropdown, as the modal dropdown is handled by Thymeleaf
            filterSelect.append(`<option value="${account}"${isSelected}>${account}</option>`);
        });
         // Re-add the server-side rendered accounts to the modal dropdown to ensure consistency
        const modalSelect = $('#incomeAccount');
        if (modalSelect.find('option').length <= 1) { // If only "Select Account" is there
            modalSelect.empty().append('<option value="">Select Account</option>');
            accounts.forEach(account => {
                modalSelect.append(`<option value="${account}">${account}</option>`);
            });
        }
    });
}

function loadIncomes() {
    const account = $('#income-account-filter').val();
    let period = $('#income-period-filter').val();
    let startDate = '';
    let endDate = '';

    if (period === 'custom') {
        startDate = $('#income-start-date').val();
        endDate = $('#income-end-date').val();
    }

    const queryParams = new URLSearchParams({ account, period, startDate, endDate, sortOrder: incomeSortOrder });
    $.get(`/api/incomes?${queryParams.toString()}`, function(incomes) {
        const incomeTable = $("#incomeTable");
        incomeTable.empty();
        incomes.forEach(income => {
            incomeTable.append(`
                <tr>
                    <td>${income.date}</td>
                    <td>${income.amount.toFixed(2)}</td>
                    <td>${income.currency}</td>
                    <td>${income.account}</td>
                    <td>${income.note}</td>
                    <td>
                        <div class="btn-group" role="group">
                            <button class="btn btn-sm btn-info" onclick="editIncome(${income.id})"><i class="bi bi-pencil-fill"></i></button>
                            <button class="btn btn-sm btn-danger" data-bs-toggle="modal" data-bs-target="#deleteIncomeModal" data-bs-income-id="${income.id}"><i class="bi bi-trash-fill"></i></button>
                        </div>
                    </td>
                </tr>
            `);
        });
    });
}

function editIncome(id) {
    $.get(`/api/incomes/${id}`, function(income) {
        $("#editIncomeId").val(income.id);
        $("#editIncomeAmount").val(income.amount);
        $("#editIncomeCurrency").val(income.currency);
        $("#editIncomeDate").val(income.date);
        $("#editIncomeNote").val(income.note);
        $('#editIncomeAccount').val(income.account);
        editIncomeModal.show();
    });
}

function validateAndSubmitForm(formId) {
    const dateInput = $(`#${formId} input[type="date"]`);
    if (dateInput.length > 0 && !validateDate(dateInput.val())) {
        return;
    }
    $(`#${formId}`).submit();
}

//================================================
// UTILITY FUNCTIONS
//================================================
function setMaxDateToToday() {
    // Get today's date in YYYY-MM-DD format
    const today = new Date().toISOString().split('T')[0];
    
    // Set the max attribute on all date inputs to allow today's date
    $('input[type="date"]').attr('max', today);
}

function validateDate(dateString) {
    if (!dateString) return true; // Empty dates pass validation
    
    // Parse dates and normalize them for comparison
    const selectedDate = new Date(dateString + 'T00:00:00'); // Ensure consistent time part
    selectedDate.setHours(0, 0, 0, 0);
    
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Reset time part for date comparison
    
    // Compare dates using timestamps to avoid timezone issues
    if (selectedDate.getTime() > today.getTime()) {
        alert('Cannot select future dates. Please select today or a past date.');
        return false;
    }
    return true;
}

//================================================
// DOCUMENT READY
//================================================
$(document).ready(function() {
    // Set max date to today for all date inputs
    setMaxDateToToday();
    const path = window.location.pathname;

    function updateSortIcon(headerId, order) {
        const icon = order === 'asc' ? 'bi-sort-up' : 'bi-sort-down';
        $(`#${headerId} .sort-icon`).removeClass('bi-sort-up bi-sort-down').addClass(icon);
    }

    if (path.includes('/expenses')) {
        // Initialize Expense Modals
        addExpenseModal = new bootstrap.Modal(document.getElementById('addExpenseModal'));
        addCategoryModal = new bootstrap.Modal(document.getElementById('addCategoryModal'));
        editExpenseModal = new bootstrap.Modal(document.getElementById('editExpenseModal'));
        
        // Add event listeners for modal show events to set max date
        $('#addExpenseModal').on('show.bs.modal', function() {
            setMaxDateToToday();
        });
        $('#editExpenseModal').on('show.bs.modal', function() {
            setMaxDateToToday();
        });

        // Initial Data Load for Expenses
        loadExpenses();
        loadCategories();
        updateSortIcon('expense-date-sort', expenseSortOrder);

        // --- Expense Event Listeners ---
        $('#addExpenseBtn').on('click', function() {
            setMaxDateToToday();
        });
        $('#expenseForm').on('submit', e => { e.preventDefault(); addExpense(); });
        $('#saveChangesBtn').on('click', function() {
            // Validate date is not in the future
            if (!validateDate($('#editDate').val())) {
                return;
            }
            
            const expenseId = $('#editExpenseId').val();
            const expense = {
                description: $('#editDescription').val(),
                amount: parseFloat($('#editAmount').val()),
                date: $('#editDate').val(),
                category: { categoryName: $('#editCategory').val() },
                paymentMode: $('input[name=editPaymentMode]:checked').val(),
                note: $('#editNote').val(),
                currency: $('#editCurrency').val()
            };
            $.ajax({
                url: `/api/expenses/${expenseId}`,
                type: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify(expense),
                success: function() {
                    editExpenseModal.hide();
                    loadExpenses();
                },
                error: function(xhr, status, error) {
                    console.error('Error updating expense:', error);
                    console.log('Response:', xhr.responseText);
                    alert('Failed to update expense. Please try again.');
                }
            });
        });

        // --- Category Event Listeners ---
        $('#addCategoryBtn').on('click', () => addCategoryModal.show());
        $('#saveCategoryBtn').on('click', function() {
            const newCategory = $('#newCategoryName').val().trim();
            if (newCategory) {
                $.ajax({
                    url: '/api/categories', type: 'POST', contentType: 'application/json', data: JSON.stringify(newCategory),
                    success: function() {
                        $('#newCategoryName').val('');
                        addCategoryModal.hide();
                        loadCategories(newCategory);
                    }
                });
            }
        });
        $('#removeCategoryBtn').on('click', function() {
            const categoryToRemove = $('#category').val();
            if (categoryToRemove && confirm(`Are you sure you want to remove the category: \"${categoryToRemove}\"?`)) {
                $.ajax({
                    url: `/api/categories/${categoryToRemove}`,
                    type: 'DELETE',
                    success: function() {
                        loadCategories();
                    },
                    error: function(xhr) {
                        if (xhr.status === 409) {
                            try {
                                const errorResponse = JSON.parse(xhr.responseText);
                                alert(errorResponse.message);
                            } catch (e) {
                                alert('This category cannot be deleted as an expense is present for this category.');
                            }
                        } else {
                            alert('An error occurred while deleting the category.');
                        }
                    }
                });
            }
        });

        // --- Filter Event Listeners ---
        $('#expense-period-filter').on('change', function() {
            if ($(this).val() === 'custom') {
                $('.expense-custom-date-range').show();
            } else {
                $('.expense-custom-date-range').hide();
                loadExpenses();
            }
        });
        $('#expense-category-filter, #expense-start-date, #expense-end-date').on('change', loadExpenses);
        $('#clear-expense-filters').on('click', function() {
            $('#expense-category-filter').val('');
            $('#expense-period-filter').val('all').trigger('change');
            $('#expense-start-date, #expense-end-date').val('');
            loadExpenses();
        });

        // --- Sort Event Listener ---
        $('#expense-date-sort').on('click', function() {
            expenseSortOrder = expenseSortOrder === 'asc' ? 'desc' : 'asc';
            updateSortIcon('expense-date-sort', expenseSortOrder);
            loadExpenses();
        });

        const deleteExpenseModal = document.getElementById('deleteExpenseModal');
        if (deleteExpenseModal) {
            const expenseIdToDeleteInput = document.getElementById('expenseIdToDelete');
            const confirmDeleteBtn = document.getElementById('confirmDeleteExpense');

            deleteExpenseModal.addEventListener('show.bs.modal', function (event) {
                const button = event.relatedTarget;
                const expenseId = button.getAttribute('data-bs-expense-id');
                expenseIdToDeleteInput.value = expenseId;
            });

            confirmDeleteBtn.addEventListener('click', function () {
                const expenseId = expenseIdToDeleteInput.value;
                const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
                const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

                fetch('/api/expenses/' + expenseId, {
                    method: 'DELETE',
                    headers: {
                        [header]: token
                    }
                })
                .then(response => {
                    if (response.ok) {
                        window.location.reload();
                    } else {
                        alert('Failed to delete expense.');
                    }
                });
            });
        }
    } else if (path.includes('/income')) {
        // Initialize Income Modals
        addIncomeModal = new bootstrap.Modal(document.getElementById('addIncomeModal'));
        editIncomeModal = new bootstrap.Modal(document.getElementById('editIncomeModal'));
        addAccountModal = new bootstrap.Modal(document.getElementById('addAccountModal'));
        
        // Add event listeners for modal show events to set max date
        $('#addIncomeModal').on('show.bs.modal', function() {
            setMaxDateToToday();
        });
        $('#editIncomeModal').on('show.bs.modal', function() {
            setMaxDateToToday();
        });

        // Initial Data Load for Income
        loadIncomes();
        loadAccounts();
        updateSortIcon('income-date-sort', incomeSortOrder);

        // --- Income Event Listeners ---
        $('#incomeForm').on('submit', e => {
            e.preventDefault();
            // Validate date is not in the future
            if (!validateDate($('#incomeDate').val())) {
                return;
            }
            
            const income = {
                amount: parseFloat($('#incomeAmount').val()),
                date: $('#incomeDate').val(),
                account: $('#incomeAccount').val(),
                note: $('#incomeNote').val(),
                currency: $('#incomeCurrency').val()
            };
            $.ajax({
                url: "/api/incomes",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(income),
                success: function() {
                    loadIncomes();
                    $("#incomeForm")[0].reset();
                    addIncomeModal.hide();
                }
            });
        });
        $('#saveIncomeChangesBtn').on('click', function() {
            // Validate date is not in the future
            if (!validateDate($('#editIncomeDate').val())) {
                return;
            }
            
            const id = $("#editIncomeId").val();
            const incomeDetails = { amount: $("#editIncomeAmount").val(), currency: $("#editIncomeCurrency").val(), date: $("#editIncomeDate").val(), account: $("#editIncomeAccount").val(), note: $("#editIncomeNote").val() };
            $.ajax({ url: `/api/incomes/${id}`, type: "PUT", contentType: "application/json", data: JSON.stringify(incomeDetails), success: () => { loadIncomes(); editIncomeModal.hide(); } });
        });

        // --- Account Event Listeners ---
        $('#addAccountBtn').on('click', () => addAccountModal.show());
        $('#saveAccountBtn').on('click', function() {
            const newAccountName = $('#newAccountName').val().trim();
            if (newAccountName) {
                $.ajax({
                    url: '/api/accounts',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify(newAccountName),
                    success: function(addedAccount) {
                        $('#newAccountName').val('');
                        addAccountModal.hide();
                        loadAccounts(addedAccount.accountName);
                    },
                    error: function(xhr, status, error) {
                        console.error("Error adding account: ", status, error);
                        alert('Failed to add account. Check console for details.');
                    }
                });
            }
        });
        $('#removeAccountBtn').on('click', function() {
            const accountToRemove = $('#incomeAccount').val();
            if (accountToRemove && confirm(`Are you sure you want to remove the account: \"${accountToRemove}\"?`)) {
                $.ajax({ url: `/api/accounts/${accountToRemove}`, type: 'DELETE', success: () => loadAccounts(), error: (xhr) => alert('Error: ' + xhr.responseText) });
            }
        });

        // --- Filter Event Listeners ---
        $('#income-period-filter').on('change', function() {
            if ($(this).val() === 'custom') {
                $('.income-custom-date-range').show();
            } else {
                $('.income-custom-date-range').hide();
                loadIncomes();
            }
        });
        $('#income-account-filter, #income-start-date, #income-end-date').on('change', loadIncomes);
        $('#clear-income-filters').on('click', function() {
            $('#income-account-filter').val('');
            $('#income-period-filter').val('all').trigger('change');
            $('#income-start-date, #income-end-date').val('');
            loadIncomes();
        });

        // --- Sort Event Listener ---
        $('#income-date-sort').on('click', function() {
            incomeSortOrder = incomeSortOrder === 'asc' ? 'desc' : 'asc';
            updateSortIcon('income-date-sort', incomeSortOrder);
            loadIncomes();
        });

        const deleteIncomeModal = document.getElementById('deleteIncomeModal');
        if (deleteIncomeModal) {
            const incomeIdToDeleteInput = document.getElementById('incomeIdToDelete');
            const confirmDeleteBtn = document.getElementById('confirmDeleteIncome');

            deleteIncomeModal.addEventListener('show.bs.modal', function (event) {
                const button = event.relatedTarget;
                const incomeId = button.getAttribute('data-bs-income-id');
                incomeIdToDeleteInput.value = incomeId;
            });

            confirmDeleteBtn.addEventListener('click', function () {
                const incomeId = incomeIdToDeleteInput.value;
                const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
                const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
                
                fetch('/api/incomes/' + incomeId, {
                    method: 'DELETE',
                    headers: {
                        [header]: token
                    }
                })
                .then(response => {
                    if (response.ok) {
                        window.location.reload();
                    } else {
                        alert('Failed to delete income record.');
                    }
                });
            });
        }
    }


});