package servercode.ResImpl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import servercode.ResInterface.ItemManager;
import servercode.ResInterface.ResourceManager;

public class TransactionManager implements Serializable {

	private HashMap<Integer, TransactionStatus> xidsToStatus = new HashMap<Integer, TransactionStatus>();

	private ItemManager rmCar;
	private ItemManager rmFlight;
	private ItemManager rmHotel;
	private transient ResourceManager rmCustomer;

	//Same as hashMap but thread safe
	//KEY=> TransactionId  Value=>vector of RMs (RMs are represented as strings)
	private ConcurrentHashMap<Integer, Vector<String>> xidsToRMNames = new ConcurrentHashMap<Integer, Vector<String>>();
	private ConcurrentHashMap<Integer,	Long> timeToLiveMap = new ConcurrentHashMap<Integer, Long>();
	private HashMap<Integer, Boolean> transactionDecision = new HashMap<Integer, Boolean>();
	private int numberOfTransactions = 0;
	private static TransactionManager instance = null;

	private long TIMEOUT = 120000;
	private transient Crash crashCondition;

	////Singleton class so private constructor
	private TransactionManager(ItemManager carRm, ItemManager flightRm,
			ItemManager hotelRm, ResourceManager rm) {
		super();
		this.rmCar = carRm;
		this.rmFlight = flightRm;
		this.rmHotel = hotelRm;
		this.rmCustomer = rm;
	}

	public static TransactionManager getInstance(ItemManager carRm, ItemManager flightRm, ItemManager hotelRm, ResourceManager rm) {
		if(instance == null) {
			instance = new TransactionManager(carRm, flightRm, hotelRm, rm);
	    }

		return instance;
	}

	public int start(){
		int id = 1;

		numberOfTransactions++;

		//Generate random numbers until we get one that is not already used
		while (xidsToRMNames.contains(id) || id == 1 || transactionDecision.containsKey(id)) {
			id = new Random().nextInt(10000) + 1; //+1 because can return 0
		}
		xidsToRMNames.put(id, new Vector<String>());
		timeToLiveMap.put(id, System.currentTimeMillis());
		xidsToStatus.put(id, TransactionStatus.NOTCOMMITTED);

		return id;
	}

	//Adds a RM as used by transaction
	public void enlist(int id, String rm) {
		Vector<String> v = xidsToRMNames.get(id);

		if (!v.contains(rm)) {
			v.add(rm);
		}

		xidsToRMNames.put(id, v);
		timeToLiveMap.put(id, System.currentTimeMillis());
	}

	public boolean commitPhase1(int xid) {
		Vector<String> rms = xidsToRMNames.get(xid);
		if (rms == null) rms = new Vector<String>();

		xidsToStatus.put(xid, TransactionStatus.PHASE1);

		int answers = 0;

		logTransactionManager();
		if (crashCondition == Crash.C_B_VR) Runtime.getRuntime().exit(42);

		for (String rm: rms) {
			if (rm.equals("car")) {
				try {
					answers += rmCar.prepare(xid);
				}
				catch (RemoteException | InvalidTransactionException e) {

				}
			}
			if (rm.equals("hotel")) {
				try {
					answers += rmHotel.prepare(xid);
				}
				catch (RemoteException | InvalidTransactionException e) {

				}
			}
			if (rm.equals("flight")) {
				try {
					answers += rmFlight.prepare(xid);
				}
				catch (RemoteException | InvalidTransactionException e) {

				}
			}
			if (rm.equals("customer")) {
				try {
					answers += rmCustomer.prepare(xid);
				}
				catch (RemoteException | InvalidTransactionException e) {

				}
			}
			if (crashCondition == Crash.C_A_ONEREPLY) Runtime.getRuntime().exit(42);
		}

		if (crashCondition == Crash.C_A_ALLREPLY) Runtime.getRuntime().exit(42);

		boolean result = answers == rms.size();
		transactionDecision.put(xid, result);  //DECISION IS TAKEN!!!
		xidsToStatus.put(xid, TransactionStatus.PHASE2);

		if (crashCondition == Crash.C_A_DECISION) Runtime.getRuntime().exit(42);

		return commitPhase2(xid);
	}

