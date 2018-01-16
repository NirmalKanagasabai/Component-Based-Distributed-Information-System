// -------------------------------
// adapated from Kevin T. Manley
// CSE 593
//
package servercode.ResImpl;

import servercode.ResInterface.*;

import hotelcode.ResImpl.Hotel;

import java.util.*;
import java.io.File;
import java.io.Serializable;
import java.rmi.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import flightcode.ResImpl.Flight;

import LockManager.*;

//public class ResourceManagerImpl extends java.rmi.server.UnicastRemoteObject
public class ResourceManagerImpl implements ResourceManager, Serializable {
    
    public static Registry registry;
    protected RMHashtable m_itemHT = new RMHashtable();
    protected Thread txnKillerThread;
    protected LockManager lm = new LockManager();
    protected WorkingSet<Customer> ws = new WorkingSet<Customer>();
    
    protected ItemManager rmHotel  = null;
    protected ItemManager rmCar    = null;
    protected ItemManager rmFlight = null;
    protected TransactionManager txnManager;
    private Crash crashCondition;
    private MasterRecord masterRecord = new MasterRecord();
    
    private static int rmiPort = 5005;
    
    private static String carServer = "localhost";
    private static String flightServer = "localhost";
    private static String hotelServer = "localhost";
    private static int carPort = 5006;
    private static int flightPort = 5007;
    private static int hotelPort = 5008;
    
    public static void main(String args[]) {
        if (args.length == 7) {
            rmiPort = Integer.parseInt(args[0]);
            carServer = args[1];
            carPort = Integer.parseInt(args[2]);
            flightServer = args[3];
            flightPort = Integer.parseInt(args[4]);
            hotelServer = args[5];
            hotelPort = Integer.parseInt(args[6]);
        }
        else {
            System.err.println("Wrong usage");
            System.out.println("Usage: java ResImpl.ResourceManagerImpl [RMI port] [car server] [car port] [flight server] [flight port] [hotel server] [hotel port]");
            System.exit(1);
        }
        
        try {
            // create a new Server object
            ResourceManagerImpl obj = new ResourceManagerImpl(carServer, carPort, flightServer, flightPort, hotelServer, hotelPort);
            // dynamically generate the stub (client proxy)
            ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
            
            // Bind the remote object's stub in the registry
            registry = LocateRegistry.getRegistry(rmiPort);
            registry.rebind("Group23_ResourceManager", rm);
            
            System.err.println("Server ready");
            obj.recoverMiddleware();
        }
        catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
        
        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
    }
    
    public ResourceManagerImpl(String carServer, int carPort, String flightServer, int flightPort, String hotelServer, int hotelPort) throws RemoteException {
        
        bindHotelManager(hotelServer, hotelPort);
        bindCarManager(carServer, carPort);
        bindFlightManager(flightServer, flightPort);
        
        txnManager = TransactionManager.getInstance(rmCar, rmFlight, rmHotel, this);
        txnKillerThread = new Thread(new TransactionKiller(txnManager, this));
        txnKillerThread.start();
        
        File mrFile = new File(getMasterRecordFileName());
        if (mrFile.exists()) {
            masterRecord = (MasterRecord) SerializeUtils.loadFromDisk(getMasterRecordFileName());
            m_itemHT = (RMHashtable) SerializeUtils.loadFromDisk(getCommittedFileName());
        }
        
    }
    
    public void rebind(String rm) throws RemoteException {
        if (rm.equals("car"))
            txnManager.updateCarManagerRef(bindCarManager(carServer, carPort));
        else if (rm.equals("flight"))
            txnManager.updateFlightManagerRef(bindFlightManager(flightServer, flightPort));
        else if (rm.equals("hotel"))
            txnManager.updatehotelManagerRef(bindHotelManager(hotelServer, hotelPort));
    }
    
    private ItemManager bindFlightManager(String flightServer, int flightPort) {
        try {
            Registry registry = LocateRegistry.getRegistry(flightServer,
                                                           flightPort);
            rmFlight = (ItemManager) registry.lookup("Group23_FlightManager");
            if (rmFlight != null) {
                System.out.println("Successfully connected to the Flight Manager");
            }
            else {
                System.out.println("Connection to the Flight Manager failed");
            }
        }
        catch (Exception e) {
            System.err.println("Flight exception: " + e.toString());
            e.printStackTrace();
        }
        
        return rmFlight;
    }
    
