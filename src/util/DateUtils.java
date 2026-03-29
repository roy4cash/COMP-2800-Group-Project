package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Helpers for formatting dates in the UI.
 */
public class DateUtils {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private DateUtils() {}

    /** Formats a LocalDate as "Mar 15, 2024". */
    public static String toDisplayString(LocalDate date) {
        return date.format(DISPLAY_FORMAT);
    }

    /** Returns today's date as a string in YYYY-MM-DD format, ready for a text field. */
    public static String todayAsInputString() {
        return LocalDate.now().toString();
    }
}
