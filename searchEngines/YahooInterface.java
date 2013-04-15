package searchEngines;

import helpers.StHttpRequest;

import java.io.IOException;  
import java.io.UnsupportedEncodingException;  
import java.net.URLDecoder;  
  
import oauth.signpost.OAuthConsumer;  
import oauth.signpost.basic.DefaultOAuthConsumer;  
  
  
	/** 
	 * Sample code to use Yahoo! Search BOSS 
	 *  
	 * Please include the following libraries  
	 * 1. Apache Log4j 
	 * 2. oAuth Signpost 
	 *  
	 * @author xyz 
	 */  
public class YahooInterface {    
	  
	/** 
	 * @param args 
	 */  
	public static void main(String[] args) {   
		  
		String searchString="((+\"diablo%203\")AND(+\"wizard%20builds\"))";
		try {  
			YahooInterface yahoo = new YahooInterface();  
			System.out.println(yahoo.returnHttpData(searchString));  
		} 
		catch (Exception e) {  
		}  
	}  
	
	//This method is the interface of this class to the others.
	//It receives the query (the part of the url that comes after "http://yboss.yahooapis.com/ysearch/").
	//It returns the response of the web service or NULL if some exception happens.
	public static String search(String searchString){
		String result=null;
		try {  
			YahooInterface yahoo = new YahooInterface();  
			result=yahoo.returnHttpData(searchString);  
		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
		}  
		return result;
	}
	
	protected static String yahooServer = "http://yboss.yahooapis.com/ysearch/";  
	  
	// Please provide your consumer key here  
	private static String consumer_key = "dj0yJmk9bVhZUDJCYVpNM0JDJmQ9WVdrOWNXWmFZazlaTm0wbWNHbzlOakF5TmpBNU5UWXkmcz1jb25zdW1lcnNlY3JldCZ4PTcw";  
	  
	// Please provide your consumer secret here  
	private static String consumer_secret = "f92448df0bd5748fd0e6e37267071ecb508a0b6f";  
	  
	/** The HTTP request object used for the connection */  
	private static StHttpRequest httpRequest = new StHttpRequest();  
	  
	/** Encode Format */  
	private static final String ENCODE_FORMAT = "UTF-8";  
	  
	/** Call Type */  
	private static final String callType = "web";  
	  
	private static final int HTTP_STATUS_OK = 200;  
  
	/** 
	 *  
	 * @return 
	 */  
	public String returnHttpData(String searchString) throws UnsupportedEncodingException, Exception{  
		String result=null;
		if(this.isConsumerKeyExists() && this.isConsumerSecretExists()) {  
			/*	OLD WAY  
			// Start with call Type  
			String params = callType;  
			  
			// Add query  
			params = params.concat("?q=");  
			  
			// Encode Query string before concatenating  
			params = params.concat(URLEncoder.encode(searchString, "UTF-8"));  
			//*/  
			
			//NEW WAY
			//*
			String params = searchString;
			//*/
			
			// Create final URL  
			String url = yahooServer + params;
			System.out.println(params);
			  
			// Create oAuth Consumer   
			OAuthConsumer consumer = new DefaultOAuthConsumer(consumer_key, consumer_secret);  
			  
			// Set the HTTP request correctly  
			httpRequest.setOAuthConsumer(consumer);  
			  
			try {  
				System.out.println("sending get request to" + URLDecoder.decode(url, ENCODE_FORMAT));  
				int responseCode = httpRequest.sendGetRequest(url);   
				  
				// Send the request  
				if(responseCode == HTTP_STATUS_OK) {  
					System.out.println("Response ");  
				} 
				else {  
					System.out.println("Error in response due to status code = " + responseCode);  
				}  
				result=httpRequest.getResponseBody();  
					  
			} 
			catch(UnsupportedEncodingException e) {  
				System.out.println("Encoding/Decording error");  
			} 
			catch (IOException e) {  
				System.out.println("Error with HTTP IO: "+e.getMessage());  
			} 
			catch (Exception e) {  
				System.out.println(httpRequest.getResponseBody() + e.getMessage());
				return null;  
			}  
				  
		} 
		else {  
			System.out.println("Key/Secret does not exist");  
			return null;
		}  
		return result;
	}  
  
	  
	private boolean isConsumerKeyExists() {  
		if(consumer_key.isEmpty()) {  
			System.out.println("Consumer Key is missing. Please provide the key");  
			return false;  
		}  
		return true;  
	}  
	  
	private boolean isConsumerSecretExists() {  
		if(consumer_secret.isEmpty()) {  
			System.out.println("Consumer Secret is missing. Please provide the key");  
			return false;  
		}  
		return true;  
	}  
	
	  
}  