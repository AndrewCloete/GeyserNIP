/* --------------------------------------------------------------------------------------------------------
 * DATE:	16 Apr 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: An HTTP client-side interface, tailored to speak M2M.
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.geyserM2M;

import java.io.*;
import java.net.*;

public class M2MHTTPClient {

	public static void get(String getURI){
		
		try{
			URL url = new URL(getURI);

			HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
			urlcon.setRequestMethod("GET");	
			urlcon.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");
			InputStream in = urlcon.getInputStream();
			InputStreamReader inr = new InputStreamReader(in);
			BufferedReader bin = new BufferedReader(inr);

			/* ALTERNATIVE
			InputStreamReader in = new InputStreamReader(url.openStream()); //(1)
			BufferedReader bin = new BufferedReader(in);
			 */
			
			String line;
			while((line = bin.readLine()) != null){
				System.out.println(line);	//Debug reply
			}

		}catch (MalformedURLException e){
			System.out.println(e);
		}catch (IOException e2){
			System.out.println(e2);
		}
	}	
	
	
	public static void post(String postURI, String postData){
		try{
			URL url = new URL("http://" + postURI);
			HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
			urlcon.setRequestMethod("POST");
			urlcon.setRequestProperty("Content-type", "text/html");
			urlcon.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");
			urlcon.setDoOutput(true); //(2)
			urlcon.setDoInput(true);
			PrintWriter pout = new PrintWriter(new OutputStreamWriter(urlcon.getOutputStream(), "8859_1"), true);
			pout.print(postData);
			pout.flush();

			InputStream in = urlcon.getInputStream();
			InputStreamReader inr = new InputStreamReader(in);
			BufferedReader bin = new BufferedReader(inr);

			String line;
			while((line = bin.readLine()) != null){
				System.out.println(line); //Debug reply
			}

		}catch (MalformedURLException e){
			System.out.println(e);
		}catch (IOException e2){
			System.out.println(e2);
		}
	
	}
	
	
	
	
}



/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * 
(1) 
openStream() is shorthand for openConnection().getInputStream().
openConnection() first returns a URLConnection object. Therefore you could have alternatively written as shown 
in comment block. But then you would'nt be able to specify header content;


(2) 
A URL connection can be used for input and/or output. Setting the doOutput flag to true indicates that the 
application intends to write data to the URL connection. Setting the doOutput flag to true indicates that 
the application intends to write data to the URL connection.
 * ---------------------------------------------------------------------------------------------------------
 */
