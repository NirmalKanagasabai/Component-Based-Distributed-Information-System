package flightcode.ResImpl;


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

public class FlightManagerImpl implements ItemManager {
    
	public static Registry registry;
    protected RMHashtable flightTable = new RMHashtable();
    
    private LockManager lm = new LockManager();
    private WorkingSet<Flight> ws = new WorkingSet<Flight>();
    private Crash crashCondition;
	private static ResourceManager middleware = null;
	private MasterRecord masterRecord = new MasterRecord();

	
    public static void main(String args[]) {
    	
        int port = 5007;
        FlightManagerImpl obj = new FlightManagerImpl();
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
        	System.err.println("Usage: java flightcode.ResImpl.FlightManagerImpl <rmi port> [<middleware host> <middleware port>]");
        	System.exit(1);
        }

        try 
        {
            // create a new Server object
            // dynamically generate the stub (client proxy)
            ItemManager rm = (ItemManager) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            registry = LocateRegistry.getRegistry(port);
            registry.rebind("Group23_FlightManager", rm);
            if (middleware != null){
                middleware.rebind("flight");
            	obj.recoverTransactionStatus();
            }

            System.err.println("Flight Server ready");
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
    
    public FlightManagerImpl() {
    	File mrFile = new File(getMasterRecordFileName());
    	if (mrFile.exists()) {
    		masterRecord = (MasterRecord) SerializeUtils.loadFromDisk(getMasterRecordFileName());
    		flightTable = (RMHashtable) SerializeUtils.loadFromDisk(getCommittedFileName());
    	}
    }
    
    @Override
    public boolean addItem(int id, String flightNum, int flightSeats, int flightPrice)
        throws RemoteException, DeadlockException {
    	
    	//Acquire write lock
    	try {
    		lm.Lock(id, flightNum, LockType.WRITE);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, flightNum);
        }
    	
    	int nflightNum = Integer.valueOf(flightNum);
    	Flight curObj;
    	
    	if (ws.hasItem(flightNum)){
    		curObj = (Flight) ws.getItem(flightNum);    		
    	} else {
    		curObj = fetchFlight(id, Flight.getKey(nflightNum));    
    		
    		if (curObj != null) {
    			curObj = curObj.getCopy();
    		}
      	} 
    	
        if (curObj == null) {
            // If Flight doesn't exist, create it and add it to 
            // the manager's hash table.
            Flight newObj = new Flight(nflightNum, flightSeats, flightPrice);
            
            ws.addCommand(id, new CommandPut(id, newObj.getKey(), newObj));
            ws.sendCurrentState(newObj.getLocation(), newObj);
            ws.addLocationToTxn(id, flightNum);
            
            Trace.info("RM::addFlight(" + id + ") created new flight "
                    + flightNum + ", seats=" + flightSeats + ", price=$" + flightPrice);
        }
        else {
            // If the Flight already exists, update its quantity (by adding
            // the new quantity) and update its price (only if the new price
            // is positive).
        	                    	
            curObj.setCount(curObj.getCount() + flightSeats);
            if (flightPrice > 0) {
                curObj.setPrice(flightPrice);
            }
            
            ws.addCommand(id, new CommandPut(id, Flight.getKey(nflightNum), curObj));
            ws.sendCurrentState(curObj.getLocation(), curObj);            
            ws.addLocationToTxn(id, flightNum);
            
            Trace.info("RM::addFlight(" + id + ") modified existing flight "
                    + flightNum + ", seats=" + curObj.getCount() + ", price=$"
                    + curObj.getPrice());
        }
                
        return true;
    }

    @Override
    public boolean deleteItem(int id, String flightNum) throws RemoteException, DeadlockException {
    	
    	try {
    		lm.Lock(id, flightNum, LockType.WRITE);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, flightNum);
        }
    	
    	int nflightNum = Integer.valueOf(flightNum);
    	
    	String itemId = Flight.getKey(nflightNum);
        Flight curObj;
        
