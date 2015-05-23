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

import acza.sun.ee.geyserM2M.SCLhttpClient;
import acza.sun.ee.geyserM2M.M2MxmlFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;

public class UDPserver
{
	private final static int PACKETSIZE = 100 ;	
	private static LinkedList<Long> active_ids = new LinkedList<Long>();

	public static void main( String args[] )
	{
		// Check the arguments
		if( args.length != 3 )
		{
			System.out.println( "Usage: <UDPServer port>  <NSCL IP address>  <NIP ID>" ) ;
			return ;
		}

		
		final String NSCL_IP_ADD =   args[1];//"52.10.236.177";//"localhost";//
		final String NIP_ID = args[2]; //(1)
		final String APP_URI = NSCL_IP_ADD + ":8080/om2m/nscl/applications";
		final String CONTAINER_URI = NSCL_IP_ADD + ":8080/om2m/nscl/applications/" + NIP_ID + "/containers";
		
		//Register NIP application with NSCL
		SCLhttpClient.post(APP_URI, M2MxmlFactory.registerApplication(NIP_ID));	//(1)
		
		
		try
		{
			// Convert the argument to ensure that is it valid
			int port = Integer.parseInt( args[0] ) ;

			// Construct the socket
			DatagramSocket socket = new DatagramSocket( port ) ;

			System.out.println( "The server is ready..." ) ;


			for( ;; )
			{
				// Create a packet
				DatagramPacket packet = new DatagramPacket( new byte[PACKETSIZE], PACKETSIZE ) ;

				// Receive a packet (blocking)
				socket.receive( packet ) ;

				// Print the packet
				String receive_msg = new String(packet.getData()).trim();
				System.out.println( packet.getAddress() + " " + packet.getPort() + ": " +  receive_msg) ;

				/*
				 * PSUEDO
				 * if r_msg = "at" 
				 * reply = {"status":"ACK"}
				 * else, parse json and read ID
				 * push json to NSCL according to ID
				 * read next element and valve state from NSCL
				 * reply = {"status":"ACK", "e":1, v:0}
				 * exception json corrupt 
				 * reply {"status":"ERR"}
				 */
				
				String reply = null;
				if(receive_msg == "at"){
					reply = "{\"status\":\"ACK\"}";
				}
				else{
					try{
						
						Long geyser_id = (Long)getValueFromJSON("id", receive_msg);
						String data_container_id = geyser_id + "_data";
						String control_container_id = geyser_id +"_control_settings";
						String data_content_uri = NSCL_IP_ADD + ":8080/om2m/nscl/applications/" + NIP_ID + "/containers/" + data_container_id + "/contentInstances";
						String control_container_uri = NSCL_IP_ADD + ":8080/om2m/nscl/applications/" + NIP_ID + "/containers/" + control_container_id + "/contentInstances";
						
						
						//Case: New geyser ID detected
						if(!active_ids.contains(geyser_id)){
							active_ids.push(geyser_id);
							System.out.println("New geyser join. ID = " + geyser_id);
							SCLhttpClient.post(CONTAINER_URI, M2MxmlFactory.addContainer(data_container_id, (long)5));
							
							//Add initialised control_settings container for external controller to post to.
							SCLhttpClient.post(CONTAINER_URI, M2MxmlFactory.addContainer(control_container_id, (long)2));
							SCLhttpClient.post(control_container_uri, "{\"e\":\"unknown\"}");
						}
						
						//Post data point to NSCL
						SCLhttpClient.post(data_content_uri, receive_msg);
						
						
						//get element state from NSCL container
						//if none available, return "{\"status\":\"ACK\",\"e\":unknown}";
						String new_element_state = (String)getValueFromJSON("e", SCLhttpClient.get(control_container_uri).trim());	
						reply = "{\"status\":\"ACK\",\"e\":"  + new_element_state + "}";
						System.out.println("To geyser: " + reply);
					}
					catch(ClassCastException e){
						reply = "{\"status\":\"ERR\"}";
					}
				}

				// Return a reply packet to the sender
				DatagramPacket ack = new DatagramPacket(reply.getBytes(), reply.getBytes().length, packet.getSocketAddress());
				socket.send( ack ) ;
				
			}  
		}
		catch( Exception e )
		{
			System.out.println( e ) ;
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
			System.out.println("position: " + pe.getPosition());
			System.out.println(pe);

			return "error";
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
 * ---------------------------------------------------------------------------------------------------------
 */