package com.expense.controller;

import com.expense.model.Account;
import com.expense.service.AccountService;
import com.expense.service.PdfService;
import com.expense.expensetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final PdfService pdfService;
    private final UserService userService;

    @Autowired
    public AccountController(AccountService accountService, PdfService pdfService, UserService userService) {
        this.accountService = accountService;
        this.pdfService = pdfService;
        this.userService = userService;
    }

    @GetMapping
    public List<String> getAccountNames() {
        Long userId = userService.getCurrentUserId();
        return accountService.getAccountNames(userId);
    }



    @PostMapping(value = {"", "/add"}) // Handle both /api/accounts and /api/accounts/add
    public ResponseEntity<?> addAccount(@RequestBody String accountName) {
        try {
            Long userId = userService.getCurrentUserId();
            // Remove quotes from the JSON string
            String cleanAccountName = accountName.trim();
            if (cleanAccountName.startsWith("\"") && cleanAccountName.endsWith("\"")) {
                cleanAccountName = cleanAccountName.substring(1, cleanAccountName.length() - 1);
            }
            Account account = accountService.addAccount(cleanAccountName, userId);
            return ResponseEntity.ok(account);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{accountName}")
    public ResponseEntity<?> deleteAccount(@PathVariable String accountName) {
        try {
            Long userId = userService.getCurrentUserId();
            accountService.deleteAccount(accountName, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("An error occurred while deleting the account"));
        }
    }
    
    // Simple error response class
    private static class ErrorResponse {
        private final String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }

    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> generateAccountsPdf() {
        Long userId = userService.getCurrentUserId();
        List<Account> accounts = accountService.getAccounts(userId);
        List<String> headers = Arrays.asList("ID", "Name", "Balance");
        List<Function<Account, String>> mappers = Arrays.asList(
                account -> account.getAccountId().toString(),
                Account::getAccountName,
                account -> account.getAccountBalance().toString()
        );

        ByteArrayInputStream bis = pdfService.createPdf(accounts, headers, "Accounts Report", mappers);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "inline; filename=accounts-report.pdf");

        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}
