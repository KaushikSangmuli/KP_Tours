package KP_TOURS.util;


import KP_TOURS.model.Trip;

public class ValidationUtil {

    // =========================================================
    // TRIP VALIDATION
    // =========================================================

    public static boolean isValidTrip(Trip trip) {

        return trip != null
                && trip.getTripDate() != null
                && isNotBlank(trip.getName())
                && isNotBlank(trip.getSector())
                && isNotBlank(trip.getAirlineName());
    }

    // =========================================================
    // STRING VALIDATION
    // =========================================================

    public static boolean isNotBlank(String value) {

        return value != null
                && !value.trim().isEmpty();
    }

    // =========================================================
    // NUMBER VALIDATION
    // =========================================================

    public static boolean isPositive(double value) {

        return value >= 0;
    }

    public static boolean isValidAmount(String value) {

        try {

            Double.parseDouble(value);

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}