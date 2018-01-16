package servercode.ResInterface;

import servercode.TransactionManager.InvalidTransactionException;
import servercode.TransactionManager.TransactionAbortedException;

import java.rmi.RemoteException;

public interface Transaction {

    public int start() throws RemoteException;
    public boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException;
    public void abort(int xid) throws RemoteException, InvalidTransactionException;
}
