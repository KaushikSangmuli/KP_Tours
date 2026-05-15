package KP_TOURS.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    // =========================================================
    // FORMATTERS
    // =========================================================

    public static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static final DateTimeFormatter DISPLAY_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");

    public static final DateTimeFormatter DB_DATE_FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE;

    public static final DateTimeFormatter DB_DATE_TIME_FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // =========================================================
    // FORMAT METHODS
    // =========================================================

    public static String formatDisplayDate(LocalDate date) {

        if (date == null) {
            return "";
        }

        return date.format(DISPLAY_DATE_FORMAT);
    }

    public static String formatDisplayDateTime(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "";
        }

        return dateTime.format(DISPLAY_DATE_TIME_FORMAT);
    }

    public static String formatDbDate(LocalDate date) {

        if (date == null) {
            return null;
        }

        return date.format(DB_DATE_FORMAT);
    }

    public static String formatDbDateTime(LocalDateTime dateTime) {

        if (dateTime == null) {
            return null;
        }

        return dateTime.format(DB_DATE_TIME_FORMAT);
    }

    // =========================================================
    // PARSE METHODS
    // =========================================================

    public static LocalDate parseDate(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value, DB_DATE_FORMAT);
    }

    public static LocalDateTime parseDateTime(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDateTime.parse(value, DB_DATE_TIME_FORMAT);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    public static boolean isToday(LocalDate date) {

        return date != null &&
                date.equals(LocalDate.now());
    }

    public static YearMonth getCurrentMonth() {
        return YearMonth.now();
    }
}