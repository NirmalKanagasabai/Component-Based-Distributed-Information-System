package clientcode;

import servercode.ResImpl.InvalidTransactionException;
import servercode.ResImpl.TransactionAbortedException;
import servercode.ResInterface.*;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.Vector;

import LockManager.DeadlockException;

enum ArgType { INT, STRING }

public class RandomCommand {
    private static Random r = new Random();
    private static String[] commands = {
        "newflight",
        "newcar",
        "newroom",
//        "newcustomerid",
//        "deleteflight",
//        "deletecar",
//        "deleteroom",
//        "deletecustomer",
//        "queryflight",
//        "querycar",
//        "queryroom",
//        "querycustomer",
        "queryflightprice",
        "querycarprice",
        "queryroomprice",
//        "reserveflight",
//        "reservecar",
//        "reserveroom",
//        "itinerary",
    };
    private int xid;
    
    public RandomCommand(int xid) {
        this.xid = xid;
    }
    
    public void execRandomCommand(ResourceManager rm) throws RemoteException, InvalidTransactionException, DeadlockException, TransactionAbortedException {
        String command = randomCommand();
        
        if (command.equals("newflight")) {
            rm.addFlight(xid, randomFlightId(), randomQuantity(), randomPrice());
        }
        else if (command.equals("newcar")) {
            rm.addCars(xid, randomCarId(), randomQuantity(), randomPrice());
        }
        else if (command.equals("newroom")) {
            rm.addRooms(xid, randomRoomId(), randomQuantity(), randomPrice());
        }
        else if (command.equals("deleteflight")) {
            rm.deleteFlight(xid, randomFlightId());
        }
        else if (command.equals("deletecar")) {
            rm.deleteCars(xid, randomCarId());
        }
        else if (command.equals("deleteroom")) {
            rm.deleteRooms(xid, randomRoomId());
        }
        else if (command.equals("deletecustomer")) {
            rm.deleteCustomer(xid, randomCustomerId());
        }
        else if (command.equals("queryflight")) {
            rm.queryFlight(xid, randomFlightId());
        }
        else if (command.equals("querycar")) {
            rm.queryCars(xid, randomCarId());
        }
        else if (command.equals("queryroom")) {
            rm.queryRooms(xid, randomRoomId());
        }
        else if (command.equals("querycustomer")) {
            rm.queryCustomerInfo(xid, randomCustomerId());
        }
        else if (command.equals("queryflightprice")) {
            rm.queryFlightPrice(xid, randomFlightId());
        }
        else if (command.equals("querycarprice")) {
            rm.queryCarsPrice(xid, randomCarId());
        }
        else if (command.equals("queryroomprice")) {
            rm.queryRoomsPrice(xid, randomRoomId());
        }
        else if (command.equals("reserveflight")) {
            rm.reserveFlight(xid, randomCustomerId(), randomFlightId());
        }
        else if (command.equals("reservecar")) {
            rm.reserveCar(xid, randomCustomerId(), randomCarId());
        }
        else if (command.equals("reserveroom")) {
            rm.reserveRoom(xid, randomCustomerId(), randomRoomId());
        }
        else if (command.equals("itinerary")) {
            int numFlights = 1+r.nextInt(6);
            Vector flights = new Vector();
            for (int i = 0; i < numFlights; ++i) {
                flights.add(String.valueOf(randomFlightId()));
            }
            rm.itinerary(xid, randomCustomerId(), flights, randomRoomId(), true, true);
        }

    }
    
    private String randomCommand() {
        int n = r.nextInt(commands.length);
        return commands[n];
    }
    
    private String randomRoomId() {
        int n = r.nextInt(1000);
        return "city"+n;
    }
    
    private String randomCarId() {
        int n = r.nextInt(1000);
        return "city"+n;
    }
    
    private int randomFlightId() {
        return r.nextInt(1000);
    }

    private int randomQuantity() {
        return r.nextInt(1000);
    }
    
    private int randomPrice() {
        return r.nextInt(1000);
    }

    private int randomCustomerId() {
        return r.nextInt(1000);
    }
}
