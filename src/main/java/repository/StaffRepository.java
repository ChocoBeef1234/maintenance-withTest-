package main.java.repository;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import main.java.model.Staff;
import main.java.model.Name;
import main.java.model.Address;
import main.java.config.FilePaths;
import main.java.config.StaffConstants;
import main.java.util.PasswordUtil;

public class StaffRepository {

    /**
     * Loads every staff record from the staff file.
     * @return list of parsed staff entries; empty if file missing
     */
    public List<Staff> findAll() throws IOException {
        List<Staff> staffList = new ArrayList<>();
        File staffFile = new File(FilePaths.STAFF);
        if (!staffFile.exists()) {
            return staffList;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(staffFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Staff staff = parseStaffLine(line);
                if (staff != null) {
                    staffList.add(staff);
                }
            }
        }
        return staffList;
    }

    /**
     * Retrieves a staff member by ID.
     * @param staffId ID to search for
     * @return staff if found; otherwise null
     */
    public Staff findById(String staffId) throws IOException {
        File staffFile = new File(FilePaths.STAFF);
        if (!staffFile.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(staffFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Staff staff = parseStaffLine(line);
                if (staff != null && staff.getStaffId().equals(staffId)) {
                    return staff;
                }
            }
        }
        return null;
    }

    /**
     * Appends a new staff record if the file exists and ID is unique.
     * Passwords are automatically hashed before saving.
     * @param staff staff entity to add
     * @return true when added; false for missing file or duplicate ID
     */
    public boolean add(Staff staff) throws IOException {
        File staffFile = new File(FilePaths.STAFF);
        if (!staffFile.exists()) {
            return false;
        }
        if (findById(staff.getStaffId()) != null) {
            return false;
        }
        Staff staffToSave = staff;
        String password = staff.getpassword();
        if (password != null && !PasswordUtil.isHashed(password)) {
            String hashedPassword = PasswordUtil.hashPassword(password);
            staffToSave = new Staff(
                staff.getStaffId(),
                hashedPassword,
                staff.getname(),
                staff.getphoneNo(),
                staff.getStaffPosition(),
                staff.getaddress()
            );
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(staffFile, true))) {
            writer.println(toLine(staffToSave));
        }
        return true;
    }

    /**
     * Updates an existing staff record by ID.
     * Passwords are automatically hashed before saving if not already hashed.
     * @param id current ID to match
     * @param updated new staff data to write
     * @return true when the record is found and replaced
     */
    public boolean update(String id, Staff updated) throws IOException {
        File staffFile = new File(FilePaths.STAFF);
        File newFile = new File(FilePaths.STAFF_TMP);
        if (!staffFile.exists()) {
            return false;
        }
        Staff staffToSave = updated;
        String password = updated.getpassword();
        if (password != null && !PasswordUtil.isHashed(password)) {
            String hashedPassword = PasswordUtil.hashPassword(password);
            staffToSave = new Staff(
                updated.getStaffId(),
                hashedPassword,
                updated.getname(),
                updated.getphoneNo(),
                updated.getStaffPosition(),
                updated.getaddress()
            );
        }
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(staffFile));
             PrintWriter writer = new PrintWriter(new FileWriter(newFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Staff staff = parseStaffLine(line);
                if (staff != null && staff.getStaffId().equals(id)) {
                    writer.println(toLine(staffToSave));
                    found = true;
                } else {
                    writer.println(line);
                }
            }
        }
        if (found) {
            staffFile.delete();
            newFile.renameTo(staffFile);
        } else {
            newFile.delete();
        }
        return found;
    }

    /**
     * Deletes a staff record by ID.
     * @param staffId ID to delete
     * @return true when a record is removed
     */
    public boolean delete(String staffId) throws IOException {
        File staffFile = new File(FilePaths.STAFF);
        File newFile = new File(FilePaths.STAFF_TMP);
        if (!staffFile.exists()) {
            return false;
        }
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(staffFile));
             PrintWriter writer = new PrintWriter(new FileWriter(newFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Staff staff = parseStaffLine(line);
                if (staff != null && staff.getStaffId().equals(staffId)) {
                    found = true;
                    continue;
                }
                writer.println(line);
            }
        }
        if (found) {
            staffFile.delete();
            newFile.renameTo(staffFile);
        } else {
            newFile.delete();
        }
        return found;
    }

    /**
     * Verifies login credentials by comparing stored password.
     * Supports both hashed and plain text passwords for backward compatibility.
     * @param id staff ID
     * @param password plain text password to verify
     * @return staff when credentials match; otherwise null
     */
    public Staff validateCredentials(String id, String password) throws IOException {
        Staff staff = findById(id);
        if (staff != null) {
            String storedPassword = staff.getpassword();
            if (PasswordUtil.verifyPassword(password, storedPassword)) {
                return staff;
            }
        }
        return null;
    }

    /**
     * Converts a file line into a Staff object when the field count matches.
     * @param line raw line from the staff file
     * @return parsed staff or null when the line is invalid
     */
    private Staff parseStaffLine(String line) {
        String[] info = line.split(Pattern.quote(StaffConstants.FIELD_DELIMITER));
        if (info.length >= StaffConstants.REQUIRED_FIELD_COUNT) {
            return new Staff(
                    info[StaffConstants.INDEX_STAFF_ID],
                    info[StaffConstants.INDEX_PASSWORD],
                    new Name(
                            info[StaffConstants.INDEX_FIRST_NAME],
                            info[StaffConstants.INDEX_LAST_NAME]),
                    info[StaffConstants.INDEX_PHONE_NO],
                    info[StaffConstants.INDEX_STAFF_POSITION],
                    new Address(
                            info[StaffConstants.INDEX_STREET],
                            info[StaffConstants.INDEX_POSTCODE],
                            info[StaffConstants.INDEX_REGION],
                            info[StaffConstants.INDEX_STATE]));
        }
        return null;
    }

    /**
     * Serialises a staff entity into a file line using the configured delimiter.
     * @param staff staff to serialise
     * @return delimited string representation
     */
    private String toLine(Staff staff) {
        return staff.getStaffId() + StaffConstants.FIELD_DELIMITER +
                staff.getpassword() + StaffConstants.FIELD_DELIMITER +
                staff.getname().getFirstName() + StaffConstants.FIELD_DELIMITER +
                staff.getname().getLastName() + StaffConstants.FIELD_DELIMITER +
                staff.getphoneNo() + StaffConstants.FIELD_DELIMITER +
                staff.getStaffPosition() + StaffConstants.FIELD_DELIMITER +
                staff.getaddress().getstreet() + StaffConstants.FIELD_DELIMITER +
                staff.getaddress().getpostcode() + StaffConstants.FIELD_DELIMITER +
                staff.getaddress().getregion() + StaffConstants.FIELD_DELIMITER +
                staff.getaddress().getstate();
    }
}

