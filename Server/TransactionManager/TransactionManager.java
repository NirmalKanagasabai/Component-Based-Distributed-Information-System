package servercode.TransactionManager;

import java.rmi.RemoteException;
import java.util.*;

import org.omg.CORBA.DynAnyPackage.Invalid;
import org.omg.PortableInterceptor.ACTIVE;

import servercode.ResInterface.MiddlewareServer;
import servercode.ResInterface.ResourceManager;
import servercode.ResInterface.Transaction;

public class TransactionManager implements Transaction {

    private Map<Integer, ActiveTransaction> activeTransactions;
    private MiddlewareServer parent;
    private int xid;
    private KeepaliveThread t;

    public TransactionManager(MiddlewareServer parent, Map<Integer, ActiveTransaction> activeTransactions) {
        setParent(parent);
        setTransactionMap(activeTransactions);
        setXID();
        setKeepaliveThread(activeTransactions);
    }

    public int start()  {

        // increment the transaction counter
        xid += 1;

        // add the transaction to the active transactions list
        addActiveTransaction(xid);

        // return the transaction id 
        return xid;

    }


    public boolean commit(int xid) throws InvalidTransactionException, TransactionAbortedException, RemoteException {

        // if the transaction doesn't exist in the list of active transactions, throw an exception
        if (!this.activeTransactions.containsKey(xid)) {
            throw new InvalidTransactionException(xid, "Invalid transaction id passed for commit.");
        }

        // remove the transaction from the list of transactions a
        ActiveTransaction t = this.activeTransactions.get(xid);

        try {

            // remove the transaction from the list of active transactions
            this.activeTransactions.remove(xid);

            // return the commit result from t
            return t.commit(xid);

        } catch(InvalidTransactionException | TransactionAbortedException | RemoteException e) {

            throw e;

        }

    }


    public void abort(int xid) throws InvalidTransactionException, RemoteException {

        // if the transaction doesn't exist in the list of active transactions, throw an exception
        if (!this.activeTransactions.containsKey(xid)) {
            throw new InvalidTransactionException(xid, "Invalid transaction id passed for commit.");
        }

        ActiveTransaction t = this.getActiveTransactions().get(xid);
        this.getActiveTransactions().remove(xid);

        try {

            t.abort(xid);

        } catch(InvalidTransactionException | RemoteException e) {
            throw e;
        }
    }


    public boolean transactionOperation(int xid, ResourceManager rm) throws InvalidTransactionException, RemoteException {

        if (!addActiveManager(xid, rm)) {
            throw new InvalidTransactionException(xid, "Invalid transaction id passed for txn operation");
        }

        // update the keepalive timer
        this.activeTransactions.get(xid).updateLastTransaction();

        return true;

    }


    public Map<Integer, ActiveTransaction> getActiveTransactions() {
        Map<Integer, ActiveTransaction> shallowCopy = new HashMap<Integer, ActiveTransaction>();
        shallowCopy.putAll(this.activeTransactions);
        return shallowCopy;
    }


    private void setKeepaliveThread(Map<Integer, ActiveTransaction> activeTransactions) {
        KeepaliveThread t = new KeepaliveThread(activeTransactions);
        t.run();
    }

    private boolean addActiveManager(int xid, ResourceManager rm) {

        if (!this.activeTransactions.containsKey(xid)) {
            return false;
        }

        ActiveTransaction txn = this.activeTransactions.get(xid);
        txn.addActiveManager(rm);

        return true;
    }

    private void addActiveTransaction(int xid) {
        ActiveTransaction txn = new ActiveTransaction(xid, 10000, new ArrayList<ResourceManager>());
        this.activeTransactions.put(xid, txn);
    }

    private void setParent(MiddlewareServer parent) {
        this.parent = parent;
    }

    private void setTransactionMap(Map<Integer, ActiveTransaction> activeTransactions) {
        this.activeTransactions = activeTransactions;
    }


    private void setXID() {
        this.xid = 0;
    }

    
}
