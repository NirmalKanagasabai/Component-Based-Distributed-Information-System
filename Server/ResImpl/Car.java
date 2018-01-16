// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------
package carcode.ResImpl;

import servercode.ResInterface.*;
import servercode.ResImpl.*;

public class Car extends ReservableItem {
	
    public Car(String location, int count, int price) {
        super(location, count, price);
    }

    public String getKey() {
        return Car.getKey(getLocation());
    }

    public static String getKey(String location) {
        String s = "car-" + location;
        return s.toLowerCase();
    }
    
    public Car getCopy(){
    	Car copy = new Car(this.getLocation(), this.getCount(), this.getPrice());
    	copy.setReserved(this.getReserved());
    	
    	return copy;
    }
}
 
