package main.java.view;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import main.java.model.Staff;
import main.java.model.Name;
import main.java.model.Address;
import main.java.config.StaffConstants;
import main.java.config.StaffMenuOption;
import main.java.controller.Validation;

public class StaffView {
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Displays the staff menu and returns the user's selection.
     * @return numeric menu choice
     */
    public int menu() {
        System.out.println(StaffConstants.MENU_HEADER);
        System.out.println(StaffConstants.MENU_TITLE);
        System.out.println(StaffConstants.MENU_SEPARATOR);
        for (StaffMenuOption option : StaffMenuOption.values()) {
            System.out.println(option.getDisplayText());
        }
        System.out.print(StaffConstants.MENU_SELECTION_PROMPT);
        return scanner.nextInt();
    }

    /**
     * Collects a new staff entry with validation for each field.
     * @return populated staff instance
     */
    public Staff promptNewStaff() {
        scanner.nextLine(); // consume newline
        String staffId = 
        promptValidated(StaffConstants.PROMPT_STAFF_ID_NEW, Validation::isStaffId, StaffConstants.MSG_INVALID_STAFF_ID_FORMAT);
        String password = 
        promptValidated(StaffConstants.PROMPT_PASSWORD, Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String first = 
        promptValidated(StaffConstants.PROMPT_FIRST_NAME, Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String last = 
        promptValidated(StaffConstants.PROMPT_LAST_NAME, Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String phone = 
        promptValidated(StaffConstants.PROMPT_PHONE_NO, Validation::isPhone, StaffConstants.MSG_INVALID_PHONE_FORMAT);
        String position = 
        promptValidated(StaffConstants.PROMPT_STAFF_POSITION, Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String street = 
        promptValidated(StaffConstants.PROMPT_STREET, Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String postcode = 
        promptValidated(StaffConstants.PROMPT_POSTCODE, Validation::isPostcode, StaffConstants.MSG_INVALID_POSTCODE_FORMAT);
        String region = 
        promptValidated(StaffConstants.PROMPT_REGION, Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String state = 
        promptValidated(StaffConstants.PROMPT_STATE, Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        return new Staff(staffId, password, new Name(first, last), phone, position, new Address(street, postcode, region, state));
    }

    /**
     * Prompts for a staff ID using the provided label.
     * @param prompt prompt text to show
     * @return validated staff ID
     */
    public String promptStaffId(String prompt) {
        scanner.nextLine(); // consume newline if needed
        return promptValidated(prompt, Validation::isStaffId, StaffConstants.MSG_INVALID_STAFF_ID_FORMAT);
    }

    /**
     * Prompts for new field values while allowing blanks to keep current data.
     * @param current existing staff data
     * @return updated staff instance
     */
    public Staff promptUpdate(Staff current) {
        scanner.nextLine(); // consume newline
        System.out.println(StaffConstants.PROMPT_LEAVE_BLANK);
        
        String staffId = promptUpdateField("New Staff ID (Sxxxx)", current.getStaffId(), Validation::isStaffId, StaffConstants.MSG_INVALID_STAFF_ID_FORMAT);
        String password = promptUpdateField("New Password", current.getpassword(), Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String first = promptUpdateField("New First Name", current.getname().getFirstName(), Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String last = promptUpdateField("New Last Name", current.getname().getLastName(), Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String phone = promptUpdateField("New Phone No", current.getphoneNo(), Validation::isPhone, StaffConstants.MSG_INVALID_PHONE_FORMAT);
        String position = promptUpdateField("New Staff Position", current.getStaffPosition(), Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String street = promptUpdateField("New Street", current.getaddress().getstreet(), Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String postcode = promptUpdateField("New Postcode", current.getaddress().getpostcode(), Validation::isPostcode, StaffConstants.MSG_INVALID_POSTCODE_FORMAT);
        String region = promptUpdateField("New Region", current.getaddress().getregion(), Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);
        String state = promptUpdateField("New State", current.getaddress().getstate(), Validation::isNonEmpty, StaffConstants.MSG_REQUIRED_FIELD);

        return new Staff(staffId, password, new Name(first, last), phone, position, new Address(street, postcode, region, state));
    }

    /**
     * Repeatedly prompts until the validator accepts the input.
     * @param prompt text to display
     * @param validator predicate to validate the input
     * @param errorMessage message to show on validation failure
     */
    private String promptValidated(String prompt, Predicate<String> validator, String errorMessage) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (validator.test(input)) {
                return input;
            }
            System.out.println(errorMessage);
        }
    }

    /**
     * Prompts for an updated field value, returning the current value if blank.
     * @param label label to show the user
     * @param currentValue value to keep when input is blank
     * @param validator predicate to validate non-blank input
     * @param errorMessage message when validation fails
     */
    private String promptUpdateField(String label, String currentValue, Predicate<String> validator, String errorMessage) {
        while (true) {
            System.out.print(label + " [" + currentValue + "]: ");
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                return currentValue;
            }
            if (validator.test(input)) {
                return input;
            }
            System.out.println(errorMessage);
        }
    }

    /**
     * Prints a single staff member's details to the console.
     * @param staff staff to display
     */
    public void showStaff(Staff staff) {
        System.out.println(StaffConstants.DISPLAY_STAFF_INFO);
        System.out.println(StaffConstants.DISPLAY_STAFF_ID + staff.getStaffId());
        System.out.println(StaffConstants.DISPLAY_PASSWORD + staff.getpassword());
        System.out.println(StaffConstants.DISPLAY_FIRST_NAME + staff.getname().getFirstName());
        System.out.println(StaffConstants.DISPLAY_LAST_NAME + staff.getname().getLastName());
        System.out.println(StaffConstants.DISPLAY_PHONE_NO + staff.getphoneNo());
        System.out.println(StaffConstants.DISPLAY_STAFF_POSITION + staff.getStaffPosition());
        System.out.println(StaffConstants.DISPLAY_STREET + staff.getaddress().getstreet());
        System.out.println(StaffConstants.DISPLAY_POSTCODE + staff.getaddress().getpostcode());
        System.out.println(StaffConstants.DISPLAY_REGION + staff.getaddress().getregion());
        System.out.println(StaffConstants.DISPLAY_STATE + staff.getaddress().getstate());
    }

    /**
     * Prints a list of staff, one by one.
     * @param staffList staff collection to display
     */
    public void showList(List<Staff> staffList) {
        System.out.println(StaffConstants.DISPLAY_ALL_STAFF);
        for (Staff s : staffList) {
            showStaff(s);
        }
    }

    /**
     * Prints an informational message to the console.
     * @param message text to display
     */
    public void info(String message) {
        System.out.println(message);
    }
}

