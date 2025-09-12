package com.expense.controller;

import com.expense.model.Income;
import com.expense.service.IncomeService;
import com.expense.service.AccountService;
import com.expense.expensetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IncomeController {
    @Autowired
    private IncomeService incomeService;

    @Autowired
    private AccountService accountService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/incomes")
    public List<Income> getAllIncomes(@RequestParam(required = false) String account, 
                                      @RequestParam(required = false) String period,
                                      @RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate,
                                      @RequestParam(defaultValue = "desc") String sortOrder) {
        return incomeService.getAllIncomes(account, period, startDate, endDate, sortOrder);
    }

    @GetMapping("/incomes/{id}")
    public Income getIncomeById(@PathVariable Long id) {
        return incomeService.getIncomeById(id);
    }

    @PostMapping("/incomes")
    public Income createIncome(@RequestBody Income income) {
        Long userId = userService.getCurrentUserId();
        income.setUserId(userId);
        return incomeService.createIncome(income);
    }

    @PutMapping("/incomes/{id}")
    public Income updateIncome(@PathVariable Long id, @RequestBody Income incomeDetails) {
        Long userId = userService.getCurrentUserId();
        incomeDetails.setUserId(userId);
        return incomeService.updateIncome(id, incomeDetails);
    }

    @DeleteMapping("/incomes/{id}")
    public void deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
    }


}
