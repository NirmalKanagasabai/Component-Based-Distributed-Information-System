package servercode.ResImpl;

public class CommandCustomerPut extends Command {

	private ResourceManagerImpl resourceManager;
	private int id;
	private String itemId;
	private Customer newCust;	
	
	public CommandCustomerPut(int id, String itemId, Customer newCustomer, ResourceManagerImpl rm){
		this.resourceManager = rm;
		this.id = id;
		this.itemId = itemId;
		this.newCust = newCustomer;	
	}

	@Override
	public void execute() {
		resourceManager.writeData(id, itemId, newCust);		
	}
}
 
