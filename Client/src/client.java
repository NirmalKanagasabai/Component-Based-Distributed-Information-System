import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.*;
import java.io.*;

import LockManager.DeadlockException;

import servercode.ResImpl.Crash;
import servercode.ResInterface.*;

public class client {

	private JFrame frame;
	
	private JPanel panelMenu;
	private JPanel panelAdd;
	private JPanel panelQuery;
	private JPanel panelReserve;
	private JPanel panelDelete;
	private JPanel panelTransaction;
	
	private JTextField txtCFlightno;
	private JTextField txtFlightSeats;
	private JTextField txtCFlightPrice;
	private JTextField txtCRoomLocation;
	private JTextField txtCNoOfRooms;
	private JTextField txtCRoomPrice;
	private JTextField txtCCarLocation;
	private JTextField txtCNoOfCars;
	private JTextField txtCCarprice;
	private JTextField txtQCustomerid;
	private JTextField txtQFlightno;
	private JTextField txtQRoomLocation;
	private JTextField txtQCarLocation;
	private JTextField txtQPriceFlightno;
	private JTextField txtQPriceRoomLocation;
	private JTextField txtQPriceCarLocation;
	private JTextField txtDFlightNo;
	private JTextField txtDRoomLocation;
	private JTextField txtDCarLocation;
	private JTextField txtDCustomerId;
	private JTextField txtRFlightno;
	private JTextField txtRRoomLocation;
	private JTextField txtRCarLocation;
	private JTextField txtRFlightCustomerId;
	private JTextField txtRRoomCustomerId;
	private JTextField txtRCarCustomerId;
	private JTextField txtRItineraryCustomerId;
	private JTextField txtRFlightno1;
	private JTextField txtRFlightno2;
	private JTextField txtRLocation;
	private JTextField txtRNoofcars;
	private JTextField txtRNoofrooms;
	
	private static JTextPane output = new JTextPane();
    private static StyledDocument doc = output.getStyledDocument(); 
    private Style style = output.addStyle("I'm a Style", null);
    	
