package test.java;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.java.controller.*;
import main.java.model.*;
import main.java.repository.*;
import main.java.util.PasswordUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestStaff {

    // --- File Management Constants and Setup ---

    private static final String STAFF_FILE_PATH = "staff.txt";
    private static final String TEMP_FILE_PATH = "NewStaff.txt";
    private static byte[] initialContent; // Holds the backup of the original file content
    private StaffRepository repository;

    // --- System Input/Output Mocking Setup ---
    private final InputStream originalSystemIn = System.in;
    private final PrintStream originalSystemOut = System.out;
    private ByteArrayOutputStream outputStreamCaptor;

    /**
     * Runs once before all tests.
     * Reads and stores the initial content of staff.txt for restoration.
     */
    @BeforeAll
    static void setupAll() throws IOException {
        Path staffPath = Path.of(STAFF_FILE_PATH);

        if (Files.exists(staffPath)) {
            // Read the original content for backup
            initialContent = Files.readAllBytes(staffPath);
        } else {
            // If staff.txt is missing, create a placeholder with test data
            String placeholderContent = 
                "S1001||password123||John||Doe||012-345-6789||Pharmacist||123 Main St||12345||Region1||State1\n" +
                "S1002||pass456||Jane||Smith||013-456-7890||Assistant||456 Oak Ave||54321||Region2||State2\n";
            Files.write(staffPath, placeholderContent.getBytes(), StandardOpenOption.CREATE);
            initialContent = Files.readAllBytes(staffPath);
        }
    }

    /**
     * Runs before each test to restore staff.txt to its initial state,
     * initialize the repository, and set up input/output mocking.
     */
    @BeforeEach
    void setup() throws IOException {
        // Restore the file content for a clean starting state
        Files.write(
                Path.of(STAFF_FILE_PATH),
                initialContent,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        // Clean up any temporary file left by a previous failed test
        Files.deleteIfExists(Path.of(TEMP_FILE_PATH));

        repository = new StaffRepository();

        // Setup for input/output mocking
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    /**
     * Cleans up the temporary file and restores System.in/out after each test.
     */
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Path.of(TEMP_FILE_PATH));

        // Restore System.in and System.out
        System.setIn(originalSystemIn);
        System.setOut(originalSystemOut);
    }

    /** Helper to set the mocked System.in input */
    private void setInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
    }

    // ==================== I. StaffRepository Tests (CRUD) ====================

    // --- 1. Create (Add) Tests ---

    @Test
    void add_newStaff_shouldReturnTrueAndBeFindable() throws IOException {
        // Arrange
        Staff newStaff = createTestStaff("S9999", "testpass", "Test", "User", 
                "012-345-6789", "Manager", "Test St", "12345", "Test Region", "Test State");

        // Act
        boolean added = repository.add(newStaff);
        Staff found = repository.findById("S9999");

        // Assert
        assertTrue(added, "The add method should return true for a new staff member.");
        assertNotNull(found, "The added staff member should be findable by ID.");
        assertEquals("S9999", found.getStaffId(), "Staff ID should match.");
        assertEquals("Test", found.getname().getFirstName(), "First name should match.");
    }

    @Test
    void add_duplicateStaffId_shouldReturnFalse() throws IOException {
        // Arrange: S1001 is assumed to exist from setupAll()
        Staff duplicate = createTestStaff("S1001", "newpass", "Duplicate", "Staff", 
                "012-345-6789", "Position", "Street", "12345", "Region", "State");

        // Act
        boolean added = repository.add(duplicate);

        // Assert
        assertFalse(added, "Adding a duplicate staff ID should return false.");
    }

    @Test
    void add_whenFileDoesNotExist_shouldReturnFalse() throws IOException {
        // Arrange: Delete the file first
        Files.deleteIfExists(Path.of(STAFF_FILE_PATH));
        Staff newStaff = createTestStaff("S9998", "pass", "Test", "User", 
                "012-345-6789", "Position", "Street", "12345", "Region", "State");

        // Act
        boolean added = repository.add(newStaff);

        // Assert
        assertFalse(added, "Adding when file doesn't exist should return false.");
    }

    // --- 2. Read (Search/Find) Tests ---

    @Test
    void findById_existingStaff_shouldReturnStaff() throws IOException {
        // Act
        Staff found = repository.findById("S1001"); // S1001 is assumed to exist from setupAll()

        // Assert
        assertNotNull(found, "Existing staff should be found.");
        assertEquals("S1001", found.getStaffId(), "Staff ID should match.");
    }

    @Test
    void findById_nonExistingStaff_shouldReturnNull() throws IOException {
        // Act
        Staff found = repository.findById("S9999");

        // Assert
        assertNull(found, "Non-existing staff should return null.");
    }

    @Test
    void findAll_shouldReturnAllStaff_isConsistent() throws IOException {
        // Arrange
        // Calculate the expected count dynamically from the initial backup
        String initialData = new String(initialContent);
        long expectedCount = initialData.lines().filter(line -> !line.trim().isEmpty()).count();

        // Act
        List<Staff> allStaff = repository.findAll();

        // Assert
        assertEquals(expectedCount, allStaff.size(),
                "findAll should return a list matching the number of non-empty lines in the original staff.txt file.");
        assertTrue(allStaff.size() > 0, "Should have at least one staff member.");
    }

    @Test
    void findAll_whenFileDoesNotExist_shouldReturnEmptyList() throws IOException {
        // Arrange: Delete the file
        Files.deleteIfExists(Path.of(STAFF_FILE_PATH));

        // Act
        List<Staff> allStaff = repository.findAll();

        // Assert
        assertNotNull(allStaff, "Should return a list (not null).");
        assertEquals(0, allStaff.size(), "Should return an empty list when file doesn't exist.");
    }

    // --- 3. Update (Modify) Tests ---

    @Test
    void update_existingStaff_shouldSucceed() throws IOException {
        // Arrange: Add S9997 first, then update it
        String testId = "S9997";
        Staff original = createTestStaff(testId, "oldpass", "Original", "Name", 
                "012-345-6789", "Old Position", "Old Street", "12345", "Old Region", "Old State");
        repository.add(original);

        Staff preUpdate = repository.findById(testId);
        assumeTrue(preUpdate != null, testId + " must exist for this test.");

        // Update staff information
        Staff updated = createTestStaff(testId, "newpass", "Updated", "Name", 
                "013-456-7890", "New Position", "New Street", "54321", "New Region", "New State");

        // Act
        boolean updatedResult = repository.update(testId, updated);
        Staff found = repository.findById(testId);

        // Assert
        assertTrue(updatedResult, "Update should return true for an existing staff member.");
        assertNotNull(found, "Updated staff should still be findable.");
        assertEquals("Updated", found.getname().getFirstName(), "First name should be updated.");
        assertEquals("New Position", found.getStaffPosition(), "Position should be updated.");
        assertTrue(PasswordUtil.verifyPassword("newpass", found.getpassword()), "Password should be updated and verifiable.");
    }

    @Test
    void update_nonExistingStaff_shouldReturnFalse() throws IOException {
        // Arrange
        Staff updated = createTestStaff("S9999", "pass", "Test", "User", 
                "012-345-6789", "Position", "Street", "12345", "Region", "State");

        // Act
        boolean updatedResult = repository.update("S9999", updated);

        // Assert
        assertFalse(updatedResult, "Update should return false for non-existing staff.");
    }

    @Test
    void update_whenFileDoesNotExist_shouldReturnFalse() throws IOException {
        // Arrange: Delete the file
        Files.deleteIfExists(Path.of(STAFF_FILE_PATH));
        Staff updated = createTestStaff("S1001", "pass", "Test", "User", 
                "012-345-6789", "Position", "Street", "12345", "Region", "State");

        // Act
        boolean updatedResult = repository.update("S1001", updated);

        // Assert
        assertFalse(updatedResult, "Update should return false when file doesn't exist.");
    }

    // --- 4. Delete Tests ---

    @Test
    void delete_existingStaff_shouldSucceedAndBeUnfindable() throws IOException {
        // Arrange: Add S9996, then delete it
        String testId = "S9996";
        Staff newStaff = createTestStaff(testId, "pass", "ToDelete", "Staff", 
                "012-345-6789", "Position", "Street", "12345", "Region", "State");
        repository.add(newStaff);

        assertNotNull(repository.findById(testId), testId + " should exist before deletion.");
        int initialCount = repository.findAll().size();

        // Act
        boolean deleted = repository.delete(testId);
        Staff foundAfterDelete = repository.findById(testId);
        int finalCount = repository.findAll().size();

        // Assert
        assertTrue(deleted, "Delete should return true for an existing staff ID (" + testId + ").");
        assertNull(foundAfterDelete, testId + " should not be found after deletion.");
        assertEquals(initialCount - 1, finalCount, "Total staff count should decrease by 1.");
    }

    @Test
    void delete_nonExistingStaff_shouldReturnFalse() throws IOException {
        // Act
        boolean deleted = repository.delete("S9999");

        // Assert
        assertFalse(deleted, "Delete should return false for non-existing staff.");
    }

    @Test
    void delete_whenFileDoesNotExist_shouldReturnFalse() throws IOException {
        // Arrange: Delete the file
        Files.deleteIfExists(Path.of(STAFF_FILE_PATH));

        // Act
        boolean deleted = repository.delete("S1001");

        // Assert
        assertFalse(deleted, "Delete should return false when file doesn't exist.");
    }

    // --- 5. Credential Validation Tests ---

    @Test
    void validateCredentials_validIdAndPassword_shouldReturnStaff() throws IOException {
        // Arrange: S1001 exists with password from setupAll()
        // Note: Assuming S1001 has password "0123456789" based on sample data

        // Act
        Staff validated = repository.validateCredentials("S1001", "password123");

        // Assert
        assertNotNull(validated, "Valid credentials should return staff object.");
        assertEquals("S1001", validated.getStaffId(), "Staff ID should match.");
    }

    @Test
    void validateCredentials_invalidId_shouldReturnNull() throws IOException {
        // Act
        Staff validated = repository.validateCredentials("S9999", "anypassword");

        // Assert
        assertNull(validated, "Invalid staff ID should return null.");
    }

    @Test
    void validateCredentials_invalidPassword_shouldReturnNull() throws IOException {
        // Arrange: S1001 exists but with different password
        // Act
        Staff validated = repository.validateCredentials("S1001", "wrongpassword");

        // Assert
        assertNull(validated, "Invalid password should return null.");
    }

    // ==================== II. StaffController Tests ====================

    @Test
    void controllerRun_exitMenu_shouldNotLoop() {
        // Arrange: User enters 5 to exit the loop immediately
        String input = "5\n" +  // Expected Exit
                       "5\n" +  // Safety Buffer 1
                       "5\n";   // Safety Buffer 2
        setInput(input);

        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Welcome to Staff Interface"), "Menu should be displayed.");
    }

    @Test
    void controllerRun_invalidMenuOption_shouldDisplayError() {
        // Arrange
        String input = "99\n" + // Invalid option
                       "5\n";   // Exit
        setInput(input);

        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Invalid input"), "Invalid input should be displayed.");
    }

    @Test
    void controllerHandleAdd_validStaff_shouldSucceed() throws IOException {
        // Arrange
        String mockedInput = 
            "1\n" + // Menu Selection: 1. Add
            "S9995\n" + // Staff ID
            "password123\n" + // Password
            "TestFirst\n" + // First Name
            "TestLast\n" + // Last Name
            "012-345-6789\n" + // Phone
            "Manager\n" + // Position
            "Test Street\n" + // Street
            "12345\n" + // Postcode
            "Test Region\n" + // Region
            "Test State\n" + // State
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Staff added successfully"), "Staff should be added successfully.");
        
        // Verify staff was actually added
        Staff added = repository.findById("S9995");
        assertNotNull(added, "Staff S9995 should be in the repository.");
    }

    @Test
    void controllerHandleAdd_invalidStaffIdFormat_shouldDisplayError() {
        // Arrange
        String mockedInput = 
            "1\n" + // Menu Selection: 1. Add
            "INVALID\n" + // Invalid Staff ID format
            "S9994\n" + // Valid Staff ID (after retry)
            "password123\n" + // Password
            "TestFirst\n" + // First Name
            "TestLast\n" + // Last Name
            "012-345-6789\n" + // Phone
            "Manager\n" + // Position
            "Test Street\n" + // Street
            "12345\n" + // Postcode
            "Test Region\n" + // Region
            "Test State\n" + // State
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Invalid Staff ID format"), "Invalid format error should be displayed.");
    }

    @Test
    void controllerHandleAdd_duplicateStaffId_shouldDisplayError() {
        // Arrange: S1001 already exists
        String mockedInput = 
            "1\n" + // Menu Selection: 1. Add
            "S1001\n" + // Duplicate Staff ID
            "password123\n" + // Password
            "TestFirst\n" + // First Name
            "TestLast\n" + // Last Name
            "012-345-6789\n" + // Phone
            "Manager\n" + // Position
            "Test Street\n" + // Street
            "12345\n" + // Postcode
            "Test Region\n" + // Region
            "Test State\n" + // State
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Staff ID already exists"), "Duplicate ID error should be displayed.");
    }

    @Test
    void controllerHandleSearch_existingStaff_shouldDisplayStaff() {
        // Arrange: S1001 exists
        String mockedInput = 
            "2\n" + // Menu Selection: 2. Search
            "S1001\n" + // Staff ID to search
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Staff Information:"), "Staff information should be displayed.");
        assertTrue(output.contains("S1001"), "Staff ID should be in the output.");
    }

    @Test
    void controllerHandleSearch_nonExistingStaff_shouldDisplayNotFound() {
        // Arrange
        String mockedInput = 
            "2\n" + // Menu Selection: 2. Search
            "S9999\n" + // Non-existing Staff ID
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Staff not found"), "Not found message should be displayed.");
    }

    @Test
    void controllerHandleModify_existingStaff_shouldSucceed() {
        // Arrange: S1001 exists
        String mockedInput = 
            "3\n" + // Menu Selection: 3. Modify
            "S1001\n" + // Staff ID to modify
            "\n" + // Staff ID (keep existing)
            "\n" + // Password (keep existing)
            "UpdatedFirst\n" + // New First Name
            "\n" + // Last Name (keep existing)
            "\n" + // Phone (keep existing)
            "\n" + // Position (keep existing)
            "\n" + // Street (keep existing)
            "\n" + // Postcode (keep existing)
            "\n" + // Region (keep existing)
            "\n" + // State (keep existing)
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Staff modified successfully"), "Modify success message should be displayed.");
    }

    @Test
    void controllerHandleModify_nonExistingStaff_shouldDisplayNotFound() {
        // Arrange
        String mockedInput = 
            "3\n" + // Menu Selection: 3. Modify
            "S9999\n" + // Non-existing Staff ID
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Staff not found"), "Not found message should be displayed.");
    }

    @Test
    void controllerHandleDelete_existingStaff_shouldSucceed() throws IOException {
        // Arrange: Add S9993 first, then delete it
        Staff toDelete = createTestStaff("S9993", "pass", "Delete", "Me", 
                "012-345-6789", "Position", "Street", "12345", "Region", "State");
        repository.add(toDelete);

        String mockedInput = 
            "4\n" + // Menu Selection: 4. Delete
            "S9993\n" + // Staff ID to delete
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Staff deleted successfully"), "Delete success message should be displayed.");
        
        // Verify staff was actually deleted
        Staff deleted = repository.findById("S9993");
        assertNull(deleted, "Staff S9993 should not exist after deletion.");
    }

    @Test
    void controllerHandleDelete_nonExistingStaff_shouldDisplayNotFound() {
        // Arrange
        String mockedInput = 
            "4\n" + // Menu Selection: 4. Delete
            "S9999\n" + // Non-existing Staff ID
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Staff not found"), "Not found message should be displayed.");
    }

    // ==================== III. StaffView Input Validation Tests ====================

    @Test
    void viewPromptNew_invalidStaffIdFormat_shouldRePrompt() {
        // Arrange
        String mockedInput = 
            "1\n" + // Menu Selection: 1. Add
            "INVALID\n" + // Invalid Staff ID (should re-prompt)
            "S9992\n" + // Valid Staff ID
            "password123\n" + // Password
            "TestFirst\n" + // First Name
            "TestLast\n" + // Last Name
            "012-345-6789\n" + // Phone
            "Manager\n" + // Position
            "Test Street\n" + // Street
            "12345\n" + // Postcode
            "Test Region\n" + // Region
            "Test State\n" + // State
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Invalid Staff ID format"), "Invalid format error should be displayed.");
    }

    @Test
    void viewPromptNew_invalidPhoneFormat_shouldRePrompt() {
        // Arrange
        String mockedInput = 
            "1\n" + // Menu Selection: 1. Add
            "S9991\n" + // Staff ID
            "password123\n" + // Password
            "TestFirst\n" + // First Name
            "TestLast\n" + // Last Name
            "INVALID-PHONE\n" + // Invalid phone (should re-prompt)
            "012-345-6789\n" + // Valid phone
            "Manager\n" + // Position
            "Test Street\n" + // Street
            "12345\n" + // Postcode
            "Test Region\n" + // Region
            "Test State\n" + // State
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Invalid phone number format"), "Invalid phone format error should be displayed.");
    }

    @Test
    void viewPromptNew_invalidPostcodeFormat_shouldRePrompt() {
        // Arrange
        String mockedInput = 
            "1\n" + // Menu Selection: 1. Add
            "S9990\n" + // Staff ID
            "password123\n" + // Password
            "TestFirst\n" + // First Name
            "TestLast\n" + // Last Name
            "012-345-6789\n" + // Phone
            "Manager\n" + // Position
            "Test Street\n" + // Street
            "INVALID\n" + // Invalid postcode (should re-prompt)
            "12345\n" + // Valid postcode
            "Test Region\n" + // Region
            "Test State\n" + // State
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Invalid postcode format"), "Invalid postcode format error should be displayed.");
    }

    @Test
    void viewPromptNew_emptyRequiredFields_shouldRePrompt() {
        // Arrange
        String mockedInput = 
            "1\n" + // Menu Selection: 1. Add
            "S9989\n" + // Staff ID
            "\n" + // Empty password (should re-prompt)
            "password123\n" + // Valid password
            "\n" + // Empty first name (should re-prompt)
            "TestFirst\n" + // Valid first name
            "TestLast\n" + // Last Name
            "012-345-6789\n" + // Phone
            "Manager\n" + // Position
            "Test Street\n" + // Street
            "12345\n" + // Postcode
            "Test Region\n" + // Region
            "Test State\n" + // State
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("This field cannot be empty"), "Required field error should be displayed.");
    }

    @Test
    void viewPromptUpdate_leaveFieldsBlank_shouldKeepExistingValues() {
        // Arrange: S1001 exists
        String mockedInput = 
            "3\n" + // Menu Selection: 3. Modify
            "S1001\n" + // Staff ID to modify
            "\n" + // Staff ID (keep existing)
            "\n" + // Password (keep existing)
            "\n" + // First Name (keep existing)
            "\n" + // Last Name (keep existing)
            "\n" + // Phone (keep existing)
            "\n" + // Position (keep existing)
            "\n" + // Street (keep existing)
            "\n" + // Postcode (keep existing)
            "\n" + // Region (keep existing)
            "\n" + // State (keep existing)
            "5\n" + // Exit menu
            "5\n"; // Safety buffer

        setInput(mockedInput);
        StaffController controller = new StaffController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Staff modified successfully"), "Modify should succeed with blank fields.");
    }

    // ==================== IV. Edge Cases and Error Scenarios ====================

    @Test
    void parseStaffLine_invalidLineFormat_shouldReturnNull() throws IOException {
        // Arrange: Add a malformed line to the file
        String malformedLine = "INCOMPLETE||LINE||WITH||FEW||FIELDS\n";
        Files.write(Path.of(STAFF_FILE_PATH), malformedLine.getBytes(), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Act
        List<Staff> allStaff = repository.findAll();

        // Assert
        assertEquals(0, allStaff.size(), "Malformed lines should be skipped.");
    }

    @Test
    void parseStaffLine_emptyLine_shouldBeSkipped() throws IOException {
        // Arrange: Add empty lines to the file
        String contentWithEmptyLines = 
            "S1001||pass||John||Doe||012-345-6789||Position||Street||12345||Region||State\n" +
            "\n" + // Empty line
            "S1002||pass||Jane||Smith||013-456-7890||Position||Street||54321||Region||State\n";
        Files.write(Path.of(STAFF_FILE_PATH), contentWithEmptyLines.getBytes(), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Act
        List<Staff> allStaff = repository.findAll();

        // Assert
        assertEquals(2, allStaff.size(), "Empty lines should be skipped.");
    }

    @Test
    void update_partialUpdate_shouldPreserveOtherFields() throws IOException {
        // Arrange: Add S9988 first
        String testId = "S9988";
        Staff original = createTestStaff(testId, "oldpass", "Original", "Name", 
                "012-345-6789", "Old Position", "Old Street", "12345", "Old Region", "Old State");
        repository.add(original);

        // Update only first name
        Staff updated = createTestStaff(testId, "oldpass", "Updated", "Name", 
                "012-345-6789", "Old Position", "Old Street", "12345", "Old Region", "Old State");

        // Act
        repository.update(testId, updated);
        Staff found = repository.findById(testId);

        // Assert
        assertNotNull(found, "Staff should still exist.");
        assertEquals("Updated", found.getname().getFirstName(), "First name should be updated.");
        assertEquals("Old Position", found.getStaffPosition(), "Position should remain unchanged.");
        assertTrue(PasswordUtil.verifyPassword("oldpass", found.getpassword()), "Password should remain unchanged and verifiable.");
    }

    @Test
    void findAll_afterMultipleOperations_shouldBeConsistent() throws IOException {
        // Arrange
        int initialCount = repository.findAll().size();
        
        // Add staff
        Staff staff1 = createTestStaff("S9987", "pass1", "First", "One", 
                "012-345-6789", "Pos1", "St1", "11111", "Reg1", "St1");
        Staff staff2 = createTestStaff("S9986", "pass2", "Second", "Two", 
                "013-456-7890", "Pos2", "St2", "22222", "Reg2", "St2");
        repository.add(staff1);
        repository.add(staff2);
        
        // Delete one
        repository.delete("S9987");
        
        // Update one
        Staff updated = createTestStaff("S9986", "newpass", "Updated", "Two", 
                "013-456-7890", "Pos2", "St2", "22222", "Reg2", "St2");
        repository.update("S9986", updated);

        // Act
        List<Staff> allStaff = repository.findAll();

        // Assert
        assertEquals(initialCount + 1, allStaff.size(), "Count should be consistent after operations.");
        Staff found = repository.findById("S9986");
        assertNotNull(found, "Updated staff should exist.");
        assertEquals("Updated", found.getname().getFirstName(), "Update should be reflected.");
    }

    // ==================== IV. Model Classes Tests ====================

    // --- Address Model Tests ---

    @Test
    void address_defaultConstructor_shouldCreateEmptyAddress() {
        // Act
        Address address = new Address();

        // Assert
        assertEquals("", address.getstreet(), "Street should be empty.");
        assertEquals("", address.getpostcode(), "Postcode should be empty.");
        assertEquals("", address.getregion(), "Region should be empty.");
        assertEquals("", address.getstate(), "State should be empty.");
    }

    @Test
    void address_parameterizedConstructor_shouldSetAllFields() {
        // Act
        Address address = new Address("123 Main St", "12345", "Region1", "State1");

        // Assert
        assertEquals("123 Main St", address.getstreet(), "Street should be set.");
        assertEquals("12345", address.getpostcode(), "Postcode should be set.");
        assertEquals("Region1", address.getregion(), "Region should be set.");
        assertEquals("State1", address.getstate(), "State should be set.");
    }

    @Test
    void address_setters_shouldUpdateFields() {
        // Arrange
        Address address = new Address();

        // Act
        address.setstreet("456 Oak Ave");
        address.setpostcode("54321");
        address.setregion("Region2");
        address.setstate("State2");

        // Assert
        assertEquals("456 Oak Ave", address.getstreet(), "Street should be updated.");
        assertEquals("54321", address.getpostcode(), "Postcode should be updated.");
        assertEquals("Region2", address.getregion(), "Region should be updated.");
        assertEquals("State2", address.getstate(), "State should be updated.");
    }

    @Test
    void address_setters_shouldHandleNullValues() {
        // Arrange
        Address address = new Address("Street", "12345", "Region", "State");

        // Act
        address.setstreet(null);
        address.setpostcode(null);
        address.setregion(null);
        address.setstate(null);

        // Assert
        assertNull(address.getstreet(), "Street should be null.");
        assertNull(address.getpostcode(), "Postcode should be null.");
        assertNull(address.getregion(), "Region should be null.");
        assertNull(address.getstate(), "State should be null.");
    }

    // --- Name Model Tests ---

    @Test
    void name_defaultConstructor_shouldCreateEmptyName() {
        // Act
        Name name = new Name();

        // Assert
        assertEquals("", name.getFirstName(), "First name should be empty.");
        assertEquals("", name.getLastName(), "Last name should be empty.");
    }

    @Test
    void name_parameterizedConstructor_shouldSetBothNames() {
        // Act
        Name name = new Name("John", "Doe");

        // Assert
        assertEquals("John", name.getFirstName(), "First name should be set.");
        assertEquals("Doe", name.getLastName(), "Last name should be set.");
    }

    @Test
    void name_setFirstName_shouldUpdateFirstName() {
        // Arrange
        Name name = new Name("John", "Doe");

        // Act
        name.setFirstName("Jane");

        // Assert
        assertEquals("Jane", name.getFirstName(), "First name should be updated.");
        assertEquals("Doe", name.getLastName(), "Last name should remain unchanged.");
    }

    @Test
    void name_setLastName_shouldUpdateLastName() {
        // Arrange
        Name name = new Name("John", "Doe");

        // Act
        name.setLastName("Smith");

        // Assert
        assertEquals("John", name.getFirstName(), "First name should remain unchanged.");
        assertEquals("Smith", name.getLastName(), "Last name should be updated.");
    }

    @Test
    void name_setters_shouldHandleNullValues() {
        // Arrange
        Name name = new Name("John", "Doe");

        // Act
        name.setFirstName(null);
        name.setLastName(null);

        // Assert
        assertNull(name.getFirstName(), "First name should be null.");
        assertNull(name.getLastName(), "Last name should be null.");
    }

    @Test
    void name_setters_shouldHandleEmptyStrings() {
        // Arrange
        Name name = new Name("John", "Doe");

        // Act
        name.setFirstName("");
        name.setLastName("");

        // Assert
        assertEquals("", name.getFirstName(), "First name should be empty.");
        assertEquals("", name.getLastName(), "Last name should be empty.");
    }

    // --- Staff Model Tests ---

    @Test
    void staff_defaultConstructor_shouldCreateEmptyStaff() {
        // Act
        Staff staff = new Staff();

        // Assert
        assertEquals("", staff.getStaffId(), "Staff ID should be empty.");
        assertEquals("", staff.getpassword(), "Password should be empty.");
        assertNotNull(staff.getname(), "Name should not be null.");
        assertEquals("", staff.getname().getFirstName(), "First name should be empty.");
        assertEquals("", staff.getname().getLastName(), "Last name should be empty.");
        assertEquals("", staff.getphoneNo(), "Phone number should be empty.");
        assertEquals("", staff.getStaffPosition(), "Position should be empty.");
        assertNotNull(staff.getaddress(), "Address should not be null.");
        assertEquals("", staff.getaddress().getstreet(), "Street should be empty.");
    }

    @Test
    void staff_parameterizedConstructor_shouldSetAllFields() {
        // Arrange
        Name name = new Name("John", "Doe");
        Address address = new Address("123 Main St", "12345", "Region1", "State1");

        // Act
        Staff staff = new Staff("S1001", "password123", name, "012-345-6789", "Pharmacist", address);

        // Assert
        assertEquals("S1001", staff.getStaffId(), "Staff ID should be set.");
        assertEquals("password123", staff.getpassword(), "Password should be set.");
        assertEquals("John", staff.getname().getFirstName(), "First name should be set.");
        assertEquals("Doe", staff.getname().getLastName(), "Last name should be set.");
        assertEquals("012-345-6789", staff.getphoneNo(), "Phone number should be set.");
        assertEquals("Pharmacist", staff.getStaffPosition(), "Position should be set.");
        assertEquals("123 Main St", staff.getaddress().getstreet(), "Street should be set.");
    }

    @Test
    void staff_setStaffId_shouldUpdateStaffId() {
        // Arrange
        Staff staff = new Staff();

        // Act
        staff.setStaffId("S9999");

        // Assert
        assertEquals("S9999", staff.getStaffId(), "Staff ID should be updated.");
    }

    @Test
    void staff_setPassword_shouldUpdatePassword() {
        // Arrange
        Staff staff = new Staff();

        // Act
        staff.setpassword("newpassword");

        // Assert
        assertEquals("newpassword", staff.getpassword(), "Password should be updated.");
    }

    @Test
    void staff_setName_shouldUpdateName() {
        // Arrange
        Staff staff = new Staff();
        Name newName = new Name("Jane", "Smith");

        // Act
        staff.setname(newName);

        // Assert
        assertNotNull(staff.getname(), "Name should not be null.");
        assertEquals("Jane", staff.getname().getFirstName(), "First name should be updated.");
        assertEquals("Smith", staff.getname().getLastName(), "Last name should be updated.");
    }

    @Test
    void staff_setPhoneNo_shouldUpdatePhoneNo() {
        // Arrange
        Staff staff = new Staff();

        // Act
        staff.setphoneNo("013-456-7890");

        // Assert
        assertEquals("013-456-7890", staff.getphoneNo(), "Phone number should be updated.");
    }

    @Test
    void staff_setStaffPosition_shouldUpdatePosition() {
        // Arrange
        Staff staff = new Staff();

        // Act
        staff.setStaffPosition("Manager");

        // Assert
        assertEquals("Manager", staff.getStaffPosition(), "Position should be updated.");
    }

    @Test
    void staff_setAddress_shouldUpdateAddress() {
        // Arrange
        Staff staff = new Staff();
        Address newAddress = new Address("456 Oak Ave", "54321", "Region2", "State2");

        // Act
        staff.setaddress(newAddress);

        // Assert
        assertNotNull(staff.getaddress(), "Address should not be null.");
        assertEquals("456 Oak Ave", staff.getaddress().getstreet(), "Street should be updated.");
        assertEquals("54321", staff.getaddress().getpostcode(), "Postcode should be updated.");
        assertEquals("Region2", staff.getaddress().getregion(), "Region should be updated.");
        assertEquals("State2", staff.getaddress().getstate(), "State should be updated.");
    }

    @Test
    void staff_setters_shouldHandleNullValues() {
        // Arrange
        Staff staff = new Staff("S1001", "pass", new Name("John", "Doe"), "012-345-6789", "Position", new Address("St", "12345", "Reg", "State"));

        // Act
        staff.setStaffId(null);
        staff.setpassword(null);
        staff.setname(null);
        staff.setphoneNo(null);
        staff.setStaffPosition(null);
        staff.setaddress(null);

        // Assert
        assertNull(staff.getStaffId(), "Staff ID should be null.");
        assertNull(staff.getpassword(), "Password should be null.");
        assertNull(staff.getname(), "Name should be null.");
        assertNull(staff.getphoneNo(), "Phone number should be null.");
        assertNull(staff.getStaffPosition(), "Position should be null.");
        assertNull(staff.getaddress(), "Address should be null.");
    }

    // --- PasswordUtil Tests ---

    @Test
    void passwordUtil_hashPassword_shouldReturnHashedFormat() {
        // Act
        String hashed = PasswordUtil.hashPassword("testpassword");

        // Assert
        assertNotNull(hashed, "Hashed password should not be null.");
        assertTrue(hashed.contains(":"), "Hashed password should contain delimiter.");
        String[] parts = hashed.split(":", 2);
        assertEquals(2, parts.length, "Hashed password should have two parts (salt:hash).");
        assertFalse(parts[0].isEmpty(), "Salt should not be empty.");
        assertFalse(parts[1].isEmpty(), "Hash should not be empty.");
    }

    @Test
    void passwordUtil_hashPassword_samePasswordShouldProduceDifferentHashes() {
        // Act
        String hash1 = PasswordUtil.hashPassword("testpassword");
        String hash2 = PasswordUtil.hashPassword("testpassword");

        // Assert
        assertNotEquals(hash1, hash2, "Same password should produce different hashes due to different salts.");
    }

    @Test
    void passwordUtil_hashPassword_nullPassword_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> PasswordUtil.hashPassword(null),
            "Should throw IllegalArgumentException for null password.");
        assertTrue(exception.getMessage().contains("null"), "Exception message should mention null.");
    }

    @Test
    void passwordUtil_hashPassword_emptyPassword_shouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> PasswordUtil.hashPassword(""),
            "Should throw IllegalArgumentException for empty password.");
        assertTrue(exception.getMessage().contains("empty"), "Exception message should mention empty.");
    }

    @Test
    void passwordUtil_verifyPassword_newFormat_shouldVerifyCorrectly() {
        // Arrange
        String plainPassword = "testpassword";
        String hashed = PasswordUtil.hashPassword(plainPassword);

        // Act & Assert
        assertTrue(PasswordUtil.verifyPassword(plainPassword, hashed), 
            "Should verify correct password with new format.");
        assertFalse(PasswordUtil.verifyPassword("wrongpassword", hashed), 
            "Should reject incorrect password.");
    }

    @Test
    void passwordUtil_verifyPassword_plainText_shouldVerifyCorrectly() {
        // Arrange
        String plainPassword = "testpassword";
        String storedPassword = "testpassword";

        // Act & Assert
        assertTrue(PasswordUtil.verifyPassword(plainPassword, storedPassword), 
            "Should verify correct plain text password.");
        assertFalse(PasswordUtil.verifyPassword("wrongpassword", storedPassword), 
            "Should reject incorrect plain text password.");
    }

    @Test
    void passwordUtil_verifyPassword_hexHash_shouldVerifyCorrectly() {
        // Arrange - SHA-256 hash of "password123" in hex
        String plainPassword = "password123";
        String hexHash = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f";

        // Act & Assert
        assertTrue(PasswordUtil.verifyPassword(plainPassword, hexHash), 
            "Should verify correct password with hex hash format.");
        assertFalse(PasswordUtil.verifyPassword("wrongpassword", hexHash), 
            "Should reject incorrect password with hex hash format.");
    }

    @Test
    void passwordUtil_verifyPassword_hexHash_caseInsensitive_shouldWork() {
        // Arrange - SHA-256 hash of "password123" in uppercase hex
        String plainPassword = "password123";
        String hexHashUpper = "EF92B778BAFE771E89245B89ECBC08A44A4E166C06659911881F383D4473E94F";

        // Act & Assert
        assertTrue(PasswordUtil.verifyPassword(plainPassword, hexHashUpper), 
            "Should verify password with uppercase hex hash (case-insensitive).");
    }

    @Test
    void passwordUtil_verifyPassword_nullInputs_shouldReturnFalse() {
        // Act & Assert
        assertFalse(PasswordUtil.verifyPassword(null, "hashed"), 
            "Should return false for null plain password.");
        assertFalse(PasswordUtil.verifyPassword("password", null), 
            "Should return false for null hashed password.");
        assertFalse(PasswordUtil.verifyPassword(null, null), 
            "Should return false for both null inputs.");
    }

    @Test
    void passwordUtil_verifyPassword_invalidFormat_shouldReturnFalse() {
        // Arrange - Invalid format (has delimiter but invalid Base64)
        String invalidHash = "invalid:base64:format";

        // Act & Assert
        assertFalse(PasswordUtil.verifyPassword("password", invalidHash), 
            "Should return false for invalid hash format.");
    }

    @Test
    void passwordUtil_verifyPassword_malformedDelimitedFormat_shouldReturnFalse() {
        // Arrange - Has delimiter but only one part
        String malformed = "onlyonepart:";

        // Act & Assert
        assertFalse(PasswordUtil.verifyPassword("password", malformed), 
            "Should return false for malformed delimited format.");
    }

    @Test
    void passwordUtil_isHashed_shouldDetectHashedPasswords() {
        // Act & Assert
        assertTrue(PasswordUtil.isHashed("salt:hash"), 
            "Should detect hashed password with delimiter.");
        assertTrue(PasswordUtil.isHashed("abc:def"), 
            "Should detect any string with delimiter as hashed.");
        assertFalse(PasswordUtil.isHashed("plaintext"), 
            "Should not detect plain text as hashed.");
        assertFalse(PasswordUtil.isHashed(null), 
            "Should not detect null as hashed.");
        assertFalse(PasswordUtil.isHashed(""), 
            "Should not detect empty string as hashed.");
    }

    @Test
    void passwordUtil_verifyPassword_shortHexString_shouldTreatAsPlainText() {
        // Arrange - String that looks like hex but is too short
        String shortHex = "abc123";

        // Act & Assert
        assertFalse(PasswordUtil.verifyPassword("password", shortHex), 
            "Short hex string should be treated as plain text and fail verification.");
    }

    @Test
    void passwordUtil_verifyPassword_invalidHexCharacters_shouldTreatAsPlainText() {
        // Arrange - 64 characters but contains invalid hex characters
        String invalidHex = "g".repeat(64);

        // Act & Assert
        assertFalse(PasswordUtil.verifyPassword("password", invalidHex), 
            "Invalid hex characters should be treated as plain text.");
    }

    /**
     * Helper method to create a test Staff object with all required fields.
     */
    private Staff createTestStaff(String staffId, String password, String firstName, String lastName,
                                   String phoneNo, String position, String street, String postcode,
                                   String region, String state) {
        return new Staff(
                staffId,
                password,
                new Name(firstName, lastName),
                phoneNo,
                position,
                new Address(street, postcode, region, state)
        );
    }
}
