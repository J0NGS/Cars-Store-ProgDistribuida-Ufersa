package carsServiceProtocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CarsServiceProtocolInterface extends Remote {
    String registerCar(String request) throws RemoteException;

    String searchByCarName(String request) throws RemoteException;

    String searchByRenavam(String request) throws RemoteException;

    String deleteCar(String request) throws RemoteException;

    String updateCarPrice(String request) throws RemoteException;

    Integer getQueueSize() throws RemoteException;

    String getAllCars() throws  RemoteException;

}
