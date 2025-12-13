package main.java.view;
import java.util.Scanner;
import main.java.config.OrderConstants;
import main.java.config.OrderMenuOption;
import main.java.controller.Validation;
import main.java.model.*;

public class OrderView {
    private final Scanner scanner = new Scanner(System.in);

    public int menu() {
        System.out.println(OrderConstants.MENU_HEADER);
        System.out.println(OrderConstants.MENU_TITLE);
        System.out.println(OrderConstants.MENU_SEPARATOR);
        for (OrderMenuOption option : OrderMenuOption.values()) {
            System.out.println(option.getDisplayText());
        }
        System.out.print(OrderConstants.MENU_SELECTION_PROMPT);
        return scanner.nextInt();
    }

    public String promptOrderNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            scanner.nextLine(); // consume newline
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println(OrderConstants.MSG_ORDER_NUMBER_REQUIRED);
                continue;
            }
            
            if (!Validation.isOrderNumber(input)) {
                System.out.println(OrderConstants.MSG_INVALID_ORDER_NUMBER_FORMAT);
                continue;
            }
            
            return input;
        }
    }

    public String promptOrderNumberForSearch(String prompt) {
        // For search, allow empty input to show all orders
        System.out.print(prompt);
        scanner.nextLine(); // consume newline
        String input = scanner.nextLine().trim();
        
        // If empty, return empty string (will show all orders)
        if (input.isEmpty()) {
            return input;
        }
        
        // If not empty, validate format
        if (!Validation.isOrderNumber(input)) {
            System.out.println(OrderConstants.MSG_INVALID_ORDER_NUMBER_FORMAT);
            return null; // Indicate invalid input
        }
        
        return input;
    }

    public boolean confirm(String prompt) {
        System.out.print(prompt);
        String c = scanner.next();
        scanner.nextLine();
        return c.equalsIgnoreCase(OrderConstants.YES_CODE);
    }

    public String promptItemCode() {
        while (true) {
            System.out.print(OrderConstants.PROMPT_ITEM_CODE);
            String input = scanner.nextLine().trim();
            
            // Allow exit code
            if (input.equalsIgnoreCase(OrderConstants.EXIT_CODE)) {
                return input;
            }
            
            if (input.isEmpty()) {
                System.out.println(OrderConstants.MSG_ITEM_CODE_REQUIRED);
                continue;
            }
            
            if (!Validation.isItemCode(input)) {
                System.out.println(OrderConstants.MSG_INVALID_ITEM_CODE_FORMAT);
                continue;
            }
            
            return input;
        }
    }

    public int promptQuantity() {
        while (true) {
            System.out.print(OrderConstants.PROMPT_QUANTITY);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println(OrderConstants.MSG_QUANTITY_REQUIRED);
                continue;
            }
            
            if (!Validation.isPositiveInteger(input)) {
                System.out.println(OrderConstants.MSG_INVALID_QUANTITY_FORMAT);
                continue;
            }
            
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(OrderConstants.MSG_INVALID_QUANTITY_FORMAT);
            }
        }
    }

    public boolean promptAddAnother() {
        System.out.print(OrderConstants.PROMPT_ADD_ANOTHER);
        String ans = scanner.nextLine().trim();
        return ans.equalsIgnoreCase(OrderConstants.YES_CODE);
    }

    public void showItemInfo(ItemRecord item) {
        String formattedPrice = String.format(OrderConstants.PRICE_FORMAT, item.getPrice());
        System.out.println(String.format(OrderConstants.DISPLAY_ITEM_INFO_FORMAT, 
            item.getCode(), item.getDescription(), formattedPrice, item.getQuantity()));
    }

    public void showItemNotFound(String code) {
        System.out.println(String.format(OrderConstants.DISPLAY_ITEM_NOT_FOUND_FORMAT, code));
    }

    public void showInvalidQuantity() {
        System.out.println(OrderConstants.MSG_QUANTITY_MUST_BE_POSITIVE);
    }

    public void show(OrderRecord record) {
        System.out.println(OrderConstants.DISPLAY_ORDER_HEADER);
        System.out.println(OrderConstants.DISPLAY_ORDER_NUMBER + record.getOrderNumber());
        System.out.println(OrderConstants.DISPLAY_DATE + record.getDate());
        for (OrderLine line : record.getLines()) {
            System.out.println(OrderConstants.DISPLAY_ITEM_PREFIX + line.getItemCode() + 
                OrderConstants.DISPLAY_QTY + line.getQuantity() + 
                OrderConstants.DISPLAY_SUBTOTAL + line.getSubtotal());
        }
        System.out.println(OrderConstants.DISPLAY_TOTAL + record.getTotal());
    }

    public void showUpdatePrompt(OrderRecord current) {
        System.out.println(OrderConstants.DISPLAY_UPDATE_HEADER + current.getOrderNumber() + OrderConstants.DISPLAY_UPDATE_FOOTER);
        System.out.println(OrderConstants.DISPLAY_CURRENT_ORDER_DETAILS);
        show(current);
        System.out.println(OrderConstants.DISPLAY_UPDATE_INSTRUCTIONS);
    }

    public void showUpdateCancelled() {
        System.out.println(OrderConstants.MSG_UPDATE_CANCELLED);
    }

    public void info(String msg) {
        System.out.println(msg);
    }
}

