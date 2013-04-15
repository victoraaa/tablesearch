package helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;

public class BulkDownloader {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BulkDownloader.downloadAllUrls("C:/Users/Victor/workspace/TableExtender/urls/fastTestURLs", "downTest");
		//BulkDownloader.eliminateBigSearchResults("C:/Users/Victor/workspace/TableExtender/urls/SiteExamples/fastTest/",750000);

	}

	public static void downloadAllUrls (String input, String output) throws IOException{
		List<String> urls = getAllUrls(input);
		downloadAllUrlsMultithread(urls,output);
	}
	
	public static List<String> getAllUrls (String input) throws IOException{
		List<String> urls = new ArrayList<String>();
		FileReader fr = new FileReader(input);
		BufferedReader br = new BufferedReader(fr);
		
		String line = "";
		while ((line=br.readLine())!=null){
			urls.add(line);
		}
		
		return urls;
	}
	
	public static void downloadAllUrlsMultithread (List<String> urls, String output) {
		
		File file = new File("files/SiteExamples/"+output+"/");
		file.mkdirs();
		ExecutorService executor = Executors.newFixedThreadPool(8);
		
		for (String url : urls){
			System.out.println(url);
			String urlForOutput = escapeForWindows(url); 
			System.out.println(urlForOutput);
			executor.execute(new AsynchronousPageDownloader(url,output,urlForOutput));
		}
		executor.shutdown();
		//We wait a total time of one second for each site, or until all downloads are complete, before ending the method.
		try {
			executor.awaitTermination(urls.size(),TimeUnit.SECONDS);
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	//This may still be used, but it is slower than its Multithread version
	public static void downloadAllUrls(List<String> urls, String output) throws IOException{
		PageDownloader pd = new PageDownloader();
		File file = new File("SiteExamples/"+output+"/");
		file.mkdirs();
		for (String url : urls){
			System.out.println(url);
			String urlForOutput = escapeForWindows(url); 
			System.out.println(urlForOutput);
			FileWriter fw = new FileWriter("SiteExamples/"+output+"/"+urlForOutput+"/");
			BufferedWriter bw = new BufferedWriter(fw);
			try {
				String page = pd.getPage(url); 
				bw.write(page);
			}
			catch (IOException e){
				System.err.println(e.getMessage());
			}
			bw.close();
			fw.close();
		}
		
	}

	public static String escapeForWindows(String url) {
		String escapedString=url;
		escapedString=escapedString.replaceAll(":","");
		escapedString=escapedString.replaceAll("\\\\","");
		escapedString=escapedString.replaceAll("\\/","");
		escapedString=escapedString.replaceAll("\\?","");
		escapedString=escapedString.replaceAll("\\*","");
		escapedString=escapedString.replaceAll("\\\"","");
		escapedString=escapedString.replaceAll("\\<","");
		escapedString=escapedString.replaceAll("\\>","");
		escapedString=escapedString.replaceAll("\\|","");
		escapedString=escapedString.replaceAll("\\.","");
		return escapedString;
	}
	
	public static void eliminateBigSearchResults(String folderPath, int maxSizeInBytes) {
		
		File folder = new File(folderPath);
		
		for (File f : folder.listFiles()){
			if (f.length()>maxSizeInBytes){
				f.delete();
			}
		}
		
		
	}
	
}
