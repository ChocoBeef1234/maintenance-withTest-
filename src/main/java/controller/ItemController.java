package main.java.controller;

import main.java.config.MenuOption;
import main.java.model.ItemRecord;
import main.java.repository.ItemRepository;
import main.java.view.ItemView;

public class ItemController {
    private final ItemRepository repository;
    private final ItemView view;

    public ItemController() {
        this.repository = new ItemRepository();
        this.view = new ItemView();
    }

    public void run() {
        boolean back = false;
        while (!back) {
            int sel = view.menu();
            
            // Map the integer input back to the enum constant
            MenuOption selectedOption = MenuOption.fromValue(sel);
            
            // The switch statement now uses the enum constant's value
            switch (selectedOption != null ? selectedOption.getValue() : -1) {
                case 1: // MenuOption.ADD.getValue()
                    handleAdd();
                    break;
                case 2: // MenuOption.SEARCH.getValue()
                    handleSearch();
                    break;
                case 3: // MenuOption.MODIFY.getValue()
                    handleModify();
                    break;
                case 4: // MenuOption.DELETE.getValue()
                    handleDelete();
                    break;
                case 5: // MenuOption.EXIT.getValue()
                    back = true;
                    break;
                default:
                    // This block is theoretically unreachable due to ItemView validation
                    view.info("\nInvalid input."); 
            }
        }
    }

    private void handleAdd() {
        ItemRecord.Type type = view.promptType();
        ItemRecord record = view.promptNew(type);
        try {
            boolean ok = repository.add(record);
            view.info(ok ? "\nItem added." : "\nItem code exists or file missing.");
        } catch (Exception e) {
            e.printStackTrace();
            view.info("\nFailed to add item.");
        }
    }

    private void handleSearch() {
        try {
            view.info("\n1 = All, 2 = By Code");
            String code = view.promptCode("Enter Item Code (or leave empty for all): ");
            if (code.trim().isEmpty()) {
                view.showList(repository.findAll());
            } else {
                ItemRecord r = repository.findByCode(code.trim());
                if (r != null) view.show(r);
                else view.info("\nItem not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.info("\nFailed to search items.");
        }
    }

    private void handleModify() {
        String oldCode = view.promptCode("Enter Item Code to modify: ").trim();
        // Handle 'X' input from promptCode to exit modify operation
        if (oldCode.equals("X")) {
            return;
        }
        
        try {
            ItemRecord current = repository.findByCode(oldCode);
            if (current == null) {
                view.info("\nItem not found.");
                return;
            }
            
            // Get updated record, which may contain a new code
            ItemRecord updated = view.promptUpdate(current);
            
            // --- FIX 1: Handle user choosing to back out during update prompting ---
            if (updated == null) {
                view.info("\nModification cancelled.");
                return;
            }
            // ---------------------------------------------------------------------

            String newCode = updated.getCode();
            
            // Check: If the code changed, ensure the new code doesn't exist.
            // This is the correct logic for preventing duplicate codes on update.
            if (!oldCode.equals(newCode) && repository.findByCode(newCode) != null) {
                view.info("\nFailed to update item. The new Item Code '" + newCode + "' already exists.");
                return;
            }

            // The update method now requires the old code to find the original line,
            // and the updated record to write the new line.
            boolean ok = repository.update(oldCode, updated);
            view.info(ok ? "\nItem updated." : "\nFailed to update item.");
        } catch (Exception e) {
            e.printStackTrace();
            view.info("\nFailed to modify item.");
        }
    }

    private void handleDelete() {
        String code = view.promptCode("Enter Item Code to delete: ");
        try {
            boolean ok = repository.delete(code.trim());
            view.info(ok ? "\nItem deleted." : "\nItem not found.");
        } catch (Exception e) {
            e.printStackTrace();
            view.info("\nFailed to delete item.");
        }
    }
}

