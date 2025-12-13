package main.java.model;

public class Staff {
    private String staffId;
    private String password;
    private Name name;
    private String phoneNo;
    private String staffPosition;
    private Address address;

    public Staff() {
        this("", "", new Name("", ""), "", "", new Address("", "", "", ""));
    }
    
    public Staff(String staffId, String password, Name name, String phoneNo, String staffPosition, Address address) {
        this.staffId = staffId;
        this.password = password;
        this.name = name;
        this.phoneNo = phoneNo;
        this.staffPosition = staffPosition;
        this.address = address;
    }
    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
    public void setpassword(String password) {
        this.password = password;
    }
    public void setname(Name name) {
        this.name = name;
    }
    public void setphoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
    public void setStaffPosition(String staffPosition) {
        this.staffPosition = staffPosition;
    }
    public void setaddress(Address address) {
        this.address = address;
    }
    public String getStaffId() {
        return staffId;
    }
    public String getpassword() {
        return password;
    }
    public Name getname() {
        return name;
    }
    public String getphoneNo() {
        return phoneNo;
    }
    public String getStaffPosition() {
        return staffPosition;
    }
    public Address getaddress() {
        return address;
    }
}