package main.java.model;

// Removed I/O, Scanner, and controller.Home imports

public abstract class Item
{
	
	protected String ItemCode;
	protected String ItemDescription;
	protected double ItemPrice;
	protected int ItemQuantity;
	private static int totalItem = 0;
	
	public Item()
	{
		ItemCode = "";
		ItemDescription = "";
		ItemPrice = 0;
		ItemQuantity = 0;
		totalItem++;
	}
	
	public Item(String ItemCode,String ItemDescription,double ItemPrice,int ItemQuantity)
	{
		this.ItemCode = ItemCode;
		this.ItemDescription = ItemDescription;
		this.ItemPrice = ItemPrice;
		this.ItemQuantity = ItemQuantity;
		totalItem++;
	}

///////////////////////////////////////////////////////////////////////////////////////
//Get method
	public static int gettotalItem()
	{
		return totalItem;
	}
	
///////////////////////////////////////////////////////////////////////////////////////
//totalItem Increment

	public static void InctotalItem()
	{
		totalItem++;
	}
	
///////////////////////////////////////////////////////////////////////////////////////
// The entire deprecated 'public static void deleteItem()' method is REMOVED.

///////////////////////////////////////////////////////////////////////////////////////
//toString function

	public String toString()
	{        
	return " Item Code\t\t: " + ItemCode + "\n Item Description\t: " + ItemDescription + "\n Item Price\t\t: RM" + String.format("%.2f", ItemPrice) + "\n Item Quantity\t\t: " + ItemQuantity;
	}
}