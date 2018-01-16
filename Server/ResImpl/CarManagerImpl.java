package carcode.ResImpl;

import java.io.File;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import servercode.ResImpl.CommandDelete;
import servercode.ResImpl.CommandPut;
import servercode.ResImpl.Crash;
import servercode.ResImpl.CrashException;
import servercode.ResImpl.InvalidTransactionException;
import servercode.ResImpl.MasterRecord;
import servercode.ResImpl.RMHashtable;
import servercode.ResImpl.ReservableItem;
import servercode.ResImpl.ReservedItem;
import servercode.ResImpl.SerializeUtils;
import servercode.ResImpl.Trace;
import servercode.ResImpl.WorkingSet;
import servercode.ResInterface.ItemManager;
import servercode.ResInterface.ResourceManager;
import LockManager.DeadlockException;
import LockManager.LockManager;
import LockManager.LockType;

public class CarManagerImpl implements ItemManager {
	 
	public static Registry registry;
	protected RMHashtable carTable = new RMHashtable();
	private LockManager lm = new LockManager();
	private WorkingSet<Car> ws = new WorkingSet<Car>();
	private static ResourceManager middleware = null;
	
	private MasterRecord masterRecord = new MasterRecord();
	private Crash crashCondition;
	
  
	public static void main(String args[]) {
    	
        int port = 5006;
        
        CarManagerImpl obj = new CarManagerImpl();        
        String middlewareHost;
        int middlewarePort;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        else if (args.length == 3) {
        	port = Integer.parseInt(args[0]);
        	middlewareHost = args[1];
        	middlewarePort = Integer.parseInt(args[2]);
            try {
                Registry registry = LocateRegistry.getRegistry(middlewareHost, middlewarePort);
                middleware = (ResourceManager) registry.lookup("Group23_ResourceManager");
                if (middleware != null) {
                    System.out.println("Successfully connected to the middleware");
                }
                else {
                    System.out.println("Connection to the middleware failed");
                    System.exit(1);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {            
        	System.err.println("Usage: java carcode.ResImpl.CarManagerImpl <rmi port> <middleware host> <middleware port>");
        	System.exit(1);
        }
        
        try 
        {
            // create a new Server object
            // dynamically generate the stub (client proxy)
            ItemManager rm = (ItemManager) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            registry = LocateRegistry.getRegistry(port);
            registry.rebind("Group23_CarManager", rm);
            
            if (middleware != null){
                middleware.rebind("car");
            	obj.recoverTransactionStatus();
            }
            System.err.println("Car Server ready");
        } 
        catch (Exception e) 
        {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
    }
    
    public CarManagerImpl() {
    	File mrFile = new File(getMasterRecordFileName());
    	if (mrFile.exists()) {
    		masterRecord = (MasterRecord) SerializeUtils.loadFromDisk(getMasterRecordFileName());
    		carTable = (RMHashtable) SerializeUtils.loadFromDisk(getCommittedFileName());
    	}
    }
    
    @Override
    public boolean addItem(int id, String location, int quantity, int price)
        throws RemoteException, DeadlockException {

    	//Acquire write lock
    	try {
    		lm.Lock(id, location, LockType.WRITE);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, location);
        }  
    	
    	Car curObj;
    	
    	if (ws.hasItem(location)){
    		curObj = (Car) ws.getItem(location);    		
    	} else {
    		curObj = fetchCar(id, Car.getKey(location));    
    		
    		if (curObj != null) {
    			curObj = curObj.getCopy();
    		}
      	}
    	
        
        if (curObj == null) {
            // If Car doesn't exist, create it and add it to 
            // the manager's hash table.
            Car newObj = new Car(location, quantity, price);
                        
            ws.addCommand(id, new CommandPut(id,newObj.getKey(), newObj));
            //putCar(id, newObj.getKey(), newObj);
            ws.sendCurrentState(newObj.getLocation(), newObj);
            ws.addLocationToTxn(id, location);
            
            Trace.info("RM::addCars(" + id + ") created new location "
                + location + ", count=" + quantity + ", price=$" + price);
        }
        else {
            // If the Car already exists, update its quantity (by adding
            // the new quantity) and update its price (only if the new price
            // is positive).
                      
            curObj.setCount(curObj.getCount() + quantity);
            if (price > 0) {
                curObj.setPrice(price);
            }
            
            ws.addCommand(id, new CommandPut(id, Car.getKey(location), curObj));
            ws.sendCurrentState(curObj.getLocation(), curObj);            
            //putCar(id, Car.getKey(location), curObj);
            ws.addLocationToTxn(id, location);
            
            Trace.info("RM::addCars(" + id + ") modified existing location "
                + location + ", count=" + curObj.getCount() + ", price=$"
                + curObj.getPrice());
        }

        return true;
    }

    @Override
    public boolean deleteItem(int id, String location) throws RemoteException, DeadlockException {
    	
    	try {
    		lm.Lock(id, location, LockType.WRITE);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, location);
        }
    	
    	String itemId = Car.getKey(location);
    	Car curObj;
    	
    	if (ws.hasItem(location)){
    		curObj = (Car) ws.getItem(location);    		
    	} else {
    		curObj = fetchCar(id, itemId);    		
    		if (curObj != null) {
    			curObj = curObj.getCopy();
    		}
    	}

        if (curObj == null) {
            Trace.warn("RM::deleteItem(" + id + ", " + itemId
                + ") failed--item doesn't exist"); 
            return false;
        }
        else {
            if (curObj.getReserved() == 0) {            	
            	ws.sendCurrentState(curObj.getLocation(), curObj);
            	
            	ws.deleteItem(curObj.getLocation()); //the item stays in ws but its current state is set to null
            	
            	ws.addCommand(id, new CommandDelete(id, curObj.getKey()));
                //deleteCar(id, curObj.getKey());
            	ws.addLocationToTxn(id, location);
            	
                Trace.info("RM::deleteItem(" + id + ", " + itemId+ ") item deleted");
                return true;
            }
            else {
                Trace.info("RM::deleteItem("+ id+ ", "+ itemId+ ") item can't be deleted because some customers reserved it");
                return false;
            }
        } 
    }

    @Override
    public int queryItemQuantity(int id, String location) throws RemoteException, DeadlockException {
    	
    	try {
    		lm.Lock(id, location, LockType.READ);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, location);
        }    
    	
    	Car curObj;
    	
    	if (ws.hasItem(location)){
    		curObj = (Car) ws.getItem(location);    		
    	} else {
    		curObj = fetchCar(id, Car.getKey(location));	
    	}
    	
        if (curObj != null) {
            return curObj.getCount();
        }

        return 0;
    }

    @Override
    public int queryItemPrice(int id, String location) throws RemoteException, DeadlockException {
    	
    	try {
    		lm.Lock(id, location, LockType.READ);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, location);
        }  
    	
    	Car curObj;
    	
    	if (ws.hasItem(location)){
    		curObj = (Car) ws.getItem(location);    		
    	} else {
    		curObj = fetchCar(id, Car.getKey(location));	
    	}
    	        
        if (curObj != null) {
            return curObj.getPrice();
        }
        return 0;   
    }


