package servercode.ResImpl;

import java.rmi.RemoteException;
import java.util.Vector;

public class TransactionKiller implements Runnable {

	private TransactionManager txnManager;
	private ResourceManagerImpl rm;

	public TransactionKiller(TransactionManager txnMan, ResourceManagerImpl rm){
		txnManager = txnMan;
		this.rm = rm;
	}

	public void run() {
        while (true){

        	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        	Vector<Integer> transactionsToKill = txnManager.getTimedOutTransactions();
        	for(Integer txn: transactionsToKill){
        		try {
					rm.abort(txn);
				} catch (RemoteException | InvalidTransactionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}

        }

    }

}
