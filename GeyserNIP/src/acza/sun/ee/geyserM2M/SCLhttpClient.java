/* --------------------------------------------------------------------------------------------------------
 * DATE:	18 Apr 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: 
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.geyserM2M;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

public class SCLhttpClient {
	

public static String get(String getURI){
		
		String decoded_reply = null;
	
		try{
			URL url = new URL("http://" + getURI);

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
			
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = bin.readLine()) != null){
				builder.append(line);
			}
			
			String content64 =  parseSCLreply(builder.toString());
			decoded_reply = new String(Base64.decodeBase64(content64.getBytes()));
			

		}catch (MalformedURLException e){
			System.out.println(e);
		}catch (IOException e2){
			System.out.println(e2);
		}

		return decoded_reply;
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
	
	
	private static String parseSCLreply(String sclXML)
	{
		//(1)
		Pattern r1 = Pattern.compile("(?<=<om2m:content xmime:contentType=\"application\\/xml\">).*(?=<\\/om2m:content>)", Pattern.DOTALL);
		Matcher m1 = r1.matcher(sclXML);
		
		if (m1.find( )) {
			return m1.group(0);
			
		} else {
			return "SCL: NO MATCH 1";
		}
	}
	
	
	public static String parseInstanceContent(String obix, String name){
		/*
		 * Regex geyserID, Temp and element
		 */
		
		//(1)
		Pattern r1 = Pattern.compile("(?<=<str val=\").{2,15}(?=\" name=\"" + name +"\")", Pattern.DOTALL);
		Matcher m1 = r1.matcher(obix);
		
		if (m1.find( )) {
			return m1.group(0);
			
		} else {
			return "oBIX: NO MATCH 1";
		}
	}
	
	
}

/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * 
 * (1)
 * Pattern r = Pattern.compile("(?<=whateverinfront).*(?=whateverattheback)")
 * ---------------------------------------------------------------------------------------------------------
 */