package clientcode;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import servercode.ResImpl.InvalidTransactionException;
import servercode.ResImpl.TransactionAbortedException;
import servercode.ResInterface.ResourceManager;
import LockManager.DeadlockException;

public class AutomatedClient {
    static int NUM_CLIENTS = 1;
    static int DELAY = 100;
    static int NUMBER_OF_ITEMS = 1000;
	static String message = "blank";
	static ResourceManager rm = null;

	public static void main(String args[])
	{
		boolean populate = false;
		
		String server = "localhost";
		int port = 5005;

		if (args.length == 2) {
			server = args[0];
			port = Integer.parseInt(args[1]);
		}
		else if (args.length == 3) {
			server = args[0];
			port = Integer.parseInt(args[1]);
			populate = true;
		} 
		else {
			System.out.println ("Usage: java client [rmihost] [port]");
			System.exit(1);
		}

		try
		{
			// get a reference to the rmiregistry
			Registry registry = LocateRegistry.getRegistry(server, port);
			// get the proxy and the remote reference by rmiregistry lookup
			rm = (ResourceManager) registry.lookup("Group5_ResourceManager");
			if(rm!=null)
			{
				System.out.println("Successful");
				System.out.println("Connected to RM");
			}
			else
			{
				System.out.println("Unsuccessful");
			}
			// make call on remote method
		}
		catch (Exception e)
		{        	
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		if (populate) {
			int xid;
			try {
				xid = rm.start();

				for (int i = 0; i < NUMBER_OF_ITEMS; ++i) {
					rm.newCustomer(xid, i);
					rm.addCars(xid, "city"+i, 1000, 10);
					rm.addRooms(xid, "city"+i, 1000, 5);
					rm.addFlight(xid, i, 1000, 700);
				}

				rm.commit(xid);
			} catch (RemoteException | InvalidTransactionException | DeadlockException e) {
				e.printStackTrace();
			}
			return;
		}

    	System.out.println("client,time,latency");
		ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS);
		try {
		    AutomatedRunner[] runners = new AutomatedRunner[NUM_CLIENTS];
		    for (int i = 0; i < NUM_CLIENTS; ++i)
		        runners[i] = new AutomatedRunner(i, rm);
            executor.invokeAll(Arrays.asList(runners), 60, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.out.println("INTERRUPTION IN EXECUTOR!!!");
        }
        
	}
}


class AutomatedRunner implements Callable<Void> {
    private ResourceManager rm;
    private int clientId;
    
    public AutomatedRunner(int clientId, ResourceManager rm) {
        this.clientId = clientId;
        this.rm = rm;
    }
    
    public Void call() {
        while (true) {
            try {
                long t1 = System.nanoTime();
                int xid = rm.start();
                for (int i = 0; i < 50; ++i) {
                	RandomCommand rc = new RandomCommand(xid);
                	rc.execRandomCommand(rm);
                }
                //rm.commit(xid);
                rm.abort(xid);
                long t2 = System.nanoTime();
                Date d = new Date();
                System.out.printf("%d,%02d:%02d:%02d,%.3f%n", clientId, d.getHours(), d.getMinutes(), d.getSeconds(), (t2-t1)/1000000.0);
                //Thread.sleep(AutomatedClient.DELAY);
            }
            catch (RemoteException | InvalidTransactionException | DeadlockException | TransactionAbortedException e) {
            	e.printStackTrace();
            }
        
        }
    }
}