    private ItemManager bindCarManager(String carServer, int carPort) {
        rmCar = null;
        
        try {
            Registry registry = LocateRegistry.getRegistry(carServer, carPort);
            rmCar = (ItemManager) registry.lookup("Group23_CarManager");
            if (rmCar != null) {
                System.out.println("Successfully connected to the Car Manager");
            }
            else {
                System.out.println("Connection to the Car Manager failed");
            }
        }
        catch (Exception e) {
            System.err.println("Car exception: " + e.toString());
            e.printStackTrace();
        }
        
        return rmCar;
    }
    
    private ItemManager bindHotelManager(String hotelServer, int hotelPort) {
        try {
            // get a reference to the rmiregistry on Hotel's server
            Registry registry = LocateRegistry.getRegistry(hotelServer, hotelPort);
            // get the proxy and the remote reference by rmiregistry lookup
            rmHotel = (ItemManager) registry.lookup("Group23_HotelManager");
            if (rmHotel != null) {
                System.out.println("Successfully connected to the Hotel Manager");
            }
            else {
                System.out.println("Connection to the Hotel Manager failed");
            }
        }
        catch (Exception e) {
            System.err.println("Hotel exception: " + e.toString());
            e.printStackTrace();
        }
        
        return rmHotel;
    }
    
    // Reads a data item
    private RMItem readData(int id, String key) {
        synchronized (m_itemHT) {
            return (RMItem) m_itemHT.get(key);
        }
    }
    
    // Writes a data item
    public void writeData(int id, String key, RMItem value) {
        synchronized (m_itemHT) {
            m_itemHT.put(key, value);
        }
    }
    
    // Remove the item out of storage
    public RMItem removeData(int id, String key) {
        synchronized (m_itemHT) {
            return (RMItem) m_itemHT.remove(key);
        }
    }
    
    protected Customer getCustomer(int customerID) {
        // Read customer object if it exists (and read lock it)
        Customer cust = (Customer) readData(0, Customer.getKey(customerID));
        if (cust == null) {
            Trace.warn("Customer " + customerID + " doesn't exist");
        }
        
        return cust;
    }
    
    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its
    // current price
    public boolean addFlight(int id, int flightNum, int flightSeats,
                             int flightPrice) throws RemoteException, InvalidTransactionException, ConnectException {
        
        //Sleep to test concurrency. Je voulais voir qu'est-ce qui se passe lorsque le middleware sleeps pis que
        //j'envoie un autre request à partir d'un autre client. Réponse: Ça ne bloque pas
        /*try {
         Thread.sleep(10000);
         } catch (Exception e) {
         
         }*/
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $"+ flightPrice + ", " + flightSeats + ") called");
        
        try {
            rmFlight.addItem(id, Integer.toString(flightNum), flightSeats, flightPrice);
            txnManager.enlist(id, "flight");
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            return false;
        }
        
