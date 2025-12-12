package main.java.config;

public enum StaffMenuOption {
    ADD(1, "Add"),
    SEARCH(2, "Search"),
    MODIFY(3, "Modify"),
    DELETE(4, "Delete"),
    BACK(5, "Back");

    private final int optionNumber;
    private final String description;

    StaffMenuOption(int optionNumber, String description) {
        this.optionNumber = optionNumber;
        this.description = description;
    }

    /** Returns the numeric value shown in the menu. */
    public int getOptionNumber() {
        return optionNumber;
    }

    /** Returns the plain-text description. */
    public String getDescription() {
        return description;
    }

    /** Returns the formatted line for menu display. */
    public String getDisplayText() {
        return optionNumber + ". " + description;
    }

    /**
     * Resolves a menu option from its number.
     * @param optionNumber number selected by the user
     * @return matching option or null when not found
     */
    public static StaffMenuOption fromOptionNumber(int optionNumber) {
        for (StaffMenuOption option : values()) {
            if (option.optionNumber == optionNumber) {
                return option;
            }
        }
        return null;
    }
}


