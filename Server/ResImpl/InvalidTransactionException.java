package servercode.ResImpl;

public class InvalidTransactionException extends Exception {
	/*
	 The transaction is deadlocked.  Somebody should abort it.
	 */

    public InvalidTransactionException(int id) {
        super("The transaction " + id + " is invalid");        
    }

}
