package main.java.model;
public class OrderLine {
    private final String itemCode;
    private final int quantity;
    private final double subtotal;

    public OrderLine(String itemCode, int quantity, double subtotal) {
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public String getItemCode() {
        return itemCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }
}

