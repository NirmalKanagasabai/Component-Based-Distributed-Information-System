package servercode.TransactionManager;

public class InvalidTransactionException extends Exception {

    private int xid;

    public InvalidTransactionException(int xid, String msg) {
        super(msg);
        setXID(xid);
    }

    private void setXID(int i) {
        this.xid = i;
    }


}
