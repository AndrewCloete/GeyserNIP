# GeyserNIP
UDP network interworking proxy (NIP) for geyser control units
## Program flow
### Main program

Init:
- Set packet size.
- Create map of ID:Application

Program parameters:
- NSCL public IP address
- Server UDP port
- aPoC port for NSCL
- Geyser registration timeout

Start Jetty server:

Main loop init:
- Create UDP listening socket
- Create SCLapi singleton object
- Start geyser watchdog thread given map

Main loop:
- Read inbound packet (blocking)
- Process recieved string:
	    if("at") //geyser successfully connected via SmartPack modem
            reply = {'status':'ACK'}
	    else //assume nothing
		    Parse geyser ID
		    if(Geyser ID is unknown):
			    register application at NSCL
			    register DATA container at NSCL
			    register SETTINGS container at NSCL
			    subscribe aPoC to SETTINGS container
			    reply = {'status':'ACK'}
		    else
			    retrieve geyser object from map using ID
			    pop latest SETTINGS command from geyser object
			    add command to reply JSON. reply = {'status':'ACK', + command}
		post DATA to NSCL 

	    get IP  address and port from UDP packet
	    send reply to client  



### aPoC HTTP servlet 
Only doPost method implemented. NSCL will not make a GET request

doPost:
- Parse geyser ID from URI. ../SETTINGS_geyserID
- Read body of request
- Create OM2M_Notify object using OM2M_XMLmapper 
- Create OM2M_ContentInstance object from Notify object
- Get content from ContentInstance.
- Get geyer object from map using geyser ID and push content to command queue


### Geyser watchdog  
    Loop forever:
	    Determine current time
	    for each geyser in map
		    get last activity time
		    compare with current time
		    if(over TIMEOUT time)
			    deregister geyser at NSCL
			    TODO: Email admain
	    sleep for TIMEOUT time.