	public boolean commitPhase2(int xid) {
		Vector<String> rms = xidsToRMNames.get(xid);
		if (rms == null) rms = new Vector<String>();

		logTransactionManager();

		boolean result = transactionDecision.get(xid);

		if (result) {
			for(String rm: rms) {
				if (rm.equals("car")) {
					try {
						rmCar.commit(xid);
					} catch (RemoteException e) {
						System.out.println("The car manager could not commit!!!");
					}
				}
				if (rm.equals("hotel")) {
					try {
						rmHotel.commit(xid);
					} catch (RemoteException e) {
						System.out.println("The hotel manager could not commit!!!");
					}
				}
				if (rm.equals("flight")) {
					try {
						rmFlight.commit(xid);
					} catch (RemoteException e) {
						System.out.println("The flight manager could not commit!!!");
					}
				}
				if (rm.equals("customer")) {
					try {
						rmCustomer.commitCustomer(xid);
					} catch (RemoteException e) {
						System.out.println("The customer manager could not commit!!!");
					}
				}

				if (crashCondition == Crash.C_A_ONECOMMIT) Runtime.getRuntime().exit(42);
			}
		}
		else {
			for(String rm: rms) {
				if (rm.equals("car")) {
					try {
						rmCar.abort(xid);
					} catch (RemoteException e) {
						System.out.println("The car manager is not available!!!");
					}
				}
				if (rm.equals("hotel")) {
					try {
						rmHotel.abort(xid);
					} catch (RemoteException e) {
						System.out.println("The hotel manager is not available!!!");
					}
				}
				if (rm.equals("flight")) {
					try {
						rmFlight.abort(xid);
					} catch (RemoteException e) {
						System.out.println("The flight manager is not available!!!");
					}
				}
				if (rm.equals("customer")) {
					try {
						rmCustomer.abortCustomer(xid);
					} catch (RemoteException e) {
						System.out.println("The customer manager is not available!!!");
					}
				}
			}
		}

		if (crashCondition == Crash.C_A_ALLCOMMIT) Runtime.getRuntime().exit(42);

        xidsToRMNames.remove(xid);
		timeToLiveMap.remove(xid);
		xidsToStatus.remove(xid);

		deleteTransactionManagerLog();

		return result;
	}

//	public boolean commitRecovery(int xid, String rm) {
//		if (transactionDecision.get(xid)) { //We have to commit
//			if (rm.equals("car")) {
//				try {
//					rmCar.commit(xid);
//				} catch (RemoteException e) {
//					System.out.println("The car manager could not commit!!!");
//					return false;
//				}
//			}else if (rm.equals("hotel")) {
//				try {
//					rmHotel.commit(xid);
//				} catch (RemoteException e) {
//					System.out.println("The hotel manager could not commit!!!");
//					return false;
//				}
//			} else if (rm.equals("flight")) {
//				try {
//					rmFlight.commit(xid);
//				} catch (RemoteException e) {
//					System.out.println("The flight manager could not commit!!!");
//					return false;
//				}
//			}
//		} else { 						//We have to abort
//			if (rm.equals("car")) {
//				try {
//					rmCar.abort(xid);
//				} catch (RemoteException e) {
//					System.out.println("The car manager could not abort!!!");
//					return false;
//				}
//			} else if (rm.equals("hotel")) {
//				try {
//					rmHotel.abort(xid);
//				} catch (RemoteException e) {
//					System.out.println("The hotel manager could not abort!!!");
//					return false;
//				}
//			} else if (rm.equals("flight")) {
//				try {
//					rmFlight.abort(xid);
//				} catch (RemoteException e) {
//					System.out.println("The flight manager could not abort!!!");
//					return false;
//				}
//			}
//		}
//
//		transactionDecision.remove(xid);
//		return true;
//	}

	private void disassociate(int xid, String rmname) {
		Vector<String> rms = xidsToRMNames.get(xid);
		rms.remove(rmname);
		if (rms.isEmpty()) {
			xidsToRMNames.remove(xid);
			numberOfTransactions--;
		}
	}

	public Vector<String> abort(int id) {
		Vector<String> v = xidsToRMNames.get(id);
		if (v == null) v = new Vector<String>();
		xidsToRMNames.remove(id);
		timeToLiveMap.remove(id);

		numberOfTransactions--;
		return v;
	}

	public boolean isValidTransaction(int id) {
		return xidsToRMNames.containsKey(id);
	}

	public boolean isActiveTransaction(int xid) {
		return xidsToRMNames.containsKey(xid) || transactionDecision.containsKey(xid);
	}

	public boolean canShutdown(){
		//return numberOfTransactions == 0 ? true: false;
	    return xidsToRMNames.isEmpty();
	}

	public Vector<Integer> getTimedOutTransactions(){

		Long currentTime = System.currentTimeMillis();
		Vector<Integer> timedOut = new Vector<Integer>();

		for (ConcurrentHashMap.Entry<Integer, Long> entry : timeToLiveMap.entrySet()) {
			if (currentTime - entry.getValue() > TIMEOUT){
				timedOut.add(entry.getKey());
				timeToLiveMap.remove(entry.getKey());
			}
		}

		return timedOut;
	}

	public void setCrashCondition(Crash crashCondition) {
		this.crashCondition = crashCondition;
	}

	public boolean getTransactionFinalAction(int xid) {
		Boolean res = transactionDecision.get(xid);
		if (res == null)
			return false;
		return res;
	}

	public void updateCarManagerRef(ItemManager im){
		rmCar = im;
	}

	public void updatehotelManagerRef(ItemManager im){
		rmHotel = im;
	}

	public void updateFlightManagerRef(ItemManager im){
		rmFlight = im;
	}

	public Vector<Integer> getAllActiveTransactions(){
		Vector<Integer> xids = new Vector<Integer>();

		for (ConcurrentHashMap.Entry<Integer, Vector<String>> entry : xidsToRMNames.entrySet()) {
			xids.add(entry.getKey());
		}

		return xids;
	}

	public TransactionStatus getTransactionStatus(int xid){
		return xidsToStatus.get(xid);
	}

	public void logTransactionManager(){
		SerializeUtils.saveToDisk(this, getTxnManagerFileName());
	}

	public TransactionManager retrieveTransactionManager(){
		return (TransactionManager)SerializeUtils.loadFromDisk(getTxnManagerFileName());
	}

	private void deleteTransactionManagerLog() {
		SerializeUtils.deleteFile(getTxnManagerFileName());
	}

	private String getTxnManagerFileName() {
		return "/tmp/Group23/txnManager.tm";
	}

	public void setRmCustomer(ResourceManager rm){
		this.rmCustomer = rm;
	}
}
