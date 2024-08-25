import discoveryInterface.DiscoveryServerInterface;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class CarStoreUsersServiceDiscoveryServer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // Solicitar ao usuário a porta para iniciar o servidor
                System.out.print("Enter the port to start the UsersService discovery server: ");
                int port = Integer.parseInt(scanner.nextLine());

                // URL RMI
                String name = "rmi://localhost:" + port + "/carStoreUsersServiceDiscovery";

                // Iniciar o discovery server
                DiscoveryServerInterface discoveryServer = new UserServiceDiscoveryServerImpl();
                LocateRegistry.createRegistry(port);
                Naming.rebind(name, discoveryServer);

                // Exibir o IP e a porta no console
                String ip = InetAddress.getLocalHost().getHostAddress();
                System.out.println("--------------------------------------------");
                System.out.println("""

                          ___ ___ _______ _______ _______        _______ ___ ___ _______        ______   ___ _______ _______ _______ ___ ___ _______ _______ ___ ___\s
                         |   Y   |   _   |   _   |   _   \\      |   _   |   Y   |   _   |      |   _  \\ |   |   _   |   _   |   _   |   Y   |   _   |   _   |   Y   |
                         |.  |   |   1___|.  1___|.  l   /      |   1___|.  |   |.  1___|      |.  |   \\|.  |   1___|.  1___|.  |   |.  |   |.  1___|.  l   |   1   |
                         |.  |   |____   |.  __)_|.  _   1______|____   |.  |   |.  |___ ______|.  |    |.  |____   |.  |___|.  |   |.  |   |.  __)_|.  _   1\\_   _/\s
                         |:  1   |:  1   |:  1   |:  |   |______|:  1   |:  1   |:  1   |______|:  1    |:  |:  1   |:  1   |:  1   |:  1   |:  1   |:  |   | |:  | \s
                         |::.. . |::.. . |::.. . |::.|:. |      |::.. . |\\:.. ./|::.. . |      |::.. . /|::.|::.. . |::.. . |::.. . |\\:.. ./|::.. . |::.|:. | |::.| \s
                         `-------`-------`-------`--- ---'      `-------' `---' `-------'      `------' `---`-------`-------`-------' `---' `-------`--- ---' `---' \s
                                                                                                                                                                    \s
                        """);
                System.out.println("UsersService Discovery Server started at " + ip + ":" + port);
                System.out.println("Waiting for registration requests...");
                System.out.println("--------------------------------------------");


                // Sair do loop após iniciar com sucesso
                break;

            } catch (Exception e) {
                System.out.println("Error starting the server on the provided port. Try again.");
                e.printStackTrace();
            }
        }
        scanner.close();
    }
}
