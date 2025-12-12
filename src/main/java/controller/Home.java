package main.java.controller;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

public class Home
{
	// Note: main() method has been moved to Main.java (root package)
	// This follows proper MVC architecture where the entry point is separate from controllers

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Main interface

	public static void Interfaceselection()
	{
		// Deprecated: replaced by MainController / MainMenuView.
		MainController controller = new MainController();
		controller.run();
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Staff interface

		public static void Staff(){
			// Deprecated: StaffController now handles flow.
			StaffController controller = new StaffController();
			controller.run();
		}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Item interface - DEPRECATED: Use ItemController instead
	
	@Deprecated
	public static void Item()
	{
		// Deprecated: Use ItemController instead
		ItemController controller = new ItemController();
		controller.run();
	}	
    

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Order interface - DEPRECATED: Use OrderController instead
    
    @Deprecated
    public static void Order()
	{
		// Deprecated: Use OrderController instead
		TransactionController transactionController = new TransactionController();
		OrderController controller = new OrderController(transactionController);
		controller.run();
	}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Transaction interface
		
	@Deprecated
	public static void Tranmenu()
	{
		// Deprecated: Use TransactionController instead
		TransactionController controller = new TransactionController();
		controller.run();
	}
	
	
/////////////////////////////////////////////////////////////////////////////////////////
//Summary function

	public static void summary()
	{
		System.out.println("\n\n================================================================");
		
	try{
		System.out.println("\n\n=========================");
		System.out.println("   Summary for Staff");
		System.out.println("=========================");
		File staffFile = new File("staff.txt");
        if (!staffFile.exists()){ 
            System.out.println("!!!Error: staff.txt does not exist");
            return;
        }

        BufferedReader staffreader = new BufferedReader(new FileReader(staffFile));
		String staffline;
        while ((staffline = staffreader.readLine()) != null)
       {
        		String[] staffinfo = staffline.split("\\|\\|");
        if (staffinfo.length >= 10) {
                    System.out.println("\nStaff ID: " + staffinfo[0]);
                    System.out.println("Password: " + staffinfo[1]);
                    System.out.println("---------------------");
                    System.out.println("First Name: " + staffinfo[2]);
                    System.out.println("Last Name: " + staffinfo[3]);
                    System.out.println("Phone No: " + staffinfo[4]);
                    System.out.println("Staff Position: " + staffinfo[5]);
                    System.out.println("Street: " + staffinfo[6]);
                    System.out.println("Postcode: " + staffinfo[7]);
                    System.out.println("Region: " + staffinfo[8]);
                    System.out.println("State: " + staffinfo[9]);	
        }
	   }
	   staffreader.close();
	    System.out.println("\n\n=========================");
	   	System.out.println("    Summary for Item");
		System.out.println("=========================");
		File ItemFile = new File("Item.txt");
        if (!ItemFile.exists()){ 
            System.out.println("!!!Error: Item.txt does not exist");
            return;
        }
        

        BufferedReader itemreader = new BufferedReader(new FileReader(ItemFile));
		String itemline;
        while ((itemline = itemreader.readLine()) != null)
       {
        		String[] iteminfo = itemline.split("\\|\\|");
        		
                	System.out.println("\n Item Code: " + iteminfo[0]);
                	System.out.println("--------------------");
                	System.out.println("Item Description: " + iteminfo[1]);
                	System.out.println("Item Price\t\t: " + iteminfo[2]);
                	System.out.println("Item Quantity\t: " + iteminfo[3]);
              
              		if(iteminfo[0].startsWith("M"))
              		{
              		System.out.println("For Disease\t\t: " + iteminfo[4]);
              		System.out.println("Amount Day Take\t: " + iteminfo[5]);	
              		}
              		else if(iteminfo[0].startsWith("S"))
              		{
              		System.out.println("Function\t\t: " + iteminfo[4]);
              		System.out.println("Expired Date\t: " + iteminfo[5]);
              		}		  
	   }
	   	itemreader.close();	
	   		
	  System.out.println("\n\n========================="); 
	  System.out.println("   Summary for Order");
	  System.out.println("=========================\n");
	  File orderFile = new File("Order.txt");
	  BufferedReader orderReader = new BufferedReader(new FileReader(orderFile));
	  String orderLine;
        while ((orderLine = orderReader.readLine()) != null) {
        String[] orderInfo = orderLine.split("\\|\\|");

      
        if (orderInfo.length >= 2) {
            System.out.println("Order Number: " + orderInfo[0]);
            System.out.println("-------------------------");
            System.out.println("Date: " + orderInfo[1]);

            for (int c = 2; c < orderInfo.length - 2; c += 3) {
                if (c + 2 < orderInfo.length) {
                    System.out.println("Item Code: " + orderInfo[c]);
                    System.out.println("Quantity: " + orderInfo[c + 1]);
                    System.out.println("Subtotal: RM" + orderInfo[c + 2] + "\n");
                } else {
                    System.out.println("\nInvalid order item data format: " + orderLine);
                }
            }
        } else {
            System.out.println("\nInvalid order data format: " + orderLine);
        }
    }
    orderReader.close();
    
    System.out.println("\n\n=========================");
    System.out.println(" Summary for Transaction");
	  System.out.println("=========================\n");
    File Tranfile = new File("Transaction.txt");
	double totalTran = 0.0;	    
		    if (!Tranfile.exists())
	        {
	            System.out.println("!!!Error: Transaction.txt does not exist");
	            return;
	        }
	        BufferedReader Tranreader = new BufferedReader(new FileReader(Tranfile));
	    	String Tranline;

            while((Tranline = Tranreader.readLine()) != null)
        	{
            	String[] Traninfo = Tranline.split("\\|\\|");
            	if (Traninfo.length >= 9)
            	{
	            	System.out.println("Transaction\t\t: "+ Traninfo[0]);
	            	System.out.println("-------------------------");
	            	double TotalPrice = Double.parseDouble(Traninfo[1]);
	            	System.out.printf("Total Price\t\t: RM%.2f", TotalPrice); 
	            	double Discount = Double.parseDouble(Traninfo[2]);
	            	System.out.printf("\nDiscount\t\t: %.2f", Discount);
	            	System.out.print("%");
	            	double DiscountAmount = Double.parseDouble(Traninfo[3]);
	            	System.out.printf("\nDiscount Amount\t: RM%.2f", DiscountAmount);
	            	double Tax = Double.parseDouble(Traninfo[4]);
	            	System.out.printf("\nTax\t\t\t\t: %.2f", Tax);
	            	System.out.print("%");
	            	double FinalPrice = Double.parseDouble(Traninfo[5]);
	            	System.out.printf("\nFinal Price\t\t: RM%.2f", FinalPrice);	
	            	totalTran += FinalPrice;
	            	
	            	if(Traninfo[8].equals("Cash"))
	            	{
	            		double PayAmount = Double.parseDouble(Traninfo[6]);
	            		System.out.printf("\nPay Amount\t\t: RM%.2f", PayAmount);
	            		double ChangeGive = Double.parseDouble(Traninfo[7]);
	            		System.out.printf("\nChange Give\t\t: RM%.2f", ChangeGive);
	            		System.out.println("\nPayment Method\t: " + Traninfo[8]);
	            		System.out.print("\n");
	            	}
	            	else if (Traninfo[8].equals("Bank"))
	            	{
	            		System.out.println("\nBank Name: " + Traninfo[6]);
	            		System.out.println("Account Num\t\t: " + Traninfo[7]);
	            		System.out.println("Payment Method\t: " + Traninfo[8]);
	            		System.out.print("\n");
	            	}
	            	else if (Traninfo[8].equals("EWallet"))
	            	{
	            		System.out.println("\nName\t\t\t: " + Traninfo[6]);
	            		System.out.println("Phone Num\t\t: " + Traninfo[7]);
	            		System.out.println("Payment Method\t: " + Traninfo[8]);
	            		System.out.print("\n");
	            	}

            	}
		    }
		    
		    System.out.println("\n\n================================================================");
		System.out.println("             TTTTTTTT  HH   HH   XX     XX                           ");
		System.out.println("                TT     HH   HH    XX   XX                            ");
		System.out.println("                TT     HHHHHHH      XXX                              ");
		System.out.println("                TT     HH   HH     XX XX                            ");
		System.out.println("                TT     HH   HH    XX   XX                           ");
		System.out.println("                TT     HH   HH   XX     XX                          ");
		System.out.println("================================================================");
		
		    System.out.printf("\n*Total Earn Today: RM%.2f", totalTran);
		    Tranreader.close();
		    	
	  } catch (IOException e) {
            e.printStackTrace();
        }
	System.exit(0);
	}
	
}