package main.java.model;

public abstract class Item {

	protected String ItemCode;
	protected String ItemDescription;
	protected double ItemPrice;
	protected int ItemQuantity;
	private static int totalItem = 0;

	public Item(String ItemCode, String ItemDescription, double ItemPrice, int ItemQuantity) {
		this.ItemCode = ItemCode;
		this.ItemDescription = ItemDescription;
		this.ItemPrice = ItemPrice;
		this.ItemQuantity = ItemQuantity;
		totalItem++;
	}

	public static int gettotalItem() {
		return totalItem;
	}

	public static void InctotalItem() {
		totalItem++;
	}

	public String getItemCode() {
		return ItemCode;
	}

	public String getItemDescription() {
		return ItemDescription;
	}

	public double getItemPrice() {
		return ItemPrice;
	}

	public int getItemQuantity() {
		return ItemQuantity;
	}

	public void setItemCode(String ItemCode) {
		this.ItemCode = ItemCode;
	}

	public void setItemDescription(String ItemDescription) {
		this.ItemDescription = ItemDescription;
	}

	public void setItemPrice(double ItemPrice) {
		this.ItemPrice = ItemPrice;
	}

	public void setItemQuantity(int ItemQuantity) {
		this.ItemQuantity = ItemQuantity;
	}

	public String toString() {
		return " Item Code\t\t: " + ItemCode + "\n Item Description\t: " + ItemDescription + "\n Item Price\t\t: RM"
				+ String.format("%.2f", ItemPrice) + "\n Item Quantity\t\t: " + ItemQuantity;
	}
}
