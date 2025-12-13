package test.java;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.java.controller.LoginController;
import main.java.view.LoginView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Jupiter tests for LoginController and LoginView,
 * focusing on authentication, validation, and error handling.
 * Note: These tests assume a config.FilePaths.STAFF constant resolves to
 * "staff.txt".
 */
public class TestLogin {

    // --- File Management Constants and Setup ---

    private static final String STAFF_FILE_PATH = "staff.txt";
    private static final String TEMP_FILE_PATH = "NewStaff.txt";
    private static byte[] initialContent; // Holds the backup of the original file content

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
     * Runs before each test to restore staff.txt to its initial state
     * and set up input/output mocking.
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
    

    // ==================== I. LoginController Tests ====================

    // --- 1. Successful Login Tests ---

    @Test
    void validateLogin_validCredentials_firstAttempt_shouldReturnTrue() throws IOException {
        // Arrange: S1001 exists with password "password123" from setupAll()
        String input = "S1001\npassword123\n";
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertTrue(result, "Valid credentials on first attempt should return true.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Enter your username to login"), "Should prompt for username.");
        assertTrue(output.contains("Enter your password"), "Should prompt for password.");
        assertTrue(output.contains("*Login successful!"), "Should show success message.");
        assertFalse(output.contains("*Invalid username or password"), "Should not show failure message.");
    }

    @Test
    void validateLogin_validCredentials_afterFailedAttempts_shouldReturnTrue() throws IOException {
        // Arrange: S1001 exists with password "password123"
        // First attempt: wrong password, second attempt: correct credentials
        String input = "S1001\nwrongpass\n" +  // First attempt (wrong password)
                       "S1001\npassword123\n";  // Second attempt (correct)
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertTrue(result, "Valid credentials after failed attempt should return true.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Login successful!"), "Should show success message.");
        assertTrue(output.contains("*Invalid username or password"), "Should show failure message for first attempt.");
    }

    // --- 2. Failed Login Tests ---

    @Test
    void validateLogin_invalidUsername_shouldReturnFalseAfterMaxAttempts() throws IOException {
        // Arrange: Non-existent username, 3 failed attempts
        String input = "S9999\nanypassword\n" +  // Attempt 1
                       "S9999\nanypassword\n" +  // Attempt 2
                       "S9999\nanypassword\n";   // Attempt 3
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertFalse(result, "Invalid username after max attempts should return false.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Invalid username or password"), "Should show failure message.");
        assertTrue(output.contains("*Too many failed login attempts. Exiting..."), 
                   "Should show max attempts message.");
        // Count occurrences of failure message (should be 3)
        long failureCount = output.lines().filter(line -> line.contains("*Invalid username or password")).count();
        assertEquals(3, failureCount, "Should show failure message 3 times.");
    }

    @Test
    void validateLogin_invalidPassword_shouldReturnFalseAfterMaxAttempts() throws IOException {
        // Arrange: Valid username but wrong password, 3 failed attempts
        String input = "S1001\nwrongpass1\n" +  // Attempt 1
                       "S1001\nwrongpass2\n" +  // Attempt 2
                       "S1001\nwrongpass3\n";   // Attempt 3
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertFalse(result, "Invalid password after max attempts should return false.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Invalid username or password"), "Should show failure message.");
        assertTrue(output.contains("*Too many failed login attempts. Exiting..."), 
                   "Should show max attempts message.");
    }

    @Test
    void validateLogin_mixedInvalidAttempts_shouldReturnFalseAfterMaxAttempts() throws IOException {
        // Arrange: Mix of invalid username and password attempts
        String input = "S9999\nanypass\n" +     // Attempt 1: Invalid username
                       "S1001\nwrongpass\n" +   // Attempt 2: Invalid password
                       "S9998\nanotherpass\n"; // Attempt 3: Invalid username
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertFalse(result, "Mixed invalid attempts after max attempts should return false.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Too many failed login attempts. Exiting..."), 
                   "Should show max attempts message.");
    }

    // --- 3. Max Attempts Boundary Tests ---

    @Test
    void validateLogin_exactlyMaxAttempts_shouldShowMaxAttemptsMessage() throws IOException {
        // Arrange: Exactly 3 failed attempts
        String input = "S9999\npass1\n" +
                       "S9999\npass2\n" +
                       "S9999\npass3\n";
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertFalse(result, "Should return false after exactly max attempts.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Too many failed login attempts. Exiting..."), 
                   "Should show max attempts message.");
    }

    // --- 4. File Not Found Handling Tests ---

    @Test
    void validateLogin_whenFileDoesNotExist_shouldReturnFalse() throws IOException {
        // Arrange: Delete the staff file - findById returns null when file doesn't exist
        // Provide enough input for one login attempt (username and password)
        // Note: When file doesn't exist, findById returns null (no IOException thrown)
        Files.deleteIfExists(Path.of(STAFF_FILE_PATH));
        
        String input = "S1001\n0123456789\ndummy\ndummy\ndummy\ndummy\n";
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        // When file doesn't exist, findById returns null, so validateCredentials returns null
        // This causes login to fail normally (not via IOException)
        assertFalse(result, "Should return false when file doesn't exist (staff not found).");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Invalid username or password"), "Should show failure message.");
    }

    // ==================== II. LoginView Tests ====================

    @Test
    void promptUsername_shouldPromptAndReturnInput() {
        // Arrange
        String input = "S1001\n";
        setInput(input);
        LoginView view = new LoginView();

        // Act
        String username = view.promptUsername();

        // Assert
        assertEquals("S1001", username, "Should return the entered username.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Enter your username to login"), "Should prompt for username.");
    }

    @Test
    void promptUsername_emptyInput_shouldReturnEmptyString() {
        // Arrange
        String input = "\n";
        setInput(input);
        LoginView view = new LoginView();

        // Act
        String username = view.promptUsername();

        // Assert
        assertEquals("", username, "Should return empty string for empty input.");
    }

    @Test
    void promptPassword_shouldPromptAndReturnInput() {
        // Arrange
        String input = "mypassword123\n";
        setInput(input);
        LoginView view = new LoginView();

        // Act
        String password = view.promptPassword();

        // Assert
        assertEquals("mypassword123", password, "Should return the entered password.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Enter your password"), "Should prompt for password.");
    }

    @Test
    void promptPassword_emptyInput_shouldReturnEmptyString() {
        // Arrange
        String input = "\n";
        setInput(input);
        LoginView view = new LoginView();

        // Act
        String password = view.promptPassword();

        // Assert
        assertEquals("", password, "Should return empty string for empty input.");
    }

    @Test
    void showLoginSuccess_shouldDisplaySuccessMessage() {
        // Arrange
        LoginView view = new LoginView();

        // Act
        view.showLoginSuccess();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Login successful!"), "Should display login success message.");
    }

    @Test
    void showLoginFailure_shouldDisplayFailureMessage() {
        // Arrange
        LoginView view = new LoginView();

        // Act
        view.showLoginFailure();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Invalid username or password."), "Should display login failure message.");
    }

    @Test
    void showMaxAttemptsReached_shouldDisplayMaxAttemptsMessage() {
        // Arrange
        LoginView view = new LoginView();

        // Act
        view.showMaxAttemptsReached();

        // Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Too many failed login attempts. Exiting..."), 
                   "Should display max attempts message.");
    }

    // ==================== III. Integration Tests ====================

    @Test
    void validateLogin_withEmptyUsername_shouldFail() throws IOException {
        // Arrange: Empty username
        String input = "\npassword123\n\n\n\n\n";
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertFalse(result, "Empty username should result in failed login.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Invalid username or password"), "Should show failure message.");
    }

    @Test
    void validateLogin_withEmptyPassword_shouldFail() throws IOException {
        // Arrange: Valid username but empty password
        String input = "S1001\n\n\n\n\n\n";
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertFalse(result, "Empty password should result in failed login.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Invalid username or password"), "Should show failure message.");
    }

    @Test
    void validateLogin_withWhitespaceInCredentials_shouldFail() throws IOException {
        // Arrange: Username/password with whitespace
        String input = "S1001 \n password123 \n\n\n\n\n";  // Note: trim might not be applied
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        // This depends on whether the repository trims input, but typically should fail
        // since "S1001 " != "S1001" and " password123 " != "password123"
        assertFalse(result, "Credentials with whitespace should fail (exact match required).");
    }

    @Test
    void validateLogin_caseSensitivePassword_shouldFail() throws IOException {
        // Arrange: Correct username but password with different case
        String input = "S1001\nPASSWORD123\n\n\n\n\n";  // Uppercase password
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertFalse(result, "Password should be case-sensitive.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Invalid username or password"), "Should show failure message.");
    }

    @Test
    void validateLogin_multipleUsers_shouldAuthenticateCorrectUser() throws IOException {
        String input = "S1111\n123456\n";
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertTrue(result, "Should authenticate correct user with correct credentials.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*Login successful!"), "Should show success message.");
    }

    // ==================== IV. Edge Cases and Error Scenarios ====================

    @Test
    void validateLogin_specialCharactersInInput_shouldHandleGracefully() throws IOException {
        // Arrange: Input with special characters
        String input = "S1001!@#\npassword123\n\n\n\n\n";
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        // Should fail because "S1001!@#" != "S1001"
        assertFalse(result, "Special characters in username should cause authentication failure.");
    }

    @Test
    void validateLogin_veryLongInput_shouldHandleGracefully() throws IOException {
        // Arrange: Very long username and password
        String longUsername = "A".repeat(1000);
        String longPassword = "B".repeat(1000);
        String input = longUsername + "\n" + longPassword + "\n\n\n\n\n";
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertFalse(result, "Very long input should cause authentication failure.");
    }

    @Test
    void validateLogin_unicodeCharacters_shouldHandleGracefully() throws IOException {
        // Arrange: Unicode characters in input
        String input = "S1001\npassword123\u00A9\n\n\n\n\n";  // Copyright symbol in password
        setInput(input);
        LoginController controller = new LoginController();

        // Act
        boolean result = controller.validateLogin();

        // Assert
        assertFalse(result, "Unicode characters should be handled (exact match required).");
    }
}
