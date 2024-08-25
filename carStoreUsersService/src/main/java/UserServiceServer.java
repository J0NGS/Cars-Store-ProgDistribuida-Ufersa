import discoveryInterface.DiscoveryServerInterface;
import usersServiceProtocols.UsersServiceProtocolInterface;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class UserServiceServer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // Prompt the user to enter the port to start the server
                System.out.print("Enter the port to start the server: ");
                int port = Integer.parseInt(scanner.nextLine());

                // Prompt the user to enter the Discovery Server address
                System.out.print("Enter the Discovery Server address of the DB (e.g., 192.168.1.100): ");
                String discoveryServerAddressDB = scanner.nextLine();
                System.out.print("Enter the Discovery Server address of the Service (e.g., 192.168.1.100): ");
                String discoveryServerAddressService = scanner.nextLine();

                // Set the RMI name for the service
                String serviceName = "rmi://localhost:" + port + "/CarStoreUsersService";

                System.out.println("""

                          ___ ___ _______ _______ _______ _______     _______ _______ _______ ___ ___ ___ _______ _______\s
                         |   Y   |   _   |   _   |   _   |   _   |   |   _   |   _   |   _   |   Y   |   |   _   |   _   |
                         |.  |   |   1___|.  1___|.  l   |   1___|   |   1___|.  1___|.  l   |.  |   |.  |.  1___|.  1___|
                         |.  |   |____   |.  __)_|.  _   |____   |   |____   |.  __)_|.  _   |.  |   |.  |.  |___|.  __)_\s
                         |:  1   |:  1   |:  1   |:  |   |:  1   |   |:  1   |:  1   |:  |   |:  1   |:  |:  1   |:  1   |
                         |::.. . |::.. . |::.. . |::.|:. |::.. . |   |::.. . |::.. . |::.|:. |\\:.. ./|::.|::.. . |::.. . |
                         `-------`-------`-------`--- ---`-------'   `-------`-------`--- ---' `---' `---`-------`-------'
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

                // Protocol implementation
                UsersServiceProtocolInterface protocol = new ProtocolUsersServiceImpl(discoveryServerAddressDB);

                // Register and bind the protocol to the RMI name
                Naming.rebind(serviceName, protocol);

                // Display the IP and port in the console
                String ip = InetAddress.getLocalHost().getHostAddress();
                String serverAddress = ip + ":" + port;
                String finalServiceName = "rmi://" + serverAddress + "/CarStoreUsersService";
                System.out.println("--------------------------------------------");
                System.out.println("Server started at " + serverAddress);
                System.out.println("Waiting for requests...");
                System.out.println("--------------------------------------------");

                // Register with the Discovery Server in a new thread
                new Thread(() -> {
                    try {
                        String discoveryName = "rmi://" + discoveryServerAddressService + "/carStoreUsersServiceDiscovery";
                        DiscoveryServerInterface discoveryServer = (DiscoveryServerInterface) Naming.lookup(discoveryName);
                        discoveryServer.registerInstance(serverAddress, finalServiceName);
                        System.out.println("Successfully registered with Discovery Server at " + discoveryServerAddressService);
                    } catch (Exception e) {
                        System.out.println("Failed to register with Discovery Server.");
                        e.printStackTrace();
                    }
                }).start();

                // Exit the loop after successfully starting
                break;

            } catch (Exception e) {
                System.out.println("Error starting the server on the provided port. Please try again.");
                e.printStackTrace();
            }
        }
        scanner.close();
    }
}
