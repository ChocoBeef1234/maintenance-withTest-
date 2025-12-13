package main.java.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import main.java.config.OrderConstants;
import main.java.config.OrderMenuOption;
import main.java.model.ItemRecord;
import main.java.model.OrderLine;
import main.java.model.OrderRecord;
import main.java.repository.ItemRepository;
import main.java.repository.OrderRepository;
import main.java.view.OrderView;

public class OrderController {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderView view;
    private final TransactionController transactionController;

    public OrderController(TransactionController transactionController) {
        this.orderRepository = new OrderRepository();
        this.itemRepository = new ItemRepository();
        this.view = new OrderView();
        this.transactionController = transactionController;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            int sel = view.menu();
            OrderMenuOption selectedOption = OrderMenuOption.fromValue(sel);
            if (selectedOption == null) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (selectedOption) {
                case ADD:
                    handleAdd();
                    break;
                case SEARCH:
                    handleSearch();
                    break;
                case UPDATE:
                    handleUpdate();
                    break;
                case DELETE:
                    handleDelete();
                    break;
                case BACK:
                    back = true;
                    break;
                default:
                    view.info(OrderConstants.MSG_INVALID_INPUT);
            }
        }
    }

    private void handleAdd() {
        String orderNumber = view.promptOrderNumber(OrderConstants.PROMPT_ORDER_NUMBER_ADD);
        try {
            // Additional validation: check if order number already exists
            if (orderRepository.findByNumber(orderNumber) != null) {
                view.info(OrderConstants.MSG_ORDER_NUMBER_EXISTS);
                return;
            }
            List<OrderLine> lines = promptOrderLines();
            if (lines.isEmpty()) {
                view.info(OrderConstants.MSG_NO_ITEMS_ADDED);
                return;
            }
            double total = lines.stream().mapToDouble(OrderLine::getSubtotal).sum();
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern(OrderConstants.DATE_FORMAT_PATTERN));
            OrderRecord record = new OrderRecord(orderNumber, date, lines, total);
            boolean ok = orderRepository.add(record);
            if (!ok) {
                view.info(OrderConstants.MSG_FAILED_TO_SAVE_ORDER);
                return;
            }
            // reduce inventory
            for (OrderLine l : lines) {
                ItemRecord item = itemRepository.findByCode(l.getItemCode());
                if (item != null) {
                    ItemRecord updated = new ItemRecord(item.getCode(), item.getDescription(), item.getPrice(),
                            item.getQuantity() - l.getQuantity(), item.getType(), item.getExtra1(), item.getExtra2());
                    itemRepository.update(item.getCode(), updated);
                }
            }
            view.info(OrderConstants.MSG_ORDER_ADDED);
            transactionController.payForOrder(record);
        } catch (Exception e) {
            e.printStackTrace();
            view.info(OrderConstants.MSG_FAILED_TO_ADD_ORDER);
        }
    }

    private List<OrderLine> promptOrderLines() throws Exception {
        List<OrderLine> lines = new ArrayList<>();
        boolean more = true;
        while (more) {
            String code = view.promptItemCode();
            if (code.equalsIgnoreCase(OrderConstants.EXIT_CODE))
                break;

            ItemRecord item = itemRepository.findByCode(code);
            if (item == null) {
                view.showItemNotFound(code);
                continue;
            }

            view.showItemInfo(item);
            int qty = view.promptQuantity();
            if (qty <= OrderConstants.MIN_QUANTITY) {
                view.showInvalidQuantity();
                continue;
            }

            double subtotal = item.getPrice() * qty;
            lines.add(new OrderLine(code, qty, subtotal));
            more = view.promptAddAnother();
        }
        return lines;
    }

    private void handleSearch() {
        try {
            String code = view.promptOrderNumberForSearch(OrderConstants.PROMPT_ORDER_NUMBER_SEARCH);

            if (code == null) {
                // Invalid format, already displayed error message
                return;
            }

            if (code.isEmpty()) {
                // Empty input - show all orders
                for (OrderRecord r : orderRepository.findAll())
                    view.show(r);
                return;
            }

            // Valid order number - search for specific order
            OrderRecord r = orderRepository.findByNumber(code);
            if (r != null)
                view.show(r);
            else
                view.info(OrderConstants.MSG_ORDER_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            view.info(OrderConstants.MSG_FAILED_TO_SEARCH_ORDERS);
        }
    }

    private void handleUpdate() {
        String orderNumber = view.promptOrderNumber(OrderConstants.PROMPT_ORDER_NUMBER_UPDATE);
        try {
            OrderRecord current = orderRepository.findByNumber(orderNumber);
            if (current == null) {
                view.info(OrderConstants.MSG_ORDER_NOT_FOUND);
                return;
            }

            // Restore inventory from old order lines
            for (OrderLine oldLine : current.getLines()) {
                ItemRecord item = itemRepository.findByCode(oldLine.getItemCode());
                if (item != null) {
                    ItemRecord restored = new ItemRecord(
                            item.getCode(),
                            item.getDescription(),
                            item.getPrice(),
                            item.getQuantity() + oldLine.getQuantity(),
                            item.getType(),
                            item.getExtra1(),
                            item.getExtra2());
                    itemRepository.update(item.getCode(), restored);
                }
            }

            // Show update prompt and get new lines
            view.showUpdatePrompt(current);
            List<OrderLine> newLines = promptOrderLines();

            if (newLines.isEmpty()) {
                // User cancelled, restore the inventory back
                view.showUpdateCancelled();
                for (OrderLine oldLine : current.getLines()) {
                    ItemRecord item = itemRepository.findByCode(oldLine.getItemCode());
                    if (item != null) {
                        ItemRecord restored = new ItemRecord(
                                item.getCode(),
                                item.getDescription(),
                                item.getPrice(),
                                item.getQuantity() - oldLine.getQuantity(),
                                item.getType(),
                                item.getExtra1(),
                                item.getExtra2());
                        itemRepository.update(item.getCode(), restored);
                    }
                }
                return;
            }

            // Calculate total and create updated order record
            double newTotal = newLines.stream().mapToDouble(OrderLine::getSubtotal).sum();
            String newDate = current.getDate(); // Keep the original date
            OrderRecord updated = new OrderRecord(current.getOrderNumber(), newDate, newLines, newTotal);

            // Update the order in repository
            boolean ok = orderRepository.update(orderNumber, updated);
            if (!ok) {
                // If update failed, restore inventory back
                for (OrderLine oldLine : current.getLines()) {
                    ItemRecord item = itemRepository.findByCode(oldLine.getItemCode());
                    if (item != null) {
                        ItemRecord restored = new ItemRecord(
                                item.getCode(),
                                item.getDescription(),
                                item.getPrice(),
                                item.getQuantity() - oldLine.getQuantity(),
                                item.getType(),
                                item.getExtra1(),
                                item.getExtra2());
                        itemRepository.update(item.getCode(), restored);
                    }
                }
                view.info(OrderConstants.MSG_FAILED_TO_UPDATE_ORDER);
                return;
            }

            // Reduce inventory for new order lines
            for (OrderLine newLine : updated.getLines()) {
                ItemRecord item = itemRepository.findByCode(newLine.getItemCode());
                if (item != null) {
                    ItemRecord updatedItem = new ItemRecord(
                            item.getCode(),
                            item.getDescription(),
                            item.getPrice(),
                            item.getQuantity() - newLine.getQuantity(),
                            item.getType(),
                            item.getExtra1(),
                            item.getExtra2());
                    itemRepository.update(item.getCode(), updatedItem);
                }
            }

            view.info(OrderConstants.MSG_ORDER_UPDATED_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            view.info(OrderConstants.MSG_FAILED_TO_UPDATE_ORDER);
        }
    }

    private void handleDelete() {
        String code = view.promptOrderNumber(OrderConstants.PROMPT_ORDER_NUMBER_DELETE);
        try {
            boolean ok = orderRepository.delete(code);
            view.info(ok ? OrderConstants.MSG_ORDER_DELETED : OrderConstants.MSG_ORDER_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            view.info(OrderConstants.MSG_FAILED_TO_DELETE_ORDER);
        }
    }
}
