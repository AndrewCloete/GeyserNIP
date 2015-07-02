/* --------------------------------------------------------------------------------------------------------
 * DATE:	30 May 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Keeps track of last seen times of a list of geyser clients, and de-registers them from the
 * 				appropriate SCL
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.geyserM2M;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GeyserWatchdog implements Runnable {

	private static final Logger logger = LogManager.getLogger(GeyserWatchdog.class);
	private Map<Long, GeyserApplication> active_geysers;
	private SCLapi registered_scl;
	private long TIMEOUT; //Whatchdog time is 10 minutes 1000millis * 60sec * 10min
	
	public GeyserWatchdog(SCLapi registered_scl, Map<Long, GeyserApplication> active_geysers){
		this.active_geysers = active_geysers;
		this.registered_scl = registered_scl;
		TIMEOUT = 600000; //Default timeout 10 minutes
	}
	
	public GeyserWatchdog(SCLapi registered_scl, Map<Long, GeyserApplication> active_geysers,  int timeout){
		this.active_geysers = active_geysers;
		this.registered_scl = registered_scl;
		TIMEOUT = 1000*60*timeout;
	}
	
	@Override
	public void run() {
		
		while(true){
			
			
			long unixTime = System.currentTimeMillis();
			
			System.out.println("GeyserWatchdog: " + unixTime);
		    System.out.println("SCL: " + registered_scl.getNSCL_ID());
			for (Entry<Long, GeyserApplication> entry : active_geysers.entrySet())
			{
				
				long last_client_act = entry.getValue().getLastClientActivityTime();
				long geyser_id = entry.getKey();
				
			    System.out.println(geyser_id + "/" + (unixTime - last_client_act));
			    
			    if((unixTime - last_client_act) > this.TIMEOUT){
			    	registered_scl.deregisterGeyserApplication(geyser_id);
			    	active_geysers.remove(geyser_id);
			    	logger.warn("Removed geyser from map: " + geyser_id);
			    }
			    /*TODO: If geyser.lastActiveTime() - unixTime < TIMEOUT
			     *			Deregister from SCL
			     *			Remove from Map.
			     *			Email admin
			    **/
			}
			
			//Periodically sleep
			try {
				Thread.sleep(this.TIMEOUT);
			} catch (InterruptedException e) {}
			
		}//End of main program loop
		
		
		
	}

}

