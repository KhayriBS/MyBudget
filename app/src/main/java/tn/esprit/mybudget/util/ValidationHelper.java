package tn.esprit.mybudget.util;

import android.util.Patterns;

/**
 * Utility class for validating user input
 */
public class ValidationHelper {

    /**
     * Validates if the email format is correct
     * 
     * @param email Email address to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates if the password meets minimum requirements
     * 
     * @param password Password to validate
     * @return true if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }

    /**
     * Validates if the username meets requirements
     * 
     * @param username Username to validate
     * @return true if username is valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        // Username should be at least 3 characters
        if (username.trim().length() < 3) {
            return false;
        }
        // Only allow alphanumeric and underscore
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Get password strength message
     * 
     * @param password Password to check
     * @return Strength message
     */
    public static String getPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return "Weak - At least 6 characters required";
        }

        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()].*");

        int strength = 0;
        if (hasUpperCase)
            strength++;
        if (hasLowerCase)
            strength++;
        if (hasDigit)
            strength++;
        if (hasSpecial)
            strength++;

        if (strength >= 4 && password.length() >= 12) {
            return "Very Strong";
        } else if (strength >= 3 && password.length() >= 8) {
            return "Strong";
        } else if (strength >= 2) {
            return "Medium";
        } else {
            return "Weak";
        }
    }
}
