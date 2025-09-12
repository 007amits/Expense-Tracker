package com.expense.expensetracker.service;

import com.expense.expensetracker.model.User;
import com.expense.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private com.expense.service.DefaultDataService defaultDataService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .or(() -> userRepository.findByPhoneNumber(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username, email, or phone number: " + username));

        String role = user.getRole();
        if (role == null || role.isEmpty()) {
            role = "USER";
        }

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), List.of(new SimpleGrantedAuthority(role)));
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        User savedUser = userRepository.save(user);
        
        // Initialize default data for the new user
        defaultDataService.initializeUserData(savedUser.getId());
        
        return savedUser;
    }

    public boolean isUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean isPhoneNumberExists(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }

    public boolean verifyUser(String username, String email, String phoneNumber) {
        return userRepository.findByUsername(username)
                .map(user -> user.getEmail().equals(email) && user.getPhoneNumber().equals(phoneNumber))
                .orElse(false);
    }

    public void updatePassword(String username, String newPassword) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        });
    }

    public Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                        user.setPassword(passwordEncoder.encode(newPassword));
                        userRepository.save(user);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
    
    /**
     * Find a user by their ID
     * @param id The user ID
     * @return The user if found, null otherwise
     */
    public User findById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.orElse(null);
    }
    
    /**
     * Update an existing user
     * @param user The user to update
     * @return The updated user
     */
    public User update(User user) {
        // Ensure the user exists
        User existingUser = findById(user.getId());
        if (existingUser == null) {
            throw new UsernameNotFoundException("User not found with id: " + user.getId());
        }
        
        // Don't update password through this method
        user.setPassword(existingUser.getPassword());
        
        // Don't change the role
        user.setRole(existingUser.getRole());
        
        return userRepository.save(user);
    }
}
