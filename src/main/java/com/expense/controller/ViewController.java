package com.expense.controller;

import com.expense.expensetracker.service.UserService;
import com.expense.model.Account;
import com.expense.model.Expense;
import com.expense.model.Income;
import com.expense.service.AccountService;
import com.expense.service.ExpenseService;
import com.expense.service.IncomeService;
import com.expense.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Controller
public class ViewController {

    private final ExpenseService expenseService;
    private final PdfService pdfService;
    private final IncomeService incomeService;
    private final AccountService accountService;
    private final UserService userService;

    @Autowired
    public ViewController(ExpenseService expenseService, PdfService pdfService, IncomeService incomeService, AccountService accountService, UserService userService) {
        this.expenseService = expenseService;
        this.pdfService = pdfService;
        this.incomeService = incomeService;
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("currentPage", "dashboard");
        return "home";
    }

    @GetMapping("/expenses")
    public String expenses(Model model) {
        model.addAttribute("currentPage", "expenses");
        return "expenses";
    }

    @GetMapping("/expenses/pdf")
    public ResponseEntity<InputStreamResource> generateExpensesPdf(@RequestParam(required = false) String category,
                                                                 @RequestParam(required = false) String period,
                                                                 @RequestParam(required = false) String startDate,
                                                                 @RequestParam(required = false) String endDate,
                                                                 @RequestParam(defaultValue = "desc") String sortOrder) {
        List<Expense> expenses = expenseService.getAllExpenses(category, period, startDate, endDate, sortOrder);
        List<String> headers = Arrays.asList("ID", "Description", "Amount", "Date", "Category");
        List<Function<Expense, String>> mappers = Arrays.asList(
                expense -> expense.getId().toString(),
                Expense::getDescription,
                expense -> expense.getAmount().toString(),
                expense -> expense.getDate().toString(),
                expense -> expense.getCategory().getCategoryName()
        );

        ByteArrayInputStream bis = pdfService.createPdf(expenses, headers, "Expenses Report", mappers);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "inline; filename=expenses-report.pdf");

        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/income")
    public String income(Model model) {
        Long userId = userService.getCurrentUserId();
        List<Account> accounts = accountService.getAccounts(userId);
        model.addAttribute("accounts", accounts);
        model.addAttribute("currentPage", "income");
        return "income";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        Long userId = userService.getCurrentUserId();
        List<Account> accounts = accountService.getAccounts(userId);
        model.addAttribute("accounts", accounts);
        model.addAttribute("currentPage", "accounts");
        return "accounts";
    }

    @GetMapping("/income/pdf")
    public ResponseEntity<InputStreamResource> generateIncomePdf(@RequestParam(required = false) String account,
                                                               @RequestParam(required = false) String period,
                                                               @RequestParam(required = false) String startDate,
                                                               @RequestParam(required = false) String endDate,
                                                               @RequestParam(defaultValue = "desc") String sortOrder) {
        List<Income> incomes = incomeService.getAllIncomes(account, period, startDate, endDate, sortOrder);
        List<String> headers = Arrays.asList("ID", "Account", "Amount", "Date");
        List<Function<Income, String>> mappers = Arrays.asList(
                income -> income.getId().toString(),
                Income::getAccount,
                income -> income.getAmount().toString(),
                income -> income.getDate().toString()
        );

        ByteArrayInputStream bis = pdfService.createPdf(incomes, headers, "Income Report", mappers);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "inline; filename=income-report.pdf");

        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}