        if (ws.hasItem(flightNum)){
    		curObj = (Flight) ws.getItem(flightNum);    		
    	} else {
    		curObj = fetchFlight(id, itemId);    		
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
            	ws.addLocationToTxn(id, flightNum);
            	
                Trace.info("RM::deleteItem(" + id + ", " + itemId + ") item deleted");
                return true;
            }
            else {
            	Trace.info("RM::deleteItem("+ id+ ", "+ itemId + ") item can't be deleted because some customers reserved it");
                return false;
            }
        } 
    }

    @Override
    public int queryItemQuantity(int id, String flightNum) throws RemoteException, DeadlockException {
    	
    	try {
    		lm.Lock(id, flightNum, LockType.READ);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, flightNum);
        }  
    	
    	int nflightNum = Integer.valueOf(flightNum);
    	Flight curObj;
    	
    	if (ws.hasItem(flightNum)){
    		curObj = (Flight) ws.getItem(flightNum);    		
    	} else {
    		curObj = fetchFlight(id, Flight.getKey(nflightNum));	
    	}
        if (curObj != null) {
            return curObj.getCount();
        }
        
        return 0;
    }

    @Override
    public int queryItemPrice(int id, String flightNum) throws RemoteException, DeadlockException {
    	
    	try {
    		lm.Lock(id, flightNum, LockType.READ);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, flightNum);
        }  
    	
    	int nflightNum = Integer.valueOf(flightNum);
    	
    	Flight curObj;
    	
    	if (ws.hasItem(flightNum)){
    		curObj = (Flight) ws.getItem(flightNum);    		
    	} else {
    		curObj = fetchFlight(id, Flight.getKey(nflightNum));	
       	}
    	
        if (curObj != null) {
            return curObj.getPrice();
        }
        return 0;   
    }
   
    @Override
    public ReservedItem reserveItem(int id, String customerId, String flightNum)
        throws RemoteException, DeadlockException {
    	
    	try {
    		lm.Lock(id, flightNum, LockType.WRITE);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, flightNum);
        }  
    	
    	int nflightNum = Integer.valueOf(flightNum);
    	
    	Flight curObj;
    	
    	if (ws.hasItem(flightNum)){
    		curObj = (Flight) ws.getItem(flightNum);    		
    	} else {
    		curObj = fetchFlight(id, Flight.getKey(nflightNum));
    		if (curObj != null) {
    			curObj = curObj.getCopy();
    			ws.sendCurrentState(curObj.getLocation(), curObj);
        		ws.addLocationToTxn(id,  flightNum);
    		}
    	}
        
        if (curObj == null) {
        	Trace.warn("RM::reserveItem( " + id + ", " + customerId + ", " + flightNum + ") failed--item doesn't exist");        	
            return null;
        }
        else if (curObj.getCount() == 0) {
        	Trace.warn("RM::reserveItem( " + id + ", " + customerId + ", " + flightNum + ") failed--No more items");        	
            return null;
        }
        else {               	
            String key = Flight.getKey(nflightNum);
            
            // decrease the number of available items in the storage
            curObj.setCount(curObj.getCount() - 1);
            curObj.setReserved(curObj.getReserved() + 1);

            ws.addCommand(id, new CommandPut(id, key, (ReservableItem)curObj));
            
            Trace.info("RM::reserveItem( " + id + ", " + customerId + ", " + key + ") succeeded");            
            
            return new ReservedItem(key, curObj.getLocation(), 1, curObj.getPrice());
        }
    } 
    
    public boolean cancelItem(int id, String flightKey, int count)
    	throws RemoteException, DeadlockException {
    	
    	System.out.println("cancelItem( " + id + ", " + flightKey + ", " + count + " )");
    	
    	String segments[] = flightKey.split("-");     	
    	String flightNum = segments[1];
    	
    	try {
    		lm.Lock(id, flightNum, LockType.WRITE);    		
    	} catch (DeadlockException deadlock) {
            throw new DeadlockException(id, flightNum);
        } 
    	    	
    	Flight curObj;
    	
    	if (ws.hasItem(flightNum)){
    		curObj = (Flight) ws.getItem(flightNum);    		
    	} else {
    		curObj = fetchFlight(id, flightKey);    		
    		if (curObj != null) {
    			curObj = curObj.getCopy();
    			ws.sendCurrentState(curObj.getLocation(), curObj);
        		ws.addLocationToTxn(id,  flightNum);
    		}
    	}
    	
    	if (curObj == null) {
    		Trace.warn("Flight " + flightKey + " can't be cancelled because item doesn't exists");
    		return false;
    	}
    	
    	//adjust available quantity
    	curObj.setCount(curObj.getCount() + count);
    	curObj.setReserved(curObj.getReserved() - count);

    	ws.addCommand(id, new CommandPut(id, flightKey, (ReservableItem)curObj));
    	
    	Trace.info("Reservation of flight " + flightKey + " cancelled.");
    	
    	return true;
    }
            
    private Flight fetchFlight(int id, String itemId) {
        synchronized (flightTable) {
            return (Flight)flightTable.get(itemId);
        }
    }
    
    public void putFlight(int id, String itemId, Flight Flight) {
        synchronized (flightTable) {
        	flightTable.put(itemId, Flight);            
        }
    }
    
    public void deleteFlight(int id, String itemId) {
        synchronized (flightTable) {
        	flightTable.remove(itemId);
        }
    }

	@Override
	synchronized public boolean commit(int id) throws RemoteException {
		if (crashCondition == Crash.P_A_COMMITRECV) Runtime.getRuntime().exit(43);
		
		ws.commit(id, this);
		
		SerializeUtils.saveToDisk(flightTable, getWorkingFileName());
		masterRecord.setLastXid(id);
		masterRecord.swap();
		SerializeUtils.saveToDisk(masterRecord, getMasterRecordFileName());
		SerializeUtils.deleteFile("/tmp/Group23/flight_" + id + ".ws");
		
		return lm.UnlockAll(id);
	}

	@Override
	public void abort(int id) throws RemoteException {
		ws.abort(id);
		SerializeUtils.deleteFile("/tmp/Group23/flight_" + id + ".ws");
		lm.UnlockAll(id);
	}
	
	public void recoverItemState(int id, ReservableItem flightBackup){
    	Flight curObj = fetchFlight(id, flightBackup.getKey());
    	
    	curObj.setCount(flightBackup.getCount());
    	curObj.setPrice(flightBackup.getPrice());
    	curObj.setReserved(flightBackup.getReserved());
    }
	
	public void shutDown() throws RemoteException{
		try{
	        // Unregister ourself
	        registry.unbind("Group23_FlightManager");

	        // Unexport; this will also remove us from the RMI runtime
	        UnicastRemoteObject.unexportObject(this, true);

	        System.out.println("Shutting Down!!! Have a good night");
	    }
	    catch(Exception e){}
	}

	@Override
	public int prepare(int xid) throws RemoteException, InvalidTransactionException {
		if (crashCondition == Crash.P_B_SAVEWS) Runtime.getRuntime().exit(42);
		
		SerializeUtils.saveToDisk(ws, getWorkingSetFileName(xid));
		
		if (crashCondition == Crash.P_A_SAVEWS) Runtime.getRuntime().exit(42);
		
		return 1;
	}

	private String getCommittedFileName() {
		return "/tmp/Group23/flightdb." + masterRecord.getCommittedIndex();
	}

	private String getWorkingFileName() {
		return "/tmp/Group23/flightdb." + masterRecord.getWorkingIndex();
	}
	
	private String getMasterRecordFileName() {
		return "/tmp/Group23/flightdb.mr";
	}

	private String getWorkingSetFileName(int xid) {
		return "/tmp/Group23/flight_" + xid + ".ws";
	}

	@Override
	public void setCrashCondition(Crash crashCondition) throws RemoteException {
		this.crashCondition = crashCondition;
	}
	
	private void recoverTransactionStatus() throws InvalidTransactionException {
		File folder = new File("/tmp/Group23");
		for (File f: folder.listFiles()) {
			if (f.getName().startsWith("flight") && f.getName().endsWith(".ws")) {
				try {
					ws = (WorkingSet<Flight>)SerializeUtils.loadFromDisk(f.getAbsolutePath());
					Set<Integer> xids = ws.getAllTransactions();
					for (int xid: xids) {
						if (middleware.getTransactionFinalAction(xid)) {
							middleware.commitRecovery(xid, "flight");
						}
						else {
							middleware.abortRecovery(xid, "flight");
						}
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
