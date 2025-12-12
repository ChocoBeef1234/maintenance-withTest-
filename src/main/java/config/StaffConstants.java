package main.java.config;

public final class StaffConstants {
    private StaffConstants() {}

    // File parsing constants
    public static final String FIELD_DELIMITER = "||";
    public static final int REQUIRED_FIELD_COUNT = 10;
    
    // Field indices for parsing
    public static final int INDEX_STAFF_ID = 0;
    public static final int INDEX_PASSWORD = 1;
    public static final int INDEX_FIRST_NAME = 2;
    public static final int INDEX_LAST_NAME = 3;
    public static final int INDEX_PHONE_NO = 4;
    public static final int INDEX_STAFF_POSITION = 5;
    public static final int INDEX_STREET = 6;
    public static final int INDEX_POSTCODE = 7;
    public static final int INDEX_REGION = 8;
    public static final int INDEX_STATE = 9;

    // Validation patterns
    public static final String STAFF_ID_PATTERN = "^S\\d{4}$";
    public static final int STAFF_ID_DIGIT_COUNT = 4;
    public static final String PHONE_PATTERN = "^(\\d{3}-\\d{3}-\\d{4}|\\d{3}-\\d{4}-\\d{4})$";
    public static final int POSTCODE_DIGIT_COUNT = 5;

    // UI Messages
    public static final String MSG_INVALID_INPUT = "\n*Invalid input";
    public static final String MSG_INVALID_STAFF_ID_FORMAT = "\nInvalid Staff ID format.";
    public static final String MSG_INVALID_PHONE_FORMAT = "\nInvalid phone number format.";
    public static final String MSG_INVALID_POSTCODE_FORMAT = "\nInvalid postcode format.";
    public static final String MSG_REQUIRED_FIELD = "\nThis field cannot be empty.";
    public static final String MSG_STAFF_ADDED_SUCCESS = "\nStaff added successfully.";
    public static final String MSG_STAFF_ID_EXISTS = "\nStaff ID already exists or file missing.";
    public static final String MSG_FAILED_TO_ADD = "\nFailed to add staff.";
    public static final String MSG_STAFF_NOT_FOUND = "\nStaff not found.";
    public static final String MSG_FAILED_TO_SEARCH = "\nFailed to search staff.";
    public static final String MSG_STAFF_MODIFIED_SUCCESS = "\nStaff modified successfully.";
    public static final String MSG_FAILED_TO_MODIFY = "\nFailed to modify staff.";
    public static final String MSG_STAFF_DELETED_SUCCESS = "\nStaff deleted successfully.";
    public static final String MSG_FAILED_TO_DELETE = "\nFailed to delete staff.";

    // UI Prompts
    public static final String PROMPT_STAFF_ID_SEARCH = "Enter Staff ID to search (Sxxxx): ";
    public static final String PROMPT_STAFF_ID_MODIFY = "Enter Staff ID to modify (Sxxxx): ";
    public static final String PROMPT_STAFF_ID_DELETE = "Enter Staff ID to delete (Sxxxx): ";
    public static final String PROMPT_STAFF_ID_NEW = "Enter Staff ID (Sxxxx): ";
    public static final String PROMPT_PASSWORD = "Enter Password: ";
    public static final String PROMPT_FIRST_NAME = "Enter First Name: ";
    public static final String PROMPT_LAST_NAME = "Enter Last Name: ";
    public static final String PROMPT_PHONE_NO = "Enter Phone No (XXX-XXX-XXXX or XXX-XXXX-XXXX): ";
    public static final String PROMPT_STAFF_POSITION = "Enter Staff Position: ";
    public static final String PROMPT_STREET = "Enter Street: ";
    public static final String PROMPT_POSTCODE = "Enter Postcode (XXXXX): ";
    public static final String PROMPT_REGION = "Enter Region: ";
    public static final String PROMPT_STATE = "Enter State: ";
    public static final String PROMPT_LEAVE_BLANK = "\nLeave blank to keep existing value.";

    // UI Display
    public static final String MENU_HEADER = "\n\n----------------------------------------";
    public static final String MENU_TITLE = "      Welcome to Staff Interface";
    public static final String MENU_SEPARATOR = "----------------------------------------";
    public static final String MENU_SELECTION_PROMPT = "\nEnter your selection: ";
    public static final String DISPLAY_STAFF_INFO = "\nStaff Information:";
    public static final String DISPLAY_STAFF_ID = "Staff ID: ";
    public static final String DISPLAY_PASSWORD = "Password: ";
    public static final String DISPLAY_FIRST_NAME = "First Name: ";
    public static final String DISPLAY_LAST_NAME = "Last Name: ";
    public static final String DISPLAY_PHONE_NO = "Phone No: ";
    public static final String DISPLAY_STAFF_POSITION = "Staff Position: ";
    public static final String DISPLAY_STREET = "Street: ";
    public static final String DISPLAY_POSTCODE = "Postcode: ";
    public static final String DISPLAY_REGION = "Region: ";
    public static final String DISPLAY_STATE = "State: ";
    public static final String DISPLAY_ALL_STAFF = "\nAll Staff:";
}

