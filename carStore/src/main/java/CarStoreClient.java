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
            CreateUserRequest registerRequest = new CreateUserRequest(username, password, policy);
            Response response = Response.fromString(gateway.registerUser(registerRequest.toString()));
            if (response.responseCode() == RESPONSE_CODE.CREATED) {
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
            System.out.println("1. Search Car by Model");
            System.out.println("2. Search Car by Renavam");
            System.out.println("3. View All Cars");
            System.out.println("4. Buy Car");
            System.out.println("5. Delete Car");
            System.out.println("6. Logout");
            System.out.print("Choose an option (1-6): ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    searchCarByModel();
                    break;
                case 2:
                    searchCarByRenavam();
                    break;
                case 3:
                    viewAllCars();
                    break;
                case 4:
                    buyCar();
                    break;
                case 5:
                    deleteCar();
                    break;
                case 6:
                    logout();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }
    }

    private static void searchCarByModel() {
        System.out.print("Enter car model: ");
        String model = scanner.nextLine();

        try {
            Response response = Response.fromString(gateway.searchCarByModel(model));
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("Cars found:\n" + response.message());
            } else {
                System.out.println("No cars found: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during search: " + e.getMessage());
        }
    }

    private static void searchCarByRenavam() {
        System.out.print("Enter car Renavam: ");
        String renavam = scanner.nextLine();

        try {
            Response response = Response.fromString(gateway.searchCarByRenavam(renavam));
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("Car found:\n" + response.message());
            } else {
                System.out.println("No car found: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during search: " + e.getMessage());
        }
    }

    private static void viewAllCars() {
        try {
            Response response = Response.fromString(gateway.getAllCars());
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("All available cars:\n" + response.message());
            } else {
                System.out.println("Failed to retrieve cars: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred while retrieving cars: " + e.getMessage());
        }
    }

    private static void buyCar() {
        System.out.print("Enter car Renavam to buy: ");
        String renavam = scanner.nextLine();

        try {
            Response response = Response.fromString(gateway.buyCar(renavam));
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("Car purchased successfully.");
            } else {
                System.out.println("Failed to purchase car: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during purchase: " + e.getMessage());
        }
    }

    private static void deleteCar() {
        System.out.print("Enter car Renavam to delete: ");
        String renavam = scanner.nextLine();

        try {
            Response response = Response.fromString(gateway.deleteCar(renavam));
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("Car deleted successfully.");
            } else {
                System.out.println("Failed to delete car: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during deletion: " + e.getMessage());
        }
    }

    private static void logout() {
        isLoggedIn = false;
        currentUsername = null;
        System.out.println("Logged out successfully.");
    }
}
