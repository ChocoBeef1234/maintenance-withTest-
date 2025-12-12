package main.java.model;

public class TransactionRecord {
    public enum Method { CASH, BANK, EWALLET }

    private final String orderNumber;
    private final double totalPrice;
    private final double discountPercent;
    private final double discountAmount;
    private final double taxPercent;
    private final double finalPrice;
    private final Method method;
    private final String field1; // cash: payAmount / bank: bankName / ewallet: name
    private final String field2; // cash: change / bank: account / ewallet: phone

    public TransactionRecord(String orderNumber, double totalPrice, double discountPercent, double discountAmount,
                             double taxPercent, double finalPrice, Method method, String field1, String field2) {
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.taxPercent = taxPercent;
        this.finalPrice = finalPrice;
        this.method = method;
        this.field1 = field1;
        this.field2 = field2;
    }

    public String getOrderNumber() { return orderNumber; }
    public double getTotalPrice() { return totalPrice; }
    public double getDiscountPercent() { return discountPercent; }
    public double getDiscountAmount() { return discountAmount; }
    public double getTaxPercent() { return taxPercent; }
    public double getFinalPrice() { return finalPrice; }
    public Method getMethod() { return method; }
    public String getField1() { return field1; }
    public String getField2() { return field2; }
}

