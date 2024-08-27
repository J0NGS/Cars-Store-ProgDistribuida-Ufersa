
import carsDBProtocols.CarsDBProtocolInterface;
import carsServiceProtocols.CarsServiceProtocolInterface;
import discoveryInterface.DiscoveryServerInterface;
import model.Cars;
import model.DTO.RESPONSE_CODE;
import model.DTO.Response;
import model.DTO.UpdateCarPriceRequest;
import model.DTO.UpdateCarRequest;
import utils.Log;

import java.io.Serial;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class ProtocolCarsServiceImpl extends UnicastRemoteObject implements CarsServiceProtocolInterface {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Queue<String> requestQueue;
    private final ExecutorService executorService;
    private final Supplier<CarsDBProtocolInterface> dbInterfaceSupplier;
    private final Log serviceLogger;

    public ProtocolCarsServiceImpl(String discoveryServerAddress) throws RemoteException {
        super();
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();

        // Logger setup
        serviceLogger = new Log(System.getProperty("user.home") + "/Desktop/carStore/", "CarsServiceLogger");
        serviceLogger.logInfo(() -> "CarsService initialized");

        // Função para obter o CarsDBProtocolInterface usando o discoveryServerAddress
        this.dbInterfaceSupplier = () -> {
            try {
                String discoveryServerName = "rmi://" + discoveryServerAddress + "/carStoreCarsDBDiscovery";
                // Conexão com o DiscoveryServer
                DiscoveryServerInterface discoveryServer = (DiscoveryServerInterface) Naming.lookup(discoveryServerName);
                // Obtendo o dbAddress do DiscoveryServer
                String dbAddress = discoveryServer.getInstace();
                return (CarsDBProtocolInterface) Naming.lookup(dbAddress);
            } catch (Exception e) {
                serviceLogger.logError(() -> "Error connecting to DiscoveryServer: " + e.getMessage());
                throw new RuntimeException("Error connecting to CarsDB", e);
            }
        };
    }

    @Override
    public String registerCar(String request) throws RemoteException {
        return handleRequest(request, db -> db.create(request));
    }

    @Override
    public String searchByCarName(String request) throws RemoteException {
        return handleRequest(request, db -> db.searchByModel(request));
    }

    @Override
    public String searchByRenavam(String request) throws RemoteException {
        return handleRequest(request, db -> db.searchByRenavam(request));
    }

    @Override
    public String deleteCar(String request) throws RemoteException {
        return handleRequest(request, db -> db.delete(request));
    }

    @Override
    public String updateCar(String request) throws RemoteException {
        return handleRequest(request, db -> {
            UpdateCarRequest carRequest = UpdateCarRequest.fromString(request);
            // Busca o carro atual pelo RENAVAM
            Cars existingCar = Cars.fromString(db.read(carRequest.renavam()));
            if (existingCar == null) {
                return new Response(RESPONSE_CODE.NOT_FOUND, "Car not found").toString();
            }
            // Remove o carro antigo
            db.delete(carRequest.renavam());
            // Cria um novo carro com os detalhes atualizados
            return db.create(carRequest.toString());
        });
    }


    @Override
    public String updatePrice(String request) throws RemoteException {
        return handleRequest(request, db -> {
            UpdateCarPriceRequest priceRequest = UpdateCarPriceRequest.fromString(request);
            // Busca o carro pelo RENAVAM
            Cars car = Cars.fromString(db.read(priceRequest.renavam()));
            if (car == null) {
                return new Response(RESPONSE_CODE.NOT_FOUND, "Car not found").toString();
            }
            // Atualiza o preço
            car.setPrice(priceRequest.newPrice());
            // Persiste a alteração
            return db.update(car.toString());
        });
    }



    @Override
    public String getAllCars() throws RemoteException {
        return handleRequest("", CarsDBProtocolInterface::getAll);
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
        String handle(CarsDBProtocolInterface db) throws RemoteException;
    }
}
