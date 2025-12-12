package test.java;

import main.java.controller.*;
import main.java.model.*;
import main.java.repository.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestTransaction {

    private static final String TRANSACTION_FILE = "Transaction.txt";
    private static final String BACKUP_FILE = "Transaction.txt.bak";
    
    private final TransactionRepository repository = new TransactionRepository();
    
    // For capturing System.out to assert console messages
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    // --- 1. DATA PROTECTION & ENVIRONMENT SETUP ---

    @BeforeAll
    public static void backupData() throws IOException {
        File original = new File(TRANSACTION_FILE);
        File backup = new File(BACKUP_FILE);
        
        if (original.exists()) {
            Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Start with a clean slate for precise assertions
        new PrintWriter(original).close(); 
    }

    @AfterAll
    public static void restoreData() throws IOException {
        File original = new File(TRANSACTION_FILE);
        File backup = new File(BACKUP_FILE);

        if (backup.exists()) {
            Files.copy(backup.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
            backup.delete();
        }
    }

    @BeforeEach
    public void setUpStreams() throws FileNotFoundException {
        // Clear file content before each test to ensure isolation
        new PrintWriter(TRANSACTION_FILE).close();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    // --- 2. HELPER METHODS ---

    private void mockUserInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    private OrderRecord createMockOrder(String id, double totalAmount) {
        List<OrderLine> lines = new ArrayList<>();
        return new OrderRecord(id, "2023-10-01", lines, totalAmount);
    }

    // --- 3. TEST CASES (BUSINESS LOGIC & PAYMENTS) ---

    @Test
    public void testHighValueOrder_CashPayment_10PercentDiscount() throws IOException {
        // Arrange: Order > RM150 triggers 10% discount
        OrderRecord order = createMockOrder("O_CASH_001", 200.00);
        
        // Input: "1"(Cash) -> "200.00"(Pay Amount)
        mockUserInput("1\n200.00\n"); 

        // Act
        TransactionController controller = new TransactionController();
        controller.payForOrder(order);

        // Assert: Verify File Persistence
        TransactionRecord record = repository.findByOrder("O_CASH_001");
        assertNotNull(record, "Transaction should be saved");
        assertEquals(TransactionRecord.Method.CASH, record.getMethod());
        assertEquals(200.00, record.getTotalPrice(), 0.01);
        assertEquals(10.0, record.getDiscountPercent(), "Should be 10% discount");
        assertEquals(190.80, record.getFinalPrice(), 0.01);
        assertEquals("200.00", record.getField1()); // Amount Tendered
    }

    @Test
    public void testMediumValueOrder_BankPayment_5PercentDiscount() throws IOException {
        // Arrange: Order > RM100 triggers 5% discount
        OrderRecord order = createMockOrder("O_BANK_002", 120.00);

        // Input: "2"(Bank) -> "Maybank" -> "123456789"
        mockUserInput("2\nMaybank\n123456789\n");

        // Act
        TransactionController controller = new TransactionController();
        controller.payForOrder(order);

        // Assert
        TransactionRecord record = repository.findByOrder("O_BANK_002");
        assertNotNull(record);
        assertEquals(TransactionRecord.Method.BANK, record.getMethod());
        assertEquals(5.0, record.getDiscountPercent(), "Should be 5% discount");
        assertEquals("Maybank", record.getField1());
        assertEquals("123456789", record.getField2());
    }

    @Test
    public void testLowValueOrder_EWalletPayment_NoDiscount() throws IOException {
        // Arrange: Order < RM100 triggers 0% discount
        OrderRecord order = createMockOrder("O_EWALLET_003", 50.00);

        // Input: "3"(EWallet) -> "TNG" -> "012345678"
        mockUserInput("3\nTNG\n012345678\n");

        // Act
        TransactionController controller = new TransactionController();
        controller.payForOrder(order);

        // Assert
        TransactionRecord record = repository.findByOrder("O_EWALLET_003");
        assertNotNull(record);
        assertEquals(0.0, record.getDiscountPercent(), "Should be 0% discount");
        assertEquals("TNG", record.getField1());
    }

    // --- 4. TEST CASES (MODEL COVERAGE) ---
    // Added to ensure 100% coverage of TransactionRecord.java getters

    @Test
    public void testModelGetters() {
        TransactionRecord r = new TransactionRecord("O_MODEL", 100.0, 5.0, 5.0, 6.0, 100.7, 
                                                    TransactionRecord.Method.CASH, "110", "9.3");
        
        assertEquals("O_MODEL", r.getOrderNumber());
        assertEquals(100.0, r.getTotalPrice());
        assertEquals(5.0, r.getDiscountAmount());
        assertEquals(6.0, r.getTaxPercent());
        assertEquals(100.7, r.getFinalPrice());
        assertEquals("110", r.getField1());
        assertEquals("9.3", r.getField2());
    }

    // --- 5. TEST CASES (REPOSITORY COVERAGE) ---
    // Added to cover findAll() and delete failure paths

    @Test
    public void testRepositoryFindAll() throws IOException {
        // Arrange: Seed multiple transactions
        repository.add(new TransactionRecord("O_1", 10.0, 0, 0, 6, 10.6, TransactionRecord.Method.CASH, "11", "0.4"));
        repository.add(new TransactionRecord("O_2", 20.0, 0, 0, 6, 21.2, TransactionRecord.Method.CASH, "22", "0.8"));

        // Act
        List<TransactionRecord> all = repository.findAll();

        // Assert
        assertEquals(2, all.size(), "Should retrieve all records from file");
    }

    @Test
    public void testRepositoryDeleteFail() throws IOException {
        // Act: Delete non-existent
        boolean result = repository.delete("O_NON_EXISTENT");
        
        // Assert
        assertFalse(result, "Deleting non-existent record should return false");
    }

    // --- 6. TEST CASES (CONTROLLER MENU FLOW) ---
    // Added to cover the full Controller loop logic

    @Test
    public void testMenu_SearchSuccess_Flow() throws IOException {
        // Arrange: Seed data
        repository.add(new TransactionRecord("O_SEARCH_OK", 100.0, 0, 0, 6.0, 106.0, 
                TransactionRecord.Method.CASH, "110", "4"));

        // Scenario: 
        // 1. Select "1" (Search) 
        // 2. Enter "O_SEARCH_OK" (ID)
        // 3. Select "3" (Back) to exit loop
        mockUserInput("1\nO_SEARCH_OK\n3\n");

        TransactionController controller = new TransactionController();
        controller.run(); 

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Transaction:"), "View should display transaction details");
        assertTrue(output.contains("O_SEARCH_OK"), "View should show the Order ID");
    }

    @Test
    public void testMenu_SearchFail_Flow() {
        // Scenario: Search for invalid ID
        // 1. Search -> 2. Invalid ID -> 3. Back
        mockUserInput("1\nO_INVALID\n3\n");

        TransactionController controller = new TransactionController();
        controller.run(); 

        String output = outContent.toString();
        assertTrue(output.contains("Transaction not found"), "View should display not found message");
    }

    @Test
    public void testMenu_DeleteSuccess_Flow() throws IOException {
        // Arrange: Seed data
        repository.add(new TransactionRecord("O_DEL_OK", 100.0, 0, 0, 6.0, 106.0, 
                TransactionRecord.Method.CASH, "110", "4"));

        // Scenario: 
        // 1. Select "2" (Delete)
        // 2. Enter "O_DEL_OK"
        // 3. Select "3" (Back)
        mockUserInput("2\nO_DEL_OK\n3\n");

        TransactionController controller = new TransactionController();
        controller.run();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Transaction deleted"), "View should confirm deletion");
        assertNull(repository.findByOrder("O_DEL_OK"), "Record should be removed from file");
    }

    @Test
    public void testMenu_InvalidInput() {
        // Scenario: User enters "99" (Invalid option) then "3" (Back)
        mockUserInput("99\n3\n");

        TransactionController controller = new TransactionController();
        controller.run();

        String output = outContent.toString();
        // Checks if default case in switch is hit
        assertTrue(output.contains("Invalid"), "Should handle invalid menu input gracefully");
    }
}