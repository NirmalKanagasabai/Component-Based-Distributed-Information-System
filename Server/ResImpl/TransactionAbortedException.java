package servercode.ResImpl;

public class TransactionAbortedException extends Exception {
		
	public TransactionAbortedException(int id) {		
		super("The transaction " + id + " had to be aborted");
	}	
}
