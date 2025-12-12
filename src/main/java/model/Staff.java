package main.java.model;

/**
 * Represents a staff member with login credentials and contact details.
 */
public class Staff {
    private String staffId;
    private String password;
    private Name name;
    private String phoneNo;
    private String staffPosition;
    private Address address;

    /** Creates an empty staff record with placeholder objects. */
    public Staff() {
        this("", "", new Name("", ""), "", "", new Address("", "", "", ""));
    }

    /**
     * Creates a staff record with the provided details.
     * @param staffId unique staff identifier
     * @param password login password
     * @param name person's name
     * @param phoneNo contact phone number
     * @param staffPosition role or position
     * @param address mailing address
     */
    public Staff(String staffId, String password, Name name, String phoneNo, String staffPosition, Address address) {
        this.staffId = staffId;
        this.password = password;
        this.name = name;
        this.phoneNo = phoneNo;
        this.staffPosition = staffPosition;
        this.address = address;
    }

    /** Sets the staff identifier. */
    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    /** Sets the staff password. */
    public void setpassword(String password) {
        this.password = password;
    }

    /** Sets the staff name. */
    public void setname(Name name) {
        this.name = name;
    }

    /** Sets the phone number. */
    public void setphoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    /** Sets the job position. */
    public void setStaffPosition(String staffPosition) {
        this.staffPosition = staffPosition;
    }

    /** Sets the address. */
    public void setaddress(Address address) {
        this.address = address;
    }

    /** Returns the staff identifier. */
    public String getStaffId() {
        return staffId;
    }

    /** Returns the password. */
    public String getpassword() {
        return password;
    }

    /** Returns the name. */
    public Name getname() {
        return name;
    }

    /** Returns the phone number. */
    public String getphoneNo() {
        return phoneNo;
    }

    /** Returns the job position. */
    public String getStaffPosition() {
        return staffPosition;
    }

    /** Returns the address. */
    public Address getaddress() {
        return address;
    }
}