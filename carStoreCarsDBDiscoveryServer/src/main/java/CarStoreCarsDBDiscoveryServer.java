import discoveryInterface.DiscoveryServerInterface;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class CarStoreCarsDBDiscoveryServer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // prompt user to input the port to start the server
                System.out.print("Enter the port to start the discovery server: ");
                int port = Integer.parseInt(scanner.nextLine());

                // rmi url
                String name = "rmi://localhost:" + port + "/carStoreCarsDBDiscovery";

                // start the discovery server
                DiscoveryServerInterface discoveryServer = new CarStoreCarsDBDiscoveryServerImpl();
                LocateRegistry.createRegistry(port);
                Naming.rebind(name, discoveryServer);

                // display the IP and port in the console
                String ip = InetAddress.getLocalHost().getHostAddress();
                System.out.println("--------------------------------------------");
                System.out.println("""

                         _____   ___  ______  _____       ____________       ______ _____ _____ _____ _____  _   _ _____________   __
                        /  __ \\ / _ \\ | ___ \\/  ___|      |  _  \\ ___ \\      |  _  \\_   _/  ___/  __ \\  _  || | | |  ___| ___ \\ \\ / /
                        | /  \\// /_\\ \\| |_/ /\\ `--. ______| | | | |_/ /______| | | | | | \\ `--.| /  \\/ | | || | | | |__ | |_/ /\\ V /\s
                        | |    |  _  ||    /  `--. \\______| | | | ___ \\______| | | | | |  `--. \\ |   | | | || | | |  __||    /  \\ / \s
                        | \\__/\\| | | || |\\ \\ /\\__/ /      | |/ /| |_/ /      | |/ / _| |_/\\__/ / \\__/\\ \\_/ /\\ \\_/ / |___| |\\ \\  | | \s
                         \\____/\\_| |_/\\_| \\_|\\____/       |___/ \\____/       |___/  \\___/\\____/ \\____/\\___/  \\___/\\____/\\_| \\_| \\_/ \s
                                                                                                                                    \s
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