	String[] boxOptions = {"-", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"};
	DefaultComboBoxModel<String> customerCComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> flightCComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> roomCComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> carCComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> customerQComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> flightQComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> roomQComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> carQComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> flightPriceQComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> roomPriceQComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> carPriceQComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> roomDComboModel = new DefaultComboBoxModel<String>(boxOptions);		
	DefaultComboBoxModel<String> flightDComboModel = new DefaultComboBoxModel<String>(boxOptions);		
	DefaultComboBoxModel<String> customerDComboModel = new DefaultComboBoxModel<String>(boxOptions);		
	DefaultComboBoxModel<String> carDComboModel = new DefaultComboBoxModel<String>(boxOptions);		
	DefaultComboBoxModel<String> flightRComboModel = new DefaultComboBoxModel<String>(boxOptions);	
	DefaultComboBoxModel<String> roomRComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> carRComboModel = new DefaultComboBoxModel<String>(boxOptions);
	DefaultComboBoxModel<String> itineraryRComboModel = new DefaultComboBoxModel<String>(boxOptions);
	
	char decimal = '.';
	String patternRegex = "\\d+(\\.\\d+)*";
	private JTextField txtTCommitTransactionId;
	private JTextField txtTAbortTransactionID;
	
    static String message = "blank";
    static ResourceManager rm = null;

    //For transactions
    Vector<Integer> txnIds = new Vector<Integer>();

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		Style style2 = output.addStyle("I'm a Style", null);


        String server = "localhost";
        int port = 5005;
        
        if (args.length == 2) {
            server = args[0];
        		port = Integer.parseInt(args[1]);
        } else {
    			StyleConstants.setForeground(style2, Color.red);
    			try { doc.insertString(doc.getLength(),"[WARN]: " + "Usage: java client [rmihost] [port]" + "\n", style2); }
    			catch (BadLocationException badLocationException){}
            System.exit(1);
        }
        
        try {
            // get a reference to the rmiregistry
            Registry registry = LocateRegistry.getRegistry(server, port);
            // get the proxy and the remote reference by rmiregistry lookup
            rm = (ResourceManager) registry.lookup("Group5_ResourceManager");
            
            if(rm != null) {  				              
            		StyleConstants.setForeground(style2, Color.gray);
    				try { doc.insertString(doc.getLength(),"Successfully connected to RM" + "\n", style2); }
    				catch (BadLocationException badLocationException){}
    			
            } else {   
            		StyleConstants.setForeground(style2, Color.red);              
    	        		try { doc.insertString(doc.getLength(),"[WARN]: " + "Connection Unsuccessful" + "\n",style2); }
    	        		catch (BadLocationException badLocationException){}
            }
          
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
    			StyleConstants.setForeground(style2, Color.red);              
    			try { doc.insertString(doc.getLength(),"[WARN]: " + "Client Exception" + e.toString() + "\n",style2); }
    			catch (BadLocationException badLocationException){}
        }

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					client window = new client();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private boolean checkIfNumber(String str, String field) {
		boolean ifNumber = false;
		int count = 0;
		if(str.matches(patternRegex)) {
			for (int i = 0; i < str.length(); i++) {		
				if(str.charAt(i) == decimal) {
					count++;
				}
			}
			//if ((count == 0) || (count == 1)) {
			if ((count == 0)) {
				ifNumber = true;
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        //try { doc.insertString(doc.getLength(),"[WARN]: " + field + ": " + str + "\"" + " has more than one decimal point. Not a valid number!"  + "\n",style); }
		        try { doc.insertString(doc.getLength(),"[WARN]: The"  + field + ": " + str + "\"" + " has decimals. Please enter only Integer values"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
				ifNumber = false;
			}
		} else {
	        StyleConstants.setForeground(style, Color.red);
	        try { doc.insertString(doc.getLength(),"[WARN]: The"  + field+ ": " + str + "\""  + " is not a valid number!" + "\n",style); }
	        catch (BadLocationException badLocationException){}
			ifNumber = false;
		}
		return ifNumber;
	}

	/**
	 * Create the application.
	 */
	public client() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
	    client obj = new client();
		frame = new JFrame();
		frame.setBounds(100, 100, 1109, 634);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new CardLayout(0, 0));
		
		output.setEditable(false);
		
		panelMenu = new JPanel();
		panelMenu.setBackground(SystemColor.menu);
		frame.getContentPane().add(panelMenu, "name_478990532285825");
		panelMenu.setLayout(null);
		
		JLabel lblClientInterface = new JLabel("CLIENT INTERFACE");
		lblClientInterface.setForeground(SystemColor.controlHighlight);
		lblClientInterface.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblClientInterface.setHorizontalAlignment(SwingConstants.CENTER);
		lblClientInterface.setBounds(380, 24, 450, 55);
		panelMenu.add(lblClientInterface);
		
		JButton btnCreate = new JButton("ADD [OR] CREATE OPERATIONS");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelAdd.setVisible(true);
				panelMenu.setVisible(false);
			}
		});
		btnCreate.setBackground(UIManager.getColor("Button.background"));
		btnCreate.setForeground(UIManager.getColor("Button.light"));
		btnCreate.setBounds(29, 137, 261, 64);
		panelMenu.add(btnCreate);
		
		JButton btnQuery = new JButton("QUERY OPERATIONS");
		btnQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelQuery.setVisible(true);
				panelMenu.setVisible(false);
			}
		});
		btnQuery.setForeground(SystemColor.controlHighlight);
		btnQuery.setBackground(SystemColor.window);
		btnQuery.setBounds(29, 234, 261, 64);
		panelMenu.add(btnQuery);
		
		JButton btnDelete = new JButton("DELETE OPERATIONS");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelDelete.setVisible(true);
				panelMenu.setVisible(false);
			}
		});
		btnDelete.setForeground(SystemColor.controlHighlight);
		btnDelete.setBackground(SystemColor.window);
		btnDelete.setBounds(29, 333, 261, 64);
		panelMenu.add(btnDelete);
		
		JButton btnReserve = new JButton("RESERVE OPERATIONS");
		btnReserve.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelReserve.setVisible(true);
				panelMenu.setVisible(false);
			}
		});
		btnReserve.setForeground(SystemColor.controlHighlight);
		btnReserve.setBackground(SystemColor.window);
		btnReserve.setBounds(29, 433, 261, 64);
		panelMenu.add(btnReserve);
		
		JButton btnTransaction = new JButton("TRANSACTION OPERATIONS");
		btnTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelTransaction.setVisible(true);
				panelMenu.setVisible(false);
			}
		});
		btnTransaction.setForeground(SystemColor.controlHighlight);
		btnTransaction.setBackground(SystemColor.window);
		btnTransaction.setBounds(29, 531, 261, 64);
		panelMenu.add(btnTransaction);
		
		JScrollPane scroll = new JScrollPane(output);
		scroll.setSize(750, 440);
		scroll.setLocation(322, 142);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		panelMenu.add(scroll);
		
		panelAdd = new JPanel();
		frame.getContentPane().add(panelAdd, "name_479645754629866");
		panelAdd.setBackground(SystemColor.menu);
		panelAdd.setLayout(null);
		
		JLabel lblCreate = new JLabel("ADD [OR] CREATE OPERATIONS");
		lblCreate.setBounds(409, 24, 294, 55);
		panelAdd.add(lblCreate);
		lblCreate.setHorizontalAlignment(SwingConstants.CENTER);
		lblCreate.setForeground(SystemColor.controlHighlight);
		lblCreate.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		
		JLabel lblCCustomer = new JLabel("CUSTOMER");
		lblCCustomer.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCCustomer.setForeground(UIManager.getColor("Label.disabledForeground"));
		lblCCustomer.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblCCustomer.setBounds(29, 190, 125, 55);
		panelAdd.add(lblCCustomer);
		
		JComboBox<String> comboBoxCCustomer = new JComboBox<String>(customerCComboModel);
		comboBoxCCustomer.setBounds(222, 190, 70, 55);
		panelAdd.add(comboBoxCCustomer);
		
		JButton btnCAddNewCustomer = new JButton("Add New Customer");
		btnCAddNewCustomer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String addCustomer = new String();
				String transactionId = comboBoxCCustomer.getSelectedItem().toString();
				
				if((transactionId != null) && (transactionId != "-")) {
					
					addCustomer = "newcustomer"  + "," + transactionId;
	
			        StyleConstants.setForeground(style, Color.blue); 
			        String blue = "[INPUT]: " + addCustomer + "\n";
			        try { doc.insertString(doc.getLength(), blue,style); }
			        catch (BadLocationException badLocationException){}
					        
			        StyleConstants.setForeground(style, Color.black);
			        try { doc.insertString(doc.getLength(), "Adding a new Customer using Transaction ID: " + transactionId + "\n",style); }
			        catch (BadLocationException badLocationException){}
			        
			        StyleConstants.setForeground(style, Color.black);
			        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
			        catch (BadLocationException badLocationException){}        

					comboBoxCCustomer.setSelectedItem("-");
					
	                try {
	                    
	                		int customerId = rm.newCustomer(Integer.parseInt(transactionId));
            				StyleConstants.setForeground(style, Color.green);
            				try { doc.insertString(doc.getLength(), "New Customer ID: " + customerId + "\n",style); }
            				catch (BadLocationException badLocationException){}    
            				
		               } catch (Exception exception){
	            				StyleConstants.setForeground(style, Color.red);
	            				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
	            				catch (BadLocationException badLocationException){} 
		                } 
					
				} else {
			        StyleConstants.setForeground(style, Color.red);
			        try { doc.insertString(doc.getLength(), "[WARN]: Invalid Transaction ID \n" ,style); }
			        catch (BadLocationException badLocationException){}
				}
		
				panelAdd.setVisible(false);
				panelMenu.setVisible(true);
				
			}
		});
		btnCAddNewCustomer.setForeground(SystemColor.controlHighlight);
		btnCAddNewCustomer.setBackground(SystemColor.window);
		btnCAddNewCustomer.setBounds(793, 190, 158, 54);
		panelAdd.add(btnCAddNewCustomer);

		
		JLabel lblCFlight = new JLabel("FLIGHT");
		lblCFlight.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCFlight.setForeground(Color.GRAY);
		lblCFlight.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblCFlight.setBounds(29, 250, 125, 55);
		panelAdd.add(lblCFlight);
		
		JComboBox<String> comboBoxCFlight = new JComboBox<String>(flightCComboModel);
		comboBoxCFlight.setBounds(222, 250, 70, 55);
		panelAdd.add(comboBoxCFlight);
		
		txtCFlightno = new JTextField();
		txtCFlightno.setForeground(SystemColor.controlHighlight);
		txtCFlightno.setColumns(10);
		txtCFlightno.setBounds(334, 259, 96, 34);
		panelAdd.add(txtCFlightno);
		txtCFlightno.setUI(new JTextFieldHintUI("Flight_No", UIManager.getColor("Button.light"))); 
		
		txtFlightSeats = new JTextField();
		txtFlightSeats.setForeground(SystemColor.controlHighlight);
		txtFlightSeats.setColumns(10);
		txtFlightSeats.setBounds(474, 259, 96, 34);
		panelAdd.add(txtFlightSeats);
		txtFlightSeats.setUI(new JTextFieldHintUI("Flight_Seats", UIManager.getColor("Button.light"))); 

		txtCFlightPrice = new JTextField();
		txtCFlightPrice.setHorizontalAlignment(SwingConstants.CENTER);
		txtCFlightPrice.setForeground(SystemColor.controlHighlight);
		txtCFlightPrice.setColumns(10);
		txtCFlightPrice.setBounds(614, 259, 96, 34);
		panelAdd.add(txtCFlightPrice);
		txtCFlightPrice.setUI(new JTextFieldHintUI("Flight_Price", UIManager.getColor("Button.light"))); 

		
		JButton btnCAddNewFlight = new JButton("Add New Flight");
		btnCAddNewFlight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String addFlight = new String();
				String transactionId = comboBoxCFlight.getSelectedItem().toString();
				String flightNo = txtCFlightno.getText();
				String flightSeats = txtFlightSeats.getText();
				String flightPrice = txtCFlightPrice.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!flightNo.isEmpty()  && checkIfNumber(flightNo, "Flight Number")) {
						if (!flightSeats.isEmpty()  && checkIfNumber(flightSeats, "Flight Seats")) {
							if (!flightPrice.isEmpty() && checkIfNumber(flightPrice, "Flight Price")) {
								
								addFlight = "newflight" + "," + transactionId + "," + flightNo + "," + flightSeats + "," + flightPrice;
								
								StyleConstants.setForeground(style, Color.blue);
						        String blue = "[INPUT]: " + addFlight  + "\n";
						        try { doc.insertString(doc.getLength(), blue,style); }
						        catch (BadLocationException badLocationException){}
								
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Adding a new Flight using Transaction ID: " + transactionId  + "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Flight Number: " + flightNo  + "\n",style); }
						        catch (BadLocationException badLocationException){}   
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Add Flight Seats: " + flightSeats  + "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(),"Set Flight Price: " + flightPrice + "\n",style); }
						        catch (BadLocationException badLocationException){}   
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
						        catch (BadLocationException badLocationException){}    	 
								
								txtCFlightno.setText("");
								txtFlightSeats.setText("");
								txtCFlightPrice.setText("");
								comboBoxCFlight.setSelectedItem("-");
								
				                try {
				                    if(rm.addFlight(Integer.parseInt(transactionId), Integer.parseInt(flightNo), Integer.parseInt(flightSeats), Integer.parseInt(flightPrice))) {
				                    		StyleConstants.setForeground(style, Color.green);
				                    		try { doc.insertString(doc.getLength(), "Flight Added!" + "\n",style); }
				                    		catch (BadLocationException badLocationException){}    		
				                    } else {
			                    			StyleConstants.setForeground(style, Color.red);
			                    			try { doc.insertString(doc.getLength(), "Flight could not be added!" + "\n",style); }
			                    			catch (BadLocationException badLocationException){}  	                    	
				                    }
				                } catch (Exception exception){
	                    				StyleConstants.setForeground(style, Color.red);
	                    				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
	                    				catch (BadLocationException badLocationException){} 
				                }
								
							} else {				
						        StyleConstants.setForeground(style, Color.red);
						        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Flight Price" + "\n",style); }
						        catch (BadLocationException badLocationException){}
							}
						} else {
					        StyleConstants.setForeground(style, Color.red);
					        try { doc.insertString(doc.getLength(), "[WARN]: Enter the number of seats in the flight" + "\n",style); }
					        catch (BadLocationException badLocationException){}
						}
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Flight number" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					}

				} else {
			        StyleConstants.setForeground(style, Color.red);
			        try { doc.insertString(doc.getLength(), "[WARN]: Invalid Transaction ID" + "\n",style); }
			        catch (BadLocationException badLocationException){}
				}
						
				panelAdd.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnCAddNewFlight.setForeground(SystemColor.controlHighlight);
		btnCAddNewFlight.setBackground(SystemColor.window);
		btnCAddNewFlight.setBounds(793, 251, 158, 54);
		panelAdd.add(btnCAddNewFlight);
		
		JLabel lblCRoom = new JLabel("ROOM");
		lblCRoom.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCRoom.setForeground(Color.GRAY);
		lblCRoom.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblCRoom.setBounds(29, 310, 125, 55);
		panelAdd.add(lblCRoom);
		
		JComboBox<String> comboBoxCRoom = new JComboBox<String>(roomCComboModel);
		comboBoxCRoom.setBounds(222, 310, 70, 55);
		panelAdd.add(comboBoxCRoom);
		
		txtCRoomLocation = new JTextField();
		txtCRoomLocation.setHorizontalAlignment(SwingConstants.CENTER);
		txtCRoomLocation.setForeground(SystemColor.controlHighlight);
		txtCRoomLocation.setColumns(10);
		txtCRoomLocation.setBounds(334, 319, 96, 34);
		panelAdd.add(txtCRoomLocation);
		txtCRoomLocation.setUI(new JTextFieldHintUI("Location", UIManager.getColor("Button.light")));
		
		txtCNoOfRooms = new JTextField();
		txtCNoOfRooms.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtCNoOfRooms.setHorizontalAlignment(SwingConstants.CENTER);
		txtCNoOfRooms.setForeground(SystemColor.controlHighlight);
		txtCNoOfRooms.setColumns(10);
		txtCNoOfRooms.setBounds(474, 319, 96, 34);
		panelAdd.add(txtCNoOfRooms);
		txtCNoOfRooms.setUI(new JTextFieldHintUI("No_of_Rooms", UIManager.getColor("Button.light")));
		
		txtCRoomPrice = new JTextField();
		txtCRoomPrice.setHorizontalAlignment(SwingConstants.CENTER);
		txtCRoomPrice.setForeground(SystemColor.controlHighlight);
		txtCRoomPrice.setColumns(10);
		txtCRoomPrice.setBounds(614, 319, 96, 34);
		panelAdd.add(txtCRoomPrice);
		txtCRoomPrice.setUI(new JTextFieldHintUI("Room_Price", UIManager.getColor("Button.light")));
		
		JButton btnCAddNewRoom = new JButton("Add New Room");
		btnCAddNewRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String addNewRoom = new String();
				String transactionId = comboBoxCRoom.getSelectedItem().toString();
				String location = txtCRoomLocation.getText();
				String noOfRooms = txtCNoOfRooms.getText();
				String pricePerRoom = txtCRoomPrice.getText();
					
				if((transactionId != null) && (transactionId != "-")) {
					if (!location.isEmpty()  && (location != "Location")) {
						if (!noOfRooms.isEmpty() && checkIfNumber(noOfRooms, "No. of Rooms")) {
							if (!pricePerRoom.isEmpty() && checkIfNumber(pricePerRoom, "Price per Room")) {
								
								addNewRoom = "newroom" + "," + transactionId + "," + location + "," + noOfRooms + "," + pricePerRoom;
								
						        StyleConstants.setForeground(style, Color.blue);
						        String input = "[INPUT]: " +  addNewRoom + "\n";
						        try { doc.insertString(doc.getLength(), input,style); }
						        catch (BadLocationException badLocationException){}
						     
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Adding a new Room using Transaction ID: " + transactionId +  "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Room Location: " + location +  "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Add No. of Rooms: " + noOfRooms +  "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Set Price Per Room: " + pricePerRoom +  "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "-----------------------------------------" +  "\n",style); }
						        catch (BadLocationException badLocationException){}					      
								
								txtCRoomLocation.setText("");
								txtCNoOfRooms.setText("");
								txtCRoomPrice.setText("");
								comboBoxCRoom.setSelectedItem("-");
								
				                try {
				                    if(rm.addRooms(Integer.parseInt(transactionId), location, Integer.parseInt(noOfRooms), Integer.parseInt(pricePerRoom))) {
			                    			StyleConstants.setForeground(style, Color.green);
			                    			try { doc.insertString(doc.getLength(), "Rooms Added!" + "\n",style); }
			                    			catch (BadLocationException badLocationException){}    		
				                    } else {
		                    				StyleConstants.setForeground(style, Color.red);
		                    				try { doc.insertString(doc.getLength(), "Rooms could not be added!" + "\n",style); }
		                    				catch (BadLocationException badLocationException){}  	                    	
				                    }
				                    
				               } catch(Exception exception){
	                    				StyleConstants.setForeground(style, Color.red);
	                    				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
	                    				catch (BadLocationException badLocationException){} 
				                } 
								
							} else {				
						        StyleConstants.setForeground(style, Color.red);
						        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Price Per Room" +  "\n",style); }
						        catch (BadLocationException badLocationException){}
							}
						} else {					
					        StyleConstants.setForeground(style, Color.red);
					        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Number of Rooms" +  "\n",style); }
					        catch (BadLocationException badLocationException){}						
						}
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Room Location" +  "\n",style); }
				        catch (BadLocationException badLocationException){}
					}

				} else {
			        StyleConstants.setForeground(style, Color.red);
			        try { doc.insertString(doc.getLength(), "[WARN]: Invalid Transaction ID" +  "\n",style); }
			        catch (BadLocationException badLocationException){}
				}
				
				panelAdd.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnCAddNewRoom.setForeground(SystemColor.controlHighlight);
		btnCAddNewRoom.setBackground(SystemColor.window);
		btnCAddNewRoom.setBounds(793, 311, 158, 54);
		panelAdd.add(btnCAddNewRoom);
		
		JLabel lblCCar = new JLabel("CAR");
		lblCCar.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCCar.setForeground(Color.GRAY);
		lblCCar.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblCCar.setBounds(29, 370, 125, 55);
		panelAdd.add(lblCCar);
		
		JComboBox<String> comboBoxCCar = new JComboBox<String>(carCComboModel);
		comboBoxCCar.setBounds(222, 370, 70, 55);
		panelAdd.add(comboBoxCCar);
		
		txtCCarLocation = new JTextField();
		txtCCarLocation.setHorizontalAlignment(SwingConstants.CENTER);
		txtCCarLocation.setForeground(SystemColor.controlHighlight);
		txtCCarLocation.setColumns(10);
		txtCCarLocation.setBounds(334, 378, 96, 34);
		panelAdd.add(txtCCarLocation);
		txtCCarLocation.setUI(new JTextFieldHintUI("Location", UIManager.getColor("Button.light")));
		
		txtCNoOfCars = new JTextField();
		txtCNoOfCars.setHorizontalAlignment(SwingConstants.CENTER);
		txtCNoOfCars.setForeground(SystemColor.controlHighlight);
		txtCNoOfCars.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtCNoOfCars.setColumns(10);
		txtCNoOfCars.setBounds(474, 378, 96, 34);
		panelAdd.add(txtCNoOfCars);
		txtCNoOfCars.setUI(new JTextFieldHintUI("No_of_Cars", UIManager.getColor("Button.light")));
		
		txtCCarprice = new JTextField();
		txtCCarprice.setHorizontalAlignment(SwingConstants.CENTER);
		txtCCarprice.setForeground(SystemColor.controlHighlight);
		txtCCarprice.setColumns(10);
		txtCCarprice.setBounds(614, 378, 96, 34);
		panelAdd.add(txtCCarprice);
		txtCCarprice.setUI(new JTextFieldHintUI("Car_Price", UIManager.getColor("Button.light")));
		
		JButton btnCAddNewCar = new JButton("Add New Car");
		btnCAddNewCar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String addNewCar = new String();
				String transactionId = comboBoxCCar.getSelectedItem().toString();
				String location = txtCCarLocation.getText();
				String noOfCars = txtCNoOfCars.getText();
				String pricePerCar = txtCCarprice.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!location.isEmpty()  && (location != "Location")) {
						if (!noOfCars.isEmpty() && checkIfNumber(noOfCars, "No. of Cars")) {
							if (!pricePerCar.isEmpty() && checkIfNumber(pricePerCar, "Price per Car")) {
								
								addNewCar = "newcar" + "," + transactionId + "," + location + "," + noOfCars + "," + pricePerCar;

						        StyleConstants.setForeground(style, Color.blue); 
						        try { doc.insertString(doc.getLength(), "[INPUT]: " +  addNewCar + "\n" ,style); }
						        catch (BadLocationException badLocationException){}
								
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Adding a new Room using Transaction ID: " + transactionId  +  "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Car Location: " + location   +  "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Add No. of Cars: " + noOfCars +  "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Set Price Per Car: " + pricePerCar +  "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "-----------------------------------------" +  "\n",style); }
						        catch (BadLocationException badLocationException){}
								
								txtCCarLocation.setText("");
								txtCNoOfCars.setText("");
								txtCCarprice.setText("");
								comboBoxCCar.setSelectedItem("-");
								
				                try {
				                    if(rm.addCars(Integer.parseInt(transactionId), location, Integer.parseInt(noOfCars), Integer.parseInt(pricePerCar))) {
			                    			StyleConstants.setForeground(style, Color.green);
			                    			try { doc.insertString(doc.getLength(), "Cars Added!" + "\n",style); }
			                    			catch (BadLocationException badLocationException){}    		
				                    } else {
		                    				StyleConstants.setForeground(style, Color.red);
		                    				try { doc.insertString(doc.getLength(), "Cars could not be added!" + "\n",style); }
		                    				catch (BadLocationException badLocationException){}  	                    	
				                    }
				                    
				               } catch(Exception exception){
	                    				StyleConstants.setForeground(style, Color.red);
	                    				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
	                    				catch (BadLocationException badLocationException){} 
				                } 
								
							} else {
						        StyleConstants.setForeground(style, Color.red);
						        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Price Per Car" + "\n",style); }
						        catch (BadLocationException badLocationException){}
							}
						} else {
					        StyleConstants.setForeground(style, Color.red);
					        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Number of Cars" + "\n",style); }
					        catch (BadLocationException badLocationException){}
						}
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Car Location" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					}

				} else {
			        StyleConstants.setForeground(style, Color.red);
			        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID" + "\n",style); }
			        catch (BadLocationException badLocationException){}
				}

				panelAdd.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnCAddNewCar.setForeground(SystemColor.controlHighlight);
		btnCAddNewCar.setBackground(SystemColor.window);
		btnCAddNewCar.setBounds(793, 370, 158, 54);
		panelAdd.add(btnCAddNewCar);
		
		JButton btnCClearAll = new JButton("Clear All");
		btnCClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				  txtCFlightno.setText("");
				  txtFlightSeats.setText("");
				  txtCFlightPrice.setText("");
				  txtCRoomLocation.setText("");
				  txtCNoOfRooms.setText("");
				  txtCRoomPrice.setText("");
				  txtCCarLocation.setText("");
				  txtCNoOfCars.setText("");
				  txtCCarprice.setText("");
				  customerCComboModel.setSelectedItem("-");
				  flightCComboModel.setSelectedItem("-");
				  roomCComboModel.setSelectedItem("-");
				  carCComboModel.setSelectedItem("-");
			}
		});
		btnCClearAll.setForeground(SystemColor.controlHighlight);
		btnCClearAll.setBackground(SystemColor.window);
		btnCClearAll.setBounds(945, 6, 158, 54);
		panelAdd.add(btnCClearAll);
		
		panelQuery = new JPanel();
		panelQuery.setBackground(SystemColor.menu);
		frame.getContentPane().add(panelQuery, "name_478996513544780");
		panelQuery.setLayout(null);
		
		JLabel lblQuery = new JLabel("QUERY OPERATIONS");
		lblQuery.setHorizontalAlignment(SwingConstants.CENTER);
		lblQuery.setForeground(SystemColor.controlHighlight);
		lblQuery.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		lblQuery.setBounds(409, 24, 236, 55);
		panelQuery.add(lblQuery);
		
		JLabel lblQCustomer = new JLabel("CUSTOMER");
		lblQCustomer.setHorizontalAlignment(SwingConstants.RIGHT);
		lblQCustomer.setForeground(Color.GRAY);
		lblQCustomer.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblQCustomer.setBounds(29, 120, 125, 55);
		panelQuery.add(lblQCustomer);
		
		JComboBox<String> comboBoxQCustomer = new JComboBox<String>(customerQComboModel);
		comboBoxQCustomer.setBounds(203, 122, 70, 55);
		panelQuery.add(comboBoxQCustomer);
		
		txtQCustomerid = new JTextField();
		txtQCustomerid.setHorizontalAlignment(SwingConstants.CENTER);
		txtQCustomerid.setForeground(SystemColor.controlHighlight);
		txtQCustomerid.setColumns(10);
		txtQCustomerid.setBounds(336, 130, 96, 34);
		panelQuery.add(txtQCustomerid);
		txtQCustomerid.setUI(new JTextFieldHintUI("Customer_ID", UIManager.getColor("Button.light")));
		
		JButton btnQQueryCustomer = new JButton("Query Customer");
		btnQQueryCustomer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String queryCustomer = new String();
				String transactionId = comboBoxQCustomer.getSelectedItem().toString();
				String customerId = txtQCustomerid.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!customerId.isEmpty() && checkIfNumber(customerId, "Customer ID")) {
						
						queryCustomer = "querycustomer" + "," + transactionId + "," + customerId;
						
				        StyleConstants.setForeground(style, Color.blue); 
				        try { doc.insertString(doc.getLength(), "[INPUT]: " + queryCustomer + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Querying Customer Information using Transaction ID: " + transactionId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Customer ID: " + customerId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
						txtQCustomerid.setText("");
						comboBoxQCustomer.setSelectedItem("-");
						
		                try {
		                    	String bill = rm.queryCustomerInfo(Integer.parseInt(transactionId),Integer.parseInt(customerId));
            					StyleConstants.setForeground(style, Color.green);
            					try { doc.insertString(doc.getLength(), "Customer Info: " + bill + "\n",style); }
            					catch (BadLocationException badLocationException){} 
		                } catch (Exception exception){
                				StyleConstants.setForeground(style, Color.red);
                				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
                				catch (BadLocationException badLocationException){} 
		                } 		                   
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the customer ID" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					} 
				
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
			}
				panelQuery.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnQQueryCustomer.setForeground(SystemColor.controlHighlight);
		btnQQueryCustomer.setBackground(SystemColor.window);
		btnQQueryCustomer.setBounds(646, 120, 158, 45);
		panelQuery.add(btnQQueryCustomer);
		
		JLabel lblQFlight = new JLabel("FLIGHT");
		lblQFlight.setHorizontalAlignment(SwingConstants.RIGHT);
		lblQFlight.setForeground(Color.GRAY);
		lblQFlight.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblQFlight.setBounds(29, 170, 125, 55);
		panelQuery.add(lblQFlight);
		
		JComboBox<String> comboBoxQFlight = new JComboBox<String>(flightQComboModel);
		comboBoxQFlight.setBounds(203, 172, 70, 55);
		panelQuery.add(comboBoxQFlight);
		
		txtQFlightno = new JTextField();
		txtQFlightno.setHorizontalAlignment(SwingConstants.CENTER);
		txtQFlightno.setForeground(SystemColor.controlHighlight);
		txtQFlightno.setColumns(10);
		txtQFlightno.setBounds(336, 180, 96, 34);
		panelQuery.add(txtQFlightno);
		txtQFlightno.setUI(new JTextFieldHintUI("Flight_No", UIManager.getColor("Button.light")));
		
		JButton btnQQueryFlight = new JButton("Query Flight");
		btnQQueryFlight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String queryFlight = new String();
				String transactionId = comboBoxQFlight.getSelectedItem().toString();
				String flightNo = txtQFlightno.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!flightNo.isEmpty() && checkIfNumber(flightNo, "Flight No. ")) {
						
						queryFlight = "queryflight" + "," + transactionId + "," + flightNo;
						
				        StyleConstants.setForeground(style, Color.blue);
				        try { doc.insertString(doc.getLength(),"[INPUT]: " + queryFlight + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(),"Querying Flight Information using Transaction ID: " + transactionId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(),"Flight No: " + flightNo + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(),"-----------------------------------------" + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
						txtQFlightno.setText("");
						comboBoxQFlight.setSelectedItem("-");
						
		                try {
		                		int seats = rm.queryFlight(Integer.parseInt(transactionId), Integer.parseInt(flightNo));
		                		if (seats < 0) {
		                			StyleConstants.setForeground(style, Color.red);
		                			try { doc.insertString(doc.getLength(), "This resource is used by someone else, please try again later" + "\n",style); }
		                			catch (BadLocationException badLocationException){} 
		                		} else {
		                			StyleConstants.setForeground(style, Color.green);
		                			try { doc.insertString(doc.getLength(), "Number of seats available" + "\n",style); }
		                			catch (BadLocationException badLocationException){} 
		                		}
		                } catch (Exception exception){
		                			StyleConstants.setForeground(style, Color.red);
		                			try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
		                			catch (BadLocationException badLocationException){} 
		                	}       
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Flight Number" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					} 
				
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
			}
				panelQuery.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnQQueryFlight.setForeground(SystemColor.controlHighlight);
		btnQQueryFlight.setBackground(SystemColor.window);
		btnQQueryFlight.setBounds(646, 170, 158, 45);
		panelQuery.add(btnQQueryFlight);
		
		JLabel lblQRoom = new JLabel("ROOM");
		lblQRoom.setHorizontalAlignment(SwingConstants.RIGHT);
		lblQRoom.setForeground(Color.GRAY);
		lblQRoom.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblQRoom.setBounds(29, 220, 125, 55);
		panelQuery.add(lblQRoom);
		
		JComboBox<String> comboBoxQRoom = new JComboBox<String>(roomQComboModel);
		comboBoxQRoom.setBounds(203, 222, 70, 55);
		panelQuery.add(comboBoxQRoom);
		
		txtQRoomLocation = new JTextField();
		txtQRoomLocation.setHorizontalAlignment(SwingConstants.CENTER);
		txtQRoomLocation.setForeground(SystemColor.controlHighlight);
		txtQRoomLocation.setColumns(10);
		txtQRoomLocation.setBounds(336, 230, 96, 34);
		panelQuery.add(txtQRoomLocation);
		txtQRoomLocation.setUI(new JTextFieldHintUI("Location", UIManager.getColor("Button.light")));
		
		JButton btnQQueryRoom = new JButton("Query Room");
		btnQQueryRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String queryRoom = new String();
				String transactionId = comboBoxQRoom.getSelectedItem().toString();
				String location = txtQRoomLocation.getText();
				
				if ((transactionId != null) && (transactionId != "-")) {
					if (!location.isEmpty()  && (location != "Location")) {
						queryRoom = "queryroom" + "," + transactionId + "," + location;

				        StyleConstants.setForeground(style, Color.blue); 
				        try { doc.insertString(doc.getLength(), "[INPUT]: " + queryRoom + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Querying Room Information using Transaction ID: " + transactionId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Room Location: " + location + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
						txtQRoomLocation.setText("");
						comboBoxQRoom.setSelectedItem("-");
						
		                try {
                				int numRooms = rm.queryRooms(Integer.parseInt(transactionId), location);
                				if (numRooms < 0) {
                					StyleConstants.setForeground(style, Color.red);
                					try { doc.insertString(doc.getLength(), "This resource is used by someone else, please try again later" + "\n",style); }
                					catch (BadLocationException badLocationException){} 
                				} else {
                					StyleConstants.setForeground(style, Color.green);
                					try { doc.insertString(doc.getLength(), "Number of Rooms at this location: " + "\n", style); }
                					catch (BadLocationException badLocationException){} 
                				}
		                } catch (Exception exception){
		                		StyleConstants.setForeground(style, Color.red);
		                		try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n", style); }
		                		catch (BadLocationException badLocationException){} 
		                } 
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Room Location" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					} 
				
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
			}
								
				panelQuery.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnQQueryRoom.setForeground(SystemColor.controlHighlight);
		btnQQueryRoom.setBackground(SystemColor.window);
		btnQQueryRoom.setBounds(646, 220, 158, 45);
		panelQuery.add(btnQQueryRoom);
		
		JLabel lblQCar = new JLabel("CAR");
		lblQCar.setHorizontalAlignment(SwingConstants.RIGHT);
		lblQCar.setForeground(Color.GRAY);
		lblQCar.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblQCar.setBounds(29, 270, 125, 55);
		panelQuery.add(lblQCar);
		
		JComboBox<String> comboBoxQCar = new JComboBox<String>(carQComboModel);
		comboBoxQCar.setBounds(203, 272, 70, 55);
		panelQuery.add(comboBoxQCar);
		
		txtQCarLocation = new JTextField();
		txtQCarLocation.setHorizontalAlignment(SwingConstants.CENTER);
		txtQCarLocation.setForeground(SystemColor.controlHighlight);
		txtQCarLocation.setColumns(10);
		txtQCarLocation.setBounds(336, 280, 96, 34);
		panelQuery.add(txtQCarLocation);
		txtQCarLocation.setUI(new JTextFieldHintUI("Location", UIManager.getColor("Button.light")));
		
		JButton btnQQueryCar = new JButton("Query Car");
		btnQQueryCar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String queryCar = new String();
				String transactionId = comboBoxQCar.getSelectedItem().toString();
				String location = txtQCarLocation.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!location.isEmpty()  && (location != "Location")) {
						queryCar = "querycar" + "," + transactionId + "," + location;
						
				        try { doc.insertString(doc.getLength(), "[INPUT]: " + queryCar + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Querying Car Information using Transaction ID: " + transactionId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Car Location: " + location + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
						txtQCarLocation.setText("");
						comboBoxQCar.setSelectedItem("-");
						
		                try {
	                			int numCars = rm.queryCars(Integer.parseInt(transactionId), location);
	                			if (numCars < 0) {
	                				StyleConstants.setForeground(style, Color.red);
	                				try { doc.insertString(doc.getLength(), "This resource is used by someone else, please try again later" + "\n",style); }
	                				catch (BadLocationException badLocationException){} 
	                			} else {
	                				StyleConstants.setForeground(style, Color.green);
	                				try { doc.insertString(doc.getLength(), "Number of Cars at this location: " + "\n", style); }
	                				catch (BadLocationException badLocationException){} 
	                			}
		                } catch (Exception exception){
	                			StyleConstants.setForeground(style, Color.red);
	                			try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n", style); }
	                			catch (BadLocationException badLocationException){} 
		                } 
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Car Location" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					} 
				
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
			}
				
				panelQuery.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnQQueryCar.setForeground(SystemColor.controlHighlight);
		btnQQueryCar.setBackground(SystemColor.window);
		btnQQueryCar.setBounds(646, 270, 158, 45);
		panelQuery.add(btnQQueryCar);
		
		JLabel lblQFlightPrice = new JLabel("FLIGHT PRICE");
		lblQFlightPrice.setHorizontalAlignment(SwingConstants.RIGHT);
		lblQFlightPrice.setForeground(Color.GRAY);
		lblQFlightPrice.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblQFlightPrice.setBounds(29, 320, 125, 55);
		panelQuery.add(lblQFlightPrice);
		
		JComboBox<String> comboBoxQFlightPrice = new JComboBox<String>(flightPriceQComboModel);
		comboBoxQFlightPrice.setBounds(203, 320, 70, 55);
		panelQuery.add(comboBoxQFlightPrice);
		
		txtQPriceFlightno = new JTextField();
		txtQPriceFlightno.setHorizontalAlignment(SwingConstants.CENTER);
		txtQPriceFlightno.setForeground(SystemColor.controlHighlight);
		txtQPriceFlightno.setColumns(10);
		txtQPriceFlightno.setBounds(336, 330, 96, 34);
		panelQuery.add(txtQPriceFlightno);
		txtQPriceFlightno.setUI(new JTextFieldHintUI("Flight_No", UIManager.getColor("Button.light")));
		
		JButton btnQQueryFlightPrice = new JButton("Query Flight Price");
		btnQQueryFlightPrice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String queryFlightPrice = new String();
				String transactionId = comboBoxQFlightPrice.getSelectedItem().toString();
				String flightNo = txtQPriceFlightno.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!flightNo.isEmpty() && checkIfNumber(flightNo, "Flight No. ")) {
						
						queryFlightPrice = "queryflightprice" + "," + transactionId + "," + flightNo;
						
				        StyleConstants.setForeground(style, Color.blue); 
				        try { doc.insertString(doc.getLength(), "[INPUT]: " + queryFlightPrice + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Querying Flight Price Information using Transaction ID: " + transactionId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Flight No: " + flightNo + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
						txtQPriceFlightno.setText("");
						comboBoxQFlightPrice.setSelectedItem("-");
						
		                try {
	                			int price = rm.queryFlightPrice(Integer.parseInt(transactionId), Integer.parseInt(flightNo));
	                			if (price < 0) {
	                				StyleConstants.setForeground(style, Color.red);
	                				try { doc.insertString(doc.getLength(), "This resource is used by someone else, please try again later" + "\n",style); }
	                				catch (BadLocationException badLocationException){} 
	                			} else {
	                				StyleConstants.setForeground(style, Color.green);
	                				try { doc.insertString(doc.getLength(), "Price of a Seat: " + price + "\n",style); }
	                				catch (BadLocationException badLocationException){} 
	                			}
		                } catch (Exception exception){
	                			StyleConstants.setForeground(style, Color.red);
	                			try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
	                			catch (BadLocationException badLocationException){} 
	                	} 
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Flight Number" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					} 
				
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
			}
				
				panelQuery.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnQQueryFlightPrice.setForeground(SystemColor.controlHighlight);
		btnQQueryFlightPrice.setBackground(SystemColor.window);
		btnQQueryFlightPrice.setBounds(646, 320, 158, 45);
		panelQuery.add(btnQQueryFlightPrice);
		
		JLabel lblQRoomPrice = new JLabel("ROOM PRICE");
		lblQRoomPrice.setHorizontalAlignment(SwingConstants.RIGHT);
		lblQRoomPrice.setForeground(Color.GRAY);
		lblQRoomPrice.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblQRoomPrice.setBounds(29, 370, 125, 55);
		panelQuery.add(lblQRoomPrice);
		
		JComboBox<String> comboBoxQRoomPrice = new JComboBox<String>(roomPriceQComboModel);
		comboBoxQRoomPrice.setBounds(203, 370, 70, 55);
		panelQuery.add(comboBoxQRoomPrice);
		
		txtQPriceRoomLocation = new JTextField();
		txtQPriceRoomLocation.setHorizontalAlignment(SwingConstants.CENTER);
		txtQPriceRoomLocation.setForeground(SystemColor.controlHighlight);
		txtQPriceRoomLocation.setColumns(10);
		txtQPriceRoomLocation.setBounds(336, 380, 96, 34);
		panelQuery.add(txtQPriceRoomLocation);
		txtQPriceRoomLocation.setUI(new JTextFieldHintUI("Location", UIManager.getColor("Button.light")));
		
		JButton btnQQueryRoomPrice = new JButton("Query Room Price");
		btnQQueryRoomPrice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String queryRoomPrice = new String();
				String transactionId = comboBoxQRoomPrice.getSelectedItem().toString();
				String location = txtQPriceRoomLocation.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!location.isEmpty()  && (location != "Location")) {
						
						queryRoomPrice = "queryroomprice" + "," + transactionId + "," + location;

				        StyleConstants.setForeground(style, Color.blue); 
				        try { doc.insertString(doc.getLength(), "[INPUT]: " + queryRoomPrice + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Querying Room Price Information using Transaction ID: " + transactionId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Room Location: " + location + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
						txtQPriceRoomLocation.setText("");
						comboBoxQRoomPrice.setSelectedItem("-");
						
		                try {
            					int price = rm.queryRoomsPrice(Integer.parseInt(transactionId), location);
            					if (price < 0) {
            						StyleConstants.setForeground(style, Color.red);
            						try { doc.insertString(doc.getLength(), "This resource is used by someone else, please try again later" + "\n",style); }
            						catch (BadLocationException badLocationException){} 
            					} else {
            						StyleConstants.setForeground(style, Color.green);
            						try { doc.insertString(doc.getLength(), "Price of a Room at this location: " + price + "\n", style); }
            						catch (BadLocationException badLocationException){} 
            					}
		                } catch (Exception exception){
	                			StyleConstants.setForeground(style, Color.red);
	                			try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n", style); }
	                			catch (BadLocationException badLocationException){} 
		                } 
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Room Location" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					} 
				
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
			}
				
				panelQuery.setVisible(false);
				panelMenu.setVisible(true);
			}
		});

		
		btnQQueryRoomPrice.setForeground(SystemColor.controlHighlight);
		btnQQueryRoomPrice.setBackground(SystemColor.window);
		btnQQueryRoomPrice.setBounds(646, 370, 158, 45);
		panelQuery.add(btnQQueryRoomPrice);
		
		JLabel lblQCarPrice = new JLabel("CAR PRICE");
		lblQCarPrice.setHorizontalAlignment(SwingConstants.RIGHT);
		lblQCarPrice.setForeground(Color.GRAY);
		lblQCarPrice.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblQCarPrice.setBounds(29, 420, 125, 55);
		panelQuery.add(lblQCarPrice);
		
		JComboBox<String> comboBoxQCarPrice = new JComboBox<String>(carPriceQComboModel);
		comboBoxQCarPrice.setBounds(203, 420, 70, 55);
		panelQuery.add(comboBoxQCarPrice);
		
		txtQPriceCarLocation = new JTextField();
		txtQPriceCarLocation.setHorizontalAlignment(SwingConstants.CENTER);
		txtQPriceCarLocation.setForeground(SystemColor.controlHighlight);
		txtQPriceCarLocation.setColumns(10);
		txtQPriceCarLocation.setBounds(336, 430, 96, 34);
		panelQuery.add(txtQPriceCarLocation);
		txtQPriceCarLocation.setUI(new JTextFieldHintUI("Location", UIManager.getColor("Button.light")));
		
		JButton btnQQueryCarPrice = new JButton("Query Car Price");
		btnQQueryCarPrice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String queryCarPrice = new String();
				String transactionId = comboBoxQCarPrice.getSelectedItem().toString();
				String location = txtQPriceCarLocation.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!location.isEmpty() && (location != "Location")) {
						queryCarPrice = "querycarprice" + "," + transactionId + "," + location;
						
				        StyleConstants.setForeground(style, Color.blue); 
				        try { doc.insertString(doc.getLength(), "[INPUT]: " + queryCarPrice + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Querying Car Price Information using Transaction ID: " + transactionId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Car Location: " + location + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
						txtQPriceCarLocation.setText("");
						comboBoxQCarPrice.setSelectedItem("-");
						
		                try {
                				int price = rm.queryCarsPrice(Integer.parseInt(transactionId), location);
                				if (price < 0) {
                					StyleConstants.setForeground(style, Color.red);
                					try { doc.insertString(doc.getLength(), "This resource is used by someone else, please try again later" + "\n",style); }
                					catch (BadLocationException badLocationException){} 
                				} else {
                					StyleConstants.setForeground(style, Color.green);
                					try { doc.insertString(doc.getLength(), "Price of a Car at this location: " + price + "\n", style); }
                					catch (BadLocationException badLocationException){} 
                				}
		                } catch (Exception exception){
		                		StyleConstants.setForeground(style, Color.red);
		                		try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n", style); }
		                		catch (BadLocationException badLocationException){} 
		                } 
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Car Location" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					} 
				
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
			}

				panelQuery.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnQQueryCarPrice.setForeground(SystemColor.controlHighlight);
		btnQQueryCarPrice.setBackground(SystemColor.window);
		btnQQueryCarPrice.setBounds(646, 420, 158, 45);
		panelQuery.add(btnQQueryCarPrice);
		
		JButton btnQClearAll = new JButton("Clear All");
		btnQClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				txtQCustomerid.setText("");
				txtQFlightno.setText("");
				txtQRoomLocation.setText("");
				txtQCarLocation.setText("");
				txtQPriceFlightno.setText("");
				txtQPriceRoomLocation.setText("");
				txtQPriceCarLocation.setText("");
				customerQComboModel.setSelectedItem("-");
				flightQComboModel.setSelectedItem("-");
				roomQComboModel.setSelectedItem("-");
				carQComboModel.setSelectedItem("-");
				flightPriceQComboModel.setSelectedItem("-");
				roomPriceQComboModel.setSelectedItem("-");
				carPriceQComboModel.setSelectedItem("-");
			}
		});
		btnQClearAll.setForeground(SystemColor.controlHighlight);
		btnQClearAll.setBackground(SystemColor.window);
		btnQClearAll.setBounds(925, 24, 158, 45);
		panelQuery.add(btnQClearAll);
		
		panelDelete = new JPanel();
		panelDelete.setBackground(SystemColor.menu);
		frame.getContentPane().add(panelDelete, "name_479001349860660");
		panelDelete.setLayout(null);
		
		JLabel lblDelete = new JLabel("DELETE OPERATIONS");
		lblDelete.setHorizontalAlignment(SwingConstants.CENTER);
		lblDelete.setForeground(SystemColor.controlHighlight);
		lblDelete.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		lblDelete.setBounds(409, 24, 236, 55);
		panelDelete.add(lblDelete);
		
		JLabel lblDCustomer = new JLabel("CUSTOMER");
		lblDCustomer.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDCustomer.setForeground(Color.GRAY);
		lblDCustomer.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblDCustomer.setBounds(29, 190, 125, 55);
		panelDelete.add(lblDCustomer);

		JComboBox<String> comboBoxDCustomer = new JComboBox<String>(customerDComboModel);
		comboBoxDCustomer.setBounds(194, 190, 70, 55);
		panelDelete.add(comboBoxDCustomer);
		
		txtDCustomerId = new JTextField();
		txtDCustomerId.setHorizontalAlignment(SwingConstants.CENTER);
		txtDCustomerId.setForeground(SystemColor.controlHighlight);
		txtDCustomerId.setColumns(10);
		txtDCustomerId.setBounds(306, 199, 96, 34);
		panelDelete.add(txtDCustomerId);
		txtDCustomerId.setUI(new JTextFieldHintUI("Customer_ID", UIManager.getColor("Button.light")));
		
		JButton btnDeleteCustomer = new JButton("Delete Customer");
		btnDeleteCustomer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String deleteCustomer = new String();
				String transactionId = comboBoxDCustomer.getSelectedItem().toString();
				String customerId = txtDCustomerId.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!customerId.isEmpty() && checkIfNumber(customerId, "Customer ID")) {
						
						deleteCustomer = "deletecustomer" + "," + transactionId + "," + customerId;
						
				        StyleConstants.setForeground(style, Color.blue); 
				        try { doc.insertString(doc.getLength(), "[INPUT]: " + deleteCustomer + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Delete Customer Information using Transaction ID: " + transactionId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Customer ID: " + customerId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
				        txtDCustomerId.setText("");
				        comboBoxDCustomer.setSelectedItem("-");
				        
		                try {
		                    if(rm.deleteCustomer(Integer.parseInt(transactionId), Integer.parseInt(customerId))) {
		                    		StyleConstants.setForeground(style, Color.green);
		                    		try { doc.insertString(doc.getLength(), "Customer Deleted!" + "\n",style); }
		                    		catch (BadLocationException badLocationException){}    		
		                    } else {
	                    			StyleConstants.setForeground(style, Color.red);
	                    			try { doc.insertString(doc.getLength(), "Customer could not be deleted!" + "\n",style); }
	                    			catch (BadLocationException badLocationException){}  	                    	
		                    }
		                } catch (Exception exception){
                				StyleConstants.setForeground(style, Color.red);
                				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
                				catch (BadLocationException badLocationException){} 
		                }
				        
				        
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the customer ID" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					} 
				
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
			}
				
				panelDelete.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnDeleteCustomer.setForeground(SystemColor.controlHighlight);
		btnDeleteCustomer.setBackground(SystemColor.window);
		btnDeleteCustomer.setBounds(646, 190, 158, 54);
		panelDelete.add(btnDeleteCustomer);
		
		JLabel lblDFlight = new JLabel("FLIGHT");
		lblDFlight.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDFlight.setForeground(Color.GRAY);
		lblDFlight.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblDFlight.setBounds(29, 250, 125, 55);
		panelDelete.add(lblDFlight);

		JComboBox<String> comboBoxDFlight = new JComboBox<String>(flightDComboModel);
		comboBoxDFlight.setBounds(194, 250, 70, 55);
		panelDelete.add(comboBoxDFlight);
		
		txtDFlightNo = new JTextField();
		txtDFlightNo.setHorizontalAlignment(SwingConstants.CENTER);
		txtDFlightNo.setForeground(SystemColor.controlHighlight);
		txtDFlightNo.setColumns(10);
		txtDFlightNo.setBounds(306, 259, 96, 34);
		panelDelete.add(txtDFlightNo);
		txtDFlightNo.setUI(new JTextFieldHintUI("Flight_No.", UIManager.getColor("Button.light")));
		
		JButton btnDDeleteFlight = new JButton("Delete Flight");
		btnDDeleteFlight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					
				String deleteFlight = new String();
				String transactionId = comboBoxDFlight.getSelectedItem().toString();
				String flightNo = txtDFlightNo.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!flightNo.isEmpty() && checkIfNumber(flightNo, "Flight No. ")) {
						
						deleteFlight = "deleteflight" + "," + transactionId + "," + flightNo;
						
				        StyleConstants.setForeground(style, Color.blue);
				        try { doc.insertString(doc.getLength(),"[INPUT]: " + deleteFlight + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(),"Deleting Flight Information using Transaction ID: " + transactionId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(),"Flight No: " + flightNo + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(),"-----------------------------------------" + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
				        txtDFlightNo.setText("");
				        comboBoxDFlight.setSelectedItem("-");
		                
		                try {
		                    if(rm.deleteFlight(Integer.parseInt(transactionId), Integer.parseInt(flightNo))) {
		                    		StyleConstants.setForeground(style, Color.green);
		                    		try { doc.insertString(doc.getLength(), "Flight Deleted!" + "\n",style); }
		                    		catch (BadLocationException badLocationException){}    		
		                    } else {
	                    			StyleConstants.setForeground(style, Color.red);
	                    			try { doc.insertString(doc.getLength(), "Flight could not be deleted!" + "\n",style); }
	                    			catch (BadLocationException badLocationException){}  	                    	
		                    }
		                } catch (Exception exception){
                				StyleConstants.setForeground(style, Color.red);
                				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
                				catch (BadLocationException badLocationException){} 
		                }
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Flight Number" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					} 
				
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
			}
				panelDelete.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnDDeleteFlight.setForeground(SystemColor.controlHighlight);
		btnDDeleteFlight.setBackground(SystemColor.window);
		btnDDeleteFlight.setBounds(646, 251, 158, 54);
		panelDelete.add(btnDDeleteFlight);
		
		JLabel lblDRoom = new JLabel("ROOM");
		lblDRoom.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDRoom.setForeground(Color.GRAY);
		lblDRoom.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblDRoom.setBounds(29, 310, 125, 55);
		panelDelete.add(lblDRoom);
		

		JComboBox<String> comboBoxDRoom = new JComboBox<String>(roomDComboModel);
		comboBoxDRoom.setBounds(194, 310, 70, 55);
		panelDelete.add(comboBoxDRoom);
		
		txtDRoomLocation = new JTextField();
		txtDRoomLocation.setHorizontalAlignment(SwingConstants.CENTER);
		txtDRoomLocation.setForeground(SystemColor.controlHighlight);
		txtDRoomLocation.setColumns(10);
		txtDRoomLocation.setBounds(306, 319, 96, 34);
		panelDelete.add(txtDRoomLocation);
		txtDRoomLocation.setUI(new JTextFieldHintUI("Location", UIManager.getColor("Button.light")));
		
		JButton btnDDeleteRoom = new JButton("Delete Room");
		btnDDeleteRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String deleteRoom = new String();
				String transactionId = comboBoxDRoom.getSelectedItem().toString();
				String location = txtDRoomLocation.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!location.isEmpty()  && (location != "Location")) {
						deleteRoom = "deleteroom" + "," + transactionId + "," + location;

				        StyleConstants.setForeground(style, Color.blue); 
				        try { doc.insertString(doc.getLength(), "[INPUT]: " + deleteRoom + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Delete Room Information using Transaction ID: " + transactionId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Room Location: " + location + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
				        txtDRoomLocation.setText("");
				        comboBoxDRoom.setSelectedItem("-");
				        
		                try {
		                    if(rm.deleteRooms(Integer.parseInt(transactionId), location)) {
		                    		StyleConstants.setForeground(style, Color.green);
		                    		try { doc.insertString(doc.getLength(), "Rooms Deleted!" + "\n",style); }
		                    		catch (BadLocationException badLocationException){}    		
		                    } else {
	                    			StyleConstants.setForeground(style, Color.red);
	                    			try { doc.insertString(doc.getLength(), "Rooms could not be deleted!" + "\n",style); }
	                    			catch (BadLocationException badLocationException){}  	                    	
		                    }
		                } catch (Exception exception){
                				StyleConstants.setForeground(style, Color.red);
                				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
                				catch (BadLocationException badLocationException){} 
		                }
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Room Location" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					} 
				
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
			}

				panelDelete.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnDDeleteRoom.setForeground(SystemColor.controlHighlight);
		btnDDeleteRoom.setBackground(SystemColor.window);
		btnDDeleteRoom.setBounds(646, 311, 158, 54);
		panelDelete.add(btnDDeleteRoom);
		
		JLabel lblDCar = new JLabel("CAR");
		lblDCar.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDCar.setForeground(Color.GRAY);
		lblDCar.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblDCar.setBounds(29, 370, 125, 55);
		panelDelete.add(lblDCar);

		JComboBox<String> comboBoxDCar = new JComboBox<String>(carDComboModel);
		comboBoxDCar.setBounds(194, 370, 70, 55);
		panelDelete.add(comboBoxDCar);
		
		txtDCarLocation = new JTextField();
		txtDCarLocation.setHorizontalAlignment(SwingConstants.CENTER);
		txtDCarLocation.setForeground(SystemColor.controlHighlight);
		txtDCarLocation.setColumns(10);
		txtDCarLocation.setBounds(306, 378, 96, 34);
		panelDelete.add(txtDCarLocation);
		txtDCarLocation.setUI(new JTextFieldHintUI("Location", UIManager.getColor("Button.light")));
		
		JButton btnDDeleteCar = new JButton("Delete Car");
		btnDDeleteCar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String deleteCar = new String();
				String transactionId = comboBoxDCar.getSelectedItem().toString();
				String location = txtDCarLocation.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!location.isEmpty()  && (location != "Location")) {
						deleteCar = "deletecar" + "," + transactionId + "," + location;
						
				        try { doc.insertString(doc.getLength(), "[INPUT]: " + deleteCar + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Deleting Car Information using Transaction ID: " + transactionId + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "Car Location: " + location + "\n",style); }
				        catch (BadLocationException badLocationException){}
				        
				        StyleConstants.setForeground(style, Color.black);
				        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
				        catch (BadLocationException badLocationException){}
						
				        txtDCarLocation.setText("");
				        comboBoxDCar.setSelectedItem("-");
				        
		                try {
		                    if(rm.deleteCars(Integer.parseInt(transactionId), location)) {
		                    		StyleConstants.setForeground(style, Color.green);
		                    		try { doc.insertString(doc.getLength(), "Cars Deleted!" + "\n",style); }
		                    		catch (BadLocationException badLocationException){}    		
		                    } else {
	                    			StyleConstants.setForeground(style, Color.red);
	                    			try { doc.insertString(doc.getLength(), "Cars could not be deleted!" + "\n",style); }
	                    			catch (BadLocationException badLocationException){}  	                    	
		                    }
		                } catch (Exception exception){
                				StyleConstants.setForeground(style, Color.red);
                				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
                				catch (BadLocationException badLocationException){} 
		                }
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(),"[WARN]: Enter the Car Location" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					} 
				
			} else {
		        StyleConstants.setForeground(style, Color.red);
		        try { doc.insertString(doc.getLength(),"[WARN]: Invalid Transaction ID"  + "\n",style); }
		        catch (BadLocationException badLocationException){}
			}
				
				panelDelete.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnDDeleteCar.setForeground(SystemColor.controlHighlight);
		btnDDeleteCar.setBackground(SystemColor.window);
		btnDDeleteCar.setBounds(646, 370, 158, 54);
		panelDelete.add(btnDDeleteCar);
		
		JButton btnDClearAll = new JButton("Clear All");
		btnDClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				txtDFlightNo.setText("");
				txtDRoomLocation.setText("");
				txtDCarLocation.setText("");
				txtDCustomerId.setText("");
				roomDComboModel.setSelectedItem("-");		
				flightDComboModel.setSelectedItem("-");		
				customerDComboModel.setSelectedItem("-");		
				carDComboModel.setSelectedItem("-");	
			}
		});
		btnDClearAll.setForeground(SystemColor.controlHighlight);
		btnDClearAll.setBackground(SystemColor.window);
		btnDClearAll.setBounds(933, 24, 158, 54);
		panelDelete.add(btnDClearAll);
		
		panelReserve = new JPanel();
		panelReserve.setBackground(SystemColor.menu);
		frame.getContentPane().add(panelReserve, "name_479003708732163");
		panelReserve.setLayout(null);
		
		JLabel lblReserve = new JLabel("RESERVE OPERATIONS");
		lblReserve.setBounds(409, 24, 236, 55);
		panelReserve.add(lblReserve);
		lblReserve.setHorizontalAlignment(SwingConstants.CENTER);
		lblReserve.setForeground(SystemColor.controlHighlight);
		lblReserve.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		
		JLabel lblRFlight = new JLabel("FLIGHT");
		lblRFlight.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRFlight.setForeground(Color.GRAY);
		lblRFlight.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblRFlight.setBounds(29, 190, 125, 55);
		panelReserve.add(lblRFlight);

		JComboBox<String> comboBoxRFlight = new JComboBox<String>(flightRComboModel);
		comboBoxRFlight.setBounds(272, 190, 70, 55);
		panelReserve.add(comboBoxRFlight);
		
		txtRFlightCustomerId = new JTextField();
		txtRFlightCustomerId.setHorizontalAlignment(SwingConstants.CENTER);
		txtRFlightCustomerId.setForeground(SystemColor.controlHighlight);
		txtRFlightCustomerId.setColumns(10);
		txtRFlightCustomerId.setBounds(444, 200, 96, 34);
		panelReserve.add(txtRFlightCustomerId);
		txtRFlightCustomerId.setUI(new JTextFieldHintUI("Customer_ID", UIManager.getColor("Button.light")));
		
		txtRFlightno = new JTextField();
		txtRFlightno.setHorizontalAlignment(SwingConstants.CENTER);
		txtRFlightno.setForeground(SystemColor.controlHighlight);
		txtRFlightno.setColumns(10);
		txtRFlightno.setBounds(647, 200, 96, 34);
		panelReserve.add(txtRFlightno);
		txtRFlightno.setUI(new JTextFieldHintUI("Flight_No", UIManager.getColor("Button.light")));
		
		JButton btnRReserveFlight = new JButton("Reserve Flight");
		btnRReserveFlight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String reserveFlight = new String();
				String transactionId = comboBoxRFlight.getSelectedItem().toString();
				String flightNo = txtRFlightno.getText();
				String customerId = txtRFlightCustomerId.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!customerId.isEmpty()  && checkIfNumber(customerId, "Customer ID ")) {
						if (!flightNo.isEmpty()  && checkIfNumber(flightNo, "Flight Number")) {
								
								reserveFlight = "reserveflight" + "," + transactionId + "," + customerId + "," + flightNo;
								
								StyleConstants.setForeground(style, Color.blue);
						        String blue = "[INPUT]: " + reserveFlight  + "\n";
						        try { doc.insertString(doc.getLength(), blue,style); }
						        catch (BadLocationException badLocationException){}
								
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Reserving a new Flight using Transaction ID: " + transactionId  + "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Customer ID: " + customerId + "\n" ,style); }
						        catch (BadLocationException badLocationException){}   
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Flight Number: " + flightNo  + "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
						        catch (BadLocationException badLocationException){}    	 
								
						        txtRFlightno.setText("");
						        txtRFlightCustomerId.setText("");
								comboBoxRFlight.setSelectedItem("-");
								
				                try {
				                    		if(rm.reserveFlight(Integer.parseInt(transactionId), Integer.parseInt(customerId), Integer.parseInt(flightNo))) {
				                    		StyleConstants.setForeground(style, Color.green);
				                    		try { doc.insertString(doc.getLength(), "Flight Reserved!" + "\n",style); }
				                    		catch (BadLocationException badLocationException){}    		
				                    } else {
	                    					StyleConstants.setForeground(style, Color.red);
	                    					try { doc.insertString(doc.getLength(), "Flight could not be Reserved!" + "\n",style); }
	                    					catch (BadLocationException badLocationException){}  	                    	
				                		}
				                    
				                } catch (Exception exception) {
	                					StyleConstants.setForeground(style, Color.red);
	                					try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
	                					catch (BadLocationException badLocationException){} 
				                }
								
						} else {
					        StyleConstants.setForeground(style, Color.red);
					        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Flight number" + "\n",style); }
					        catch (BadLocationException badLocationException){}
						}
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Customer ID" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					}

				} else {
			        StyleConstants.setForeground(style, Color.red);
			        try { doc.insertString(doc.getLength(), "[WARN]: Invalid Transaction ID" + "\n",style); }
			        catch (BadLocationException badLocationException){}
				}
				
				panelReserve.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnRReserveFlight.setForeground(SystemColor.controlHighlight);
		btnRReserveFlight.setBackground(SystemColor.window);
		btnRReserveFlight.setBounds(841, 192, 158, 54);
		panelReserve.add(btnRReserveFlight);
		
		JLabel lblRRoom = new JLabel("ROOM");
		lblRRoom.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRRoom.setForeground(Color.GRAY);
		lblRRoom.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblRRoom.setBounds(29, 250, 125, 55);
		panelReserve.add(lblRRoom);

		JComboBox<String> comboBoxRRoom = new JComboBox<String>(roomRComboModel);
		comboBoxRRoom.setBounds(272, 250, 70, 55);
		panelReserve.add(comboBoxRRoom);
		
		txtRRoomCustomerId = new JTextField();
		txtRRoomCustomerId.setHorizontalAlignment(SwingConstants.CENTER);
		txtRRoomCustomerId.setForeground(SystemColor.controlHighlight);
		txtRRoomCustomerId.setColumns(10);
		txtRRoomCustomerId.setBounds(444, 260, 96, 34);
		panelReserve.add(txtRRoomCustomerId);
		txtRRoomCustomerId.setUI(new JTextFieldHintUI("Customer_ID", UIManager.getColor("Button.light")));
		
		txtRRoomLocation = new JTextField();
		txtRRoomLocation.setHorizontalAlignment(SwingConstants.CENTER);
		txtRRoomLocation.setForeground(SystemColor.controlHighlight);
		txtRRoomLocation.setColumns(10);
		txtRRoomLocation.setBounds(647, 260, 96, 34);
		panelReserve.add(txtRRoomLocation);
		txtRRoomLocation.setUI(new JTextFieldHintUI("Location", UIManager.getColor("Button.light")));
		
		JButton btnRReserveRoom = new JButton("Reserve Room");
		btnRReserveRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String reserveRoom = new String();
				String transactionId = comboBoxRRoom.getSelectedItem().toString();
				String location = txtRRoomLocation.getText();
				String customerId = txtRRoomCustomerId.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!customerId.isEmpty()  && checkIfNumber(customerId, "Customer ID ")) {
						if (!location.isEmpty()  && location != "Location") {
								
							reserveRoom = "reserveroom" + "," + transactionId + "," + customerId + "," + location;
								
								StyleConstants.setForeground(style, Color.blue);
						        String blue = "[INPUT]: " + reserveRoom  + "\n";
						        try { doc.insertString(doc.getLength(), blue,style); }
						        catch (BadLocationException badLocationException){}
								
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Reserving a new Room using Transaction ID: " + transactionId  + "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Customer ID: " + customerId + "\n" ,style); }
						        catch (BadLocationException badLocationException){}   
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Room Location: " + location  + "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
						        catch (BadLocationException badLocationException){}    	 
								
						        txtRRoomLocation.setText("");
						        txtRRoomCustomerId.setText("");
						        comboBoxRRoom.setSelectedItem("-");
						        
				                try {
	                    				if(rm.reserveRoom(Integer.parseInt(transactionId), Integer.parseInt(customerId), location)) {
	                    					StyleConstants.setForeground(style, Color.green);
	                    					try { doc.insertString(doc.getLength(), "Room Reserved!" + "\n",style); }
	                    					catch (BadLocationException badLocationException){}    		
	                    				} else {
	                    						StyleConstants.setForeground(style, Color.red);
	                    						try { doc.insertString(doc.getLength(), "Room could not be Reserved!" + "\n",style); }
	                    						catch (BadLocationException badLocationException){}  	                    	
	                    				}
	                    
				                } catch (Exception exception) {
			                			StyleConstants.setForeground(style, Color.red);
			                			try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
			                			catch (BadLocationException badLocationException){} 
				                }	
								
						} else {
					        StyleConstants.setForeground(style, Color.red);
					        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Room Location" + "\n",style); }
					        catch (BadLocationException badLocationException){}
						}
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Customer ID" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					}

				} else {
			        StyleConstants.setForeground(style, Color.red);
			        try { doc.insertString(doc.getLength(), "[WARN]: Invalid Transaction ID" + "\n",style); }
			        catch (BadLocationException badLocationException){}
				}
				
				panelReserve.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnRReserveRoom.setForeground(SystemColor.controlHighlight);
		btnRReserveRoom.setBackground(SystemColor.window);
		btnRReserveRoom.setBounds(841, 252, 158, 54);
		panelReserve.add(btnRReserveRoom);
		
		JLabel lblRCar = new JLabel("CAR");
		lblRCar.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRCar.setForeground(Color.GRAY);
		lblRCar.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblRCar.setBounds(29, 310, 125, 55);
		panelReserve.add(lblRCar);

		JComboBox<String> comboBoxRCar = new JComboBox<String>(carRComboModel);
		comboBoxRCar.setBounds(272, 310, 70, 55);
		panelReserve.add(comboBoxRCar);
		
		txtRCarCustomerId = new JTextField();
		txtRCarCustomerId.setHorizontalAlignment(SwingConstants.CENTER);
		txtRCarCustomerId.setForeground(SystemColor.controlHighlight);
		txtRCarCustomerId.setColumns(10);
		txtRCarCustomerId.setBounds(444, 319, 96, 34);
		panelReserve.add(txtRCarCustomerId);
		txtRCarCustomerId.setUI(new JTextFieldHintUI("Customer_ID", UIManager.getColor("Button.light")));
		
		txtRCarLocation = new JTextField();
		txtRCarLocation.setHorizontalAlignment(SwingConstants.CENTER);
		txtRCarLocation.setForeground(SystemColor.controlHighlight);
		txtRCarLocation.setColumns(10);
		txtRCarLocation.setBounds(647, 319, 96, 34);
		panelReserve.add(txtRCarLocation);
		txtRCarLocation.setUI(new JTextFieldHintUI("Location", UIManager.getColor("Button.light")));
		
		JButton btnRReserveCar = new JButton("Reserve Car");
		btnRReserveCar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String reserveCar = new String();
				String transactionId = comboBoxRCar.getSelectedItem().toString();
				String location = txtRCarLocation.getText();
				String customerId = txtRCarCustomerId.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!customerId.isEmpty()  && checkIfNumber(customerId, "Customer ID ")) {
						if (!location.isEmpty()  && location != "Location") {
								
							reserveCar = "reservecar" + "," + transactionId + "," + customerId + "," + location;
								
								StyleConstants.setForeground(style, Color.blue);
						        String blue = "[INPUT]: " + reserveCar  + "\n";
						        try { doc.insertString(doc.getLength(), blue,style); }
						        catch (BadLocationException badLocationException){}
								
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Reserving a new Car using Transaction ID: " + transactionId  + "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Customer ID: " + customerId + "\n" ,style); }
						        catch (BadLocationException badLocationException){}   
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "Car Location: " + location  + "\n",style); }
						        catch (BadLocationException badLocationException){}
						        
						        StyleConstants.setForeground(style, Color.black);
						        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
						        catch (BadLocationException badLocationException){}    	 
								
						        txtRCarLocation.setText("");
						        txtRCarCustomerId.setText("");
						        comboBoxRCar.setSelectedItem("-");
						        
				                try {
		                    			if(rm.reserveCar(Integer.parseInt(transactionId), Integer.parseInt(customerId), location)) {
		                    				StyleConstants.setForeground(style, Color.green);
		                    				try { doc.insertString(doc.getLength(), "Car Reserved!" + "\n",style); }
		                    				catch (BadLocationException badLocationException){}    		
		                    		} else {
		                    			StyleConstants.setForeground(style, Color.red);
		                    			try { doc.insertString(doc.getLength(), "Car could not be Reserved!" + "\n",style); }
		                    			catch (BadLocationException badLocationException){}  	                    	
		                    		}
		                    
				                } catch (Exception exception) {
				                		StyleConstants.setForeground(style, Color.red);
				                		try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
				                		catch (BadLocationException badLocationException){} 
				                }
								
						} else {
					        StyleConstants.setForeground(style, Color.red);
					        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Car Location" + "\n",style); }
					        catch (BadLocationException badLocationException){}
						}
						
					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Customer ID" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					}

				} else {
			        StyleConstants.setForeground(style, Color.red);
			        try { doc.insertString(doc.getLength(), "[WARN]: Invalid Transaction ID" + "\n",style); }
			        catch (BadLocationException badLocationException){}
				}
				
				panelReserve.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnRReserveCar.setForeground(SystemColor.controlHighlight);
		btnRReserveCar.setBackground(SystemColor.window);
		btnRReserveCar.setBounds(841, 311, 158, 54);
		panelReserve.add(btnRReserveCar);
		
		JLabel lblRItinerary = new JLabel("ITINERARY");
		lblRItinerary.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRItinerary.setForeground(Color.GRAY);
		lblRItinerary.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblRItinerary.setBounds(29, 370, 125, 55);
		panelReserve.add(lblRItinerary);

		JComboBox<String> comboBoxRItinerary = new JComboBox<String>(itineraryRComboModel);
		comboBoxRItinerary.setBounds(272, 377, 70, 55);
		panelReserve.add(comboBoxRItinerary);
		
		txtRItineraryCustomerId = new JTextField();
		txtRItineraryCustomerId.setHorizontalAlignment(SwingConstants.CENTER);
		txtRItineraryCustomerId.setForeground(SystemColor.controlHighlight);
		txtRItineraryCustomerId.setColumns(10);
		txtRItineraryCustomerId.setBounds(382, 386, 96, 34);
		panelReserve.add(txtRItineraryCustomerId);
		txtRItineraryCustomerId.setUI(new JTextFieldHintUI("Customer_ID", UIManager.getColor("Button.light")));
		
		txtRFlightno1 = new JTextField();
		txtRFlightno1.setHorizontalAlignment(SwingConstants.CENTER);
		txtRFlightno1.setForeground(SystemColor.controlHighlight);
		txtRFlightno1.setColumns(10);
		txtRFlightno1.setBounds(522, 386, 96, 34);
		panelReserve.add(txtRFlightno1);
		txtRFlightno1.setUI(new JTextFieldHintUI("Flight_No1", UIManager.getColor("Button.light")));
		
		txtRFlightno2 = new JTextField();
		txtRFlightno2.setHorizontalAlignment(SwingConstants.CENTER);
		txtRFlightno2.setForeground(SystemColor.controlHighlight);
		txtRFlightno2.setColumns(10);
		txtRFlightno2.setBounds(662, 386, 96, 34);
		panelReserve.add(txtRFlightno2);
		txtRFlightno2.setUI(new JTextFieldHintUI("Flight_No2", UIManager.getColor("Button.light")));
		
		txtRLocation = new JTextField();
		txtRLocation.setHorizontalAlignment(SwingConstants.CENTER);
		txtRLocation.setForeground(SystemColor.controlHighlight);
		txtRLocation.setColumns(10);
		txtRLocation.setBounds(382, 436, 96, 34);
		panelReserve.add(txtRLocation);
		txtRLocation.setUI(new JTextFieldHintUI("Location", UIManager.getColor("Button.light")));
		
		txtRNoofcars = new JTextField();
		txtRNoofcars.setHorizontalAlignment(SwingConstants.CENTER);
		txtRNoofcars.setForeground(SystemColor.controlHighlight);
		txtRNoofcars.setColumns(10);
		txtRNoofcars.setBounds(522, 436, 96, 34);
		panelReserve.add(txtRNoofcars);
		txtRNoofcars.setUI(new JTextFieldHintUI("Book_Cars?", UIManager.getColor("Button.light")));
		
		txtRNoofrooms = new JTextField();
		txtRNoofrooms.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtRNoofrooms.setHorizontalAlignment(SwingConstants.CENTER);
		txtRNoofrooms.setForeground(SystemColor.controlHighlight);
		txtRNoofrooms.setColumns(10);
		txtRNoofrooms.setBounds(662, 436, 96, 34);
		panelReserve.add(txtRNoofrooms);
		txtRNoofrooms.setUI(new JTextFieldHintUI("Book_Rooms?", UIManager.getColor("Button.light")));
		
		JButton btnRReserveItinerary = new JButton("Reserve Itinerary");
		btnRReserveItinerary.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String reserveItinerary = new String();
				String transactionId = comboBoxRItinerary.getSelectedItem().toString();
				String location = txtRLocation.getText();
				String customerId = txtRItineraryCustomerId.getText();
				String flight1 = txtRFlightno1.getText();
				String flight2 = txtRFlightno2.getText();
				String boolCars = txtRNoofcars.getText();
				String boolCRooms = txtRNoofrooms.getText();
				
				if((transactionId != null) && (transactionId != "-")) {
					if (!customerId.isEmpty()  && checkIfNumber(customerId, "Customer ID ")) {
						if(!flight1.isEmpty() && checkIfNumber(flight1, "Flight 1 ID")) {
							if(!flight2.isEmpty() && checkIfNumber(flight2, "Flight 2 ID")) {
								if (!location.isEmpty()  && location != "Location") {
									if(!boolCars.isEmpty() && ((boolCars=="true") || (boolCars == "false"))) {
										if(!boolCRooms.isEmpty() && ((boolCRooms=="true") || (boolCRooms == "false"))) {
											
											reserveItinerary = "reserveitinerary" + "," + transactionId + "," + customerId + "," + flight1 + "," + flight2 + "," + location + "," + boolCars + "," + boolCRooms;
											
											StyleConstants.setForeground(style, Color.blue);
									        String blue = "[INPUT]: " + reserveItinerary  + "\n";
									        try { doc.insertString(doc.getLength(), blue,style); }
									        catch (BadLocationException badLocationException){}
									        
									        StyleConstants.setForeground(style, Color.black);
									        try { doc.insertString(doc.getLength(), "Reserving a new Car using Transaction ID: " + transactionId  + "\n",style); }
									        catch (BadLocationException badLocationException){}
									        
									        StyleConstants.setForeground(style, Color.black);
									        try { doc.insertString(doc.getLength(), "Customer ID: " + customerId + "\n" ,style); }
									        catch (BadLocationException badLocationException){}   
									        
									        StyleConstants.setForeground(style, Color.black);
									        try { doc.insertString(doc.getLength(), "Flight 1 ID: " + flight1  + "\n",style); }
									        catch (BadLocationException badLocationException){}
									        
									        StyleConstants.setForeground(style, Color.black);
									        try { doc.insertString(doc.getLength(), "Flight 2 ID: " + flight2  + "\n",style); }
									        catch (BadLocationException badLocationException){}
									        
									        StyleConstants.setForeground(style, Color.black);
									        try { doc.insertString(doc.getLength(), "Room [or] Car Location: " + location  + "\n",style); }
									        catch (BadLocationException badLocationException){}
									        
									        StyleConstants.setForeground(style, Color.black);
									        try { doc.insertString(doc.getLength(), "Are Rooms Booked? " + boolCRooms  + "\n",style); }
									        catch (BadLocationException badLocationException){}
									        
									        StyleConstants.setForeground(style, Color.black);
									        try { doc.insertString(doc.getLength(), "Are Cars Booked? " + boolCars  + "\n",style); }
									        catch (BadLocationException badLocationException){}
									        
									        StyleConstants.setForeground(style, Color.black);
									        try { doc.insertString(doc.getLength(), "-----------------------------------------" + "\n",style); }
									        catch (BadLocationException badLocationException){}    	   
									        
									        txtRLocation.setText("");
									        txtRItineraryCustomerId.setText("");
									        txtRFlightno1.setText("");
									        txtRFlightno2.setText("");
									        txtRNoofcars.setText("");
									        txtRNoofrooms.setText("");
									        comboBoxRItinerary.setSelectedItem("-");
									        
									        Vector flightNumbers = new Vector();
						                    
						                    flightNumbers.addElement(flight1);
						                    flightNumbers.addElement(flight2);
									        
							                try {
			                    				if(rm.itinerary(Integer.parseInt(transactionId), Integer.parseInt(customerId), flightNumbers, location, Boolean.parseBoolean(boolCars), Boolean.parseBoolean(boolCRooms))) {
			                    					StyleConstants.setForeground(style, Color.green);
			                    					try { doc.insertString(doc.getLength(), "Itinerary Reserved!" + "\n",style); }
			                    					catch (BadLocationException badLocationException){}    		
			                    				} else {
			                    						StyleConstants.setForeground(style, Color.red);
			                    						try { doc.insertString(doc.getLength(), "Itinerary could not be Reserved!" + "\n",style); }
			                    						catch (BadLocationException badLocationException){}  	                    	
			                    				}
			                    
						                } catch (Exception exception) {
					                			StyleConstants.setForeground(style, Color.red);
					                			try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
					                			catch (BadLocationException badLocationException){} 
						                }	
													        
										} else {
									        StyleConstants.setForeground(style, Color.red);
									        try { doc.insertString(doc.getLength(), "[WARN]: Enter true [or] false for Booking Rooms" + "\n",style); }
									        catch (BadLocationException badLocationException){}
										}
										
									} else {
								        StyleConstants.setForeground(style, Color.red);
								        try { doc.insertString(doc.getLength(), "[WARN]: Enter true [or] false for Booking Cars" + "\n",style); }
								        catch (BadLocationException badLocationException){}
									}
													
								} else {
							        StyleConstants.setForeground(style, Color.red);
							        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Location to book Cars or Rooms" + "\n",style); }
							        catch (BadLocationException badLocationException){}
								}
							} else {
						        StyleConstants.setForeground(style, Color.red);
						        try { doc.insertString(doc.getLength(), "[WARN]: Enter the ID of Flight 2" + "\n",style); }
						        catch (BadLocationException badLocationException){}
							}
						} else {
					        StyleConstants.setForeground(style, Color.red);
					        try { doc.insertString(doc.getLength(), "[WARN]: Enter the ID of Flight 1" + "\n",style); }
					        catch (BadLocationException badLocationException){}
						}

					} else {
				        StyleConstants.setForeground(style, Color.red);
				        try { doc.insertString(doc.getLength(), "[WARN]: Enter the Customer ID" + "\n",style); }
				        catch (BadLocationException badLocationException){}
					}

				} else {
			        StyleConstants.setForeground(style, Color.red);
			        try { doc.insertString(doc.getLength(), "[WARN]: Invalid Transaction ID" + "\n",style); }
			        catch (BadLocationException badLocationException){}
				}
					
				panelReserve.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnRReserveItinerary.setForeground(SystemColor.controlHighlight);
		btnRReserveItinerary.setBackground(SystemColor.window);
		btnRReserveItinerary.setBounds(841, 386, 158, 84);
		panelReserve.add(btnRReserveItinerary);
		
		JButton btnRClearAll = new JButton("Clear All");
		btnRClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				txtRFlightno.setText("");
				txtRRoomLocation.setText("");
				txtRCarLocation.setText("");
				txtRFlightCustomerId.setText("");
				txtRRoomCustomerId.setText("");
				txtRCarCustomerId.setText("");
				txtRItineraryCustomerId.setText("");
				txtRFlightno1.setText("");
				txtRFlightno2.setText("");
				txtRLocation.setText("");
				txtRNoofcars.setText("");
				txtRNoofrooms.setText("");
				flightRComboModel.setSelectedItem("-");	
				roomRComboModel.setSelectedItem("-");
				carRComboModel.setSelectedItem("-");
				itineraryRComboModel.setSelectedItem("-");
			}
		});
		btnRClearAll.setForeground(SystemColor.controlHighlight);
		btnRClearAll.setBackground(SystemColor.window);
		btnRClearAll.setBounds(929, 26, 158, 54);
		panelReserve.add(btnRClearAll);
		
		panelTransaction = new JPanel();
		panelTransaction.setBackground(SystemColor.menu);
		frame.getContentPane().add(panelTransaction, "name_479033938227253");
		panelTransaction.setLayout(null);
		
		JLabel lblTransaction = new JLabel("TRANSACTION OPERATIONS");
		lblTransaction.setHorizontalAlignment(SwingConstants.CENTER);
		lblTransaction.setForeground(SystemColor.controlHighlight);
		lblTransaction.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		lblTransaction.setBounds(421, 24, 236, 55);
		panelTransaction.add(lblTransaction);
		
		JButton btnTStart = new JButton("START TRANSACTION");
		btnTStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String startTransaction = "start";
		        StyleConstants.setForeground(style, Color.pink);
		        try { doc.insertString(doc.getLength(), startTransaction + "\n",style); }
		        catch (BadLocationException badLocationException){}
		        
            		try {
            			int transactionId = rm.start();
            				txnIds.add(transactionId);
            		        StyleConstants.setForeground(style, Color.pink);
            		        try { doc.insertString(doc.getLength(), "New Transaction ID: " + transactionId + "\n",style); }
            		        catch (BadLocationException badLocationException){}
	                } catch (Exception exception) {
            				StyleConstants.setForeground(style, Color.red);
            				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
            				catch (BadLocationException badLocationException){} 
	                }	
				
				panelTransaction.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		btnTStart.setForeground(SystemColor.controlHighlight);
		btnTStart.setBackground(SystemColor.window);
		btnTStart.setBounds(409, 190, 261, 64);
		panelTransaction.add(btnTStart);
		
		txtTCommitTransactionId = new JTextField();
		txtTCommitTransactionId.setHorizontalAlignment(SwingConstants.CENTER);
		txtTCommitTransactionId.setForeground(SystemColor.controlHighlight);
		txtTCommitTransactionId.setColumns(10);
		txtTCommitTransactionId.setBounds(234, 304, 123, 34);
		txtTCommitTransactionId.setUI(new JTextFieldHintUI("Transaction ID", UIManager.getColor("Button.light")));
		panelTransaction.add(txtTCommitTransactionId);

		
		JButton btnTCommit = new JButton("COMMIT TRANSACTION");
		btnTCommit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String transactionID = txtTCommitTransactionId.getText();
				String commitTransaction = new String();
				
				if((!transactionID.isEmpty()) && (checkIfNumber(transactionID, "Transaction ID"))) {
					
					commitTransaction = "commit" + "," + transactionID;
					
			        StyleConstants.setForeground(style, Color.pink);
			        try { doc.insertString(doc.getLength(), commitTransaction + "\n",style); }
			        catch (BadLocationException badLocationException){}
			        
			        txtTCommitTransactionId.setText("");
			        
			        try {
            		        StyleConstants.setForeground(style, Color.pink);
            		        try { doc.insertString(doc.getLength(), "Committing Transaction ID: " + transactionID + "\n",style); }
            		        catch (BadLocationException badLocationException){}
	            			rm.commit(Integer.parseInt(transactionID));   //always succeeds for now
	            			txnIds.removeElement(Integer.parseInt(transactionID));
	            			
            		        StyleConstants.setForeground(style, Color.pink);
            		        try { doc.insertString(doc.getLength(), "Committed Transaction ID: " + transactionID + "\n",style); }
            		        catch (BadLocationException badLocationException){}
            		        
	                } catch (Exception exception) {
            				StyleConstants.setForeground(style, Color.red);
            				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
            				catch (BadLocationException badLocationException){} 
	                }	
					
				} else {
			        StyleConstants.setForeground(style, Color.red);
			        try { doc.insertString(doc.getLength(), "[WARN]: The Transaction " + transactionID + " is invalid" + "\n",style); }
			        catch (BadLocationException badLocationException){}
				}
				
				panelTransaction.setVisible(false);
				panelMenu.setVisible(true);
			}
		});
		
		btnTCommit.setForeground(SystemColor.controlHighlight);
		btnTCommit.setBackground(SystemColor.window);
		btnTCommit.setBounds(409, 290, 261, 64);
		panelTransaction.add(btnTCommit);

		txtTAbortTransactionID = new JTextField();
		txtTAbortTransactionID.setHorizontalAlignment(SwingConstants.CENTER);
		txtTAbortTransactionID.setForeground(SystemColor.controlHighlight);
		txtTAbortTransactionID.setColumns(10);
		txtTAbortTransactionID.setBounds(234, 404, 123, 34);
		txtTAbortTransactionID.setUI(new JTextFieldHintUI("Transaction ID", UIManager.getColor("Button.light")));
		panelTransaction.add(txtTAbortTransactionID);
		
		JButton btnTAbort = new JButton("ABORT TRANSACTION");
		btnTAbort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String transactionID = txtTAbortTransactionID.getText();
				String abortTransaction = new String();
				
				if((!transactionID.isEmpty()) && (checkIfNumber(transactionID, "Transaction ID"))) {
					
					abortTransaction = "abort" + "," + transactionID;
					
			        StyleConstants.setForeground(style, Color.pink);
			        try { doc.insertString(doc.getLength(), abortTransaction + "\n",style); }
			        catch (BadLocationException badLocationException){}
			        
			        txtTAbortTransactionID.setText("");
			        
	            	try{
	            		rm.abort(Integer.parseInt(transactionID));
	                    txnIds.removeElement(Integer.parseInt(transactionID));
	                    StyleConstants.setForeground(style, Color.pink);
	                    try { doc.insertString(doc.getLength(), "Aborted Transaction ID: " + transactionID + "\n",style); }
	                    catch (BadLocationException badLocationException){}
        		        
                } catch (Exception exception) {
        				StyleConstants.setForeground(style, Color.red);
        				try { doc.insertString(doc.getLength(), "EXCEPTION: " + exception.getMessage() + "\n",style); }
        				catch (BadLocationException badLocationException){} 
                }	
			        
			        
					
				} else {
			        StyleConstants.setForeground(style, Color.red);
			        try { doc.insertString(doc.getLength(), "[WARN]: The Transaction " + transactionID + " is invalid" + "\n",style); }
			        catch (BadLocationException badLocationException){}
				}
				
				panelTransaction.setVisible(false);
				panelMenu.setVisible(true);
			}
		});

		btnTAbort.setForeground(SystemColor.controlHighlight);
		btnTAbort.setBackground(UIManager.getColor("Button.light"));
		btnTAbort.setBounds(409, 390, 261, 64);
		panelTransaction.add(btnTAbort);
		
		JButton button = new JButton("Clear All");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				txtTCommitTransactionId.setText("");
				txtTAbortTransactionID.setText("");
				
			}
		});
		button.setForeground(SystemColor.controlHighlight);
		button.setBackground(SystemColor.window);
		button.setBounds(928, 25, 158, 54);
		panelTransaction.add(button);
	}
}
