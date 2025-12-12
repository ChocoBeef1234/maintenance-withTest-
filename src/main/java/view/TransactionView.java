package main.java.view;
import java.util.Scanner;
import main.java.model.TransactionRecord;

public class TransactionView {
    private final Scanner scanner = new Scanner(System.in);

    public int menu() {
        System.out.println("\n\n--------------------");
        System.out.println("   Transaction");
        System.out.println("--------------------");
        System.out.println("1. Search");
        System.out.println("2. Delete");
        System.out.println("3. Back");
        System.out.print("Select Function: ");
        return scanner.nextInt();
    }

    public String promptOrderCode(String prompt) {
        System.out.print(prompt);
        scanner.nextLine();
        return scanner.nextLine().trim();
    }

    public TransactionRecord.Method promptMethod() {
        System.out.println("\nSelect Payment Method:");
        System.out.println("1. Cash");
        System.out.println("2. Bank");
        System.out.println("3. E-Wallet");
        System.out.print("Selection: ");
        int s = scanner.nextInt();
        scanner.nextLine();
        if (s == 1) return TransactionRecord.Method.CASH;
        if (s == 2) return TransactionRecord.Method.BANK;
        return TransactionRecord.Method.EWALLET;
    }

    public double promptCash(double finalPrice) {
        System.out.print("Enter Pay Amount: RM");
        double pay = scanner.nextDouble();
        scanner.nextLine();
        while (pay < finalPrice) {
            System.out.println("Pay Amount cannot be less than Final Price.");
            System.out.print("Enter Pay Amount: RM");
            pay = scanner.nextDouble();
            scanner.nextLine();
        }
        return pay;
    }

    public String promptBankName() {
        System.out.print("Enter Bank Name: ");
        return scanner.nextLine();
    }

    public String promptAccount() {
        System.out.print("Enter Account Number (XXXX-XXXX-XXXX-XXXX): ");
        return scanner.nextLine();
    }

    public String promptName() {
        System.out.print("Enter Name: ");
        return scanner.nextLine();
    }

    public String promptPhone() {
        System.out.print("Enter Phone (XXX-XXX-XXXX or XXX-XXXX-XXXX): ");
        return scanner.nextLine();
    }

    public void showSummary(String orderNumber, double total, double discountPct, double discountAmt, double tax, double finalPrice) {
        System.out.printf("\nOrder: %s\nTotal: RM%.2f\nDiscount: %.2f%% (RM%.2f)\nTax: %.2f%%\nFinal: RM%.2f\n",
                orderNumber, total, discountPct, discountAmt, tax, finalPrice);
    }

    public void show(TransactionRecord r) {
        System.out.println("\nTransaction:");
        System.out.println("Order: " + r.getOrderNumber());
        System.out.println("Total: " + r.getTotalPrice());
        System.out.println("Discount: " + r.getDiscountPercent() + "% (" + r.getDiscountAmount() + ")");
        System.out.println("Tax: " + r.getTaxPercent() + "%");
        System.out.println("Final: " + r.getFinalPrice());
        System.out.println("Method: " + r.getMethod());
        System.out.println("Field1: " + r.getField1());
        System.out.println("Field2: " + r.getField2());
    }

    public void info(String msg) {
        System.out.println(msg);
    }
}

