import BD.ProtocolUsersBDImpl;
import UserBDProtocols.UserBDProtocolInterface;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class ServerUsersDB {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                // Solicita ao usuário a porta para iniciar o servidor
                System.out.print("Digite a porta para iniciar o servidor: ");
                int port = Integer.parseInt(scanner.nextLine());

                // Protocolo implementado
                // Endereço
                String name = "rmi://localhost:" + port + "/userDb";

                System.out.println("\r\n" + //
                        " __    __       _______. _______ .______          _______.   .______    _______  \r\n" + //
                        "|  |  |  |     /       ||   ____||   _  \\        /       |   |   _  \\  |       \\ \r\n" + //
                        "|  |  |  |    |   (----`|  |__   |  |_)  |      |   (----`   |  |_)  | |  .--.  |\r\n" + //
                        "|  |  |  |     \\   \\    |   __|  |      /        \\   \\       |   _  <  |  |  |  |\r\n" + //
                        "|  `--'  | .----)   |   |  |____ |  |\\  \\----.----)   |      |  |_)  | |  '--'  |\r\n" + //
                        " \\______/  |_______/    |_______|| _| `._____|_______/       |______/  |_______/ \r\n" + //
                        "                                                                                 \r\n" + //
                        "");

                // Cria o registro na porta especificada
                UserBDProtocolInterface protocol = new ProtocolUsersBDImpl();
                LocateRegistry.createRegistry(port);

                // Registrando e associando o protocolo
                Naming.rebind(name, protocol);

                // Exibe o IP e a porta no console
                String ip = InetAddress.getLocalHost().getHostAddress();
                System.out.println("--------------------------------------------");
                System.out.println("Servidor iniciado em " + ip + ":" + port);
                System.out.println("Esperando por requisições...");
                System.out.println("--------------------------------------------");
                String user = protocol.read("oi");
                System.out.println(user);
                // Saindo do loop após iniciar com sucesso
                break;

            } catch (Exception e) {
                System.out.println("Erro ao iniciar o servidor na porta fornecida. Tente novamente.");
                e.printStackTrace();
            }
        }
        scanner.close();
    }
}
