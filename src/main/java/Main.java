/**
 * Application Entry Point
 * 
 * In MVC architecture, the main() method should be in a separate entry point class
 * that is independent of Model, View, and Controller layers.
 * 
 * This class is responsible only for:
 * - Application initialization
 * - Starting the application flow
 * - Handling application-level concerns (like login)
 */
package main.java;

import main.java.controller.LoginController;
import main.java.controller.MainController;

public class Main {
    
    public static void main(String[] args) {
        // Handle authentication
        LoginController loginController = new LoginController();
        if (!loginController.validateLogin()) {
            System.exit(0);
        }
        
        // Start main application
        MainController mainController = new MainController();
        mainController.run();
    }
}

