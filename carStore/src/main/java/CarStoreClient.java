import model.DTO.CreateUserRequest;
import model.DTO.Login;
import model.DTO.RESPONSE_CODE;
import model.DTO.Response;
import model.USER_POLICY;
import carStoreGatewayInterface.CarStoreGatewayInterface;

import java.rmi.Naming;
import java.util.Scanner;
import java.util.UUID;

public class CarStoreClient {

    private static CarStoreGatewayInterface gateway;
    private static Scanner scanner = new Scanner(System.in);
    private static boolean isLoggedIn = false;
    private static String currentUsername;

    public static void main(String[] args) {
        while (true) {
            try {
                connectToGateway();
                if (isLoggedIn) {
                    userMenu();
                } else {
                    mainMenu();
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void connectToGateway() {
        while (gateway == null) {
            try {
                System.out.print("Enter the Gateway address (e.g., 192.168.1.101:1099): ");
                String gatewayAddress = scanner.nextLine();
                String gatewayUrl = "rmi://" + gatewayAddress + "/CarStoreGateway";
                gateway = (CarStoreGatewayInterface) Naming.lookup(gatewayUrl);
                System.out.println("Connected to Gateway successfully.");
            } catch (Exception e) {
                System.out.println("Failed to connect to Gateway. Please try again.");
                e.printStackTrace();
            }
        }
    }

    private static void mainMenu() {
        System.out.println("Main Menu:");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.print("Choose an option (1 or 2): ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
    }

    private static void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            Login loginRequest = new Login(username, password);
            Response response = Response.fromString(gateway.authenticate(loginRequest.toString()));
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("Login successful.");
                currentUsername = username;
                isLoggedIn = true;
                userMenu();
            } else {
                System.out.println("Login failed: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during login: " + e.getMessage());
        }
    }

    private static void register() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter new password: ");
        String password = scanner.nextLine();
        System.out.print("Enter user policy (e.g., EMPLOYEE): ");
        USER_POLICY policy = USER_POLICY.valueOf(scanner.nextLine().toUpperCase());

        try {
            CreateUserRequest registerRequest = new CreateUserRequest(username, password, USER_POLICY.CUSTOMER);
            Response response = Response.fromString(gateway.registerUser(registerRequest.toString()));
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("Registration successful.");
            } else {
                System.out.println("Registration failed: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during registration: " + e.getMessage());
        }
    }

    private static void userMenu() {
        while (isLoggedIn) {
            System.out.println("User Menu:");
            System.out.println("1. Logout");
            System.out.print("Choose an option (1): ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    logout();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }
    }

    private static void logout() {
        isLoggedIn = false;
        currentUsername = null;
        System.out.println("Logged out successfully.");
    }
}
