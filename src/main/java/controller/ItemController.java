package main.java.controller;

import main.java.config.ItemMenuOption;
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
            ItemMenuOption selectedOption = ItemMenuOption.fromValue(sel);
            
            // The switch statement now uses the enum constant's value
            switch (selectedOption) {
                case ADD: 
                    handleAdd();
                    break;
                case SEARCH: 
                    handleSearch();
                    break;
                case MODIFY: 
                    handleModify();
                    break;
                case DELETE: 
                    handleDelete();
                    break;
                case EXIT: 
                    back = true;
                    break;
                default:
                    // Usually unreachable due to ItemView validation
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
        // 'X' to cancel modify operation
        if (oldCode.equals("X")) {
            return;
        }
        
        try {
            ItemRecord current = repository.findByCode(oldCode);
            if (current == null) {
                view.info("\nItem not found.");
                return;
            }
            
            // Get updated record
            ItemRecord updated = view.promptUpdate(current);
            
            if (updated == null) {
                view.info("\nModification cancelled.");
                return;
            }
            String newCode = updated.getCode();
            
            if (!oldCode.equals(newCode) && repository.findByCode(newCode) != null) {
                view.info("\nFailed to update item. The new Item Code '" + newCode + "' already exists.");
                return;
            }
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

