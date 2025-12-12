// File: config/MenuOption.java

package main.java.config;

public enum MenuOption {
    ADD(1, "Add"),
    SEARCH(2, "Search"),
    MODIFY(3, "Modify"),
    DELETE(4, "Delete"),
    EXIT(5, "Back"); // Renamed 'Back' to 'EXIT' for better internal clarity

    private final int value;
    private final String description;

    MenuOption(int value, String description) {
        this.value = value;
        this.description = description;
    }

    // Getter for the integer value (used in ItemController's switch)
    public int getValue() {
        return value;
    }

    // Getter for the display text (used in ItemView's menu)
    public String getDescription() {
        return description;
    }

    // Helper method to look up an enum constant from the integer input
    public static MenuOption fromValue(int value) {
        for (MenuOption option : MenuOption.values()) {
            if (option.value == value) {
                return option;
            }
        }
        return null; // Return null for invalid input
    }
}