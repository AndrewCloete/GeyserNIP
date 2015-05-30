/* --------------------------------------------------------------------------------------------------------
 * DATE:	29 May 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Class represents a proxy of the NSCL application to the geyser client
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.geyserM2M;

import java.util.LinkedList;

public class GeyserApplication {

	private final long ID;
	private long last_clientactivity_time;
	private LinkedList<String> commandQueue;
	
	public GeyserApplication(long id){
		this.ID = id;
		this.last_clientactivity_time = System.currentTimeMillis();
		this.commandQueue = new LinkedList<String>();
	}
	
	public long getID(){
		return this.ID;
	}
	
	public void pushCommand(String jsonCommand){
		this.commandQueue.push(jsonCommand);
	}
	
	public String popCommand(){
		this.last_clientactivity_time = System.currentTimeMillis();
		if(this.commandQueue.isEmpty())
			return "";
		else
			return commandQueue.pop();
	}
	
	public long getLastClientActivityTime(){
		return last_clientactivity_time;
	}
	
}

