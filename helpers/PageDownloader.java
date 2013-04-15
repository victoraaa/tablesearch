package helpers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.jsoup.Jsoup;


public class PageDownloader {

	public static void main(String[] args) throws IOException {
		PageDownloader pd = new PageDownloader();
		System.out.println(Jsoup.parse(pd.getPage("http://us.337.com/pages/205498/Pok%C3%A9mon%20Mystery%20Dungeon")).html());
	}
	
	public String getPage(String address) throws IOException{
		return getPage (new URL(address));
	}
	
	private String getPage(URL url) throws IOException{
		HttpURLConnection connection = setUpConnection(url);
		
		return getPage(connection);
	}
	
	public String getPage(URL url, Map<String,String> additionalHeaderProperties) throws IOException{
		HttpURLConnection connection = setUpConnection(url);
		
		for (String key : additionalHeaderProperties.keySet()){
			connection.addRequestProperty(key, additionalHeaderProperties.get(key));
		}
		
		
		return getPage(connection);
	}
	
	private HttpURLConnection setUpConnection(URL url) throws IOException{
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Accept-Charset", "UTF-8");
		connection.setDefaultUseCaches(false);
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(10000);
		//this sets the user-agent so the website will think we are a regular web browser
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6");
		
		return connection;
	}
	
	private String getPage(HttpURLConnection connection) throws IOException {
		
		connection.connect();
		InputStreamReader isr = new InputStreamReader(connection.getInputStream());
		BufferedReader br = new BufferedReader(isr);
		
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line=br.readLine())!=null){
			sb.append(line);
		}
		connection.disconnect();
		String page = sb.toString();
		
		return page;
	}
	
	

}
