package servercode.LockManager;

/*
    The transaction is deadlocked.  Somebody should abort it.
*/

public class DeadlockException extends RuntimeException
{
    private int xid = 0;
    
    public DeadlockException (int xid, String msg)
    {
        super("The transaction " + xid + " is deadlocked:" + msg);
        this.xid = xid;
    }
    
    public int getXID()
    {
        return xid;
    }
}
