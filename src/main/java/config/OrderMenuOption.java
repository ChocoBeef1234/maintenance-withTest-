package main.java.config;

public enum OrderMenuOption {
    ADD(1, "Add"),
    SEARCH(2, "Search"),
    UPDATE(3, "Update"),
    DELETE(4, "Delete"),
    BACK(5, "Back");

    private final int value;
    private final String description;

    OrderMenuOption(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayText() {
        return value + ". " + description;
    }

    public static OrderMenuOption fromValue(int value) {
        for (OrderMenuOption option : OrderMenuOption.values()) {
            if (option.value == value) {
                return option;
            }
        }
        return null;
    }
}
