package main.java.controller;
import main.java.view.MainMenuView;

public class MainController {
    private final MainMenuView mainMenuView = new MainMenuView();
    private final StaffController staffController = new StaffController();
    private final ItemController itemController = new ItemController();
    private final TransactionController transactionController = new TransactionController();
    private final OrderController orderController = new OrderController(transactionController);

    public void run() {
        boolean exit = false;
        while (!exit) {
            int selection = mainMenuView.menu();
            switch (selection) {
                case 1:
                    staffController.run();
                    break;
                case 2:
                    itemController.run();
                    break;
                case 3:
                    orderController.run();
                    break;
                case 4:
                    transactionController.run();
                    break;
                case 5:
                    exit = true;
                    break;
                default:
                    System.out.println("\nInvalid input.");
            }
        }
    }
}

