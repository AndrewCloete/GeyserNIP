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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.Notify;
import org.eclipse.om2m.commons.utils.XmlMapper;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class GeyserNIP{
	
	private static final Logger logger = LogManager.getLogger(GeyserNIP.class);
	private final static int PACKETSIZE = 1024 ;	
	private static Map<Long, GeyserApplication> active_geysers = new ConcurrentHashMap<Long, GeyserApplication>();
	

	public static void main( String args[] )
	{
		
		// ---------------------- Sanity checking of command line arguments -------------------------------------------
		if( args.length != 4)
		{
			System.out.println( "Usage: <NSCL IP address>  <UDPServer port>  <aPoc server port>  <registration timout>" ) ;
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
		
		final int APOC_PORT;
		try{
			APOC_PORT = Integer.parseInt( args[2] ); // Convert the argument to ensure that is it valid
		}catch ( Exception e ){
			System.out.println( "aPoc port invalid." ) ;
			return;
		}
		
		final int REGISTRATION_TIMEOUT;
		try{
			REGISTRATION_TIMEOUT = Integer.parseInt( args[3] ); // Convert the argument to ensure that is it valid
			if(REGISTRATION_TIMEOUT < 5 || REGISTRATION_TIMEOUT > 60){
				System.out.println( "Please enter registration timeout between 5 and 60 minutes.") ;
				return;
			}
		}catch ( Exception e ){
			System.out.println("Registration time invalid") ;
			return;
		}
		//---------------------------------------------------------------------------------------------------------------
		
		logger.info("GeyserNIP started with parameters: " + args[0] + " " + args[1] + " " + args[2] + " " + args[3]);
		
		/* ***************************** START APOC SERVER ************************************************/

		Server server = new Server(APOC_PORT);

		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		// IMPORTANT:
		// This is a raw Servlet, not a Servlet that has been configured
		// through a web.xml @WebServlet annotation, or anything similar.
		handler.addServletWithMapping(ApocServlet.class, "/*");

		try {
			server.start();
			logger.info("Apoc server started.");
		} catch (Exception e) {
			logger.fatal("Apoc server failed.", e);
			return;
		}
		/* ********************************************************************************************/
		
		
		
		DatagramSocket socket = null;
		try{
			// Construct the socket
			socket = new DatagramSocket(UDP_PORT) ;
			logger.info( "Geyser UDP socket created.") ;
		}catch (SocketException e){
			logger.fatal("Unable to create UDP socket: " + e);
			return;
		}


		SCLapi nscl = new SCLapi();
		
		
		new Thread(new GeyserWatchdog(nscl, active_geysers, REGISTRATION_TIMEOUT)).start();

		for(;;){
			
			try{//Main loop exception catcher/logger
				
				// Create a packet
				DatagramPacket packet = new DatagramPacket( new byte[PACKETSIZE], PACKETSIZE ) ;

				// Receive a packet (blocking)
				try{
					socket.receive( packet ) ;
				}catch (IOException e){
					logger.error("Packet recieve error: " + e) ;
				}

				// Print the packet
				String receive_msg = new String(packet.getData()).trim();
				System.out.println("Recieved: " + receive_msg + " from: " +  packet.getAddress() + " " + packet.getPort()) ;


				//Interpret message from client
				String reply = null;
				if(receive_msg.equalsIgnoreCase("at")){
					reply = "{\"status\":\"ACK\"}";
				}
				else{
					try{
						

						Long geyser_id = (Long)getValueFromJSON("ID", receive_msg);


						//Case: New geyser ID detected
						if(!active_geysers.containsKey(geyser_id)){

							//Creat new geyser and add to list.
							GeyserApplication new_geyser = new GeyserApplication(geyser_id);
							active_geysers.put(geyser_id, new_geyser);
							logger.info("New geyser " + geyser_id + " added to map");
							
							nscl.registerGeyserApplication(geyser_id);	
							nscl.createContainer(geyser_id, "DATA");
							nscl.createContainer(geyser_id, "SETTINGS");
							nscl.subscribeToContent(geyser_id, "SETTINGS", "settings", "localhost:"+ APOC_PORT);

							reply = "{\"status\":\"ACK\"}";	
						}
						else{

							//PARADOX: You can't to de-registration here.
							GeyserApplication current_geyser =  active_geysers.get(geyser_id);
							String command = current_geyser.popCommand().trim();

							if(command.isEmpty())
								reply = "{\"status\":\"ACK\"}";
							else{
								// TODO: Verify valid JSON
								reply = "{\"status\":\"ACK\", " + command.substring(1);
							}

							System.out.println("Outbound reply: " + reply);
						}

						//Post data point to NSCL
						nscl.createContentInstance(geyser_id, "DATA", receive_msg);
					}
					catch(ParseException e){
						logger.warn("Corrupt JSON data from geyser: " + receive_msg);
						reply = "{\"status\":\"ERR\"}";
					}
					catch(ClassCastException e){//Message was not "at" and is corrupt
						logger.warn("Corrupt JSON data from geyser: " + receive_msg);
						reply = "{\"status\":\"ERR\"}";
					}
				}

				// Return a reply packet to the sender
				try {
					DatagramPacket ack = new DatagramPacket(reply.getBytes(), reply.getBytes().length, packet.getSocketAddress());
					socket.send( ack ) ;
				} catch (SocketException e) {
					logger.error("Error creating reply packet to client: " + e);
				} catch (IOException e) {
					logger.error("Error sending reply packet to client: " + e);
				}

			}
			catch(Exception e){
				logger.fatal("Unknown exception in main program loop." + e);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
			}

		}	// End of main program loop
	}	//End of main program.
	
	
	@SuppressWarnings("serial")
	public static class ApocServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

			String requestURI = request.getRequestURI();
			
			String target_resource = requestURI.substring(requestURI.lastIndexOf("/")+1);
			if(target_resource.startsWith("settings")){
				Long target_geyserclient_id = (long)0000;
				try{
					target_geyserclient_id = new Long(target_resource.substring(target_resource.lastIndexOf("_")+1));
					logger.info("aPoC request received for geyser: " + target_geyserclient_id);
				} catch (Exception e){
					logger.error("Apoc URI failure: " + target_resource); 
					return;
					//This should never happen if the apoc was correctly registered
					//This can imply hacking
				}

				InputStream in = request.getInputStream();
				InputStreamReader inr = new InputStreamReader(in);
				BufferedReader bin = new BufferedReader(inr);

				StringBuilder builder = new StringBuilder();
				String line;
				while((line = bin.readLine()) != null){
					builder.append(line);
				}

				XmlMapper xm = XmlMapper.getInstance();
				Notify notify = (Notify) xm.xmlToObject(builder.toString());
				System.out.println("Inbound notification: " + notify.getStatusCode());
				ContentInstance ci = (ContentInstance) xm.xmlToObject(new String(notify.getRepresentation().getValue(), StandardCharsets.ISO_8859_1));
				System.out.println("Inbound content instance: " + ci.getId());
				String jsonCommand = new String(ci.getContent().getValue(), StandardCharsets.ISO_8859_1);
				System.out.println("Inbound command string for Geyser "+ target_geyserclient_id +": " + jsonCommand);


				// Push new command to correct geyser (identify geyser using URI)
				if(active_geysers.containsKey(target_geyserclient_id)){
					GeyserApplication geyser_to_update =  active_geysers.get(target_geyserclient_id);
					try{
						verifyJSON(jsonCommand); //Throws parse exception if invalid;
						geyser_to_update.pushCommand(jsonCommand);
					}catch(ParseException e){
						logger.warn("Invalid inbound JSON settings: "+ jsonCommand);
					}
					
				}
				else{
					logger.warn("Inbound command for unregistered Geyser "+ target_geyserclient_id +": " + jsonCommand);
				}
			}
			else{
				logger.warn("Unknown target resource apoc recieved: " + target_resource);
			}
		}

	}
	
	private static Object getValueFromJSON(String key, String JSON) throws ParseException{

		JSONParser parser=new JSONParser();

		Object obj = parser.parse(JSON);
		JSONArray array = new JSONArray();
		array.add(obj);	
		JSONObject jobj = (JSONObject)array.get(0);

		return jobj.get(key);
	}
	
	private static void verifyJSON(String JSON) throws ParseException{
		JSONParser parser=new JSONParser();

		Object obj = parser.parse(JSON);
		JSONArray array = new JSONArray();
	}
	
	private static boolean ipAddressValidator(final String ip_adr){
		
		if(ip_adr.equalsIgnoreCase("localhost"))
			return true;
		
		 Pattern adr_pattern = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$", Pattern.DOTALL);
		 Matcher matcher = adr_pattern.matcher(ip_adr);
		 return matcher.matches();
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
 * 
 * 
 * ---------------------------------------------------------------------------------------------------------
 */