package com.expense.expensetracker.controller;

import com.expense.expensetracker.model.User;
import com.expense.expensetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping({"/login", "/signup", "/auth"})
    public String showAuthPage(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "auth";
    }

    @PostMapping("/signup")
    public String registerUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        // Validate date of birth is not in the future
        if (user.getDateOfBirth() != null && user.getDateOfBirth().isAfter(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error", "Date of birth cannot be in the future.");
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/auth";
        }
        
        if (userService.isUsernameExists(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Username already exists. Please choose another.");
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/auth";
        }
        if (userService.isEmailExists(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already registered. Please use another email.");
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/auth";
        }
        if (userService.isPhoneNumberExists(user.getPhoneNumber())) {
            redirectAttributes.addFlashAttribute("error", "Phone number already registered. Please use another number.");
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/auth";
        }

        userService.save(user);
        redirectAttributes.addFlashAttribute("signupSuccess", "Registration successful! Please sign in.");
        return "redirect:/auth";
    }
}