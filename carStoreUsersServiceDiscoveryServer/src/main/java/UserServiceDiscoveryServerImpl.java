import discoveryInterface.DiscoveryServerInterface;
import usersServiceProtocols.UsersServiceProtocolInterface;

import java.io.Serial;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class UserServiceDiscoveryServerImpl extends UnicastRemoteObject implements DiscoveryServerInterface {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Map<String, String> serviceInstances; // serviceName -> address

    protected UserServiceDiscoveryServerImpl() throws RemoteException {
        super();
        this.serviceInstances = new ConcurrentHashMap<>();
    }

    @Override
    public void registerInstance(String serviceName, String address) throws RemoteException {
        serviceInstances.put(serviceName, address);
        System.out.println("Instance registered: " + serviceName + " at " + address);
    }

    @Override
    public void registerInstance(String serviceName, String address, int weight) throws RemoteException {

    }

    @Override
    public void deregisterInstance(String serviceName) throws RemoteException {
        serviceInstances.remove(serviceName);
        System.out.println("Instance deregistered: " + serviceName);
    }

    @Override
    public Map<String, String> getInstances() throws RemoteException {
        return new HashMap<>(serviceInstances);
    }

    @Override
    public String getInstace() throws RemoteException {
        return loadBalance();
    }

    @Override
    public String loadBalance() throws RemoteException {
        String selectedInstance = loadBalanceInstance();
        if (selectedInstance != null) {
            System.out.println("Instance selected->" + selectedInstance);
            return selectedInstance;
        } else {
            throw new RemoteException("No available instances.");
        }
    }

    private String loadBalanceInstance() {
        Map<String, CompletableFuture<QueueSizeResult>> futures = new HashMap<>();

        for (Map.Entry<String, String> entry : serviceInstances.entrySet()) {
            String serviceName = entry.getKey();
            String serviceAddress = entry.getValue();

            CompletableFuture<QueueSizeResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    UsersServiceProtocolInterface service = (UsersServiceProtocolInterface) Naming.lookup(serviceAddress);
                    Integer queueSize = service.getQueueSize();
                    return new QueueSizeResult(serviceAddress, queueSize);
                } catch (Exception e) {
                    System.err.println("Error contacting service instance: " + e.getMessage());
                    return new QueueSizeResult(serviceAddress, Integer.MAX_VALUE); // Treat errors as worst case
                }
            });

            futures.put(serviceName, future);
        }

        String selectedInstance = null;
        int minQueueSize = Integer.MAX_VALUE;

        for (Map.Entry<String, CompletableFuture<QueueSizeResult>> entry : futures.entrySet()) {
            try {
                QueueSizeResult result = entry.getValue().get();
                if (result.getQueueSize() < minQueueSize) {
                    minQueueSize = result.getQueueSize();
                    selectedInstance = result.getServiceAddress();
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error during queue size retrieval: " + e.getMessage());
            }
        }

        return selectedInstance;
    }

    private static class QueueSizeResult {
        private final String serviceAddress;
        private final Integer queueSize;

        public QueueSizeResult(String serviceAddress, Integer queueSize) {
            this.serviceAddress = serviceAddress;
            this.queueSize = queueSize;
        }

        public String getServiceAddress() {
            return serviceAddress;
        }

        public Integer getQueueSize() {
            return queueSize;
        }
    }
}
