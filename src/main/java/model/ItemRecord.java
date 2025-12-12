package main.java.model;
public class ItemRecord {
    public enum Type { MEDICINE, SUPPLEMENT }

    private final String code;
    private final String description;
    private final double price;
    private final int quantity;
    private final Type type;
    private final String extra1; // ForDisease or Function
    private final int extra2;    // amountDayTake or expireDate

    public ItemRecord(String code, String description, double price, int quantity, Type type, String extra1, int extra2) {
        this.code = code;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.type = type;
        this.extra1 = extra1;
        this.extra2 = extra2;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public Type getType() { return type; }
    public String getExtra1() { return extra1; }
    public int getExtra2() { return extra2; }
}

