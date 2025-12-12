package test.java;


// Import ItemView for validation tests

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.java.controller.*;
import main.java.model.*;
import main.java.repository.*;

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

/**
 * JUnit Jupiter tests for ItemRepository, ItemView, and ItemController,
 * focusing on safe CRUD operations, validation, and input handling.
 * Note: These tests assume a config.FilePaths.ITEM constant resolves to
 * "Item.txt".
 */
public class TestItem {

    // --- File Management Constants and Setup ---

    private static final String ITEM_FILE_PATH = "Item.txt";
    private static final String TEMP_FILE_PATH = "newItem.txt";
    private static byte[] initialContent; // Holds the backup of the original file content
    private ItemRepository repository;

    // --- System Input/Output Mocking Setup ---
    private final InputStream originalSystemIn = System.in;
    private final PrintStream originalSystemOut = System.out;
    private ByteArrayOutputStream outputStreamCaptor;

    /**
     * 1. Runs once before all tests.
     * 2. Reads and stores the initial content of Item.txt for restoration.
     */
    @BeforeAll
    static void setupAll() throws IOException {
        Path itemPath = Path.of(ITEM_FILE_PATH);

        if (Files.exists(itemPath)) {
            // Read the original content for backup
            initialContent = Files.readAllBytes(itemPath);
        } else {
            // If Item.txt is missing, create a placeholder with one line for testing
            String placeholderContent = "M0001||Aspirin||3.0||43||Pain Relief||2\nS0005||Vitamin C||5.0||27||Immune Support||230525\n";
            Files.write(itemPath, placeholderContent.getBytes(), StandardOpenOption.CREATE);
            initialContent = Files.readAllBytes(itemPath);
        }
    }

