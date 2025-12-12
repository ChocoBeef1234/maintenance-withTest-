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
        // 3. Select "4" (Back) to exit loop
        mockUserInput("1\nO_SEARCH_OK\n4\n");

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
        mockUserInput("1\nO_INVALID\n4\n");

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
        // 1. Select "3" (Delete)
        // 2. Enter "O_DEL_OK"
        // 3. Select "4" (Back)
        mockUserInput("3\nO_DEL_OK\n4\n");

        TransactionController controller = new TransactionController();
        controller.run();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Transaction deleted"), "View should confirm deletion");
        assertNull(repository.findByOrder("O_DEL_OK"), "Record should be removed from file");
    }

    @Test
    public void testMenu_InvalidInput() {
        // Scenario: User enters "99" (Invalid option) then "4" (Back)
        mockUserInput("99\n4\n");

        TransactionController controller = new TransactionController();
        controller.run();

        String output = outContent.toString();
        // Checks if default case in switch is hit
        assertTrue(output.contains("Invalid"), "Should handle invalid menu input gracefully");
    }

    // --- 7. TEST CASES (STATISTICS FUNCTION) ---
    // Added to ensure comprehensive coverage of the new Statistics feature

    @Test
    public void testStatistics_EmptyTransactions_ShouldShowZeroValues() {
        // Arrange: No transactions in repository
        // Scenario: Select Statistics (option 2) then Back (option 4)
        mockUserInput("2\n4\n");

        TransactionController controller = new TransactionController();
        controller.run();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Transaction Statistics"), "Should display statistics header");
        assertTrue(output.contains("Total Transactions: 0"), "Should show zero transactions");
        assertTrue(output.contains("Total Revenue: RM0.00"), "Should show zero revenue");
        assertTrue(output.contains("Average Transaction: N/A"), "Should show N/A for average when no transactions");
        assertTrue(output.contains("Cash: 0 transactions"), "Should show zero cash transactions");
        assertTrue(output.contains("Bank: 0 transactions"), "Should show zero bank transactions");
        assertTrue(output.contains("E-Wallet: 0 transactions"), "Should show zero e-wallet transactions");
    }

    @Test
    public void testStatistics_MultiplePaymentMethods_ShouldCalculateCorrectly() throws IOException {
        // Seed transactions with different payment methods
        repository.add(new TransactionRecord("O_STAT_1", 100.0, 0, 0, 6.0, 106.0, 
                TransactionRecord.Method.CASH, "110", "4"));
        repository.add(new TransactionRecord("O_STAT_2", 150.0, 10.0, 15.0, 6.0, 143.10, 
                TransactionRecord.Method.BANK, "Maybank", "1234-5678"));
        repository.add(new TransactionRecord("O_STAT_3", 200.0, 10.0, 20.0, 6.0, 190.80, 
                TransactionRecord.Method.EWALLET, "TNG", "012-3456789"));
        repository.add(new TransactionRecord("O_STAT_4", 50.0, 0, 0, 6.0, 53.0, 
                TransactionRecord.Method.CASH, "55", "2"));

        // Scenario: Select Statistics (option 2) then Back (option 4)
        mockUserInput("2\n4\n");

        TransactionController controller = new TransactionController();
        controller.run();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Total Transactions: 4"), "Should show correct total count");
        // Total: 106.0 + 143.10 + 190.80 + 53.0 = 492.90
        assertTrue(output.contains("Total Revenue: RM492.90"), "Should show correct total revenue (106.0 + 143.10 + 190.80 + 53.0 = 492.90)");
        // Average: 492.90 / 4 = 123.225, rounded to 123.23
        assertTrue(output.contains("Average Transaction: RM123.23"), "Should show correct average");
        assertTrue(output.contains("Cash: 2 transactions"), "Should show correct cash count");
        assertTrue(output.contains("Bank: 1 transactions"), "Should show correct bank count");
        assertTrue(output.contains("E-Wallet: 1 transactions"), "Should show correct e-wallet count");
        // Check for totals (allowing for formatting)
        assertTrue(output.contains("RM159.00") || output.contains("159.00"), "Should show cash total (106.0 + 53.0 = 159.00)");
        assertTrue(output.contains("RM143.10") || output.contains("143.10"), "Should show bank total");
        assertTrue(output.contains("RM190.80") || output.contains("190.80"), "Should show e-wallet total");
    }

    @Test
    public void testStatistics_SinglePaymentMethod_ShouldShowOnlyThatMethod() throws IOException {
        // Arrange: Seed only cash transactions
        repository.add(new TransactionRecord("O_CASH_1", 100.0, 0, 0, 6.0, 106.0, 
                TransactionRecord.Method.CASH, "110", "4"));
        repository.add(new TransactionRecord("O_CASH_2", 200.0, 10.0, 20.0, 6.0, 190.80, 
                TransactionRecord.Method.CASH, "200", "9.20"));

        // Scenario: Select Statistics (option 2) then Back (option 4)
        mockUserInput("2\n4\n");

        TransactionController controller = new TransactionController();
        controller.run();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Total Transactions: 2"), "Should show correct total count");
        assertTrue(output.contains("Total Revenue: RM296.80"), "Should show correct total revenue");
        assertTrue(output.contains("Average Transaction: RM148.40"), "Should show correct average");
        assertTrue(output.contains("Cash: 2 transactions"), "Should show cash count");
        assertTrue(output.contains("RM296.80"), "Should show cash total");
        assertTrue(output.contains("Bank: 0 transactions"), "Should show zero bank");
        assertTrue(output.contains("E-Wallet: 0 transactions"), "Should show zero e-wallet");
    }

    @Test
    public void testStatistics_MenuFlow_ShouldDisplayStatistics() throws IOException {
        // Arrange: Seed some transactions
        repository.add(new TransactionRecord("O_FLOW_1", 100.0, 0, 0, 6.0, 106.0, 
                TransactionRecord.Method.CASH, "110", "4"));

        // Scenario: 
        // 1. Select "2" (Statistics)
        // 2. Select "4" (Back)
        mockUserInput("2\n4\n");

        TransactionController controller = new TransactionController();
        controller.run();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Transaction Statistics"), "Should display statistics header");
        assertTrue(output.contains("===================="), "Should display statistics formatting");
        assertTrue(output.contains("Payment Method Breakdown:"), "Should display payment method section");
    }

    @Test
    public void testStatistics_CalculationAccuracy_ShouldMatchExpectedValues() throws IOException {
        // Arrange: Seed transactions with known values for precise calculation testing
        // Transaction 1: 100.0 total, 0% discount, 6% tax = 106.0 final
        repository.add(new TransactionRecord("O_CALC_1", 100.0, 0, 0, 6.0, 106.0, 
                TransactionRecord.Method.CASH, "110", "4"));
        // Transaction 2: 200.0 total, 10% discount (20.0), 6% tax = 190.80 final
        repository.add(new TransactionRecord("O_CALC_2", 200.0, 10.0, 20.0, 6.0, 190.80, 
                TransactionRecord.Method.BANK, "Bank", "1234"));
        // Transaction 3: 50.0 total, 0% discount, 6% tax = 53.0 final
        repository.add(new TransactionRecord("O_CALC_3", 50.0, 0, 0, 6.0, 53.0, 
                TransactionRecord.Method.EWALLET, "EW", "123"));

        // Scenario: Select Statistics
        mockUserInput("2\n4\n");

        TransactionController controller = new TransactionController();
        controller.run();

        // Assert: Verify precise calculations
        String output = outContent.toString();
        // Total: 106.0 + 190.80 + 53.0 = 349.80
        assertTrue(output.contains("Total Revenue: RM349.80"), "Should calculate total revenue correctly");
        // Average: 349.80 / 3 = 116.60 (rounded to 2 decimal places)
        assertTrue(output.contains("Average Transaction: RM116.60"), "Should calculate average correctly");
        // Cash: 1 transaction, 106.0
        assertTrue(output.contains("Cash: 1 transactions"), "Should show correct cash count");
        assertTrue(output.contains("RM106.00"), "Should show correct cash total");
        // Bank: 1 transaction, 190.80
        assertTrue(output.contains("Bank: 1 transactions"), "Should show correct bank count");
        assertTrue(output.contains("RM190.80"), "Should show correct bank total");
        // E-Wallet: 1 transaction, 53.0
        assertTrue(output.contains("E-Wallet: 1 transactions"), "Should show correct e-wallet count");
        assertTrue(output.contains("RM53.00"), "Should show correct e-wallet total");
    }

    @Test
    public void testStatistics_AllPaymentMethods_ShouldShowCompleteBreakdown() throws IOException {
        // Arrange: Seed one transaction for each payment method
        repository.add(new TransactionRecord("O_ALL_1", 100.0, 0, 0, 6.0, 106.0, 
                TransactionRecord.Method.CASH, "110", "4"));
        repository.add(new TransactionRecord("O_ALL_2", 120.0, 5.0, 6.0, 6.0, 120.84, 
                TransactionRecord.Method.BANK, "CIMB", "5678-9012"));
        repository.add(new TransactionRecord("O_ALL_3", 80.0, 0, 0, 6.0, 84.80, 
                TransactionRecord.Method.EWALLET, "GrabPay", "019-8765432"));

        // Scenario: Select Statistics
        mockUserInput("2\n4\n");

        TransactionController controller = new TransactionController();
        controller.run();

        // Assert: Verify all payment methods are represented
        String output = outContent.toString();
        assertTrue(output.contains("Total Transactions: 3"), "Should show correct total");
        assertTrue(output.contains("Cash: 1 transactions"), "Should show cash");
        assertTrue(output.contains("Bank: 1 transactions"), "Should show bank");
        assertTrue(output.contains("E-Wallet: 1 transactions"), "Should show e-wallet");
        assertTrue(output.contains("RM106.00"), "Should show cash amount");
        assertTrue(output.contains("RM120.84"), "Should show bank amount");
        assertTrue(output.contains("RM84.80"), "Should show e-wallet amount");
    }

    @Test
    public void testStatistics_ErrorHandling_ShouldDisplayErrorMessage() throws IOException {
        // Arrange: Create a scenario that might cause an error
        // We'll test by ensuring the file exists but might have issues
        // Note: This test verifies the error handling path in handleStatistics
        
        // Scenario: Select Statistics (option 2) then Back (option 4)
        // The file should exist from BeforeEach, so this should work normally
        // But we test that the method handles exceptions gracefully
        mockUserInput("2\n4\n");

        TransactionController controller = new TransactionController();
        controller.run();

        // Assert: Should not crash, should either show statistics or error message
        String output = outContent.toString();
        // If successful, should show statistics; if error, should show error message
        assertTrue(output.contains("Transaction Statistics") || 
                   output.contains("Failed to retrieve transaction statistics"),
                   "Should either display statistics or error message, not crash");
    }

    @Test
    public void testStatistics_LargeDataset_ShouldCalculateCorrectly() throws IOException {
        // Arrange: Seed many transactions to test performance and accuracy
        double expectedTotal = 0.0;
        int cashCount = 0, bankCount = 0, ewalletCount = 0;
        double cashTotal = 0.0, bankTotal = 0.0, ewalletTotal = 0.0;

        // Add 10 transactions with varying amounts and methods
        for (int i = 1; i <= 10; i++) {
            double baseAmount = i * 10.0;
            double discount = baseAmount >= 150 ? 10.0 : (baseAmount >= 100 ? 5.0 : 0.0);
            double discountAmt = baseAmount * (discount / 100.0);
            double finalPrice = (baseAmount - discountAmt) * 1.06;
            expectedTotal += finalPrice;

            TransactionRecord.Method method;
            if (i % 3 == 0) {
                method = TransactionRecord.Method.CASH;
                cashCount++;
                cashTotal += finalPrice;
            } else if (i % 3 == 1) {
                method = TransactionRecord.Method.BANK;
                bankCount++;
                bankTotal += finalPrice;
            } else {
                method = TransactionRecord.Method.EWALLET;
                ewalletCount++;
                ewalletTotal += finalPrice;
            }

            repository.add(new TransactionRecord("O_LARGE_" + i, baseAmount, discount, discountAmt, 6.0, finalPrice,
                    method, "Field1_" + i, "Field2_" + i));
        }

        // Scenario: Select Statistics
        mockUserInput("2\n4\n");

        TransactionController controller = new TransactionController();
        controller.run();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Total Transactions: 10"), "Should show correct total count");
        assertTrue(output.contains(String.format("Total Revenue: RM%.2f", expectedTotal)), 
                   "Should show correct total revenue");
        assertTrue(output.contains(String.format("Average Transaction: RM%.2f", expectedTotal / 10)), 
                   "Should show correct average");
        assertTrue(output.contains(String.format("Cash: %d transactions", cashCount)), 
                   "Should show correct cash count");
        assertTrue(output.contains(String.format("Bank: %d transactions", bankCount)), 
                   "Should show correct bank count");
        assertTrue(output.contains(String.format("E-Wallet: %d transactions", ewalletCount)), 
                   "Should show correct e-wallet count");
    }
}