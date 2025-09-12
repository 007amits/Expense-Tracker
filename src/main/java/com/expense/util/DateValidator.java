package com.expense.util;

import java.time.LocalDate;

/**
 * Utility class for validating dates in the application
 */
public class DateValidator {
    
    /**
     * Validates that a date is not in the future
     * @param date The date to validate
     * @return true if the date is valid (not in the future), false otherwise
     */
    public static boolean isValidDate(LocalDate date) {
        if (date == null) {
            return true; // Null dates are considered valid (will be handled elsewhere if needed)
        }
        
        LocalDate today = LocalDate.now();
        return !date.isAfter(today);
    }
    
    /**
     * Validates that a date is not in the future and throws an exception if it is
     * @param date The date to validate
     * @throws IllegalArgumentException if the date is in the future
     */
    public static void validateDateNotInFuture(LocalDate date) {
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Future dates are not allowed");
        }
    }
}
