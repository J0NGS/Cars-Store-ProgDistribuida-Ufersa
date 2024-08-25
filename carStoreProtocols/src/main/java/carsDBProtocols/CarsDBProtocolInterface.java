package carsDBProtocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CarsDBProtocolInterface extends Remote {
    boolean ping() throws RemoteException;
    String create(String request) throws RemoteException;
    String read(String request) throws RemoteException;
    String update(String request) throws RemoteException;
    String delete(String request) throws RemoteException;
    String searchByRenavam(String request) throws RemoteException;
    String searchByModel(String request)  throws RemoteException;
    String getAll() throws RemoteException;
}