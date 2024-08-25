import discoveryInterface.DiscoveryServerInterface;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class CarStoreUsersDBDiscoveryServer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // prompt user to input the port to start the server
                System.out.print("Enter the port to start the discovery server: ");
                int port = Integer.parseInt(scanner.nextLine());

                // rmi url
                String name = "rmi://localhost:" + port + "/carStoreUsersDBDiscovery";

                // start the discovery server
                DiscoveryServerInterface discoveryServer = new CarStoreUsersDBDiscoveryServerImpl();
                LocateRegistry.createRegistry(port);
                Naming.rebind(name, discoveryServer);

                // display the IP and port in the console
                String ip = InetAddress.getLocalHost().getHostAddress();
                System.out.println("--------------------------------------------");
                System.out.println("""

                          ___ ___ _______ _______ _______        ______   _______         ______   ___ _______ _______ _______ ___ ___ _______ _______ ___ ___\s
                         |   Y   |   _   |   _   |   _   \\      |   _  \\ |   _   \\       |   _  \\ |   |   _   |   _   |   _   |   Y   |   _   |   _   |   Y   |
                         |.  |   |   1___|.  1___|.  l   /      |.  |   \\|.  1   /       |.  |   \\|.  |   1___|.  1___|.  |   |.  |   |.  1___|.  l   |   1   |
                         |.  |   |____   |.  __)_|.  _   1______|.  |    |.  _   \\ ______|.  |    |.  |____   |.  |___|.  |   |.  |   |.  __)_|.  _   1\\_   _/\s
                         |:  1   |:  1   |:  1   |:  |   |______|:  1    |:  1    |______|:  1    |:  |:  1   |:  1   |:  1   |:  1   |:  1   |:  |   | |:  | \s
                         |::.. . |::.. . |::.. . |::.|:. |      |::.. . /|::.. .  /      |::.. . /|::.|::.. . |::.. . |::.. . |\\:.. ./|::.. . |::.|:. | |::.| \s
                         `-------`-------`-------`--- ---'      `------' `-------'       `------' `---`-------`-------`-------' `---' `-------`--- ---' `---' \s
                                                                                                                                                              \s
                        """);
                System.out.println("Discovery Server started at " + ip + ":" + port);
                System.out.println("Waiting for registration requests...");
                System.out.println("--------------------------------------------");

                // exit loop after successfully starting
                break;

            } catch (Exception e) {
                System.out.println("Error starting the server on the provided port. Try again.");
                e.printStackTrace();
            }
        }
        scanner.close();
    }
}