import discoveryInterface.DiscoveryServerInterface;
import carsServiceProtocols.CarsServiceProtocolInterface;
import model.CAR_CATEGORY;
import model.DTO.CreateCarRequest;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CarsServiceServer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // Prompt the user to enter the port to start the server
                System.out.print("Enter the port to start the server: ");
                int port = Integer.parseInt(scanner.nextLine());

                // Prompt the user to enter the Discovery Server address
                System.out.print("Enter the Discovery Server address of the DATABASE (e.g., 192.168.1.100): ");
                String discoveryServerAddressDB = scanner.nextLine();
                System.out.print("Enter the Discovery Server address of the SERVICE (e.g., 192.168.1.100): ");
                String discoveryServerAddressService = scanner.nextLine();

                // Set the RMI name for the service
                String serviceName = "rmi://localhost:" + port + "/CarStoreCarsService";

                System.out.println("""

                         _____   ___  ______  _____   _____ ___________ _   _ _____ _____  _____\s
                        /  __ \\ / _ \\ | ___ \\/  ___| /  ___|  ___| ___ \\ | | |_   _/  __ \\|  ___|
                        | /  \\// /_\\ \\| |_/ /\\ `--.  \\ `--.| |__ | |_/ / | | | | | | /  \\/| |__ \s
                        | |    |  _  ||    /  `--. \\  `--. \\  __||    /| | | | | | | |    |  __|\s
                        | \\__/\\| | | || |\\ \\ /\\__/ / /\\__/ / |___| |\\ \\\\ \\_/ /_| |_| \\__/\\| |___\s
                         \\____/\\_| |_/\\_| \\_|\\____/  \\____/\\____/\\_| \\_|\\___/ \\___/ \\____/\\____/\s
                                                                                                \s
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
                CarsServiceProtocolInterface protocol = new ProtocolCarsServiceImpl(discoveryServerAddressDB);

                // Register and bind the protocol to the RMI name
                Naming.rebind(serviceName, protocol);

                // Display the IP and port in the console
                String ip = InetAddress.getLocalHost().getHostAddress();
                String serverAddress = ip + ":" + port;
                String finalServiceName = "rmi://" + serverAddress + "/CarStoreCarsService";
                System.out.println("--------------------------------------------");
                System.out.println("Server started at " + serverAddress);
                System.out.println("Waiting for requests...");
                System.out.println("--------------------------------------------");
                // Register with the Discovery Server in a new thread
                new Thread(() -> {
                    try {
                        String discoveryName = "rmi://" + discoveryServerAddressService + "/carStoreCarsServiceDiscovery";
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
