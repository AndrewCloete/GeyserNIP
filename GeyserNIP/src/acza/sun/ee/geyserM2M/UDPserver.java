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

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPserver
{
	private final static int PACKETSIZE = 100 ;

	public static void main( String args[] )
	{
		// Check the arguments
		if( args.length != 1 )
		{
			System.out.println( "usage: DatagramServer port" ) ;
			return ;
		}

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
						System.out.println("ID = " + (Integer)getValueFromJSON("id", receive_msg));
						reply = "{\"status\":\"ACK\",\"e\":true}";
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
 * ---------------------------------------------------------------------------------------------------------
 */