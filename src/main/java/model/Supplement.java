package main.java.model;

public class Supplement extends Item {

	private String Function;
	private int expireDate;

	public Supplement() {
		super("", "", 0.0, 0);
		Function = "";
		expireDate = 0;

	}

	public Supplement(String ItemCode, String ItemDescription, double ItemPrice, int ItemQuantity, String Function,
			int expireDate) {
		super(ItemCode, ItemDescription, ItemPrice, ItemQuantity);
		this.Function = Function;
		this.expireDate = expireDate;
	}

	public void setFunction(String Function) {
		this.Function = Function;
	}

	public void setexpireDate(int expireDate) {
		this.expireDate = expireDate;
	}

	public String getFunction() {
		return Function;
	}

	public int getexpireDate() {
		return expireDate;
	}

	public String toString() {
		return super.toString() + "\n Function\t\t: " + Function + "\n Expire Date\t\t: " + expireDate;
	}
}
