import discoveryInterface.DiscoveryServerInterface;

import java.io.Serial;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CarStoreCarsDBDiscoveryServerImpl extends UnicastRemoteObject implements DiscoveryServerInterface {
    @Serial
    private static final long serialVersionUID = 1L;

    // Map to store instances with their associated IP, port, and weight
    private final Map<String, InstanceDetails> instances;
    private final AtomicInteger currentIndex;
    private int currentWeight;
    private int gcdWeight;
    private int maxWeight;

    // Constructor
    protected CarStoreCarsDBDiscoveryServerImpl() throws RemoteException {
        instances = new ConcurrentHashMap<>();
        currentIndex = new AtomicInteger(-1);
        currentWeight = 0;
        gcdWeight = 0;
        maxWeight = 0;
    }

    // Helper class to store instance details
    private static class InstanceDetails {
        String address;
        int weight;

        InstanceDetails(String address, int weight) {
            this.address = address;
            this.weight = weight;
        }
    }

    @Override
    public void registerInstance(String serviceName, String address) throws RemoteException {

    }

    // Register a new instance with weight
    @Override
    public void registerInstance(String serviceName, String address, int weight) throws RemoteException {
        instances.put(serviceName, new InstanceDetails(address, weight));
        updateWeights();
        System.out.println("Instance registered: " + serviceName + " at " + address + " with weight " + weight);
    }

    // Deregister an instance
    @Override
    public void deregisterInstance(String serviceName) throws RemoteException {
        instances.remove(serviceName);
        updateWeights();
        System.out.println("Instance deregistered: " + serviceName);
    }

    @Override
    public String loadBalance() throws RemoteException {
        return "";
    }

    // Get the list of all registered instances, removing any that are not reachable
    @Override
    public Map<String, String> getInstances() throws RemoteException {
        instances.entrySet().removeIf(entry -> !isInstanceReachable(entry.getValue().address));
        return instances.entrySet().stream()
                .collect(ConcurrentHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().address), ConcurrentHashMap::putAll);
    }

    // Helper method to check if an instance is reachable
    private boolean isInstanceReachable(String address) {
        try {
            Naming.lookup(address);
            return true;
        } catch (Exception e) {
            System.out.println("Instance not reachable, removing: " + address);
            return false;
        }
    }

    // Update the maximum weight and greatest common divisor (GCD) of all instance weights
    private void updateWeights() {
        List<InstanceDetails> instanceList = new ArrayList<>(instances.values());
        gcdWeight = instanceList.stream().mapToInt(i -> i.weight).reduce(this::gcd).orElse(1);
        maxWeight = instanceList.stream().mapToInt(i -> i.weight).max().orElse(1);
    }

    // Weighted Round-Robin load balancing algorithm
    @Override
    public String getInstace() throws RemoteException {
        List<InstanceDetails> validInstances = new ArrayList<>(instances.values());

        if (validInstances.isEmpty()) {
            throw new RemoteException("No instances available");
        }

        while (true) {
            currentIndex.set((currentIndex.get() + 1) % validInstances.size());
            if (currentIndex.get() == 0) {
                currentWeight -= gcdWeight;
                if (currentWeight <= 0) {
                    currentWeight = maxWeight;
                    if (currentWeight == 0) {
                        throw new RemoteException("No instances available");
                    }
                }
            }

            InstanceDetails instance = validInstances.get(currentIndex.get());
            if (instance.weight >= currentWeight) {
                return instance.address;
            }
        }
    }

    // Helper method to calculate the greatest common divisor (GCD) of two numbers
    private int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}
