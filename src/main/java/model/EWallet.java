package main.java.model;
import java.util.Scanner;

import main.java.controller.Home;
import main.java.repository.orderList;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;

public class EWallet extends Transaction
{
	private String name;
	private String PhoneNumber;
	
	public EWallet()
	{
		super(0.00,0.00);
		name = "";
		PhoneNumber = "";
	}
	
	public EWallet (double TotalPrice,double Discount, String name, String PhoneNumber)
	{
		super(TotalPrice, Discount);
		this.name = name;
		this.PhoneNumber = PhoneNumber;
	}
	
	public void setname (String name)
	{
		this.name = name;
	}
	public void setPhoneNumber (String PhoneNumber)
	{
		this.PhoneNumber = PhoneNumber;
	}
	
	public String getname()
	{
		return name;
	}
	public String getPhoneNumber()
	{
		return PhoneNumber;
	}
//EWALLET PAYMENT
	/**
	 * @deprecated This method violates MVC pattern. Use TransactionController.payForOrder() with TransactionView instead.
	 */
	@Deprecated
	public static void saveewallet(String orderNumber)
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
				
				//EWallet INPUT
				String PayMethod = "EWallet";
				System.out.print("\nEnter Your Name: ");
				String name = scanner.nextLine();
				
				System.out.print("\nEnter Your Phone Number (XXX-XXX-XXXX or XXX-XXXX-XXXX): ");
				String PhoneNumber = scanner.nextLine();
				while(!PhoneNumber.matches("^(\\d{3}-\\d{3}-\\d{4}|\\d{3}-\\d{4}-\\d{4})$"))
				{
					System.out.println("\n*Phoen No must be XXX-XXX-XXXX or XXX-XXXX-XXXX");
					System.out.print("\nEnter Your Phone Number (XXX-XXX-XXXX or XXX-XXXX-XXXX): ");
					PhoneNumber = scanner.nextLine();
				}
				//Confirmation
				System.out.print("\nComfirm Adding? (Y for Yes / N for No): ");
	            String comfirmAdding = scanner.next();
	            
	            if(comfirmAdding.equals("Y") || comfirmAdding.equals("y"))
	            {
	            	writer.println(orderInfo[0] + "||" + TotalPrice + "||" + Discount + "||" + DiscountAmount + "||" + Tax + "||" + FinalPrice + "||" + name + "||" + PhoneNumber + "||" + PayMethod);
	            	System.out.println("\n*Payment Success.");
	            	writer.flush();
	            	writer.close();
	            	
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