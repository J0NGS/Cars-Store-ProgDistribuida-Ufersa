import discoveryInterface.DiscoveryServerInterface;
import model.DTO.RESPONSE_CODE;
import model.DTO.Response;

import java.io.Serial;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CarStoreUsersDBDiscoveryServerImpl extends UnicastRemoteObject implements DiscoveryServerInterface {
    @Serial
    private static final long serialVersionUID = 1L;

    // map to store instances with their associated IP and port
    private final Map<String, String> instances;
    private final AtomicInteger roundRobinIndex;

    // constructor
    protected CarStoreUsersDBDiscoveryServerImpl() throws RemoteException {
        instances = new ConcurrentHashMap<>();
        roundRobinIndex = new AtomicInteger(0);
    }

    // register a new instance
    @Override
    public void registerInstance(String serviceName, String address) throws RemoteException {
        instances.put(serviceName, address);
        System.out.println("Instance registered: " + serviceName + " at " + address);
    }

    @Override
    public void registerInstance(String serviceName, String address, int weight) throws RemoteException {

    }

    // deregister an instance
    @Override
    public void deregisterInstance(String serviceName) throws RemoteException {
        instances.remove(serviceName);
        System.out.println("Instance deregistered: " + serviceName);
    }

    // get the list of all registered instances, removing any that are not reachable
    @Override
    public Map<String, String> getInstances() throws RemoteException {
        instances.entrySet().removeIf(entry -> !isInstanceReachable(entry.getValue()));
        return instances;
    }

    // helper method to check if an instance is reachable
    private boolean isInstanceReachable(String address) {
        try {
            // try to lookup the remote object
            Naming.lookup(address);
            return true;
        } catch (Exception e) {
            System.out.println("Instance not reachable, removing: " + address);
            return false;
        }
    }

    // round-robin load balancing algorithm
    @Override
    public String loadBalance() throws RemoteException {
        // no implementation needed for round-robin; selection is handled in getInstance()
        return new Response(RESPONSE_CODE.NOT_IMPLEMENTED, RESPONSE_CODE.NOT_IMPLEMENTED.getDescription()).toString();
    }

    @Override
    public String getInstace() throws RemoteException {
        List<String> validInstances = getInstances().values().stream().toList();

        if (validInstances.isEmpty()) {
            throw new RemoteException("No instances available");
        }

        int index = roundRobinIndex.getAndUpdate(i -> (i + 1) % validInstances.size());
        String selected = validInstances.get(index);
        System.out.println("Instance selected->" + selected);
        return selected;
    }


}