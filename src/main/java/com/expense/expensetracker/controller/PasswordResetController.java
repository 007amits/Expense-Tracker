package com.expense.expensetracker.controller;

import com.expense.expensetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class PasswordResetController {

    @Autowired
    private UserService userService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String username, @RequestParam String email, @RequestParam String phoneNumber, RedirectAttributes redirectAttributes) {
        boolean isValid = userService.verifyUser(username, email, phoneNumber);

        if (isValid) {
            return "redirect:/reset-password?username=" + username;
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid details. Please try again.");
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String username, @RequestParam String password, @RequestParam String confirmPassword, RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            redirectAttributes.addAttribute("username", username);
            return "redirect:/reset-password";
        }

        userService.updatePassword(username, password);
        redirectAttributes.addFlashAttribute("signupSuccess", "Password has been reset successfully. Please sign in.");
        return "redirect:/auth";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam String oldPassword, @RequestParam String newPassword, @RequestParam String confirmPassword, Principal principal, RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/change-password";
        }

        boolean isPasswordChanged = userService.changePassword(principal.getName(), oldPassword, newPassword);

        if (isPasswordChanged) {
            redirectAttributes.addFlashAttribute("success", "Password changed successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Incorrect old password.");
        }

        return "redirect:/change-password";
    }
}