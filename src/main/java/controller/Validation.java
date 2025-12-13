package main.java.controller;
import main.java.config.StaffConstants;
import main.java.config.OrderConstants;

public final class Validation {
    private Validation() {}

    public static boolean isStaffId(String value) {
        return value != null && value.matches(StaffConstants.STAFF_ID_PATTERN);
    }

    public static boolean isPhone(String value) {
        return value != null && value.matches(StaffConstants.PHONE_PATTERN);
    }

    public static boolean isPostcode(String value) {
        return value != null && value.matches("^\\d{" + StaffConstants.POSTCODE_DIGIT_COUNT + "}$");
    }

    public static boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean hasMaxLength(String value, int max) {
        return value != null && value.length() <= max;
    }

    public static boolean isOrderNumber(String value) {
        return value != null && value.matches(OrderConstants.ORDER_NUMBER_PATTERN);
    }

    public static boolean isItemCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String trimmed = value.trim();
        // Item code format: Mxxxx or Sxxxx (M or S followed by 4 digits)
        return trimmed.matches("^[MS]\\d{4}$");
    }

    public static boolean isPositiveInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            int num = Integer.parseInt(value.trim());
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