        return true;
    }
    
    public boolean deleteFlight(int id, int flightNum) throws RemoteException, InvalidTransactionException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        boolean result;
        
        try {
            result = rmFlight.deleteItem(id, Integer.toString(flightNum));
            txnManager.enlist(id, "flight");
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            result = false;
        }
        
        return result;
    }
    
    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains
    // its current price
    public boolean addRooms(int id, String location, int count, int price)
    throws RemoteException, InvalidTransactionException, ConnectException {
        Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called");
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            rmHotel.addItem(id, location, count, price);
            txnManager.enlist(id, "hotel");
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            return false;
        }
        return true;
    }
    
    // Delete rooms from a location
    public boolean deleteRooms(int id, String location) throws RemoteException, InvalidTransactionException, ConnectException {
        Trace.info("RM::deleteRoom(" + id + ", " + location + ")");
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        boolean result;
        
        try {
            result = rmHotel.deleteItem(id, location);
            txnManager.enlist(id, "room");
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            result = false;
        }
        
        return result;
    }
    
    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its
    // current price
    public boolean addCars(int id, String location, int count, int price)
    throws RemoteException, InvalidTransactionException, ConnectException {
        Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called");
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            rmCar.addItem(id, location, count, price);
            txnManager.enlist(id, "car");
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            return false;
        }
        
        return true;
    }
    
    // Delete cars from a location
    public boolean deleteCars(int id, String location) throws RemoteException, InvalidTransactionException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        boolean result;
        
        try {
            result = rmCar.deleteItem(id, location);
            txnManager.enlist(id, "car");
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            result = false;
        }
        
        return result;
    }
    
    // Returns the number of empty seats on this flight
    public int queryFlight(int id, int flightNum) throws RemoteException, InvalidTransactionException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            int qty = rmFlight.queryItemQuantity(id, Integer.toString(flightNum));
            txnManager.enlist(id, "flight");
            return qty;
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            return -1;
        }
        
    }
    
    // Returns price of this flight
    public int queryFlightPrice(int id, int flightNum) throws RemoteException, InvalidTransactionException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            int price = rmFlight.queryItemPrice(id, Integer.toString(flightNum));
            txnManager.enlist(id, "flight");
            return price;
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            return -1;
        }
        
    }
    
    // Returns the number of rooms available at a location
    public int queryRooms(int id, String location) throws RemoteException, InvalidTransactionException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            int qty = rmHotel.queryItemQuantity(id, location);
            txnManager.enlist(id, "hotel");
            return qty;
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            return -1;
        }
    }
    
    // Returns room price at this location
    public int queryRoomsPrice(int id, String location) throws RemoteException, InvalidTransactionException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            int price = rmHotel.queryItemPrice(id, location);
            txnManager.enlist(id, "hotel");
            return price;
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            return -1;
        }
    }
    
    // Returns the number of cars available at a location
    public int queryCars(int id, String location) throws RemoteException, InvalidTransactionException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            int qty = rmCar.queryItemQuantity(id, location);
            txnManager.enlist(id, "car");
            return qty;
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            //txnManager.abort(id);
            return -1;
        }
    }
    
    // Returns price of cars at this location
    public int queryCarsPrice(int id, String location) throws RemoteException, InvalidTransactionException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            int price = rmCar.queryItemPrice(id, location);
            txnManager.enlist(id, "car");
            return price;
        } catch (DeadlockException e) {
            Trace.error(e.getMessage());
            //throw new DeadlockException(id, location);
            return -1;
        }
    }
    
    // Returns data structure containing customer reservation info. Returns null
    // if the
    // customer doesn't exist. Returns empty RMHashtable if customer exists but
    // has no
    // reservations.
    /*public RMHashtable getCustomerReservations(int id, int customerID)
     throws RemoteException {
     Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called");
     
     try {
     lm.Lock(id, String.valueOf(customerID), LockType.READ);
     } catch (DeadlockException e) {
     
     }
     
     Customer cust = (Customer) readData(id, Customer.getKey(customerID));
     if (cust == null) {
     Trace.warn("RM::getCustomerReservations failed(" + id + ", "
     + customerID + ") failed--customer doesn't exist");
     return null;
     }
     else {
     return cust.getReservations();
     } // if
     }*/
    
    // return a bill
    public String queryCustomerInfo(int id, int customerID)
    throws RemoteException, DeadlockException, InvalidTransactionException {
        Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called");
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            lm.Lock(id, String.valueOf(customerID), LockType.READ);
        } catch (DeadlockException e) {
            throw new DeadlockException(id, String.valueOf(customerID));
        }
        
        Customer cust;
        
        if (ws.hasItem(customerID+"")){
            cust = (Customer) ws.getItem(customerID+"");
        } else {
            cust = (Customer) readData(id, Customer.getKey(customerID));
        }
        
        txnManager.enlist(id, "customer");
        if (cust == null) {
            Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID
                       + ") failed--customer doesn't exist");
            return "Customer does not exist"; // NOTE: don't change this--WC counts on this value
            // indicating a customer does not exist...
        }
        else {
            String s = cust.printBill();
            
            Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID
                       + "), bill follows...");
            System.out.println(s);
            return s;
        } // if
    }
    
    // customer functions
    // new customer just returns a unique customer identifier
    public int newCustomer(int id) throws RemoteException, DeadlockException, InvalidTransactionException {
        Trace.info("INFO: RM::newCustomer(" + id + ") called");
        // Generate a globally unique ID for the new customer
        int cid = Integer.parseInt(String.valueOf(id)
                                   + String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND))
                                   + String.valueOf(Math.round(Math.random() * 100 + 1)));
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            lm.Lock(id, String.valueOf(cid), LockType.WRITE);
        } catch (DeadlockException e) {
            throw new DeadlockException(id, String.valueOf(cid));
        }
        
        txnManager.enlist(id, "customer");
        Customer cust = new Customer(cid);
        
        ws.addCommand(id, new CommandCustomerPut(id,  cust.getKey(), cust));
        ws.sendCurrentState(cust.getKey(), cust);
        ws.addLocationToTxn(id,  cust.getKey());
        //writeData(id, cust.getKey(), cust);
        Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
        return cid;
    }
    
    // I opted to pass in customerID instead. This makes testing easier
    public boolean newCustomer(int id, int customerID) throws RemoteException, DeadlockException, InvalidTransactionException {
        Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called");
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            lm.Lock(id, String.valueOf(customerID), LockType.WRITE);
        } catch (DeadlockException e) {
            throw new DeadlockException(id, String.valueOf(customerID));
        }
        
        txnManager.enlist(id, "customer");
        Customer cust;
        
        if (ws.hasItem(customerID+"")){
            cust = (Customer) ws.getItem(customerID+"");
        } else {
            cust = (Customer) readData(id, Customer.getKey(customerID));
            
            if (cust != null) {
                cust = cust.getCopy();
            }
        }
        
        if (cust == null) {
            cust = new Customer(customerID);
            
            ws.addCommand(id, new CommandCustomerPut(id,  cust.getKey(), cust));
            ws.sendCurrentState(customerID+"", cust);
            ws.addLocationToTxn(id, customerID+"");
            
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID
                       + ") created a new customer");
            return true;
        }
        else {
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID
                       + ") failed--customer already exists");
            return false;
        } // else
    }
    
    // Deletes customer from the database.
    public boolean deleteCustomer(int id, int customerID)
    throws RemoteException, DeadlockException, InvalidTransactionException {
        Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called");
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            lm.Lock(id, String.valueOf(customerID), LockType.WRITE);
        } catch (DeadlockException e) {
            throw new DeadlockException(id, String.valueOf(customerID));
        }
        
        Customer cust;
        
        if (ws.hasItem(customerID+"")){
            cust = (Customer) ws.getItem(customerID+"");
        } else {
            cust = (Customer) readData(id, Customer.getKey(customerID));
            
            if (cust != null) {
                cust = cust.getCopy();
                ws.sendCurrentState(customerID+"", cust);
                ws.addLocationToTxn(id,  customerID+"");
            }
        }
        
        txnManager.enlist(id, "customer");
        if (cust == null) {
            Trace.warn("RM::deleteCustomer(" + id + ", " + customerID
                       + ") failed--customer doesn't exist");
            return false;
        }
        else {
            // Un-reserve all reservations the customer had made
            RMHashtable reservationHT = cust.getReservations();
            for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {
                String reservedkey = (String) (e.nextElement());
                ReservedItem reserveditem = cust.getReservedItem(reservedkey);
                Trace.info("RM::deleteCustomer(" + id + ", " + customerID
                           + ") has reserved " + reserveditem.getKey() + " "
                           + reserveditem.getCount() + " times");
                
                System.out.println("Cancelling reservation: "
                                   + reserveditem.getKey());
                String itemType = reserveditem.getKey().split("\\-")[0];
                if (itemType.equals("room")) {
                    try {
                        rmHotel.cancelItem(id, reserveditem.getKey(), reserveditem.getCount());
                        txnManager.enlist(id, "hotel");
                    } catch (DeadlockException exc) {
                        Trace.error(exc.getMessage());
                        this.abort(id);
                        throw exc;
                    }
                }
                else if (itemType.equals("car")) {
                    try {
                        rmCar.cancelItem(id, reserveditem.getKey(), reserveditem.getCount());
                        txnManager.enlist(id, "car");
                    } catch (DeadlockException exc) {
                        Trace.error(exc.getMessage());
                        this.abort(id);
                        throw exc;
                    }
                }
                else if (itemType.equals("flight")) {
                    try {
                        rmFlight.cancelItem(id, reserveditem.getKey(), reserveditem.getCount());
                        txnManager.enlist(id, "flight");
                    } catch (DeadlockException exc) {
                        Trace.error(exc.getMessage());
                        this.abort(id);
                        throw exc;
                    }
                }
            }
            
            // remove the customer from the storage
            ws.addCommand(id, new CommandCustomerDelete(id, cust.getKey()));
            ws.deleteItem(customerID+"");
            
            Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded");
            return true;
        }
    }
    
    // Adds car reservation to this customer.
    public boolean reserveCar(int id, int customerID, String location)
    throws RemoteException, InvalidTransactionException, DeadlockException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            lm.Lock(id, String.valueOf(customerID), LockType.WRITE);
            txnManager.enlist(id, "customer");
        } catch (DeadlockException exc) {
            throw exc;
        }
        
        Customer cust;
        
        if (ws.hasItem(customerID+"")){
            cust = (Customer) ws.getItem(customerID+"");
        } else {
            cust = getCustomer(customerID);
            
            if (cust != null) {
                cust = cust.getCopy();
                ws.sendCurrentState(customerID+"", cust);
                ws.addLocationToTxn(id,  customerID+"");
            }
        }
        
        if (cust == null) {
            return false;
        }
        
        ReservedItem reservedItem = null;
        try {
            reservedItem = rmCar.reserveItem(id, cust.getKey(), location);
            txnManager.enlist(id, "car");
        } catch (DeadlockException exc) {
            Trace.error(exc.getMessage());
            this.abort(id);
            throw exc;
        }
        
        if (reservedItem != null) {
            
            cust.reserve(reservedItem.getKey(), reservedItem.getLocation(), reservedItem.getPrice());
            ws.addCommand(id, new CommandCustomerPut(id, cust.getKey(), cust));
            
            return true;
        }
        
        return false;
    }
    
    // Adds room reservation to this customer.
    public boolean reserveRoom(int id, int customerID, String location)
    throws RemoteException, InvalidTransactionException, DeadlockException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            lm.Lock(id, String.valueOf(customerID), LockType.WRITE);
            txnManager.enlist(id, "customer");
        } catch (DeadlockException exc) {
            throw exc;
        }
        
        Customer cust;
        
        if (ws.hasItem(customerID+"")){
            cust = (Customer) ws.getItem(customerID+"");
        } else {
            cust = getCustomer(customerID);
            
            if (cust != null) {
                cust = cust.getCopy();
                ws.sendCurrentState(customerID+"", cust);
                ws.addLocationToTxn(id,  customerID+"");
            }
        }
        
        if (cust == null) {
            return false;
        }
        
        ReservedItem reservedItem = null;
        try {
            reservedItem = rmHotel.reserveItem(id, cust.getKey(), location);
            txnManager.enlist(id, "hotel");
        } catch (DeadlockException exc) {
            Trace.error(exc.getMessage());
            this.abort(id);
            throw exc;
        }
        
        if (reservedItem != null) {
            cust.reserve(reservedItem.getKey(), reservedItem.getLocation(), reservedItem.getPrice());
            ws.addCommand(id, new CommandCustomerPut(id, cust.getKey(), cust));
            return true;
        }
        
        return false;
    }
    
    // Adds flight reservation to this customer.
    public boolean reserveFlight(int id, int customerID, int flightNum)
    throws RemoteException, InvalidTransactionException, DeadlockException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            lm.Lock(id, String.valueOf(customerID), LockType.WRITE);
            txnManager.enlist(id, "customer");
        } catch (DeadlockException exc) {
            throw exc;
        }
        
        Customer cust;
        
        if (ws.hasItem(customerID+"")){
            cust = (Customer) ws.getItem(customerID+"");
        } else {
            cust = getCustomer(customerID);
            
            if (cust != null) {
                cust = cust.getCopy();
                ws.sendCurrentState(customerID+"", cust);
                ws.addLocationToTxn(id,  customerID+"");
            }
        }
        
        if (cust == null) {
            return false;
        }
        
        String strflightNum = Integer.toString(flightNum);
        
        ReservedItem reservedItem = null;
        try {
            reservedItem = rmFlight.reserveItem(id, cust.getKey(), strflightNum);
            txnManager.enlist(id, "flight");
        } catch (DeadlockException exc) {
            Trace.error(exc.getMessage());
            this.abort(id);
            throw exc;
        }
        
        if (reservedItem != null) {
            cust.reserve(reservedItem.getKey(), reservedItem.getLocation(), reservedItem.getPrice());
            ws.addCommand(id, new CommandCustomerPut(id, cust.getKey(), cust));
            return true;
        }
        
        return false;
    }
    
    /* reserve an itinerary */
    public boolean itinerary(int id, int customer, Vector flightNumbers, String location, boolean car, boolean room)
    throws RemoteException, InvalidTransactionException, TransactionAbortedException, DeadlockException, ConnectException {
        
        //Unlike for the other operations, the transaction is aborted as soon as a component of the itinerary fails.
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        try {
            lm.Lock(id, String.valueOf(customer), LockType.WRITE);
            txnManager.enlist(id, "customer");
        } catch (DeadlockException exc) {
            throw exc;
        }
        
        Customer cust;
        
        if (ws.hasItem(customer+"")){
            cust = (Customer) ws.getItem(customer+"");
        } else {
            cust = getCustomer(customer);
            
            if (cust != null) {
                cust = cust.getCopy();
                ws.sendCurrentState(customer+"", cust);
                ws.addLocationToTxn(id,  customer+"");
            }
        }
        
        if (cust == null) {
            this.abort(id);
            return false;
        }
        
        System.out.println("BOOKING ITINERARY");
        
        for (int i = 0; i < flightNumbers.size(); i++) {
            int flightNumber = Integer.parseInt((String)flightNumbers.get(i));
            
            //the boolean is required when reserveFlight fails for reasons other than deadlocks
            //i.e. no more flights available
            boolean flightResult;
            try {
                flightResult = reserveFlight(id, customer, flightNumber);
            } catch (DeadlockException e) {
                Trace.error(e.getMessage());
                this.abort(id);
                throw new TransactionAbortedException(id);
            }
            
            if (!flightResult){
                this.abort(id);
                throw new TransactionAbortedException(id);
            }
        }
        
        if (car) {
            boolean carResult;
            try {
                carResult = reserveCar(id, cust.getID(), location);
            } catch (DeadlockException e) {
                Trace.error(e.getMessage());
                this.abort(id);
                throw new TransactionAbortedException(id);
            }
            
            if (!carResult) {
                this.abort(id);
                throw new TransactionAbortedException(id);
            }
        }
        
        if (room) {
            // Try to reserve a room at destination
            boolean roomResult;
            try {
                roomResult = reserveRoom(id, cust.getID(), location);
            } catch (DeadlockException e) {
                Trace.error(e.getMessage());
                this.abort(id);
                throw new TransactionAbortedException(id);
            }
            
            if (!roomResult) {
                this.abort(id);
                throw new TransactionAbortedException(id);
            }
        }
        
        return true;
    }
    
    @Override
    public int start() throws RemoteException {
        return txnManager.start();
    }
    
    @Override
    public boolean commit(int id) throws RemoteException, InvalidTransactionException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        System.out.println("Committing transaction: " + id);
        return txnManager.commitPhase1(id);
    }
    
    public boolean commitRecovery(int id, String rm) throws RemoteException, InvalidTransactionException {
        if (!txnManager.isActiveTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        System.out.println("RECOVERING " + rm +"... Committing transaction: " + id);
        return txnManager.commitPhase2(id);
    }
    
    public void abortRecovery(int id, String rm) throws RemoteException, InvalidTransactionException {
        if (!txnManager.isActiveTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        System.out.println("Aborting transaction: " + id);
        
        if (rm.equals("car"))
            rmCar.abort(id);
        else if (rm.equals("flight"))
            rmFlight.abort(id);
        else if (rm.equals("hotel"))
            rmHotel.abort(id);
        else if (rm.equals("customer")){
            lm.UnlockAll(id);
            ws.abort(id);
        }
    }
    
    @Override
    public void abort(int id) throws RemoteException, InvalidTransactionException, ConnectException {
        
        if (!txnManager.isValidTransaction(id)) {
            throw new InvalidTransactionException(id);
        }
        
        System.out.println("Aborting transaction: " + id);
        Vector<String> rms = txnManager.abort(id);
        
        
        for(String rm: rms) {
            if (rm.equals("car"))
                rmCar.abort(id);
            else if (rm.equals("flight"))
                rmFlight.abort(id);
            else if (rm.equals("hotel"))
                rmHotel.abort(id);
            else if (rm.equals("customer")){
                lm.UnlockAll(id);
                ws.abort(id);
            }
        }
    }
    
    @Override
    public boolean shutdown() throws RemoteException, ConnectException {
        
        if (txnManager.canShutdown()) {
            
            System.out.println("SHUTTING SYSTEM DOWN");
            rmCar.shutDown();
            rmHotel.shutDown();
            rmFlight.shutDown();
            
            try{
                // Unregister ourself
                registry.unbind("Group23_ResourceManager");
                
                // Unexport; this will also remove us from the RMI runtime
                UnicastRemoteObject.unexportObject(this, true);
                txnKillerThread.stop();
                System.out.println("Shutting Down!!! Have a good night");
            }
            catch(Exception e){}
            return true;
        }
        
        System.out.println("Can't shut system down since transactions are still alive");
        return false;
    }
    
    public void exit(){
        try{
            // Unregister ourself
            registry.unbind("Group23_ResourceManager");
            
            // Unexport; this will also remove us from the RMI runtime
            UnicastRemoteObject.unexportObject(this, true);
            txnKillerThread.stop();
            System.out.println("Shutting Down!!! Have a good night");
        }
        catch(Exception e){}
    }
    
    @Override
    public void setCrashCondition(Crash crashCondition, String rmName) throws RemoteException {
        if (rmName.equals("tm"))
            txnManager.setCrashCondition(crashCondition);
        else if (rmName.equals("car"))
            rmCar.setCrashCondition(crashCondition);
        else if (rmName.equals("flight"))
            rmFlight.setCrashCondition(crashCondition);
        else if (rmName.equals("hotel"))
            rmHotel.setCrashCondition(crashCondition);
        else if (rmName.equals("customer"))
            this.crashCondition = crashCondition;
    }
    
    @Override
    public boolean getTransactionFinalAction(int xid) throws RemoteException {
        return txnManager.getTransactionFinalAction(xid);
    }
    
    @Override
    public int prepare(int xid) throws RemoteException, InvalidTransactionException {
        //if (crashCondition == Crash.P_B_SAVEWS) System.exit(42);
        
        SerializeUtils.saveToDisk(ws, getWorkingSetFileName(xid));
        
        //if (crashCondition == Crash.P_A_SAVEWS) System.exit(42);
        
        return 1; 
    }
    
    private String getCommittedFileName() {
        return "/tmp/Group23/customerdb." + masterRecord.getCommittedIndex();
    }
    
    private String getWorkingFileName() {
        return "/tmp/Group23/customerdb." + masterRecord.getWorkingIndex();
    }
    
    private String getMasterRecordFileName() {
        return "/tmp/Group23/customerdb.mr";
    }
    
    private String getWorkingSetFileName(int xid) {
        return "/tmp/Group23/customer_" + xid + ".ws";
    }
    
    synchronized public boolean commitCustomer(int id){
        //if (crashCondition == Crash.P_A_COMMITRECV) System.exit(42);
        
        ws.commit(id, this);
        
        SerializeUtils.saveToDisk(m_itemHT, getWorkingFileName());
        masterRecord.setLastXid(id);
        masterRecord.swap();
        SerializeUtils.saveToDisk(masterRecord, getMasterRecordFileName());
        SerializeUtils.deleteFile("/tmp/Group23/customer_" + id + ".ws");
        
        return lm.UnlockAll(id);
    }
    
    public void abortCustomer(int id) {
        ws.abort(id);
        
        SerializeUtils.deleteFile("/tmp/Group23/customer_" + id + ".ws");
        
        lm.UnlockAll(id);		
    } 	
    
    private void recoverMiddleware() {
        
        TransactionManager temp = txnManager.retrieveTransactionManager();
        if (temp == null) return;
        txnManager = temp;
        
        txnManager.setRmCustomer(this);
        
        File folder = new File("/tmp/Group23");
        for (File f: folder.listFiles()) {
            if (f.getName().startsWith("customer") && f.getName().endsWith(".ws")) {
                ws = (WorkingSet<Customer>)SerializeUtils.loadFromDisk(f.getAbsolutePath());
            }
        }
        
        Vector<Integer> xids = txnManager.getAllActiveTransactions();
        for (Integer xid: xids){
            TransactionStatus status = txnManager.getTransactionStatus(xid);
            if (status == TransactionStatus.NOTCOMMITTED)
                try {
                    abort(xid);
                } catch (Exception e) {	} 
            else if (status == TransactionStatus.PHASE1)
                txnManager.commitPhase1(xid);
            else if (status == TransactionStatus.PHASE2)
                txnManager.commitPhase2(xid);
        }
        
    }
    
}
