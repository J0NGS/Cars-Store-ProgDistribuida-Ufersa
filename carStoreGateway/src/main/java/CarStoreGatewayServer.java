import carStoreGatewayInterface.CarStoreGatewayInterface;
import model.DTO.CreateUserRequest;
import model.USER_POLICY;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CarStoreGatewayServer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // Prompt the user to enter the port to start the server
                System.out.print("Enter the port to start the server: ");
                int port = Integer.parseInt(scanner.nextLine());

                // Prompt the user to enter the Discovery Server address for User Service
                System.out.print("Enter the address of the -USERS- Service Discovery Server (e.g., 192.168.1.101): ");
                String userServiceDiscoveryAddress = scanner.nextLine();
                System.out.print("Enter the address of the -CARS- Service Discovery Server (e.g., 192.168.1.101): ");
                String carsServiceDiscoveryAddress = scanner.nextLine();

                userServiceDiscoveryAddress = "rmi://" + userServiceDiscoveryAddress + "/carStoreUsersServiceDiscovery";
                carsServiceDiscoveryAddress = "rmi://" + carsServiceDiscoveryAddress + "/carStoreCarsServiceDiscovery";
                // Set the RMI name for the service
                String serviceName = "rmi://localhost:" + port + "/CarStoreGateway";

                System.out.println("""

                          _______ _______ _______ _______ ___ ___ _______ ___ ___\s
                         |   _   |   _   |       |   _   |   Y   |   _   |   Y   |
                         |.  |___|.  1   |.|   | |.  1___|.  |   |.  1   |   1   |
                         |.  |   |.  _   `-|.  |-|.  __)_|. / \\  |.  _   |\\_   _/\s
                         |:  1   |:  |   | |:  | |:  1   |:      |:  |   | |:  | \s
                         |::.. . |::.|:. | |::.| |::.. . |::.|:. |::.|:. | |::.| \s
                         `-------`--- ---' `---' `-------`--- ---`--- ---' `---' \s
                                                                                 \s
                        """);

                // Create the RMI registry on the specified port, if necessary
                Registry registry;
                try {
                    registry = LocateRegistry.getRegistry(port);
                    registry.list();  // Check if the registry already exists
                } catch (Exception e) {
                    registry = LocateRegistry.createRegistry(port);  // Create the registry if it does not exist
                }

                // Gateway implementation with the provided Discovery Server address
                CarStoreGatewayInterface gateway = new CarStoreGatewayImpl(userServiceDiscoveryAddress, carsServiceDiscoveryAddress);

                // Register and bind the gateway to the RMI name
                Naming.rebind(serviceName, gateway);

                // Display the IP and port in the console
                String ip = InetAddress.getLocalHost().getHostAddress();
                String serverAddress = ip + ":" + port;
                System.out.println("--------------------------------------------");
                System.out.println("Gateway started at " + serverAddress);
                System.out.println("Waiting for requests...");
                System.out.println("--------------------------------------------");
                System.out.println(gateway.registerUser(new CreateUserRequest("admin", "admin", USER_POLICY.ADMIN).toString()));
                break;
            } catch (Exception e) {
                System.out.println("Error starting the server on the provided port. Please try again.");
                e.printStackTrace();
            }
        }
    }
}
