package servercode.TransactionManager;

public class TransactionAbortedException extends Exception {

    private int xid;

    public TransactionAbortedException(int xid, String msg) {
        super(msg);
        setXID(xid);
    }

    private void setXID(int i) {
        this.xid = i;
    }
}
