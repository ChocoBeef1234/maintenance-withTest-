package main.java.model;
import java.util.Scanner;

import main.java.repository.orderList;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ORDER{
//2.data fields
private String OrderNumber;
public static int totalOrder = 0;

//3.constructor no-arg
public ORDER(){
	OrderNumber = "";
	totalOrder++;
}

//4.constructor with arg
public ORDER(String OrderNumber){
	this.OrderNumber = OrderNumber;
	totalOrder++;
}

//5.methods
//set methods
public void setOrderNumber(String OrderNumber){
	this.OrderNumber = OrderNumber;
}

//get mothods
public String getOrderNumber(){
	return OrderNumber;
}

public static int gettotalOrder(){
	return totalOrder;
}

//methods
    public static String getcalander() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return currentDateTime.format(formatter);
    }

String date = getcalander();

public String toString(){
	return "Order Number :" + OrderNumber + "\nDate" + date + "\n";
}


	/**
	 * @deprecated This method violates MVC pattern. Use ItemRepository.findAll() with ItemView.showList() instead.
	 */
	@Deprecated
	public static void displayItem()
	{
		System.err.println("WARNING: displayItem() is deprecated. Use ItemController with ItemView instead.");
		// This method violates MVC - removed I/O logic
	}

// Method to add an order to the orderList
/**
 * @deprecated This method violates MVC pattern. Use OrderController.handleAdd() instead.
 */
@Deprecated
 	public static void addOrder()
	{
		Scanner scanner = new Scanner(System.in);

		  	System.out.println("   Adding Order      ");
		  	System.out.println("----------------------------------------");	
		  	orderList.addOrder();
	}
    
/**
 * @deprecated This method violates MVC pattern. Use OrderController.handleDelete() instead.
 */
@Deprecated
    public static void deleteOrder()
	{
		Scanner scanner = new Scanner(System.in);

		  	System.out.println("   deleting Order      ");
		  	System.out.println("----------------------------------------");	
		  	orderList.deleteOrder();
	}
	
/**
 * @deprecated This method violates MVC pattern. Use OrderController.handleSearch() instead.
 */
@Deprecated
	public static void searchOrder()
	{
		Scanner scanner = new Scanner(System.in);

		  	System.out.println("   searching Order      ");
		  	System.out.println("----------------------------------------");	
		  	orderList.searchOrder();
	}


}