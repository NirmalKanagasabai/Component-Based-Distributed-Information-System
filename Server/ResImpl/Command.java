package servercode.ResImpl;

import java.io.Serializable;

public abstract class Command implements Serializable {
	
	public Command(){		
		
	}
	
	public abstract void execute();

}
 
