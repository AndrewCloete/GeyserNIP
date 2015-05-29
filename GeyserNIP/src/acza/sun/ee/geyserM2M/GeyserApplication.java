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

	private final int ID;
	private int registration_ttl;
	private LinkedList<String> commandQueue;
	
	private static int TTL_RESET = 5;
	
	public GeyserApplication(int id){
		this.ID = id;
		this.registration_ttl = TTL_RESET;
		this.commandQueue = new LinkedList<String>();
	}
	
	public int getID(){
		return this.ID;
	}
	
	public void pushCommand(String jsonCommand){
		this.commandQueue.push(jsonCommand);
	}
	
	public String popCommand(){
		if(this.commandQueue.isEmpty())
			return "";
		else
			return commandQueue.pop();
	}
	
}

