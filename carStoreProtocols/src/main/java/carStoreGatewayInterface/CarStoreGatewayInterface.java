package carStoreGatewayInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CarStoreGatewayInterface extends Remote {
    String registerUser(String request) throws RemoteException;
    String authenticate(String request) throws RemoteException ;
    String searchUserByLogin(String request) throws RemoteException ;
    String updatePassword(String request) throws RemoteException;
    String updateUsername(String request) throws RemoteException;
    String deleteUser(String request) throws RemoteException;
    String registerCar(String request) throws RemoteException;
    String updateCar(String request) throws RemoteException;
    String deleteCar(String request) throws RemoteException;
    String searchCarByModel(String request) throws RemoteException;
    String searchCarByRenavam(String request) throws RemoteException;
    String getAllCars() throws RemoteException;
    String buyCar(String request) throws RemoteException;
}
