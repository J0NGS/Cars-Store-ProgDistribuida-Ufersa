import discoveryInterface.DiscoveryServerInterface;
import userDBProtocols.UsersDBProtocolInterface;
import usersServiceProtocols.UsersServiceProtocolInterface;
import model.DTO.*;
import utils.Log;

import java.io.Serial;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class ProtocolUsersServiceImpl extends UnicastRemoteObject implements UsersServiceProtocolInterface {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Queue<String> requestQueue;
    private final ExecutorService executorService;
    private final Supplier<UsersDBProtocolInterface> dbInterfaceSupplier;
    private final Log serviceLogger;

    public ProtocolUsersServiceImpl(String discoveryServerAddress) throws RemoteException {
        super();
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();

        // Logger setup
        serviceLogger = new Log(System.getProperty("user.home") + "/Desktop/carStore/", "UserServiceLogger");
        serviceLogger.logInfo(() -> "UserService initialized");

        // Função para obter o UserBDProtocolInterface usando o discoveryServerAddress
        this.dbInterfaceSupplier = () -> {
            try {
                String discoveryServerName = "rmi://" + discoveryServerAddress + "/carStoreUsersDBDiscovery";
                // Conexão com o DiscoveryServer
                DiscoveryServerInterface discoveryServer = (DiscoveryServerInterface) Naming.lookup(discoveryServerName);
                // Obtendo o dbAdress do DiscoveryServer
                String dbAddress = discoveryServer.getInstace();
                return (UsersDBProtocolInterface) Naming.lookup(dbAddress);
            } catch (Exception e) {
                serviceLogger.logError(() -> "Error connecting to DiscoveryServer: " + e.getMessage());
                throw new RuntimeException("Error connecting to UserDB", e);
            }
        };
    }

    @Override
    public String registerUser(String request) throws RemoteException {
        return handleRequest(request, db -> db.create(request));
    }

    @Override
    public String searchByUsername(String request) throws RemoteException {
        return handleRequest(request, db -> db.searchByLogin(request));
    }

    @Override
    public String authenticate(String request) throws RemoteException {
        return handleRequest(request, db -> db.authenticate(request));
    }

    @Override
    public String deleteUser(String request) throws RemoteException {
        return handleRequest(request, db -> db.delete(request));
    }

    @Override
    public String updatePassword(String request) throws RemoteException {
        return handleRequest(request, db -> db.update(request));
    }

    @Override
    public Integer getQueueSize() throws RemoteException {
        return requestQueue.size();
    }

    private String handleRequest(String request, RequestHandler handler) {
        try {
            requestQueue.add(request);
            Future<String> future = executorService.submit(() -> handler.handle(dbInterfaceSupplier.get()));
            String dbResponse = future.get();
            serviceLogger.logInfo(() -> "Request handled: " + request);
            return dbResponse;
        } catch (Exception e) {
            serviceLogger.logError(() -> "Error processing request: " + e.getMessage());
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, e.getMessage()).toString();
        }
    }

    @FunctionalInterface
    private interface RequestHandler {
        String handle(UsersDBProtocolInterface db) throws RemoteException;
    }
}
