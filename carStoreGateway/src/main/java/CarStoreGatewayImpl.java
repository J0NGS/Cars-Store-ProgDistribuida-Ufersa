import carStoreGatewayInterface.CarStoreGatewayInterface;
import discoveryInterface.DiscoveryServerInterface;
import model.DTO.RESPONSE_CODE;
import model.DTO.Response;
import usersServiceProtocols.UsersServiceProtocolInterface;
import utils.Log;

import java.io.Serial;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class CarStoreGatewayImpl extends UnicastRemoteObject implements CarStoreGatewayInterface {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Queue<String> requestQueue;
    private final ExecutorService executorService;
    private final Supplier<UsersServiceProtocolInterface> userServiceSupplier;
    private final Log gatewayLogger;

    public CarStoreGatewayImpl(String discoveryServerAddress) throws RemoteException {
        super();
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();

        // Logger setup
        gatewayLogger = new Log(System.getProperty("user.home") + "/Desktop/carStore/", "CarStoreGatewayLogger");
        gatewayLogger.logInfo(() -> "CarStoreGateway initialized");

        // Função para obter o UsersServiceProtocolInterface usando o discoveryServerAddress
        this.userServiceSupplier = () -> {
            try {
                // Conexão com o DiscoveryServer
                DiscoveryServerInterface discoveryServer = (DiscoveryServerInterface) Naming.lookup(discoveryServerAddress);
                gatewayLogger.logInfo(() -> "Connecting to DiscoveryServer successful");

                // Obtendo o serviceAddress do DiscoveryServer
                String serviceAddress = discoveryServer.getInstace();
                gatewayLogger.logInfo(() -> "Service address obtained: " + serviceAddress);
                return (UsersServiceProtocolInterface) Naming.lookup(serviceAddress);
            } catch (Exception e) {
                gatewayLogger.logError(() -> "Error connecting to DiscoveryServer: " + e.getMessage());
                throw new RuntimeException("Error connecting to UserService", e);
            }
        };
    }

    @Override
    public String registerUser(String request) throws RemoteException {
        try {
            return handleRequest(request, userService -> userService.registerUser(request));
        } catch (Exception e){
            gatewayLogger.logError(() -> "Error processing registerUser request: " + e.getMessage());
            return "";
        }
    }

    @Override
    public String authenticate(String request) throws RemoteException {
        return handleRequest(request, userService -> userService.authenticate(request));
    }

    @Override
    public String searchUserByLogin(String request) throws RemoteException {
        return handleRequest(request, userService -> userService.searchByUsername(request));
    }

    private String handleRequest(String request, RequestHandler handler) {
        try {
            requestQueue.add(request);
            Future<String> future = executorService.submit(() -> handler.handle(userServiceSupplier.get()));
            String serviceResponse = future.get();  // Espera a execução da thread e obtém a resposta
            gatewayLogger.logInfo(() -> "Request handled: " + request);
            return serviceResponse;
        } catch (Exception e) {
            gatewayLogger.logError(() -> "Error processing request: " + e.getMessage());
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, e.getMessage()).toString();
        }
    }

    @FunctionalInterface
    private interface RequestHandler {
        String handle(UsersServiceProtocolInterface userService) throws RemoteException;
    }
}
