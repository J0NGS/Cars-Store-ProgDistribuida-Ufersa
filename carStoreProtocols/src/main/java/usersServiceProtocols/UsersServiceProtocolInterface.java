package usersServiceProtocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UsersServiceProtocolInterface extends Remote  {
    String registerUser(String request) throws RemoteException;
    String searchByUsername(String request) throws RemoteException;
    String authenticate(String request) throws RemoteException;
    String deleteUser(String request) throws RemoteException;
    String updatePassword(String request) throws RemoteException;
    Integer getQueueSize() throws RemoteException;
}
