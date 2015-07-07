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
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.om2m.comm.http.RestHttpClient;
import org.eclipse.om2m.commons.rest.RequestIndication;
import org.eclipse.om2m.commons.rest.ResponseConfirm;
import org.eclipse.om2m.commons.resource.Application;
import org.eclipse.om2m.commons.resource.Applications;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.NamedReferenceCollection;
import org.eclipse.om2m.commons.resource.ReferenceToNamedResource;
import org.eclipse.om2m.commons.resource.StatusCode;
import org.eclipse.om2m.commons.resource.Subscription;
import org.eclipse.om2m.commons.utils.XmlMapper;

public class SCLapi {

	private static final Logger logger = LogManager.getLogger(SCLapi.class);
	
	private	RestHttpClient http_client;
	
	private RequestIndication request;
	private ResponseConfirm response;
	
	private String NSCL_ID;
	private String NSCL_BASEURI;
	private String REQENTITY;

	//Default constructor
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
	
	//Register new general application at NSCL
	public void registerApplication(String app_ID){
		request = new RequestIndication("CREATE","/applications",REQENTITY,new Application(app_ID));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Register application: " + response.getStatusCode());
		//TODO: Confirm registration
		//TODO: Check conflict
	}
	
	//Deregister general application at NSCL
	public void deregisteApplication(String app_ID){
		request = new RequestIndication("DELETE","/applications/"+app_ID,REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Deregister application: " + app_ID + " - " + response.getStatusCode());
		//TODO: Confirm deregistration
	}
	
	
	//Register new geyser application at NSCL
	public void registerGeyserApplication(long geyser_ID){
		request = new RequestIndication("CREATE","/applications",REQENTITY,new Application("geyser_" + geyser_ID));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Register application: " + response.getStatusCode());
		//TODO: Confirm registration
		//TODO: Check conflict
	}
	
	//Deregister geyser application at NSCL
	public void deregisterGeyserApplication(long geyser_ID){
		request = new RequestIndication("DELETE","/applications/geyser_" + geyser_ID,REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Deregister application, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		//TODO: Confirm deregistration
	}
	
	public void  createContainer(String app_ID, String containerID){
		//Create DATA container
		request = new RequestIndication("RETRIEVE","/applications/"+ app_ID +"/containers/"+ containerID ,REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Poll container: " + app_ID + " - " + response.getStatusCode());
		
		if(response.getStatusCode().equals(StatusCode.STATUS_NOT_FOUND)){	//Case: Container does not exist
			Container new_container = new Container(containerID);
			new_container.setMaxNrOfInstances((long)5);
			request = new RequestIndication("CREATE","/applications/"+ app_ID +"/containers/",REQENTITY, new_container);
			request.setBase(this.NSCL_BASEURI);
			response = http_client.sendRequest(request);
			logger.info("Create container: " + app_ID + " - " + response.getStatusCode());
		}
		else{//Case: Container already exists
			logger.info("Container already exists: " + app_ID + " - " + response.getStatusCode());
		}
		//TODO: Confirm creation
	}
	
	
	public void  createContainer(long geyser_ID, String containerID){
		//Create DATA container
		request = new RequestIndication("RETRIEVE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID ,REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Poll container, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		
		if(response.getStatusCode().equals(StatusCode.STATUS_NOT_FOUND)){	//Case: Container does not exist
			Container new_container = new Container(containerID);
			new_container.setMaxNrOfInstances((long)5);
			request = new RequestIndication("CREATE","/applications/geyser_"+ geyser_ID +"/containers/",REQENTITY, new_container);
			request.setBase(this.NSCL_BASEURI);
			response = http_client.sendRequest(request);
			logger.info("Create container, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		}
		else{//Case: Container already exists
			logger.info("Container already exists, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		}
		//TODO: Confirm creation
	}
	
	public void createContentInstance(String app_ID, String containerID, String content){
		request = new RequestIndication("CREATE","/applications/"+ app_ID +"/containers/"+ containerID +"/contentInstances",REQENTITY, new ContentInstance(content.getBytes()));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		
		if(!response.getStatusCode().equals(StatusCode.STATUS_CREATED)){
			String content_log;
			if(content.length() >= 15)
				content_log = content.substring(0,14);
			else
				content_log = content;
			
			logger.warn("Create content instance fail: " + app_ID + "/containers/" + containerID + "- \"" + content_log + "...\""+ " - " + response.getStatusCode());
		}
		//TODO: Confirm content
		//TODO: Check conflict
	}
	
	public void createContentInstance(long geyser_ID, String containerID, String content){
		request = new RequestIndication("CREATE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID +"/contentInstances",REQENTITY, new ContentInstance(content.getBytes()));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		
		if(!response.getStatusCode().equals(StatusCode.STATUS_CREATED)){
			String content_log;
			if(content.length() >= 15)
				content_log = content.substring(0,14);
			else
				content_log = content;
			
			logger.warn("Create content instance fail, geyser_" + geyser_ID + "/containers/" + containerID + " - \"" + content_log + "...\""+ " - " + response.getStatusCode());
		}
		//TODO: Confirm content
		//TODO: Check conflict
	}
	
	public void updateContentInstance(String app_ID, String containerID, String content){
		request = new RequestIndication("UPDATE","/applications/"+ app_ID +"/containers/"+ containerID +"/contentInstances",REQENTITY, new ContentInstance(content.getBytes()));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Update content instance: " + app_ID + " - " + response.getStatusCode());
		//TODO: Confirm content
		//TODO: Check conflict
	}
	
	public void updateContentInstance(long geyser_ID, String containerID, String content){
		request = new RequestIndication("UPDATE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID +"/contentInstances",REQENTITY, new ContentInstance(content.getBytes()));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Update content instance, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		//TODO: Confirm content
		//TODO: Check conflict
	}
	
	public void subscribeToContent(String app_ID, String containerID, String subscriptionID, String server_baseURI_apoc){
		request = new RequestIndication("CREATE","/applications/"+ app_ID +"/containers/"+ containerID +"/contentInstances/subscriptions",REQENTITY,new Subscription(subscriptionID, "http://"+ server_baseURI_apoc +"/"+ subscriptionID));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Subscribe to content: " + app_ID + " - " + response.getStatusCode());
		//TODO: Confirm subscription
		//TODO: Check conflict
	}
	
	
	public void subscribeToContent(long geyser_ID, String containerID, String subscriptionID, String server_baseURI_apoc){
		request = new RequestIndication("CREATE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID +"/contentInstances/subscriptions",REQENTITY,new Subscription(subscriptionID, "http://"+ server_baseURI_apoc +"/"+ subscriptionID +"_" + geyser_ID));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Subscribe to content, Geyser: " + geyser_ID + " - " + response.getStatusCode());
		//TODO: Confirm subscription
		//TODO: Check conflict
	}
	
	public void subscribeToApplications(String subscriptionID, String server_baseURI_apoc){
		request = new RequestIndication("CREATE","/applications/subscriptions",REQENTITY,new Subscription(subscriptionID, "http://"+ server_baseURI_apoc +"/"+ subscriptionID + "/application"));
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Subscribe to applications - " + response.getStatusCode());
		//TODO: Confirm subscription
		//TODO: Check conflict
	}
	
	public List<String> retrieveApplicationList(){
		request = new RequestIndication("RETRIEVE","/applications",REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		logger.info("Retrieve application list: " + response.getStatusCode());
		
		XmlMapper xm = XmlMapper.getInstance();
		Applications apps = (Applications)xm.xmlToObject(response.getRepresentation());
		NamedReferenceCollection nrc = apps.getApplicationCollection();
		List<ReferenceToNamedResource> list = nrc.getNamedReference();
		
		List<String> appArray = new LinkedList<String>();
		
		for (ReferenceToNamedResource rtnr : list){
			appArray.add(rtnr.getId());
		}
		
		return appArray;
	}
	
	public boolean applicationExists(String app_ID){
		request = new RequestIndication("RETRIEVE","/applications/"+ app_ID,REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println("Prompt apllication " + app_ID + " existance: " + response.getStatusCode());
		
		return !response.getStatusCode().equals(StatusCode.STATUS_NOT_FOUND);


	}
	
	public String retrieveLatestContent(long geyser_ID, String containerID){
		request = new RequestIndication("RETRIEVE","/applications/geyser_"+ geyser_ID +"/containers/"+ containerID +"/contentInstances/latest",REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println("Retrieve latest content: " + response.getStatusCode());
		//TODO: Confirm content
		//TODO: Check conflict
		
		
		XmlMapper xm = XmlMapper.getInstance();
		ContentInstance ci = (ContentInstance) xm.xmlToObject(response.getRepresentation());
		return new String(ci.getContent().getValue(), StandardCharsets.ISO_8859_1);
	}
	
	public String retrieveLatestContent(String app_ID, String containerID){
		request = new RequestIndication("RETRIEVE","/applications/"+ app_ID +"/containers/"+ containerID +"/contentInstances/latest",REQENTITY);
		request.setBase(this.NSCL_BASEURI);
		response = http_client.sendRequest(request);
		System.out.println("Retrieve latest content: " + response.getStatusCode());
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

