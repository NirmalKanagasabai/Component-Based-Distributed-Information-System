package servercode.TransactionManager;

import servercode.ResInterface.ResourceManager;
import servercode.ResInterface.Transaction;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class KeepaliveThread implements Runnable {

    Map<Integer, ActiveTransaction> activeTransactionMap;

    public KeepaliveThread(Map<Integer, ActiveTransaction> activeTransactions) {
        setActiveTransactionMap(activeTransactions);
    }

    public void run() {

        while (true) {

            for(Map.Entry<Integer, ActiveTransaction> activeTransaction : activeTransactionMap.entrySet()) {

                if(activeTransaction.getValue().getLastTransationTime().getTime() - new Date().getTime() > activeTransaction.getValue().getTimeToLive()) {

                    System.out.println("Abort this damn transation");
                    try {

                        // abort the transaction
                        activeTransaction.getValue().abort(activeTransaction.getKey());

                        // remove it from the table of active transactions
                        activeTransactionMap.remove(activeTransaction.getKey());

                    } catch (InvalidTransactionException | RemoteException e) {

                        System.out.println("Keepalive thread interrupted.");
                    }
                }
            }

            try {

                Thread.sleep(100);

            } catch (InterruptedException e) {

                Thread.interrupted();

            }

        }
    }

    private void setActiveTransactionMap(Map<Integer, ActiveTransaction> activeTransactionMap) {
        this.activeTransactionMap = activeTransactionMap;
    }


}
