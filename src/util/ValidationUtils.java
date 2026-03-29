package util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Static helpers for validating user input.
 * Keeps validation logic out of the UI panels.
 */
public class ValidationUtils {

    private ValidationUtils() {}

    /**
     * Validates all fields required to add an expense.
     * Returns an error message string, or null if everything is valid.
     */
    public static String validateExpenseInput(String description, String amountText, String dateText) {
        if (description == null || description.trim().isEmpty()) {
            return "Description cannot be empty.";
        }
        if (!isPositiveNumber(amountText)) {
            return "Amount must be a positive number (e.g. 12.50).";
        }
        if (!isValidDate(dateText)) {
            return "Date must be in YYYY-MM-DD format (e.g. 2024-03-15).";
        }
        return null; // no error
    }

    /** Returns true if the text is a valid positive decimal number. */
    public static boolean isPositiveNumber(String text) {
        try {
            return Double.parseDouble(text) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Returns true if the text can be parsed as a LocalDate (YYYY-MM-DD). */
    public static boolean isValidDate(String text) {
        try {
            LocalDate.parse(text);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
