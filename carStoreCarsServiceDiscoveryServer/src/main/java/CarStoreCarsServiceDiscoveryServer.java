import discoveryInterface.DiscoveryServerInterface;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class CarStoreCarsServiceDiscoveryServer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // Solicitar ao usuário a porta para iniciar o servidor
                System.out.print("Enter the port to start the CarsService discovery server: ");
                int port = Integer.parseInt(scanner.nextLine());

                // URL RMI
                String name = "rmi://localhost:" + port + "/carStoreCarsServiceDiscovery";

                // Iniciar o discovery server
                DiscoveryServerInterface discoveryServer = new CarsServiceDiscoveryServerImpl();
                LocateRegistry.createRegistry(port);
                Naming.rebind(name, discoveryServer);

                // Exibir o IP e a porta no console
                String ip = InetAddress.getLocalHost().getHostAddress();
                System.out.println("--------------------------------------------");
                System.out.println("""

                         _____   ___  ______  _____        _____  _   _ _____      ______ _____ _____ _____ _____  _   _ _____________   __
                        /  __ \\ / _ \\ | ___ \\/  ___|      /  ___|| | | /  __ \\     |  _  \\_   _/  ___/  __ \\  _  || | | |  ___| ___ \\ \\ / /
                        | /  \\// /_\\ \\| |_/ /\\ `--. ______\\ `--. | | | | /  \\/_____| | | | | | \\ `--.| /  \\/ | | || | | | |__ | |_/ /\\ V /\s
                        | |    |  _  ||    /  `--. \\______|`--. \\| | | | |  |______| | | | | |  `--. \\ |   | | | || | | |  __||    /  \\ / \s
                        | \\__/\\| | | || |\\ \\ /\\__/ /      /\\__/ /\\ \\_/ / \\__/\\     | |/ / _| |_/\\__/ / \\__/\\ \\_/ /\\ \\_/ / |___| |\\ \\  | | \s
                         \\____/\\_| |_/\\_| \\_|\\____/       \\____/  \\___/ \\____/     |___/  \\___/\\____/ \\____/\\___/  \\___/\\____/\\_| \\_| \\_/ \s
                                                                                                                                          \s
                                                                                                                                          \s
                        """);
                System.out.println("CarsService Discovery Server started at " + ip + ":" + port);
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
