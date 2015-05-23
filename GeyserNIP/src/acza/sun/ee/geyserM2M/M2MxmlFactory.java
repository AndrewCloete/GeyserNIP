/* --------------------------------------------------------------------------------------------------------
 * DATE:	16 Apr 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Uses org.eclipse.om2m.commons.util.XmlMapper and org.eclipse.om2m.commons.resources to 
 * 				create required XML strings for OM2M platform
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.geyserM2M;

import org.eclipse.om2m.commons.resource.APoCPath;
import org.eclipse.om2m.commons.resource.APoCPaths;
import org.eclipse.om2m.commons.resource.Application;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.utils.XmlMapper;

public class M2MxmlFactory { //(1) 

	public static String registerApplication(String appID){
		Application app = new Application();
		app.setAppId(appID);
		
		return XmlMapper.getInstance().objectToXml(app);
	}
	
	//URI localhost:8181/om2m/gscl/applications
	public static String registerApplication(String appID, String apoc){
		Application app = new Application();
		APoCPaths apocpaths = new APoCPaths();
		APoCPath apocpath = new APoCPath();
		
		apocpath.setPath(apoc);
		apocpaths.getAPoCPath().add(apocpath);
		app.setAPoCPaths(apocpaths);
		app.setAppId(appID);
		
		return XmlMapper.getInstance().objectToXml(app);
	}
	
		
	public static String addContainer(String containerID, Long size){
		Container container = new Container(containerID);
		container.setMaxNrOfInstances(size);
		/*
		 * For some reason, if you set this too 1, NO content instances show up!!
		 * This is actually gives stranger behaviour:
		 	* In the tree it shows up as size-1.
		 	* But in the info box it still gives a total count.
		 	* (Get to the bottom of this soon)
		 */
		
		
		return XmlMapper.getInstance().objectToXml(container);
	}
}

/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * 
 *(1) NB!
 * When you export the project as a runnable jar, Eclipse tries to be clever and throws away certain "unnecessary" 
 * files to keep the size down. In particular, it throws away jaxb.index which ought to be in 
 * org.eclipse.om2m.commons.resources (JAXB uses this file to determine which classes can be marshaled to XML).
 * I don't know how to tell eclipse not to do this, but one solution is to simple add the file after the jar has 
 * been created. You can use Archive Manager to do this easily.
 * 
 * It might be worth investigating using the command line instead to get the export right. But a more permanent 
 * solution would be to use Maven!! Since you are staring to use external libraries, it is seriously time to 
 * bite the bullet and get Maven up and running.
 * ---------------------------------------------------------------------------------------------------------
 */

