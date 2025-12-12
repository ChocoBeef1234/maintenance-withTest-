package main.java.view;

import main.java.config.MenuOption;
import java.util.List;
import java.util.Scanner;
import main.java.model.ItemRecord;

public class ItemView {
    private final Scanner scanner = new Scanner(System.in);

    public int menu() {
        System.out.println("\n\n----------------------------------------");
        System.out.println("      Welcome to Item Interface");
        System.out.println("----------------------------------------");
        
        // Use the enum to print the menu options
        for (MenuOption option : MenuOption.values()) {
            System.out.println(option.getValue() + ". " + option.getDescription());
        }
        
        System.out.print("\nEnter your selection: ");
        
        // Get min and max values from the enum for robust validation message
        int min = MenuOption.ADD.getValue();
        int max = MenuOption.EXIT.getValue();
        String range = "(" + min + "-" + max + ")";

        int selection = -1;
        boolean valid = false;
        
        do {
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.print("*Invalid input. Enter your selection " + range + ": ");
                continue;
            }
            try {
                selection = Integer.parseInt(input);
                
                // Validate the range using the enum values
                if (selection >= min && selection <= max) {
                    valid = true;
                } else {
                    System.out.print("*Invalid input. Please enter a number " + range + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("*Invalid input. Please enter a number " + range + ": ");
            }
        } while (!valid);
        
        return selection;
    }

    // RETURNS NULL IF USER ENTERS 'X'
    public ItemRecord.Type promptType() {
        System.out.println("Choose Item type (X to back):");
        System.out.println("1. Medicine");
        System.out.println("2. Supplement");
        System.out.print("Selection: ");
        
        int sel = -1;
        boolean valid = false;
        
        do {
            String input = scanner.nextLine().trim().toUpperCase();
            
            if (input.equals("X")) { // Exit condition
                return null;
            }
            
            if (input.equals("1")) {
                sel = 1;
                valid = true;
            } else if (input.equals("2")) {
                sel = 2;
                valid = true;
            } else {
                System.out.print("*Invalid input. Enter 1, 2, or X: ");
            }
        } while (!valid);
        
        return sel == 1 ? ItemRecord.Type.MEDICINE : ItemRecord.Type.SUPPLEMENT;
    }

    // RETURNS NULL IF USER ENTERS 'X' AT ITEM CODE PROMPT
    public ItemRecord promptNew(ItemRecord.Type type) {
        String typeChar = type == ItemRecord.Type.MEDICINE ? "M" : "S";
        String codeFormat = typeChar + "xxxx";
        String code;
        
        // 1. Item Code Validation (Allows 'X' to back)
        do {
            System.out.print("Enter Item Code (" + codeFormat + ") (X to back): ");
            code = scanner.nextLine().trim().toUpperCase();

            if (code.equals("X")) { // Exit condition
                return null; 
            }
            
            if (code.length() == 5 && code.startsWith(typeChar)) {
                break;
            }
            System.out.println("*Invalid Item Code format. Must be 5 characters and start with '" + typeChar + "'.");
        } while (true);

        // 2. Description
        System.out.print("Enter Description: ");
        String desc = scanner.nextLine();

        // 3. Price Validation (Non-numeric and non-negative check)
        double price = 0.0;
        boolean validPrice = false;
        do {
            System.out.print("Enter Price: ");
            String priceStr = scanner.nextLine().trim();
            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) {
                     System.out.println("*Invalid price input. Price cannot be negative.");
                } else {
                    validPrice = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("*Invalid price input. Please enter a number.");
            }
        } while (!validPrice);
        
        // 4. Quantity Validation (Non-numeric and non-negative check)
        int qty = 0;
        boolean validQty = false;
        do {
            System.out.print("Enter Quantity: ");
            String qtyStr = scanner.nextLine().trim();
            try {
                qty = Integer.parseInt(qtyStr);
                if (qty < 0) {
                     System.out.println("*Invalid quantity input. Quantity cannot be negative.");
                } else {
                    validQty = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("*Invalid quantity input. Please enter a whole number.");
            }
        } while (!validQty);

        // 5. Extra 1 (String)
        String extra1Label = type == ItemRecord.Type.MEDICINE ? "For Disease" : "Function";
        System.out.print("Enter " + extra1Label + ": ");
        String extra1 = scanner.nextLine();

        // 6. Extra 2 (Integer) Validation (Non-numeric and non-negative check)
        String extra2Label = type == ItemRecord.Type.MEDICINE ? "Amount Day Take" : "Expire Date (YYYYMMDD)";
        int extra2 = 0;
        boolean validExtra2 = false;
        do {
            System.out.print("Enter " + extra2Label + ": ");
            String extra2Str = scanner.nextLine().trim();
            try {
                extra2 = Integer.parseInt(extra2Str);
                if (extra2 < 0) {
                     System.out.println("*Invalid " + extra2Label + " input. Value cannot be negative.");
                } else {
                    validExtra2 = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("*Invalid " + extra2Label + " input. Please enter a whole number.");
            }
        } while (!validExtra2);
        
        return new ItemRecord(code, desc, price, qty, type, extra1, extra2);
    }

    // RETURNS "X" IF USER ENTERS 'X'
    public String promptCode(String prompt) {
        System.out.print(prompt + " (X to back): ");
        // Ensure input is trimmed and converted to upper case for consistent 'X' check in controller
        return scanner.nextLine().trim().toUpperCase(); 
    }

    public ItemRecord promptUpdate(ItemRecord current) {
        System.out.println("\n--- Modifying Item: " + current.getCode() + " (" + current.getType() + ") ---");
        
        // 0. Item Code (Allows 'X' to back or empty to keep current)
        String newCode = current.getCode();
        String typeChar = current.getType() == ItemRecord.Type.MEDICINE ? "M" : "S";
        String codePrompt = "Item Code (current: " + current.getCode() + " / Leave empty to keep / X to back): ";

        do {
            System.out.print(codePrompt);
            String codeStr = scanner.nextLine().trim().toUpperCase();
            
            if (codeStr.equals("X")) { // Exit condition
                return null; // <-- This is correctly handled by the controller fix
            }
            
            if (codeStr.isEmpty()) {
                newCode = current.getCode();
                break;
            }

            // Basic validation for the new code format
            if (codeStr.length() == 5 && codeStr.startsWith(typeChar)) {
                newCode = codeStr;
                break;
            }
            System.out.println("*Invalid Item Code. Must be 5 characters and start with '" + typeChar + "'.");
        } while (true);

        // 1. Description
        System.out.print("Description [" + current.getDescription() + "]: ");
        String desc = scanner.nextLine();
        if (desc.isEmpty()) desc = current.getDescription();

        // 2. Price (Non-numeric and non-negative check)
        double price = current.getPrice();
        String priceLabel = "Price [RM" + String.format("%.1f", current.getPrice()) + "]: ";
        boolean validPrice = false;

        do {
            System.out.print(priceLabel);
            String priceStr = scanner.nextLine().trim();
            if (priceStr.isEmpty()) {
                validPrice = true;
            } else {
                try {
                    double tempPrice = Double.parseDouble(priceStr);
                    if (tempPrice < 0) {
                        System.out.println("*Invalid price input. Price cannot be negative.");
                    } else {
                        price = tempPrice;
                        validPrice = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("*Invalid price input. Please enter a number.");
                }
            }
        } while (!validPrice);

        // 3. Quantity (Non-numeric and non-negative check)
        int qty = current.getQuantity();
        String qtyLabel = "Quantity [" + current.getQuantity() + "]: ";
        boolean validQty = false;

        do {
            System.out.print(qtyLabel);
            String qtyStr = scanner.nextLine().trim();
            if (qtyStr.isEmpty()) {
                validQty = true;
            } else {
                try {
                    int tempQty = Integer.parseInt(qtyStr);
                    if (tempQty < 0) {
                        System.out.println("*Invalid quantity input. Quantity cannot be negative.");
                    } else {
                        qty = tempQty;
                        validQty = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("*Invalid quantity input. Please enter a whole number.");
                }
            }
        } while (!validQty);

        // 4. Extra Fields
        String extra1Label = current.getType() == ItemRecord.Type.MEDICINE ? "For Disease" : "Function";
        String extra2Label = current.getType() == ItemRecord.Type.MEDICINE ? "Amount Day Take" : "Expire Date (YYYYMMDD)";

        // Extra 1 (String)
        System.out.print(extra1Label + " [" + current.getExtra1() + "]: ");
        String extra1 = scanner.nextLine();
        if (extra1.isEmpty()) extra1 = current.getExtra1();

        // Extra 2 (Integer) (Non-numeric and non-negative check)
        int extra2 = current.getExtra2();
        String extra2Prompt = extra2Label + " [" + current.getExtra2() + "]: ";
        boolean validExtra2 = false;

        do {
            System.out.print(extra2Prompt);
            String extra2Str = scanner.nextLine().trim();
            if (extra2Str.isEmpty()) {
                validExtra2 = true;
            } else {
                try {
                    int tempExtra2 = Integer.parseInt(extra2Str);
                    if (tempExtra2 < 0) {
                        System.out.println("*Invalid " + extra2Label + " input. Value cannot be negative.");
                    } else {
                        extra2 = tempExtra2;
                        validExtra2 = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("*Invalid " + extra2Label + " input. Please enter a whole number.");
                }
            }
        } while (!validExtra2);

        return new ItemRecord(newCode, desc, price, qty, current.getType(), extra1, extra2);
    }

    public void show(ItemRecord r) {
        System.out.println("\nItem:");
        System.out.println("Code: " + r.getCode());
        System.out.println("Description: " + r.getDescription());
        System.out.println("Price: RM" + String.format("%.2f", r.getPrice())); 
        System.out.println("Quantity: " + r.getQuantity());
        if (r.getType() == ItemRecord.Type.MEDICINE) {
            System.out.println("For Disease: " + r.getExtra1());
            System.out.println("Amount Day Take: " + r.getExtra2());
        } else {
            System.out.println("Function: " + r.getExtra1());
            System.out.println("Expire Date: " + r.getExtra2());
        }
    }

    public void showList(List<ItemRecord> list) {
        for (ItemRecord r : list)
            show(r);
    }

    public void info(String msg) {
        System.out.println(msg);
    }
}