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
import org.eclipse.om2m.commons.resource.StatusCode;
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
		System.out.println("Register application: " + response.getStatusCode());
		//TODO: Confirm registration
		//TODO: Check conflict
	}
	
	//Register new geyser application at NSCL
	public void deregisterGeyserApplication(long geyser_ID){
		request = new RequestIndication("DELETE","/applications/geyser_" + geyser_ID,REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println("Deregister application, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		//TODO: Confirm deregistration
	}
	
	public void  createContainer(long geyser_ID, String containerID){
		//Create DATA container
		request = new RequestIndication("RETRIEVE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID ,REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println("Poll container, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		
		if(response.getStatusCode().equals(StatusCode.STATUS_NOT_FOUND)){	//Case: Container does not exist
			Container new_container = new Container(containerID);
			new_container.setMaxNrOfInstances((long)5);
			request = new RequestIndication("CREATE","/applications/geyser_"+ geyser_ID +"/containers/",REQENTITY, new_container);
			request.setBase(this.NSCL_BASEURI);
			response = http_client.sendRequest(request);
			System.out.println("Create container, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		}
		else{//Case: Container already exists
			System.out.println("Container already exists, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		}
		
		//TODO: Confirm creation
		//TODO: Check conflict
	}
	
	public void createContentInstance(long geyser_ID, String containerID, String content){
		request = new RequestIndication("CREATE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID +"/contentInstances",REQENTITY, new ContentInstance(content.getBytes()));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println("Create content instance, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		//TODO: Confirm content
		//TODO: Check conflict
	}
	
	public void subscribeToContent(long geyser_ID, String containerID, String subscriptionID, String server_baseURI_apoc){
		request = new RequestIndication("CREATE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID +"/contentInstances/subscriptions",REQENTITY,new Subscription(subscriptionID, "http://"+ server_baseURI_apoc +"/"+ subscriptionID +"_" + geyser_ID));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println("Subscribe to content, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		//TODO: Confirm subscription
		//TODO: Check conflict
	}
	
	public String retrieveLatestContent(long geyser_ID, String containerID){
		//Create DATA container
		request = new RequestIndication("RETRIEVE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID +"/contentInstances/latest",REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println(response.getStatusCode());
		//TODO: Confirm content
		//TODO: Check conflict
		
		
		XmlMapper xm = XmlMapper.getInstance();
		ContentInstance ci = (ContentInstance) xm.xmlToObject(response.getRepresentation());
		return new String(ci.getContent().getValue(), StandardCharsets.ISO_8859_1);
	}

	public String getNSCL_ID() {
		return NSCL_ID;
	}
	
}

