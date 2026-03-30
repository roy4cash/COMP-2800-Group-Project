/**
 * File: ValidationUtils.java
 * Purpose: Centralizes reusable validation rules for expense, budget, and investment forms.
 *
 * Keeping validation in one utility makes the UI easier to maintain and keeps
 * JDBC operations from receiving invalid user input.
 */
package util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Static helpers for validating user input.
 * Keeps validation logic out of the UI panels.
 */
public class ValidationUtils {

    private static final int EXPENSE_DESCRIPTION_MAX = 255;
    private static final int INVESTMENT_NAME_MAX     = 100;
    private static final int TICKER_MAX              = 20;

    /** Prevents instantiation because validation is exposed entirely through static helpers. */
    private ValidationUtils() {}

    /**
     * Validates all fields required to add an expense.
     * Returns an error message string, or null if everything is valid.
     */
    public static String validateExpenseInput(String description, String amountText, String dateText) {
        if (isBlank(description)) {
            return "Description cannot be empty.";
        }
        if (description.trim().length() > EXPENSE_DESCRIPTION_MAX) {
            return "Description must be 255 characters or fewer.";
        }
        if (!isPositiveMoney(amountText)) {
            return "Amount must be a positive number with up to 2 decimal places (e.g. 12.50).";
        }
        if (!isValidDate(dateText)) {
            return "Date must be in YYYY-MM-DD format (e.g. 2024-03-15).";
        }
        if (isFutureDate(dateText)) {
            return "Expense date cannot be in the future.";
        }
        return null; // no error
    }

    /** Validates the monthly budget field. Returns null when valid. */
    public static String validateBudgetInput(String amountText) {
        if (!isPositiveMoney(amountText)) {
            return "Budget must be a positive number with up to 2 decimal places.";
        }
        return null;
    }

    /** Validates all fields required to add an investment. */
    public static String validateInvestmentInput(String name, String ticker, String sharesText,
                                                 String buyPriceText, String currentPriceText,
                                                 String dateText) {
        if (isBlank(name)) {
            return "Investment name is required.";
        }
        if (name.trim().length() > INVESTMENT_NAME_MAX) {
            return "Investment name must be 100 characters or fewer.";
        }
        if (!isBlank(ticker) && ticker.trim().length() > TICKER_MAX) {
            return "Ticker must be 20 characters or fewer.";
        }
        if (!isBlank(ticker) && !isValidTicker(ticker)) {
            return "Ticker may only contain letters, numbers, dots, and dashes.";
        }
        if (!isPositiveQuantity(sharesText)) {
            return "Shares must be a positive number with up to 6 decimal places.";
        }
        if (!isPositiveMoney(buyPriceText)) {
            return "Buy price must be a positive number with up to 2 decimal places.";
        }
        if (!isBlank(currentPriceText) && !isNonNegativeMoney(currentPriceText)) {
            return "Current price must be zero or greater, with up to 2 decimal places.";
        }
        if (!isValidDate(dateText)) {
            return "Purchase date must be in YYYY-MM-DD format.";
        }
        if (isFutureDate(dateText)) {
            return "Purchase date cannot be in the future.";
        }
        return null;
    }

    /** Returns true if the text is a valid positive decimal number. */
    public static boolean isPositiveNumber(String text) {
        try {
            double value = Double.parseDouble(text.trim());
            return Double.isFinite(value) && value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Returns true if the text is a valid non-negative decimal number. */
    public static boolean isNonNegativeNumber(String text) {
        try {
            double value = Double.parseDouble(text.trim());
            return Double.isFinite(value) && value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Returns true for positive money values with up to 2 decimal places. */
    public static boolean isPositiveMoney(String text) {
        return matchesPattern(text, "^\\d{1,9}(\\.\\d{1,2})?$") && isPositiveNumber(text);
    }

    /** Returns true for non-negative money values with up to 2 decimal places. */
    public static boolean isNonNegativeMoney(String text) {
        return matchesPattern(text, "^\\d{1,9}(\\.\\d{1,2})?$") && isNonNegativeNumber(text);
    }

    /** Returns true for positive quantities with up to 6 decimal places. */
    public static boolean isPositiveQuantity(String text) {
        return matchesPattern(text, "^\\d{1,9}(\\.\\d{1,6})?$") && isPositiveNumber(text);
    }

    /** Returns true when the ticker contains only common ticker characters. */
    public static boolean isValidTicker(String text) {
        return matchesPattern(text, "^[A-Za-z0-9.-]{1,20}$");
    }

    /** Returns true if the text can be parsed as a LocalDate (YYYY-MM-DD). */
    public static boolean isValidDate(String text) {
        try {
            LocalDate.parse(text.trim());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /** Returns true if the parsed date is after today. */
    public static boolean isFutureDate(String text) {
        try {
            return LocalDate.parse(text.trim()).isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /** Returns true when text is null or only whitespace. */
    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Shared helper for regex-based validation rules.
     *
     * Centralizing the trim-and-match logic keeps the public validation methods
     * easier to read and reduces repeated null handling.
     */
    private static boolean matchesPattern(String text, String regex) {
        return text != null && text.trim().matches(regex);
    }
}
