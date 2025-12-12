package main.java.config;

public final class OrderConstants {
    private OrderConstants() {}

    // File parsing constants
    public static final String FIELD_DELIMITER = "||";
    public static final String FIELD_DELIMITER_REGEX = "\\|\\|"; // Regex-escaped version for split operations
    public static final String TEMP_FILE_EXTENSION = ".tmp";
    public static final int MIN_PARTS_LENGTH = 5;
    public static final int ORDER_LINE_FIELD_COUNT = 3; // code, qty, subtotal
    public static final int ORDER_LINE_START_INDEX = 2; // Order lines start at index 2 (after orderNumber and date)

    // Date format
    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // User input constants
    public static final String EXIT_CODE = "X";
    public static final String YES_CODE = "Y";
    public static final String NO_CODE = "N";
    public static final int INVALID_QUANTITY = -1;
    public static final int MIN_QUANTITY = 0;

    // Order number format
    public static final String ORDER_NUMBER_FORMAT = "O****";
    public static final String ORDER_NUMBER_PREFIX = "O";
    public static final String ORDER_NUMBER_PATTERN = "^O\\d+$"; // O followed by one or more digits

    // Price format
    public static final String PRICE_FORMAT = "%.2f";

    // UI Messages
    public static final String MSG_INVALID_INPUT = "\nInvalid input.";
    public static final String MSG_ORDER_NUMBER_EXISTS = "\nOrder number exists.";
    public static final String MSG_NO_ITEMS_ADDED = "\nNo items added.";
    public static final String MSG_FAILED_TO_SAVE_ORDER = "\nFailed to save order (file missing?).";
    public static final String MSG_ORDER_ADDED = "\nOrder added. Proceed to payment.";
    public static final String MSG_FAILED_TO_ADD_ORDER = "\nFailed to add order.";
    public static final String MSG_ORDER_NOT_FOUND = "\nOrder not found.";
    public static final String MSG_FAILED_TO_SEARCH_ORDERS = "\nFailed to search orders.";
    public static final String MSG_ORDER_UPDATED_SUCCESS = "\nOrder updated successfully.";
    public static final String MSG_FAILED_TO_UPDATE_ORDER = "\nFailed to update order.";
    public static final String MSG_ORDER_DELETED = "\nOrder deleted.";
    public static final String MSG_FAILED_TO_DELETE_ORDER = "\nFailed to delete order.";
    public static final String MSG_UPDATE_CANCELLED = "No items added. Update cancelled.";

    // UI Prompts
    public static final String PROMPT_ORDER_NUMBER_ADD = "Enter Order Number (O****): ";
    public static final String PROMPT_ORDER_NUMBER_SEARCH = "Enter Order Code to search (or blank for all): ";
    public static final String PROMPT_ORDER_NUMBER_UPDATE = "Enter Order Number to update: ";
    public static final String PROMPT_ORDER_NUMBER_DELETE = "Enter Order Code to delete: ";
    public static final String PROMPT_ITEM_CODE = "Enter Item Code (Mxxxx/Sxxxx) or X to finish: ";
    public static final String PROMPT_QUANTITY = "Enter Quantity: ";
    public static final String PROMPT_ADD_ANOTHER = "Add another item? (Y/N): ";

    // UI Display
    public static final String MENU_HEADER = "\n\n----------------------------------------";
    public static final String MENU_TITLE = "      Welcome to Order Interface";
    public static final String MENU_SEPARATOR = "----------------------------------------";
    public static final String MENU_SELECTION_PROMPT = "\nEnter your selection: ";
    public static final String DISPLAY_ORDER_HEADER = "\nOrder:";
    public static final String DISPLAY_ORDER_NUMBER = "Order Number: ";
    public static final String DISPLAY_DATE = "Date: ";
    public static final String DISPLAY_TOTAL = "Total: ";
    public static final String DISPLAY_ITEM_PREFIX = "- Item: ";
    public static final String DISPLAY_QTY = " Qty: ";
    public static final String DISPLAY_SUBTOTAL = " Subtotal: ";
    public static final String DISPLAY_UPDATE_HEADER = "\n--- Updating Order: ";
    public static final String DISPLAY_UPDATE_FOOTER = " ---";
    public static final String DISPLAY_CURRENT_ORDER_DETAILS = "Current Order Details:";
    public static final String DISPLAY_UPDATE_INSTRUCTIONS = "\nEnter new order lines (this will replace existing lines):";
    public static final String DISPLAY_ITEM_INFO_FORMAT = "Item: %s - %s (Price: RM%s, Stock: %d)";
    public static final String DISPLAY_ITEM_NOT_FOUND_FORMAT = "Item not found: %s";
    public static final String MSG_QUANTITY_MUST_BE_POSITIVE = "Quantity must be positive.";
    public static final String MSG_INVALID_ORDER_NUMBER_FORMAT = "\nInvalid order number format. Must be O followed by digits (e.g., O0001).";
    public static final String MSG_ORDER_NUMBER_REQUIRED = "\nOrder number cannot be empty.";
    public static final String MSG_INVALID_ITEM_CODE_FORMAT = "\nInvalid item code format. Must be Mxxxx or Sxxxx (e.g., M0001, S0005).";
    public static final String MSG_ITEM_CODE_REQUIRED = "\nItem code cannot be empty. Enter X to finish.";
    public static final String MSG_INVALID_QUANTITY_FORMAT = "\nInvalid quantity. Please enter a positive whole number.";
    public static final String MSG_QUANTITY_REQUIRED = "\nQuantity cannot be empty.";
}
