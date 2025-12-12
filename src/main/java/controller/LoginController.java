package main.java.controller;
import java.io.IOException;
import main.java.model.Staff;
import main.java.repository.StaffRepository;
import main.java.view.LoginView;

public class LoginController {
    private final StaffRepository repository;
    private final LoginView view;
    private static final int MAX_ATTEMPTS = 3;

    public LoginController() {
        this.repository = new StaffRepository();
        this.view = new LoginView();
    }

    public boolean validateLogin() {
        try {
            boolean loggedIn = false;
            int attempts = 0;

            while (!loggedIn) {
                if (attempts >= MAX_ATTEMPTS) {
                    view.showMaxAttemptsReached();
                    return false;
                }

                String loginCode = view.promptUsername();
                String password = view.promptPassword();

                Staff staff = repository.validateCredentials(loginCode, password);
                if (staff != null) {
                    loggedIn = true;
                    view.showLoginSuccess();
                    return true;
                } else {
                    view.showLoginFailure();
                    attempts++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}

