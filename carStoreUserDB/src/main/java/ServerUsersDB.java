import BD.ProtocolUsersDBImpl;
import discoveryInterface.DiscoveryServerInterface;
import userBDProtocols.UserBDProtocolInterface;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ServerUsersDB {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // prompt the user for the port to start the server
                System.out.print("enter the port to start the server: ");
                int port = Integer.parseInt(scanner.nextLine());

                // prompt the user for the discovery server address
                System.out.print("enter the discovery server address (e.g., 192.168.1.100): ");
                String discoveryServerAddress = scanner.nextLine();
                // protocol implementation
                String name = "rmi://localhost:" + port + "/CarStoreUsersDb";

                System.out.println("\r\n" +
                        " __    __       _______. _______ .______          _______.   .______    _______  \r\n" +
                        "|  |  |  |     /       ||   ____||   _  \\        /       |   |   _  \\  |       \\ \r\n" +
                        "|  |  |  |    |   (----|  |__   |  |_)  |      |   (----   |  |_)  | |  .--.  |\r\n" +
                        "|  |  |  |     \\   \\    |   __|  |      /        \\   \\       |   _  <  |  |  |  |\r\n" +
                        "|  --'  | .----)   |   |  |____ |  |\\  \\----.----)   |      |  |_)  | |  '--'  |\r\n" +
                        " \\______/  |_______/    |_______|| _| ._____|_______/       |______/  |_______/ \r\n" +
                        "                                                                                 \r\n" +
                        "");

                // create the registry on the specified port if needed
                Registry registry;
                try {
                    registry = LocateRegistry.getRegistry(port);
                    registry.list();  // check if the registry already exists
                } catch (Exception e) {
                    registry = LocateRegistry.createRegistry(port);  // create the registry if it does not exist
                }

                // protocol implementation
                UserBDProtocolInterface protocol = new ProtocolUsersDBImpl();

                // register and bind the protocol
                Naming.rebind(name, protocol);

                // display the ip and port on the console
                String ip = InetAddress.getLocalHost().getHostAddress();
                String serverAdress = ip + ":" + port;
                String finalName = "rmi://" + serverAdress + "/CarStoreUsersDb";
                System.out.println("--------------------------------------------");
                System.out.println("server started at " + serverAdress);
                System.out.println("waiting for requests...");
                System.out.println("--------------------------------------------");

                // register with the discovery server in a new thread
                new Thread(() -> {
                    try {
                        String discoveryName = "rmi://" + discoveryServerAddress + "/carStoreUsersDBDiscovery";
                        DiscoveryServerInterface discoveryServer = (DiscoveryServerInterface) Naming.lookup(discoveryName);
                        discoveryServer.registerInstance(serverAdress, finalName);
                        System.out.println("successfully registered with the discovery server at " + discoveryServerAddress);
                    } catch (Exception e) {
                        System.out.println("failed to register with the discovery server.");
                        e.printStackTrace();
                    }
                }).start();

                // exit the loop after successful start
                break;

            } catch (Exception e) {
                System.out.println("error starting the server on the provided port. please try again.");
                e.printStackTrace();
            }
        }
        scanner.close();
    }
}