    /**
     * Runs before each test to restore Item.txt to its initial state,
     * initialize the repository, and set up input/output mocking.
     */
    @BeforeEach
    void setup() throws IOException {
        // Restore the file content for a clean starting state for the current test
        Files.write(
                Path.of(ITEM_FILE_PATH),
                initialContent,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        // Clean up any temporary file left by a previous failed test
        Files.deleteIfExists(Path.of(TEMP_FILE_PATH));

        repository = new ItemRepository();

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

    // ------------------------- I. ItemRepository Tests (CRUD)
    // -------------------------

    // --- 1. Create (Add) Tests (Validation covered in Section II) ---

    @Test
    void add_newMedicine_shouldReturnTrueAndBeFindable() throws IOException {
        // Arrange
        ItemRecord newMed = new ItemRecord("M9999", "Test Pain Reliever", 10.50, 50,
                ItemRecord.Type.MEDICINE, "Headache", 2);

        // Act
        boolean added = repository.add(newMed);
        ItemRecord found = repository.findByCode("M9999");

        // Assert
        assertTrue(added, "The add method should return true for a new Medicine item.");
        assertNotNull(found, "The added Medicine item should be findable by code.");
    }

    @Test
    void add_newSupplement_shouldReturnTrueAndBeFindable() throws IOException {
        // Arrange
        ItemRecord newSupp = new ItemRecord("S9999", "Test Multivitamin", 15.75, 20,
                ItemRecord.Type.SUPPLEMENT, "General Health", 251231);

        // Act
        boolean added = repository.add(newSupp);
        ItemRecord found = repository.findByCode("S9999");

        // Assert
        assertTrue(added, "The add method should return true for a new Supplement item.");
        assertNotNull(found, "The added Supplement item should be findable by code.");
    }

    @Test
    void add_duplicateCode_shouldReturnFalse() throws IOException {
        // Arrange: M0001 is assumed to exist from setupAll()
        ItemRecord duplicate = new ItemRecord("M0001", "Aspirin Duplicate", 100.0, 1,
                ItemRecord.Type.MEDICINE, "Test", 1);

        // Act
        boolean added = repository.add(duplicate);

        // Assert
        assertFalse(added, "Adding a duplicate item code should return false.");
    }

    // --- 2. Read (Search/Find) Tests ---

    @Test
    void findByCode_existingSupplement_shouldReturnRecord() throws IOException {
        // Act
        ItemRecord found = repository.findByCode("S0005"); // S0005 is assumed to exist from setupAll()

        // Assert
        assertNotNull(found, "Existing Supplement should be found.");
    }

    @Test
    void findAll_shouldReturnAllItems_isConsistent() throws IOException {
        // Arrange
        // Calculate the expected count dynamically from the initial backup
        String initialData = new String(initialContent);
        long expectedCount = initialData.lines().filter(line -> !line.trim().isEmpty()).count();

        // Act
        List<ItemRecord> allItems = repository.findAll();

        // Assert
        assertEquals(expectedCount, allItems.size(),
                "findAll should return a list matching the number of non-empty lines in the original Item.txt file.");
    }

    // --- 3. Update (Modify) Tests (Covering update with and without code change)
    // ---

    @Test
    void update_existingItem_noCodeChange_shouldSucceed() throws IOException {
        // Arrange: Add M9999 first, then update it.
        String testCode = "M9999";
        ItemRecord original = new ItemRecord(testCode, "Test Pain Reliever", 10.50, 50,
                ItemRecord.Type.MEDICINE, "Headache", 2);
        repository.add(original); // Add M9999

        ItemRecord preUpdate = repository.findByCode(testCode);
        assumeTrue(preUpdate != null, testCode + " must exist for this test.");

        // Update its description and price
        ItemRecord updatedMed = new ItemRecord(testCode, "Test Pain Reliever - New Price", 99.99, 10,
                preUpdate.getType(), preUpdate.getExtra1(), preUpdate.getExtra2());

        boolean updated = repository.update(testCode, updatedMed);
        ItemRecord found = repository.findByCode(testCode);

        // Assert
        assertTrue(updated, "Update should return true for an existing item with no code change.");
        assertEquals("Test Pain Reliever - New Price", found.getDescription(), "Description should be updated.");
        assertEquals(99.99, found.getPrice(), 0.001, "Price should be updated.");
    }

    @Test
    void update_existingItem_withCodeChange_shouldSucceed() throws IOException {
        // Arrange: Add S9999 first, then change its code to S9998.
        String oldCode = "S9999";
        String newCode = "S9998";

        ItemRecord original = new ItemRecord(oldCode, "Test Multivitamin", 15.75, 20,
                ItemRecord.Type.SUPPLEMENT, "General Health", 251231);
        repository.add(original); // Add S9999

        ItemRecord preUpdate = repository.findByCode(oldCode);
        assumeTrue(preUpdate != null, oldCode + " must exist for this test.");

        // New record with updated code
        ItemRecord updatedSupp = new ItemRecord(newCode, preUpdate.getDescription(), preUpdate.getPrice(),
                preUpdate.getQuantity(), preUpdate.getType(), preUpdate.getExtra1(), preUpdate.getExtra2());

        // Act (Assuming the required signature for code change exists in the
        // repository)
        boolean updated = repository.update(oldCode, updatedSupp);
        ItemRecord foundOld = repository.findByCode(oldCode);
        ItemRecord foundNew = repository.findByCode(newCode);

        // Assert
        assertTrue(updated,
                "Update should return true for a valid item code change (" + oldCode + " to " + newCode + ").");
        assertNull(foundOld, "Original item should be deleted after code change.");
        assertNotNull(foundNew, "New item code should be found.");
        assertEquals(newCode, foundNew.getCode(), "Item code should be updated.");
    }

    @Test
    void update_withCodeChange_toExistingCode_shouldReturnFalse_viaController() throws IOException {
        // Arrange: M0001 exists.
        String oldCode = "S9997";
        String duplicateCode = "S0005";

        // 1. Setup the item to be modified (S9997)
        ItemRecord original = new ItemRecord(oldCode, "Test Duplicate Check", 1.0, 1,
                ItemRecord.Type.SUPPLEMENT, "Check", 1);
        repository.add(original);

        assumeTrue(repository.findByCode(oldCode) != null, oldCode + " must exist for this test.");
        assumeTrue(repository.findByCode(duplicateCode) != null, duplicateCode + " must exist for this test.");

        int initialCount = repository.findAll().size();

        // FIX: Added extra '5\n' to safely terminate the program.
        String mockedInput = "3\n" + // 1. Menu Selection: 3. Modify
                oldCode + "\n" + // 2. Item Code to modify (S9997)
                duplicateCode + "\n" + // 3. New Item Code (S0005)
                "\n" + // 4. Description
                "\n" + // 5. Price
                "\n" + // 6. Quantity
                "\n" + // 7. Extra1
                "\n" + // 8. Extra2
                "5\n" + // 9. Second Menu Call (Expected exit)
                "5\n" + // 10. *** Safety Exit 1 (Buffer) ***
                "5\n"; // 11. *** Safety Exit 2 (Buffer) ***

        setInput(mockedInput);
        ItemController controller = new ItemController();

        // Act
        controller.run();

        // Assert (Test the Controller's validation logic)
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("Failed to update item. The new Item Code 'S0005' already exists."),
                "Controller should catch the duplicate code change and display the error message.");

        assertEquals(initialCount, repository.findAll().size(), "Total item count should not change.");

        ItemRecord s9997 = repository.findByCode(oldCode);
        assertNotNull(s9997, "Original item " + oldCode + " should still exist.");
        assertEquals("Test Duplicate Check", s9997.getDescription(), "Original item should not be modified.");
    }

    // --- 4. Delete Tests ---

    @Test
    void delete_existingSupplement_shouldSucceedAndBeUnfindable() throws IOException {
        // Arrange: Add S9999, then delete it.
        String testCode = "S9999";
        ItemRecord newSupp = new ItemRecord(testCode, "Test Multivitamin for Deletion", 15.75, 20,
                ItemRecord.Type.SUPPLEMENT, "General Health", 251231);
        repository.add(newSupp); // Add S9999

        assertNotNull(repository.findByCode(testCode), testCode + " should exist before deletion.");
        int initialCount = repository.findAll().size();

        // Act: Delete the item
        boolean deleted = repository.delete(testCode);
        ItemRecord foundAfterDelete = repository.findByCode(testCode);
        int finalCount = repository.findAll().size();

        // Assert
        assertTrue(deleted, "Delete should return true for an existing supplement code (" + testCode + ").");
        assertNull(foundAfterDelete, testCode + " should not be found after deletion.");
        assertEquals(initialCount - 1, finalCount, "Total item count should decrease by 1.");
    }

    // ------------------------- II. ItemView and Controller Input Validation Tests
    // -------------------------

    // These tests mock System.in to cover the input validation and error handling
    // loops in ItemView and ItemController.
    
    @Test
    void controllerRun_exitMenu_shouldNotLoop() {
        // Arrange: User enters 5 to exit the loop immediately
        // FIX: Added two extra '5\n' for safe termination buffer.
        String input = "5\n" +  // Expected Exit
                       "5\n" +  // Safety Buffer 1
                       "5\n";   // Safety Buffer 2
        setInput(input);

        ItemController controller = new ItemController();

        // Act
        controller.run();

        // Assert: The controller should exit without looping
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Welcome to Item Interface"), "Menu should be displayed.");
        assertFalse(output.contains("Invalid input. Please enter a number"), "No invalid input loop should occur.");
    }

    @Test
    void promptNew_invalidPriceAndQuantity_shouldRePromptAndSucceed() throws Exception {
        // Arrange:
        // FIX: Added two extra '5\n' for safe termination buffer.
        // Final Record: M9997, NewMed, 10.0, 20, Disease, 1
        String mockedInput =
                // ItemView.menu() calls nextLine, so we need a selection first
                "1\n" + // Menu Selection: 1. Add
                        "1\n" + // Type: 1. Medicine

                        // ItemView.promptNew()
                        "M9997\n" + // Item Code (Valid)
                        "NewMed\n" + // Description
                        "-10.0\n" + // Price (Negative -> Invalid)
                        "abc\n" + // Price (Text -> Invalid)
                        "10.0\n" + // Price (Valid)
                        "-20\n" + // Quantity (Negative -> Invalid)
                        "xyz\n" + // Quantity (Text -> Invalid)
                        "20\n" + // Quantity (Valid)
                        "Disease\n" + // Extra1 (ForDisease)
                        "1\n" + // Extra2 (amountDaytake)

                        // ItemController will call ItemRepository.add and go back to ItemView.menu()
                        "5\n" +  // Menu Selection: 5. Back (Exits controller loop)
                        "5\n" +  // Safety Buffer 1
                        "5\n";   // Safety Buffer 2 

        setInput(mockedInput);
        ItemController controller = new ItemController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        // Check for error messages that confirm re-prompting occurred
        assertTrue(output.contains("*Invalid price input. Price cannot be negative."),
                "Negative price validation failed.");
        assertTrue(output.contains("*Invalid price input. Please enter a number."),
                "Alphabet price validation failed.");
        assertTrue(output.contains("*Invalid quantity input. Quantity cannot be negative."),
                "Negative quantity validation failed.");
        assertTrue(output.contains("*Invalid quantity input. Please enter a whole number."),
                "Alphabet quantity validation failed.");
        assertTrue(output.contains("Item added."), "Valid item should be added successfully.");

        // Final check that the item was actually added
        ItemRecord addedRecord = repository.findByCode("M9997");
        assertNotNull(addedRecord, "Item M9997 should be in the repository.");
    }

    @Test
    void promptUpdate_invalidExtra2AndCodeChange_shouldRePromptAndSucceed() throws Exception {
        // Arrange:
        String oldCode = "M0001";
        String newCode = "M9996";

        // FIX: Added two extra '5\n' for safe termination buffer.
        // M0001 is a MEDICINE, so Extra2 is amountDayTake (int)
        String mockedInput =
                // ItemView.menu() calls nextLine, so we need a selection first
                "3\n" + // Menu Selection: 3. Modify

                        // ItemController.handleModify()
                        oldCode + "\n" + // Item Code to modify

                        // ItemView.promptUpdate(current)
                        newCode + "\n" + // New Item Code (Valid code change)
                        "\n" + // Description (Empty -> uses old description)
                        "\n" + // Price (Empty -> uses old price)
                        "\n" + // Quantity (Empty -> uses old quantity)
                        "\n" + // Extra1 (Empty -> uses old Extra1)
                        "five\n" + // Extra2 (Text/Invalid format for int)
                        "5\n" + // Extra2 (Valid int)

                        // ItemController will call ItemRepository.update and go back to ItemView.menu()
                        "5\n" + // Menu Selection: 5. Back (Exits controller loop)
                        "5\n" + // *** Safety Exit 1 (Buffer) ***
                        "5\n"; // *** Safety Exit 2 (Buffer) ***

        setInput(mockedInput);
        ItemController controller = new ItemController();

        // Act
        controller.run();

        // Assert
        String output = outputStreamCaptor.toString();
        // Check for error messages that confirm re-prompting occurred
        assertTrue(output.contains("*Invalid Amount Day Take input. Please enter a whole number."),
                "Alphabet Extra2 validation failed.");
        assertTrue(output.contains("Item updated."), "Valid item should be updated successfully.");

        // Final check that the code change was applied
        ItemRecord foundNew = repository.findByCode(newCode);
        assertNotNull(foundNew, "Item " + newCode + " should be found after code change.");
        assertEquals(5, foundNew.getExtra2(), "Extra2 (Amount Day Take) should be updated.");

        ItemRecord foundOld = repository.findByCode(oldCode);
        assertNull(foundOld, "Original item " + oldCode + " should be deleted after code change.");
    }
}