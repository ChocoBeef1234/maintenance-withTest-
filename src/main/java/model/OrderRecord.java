package main.java.model;
import java.util.List;

public class OrderRecord {
    private final String orderNumber;
    private final String date;
    private final List<OrderLine> lines;
    private final double total;

    public OrderRecord(String orderNumber, String date, List<OrderLine> lines, double total) {
        this.orderNumber = orderNumber;
        this.date = date;
        this.lines = lines;
        this.total = total;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getDate() {
        return date;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public double getTotal() {
        return total;
    }
}

