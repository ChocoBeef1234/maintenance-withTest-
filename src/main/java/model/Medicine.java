package main.java.model;

// Removed I/O, Scanner, and controller.Home imports

public class Medicine extends Item {

	private String ForDisease;
	private int amountDaytake;

	public Medicine() {
		super("", "", 0.0, 0);
		ForDisease = "";
		amountDaytake = 0;

	}

	public Medicine(String ItemCode, String ItemDescription, double ItemPrice, int ItemQuantity, String ForDisease,
			int amountDaytake) {
		super(ItemCode, ItemDescription, ItemPrice, ItemQuantity);
		this.ForDisease = ForDisease;
		this.amountDaytake = amountDaytake;
	}

	public void setForDisease(String ForDisease) {
		this.ForDisease = ForDisease;
	}

	public void setamountDaytake(int amountDaytake) {
		this.amountDaytake = amountDaytake;
	}

	public String getForDisease() {
		return ForDisease;
	}

	public int getamountDaytake() {
		return amountDaytake;
	}

	public String toString() {
		return super.toString() + "\n For Disease\t\t: " + ForDisease + "\n Amount day take\t: " + amountDaytake;
	}
}
