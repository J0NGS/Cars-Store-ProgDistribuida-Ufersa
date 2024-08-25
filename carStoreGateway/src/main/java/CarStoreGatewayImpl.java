import carStoreGatewayInterface.CarStoreGatewayInterface;
import carsServiceProtocols.CarsServiceProtocolInterface;
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
    private final Supplier<CarsServiceProtocolInterface> carsServiceSupplier;
    private final Log gatewayLogger;

    public CarStoreGatewayImpl(String userDiscoveryServerAddress, String carsDiscoveryServerAddress) throws RemoteException {
        super();
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();

        // Logger setup
        gatewayLogger = new Log(System.getProperty("user.home") + "/Desktop/carStore/", "CarStoreGatewayLogger");
        gatewayLogger.logInfo(() -> "CarStoreGateway initialized");

        // Função para obter o UsersServiceProtocolInterface usando o userDiscoveryServerAddress
        this.userServiceSupplier = () -> {
            try {
                // Conexão com o DiscoveryServer de usuários
                DiscoveryServerInterface discoveryServer = (DiscoveryServerInterface) Naming.lookup(userDiscoveryServerAddress);
                gatewayLogger.logInfo(() -> "Connecting to User DiscoveryServer successful");

                // Obtendo o serviceAddress do DiscoveryServer
                String serviceAddress = discoveryServer.getInstace();
                gatewayLogger.logInfo(() -> "UserService address obtained: " + serviceAddress);
                return (UsersServiceProtocolInterface) Naming.lookup(serviceAddress);
            } catch (Exception e) {
                gatewayLogger.logError(() -> "Error connecting to User DiscoveryServer: " + e.getMessage());
                throw new RuntimeException("Error connecting to UserService", e);
            }
        };

        // Função para obter o CarsServiceProtocolInterface usando o carsDiscoveryServerAddress
        this.carsServiceSupplier = () -> {
            try {
                // Conexão com o DiscoveryServer de carros
                DiscoveryServerInterface discoveryServer = (DiscoveryServerInterface) Naming.lookup(carsDiscoveryServerAddress);
                gatewayLogger.logInfo(() -> "Connecting to Car DiscoveryServer successful");

                // Obtendo o serviceAddress do DiscoveryServer
                String serviceAddress = discoveryServer.getInstace();
                gatewayLogger.logInfo(() -> "CarService address obtained: " + serviceAddress);
                return (CarsServiceProtocolInterface) Naming.lookup(serviceAddress);
            } catch (Exception e) {
                gatewayLogger.logError(() -> "Error connecting to Car DiscoveryServer: " + e.getMessage());
                throw new RuntimeException("Error connecting to CarService", e);
            }
        };
    }

    @Override
    public String registerUser(String request) throws RemoteException {
        try {
            return handleUserRequest(request, userService -> userService.registerUser(request));
        } catch (Exception e) {
            gatewayLogger.logError(() -> "Error processing registerUser request: " + e.getMessage());
            return "";
        }
    }

    @Override
    public String authenticate(String request) throws RemoteException {
        return handleUserRequest(request, userService -> userService.authenticate(request));
    }

    @Override
    public String searchUserByLogin(String request) throws RemoteException {
        return handleUserRequest(request, userService -> userService.searchByUsername(request));
    }

    @Override
    public String searchCarByModel(String request) throws RemoteException {
        return handleCarRequest(request, carService -> carService.searchByCarName(request));
    }

    @Override
    public String searchCarByRenavam(String request) throws RemoteException {
        return handleCarRequest(request, carService -> carService.searchByRenavam(request));
    }

    @Override
    public String getAllCars() throws RemoteException {
        return handleCarRequest("", CarsServiceProtocolInterface::getAllCars);
    }

    @Override
    public String buyCar(String request) throws RemoteException {
        return handleCarRequest(request, carService -> carService.deleteCar(request));
    }

    @Override
    public String deleteCar(String request) throws RemoteException {
        return handleCarRequest(request, carService -> carService.deleteCar(request));
    }

    private String handleUserRequest(String request, UserRequestHandler handler) {
        try {
            requestQueue.add(request);
            Future<String> future = executorService.submit(() -> handler.handle(userServiceSupplier.get()));
            String serviceResponse = future.get();  // Espera a execução da thread e obtém a resposta
            gatewayLogger.logInfo(() -> "User request handled: " + request);
            return serviceResponse;
        } catch (Exception e) {
            gatewayLogger.logError(() -> "Error processing user request: " + e.getMessage());
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, e.getMessage()).toString();
        }
    }

    private String handleCarRequest(String request, CarRequestHandler handler) {
        try {
            requestQueue.add(request);
            Future<String> future = executorService.submit(() -> handler.handle(carsServiceSupplier.get()));
            String serviceResponse = future.get();  // Espera a execução da thread e obtém a resposta
            gatewayLogger.logInfo(() -> "Car request handled: " + request);
            return serviceResponse;
        } catch (Exception e) {
            gatewayLogger.logError(() -> "Error processing car request: " + e.getMessage());
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, e.getMessage()).toString();
        }
    }

    @FunctionalInterface
    private interface UserRequestHandler {
        String handle(UsersServiceProtocolInterface userService) throws RemoteException;
    }

    @FunctionalInterface
    private interface CarRequestHandler {
        String handle(CarsServiceProtocolInterface carService) throws RemoteException;
    }
}
