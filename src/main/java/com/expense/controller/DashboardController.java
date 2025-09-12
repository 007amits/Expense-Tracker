package com.expense.controller;

import com.expense.model.DashboardSummaryDTO;
import com.expense.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryDTO getSummary(@RequestParam String period, @RequestParam(defaultValue = "desc") String sortOrder) {
        return dashboardService.getDashboardSummary(period, sortOrder);
    }
}
