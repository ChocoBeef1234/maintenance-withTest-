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

public class StaffRepository {

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

    public boolean add(Staff staff) throws IOException {
        File staffFile = new File(FilePaths.STAFF);
        if (!staffFile.exists()) {
            return false;
        }
        if (findById(staff.getStaffId()) != null) {
            return false;
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(staffFile, true))) {
            writer.println(toLine(staff));
        }
        return true;
    }

    public boolean update(String id, Staff updated) throws IOException {
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
                if (staff != null && staff.getStaffId().equals(id)) {
                    writer.println(toLine(updated));
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

    public Staff validateCredentials(String id, String password) throws IOException {
        Staff staff = findById(id);
        if (staff != null && staff.getpassword().equals(password)) {
            return staff;
        }
        return null;
    }

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

