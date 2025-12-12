package main.java.model;

// Simple value object for a staff member's name (no inheritance to avoid recursion)
public class Name {
    // Data fields
    private String firstName;
    private String lastName;

    // Constructors
    public Name() {
        firstName = "";
        lastName = "";
    }

    public Name(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Set methods
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Get methods
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}