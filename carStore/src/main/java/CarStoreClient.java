import model.CAR_CATEGORY;
import model.DTO.*;
import model.USER_POLICY;
import carStoreGatewayInterface.CarStoreGatewayInterface;
import model.Users;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CarStoreClient {

    private static CarStoreGatewayInterface gateway;
    private static Scanner scanner = new Scanner(System.in);
    private static boolean isLoggedIn = false;
    private static String currentUsername;
    private static USER_POLICY currentUserPolicy;

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
                String teste = gateway.searchUserByLogin(username);
                response = Response.fromString(gateway.searchUserByLogin(username));
                Users user = Users.fromString(response.message());
                currentUserPolicy = user.getPolicy();
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

        try {
            CreateUserRequest registerRequest = new CreateUserRequest(username, password, USER_POLICY.CUSTOMER);
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

    private static void userMenu() throws RemoteException {
        while (isLoggedIn) {
            switch (currentUserPolicy) {
                case ADMIN:
                    adminMenu();
                    break;
                case EMPLOYEE:
                    employeeMenu();
                    break;
                case CUSTOMER:
                    customerMenu();
                    break;
                default:
                    System.out.println("Unknown user policy.");
                    logout();
                    break;
            }
        }
    }

    private static void adminMenu() throws RemoteException {
        System.out.println("Admin Menu:");
        System.out.println("1. Create/Update/Delete Car");
        System.out.println("2. Create/Update/Delete User");
        System.out.println("3. View All Cars");
        System.out.println("4. Search Car by Model");
        System.out.println("5. Search Car by Renavam");
        System.out.println("6. Logout");
        System.out.print("Choose an option (1-6): ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1:
                crudCar();
                break;
            case 2:
                crudUser();
                break;
            case 3:
                viewAllCars();
                break;
            case 4:
                searchCarByModel();
                break;
            case 5:
                searchCarByRenavam();
                break;
            case 6:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
    }

    private static void viewAllCars() throws RemoteException {
        String responseString = gateway.getAllCars();
        Response response = Response.fromString(responseString);
        System.out.println(response.message());
    }

    private static void employeeMenu() throws RemoteException {
        System.out.println("Employee Menu:");
        System.out.println("1. Create/Update/Delete Car");
        System.out.println("2. View All Cars");
        System.out.println("3. Search Car by Model");
        System.out.println("4. Search Car by Renavam");
        System.out.println("5. Logout");
        System.out.print("Choose an option (1-5): ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1:
                crudCar();
                break;
            case 2:
                viewAllCars();
                break;
            case 3:
                searchCarByModel();
                break;
            case 4:
                searchCarByRenavam();
                break;
            case 5:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
    }

    private static void customerMenu() throws RemoteException {
        System.out.println("Customer Menu:");
        System.out.println("1. View All Cars");
        System.out.println("2. Search Car by Model");
        System.out.println("3. Search Car by Renavam");
        System.out.println("4. Buy Car");
        System.out.println("5. Logout");
        System.out.print("Choose an option (1-5): ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1:
                viewAllCars();
                break;
            case 2:
                searchCarByModel();
                break;
            case 3:
                searchCarByRenavam();
                break;
            case 4:
                buyCar();
                break;
            case 5:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
    }

    private static void crudCar() {
        System.out.println("CRUD Car Menu:");
        System.out.println("1. Create Car");
        System.out.println("2. Update Car");
        System.out.println("3. Delete Car");
        System.out.print("Choose an option (1-3): ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1:
                createCar();
                break;
            case 2:
                updateCar();
                break;
            case 3:
                deleteCar();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
    }

    private static void createCar() {
        System.out.print("Enter car model: ");
        String model = scanner.nextLine();
        System.out.print("Enter new Category (ECONOMIC, EXECUTIVE, INTERMEDIARY: ");
        String newCategory = scanner.nextLine().toUpperCase();
        System.out.print("Enter car year: ");
        int year = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter car RENAVAM: ");
        String renavam = scanner.nextLine();
        System.out.print("Enter car price: ");
        double price = Double.parseDouble(scanner.nextLine());

        try {
            CreateCarRequest request = new CreateCarRequest(model, CAR_CATEGORY.valueOf(newCategory),year, price, renavam);
            Response response = Response.fromString(gateway.registerCar(request.toString()));
            if (response.responseCode() == RESPONSE_CODE.CREATED) {
                System.out.println("Car created successfully.");
            } else {
                System.out.println("Car creation failed: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during car creation: " + e.getMessage());
        }
    }

    private static void updateCar() {
        System.out.print("Enter RENAVAM of the car to update: ");
        String renavam = scanner.nextLine();
        System.out.print("Enter new model: ");
        String newModel = scanner.nextLine();
        System.out.print("Enter new Category (ECONOMIC, EXECUTIVE, INTERMEDIARY: ");
        String newCategory = scanner.nextLine().toUpperCase();
        System.out.print("Enter new year: ");
        int newYear = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter new price: ");
        double newPrice = Double.parseDouble(scanner.nextLine());

        try {
            UpdateCarRequest request = new UpdateCarRequest(renavam, newModel, CAR_CATEGORY.valueOf(newCategory),newYear, newPrice);
            Response response = Response.fromString(gateway.updateCar(request.toString()));
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("Car updated successfully.");
            } else {
                System.out.println("Car update failed: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during car update: " + e.getMessage());
        }
    }

    private static void deleteCar() {
        System.out.print("Enter RENAVAM of the car to delete: ");
        String renavam = scanner.nextLine();

        try {
            Response response = Response.fromString(gateway.deleteCar(renavam));
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("Car deleted successfully.");
            } else {
                System.out.println("Car deletion failed: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during car deletion: " + e.getMessage());
        }
    }

    private static void crudUser() {
        System.out.println("CRUD User Menu:");
        System.out.println("1. Create User");
        System.out.println("2. Update Username");
        System.out.println("3. Update Password");
        System.out.println("4. Delete User");
        System.out.print("Choose an option (1-4): ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1:
                createUser();
                break;
            case 2:
                updateUsername();
                break;
            case 3:
                updatePassword();
                break;
            case 4:
                deleteUser();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
    }

    private static void createUser() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter new password: ");
        String password = scanner.nextLine();
        System.out.print("Enter user policy (ADMIN, EMPLOYEE, CUSTOMER): ");
        USER_POLICY policy = USER_POLICY.valueOf(scanner.nextLine().toUpperCase());

        try {
            CreateUserRequest request = new CreateUserRequest(username, password, policy);
            Response response = Response.fromString(gateway.registerUser(request.toString()));
            if (response.responseCode() == RESPONSE_CODE.CREATED) {
                System.out.println("User created successfully.");
            } else {
                System.out.println("User creation failed: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during user creation: " + e.getMessage());
        }
    }

    private static void updateUsername() {
        System.out.print("Enter current username: ");
        String oldUsername = scanner.nextLine();
        System.out.print("Enter new username: ");
        String newUsername = scanner.nextLine();

        try {
            UpdateUsernameRequest request = new UpdateUsernameRequest(oldUsername, newUsername);
            Response response = Response.fromString(gateway.updateUsername(request.toString()));
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("Username updated successfully.");
            } else {
                System.out.println("Username update failed: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during username update: " + e.getMessage());
        }
    }

    private static void updatePassword() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        try {
            UpdatePasswordRequest request = new UpdatePasswordRequest(username, newPassword);
            Response response = Response.fromString(gateway.updatePassword(request.toString()));
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("Password updated successfully.");
            } else {
                System.out.println("Password update failed: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during password update: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        System.out.print("Enter username to delete: ");
        String username = scanner.nextLine();

        try {
            Response response = Response.fromString(gateway.deleteUser(username));
            if (response.responseCode() == RESPONSE_CODE.OK) {
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("User deletion failed: " + response.message());
            }
        } catch (Exception e) {
            System.out.println("An error occurred during user deletion: " + e.getMessage());
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

    public static Response fromString(String str) {
        // Regex ajustada para capturar o campo message que contém strings complexas e múltiplas linhas
        Pattern pattern = Pattern.compile("Response\\{responseCode=([A-Z_]+), message='(?s)(.*?)'\\}");
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            RESPONSE_CODE responseCode = RESPONSE_CODE.valueOf(matcher.group(1));
            String message = matcher.group(2);
            return new Response(responseCode, message);
        }

        throw new IllegalArgumentException("Invalid string format");
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
            System.out.println("An error occurred during the purchase: " + e.getMessage());
        }
    }

    private static void logout() {
        System.out.println("Logging out...");
        isLoggedIn = false;
        currentUsername = null;
        currentUserPolicy = null;
    }
}
