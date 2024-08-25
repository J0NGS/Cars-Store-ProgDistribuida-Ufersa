package discoveryInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface DiscoveryServerInterface extends Remote {
    void registerInstance(String serviceName, String address) throws RemoteException;

    // Register a new instance with weight
    void registerInstance(String serviceName, String address, int weight) throws RemoteException;

    void deregisterInstance(String serviceName) throws RemoteException;
    String loadBalance() throws RemoteException;
    Map<String, String> getInstances() throws RemoteException;
    String getInstace() throws RemoteException;
}
