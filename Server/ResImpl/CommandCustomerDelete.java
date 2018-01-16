package servercode.ResImpl;

public class CommandCustomerDelete extends Command {

	private ResourceManagerImpl resourceManager;
	private int id;
	private String itemId;
	
	public CommandCustomerDelete(int id, String itemId, ResourceManagerImpl rm){
		this.resourceManager = rm;
		this.id = id;
		this.itemId = itemId;
	}

	@Override
	public void execute() {
		resourceManager.removeData(id, itemId);		
	}
}
 
