package userDBProtocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UsersDBProtocolInterface extends Remote {
    boolean ping() throws RemoteException;
    String create(String request) throws RemoteException;
    String read(String request) throws RemoteException;
    String update(String request) throws RemoteException;
    String delete(String request) throws RemoteException;
    String searchByLogin(String request) throws RemoteException;
    String authenticate(String request) throws RemoteException;
}
