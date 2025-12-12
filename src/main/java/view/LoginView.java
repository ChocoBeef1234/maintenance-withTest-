package main.java.view;
import java.util.Scanner;

public class LoginView {
    private final Scanner scanner = new Scanner(System.in);

    public String promptUsername() {
        System.out.print("\nEnter your username to login: ");
        return scanner.nextLine();
    }

    public String promptPassword() {
        System.out.print("\nEnter your password: ");
        return scanner.nextLine();
    }

    public void showLoginSuccess() {
        System.out.println("\n\n*Login successful!");
    }

    public void showLoginFailure() {
        System.out.println("\n*Invalid username or password.");
    }

    public void showMaxAttemptsReached() {
        System.out.println("\n*Too many failed login attempts. Exiting...");
    }
}

