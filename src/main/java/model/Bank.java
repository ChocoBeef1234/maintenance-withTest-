package main.java.model;
import java.util.Scanner;

import main.java.controller.Home;
import main.java.repository.orderList;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;


public class Bank extends Transaction
{
	private String BankName;
	private String AccountNumber;
	
	public Bank()
	{
		super(0.00,0.00);
		BankName = "";
		AccountNumber = "";
	}
	
	public Bank (double TotalPrice,double Discount, String BankName, String AccountNumber)
	{
		super(TotalPrice, Discount);
		this.BankName = BankName;
		this.AccountNumber = AccountNumber;
	}
	
	public void setBankName (String BankName)
	{
		this.BankName = BankName;
	}
	public void setAccountNumber (String AccountNumber)
	{
		this.AccountNumber = AccountNumber;
	}
	
	public String getBankName()
	{
		return BankName;
	}
	public String getAccountNumber()
	{
		return AccountNumber;
	}
//BANK PAYMENT
	/**
	 * @deprecated This method violates MVC pattern. Use TransactionController.payForOrder() with TransactionView instead.
	 */
	@Deprecated
	public static void savebank(String orderNumber)
	{
		try 
    	{
		    File Tranfile = new File("Transaction.txt");
		    File orderFile = new File("Order.txt");
		    
		    if (!orderFile.exists()) 
        	{
           	 	System.out.println("!!!Error: Order.txt does not exist");
           		return;
        	}
	        
	        PrintWriter writer = new PrintWriter(new FileWriter(Tranfile,true));
	        BufferedReader orderReader = new BufferedReader(new FileReader(orderFile));
	
	    	boolean orderFound = false;
	    	String orderLine;
	    	Scanner scanner = new Scanner(System.in);
	    	
		    
			while ((orderLine = orderReader.readLine()) != null) 
			{
            	String[] orderInfo = orderLine.split("\\|\\|");
            	if (orderInfo[0].equals(orderNumber))
            	{
            		System.out.println("Order Number:" + orderInfo[0]);
            		//define total price from ORDER FILE
            		int k = orderInfo.length -1;
            		double TotalPrice = Double.parseDouble(orderInfo[k]);
	            	System.out.printf("\nTotal Price\t\t:RM%.2f",TotalPrice);
	            	orderFound = true;
	            	
            	double Discount;
				//DISCOUNT
				if(TotalPrice >= 150.00)
				{
					Discount = 10;
				}
				else if (TotalPrice >= 100.00)
				{
					Discount = 5;
				}
				else 
				{
					Discount = 0;
				}
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			    System.out.print("\nDiscount\t\t:"+Discount +"%");
			    //DISCOUNT AMOUNT
				double DiscountAmount = TotalPrice * (Discount/100);
				System.out.printf("\nDiscount Amount\t:RM%.2f",DiscountAmount);
				double Tax = 6;
				System.out.print("\nTax\t\t\t\t:"+Tax +"%");
				//FINAL PRICE
				double FinalPrice = (TotalPrice - DiscountAmount) * ((Tax+100)/100);
				System.out.printf("\nFinal Price\t\t:RM%.2f",FinalPrice);
				
				//BANK INPUT
				String PayMethod = "Bank";
				System.out.print("\n\nEnter Bank Name: ");
				String BankName = scanner.nextLine();
				
				System.out.print("Enter Your Account Number (XXXX-XXXX-XXXX-XXXX): ");
				String AccountNumber = scanner.nextLine();
				String pattern = "\\d{4}-\\d{4}-\\d{4}-\\d{4}";
				
				//validation
				while(!AccountNumber.matches(pattern)) 
				{
				    System.out.println("Invalid Account Number format. \n\nPlease enter it in the format (XXXX-XXXX-XXXX-XXXX).");
				    System.out.print("Enter Your Account Number (XXXX-XXXX-XXXX-XXXX): ");
					AccountNumber = scanner.nextLine();
				}
				//Confirmation
				System.out.print("\nComfirm Adding? (Y for Yes / N for No): ");
	            String comfirmAdding = scanner.next();
	            
	            if(comfirmAdding.equals("Y") || comfirmAdding.equals("y"))
	            {
	            	writer.println(orderInfo[0] + "||" + TotalPrice + "||" + Discount + "||" + DiscountAmount + "||" + Tax + "||" + FinalPrice + "||" + BankName + "||" + AccountNumber + "||" + PayMethod);
	            	writer.flush();
	            	writer.close();
	            	System.out.println("\n*Payment Success.");
	            	
	            	System.out.print("\nDo you want to continue another order (Y for Yes / N for No): ");
            		String continueAdding = scanner.next();
        			if(continueAdding.equalsIgnoreCase("Y"))
        			{
        				orderList.addOrder();
        			}
        			else if(continueAdding.equalsIgnoreCase("N"))
        			{
        					
        				Home.Interfaceselection();
        			}
        			else
        			{
        				System.out.println("Invalid input");
        				System.out.print("\nDo you want to continue another order (Y for Yes / N for No): ");
            			continueAdding = scanner.next();
        			}
	            }
	            else if(comfirmAdding.equals("N") || comfirmAdding.equals("n"))
	        	{
	        	System.out.println("\n*Payment Unsuccess.");
	        	Transaction.PaymentSelection(orderNumber);
	        	}
	        	else
	        	{
	        		System.out.println("\n\n*Invalid input*");
	        		System.out.print("\nComfirm Adding? (Y for Yes / N for No): ");
	        		comfirmAdding = scanner.next();
	        	}
	            }
			}
    	}
		catch (IOException e) 
   		{
    		e.printStackTrace();
		}
		Home.Interfaceselection();
	}
}