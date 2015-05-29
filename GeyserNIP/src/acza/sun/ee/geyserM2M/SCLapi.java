/* --------------------------------------------------------------------------------------------------------
 * DATE:	29 May 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: An API for SCL interaction. 
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.geyserM2M;

import java.nio.charset.StandardCharsets;

import org.eclipse.om2m.comm.http.RestHttpClient;
import org.eclipse.om2m.commons.rest.RequestIndication;
import org.eclipse.om2m.commons.rest.ResponseConfirm;
import org.eclipse.om2m.commons.resource.Application;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.Subscription;
import org.eclipse.om2m.commons.utils.XmlMapper;

public class SCLapi {

	private	RestHttpClient http_client;
	
	private RequestIndication request;
	private ResponseConfirm response;
	
	private String NSCL_ID;
	private String NSCL_BASEURI;
	private String REQENTITY;

	//Default contructor
	public SCLapi(){
		this.http_client = new RestHttpClient();
		this.NSCL_ID = "nscl";
		this.NSCL_BASEURI = "localhost:8080/om2m/" + NSCL_ID;
		this.REQENTITY = "admin:admin";
	}
	
	//Specific constructor
	public SCLapi(String nscl_id, String nscl_IP, String nscl_port, String reqentity){
		this.http_client = new RestHttpClient();
		this.NSCL_ID = nscl_id;
		this.NSCL_BASEURI = nscl_IP + ":" + nscl_port + "/om2m/" + NSCL_ID;
		this.REQENTITY = reqentity;
	}
	
	
	//Register new geyser application at NSCL
	public void registerGeyserApplication(long geyser_ID){
		request = new RequestIndication("CREATE","/applications",REQENTITY,new Application("geyser_" + geyser_ID));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println(response);
		//TODO: Confirm registration
		//TODO: Check conflict
	}
	
	public void  createContainer(long geyser_ID, String containerID){
		//Create DATA container
		//TODO: Set max size
		request = new RequestIndication("CREATE","/applications/geyser_"+ geyser_ID +"/containers/",REQENTITY,new Container(containerID));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println(response);
		//TODO: Confirm creation
		//TODO: Check conflict
	}
	
	public void createContentInstance(long geyser_ID, String containerID, String content){
		request = new RequestIndication("CREATE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID +"/contentInstances",REQENTITY, new ContentInstance(content.getBytes()));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println(response);
		//TODO: Confirm content
		//TODO: Check conflict
	}
	
	public void subscribeToContent(long geyser_ID, String containerID, String subscriptionID, String server_baseURI_apoc){
		request = new RequestIndication("CREATE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID +"/contentInstances/subscriptions",REQENTITY,new Subscription(subscriptionID, "http://"+ server_baseURI_apoc +"/"+ subscriptionID +"_" + geyser_ID));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println(response);
		//TODO: Confirm subscription
		//TODO: Check conflict
	}
	
	public String retrieveLatestContent(long geyser_ID, String containerID){
		//Create DATA container
		request = new RequestIndication("RETRIEVE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID +"/contentInstances/latest",REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println(response);
		//TODO: Confirm content
		//TODO: Check conflict
		
		
		XmlMapper xm = XmlMapper.getInstance();
		ContentInstance ci = (ContentInstance) xm.xmlToObject(response.getRepresentation());
		return new String(ci.getContent().getValue(), StandardCharsets.ISO_8859_1);
	}
	
}

