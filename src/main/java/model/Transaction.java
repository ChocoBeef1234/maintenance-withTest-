package main.java.model;

//class name
public class Transaction
{
//data field
	private double TotalPrice;
	private double Discount;
	private static double Tax =6;
	

//constructor no arg
	public Transaction ()
	{
		TotalPrice = 0.00;
		Discount = 0.00;
	}	
//constructor with arg
	public Transaction (double TotalPrice,double Discount)
	{
		this.TotalPrice = TotalPrice; 
		this.Discount = Discount;
	}
//method
//set methods
	public void setTotalPrice (double TotalPrice)
	{
		this.TotalPrice = TotalPrice;
	}
	public void setDiscount (double Discount)
	{
		this.Discount = Discount;
	}
	public static void setTax (double Tax)
	{
		Transaction.Tax = Tax;
	}	
//get method
	public double getTotalPrice()
	{
		return TotalPrice;
	}
	public double getDiscount ()
	{
		return Discount;
	}
	public static double getTax ()
	{
		return Tax;
	}
//~~~~~~~~~~~~~Choice of discount~~~~~~~~~~~~~
	/**
	 * @deprecated This method violates MVC pattern. Use TransactionController.payForOrder() instead.
	 */
	@Deprecated
	public static void PaymentSelection(String orderNumber)
	{
		System.err.println("WARNING: PaymentSelection() is deprecated. Use TransactionController.payForOrder() instead.");
		// This method violates MVC - removed UI logic
	}
	
	//SEARCH TRANSACTION - DEPRECATED: Use TransactionController.handleSearch() instead
	/**
	 * @deprecated This method violates MVC pattern. Use TransactionController.handleSearch() with TransactionView instead.
	 */
	@Deprecated
	public static void searchTran()
	{
		System.err.println("WARNING: searchTran() is deprecated. Use TransactionController.handleSearch() instead.");
		// This method violates MVC - removed UI logic
		// Original implementation removed - use TransactionController.handleSearch() instead
	}
	
	/**
	 * @deprecated This method violates MVC pattern. Use TransactionController.handleDelete() with TransactionView instead.
	 */
	@Deprecated
	public static void deleteTran()
	{
		System.err.println("WARNING: deleteTran() is deprecated. Use TransactionController.handleDelete() instead.");
		// This method violates MVC - removed UI logic
		// Original implementation removed - use TransactionController.handleDelete() instead
	}
	
}