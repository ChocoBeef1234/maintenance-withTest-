package main.java.controller;
import main.java.model.TransactionRecord;
import main.java.model.OrderRecord;
import main.java.repository.TransactionRepository;
import main.java.repository.OrderRepository;
import main.java.view.TransactionView;

public class TransactionController {
    private final TransactionRepository repository;
    private final TransactionView view;
    public TransactionController() {
        this.repository = new TransactionRepository();
        this.view = new TransactionView();
        new OrderRepository();
    }

    public void payForOrder(OrderRecord order) {
        double total = order.getTotal();
        double discountPct = calcDiscount(total);
        double discountAmt = total * (discountPct / 100.0);
        double taxPct = 6.0;
        double finalPrice = (total - discountAmt) * ((taxPct + 100.0) / 100.0);

        view.showSummary(order.getOrderNumber(), total, discountPct, discountAmt, taxPct, finalPrice);
        TransactionRecord.Method method = view.promptMethod();
        TransactionRecord record = null;

        switch (method) {
            case CASH:
                double pay = view.promptCash(finalPrice);
                double change = pay - finalPrice;
                record = new TransactionRecord(order.getOrderNumber(), total, discountPct, discountAmt, taxPct, finalPrice, method,
                        String.format("%.2f", pay), String.format("%.2f", change));
                break;
            case BANK:
                String bank = view.promptBankName();
                String account = view.promptAccount();
                record = new TransactionRecord(order.getOrderNumber(), total, discountPct, discountAmt, taxPct, finalPrice, method,
                        bank, account);
                break;
            case EWALLET:
                String name = view.promptName();
                String phone = view.promptPhone();
                record = new TransactionRecord(order.getOrderNumber(), total, discountPct, discountAmt, taxPct, finalPrice, method,
                        name, phone);
                break;
        }

        try {
            boolean ok = repository.add(record);
            view.info(ok ? "\nPayment saved." : "\nFailed to save payment (file missing?).");
        } catch (Exception e) {
            e.printStackTrace();
            view.info("\nFailed to save payment.");
        }
    }

    public void run() {
        boolean back = false;
        while (!back) {
            int sel = view.menu();
            switch (sel) {
                case 1:
                    handleSearch();
                    break;
                case 2:
                    handleDelete();
                    break;
                case 3:
                    back = true;
                    break;
                default:
                    view.info("\nInvalid input.");
            }
        }
    }

    private void handleSearch() {
        try {
            String code = view.promptOrderCode("Enter Order Code (blank for all): ");
            if (code.isEmpty()) {
                for (TransactionRecord r : repository.findAll()) view.show(r);
            } else {
                TransactionRecord r = repository.findByOrder(code);
                if (r != null) view.show(r);
                else view.info("\nTransaction not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.info("\nFailed to search transactions.");
        }
    }

    private void handleDelete() {
        try {
            String code = view.promptOrderCode("Enter Order Code to delete transaction: ");
            boolean ok = repository.delete(code);
            view.info(ok ? "\nTransaction deleted." : "\nTransaction not found.");
        } catch (Exception e) {
            e.printStackTrace();
            view.info("\nFailed to delete transaction.");
        }
    }

    private double calcDiscount(double total) {
        if (total >= 150.0) return 10.0;
        if (total >= 100.0) return 5.0;
        return 0.0;
    }
}

