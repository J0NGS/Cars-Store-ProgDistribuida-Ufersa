package UserBDProtocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserBDProtocolInterface extends Remote {
    public boolean ping() throws RemoteException;
    public String create(String request) throws RemoteException;
    public String read(String request) throws RemoteException;
    public String update(String request) throws RemoteException;
    public String delete(String request) throws RemoteException;
    public String searchByLogin(String request) throws RemoteException;
    public String authenticate(String request) throws RemoteException;
}
