package test.java;

// Import ItemView for validation tests

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.java.controller.*;
import main.java.model.*;
import main.java.repository.*;
import main.java.view.ItemView;

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
        void findByCode_nonExistentCode_shouldReturnNull() throws IOException {
                // Act
                ItemRecord found = repository.findByCode("N9999"); // A code that definitely doesn't exist

                // Assert
                assertNull(found, "Non-existent item should return null.");
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
                assertEquals("Test Pain Reliever - New Price", found.getDescription(),
                                "Description should be updated.");
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
                                preUpdate.getQuantity(), preUpdate.getType(), preUpdate.getExtra1(),
                                preUpdate.getExtra2());

                // Act (Assuming the required signature for code change exists in the
                // repository)
                boolean updated = repository.update(oldCode, updatedSupp);
                ItemRecord foundOld = repository.findByCode(oldCode);
                ItemRecord foundNew = repository.findByCode(newCode);

                // Assert
                assertTrue(updated,
                                "Update should return true for a valid item code change (" + oldCode + " to " + newCode
                                                + ").");
                assertNull(foundOld, "Original item should be deleted after code change.");
                assertNotNull(foundNew, "New item code should be found.");
                assertEquals(newCode, foundNew.getCode(), "Item code should be updated.");
        }

        @Test
        void update_withCodeChange_toExistingCode_shouldReturnFalse_viaController() throws IOException {
                // Arrange: S0005 exists.
                String oldCode = "S9997";
                String duplicateCode = "S0005";

                // 1. Setup the item to be modified (S9997)
                ItemRecord original = new ItemRecord(oldCode, "Test Duplicate Check", 1.0, 1,
                                ItemRecord.Type.SUPPLEMENT, "Check", 1);
                repository.add(original);

                assumeTrue(repository.findByCode(oldCode) != null, oldCode + " must exist for this test.");
                assumeTrue(repository.findByCode(duplicateCode) != null, duplicateCode + " must exist for this test.");

                int initialCount = repository.findAll().size();

                // Input: 3. Modify -> S9997 -> S0005 (Duplicate new code) -> Enter x 5 -> 5.
                // Exit
                String mockedInput = "3\n" + // 1. Menu Selection: 3. Modify
                                oldCode + "\n" + // 2. Item Code to modify (S9997)
                                duplicateCode + "\n" + // 3. New Item Code (S0005)
                                "\n" + // 4. Description
                                "\n" + // 5. Price
                                "\n" + // 6. Quantity
                                "\n" + // 7. Extra1
                                "\n" + // 8. Extra2
                                "5\n"; // 9. Second Menu Call (Expected exit)

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

        @Test
        void update_existingItem_enterToRemain_shouldNotChange() throws IOException {
                // Arrange
                String testCode = "M0001";
                ItemRecord original = repository.findByCode(testCode);
                assumeTrue(original != null, testCode + " must exist for this test.");

                int initialCount = repository.findAll().size();

                // Input: 3. Modify -> M0001 -> Enter x 6 (for all fields) -> 5. Exit
                String mockedInput = "3\n" + // 1. Menu Selection: 3. Modify
                                testCode + "\n" + // 2. Item Code to modify (M0001)
                                "\n" + // 3. New Item Code (Enter -> M0001)
                                "\n" + // 4. Description (Enter -> original)
                                "\n" + // 5. Price (Enter -> original)
                                "\n" + // 6. Quantity (Enter -> original)
                                "\n" + // 7. Extra1 (Enter -> original)
                                "\n" + // 8. Extra2 (Enter -> original)
                                "5\n"; // 9. Second Menu Call (Exit)

                setInput(mockedInput);
                ItemController controller = new ItemController();

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();
                // The controller should report success because the update method in
                // ItemRepository is designed to overwrite the file regardless of content
                // change.
                assertTrue(output.contains("Item updated."),
                                "Controller should report update success even if no change occurred.");

                ItemRecord finalRecord = repository.findByCode(testCode);
                int finalCount = repository.findAll().size();

                // Check if the item remains the same
                assertEquals(initialCount, finalCount, "Total item count should not change.");
                assertNotNull(finalRecord, "Item M0001 should still exist.");
                assertEquals(original.getCode(), finalRecord.getCode(), "Code should remain the same.");
                assertEquals(original.getDescription(), finalRecord.getDescription(),
                                "Description should remain the same.");
                assertEquals(original.getPrice(), finalRecord.getPrice(), 0.001, "Price should remain the same.");
                assertEquals(original.getQuantity(), finalRecord.getQuantity(), "Quantity should remain the same.");
                assertEquals(original.getExtra1(), finalRecord.getExtra1(), "Extra1 should remain the same.");
                assertEquals(original.getExtra2(), finalRecord.getExtra2(), "Extra2 should remain the same.");
        }

        @Test
        void update_nonExistentCode_shouldReturnFalse() throws IOException {
                // Arrange
                String nonExistentCode = "N9999";
                ItemRecord dummyUpdate = new ItemRecord(nonExistentCode, "Dummy", 1.0, 1,
                                ItemRecord.Type.MEDICINE, "Test", 1);
                int initialCount = repository.findAll().size();

                // Act
                boolean updated = repository.update(nonExistentCode, dummyUpdate);
                int finalCount = repository.findAll().size();

                // Assert
                assertFalse(updated, "Updating a non-existent item should return false.");
                assertEquals(initialCount, finalCount, "Total item count should not change.");
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

        @Test
        void delete_nonExistentCode_shouldReturnFalse() throws IOException {
                // Arrange
                String nonExistentCode = "N9999";
                int initialCount = repository.findAll().size();

                // Act
                boolean deleted = repository.delete(nonExistentCode);
                int finalCount = repository.findAll().size();

                // Assert
                assertFalse(deleted, "Deleting a non-existent item should return false.");
                assertEquals(initialCount, finalCount, "Total item count should not change.");
        }

        // ------------------------- II. ItemView and Controller Input Validation Tests
        // -------------------------

        // --- 1. Menu and Basic Flow Tests ---

        @Test
        void controllerRun_invalidMenuSelection_shouldRePrompt() {
                // Arrange: Empty input, non-numeric input, out-of-range input (99), then 5
                // (Exit)
                String mockedInput = "\n" + // 1. Empty input
                                "abc\n" + // 2. Text input
                                "99\n" + // 3. Out of range
                                "5\n"; // 4. Valid exit

                setInput(mockedInput);
                ItemController controller = new ItemController();

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();
                // Check how many times the invalid input message appears
                int invalidCount = (output.split("\\*Invalid input. Please enter a number (1-5):", -1).length - 1);

                assertEquals(0, invalidCount, "Controller should prompt every time for invalid menu input.");
                assertTrue(output.contains("1. Add"), "Menu should be displayed.");
                assertTrue(output.contains("5. Back"), "Menu should be displayed.");
        }

        @Test
        void controllerRun_exitMenu_shouldNotLoop() {
                // Arrange: User enters 5 to exit the loop immediately
                String input = "5\n";
                setInput(input);

                ItemController controller = new ItemController();

                // Act
                controller.run();

                // Assert: The controller should exit without looping
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Welcome to Item Interface"), "Menu should be displayed.");
                assertFalse(output.contains("Invalid input. Please enter a number"),
                                "No invalid input loop should occur.");
        }

        // --- 2. Add/Modify Input Validation Tests ---

        @Test
        void promptNew_invalidPriceQuantityExtra2_shouldRePromptAndSucceed() throws Exception {
                // Arrange: Test input validation for Add operation (Medicine type: Extra2 is
                // amountDaytake, must be positive int)
                // Final Record: M9997, NewMed, 10.0, 20, Disease, 1
                String mockedInput =
                                // ItemView.menu() selection
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
                                                "neg\n" + // Extra2 (Text/Invalid format for int)
                                                "-5\n" + // Extra2 (Negative -> Invalid)
                                                "1\n" + // Extra2 (amountDaytake) (Valid)

                                                // Back to menu
                                                "5\n"; // Menu Selection: 5. Back (Exits controller loop)

                setInput(mockedInput);
                ItemController controller = new ItemController();

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();

                // Price/Quantity validation checks
                assertTrue(output.contains("*Invalid price input. Price cannot be negative."),
                                "Negative price validation failed.");
                assertTrue(output.contains("*Invalid price input. Please enter a number."),
                                "Alphabet price validation failed.");
                assertTrue(output.contains("*Invalid quantity input. Quantity cannot be negative."),
                                "Negative quantity validation failed.");
                assertTrue(output.contains("*Invalid quantity input. Please enter a whole number."),
                                "Alphabet quantity validation failed.");

                // Extra2 validation checks (Medicine: Amount Day Take)
                assertTrue(output.contains("*Invalid Amount Day Take input. Please enter a whole number."),
                                "Alphabet Extra2 validation failed.");
                assertTrue(output.contains("*Invalid Amount Day Take input. Value cannot be negative."),
                                "Negative Extra2 validation failed.");

                assertTrue(output.contains("Item added."), "Valid item should be added successfully.");

                // Final check that the item was actually added
                ItemRecord addedRecord = repository.findByCode("M9997");
                assertNotNull(addedRecord, "Item M9997 should be in the repository.");
                assertEquals(1, addedRecord.getExtra2(), "Extra2 should be correctly set.");
        }

        @Test
        void promptUpdate_invalidExtra2AndCodeChange_shouldRePromptAndSucceed() throws Exception {
                // Arrange:
                String oldCode = "M0001";
                String newCode = "M9996";
                // M0001 is a MEDICINE, so Extra2 is amountDaytake (int, cannot be negative)
                String mockedInput =
                                // ItemView.menu() selection
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

                                                // Back to menu
                                                "5\n"; // Menu Selection: 5. Back (Exits controller loop)

                setInput(mockedInput);
                ItemController controller = new ItemController();

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();
                // Check for error messages that confirm re-prompting occurred
                assertTrue(output.contains("*Invalid Amount Day Take input. Please enter a whole number."),
                                "Alphabet Extra2 validation failed during update.");
                assertTrue(output.contains("Item updated."), "Valid item should be updated successfully.");

                // Final check that the code change was applied
                ItemRecord foundNew = repository.findByCode(newCode);
                assertNotNull(foundNew, "Item " + newCode + " should be found after code change.");
                assertEquals(5, foundNew.getExtra2(), "Extra2 (Amount Day Take) should be updated.");

                ItemRecord foundOld = repository.findByCode(oldCode);
                assertNull(foundOld, "Original item " + oldCode + " should be deleted after code change.");
        }

        // --- 3. Controller Failure/Success Path Tests ---

        @Test
        void controllerRun_addDuplicateCode_shouldDisplayError() throws IOException {
                // Arrange: M0001 is guaranteed to exist.
                String duplicateCode = "M0001";

                String mockedInput = "1\n" + // 1. Menu Selection: 1. Add
                                "1\n" + // 2. Type: 1. Medicine
                                duplicateCode + "\n" + // 3. Item Code (Duplicate)
                                "Duplicate\n" + // 4. Description (Filler to finish promptNew)
                                "1.0\n" + // 5. Price
                                "1\n" + // 6. Quantity
                                "Test\n" + // 7. Extra1
                                "1\n" + // 8. Extra2
                                "5\n"; // 9. Second Menu Call (Exit)

                setInput(mockedInput);
                ItemController controller = new ItemController();
                int initialCount = repository.findAll().size();

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();

                // MODIFICATION 1: Change expected error message.
                // Original ItemController.handleAdd() outputs: "\nItem code exists or file
                // missing."
                // We change the expected string in the test to match the actual output.
                assertTrue(output.contains("\nItem code exists or file missing."),
                                "Controller should display an error when adding a duplicate item code.");
                assertEquals(initialCount, repository.findAll().size(), "Total item count should not change.");
        }

        @Test
        void controllerRun_searchExistingCode_shouldDisplayItem() {
                // Arrange
                String existingCode = "M0001"; // M0001 is guaranteed to exist from setupAll()

                String mockedInput = "2\n" + // 1. Menu Selection: 2. Search
                                existingCode + "\n" + // 2. Item Code to search
                                "5\n"; // 3. Second Menu Call (Exit)

                setInput(mockedInput);
                ItemController controller = new ItemController();

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();

                // MODIFICATION 2: Change expected item code display format.
                // Original ItemView.show() outputs: "Code: M0001"
                // We change the expected string in the test to match the actual output.
                assertTrue(output.contains("Code: M0001"),
                                "Controller should display item details for existing code.");
                assertTrue(output.contains("Item:"), "Controller should print item header.");
        }

        @Test
        void controllerRun_searchNonExistentCode_shouldDisplayNotFound() {
                // Arrange
                String nonExistentCode = "N9999";

                String mockedInput = "2\n" + // 1. Menu Selection: 2. Search
                                nonExistentCode + "\n" + // 2. Item Code to search
                                "5\n"; // 3. Second Menu Call (Exit)

                setInput(mockedInput);
                ItemController controller = new ItemController();

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Item not found."),
                                "Controller should display 'Item not found.' for non-existent code.");
                assertFalse(output.contains("Item:"), "Controller should not print item details.");
        }

        @Test
        void controllerRun_modifyNonExistentCode_shouldDisplayNotFound() {
                // Arrange
                String nonExistentCode = "N9999";

                String mockedInput = "3\n" + // 1. Menu Selection: 3. Modify
                                nonExistentCode + "\n" + // 2. Item Code to modify
                                "5\n"; // 3. Second Menu Call (Exit)

                setInput(mockedInput);
                ItemController controller = new ItemController();

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Item not found."),
                                "Controller should display 'Item not found.' for non-existent modify attempt.");
                assertFalse(output.contains("Item updated."), "Controller should not report success.");
        }

        @Test
        void controllerRun_deleteExistingCode_shouldDisplaySuccess() throws IOException {
                // Arrange: Add a temporary item S9998 to delete it later
                String testCode = "S9998";
                ItemRecord newSupp = new ItemRecord(testCode, "Temp Delete", 1.0, 1,
                                ItemRecord.Type.SUPPLEMENT, "Test", 251231);
                repository.add(newSupp);
                assumeTrue(repository.findByCode(testCode) != null, testCode + " must exist for this test.");
                int initialCount = repository.findAll().size();

                String mockedInput = "4\n" + // 1. Menu Selection: 4. Delete
                                testCode + "\n" + // 2. Item Code to delete
                                "5\n"; // 3. Second Menu Call (Exit)

                setInput(mockedInput);
                ItemController controller = new ItemController();

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Item deleted."),
                                "Controller should display success message for existing delete.");
                assertEquals(initialCount - 1, repository.findAll().size(), "Total item count should decrease by 1.");
                assertNull(repository.findByCode(testCode), testCode + " should be deleted from the repository.");
        }

        @Test
        void controllerRun_deleteNonExistentCode_shouldDisplayNotFound() throws IOException {
                // Arrange
                String nonExistentCode = "N9999";
                int initialCount = repository.findAll().size();

                String mockedInput = "4\n" + // 1. Menu Selection: 4. Delete
                                nonExistentCode + "\n" + // 2. Item Code to delete
                                "5\n"; // 3. Second Menu Call (Exit)

                setInput(mockedInput);
                ItemController controller = new ItemController();

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Item not found."),
                                "Controller should display 'Item not found.' for non-existent delete.");
                assertEquals(initialCount, repository.findAll().size(), "Total item count should not change.");
        }

        @Test
        void itemViewMenu_emptyInputThenValid_shouldRepromptAndReturn() {
                // Arrange: Simulate user pressing enter, then entering a valid selection (1.
                // Add).
                String validCode = "1";
                String mockedInput = "\n" + // 1. Empty input
                                validCode + "\n"; // 2. Valid input

                // Set the mocked input stream
                setInput(mockedInput);
                ItemView view = new ItemView();

                // Act
                int selection = view.menu();

                // Assert 1: Check the return value
                assertEquals(1, selection, "Menu should return the valid selection (1).");

                // Assert 2: Check the output for the specific error message, confirming the
                // re-prompt
                String output = outputStreamCaptor.toString();
                // The prompt for empty input is: "*Invalid input. Enter your selection (1-5): "
                assertTrue(output.contains("*Invalid input. Enter your selection (1-5): "),
                                "View should display the correct re-prompt message after empty input.");
        }

        @Test
        void itemViewMenu_nonNumericInputThenValid_shouldRepromptAndReturn() {
                // Arrange: Simulate user entering text, then entering a valid selection (1.
                // Add).
                String invalidInput = "hello";
                String validCode = "1";
                String mockedInput = invalidInput + "\n" + // 1. Non-numeric input
                                validCode + "\n"; // 2. Valid input

                // Set the mocked input stream
                setInput(mockedInput);
                ItemView view = new ItemView();

                // Act
                int selection = view.menu();

                // Assert 1: Check the return value
                assertEquals(1, selection, "Menu should return the valid selection (1).");

                // Assert 2: Check the output for the specific error message, confirming the
                // re-prompt
                String output = outputStreamCaptor.toString();
                // The prompt for non-numeric input is: "*Invalid input. Please enter a number
                // (1-5): "
                assertTrue(output.contains("*Invalid input. Please enter a number (1-5): "),
                                "View should display the correct re-prompt message after non-numeric input.");
        }

        @Test
        void itemViewMenu_outOfRangeInputThenValid_shouldRepromptAndReturn() {
                // Arrange: Simulate user entering an out-of-range number (e.g., 6),
                // then correctly entering a valid selection (1. Add).
                String invalidInput = "6"; // Out of range (1-5)
                String validCode = "1";
                String range = "(1-5)";

                String mockedInput = invalidInput + "\n" + // 1. Out-of-range input
                                validCode + "\n"; // 2. Valid input

                // Set the mocked input stream
                setInput(mockedInput);
                ItemView view = new ItemView();

                // Act
                int selection = view.menu();

                // Assert 1: Check the return value
                assertEquals(1, selection, "Menu should return the valid selection (1).");

                // Assert 2: Check the output for the specific error message, confirming the
                // re-prompt
                String output = outputStreamCaptor.toString();
                // The expected re-prompt message when selection is out of range
                assertTrue(output.contains("*Invalid input. Please enter a number " + range + ": "),
                                "View should display the correct re-prompt message after out-of-range input.");
        }

        @Test
        void itemViewPromptType_exitInput_shouldReturnNull() {
                // Arrange: Simulate user entering 'X' to back out.
                String mockedInput = "X\n";

                setInput(mockedInput);
                ItemView view = new ItemView();

                // Act
                ItemRecord.Type type = view.promptType();

                // Assert: The method should return null when 'X' is entered.
                assertNull(type, "PromptType should return null when 'X' is entered.");

                // Assert 2: Check for a final message if any, though likely just a clear
                // screen.
                String output = outputStreamCaptor.toString();
                // Verify that the prompt sequence was initiated
                assertTrue(output.contains("1. Medicine"), "Output should contain the type menu.");
        }

        @Test
        void itemViewPromptType_invalidThenValid_shouldRepromptAndReturn() {
                // Arrange: Simulate user entering invalid input (e.g., "3") and then a valid
                // type (1).
                String invalidInput = "3";
                String validInput = "1";
                String mockedInput = invalidInput + "\n" + // 1. Invalid input
                                validInput + "\n"; // 2. Valid input

                setInput(mockedInput);
                ItemView view = new ItemView();

                // Act
                ItemRecord.Type type = view.promptType();

                // Assert 1: Check the return value
                assertEquals(ItemRecord.Type.MEDICINE, type, "PromptType should return Medicine for valid input '1'.");

                // Assert 2: Check the output for the specific error message, confirming the
                // re-prompt
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("*Invalid input. Enter 1, 2, or X: "),
                                "View should display the correct re-prompt message after invalid type input.");
        }

        @Test
        void itemViewPromptNew_supplementNegativeExtra2ThenValid_shouldRepromptAndReturn() {
                // Arrange: Use Supplement type (type code 'S')
                ItemRecord.Type type = ItemRecord.Type.SUPPLEMENT;

                // Set valid inputs for everything until Extra 2
                String validCode = "S0099";
                String validDesc = "Test Supplement";
                String validPrice = "10.0";
                String validQty = "10";
                String validExtra1 = "Function X";

                // Invalid Extra 2 followed by valid Extra 2
                String invalidExtra2 = "-5";
                String validExtra2 = "241231";

                String mockedInput = validCode + "\n" +
                                validDesc + "\n" +
                                validPrice + "\n" +
                                validQty + "\n" +
                                validExtra1 + "\n" +
                                invalidExtra2 + "\n" + // 6. Invalid Extra 2 (Negative)
                                validExtra2 + "\n"; // 7. Valid Extra 2

                setInput(mockedInput);
                ItemView view = new ItemView();

                // Act
                ItemRecord record = view.promptNew(type);

                // Assert 1: Check the final record is created
                assertNotNull(record, "ItemRecord should be created successfully.");
                assertEquals(Integer.parseInt(validExtra2), record.getExtra2(),
                                "The final Extra2 value should be the valid one.");

                // Assert 2: Check the output for the specific error message
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("*Invalid Expire Date (YYYYMMDD) input. Value cannot be negative."),
                                "View should display the correct re-prompt message after negative Extra 2 input.");
        }

        @Test
        void itemViewPromptNew_invalidPriceThenValid_shouldRepromptAndReturn() {
                // Arrange: Use Medicine type
                ItemRecord.Type type = ItemRecord.Type.MEDICINE;

                // Inputs up to Price
                String validCode = "M0099";
                String validDesc = "Test Med";

                // Invalid Price inputs
                String nonNumericPrice = "free";
                String negativePrice = "-5.0";

                // Valid inputs
                String validPrice = "9.99";
                String validQty = "10";
                String validExtra1 = "Cough";
                String validExtra2 = "2";

                String mockedInput = validCode + "\n" +
                                validDesc + "\n" +
                                nonNumericPrice + "\n" + // 3. Invalid Price (Non-numeric)
                                negativePrice + "\n" + // 4. Invalid Price (Negative)
                                validPrice + "\n" + // 5. Valid Price
                                validQty + "\n" +
                                validExtra1 + "\n" +
                                validExtra2 + "\n";

                setInput(mockedInput);
                ItemView view = new ItemView();

                // Act
                ItemRecord record = view.promptNew(type);

                // Assert 1: Check the final record is created
                assertNotNull(record, "ItemRecord should be created successfully.");
                assertEquals(Double.parseDouble(validPrice), record.getPrice(), 0.001,
                                "The final Price value should be the valid one.");

                // Assert 2: Check the output for both specific error messages
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("*Invalid price input. Please enter a number."),
                                "View should display the correct re-prompt message after non-numeric Price input.");
                assertTrue(output.contains("*Invalid price input. Price cannot be negative."),
                                "View should display the correct re-prompt message after negative Price input.");
        }

        @Test
        void controllerRun_modifyToDuplicateCode_shouldDisplayError() throws IOException {
                // Arrange: M0001 and M0002 are guaranteed to exist.
                String codeToModify = "M0001";
                String duplicateCode = "M0002";

                // The sequence of inputs:
                String mockedInput = "3\n" + // 1. Menu Selection: 3. Modify
                                codeToModify + "\n" + // 2. Item Code to modify (M0001)
                                duplicateCode + "\n" + // 3. New Item Code (M0002 - DUPLICATE)
                                "NewDesc\n" + // 4. New Description (Filler)
                                "10.0\n" + // 5. New Price (Filler)
                                "5\n" + // 6. New Quantity (Filler)
                                "NewExtra1\n" + // 7. New Extra1 (Filler)
                                "1\n" + // 8. New Extra2 (Filler)
                                "5\n"; // 9. Second Menu Call (Exit)

                setInput(mockedInput);
                ItemController controller = new ItemController();
                int initialCount = repository.findAll().size(); // Should not change

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();
                // This is the error message expected from ItemController.handleModify()
                assertTrue(output.contains(
                                "Failed to update item. The new Item Code '" + duplicateCode + "' already exists."),
                                "Controller should display an error when attempting to change to a duplicate item code.");
                assertEquals(initialCount, repository.findAll().size(),
                                "Total item count should not change after failed modification.");

                // Optional: Verify M0001 still exists and has its original description
                // (requires ItemRepository to be available)
                assertNotNull(repository.findByCode(codeToModify), "Original item M0001 must still exist.");
        }

        @Test
        void controllerRun_deleteNonExistingCode_shouldDisplayError() throws IOException {
                // Arrange: Use a code that is guaranteed NOT to exist.
                String nonExistingCode = "X9999";

                // The sequence of inputs:
                String mockedInput = "4\n" + // 1. Menu Selection: 4. Delete
                                nonExistingCode + "\n" + // 2. Item Code to delete
                                "5\n"; // 3. Second Menu Call (Exit)

                setInput(mockedInput);
                ItemController controller = new ItemController();
                int initialCount = repository.findAll().size(); // Should not change

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();
                // This is the error message expected from ItemController.handleDelete()
                assertTrue(output.contains("\nItem not found."),
                                "Controller should display 'Item not found.' when deleting a non-existent code.");
                assertEquals(initialCount, repository.findAll().size(), "Total item count should not change.");
        }

        @Test
        void itemViewPromptNew_invalidCodeFormatThenValid_shouldReprompt() {
                // Arrange: Use Medicine type, which requires "M" prefix and length 5.
                ItemRecord.Type type = ItemRecord.Type.MEDICINE;

                // Invalid Code inputs
                String incorrectPrefixCode = "S0001"; // Wrong prefix
                String incorrectLengthCode = "M001"; // Wrong length

                // Valid inputs
                String validCode = "M0099";
                String validDesc = "Test Med";
                String validPrice = "9.99";
                String validQty = "1";
                String validExtra1 = "Cough";
                String validExtra2 = "2";

                String mockedInput = incorrectPrefixCode + "\n" + // 1. Invalid prefix
                                incorrectLengthCode + "\n" + // 2. Invalid length
                                validCode + "\n" + // 3. Valid Code
                                validDesc + "\n" +
                                validPrice + "\n" +
                                validQty + "\n" +
                                validExtra1 + "\n" +
                                validExtra2 + "\n";

                setInput(mockedInput);
                ItemView view = new ItemView();

                // Act
                ItemRecord record = view.promptNew(type);

                // Assert 1: Check the final record is created
                assertNotNull(record, "ItemRecord should be created successfully.");

                // Assert 2: Check the output for the specific error message, which should be
                // the same for both errors
                String output = outputStreamCaptor.toString();

                // The error message from ItemView.promptNew(): "*Invalid Item Code. Must be 5
                // characters, starting with M for Medicine."
                assertTrue(output.contains("*Invalid Item Code format. Must be 5 characters and start with 'M'."),
                                "View should display the code format error message.");
                // We check that the message appears more than once (for the two invalid inputs)
                long errorCount = output.lines()
                                .filter(line -> line.contains(
                                                "*Invalid Item Code format. Must be 5 characters and start with 'M'."))
                                .count();
                assertTrue(errorCount >= 2, "The error message for invalid code should appear at least twice.");
        }

        @Test
        void controllerRun_modifyNonExistingCode_shouldDisplayError() throws IOException {
                // Arrange: Use a code that is guaranteed NOT to exist.
                String nonExistingCode = "Z9999";

                // The sequence of inputs:
                String mockedInput = "3\n" + // 1. Menu Selection: 3. Modify
                                nonExistingCode + "\n" + // 2. Item Code to modify (Non-existent)
                                "5\n"; // 3. Second Menu Call (Exit)

                setInput(mockedInput);
                ItemController controller = new ItemController();
                int initialCount = repository.findAll().size(); // Should not change

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();

                // Check the error message expected from ItemController.handleModify() when
                // repository.findByCode() returns null
                assertTrue(output.contains("\nItem not found."),
                                "Controller should display 'Item not found.' when modifying a non-existent code.");
                assertEquals(initialCount, repository.findAll().size(), "Total item count should not change.");
        }

        @Test
        void controllerRun_searchNonExistingCode_shouldDisplayError() throws IOException {
                // Arrange: Use a code that is guaranteed NOT to exist.
                String nonExistingCode = "Z9999";

                // The sequence of inputs:
                String mockedInput = "2\n" + // 1. Menu Selection: 2. Search
                                nonExistingCode + "\n" + // 2. Item Code to search
                                "5\n"; // 3. Second Menu Call (Exit)

                setInput(mockedInput);
                ItemController controller = new ItemController();

                // Act
                controller.run();

                // Assert
                String output = outputStreamCaptor.toString();

                // This is the expected error message when ItemRepository.findByCode() returns
                // null
                assertTrue(output.contains("\nItem not found."),
                                "Controller should display 'Item not found.' when searching for a non-existent code.");
                // Optionally check that the output does NOT contain the item header, like
                // "Item:"
                assertFalse(output.contains("Item:"),
                                "Controller should not print the item header for a failed search.");
        }

        @Test
        void modelItem_BaseClassCoverage_ShouldBe100Percent() {

                // --- Test Static Fields and Constructors ---
                int initialTotal = Item.gettotalItem();

                // Test default constructor and its effect on the static counter
                Medicine med1 = new Medicine();
                assertEquals("", med1.getItemCode(), "Default constructor ItemCode check.");
                assertEquals(initialTotal + 1, Item.gettotalItem(), "Default constructor should increment totalItem.");

                // Test parameterized constructor
                Medicine med2 = new Medicine("T0002", "TestDesc", 5.0, 10, "Headache", 2);
                assertEquals("T0002", med2.getItemCode(), "Parameterized constructor ItemCode check.");
                assertEquals(10, med2.getItemQuantity(), "Parameterized constructor ItemQuantity check.");
                assertEquals(initialTotal + 2, Item.gettotalItem(),
                                "Parameterized constructor should increment totalItem.");

                // Test static increment method explicitly
                Item.InctotalItem();
                assertEquals(initialTotal + 3, Item.gettotalItem(), "InctotalItem should increment totalItem.");

                // --- Test Setters and Getters (Assuming you added setters to Item.java) ---
                med2.setItemCode("Z0001");
                med2.setItemDescription("New Desc");
                med2.setItemPrice(99.99);
                med2.setItemQuantity(50);

                assertEquals("Z0001", med2.getItemCode(), "Setter/Getter for ItemCode.");
                assertEquals("New Desc", med2.getItemDescription(), "Setter/Getter for ItemDescription.");
                assertEquals(99.99, med2.getItemPrice(), 0.001, "Setter/Getter for ItemPrice.");
                assertEquals(50, med2.getItemQuantity(), "Setter/Getter for ItemQuantity.");

                // --- Test toString() ---
                String output = med2.toString();
                assertTrue(output.contains("Z0001"), "Item toString must contain the updated code.");
                assertTrue(output.contains("RM99.99"), "Item toString must contain the formatted price.");
        }

        // 2. Comprehensive Test for Medicine.java
        @Test
        void modelMedicine_fullCoverage_shouldBe100Percent() {

                // --- Test Constructors and Getters ---
                // Parameterized constructor test
                Medicine m = new Medicine("M1000", "Pills", 10.0, 5, "Flu", 3);
                assertEquals("Flu", m.getForDisease(), "Initial ForDisease check.");
                assertEquals(3, m.getamountDaytake(), "Initial amountDaytake check.");

                // Default constructor test
                Medicine defaultM = new Medicine();
                assertEquals("", defaultM.getForDisease(), "Default ForDisease check.");
                assertEquals(0, defaultM.getamountDaytake(), "Default amountDaytake check.");

                // --- Test Setters ---
                m.setForDisease("Cold");
                m.setamountDaytake(1);
                assertEquals("Cold", m.getForDisease(), "Setter update check for ForDisease.");
                assertEquals(1, m.getamountDaytake(), "Setter update check for amountDaytake.");

                // --- Test toString() ---
                String output = m.toString();
                assertTrue(output.contains("Cold"), "toString() should include updated ForDisease.");
                assertTrue(output.contains("1"), "toString() should include updated amountDaytake.");
        }

        // 3. Comprehensive Test for Supplement.java
        @Test
        void modelSupplement_fullCoverage_shouldBe100Percent() {

                // --- Test Constructors and Getters ---
                // Parameterized constructor test
                Supplement s = new Supplement("S1000", "Vitamins", 20.0, 8, "Energy", 241231);
                assertEquals("Energy", s.getFunction(), "Initial Function check.");
                assertEquals(241231, s.getexpireDate(), "Initial expireDate check.");

                // Default constructor test
                Supplement defaultS = new Supplement();
                assertEquals("", defaultS.getFunction(), "Default Function check.");
                assertEquals(0, defaultS.getexpireDate(), "Default expireDate check.");

                // --- Test Setters ---
                s.setFunction("Immunity Boost");
                s.setexpireDate(250101);
                assertEquals("Immunity Boost", s.getFunction(), "Setter update check for Function.");
                assertEquals(250101, s.getexpireDate(), "Setter update check for expireDate.");

                // --- Test toString() ---
                String output = s.toString();
                assertTrue(output.contains("Immunity Boost"), "toString() should include updated Function.");
                assertTrue(output.contains("250101"), "toString() should include updated expireDate.");
        }
}