    @Override
    public ReservedItem reserveItem(int id, String customerId, String location)
        throws RemoteException, DeadlockException {

    	try {
    		lm.Lock(id, location, LockType.WRITE);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, location);
        }  
    	
    	Car curObj;
    	
    	if (ws.hasItem(location)){
    		curObj = (Car) ws.getItem(location);    		
    	} else {
    		curObj = fetchCar(id, Car.getKey(location));
    		if (curObj != null) {
    			curObj = curObj.getCopy();
    			ws.sendCurrentState(curObj.getLocation(), curObj);
        		ws.addLocationToTxn(id,  location);
    		}
    	}
    	
        if (curObj == null) {        	
            Trace.warn("RM::reserveCar( " + id + ", " + customerId + ", " + location + ") failed--item doesn't exist"); 
            return null;
        }
        else if (curObj.getCount() == 0) {
            Trace.warn("RM::reserveCar( " + id + ", " + customerId + ", " + location + ") failed--No more items");
            return null;
        }
        else {            	
            String key = Car.getKey(location);

            // decrease the number of available items in the storage
            curObj.setCount(curObj.getCount() - 1);
            curObj.setReserved(curObj.getReserved() + 1);

            ws.addCommand(id, new CommandPut(id, key, (ReservableItem)curObj));

            Trace.info("RM::reserveCar( " + id + ", " + customerId + ", " + key + ") succeeded");  
            return new ReservedItem(key, curObj.getLocation(), 1, curObj.getPrice());
        }
    }

    public boolean cancelItem(int id, String carKey, int count)
        throws RemoteException, DeadlockException {
    	    	   
    	String segments[] = carKey.split("-");    	
    	String location = segments[1];
    	
    	try {
    		lm.Lock(id, location, LockType.WRITE);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, location);
        }  
    	
    	Car curObj;
    	
    	if (ws.hasItem(location)){
    		curObj = (Car) ws.getItem(location);    		
    	} else {
    		curObj = fetchCar(id, carKey);    		
    		if (curObj != null) {
    			curObj = curObj.getCopy();
    			ws.sendCurrentState(curObj.getLocation(), curObj);
        		ws.addLocationToTxn(id,  location);
    		}
    	}
    	    	
    	if (curObj == null) {
            System.out.println("Car " + carKey + " can't be cancelled because none exists");
            return false;
        }
    	
        System.out.println("cancelItem( " + id + ", " + carKey + ", " + count + " )");
        
        //adjust available quantity
        curObj.setCount(curObj.getCount() + count);
        curObj.setReserved(curObj.getReserved() - count);
        
        ws.addCommand(id, new CommandPut(id, curObj.getKey(), (ReservableItem)curObj));

        return true;
    }

    private Car fetchCar(int id, String itemId) {
        synchronized (carTable) {
            return (Car)carTable.get(itemId);
        }
    }
    
    public void putCar(int id, String itemId, Car car) {
        synchronized (carTable) {
            carTable.put(itemId, car);            
        }
    }

    public void deleteCar(int id, String itemId) {
        synchronized (carTable) {
            carTable.remove(itemId);
        }
    }

	@Override
	synchronized public boolean commit(int id) throws RemoteException {
		if (crashCondition == Crash.P_A_COMMITRECV) Runtime.getRuntime().exit(42);
		
		ws.commit(id, this);
		
		SerializeUtils.saveToDisk(carTable, getWorkingFileName());
		masterRecord.setLastXid(id);
		masterRecord.swap();
		SerializeUtils.saveToDisk(masterRecord, getMasterRecordFileName());
		SerializeUtils.deleteFile("/tmp/Group23/car_" + id + ".ws");
		
		return lm.UnlockAll(id);
	}

	@Override
	public void abort(int id) throws RemoteException {
		ws.abort(id);
		
		SerializeUtils.deleteFile("/tmp/Group23/car_" + id + ".ws");
		
		lm.UnlockAll(id);		
	}

	
	public void shutDown() throws RemoteException{
		
		try{
	        // Unregister ourself
	        registry.unbind("Group23_CarManager");
	        
	        // Unexport; this will also remove us from the RMI runtime
	        UnicastRemoteObject.unexportObject(this, true);

	        System.out.println("Shutting Down!!! Have a good night");
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }		
	}
	

	@Override
	public int prepare(int xid) throws RemoteException, InvalidTransactionException {
		if (crashCondition == Crash.P_B_SAVEWS) Runtime.getRuntime().exit(42);
		
		SerializeUtils.saveToDisk(ws, getWorkingSetFileName(xid));
		
		if (crashCondition == Crash.P_A_SAVEWS) Runtime.getRuntime().exit(42);
		
		return 1; 
	}

	private String getCommittedFileName() {
		return "/tmp/Group23/cardb." + masterRecord.getCommittedIndex();
	}

	private String getWorkingFileName() {
		return "/tmp/Group23/cardb." + masterRecord.getWorkingIndex();
	}
	
	private String getMasterRecordFileName() {
		return "/tmp/Group23/cardb.mr";
	}

	private String getWorkingSetFileName(int xid) {
		return "/tmp/Group23/car_" + xid + ".ws";
	}

	@Override
	public void setCrashCondition(Crash crashCondition) throws RemoteException {
		this.crashCondition = crashCondition;
	}
	
	private void recoverTransactionStatus() throws InvalidTransactionException {
		File folder = new File("/tmp/Group23");
		for (File f: folder.listFiles()) {
			if (f.getName().startsWith("car") && f.getName().endsWith(".ws")) {
				try {
					ws = (WorkingSet<Car>)SerializeUtils.loadFromDisk(f.getAbsolutePath());
					Set<Integer> xids = ws.getAllTransactions();
					for (int xid: xids) {
						if (middleware.getTransactionFinalAction(xid)) {
							middleware.commitRecovery(xid, "car");
						}
						else {
							middleware.abortRecovery(xid, "car");
						}
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

} 
