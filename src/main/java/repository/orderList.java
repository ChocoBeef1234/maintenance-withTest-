package main.java.repository;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;
import main.java.model.ORDER;
import main.java.model.Transaction;
import main.java.controller.Home;

/**
 * @deprecated This class violates MVC pattern by containing UI logic (Scanner, System.out).
 * Use OrderRepository, OrderController, and OrderView instead for proper MVC implementation.
 * This class is kept for backward compatibility with deprecated code only.
 */
@Deprecated
public class orderList extends ORDER
{
	private double totaltotal;

	
	public orderList(){
		super("");
		totaltotal = 0.0;
	}
	
	public orderList(String OrderNumber){
		super(OrderNumber);
	}

public void settotaltotal(double totaltotal){
	this.totaltotal = totaltotal;
}

public double gettotaltotal(){
	return totaltotal;
}


//method
/**
 * @deprecated Use OrderController.handleAdd() instead
 */
@Deprecated
public static void addOrder() 
{
    try 
    {
        File orderFile = new File("Order.txt");
        File itemFile = new File("Item.txt");
        int totalOrder = gettotalOrder();
        int numadd = 1;

        if (!orderFile.exists()) 
        {
            System.out.println("!!!Error: Order.txt does not exist");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        
 
        System.out.println("\nAdding Order " + (totalOrder + numadd) + ":");
        System.out.print("\nEnter Order Number(O****): ");
        String orderNumber = scanner.nextLine();
        
        if (orderNumber.equalsIgnoreCase("X")) {
        Home.Order();
        return; 
    }
        while(!orderNumber.matches("^O.{3,4}$")){
        	System.out.println("\nOrder Number must start with O and must have only 4 to 5 words");
            System.out.print("Enter Order Number(O****): ");
            orderNumber = scanner.nextLine();	
            }
            
        BufferedReader readerO = new BufferedReader(new FileReader(orderFile));
        String line;
        while((line = readerO.readLine()) != null){
            String[] orderinfo = line.split("\\|\\|");
            if(orderinfo[0].equals(orderNumber))
            {
            	System.out.print("\nOrder Number existed");
            	addOrder();
            }
        }
        readerO.close();
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		String[] itemCode = new String[50];
		double[] itemQuantity = new double[50];
		double[] itemPrice = new double[50];
		double[] subtotal = new double[50];
		double[] itemQ = new double[50];
		String[] itemD = new String[50];
		String[] itemE = new String[50];
		String[] itemF = new String[50];
		double[] newitemQ = new double[50];
		int[] itemQInt = new int[itemQ.length];
		int[] itemQuantityInt = new int[itemQuantity.length];
		
		double totaltotal = 0;
        int i =0;
        String itemconfirm = "Y";

		
			
			
		if (!itemFile.exists()) 
        {
            System.out.println("!!!Error: Item.txt does not exist");
            return;
        }

		while (itemconfirm.equalsIgnoreCase("Y")) {
    ORDER.displayItem();
    System.out.print("\nEnter ItemCode (X to exit): ");
    itemCode[i] = scanner.next();
    scanner.nextLine();

    if (itemCode[i].equalsIgnoreCase("X")) {
        Home.Order();
        return;
    }
    
    while(!itemCode[i].matches("^S.{3,4}$") && !itemCode[i].matches("^M.{3,4}$")){
        	System.out.println("\nItem Code must start with S or M and with 4 number");
            System.out.print("\nEnter ItemCode (X to exit): ");
    		itemCode[i] = scanner.next();
    		scanner.nextLine();	
            }
		BufferedReader reader = new BufferedReader(new FileReader(itemFile));
		String linee;
		boolean itemFound = false;
    

    while ((linee = reader.readLine()) != null) {
        String[] iteminfo = linee.split("\\|\\|");
        itemQ[i] = Double.parseDouble(iteminfo[3]);
		itemD[i] = iteminfo[1];
		itemE[i] = iteminfo[4];
		itemF[i] = iteminfo[5];
        if (iteminfo[0].equals(itemCode[i])) {
            System.out.print("\nEnter Quantity: ");
            itemQuantity[i] = scanner.nextDouble();
            scanner.nextLine();
            

            while (itemQuantity[i] >= itemQ[i]) { // Check if the entered quantity is greater than available quantity
                System.out.println("\nQuantity not enough");
                System.out.print("Enter Quantity: ");
                itemQuantity[i] = scanner.nextDouble();
                scanner.nextLine();

                if (itemQuantity[i] < 0) {
                    System.out.println("\nQuantity cannot be negative");
                    System.out.print("Enter Quantity: ");
                    itemQuantity[i] = scanner.nextDouble();
                    scanner.nextLine();
                }
                
            }
            
            itemQ[i] -= itemQuantity[i];
            itemPrice[i] = Double.parseDouble(iteminfo[2]);
            subtotal[i] = itemPrice[i] * itemQuantity[i];
            totaltotal += subtotal[i];
            itemFound = true;
            
			break;
        }
        
    }

    if (!itemFound) {
        System.out.println("Item not found");
        i--;
    }
reader.close();
    System.out.println("Next item? (Y for Yes / N for No):");
    itemconfirm = scanner.next();
    scanner.nextLine();
    
    
    i++;
}

            	if(itemconfirm.equalsIgnoreCase("N")){
       				 File delitemMFile = new File("newitem2.txt");
       				 
				
       				boolean itemMod = true;
        			String itemLine;
        			String nextitem = null;
        			
      				System.out.print("\nComfirm Adding? (Y for Yes / N for No): ");
            		String comfirmAdding = scanner.next();
            	
            		if(comfirmAdding.equalsIgnoreCase("Y")){
        				PrintWriter writer = new PrintWriter(new FileWriter(orderFile, true));
        				
        				for (int t = 0; t < itemQ.length; t++) {
   						itemQInt[t] = (int) itemQ[t];
   						itemQuantityInt[t] = (int) itemQuantity[t];
						}

        				
            			writer.print(orderNumber + "||" + ORDER.getcalander() + "||");
						for(int j = 0;j < i; j++){
							writer.print(itemCode[j] + "||" + itemQuantityInt[j] + "||" + subtotal[j] + "||");
							
						BufferedReader itemMReader = new BufferedReader(new FileReader(itemFile));
        				PrintWriter itemMWriter = new PrintWriter(new FileWriter(delitemMFile, true));
							while ((itemLine = itemMReader.readLine()) != null) {
            					String[] itemInfo = itemLine.split("\\|\\|");
            					
           						if (itemInfo[0].equals(itemCode[j])) {
									itemQ[j] = Math.max(0, itemQ[j]); // Ensure quantity is not negative
                					String updatedItemLine = itemCode[j] + "||" + itemD[j] + "||" + itemPrice[j] + "||" + itemQInt[j] + "||" + itemE[j] + "||" + itemF[j];
                	 				itemMWriter.println(updatedItemLine);
                	 				
            					} 
            					else {
                					itemMWriter.println(itemLine);
            					}
						}
						itemMReader.close();
        				itemMWriter.close();
        				if (itemFile.delete()) {
    			delitemMFile.renameTo(itemFile);
       			 }
					else {
    					System.out.println("\n!!!Error: Original item.txt file not found.");
					}
						
        					
								
        					

	
        						
        						
								}
        writer.print(totaltotal + "||" );
					writer.print("\n");   
						writer.close();
               		
            		
					
					System.out.println("\n*Order added successfully.");
        			numadd++;
        			writer.close();
       	 			ORDER.totalOrder ++;
       	 			Transaction.PaymentSelection(orderNumber);
        		} 
        		
        else if(comfirmAdding.equalsIgnoreCase("N"))
        {
        	System.out.println("\n*Order Fail to Add.");
        	Home.Order();
        	return;
        }
        else
        {
        	System.out.println("\n\n*Invalid input*");
        	orderList.addOrder();
        }
      		}
      		else {
      			System.out.println("\n\n*Invalid input*");
        	orderList.addOrder();
      		}
            	     		     		
}
catch (IOException e) {
        e.printStackTrace();
    }


}


/**
 * @deprecated Use OrderController.handleDelete() instead
 */
@Deprecated
public static void deleteOrder() {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter Order Code to Delete (X to exit): ");
    String deleteCode = scanner.nextLine();

    if (deleteCode.equalsIgnoreCase("X")) {
        Home.Order();
        return;  // Exit the method
    }

    while (!deleteCode.matches("^O.{3,4}$")) {
        System.out.println("\nOrder Number must start with O and must have only 5 characters");
        System.out.print("Enter Order Number (O****): ");
        deleteCode = scanner.nextLine();

        if (deleteCode.equalsIgnoreCase("X")) {
            Home.Order();
            return;  // Exit the method
        }
    }

    try {
        File orderFile = new File("Order.txt");
        File newOrderFile = new File("newOrder.txt");

        if (!orderFile.exists()) {
            System.out.println("!!!Error: Order.txt does not exist");
            return;
        }

        BufferedReader orderReader = new BufferedReader(new FileReader(orderFile));
        PrintWriter orderWriter = new PrintWriter(new FileWriter(newOrderFile));

        boolean orderFound = false;
        boolean orderDelete = true;
        String orderLine;
        String nextOrder = null;

        while ((orderLine = orderReader.readLine()) != null) {
            String[] orderInfo = orderLine.split("\\|\\|");

            if (orderInfo[0].equals(deleteCode)) {
            	
                System.out.println("\n\nOrder found:");
                System.out.println("Order Number: " + orderInfo[0]);
                System.out.println("Date: " + orderInfo[1]);

                System.out.print("\nConfirm to delete selected Order (Y for Yes / N for No): ");
                String confirm = scanner.nextLine();
                confirm = confirm.trim().toLowerCase(); // Convert to lowercase

                if (confirm.equals("n")) {
                    orderWriter.println(orderLine);
                    orderDelete = false;
                }
                else if(confirm.equals("y"))
                {orderDelete = true;
            }
            else{
            	System.out.println("Invalid input!!!");
            	Home.Order();
            }
            orderFound = true;
            } 
            else {
                orderWriter.println(orderLine);

            }
        }

        orderReader.close();
        orderWriter.close();

        if (!orderFound) {
            System.out.println("\n\n*Order Not Found");
            newOrderFile.delete();
        } else {
            if (!orderDelete) {
                System.out.println("\n\n*Order did not delete successfully.");
                newOrderFile.delete();
            } else {
                if (orderFile.exists()) {
    orderFile.delete();
    newOrderFile.renameTo(orderFile);
} else {
    System.out.println("\n!!!Error: Original Order.txt file not found.");
}
                System.out.println("\n\n*Order deleted successfully.");
            }
        }

       

        
    } catch (IOException e) {
        e.printStackTrace();
    }

    Home.Order();
}


/**
 * @deprecated Use OrderController.handleSearch() instead
 */
@Deprecated
public static void searchOrder() {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter Order Code to Search (X to exit): ");
    String searchCode = scanner.nextLine();

    if (searchCode.equalsIgnoreCase("X")) {
        Home.Order();
        return;  // Exit the method
    }

    while (!searchCode.matches("^O.{3,4}$")) {
        System.out.println("\nOrder Number must start with O and must have only 5 characters");
        System.out.print("Enter Order Number (O****): ");
        searchCode = scanner.nextLine();

        if (searchCode.equalsIgnoreCase("X")) {
            Home.Order();
            return;  // Exit the method
        }
    }

    try {
        File orderFile = new File("Order.txt");

        if (!orderFile.exists()) {
            System.out.println("!!!Error: Order.txt does not exist");
            return;
        }

        BufferedReader orderReader = new BufferedReader(new FileReader(orderFile));

        boolean orderFound = false;
        String orderLine;

        while ((orderLine = orderReader.readLine()) != null) {
            String[] orderInfo = orderLine.split("\\|\\|");

            if (orderInfo[0].equals(searchCode)) {
                System.out.println("\n\nOrder found!!!!!");
                System.out.println("Order Number: " + orderInfo[0]);
                System.out.println("Date: " + orderInfo[1]);
                System.out.println("=========================================");

                for (int c = 2; c < orderInfo.length - 1 ; c += 3) {
                    System.out.println("Item Code: " + orderInfo[c]);
                    System.out.println("Quantity: " + orderInfo[c + 1]);
                    System.out.println("Subtotal: RM" + orderInfo[c + 2] + "\n");
                }
                int k = orderInfo.length - 1;
                System.out.println("Total: " + orderInfo[k]);

                orderFound = true;
            }
        }

        orderReader.close();

        if (!orderFound) {
            System.out.println("\n\n*Order Not Found");
        }
		Home.Order();
    } catch (IOException e) {
        e.printStackTrace();
    }

    
}
}
