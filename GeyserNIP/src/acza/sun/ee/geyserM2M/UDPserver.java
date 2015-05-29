/* --------------------------------------------------------------------------------------------------------
 * DATE:	22 May 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: UDP network interworking proxy for geyser modems
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: - Reads JSON requests from geyser clients
 * 			- Responds with acks and piggy-backed data .
 * 			- Registers applications/buffers with NSCL
 * ---------------------------------------------------------------------------------------------------------
 */


package acza.sun.ee.geyserM2M;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UDPserver{
	
	private static String DEBUG_OPTION = "-n"; //Default is no debug.
	private final static int PACKETSIZE = 1024 ;	
	private static LinkedList<Long> active_ids = new LinkedList<Long>();

	public static void main( String args[] )
	{
		// ---------------------- Sanity checking of command line arguments -------------------------------------------
		if( args.length < 3 )
		{
			System.out.println( "Usage: <NSCL IP address>  <UDPServer port>  <NIP ID>  <Optional MODE: -d Debug, -v Verbose>" ) ;
			return;
		}
		
		final String NSCL_IP_ADD = args[0];//"52.10.236.177";//"localhost";//
		if(!ipAddressValidator(NSCL_IP_ADD)){
			System.out.println( "IPv4 address invalid." ) ;
			return;
		}
		
		final int UDP_PORT;
		try{
			UDP_PORT = Integer.parseInt( args[1] ); // Convert the argument to ensure that is it valid
		}catch ( Exception e ){
			System.out.println( "UDP port invalid." ) ;
			return;
		}
		
		//(3)
		if( args.length == 4 )
		{
			if(args[3].equalsIgnoreCase("-d"))
				DEBUG_OPTION = "-d";
			else if (args[3].equalsIgnoreCase("-v"))
				DEBUG_OPTION = "-v";
			else{
				System.out.println( "Invalid option: " + args[3]) ;
				return;
			}
		}
		//---------------------------------------------------------------------------------------------------------------
			
				
		DatagramSocket socket = null;
		try{
			// Construct the socket
			socket = new DatagramSocket(UDP_PORT) ;
			System.out.println( "Geyser UDP socket created.") ;
		}catch (SocketException e){
			System.err.println("Unable to create UDP socket: " + e ) ;
			return;
			//TODO: Log and email
		}


		SCLapi nscl = new SCLapi();

		for( ;; ){
			
			// Create a packet
			DatagramPacket packet = new DatagramPacket( new byte[PACKETSIZE], PACKETSIZE ) ;

			// Receive a packet (blocking)
			try{
				socket.receive( packet ) ;
			}catch (IOException e){
				System.err.println("Packet recieve error: " + e ) ;
			}

			// Print the packet
			String receive_msg = new String(packet.getData()).trim();
			printDebug("Recieved: " + receive_msg + " from: " +  packet.getAddress() + " " + packet.getPort(), "-d") ;

			
			//Interpret message from client
			String reply = null;
			if(receive_msg.equalsIgnoreCase("at")){
				reply = "{\"status\":\"ACK\"}";
			}
			else{
				try{

					Long geyser_id = (Long)getValueFromJSON("id", receive_msg);
					
					//Case: New geyser ID detected
					if(!active_ids.contains(geyser_id)){
						active_ids.push(geyser_id);
						
					nscl.registerGeyserApplication(geyser_id);	
					nscl.createContainer(geyser_id, "DATA");
					nscl.createContainer(geyser_id, "SETTINGS");
					
						
					reply = "{\"status\":\"ACK\"}";	
					}
					else{
						//TODO: Reset client registration TTL
						//TODO: System.out.println("Client registration timed out. ID = " + geyser_id);
					}

					//Post data point to NSCL
					nscl.createContentInstance(geyser_id, "DATA", receive_msg);

					//get element state from NSCL container
					//String new_command = nscl.retrieveLatestContent(geyser_id, "SETTINGS");
					reply = "{\"status\":\"ACK\"}";	
				}
				catch(ClassCastException e){
					reply = "{\"status\":\"ERR\"}";
				}
			}

			// Return a reply packet to the sender
			try {
				DatagramPacket ack = new DatagramPacket(reply.getBytes(), reply.getBytes().length, packet.getSocketAddress());
				socket.send( ack ) ;
			} catch (SocketException e) {
				System.err.println("Error creating reply packet to client: " + e);
			} catch (IOException e) {
				System.err.println("Error sending reply packet to client: " + e);
			}

		}  
	}


	private static Object getValueFromJSON(String key, String JSON){

		JSONParser parser=new JSONParser();
		try{
			Object obj = parser.parse(JSON);
			JSONArray array = new JSONArray();
			array.add(obj);	
			JSONObject jobj = (JSONObject)array.get(0);

			return jobj.get(key);

		}catch(ParseException pe){
			printDebug("JSON parse exeption at position: " + pe.getPosition() + " : " + pe, "-d");
			return "Error";
		}
	}
	
	private static boolean ipAddressValidator(final String ip_adr){
		
		if(ip_adr.equalsIgnoreCase("localhost"))
			return true;
		
		 Pattern adr_pattern = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$", Pattern.DOTALL);
		 Matcher matcher = adr_pattern.matcher(ip_adr);
		 return matcher.matches();
	}
	
	//(3)
	private static void printDebug(final String msg, final String option){
		if(DEBUG_OPTION.equalsIgnoreCase("-d") && option.equalsIgnoreCase("-d")){
			System.out.println(msg);
		}
		else if (DEBUG_OPTION.equalsIgnoreCase("-v")){
			System.out.println(msg);
		}
	}
}



/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * 
 * 
 * (1) NB!
 * When you export the project as a runnable jar, Eclipse tries to be clever and throws away certain "unnecessary" 
 * files to keep the size down. In particular, it throws away jaxb.index which ought to be in 
 * org.eclipse.om2m.commons.resources (JAXB uses this file to determine which classes can be marshaled to XML).
 * I don't know how to tell eclipse not to do this, but one solution is to simply add the file after the jar has 
 * been created. You can use Archive Manager to do this easily.
 * 
 * It might be worth investigating using the command line instead to get the export right. But a more permanent 
 * solution would be to use Maven!! Since you are staring to use external libraries, it is seriously time to 
 * bite the bullet and get Maven up and running.
 * 
 * 
 * (2)
 * PSUEDO
 * if r_msg = "at" 
 * reply = {"status":"NEW"}
 * else, parse json and read ID
 * push json to NSCL according to ID
 * read next element and valve state from NSCL
 * reply = {"status":"ACK", "e":1, v:0}
 * exception json corrupt 
 * reply {"status":"ERR"}
 * 
 * 
 * (3)
 * Use debug while building/debugging the server.
 * Use System.out when permanent message should be printed. (Will be mailed to system admin)
 * 
 * 
 * 
 * ---------------------------------------------------------------------------------------------------------
 */