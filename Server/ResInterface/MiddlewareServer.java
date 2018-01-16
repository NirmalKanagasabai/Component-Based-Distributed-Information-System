package servercode.ResInterface;

import java.rmi.registry.Registry;
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;

// for now this extends Remote
// However, we eventually want it to be able to do everything a ResourceManager does since the client will only interface with MiddlewareServer
    // book customer
    // book flight
    // itinerary, et...
// Therefore, it'll be easier to just have this extend ResourceManager and then override all of those methods in the class implementation 

public interface MiddlewareServer extends ResourceManager {
    
    public void connectToManagers(String [] activeManagers) throws RemoteException;
    public ResourceManager getFlightManager() throws RemoteException;
    public ResourceManager getCarManager() throws RemoteException;
    public ResourceManager getRoomManager() throws RemoteException;

}