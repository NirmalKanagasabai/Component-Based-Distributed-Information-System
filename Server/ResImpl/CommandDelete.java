package servercode.ResImpl;

import flightcode.ResImpl.FlightManagerImpl;
import hotelcode.ResImpl.HotelManagerImpl;
import servercode.ResInterface.ItemManager;
import carcode.ResImpl.Car;
import carcode.ResImpl.CarManagerImpl;

public class CommandDelete extends Command {
	
	private ItemManager itemManager;
	private int id;
	private String itemId;
	
	public CommandDelete(int id, String itemId, ItemManager im){
		this.itemManager = im;
		this.id = id;
		this.itemId = itemId;		
	}
	
	public void execute(){
		
		if (itemManager instanceof CarManagerImpl){
			((CarManagerImpl) itemManager).deleteCar(id, itemId);			
		}
		
		if (itemManager instanceof HotelManagerImpl){
			((HotelManagerImpl) itemManager).deleteHotel(id, itemId);			
		}
		
		if (itemManager instanceof FlightManagerImpl){
			((FlightManagerImpl) itemManager).deleteFlight(id, itemId);			
		}
			
	}

}
 
