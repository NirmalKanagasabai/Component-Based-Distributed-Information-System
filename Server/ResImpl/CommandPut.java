package servercode.ResImpl;

import flightcode.ResImpl.Flight;
import flightcode.ResImpl.FlightManagerImpl;
import hotelcode.ResImpl.Hotel;
import hotelcode.ResImpl.HotelManagerImpl;
import servercode.ResInterface.ItemManager;
import carcode.ResImpl.Car;
import carcode.ResImpl.CarManagerImpl;


public class CommandPut extends Command{
	
	private ItemManager itemManager;
	private int id;
	private String itemId;
	private ReservableItem newObj;	
	
	
	public CommandPut(int id, String itemId, ReservableItem newObj, ItemManager im){
		this.itemManager = im;
		this.id = id;
		this.itemId = itemId;
		this.newObj = newObj;	
	}
	
	public void execute(){
		
		if (itemManager instanceof CarManagerImpl){
			((CarManagerImpl) itemManager).putCar(id, itemId, (Car)newObj);
		}
		
		if (itemManager instanceof HotelManagerImpl){
			((HotelManagerImpl) itemManager).putHotel(id, itemId, (Hotel)newObj);
		}
		
		if (itemManager instanceof FlightManagerImpl){
			((FlightManagerImpl) itemManager).putFlight(id, itemId, (Flight)newObj);
		}
					
	}
	

} 
