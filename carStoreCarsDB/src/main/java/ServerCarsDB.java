import BD.ProtocolCarsDBImpl;
import discoveryInterface.DiscoveryServerInterface;
import carsDBProtocols.CarsDBProtocolInterface;
import model.CAR_CATEGORY;
import model.Cars;
import model.DTO.CreateCarRequest;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ServerCarsDB {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // Prompt the user for the port to start the server
                System.out.print("Enter the port to start the server: ");
                int port = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter the wigth to the server: ");
                int weight = Integer.parseInt(scanner.nextLine());

                // Prompt the user for the discovery server address
                System.out.print("Enter the discovery server address (e.g., 192.168.1.100): ");
                String discoveryServerAddress = scanner.nextLine();

                // Protocol implementation
                String name = "rmi://localhost:" + port + "/CarStoreCarsDb";

                System.out.println("""

                         _   _ _____ ___________  _____  ____________\s
                        | | | /  ___|  ___| ___ \\/  ___| |  _  \\ ___ \\
                        | | | \\ `--.| |__ | |_/ /\\ `--.  | | | | |_/ /
                        | | | |`--. \\  __||    /  `--. \\ | | | | ___ \\
                        | |_| /\\__/ / |___| |\\ \\ /\\__/ / | |/ /| |_/ /
                         \\___/\\____/\\____/\\_| \\_|\\____/  |___/ \\____/\s
                                                                     \s
                                                                     \s
                        """);

                // Create the registry on the specified port if needed
                Registry registry;
                try {
                    registry = LocateRegistry.getRegistry(port);
                    registry.list();  // Check if the registry already exists
                } catch (Exception e) {
                    registry = LocateRegistry.createRegistry(port);  // Create the registry if it does not exist
                }

                // Protocol implementation
                CarsDBProtocolInterface protocol = new ProtocolCarsDBImpl();

                // Register and bind the protocol
                Naming.rebind(name, protocol);

                // Display the IP and port on the console
                String ip = InetAddress.getLocalHost().getHostAddress();
                String serverAddress = ip + ":" + port;
                String finalName = "rmi://" + serverAddress + "/CarStoreCarsDb";
                System.out.println("--------------------------------------------");
                System.out.println("Server started at " + serverAddress);
                System.out.println("Waiting for requests...");
                System.out.println("--------------------------------------------");

                // Register with the discovery server in a new thread
                new Thread(() -> {
                    try {
                        String discoveryName = "rmi://" + discoveryServerAddress + "/carStoreCarsDBDiscovery";
                        DiscoveryServerInterface discoveryServer = (DiscoveryServerInterface) Naming.lookup(discoveryName);
                        discoveryServer.registerInstance(serverAddress, finalName, weight);
                        System.out.println("Successfully registered with the discovery server at " + discoveryServerAddress);
                    } catch (Exception e) {
                        System.out.println("Failed to register with the discovery server.");
                        e.printStackTrace();
                    }
                }).start();
                // Exit the loop after successful start
                break;

            } catch (Exception e) {
                System.out.println("Error starting the server on the provided port. Please try again.");
                e.printStackTrace();
            }
        }
        scanner.close();
    }
}
