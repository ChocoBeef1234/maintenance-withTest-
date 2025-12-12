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

    public int getOptionNumber() {
        return optionNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayText() {
        return optionNumber + ". " + description;
    }

    public static StaffMenuOption fromOptionNumber(int optionNumber) {
        for (StaffMenuOption option : values()) {
            if (option.optionNumber == optionNumber) {
                return option;
            }
        }
        return null;
    }
}


