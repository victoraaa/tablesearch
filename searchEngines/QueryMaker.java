package searchEngines;

import helpers.BulkDownloader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import exceptions.NoSearchResultsException;

public class QueryMaker {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String searchName = "games";
		
		QueryMaker qm = new QueryMaker();
		List<String> keywords = new ArrayList<String>();
		keywords.add("Diablo 3");
		keywords.add("Blizzard");
		keywords.add("Skyrim");
		keywords.add("Bethesda");
		keywords.add("Street Fighter");
		keywords.add("capcom");

		qm.saveUrlsToFile(keywords,searchName,100);
		
	}

	
	//The strings in keywords should be escaped for java.
	//nUrls should be a multiple of 50
	public void saveUrlsToFile (List<String> keywords, String outputFile, int nUrls) throws NoSearchResultsException {
		
		List<String> urls = new ArrayList<String>();
		int iterations = (int) Math.ceil((double) nUrls/50);
		for (int i=0;i<iterations;i++){
			String query;
			String searchResult = null;
			try {
				query = makeQuery(keywords,50*i);
				searchResult = YahooInterface.search(query);
			} catch (UnsupportedEncodingException e) {
				System.err.println("It was not possible to encode this query.");
				e.printStackTrace();
			}
			 
			if(searchResult==null) {
				throw new NoSearchResultsException("It was not possible to complete the search");
			}
			urls.addAll(parseUrlResults(searchResult));
		}

		try {
			saveUrls(urls,outputFile);
		}
		catch (IOException e){
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}

	//Gets 50 url results for the query made with the keywords, starting from the result at the position 'startingPosition'
	//Returns them as a List<String>
	//The keywords should be escaped for java.
	public List<String> get50SearchResults (List<String> keywords, int startingPosition) throws NoSearchResultsException {
		
		List<String> urls = new ArrayList<String>();
		
		String query;
		String searchResult = null;
		try {
			query = makeQuery(keywords,startingPosition);
			searchResult = YahooInterface.search(query);
		} catch (UnsupportedEncodingException e) {
			System.err.println("It was not possible to encode this query.");
			e.printStackTrace();
		}
		 
		if(searchResult==null) {
			throw new NoSearchResultsException("It was not possible to complete the search");
		}
		urls.addAll(parseUrlResults(searchResult));
		return urls;
	}

	//The strings in keywords should be escaped for java.
	private String makeQuery(List<String> keywords, int startingPosition) throws UnsupportedEncodingException {
		
		keywords=addQuotationMarks(keywords);
		keywords=encodeSpacebar(keywords);
		String beggining = "web?q=";
		String end = "&start="+startingPosition+"&format=xml";
		
		StringBuilder params = new StringBuilder("(");
		Iterator<String> iterator = keywords.iterator();
		while(iterator.hasNext()){
			params.append("+"+iterator.next());
			/*
			params.append("(+"+iterator.next()+")");
			if (iterator.hasNext()){
				params.append("AND");
			}
			*/
		}
			
		params.append(")");
		
		StringBuilder query = new StringBuilder();
		query.append(beggining);
		query.append(URLEncoder.encode(params.toString(),"UTF-8"));
		query.append(end);
		
		return query.toString();
	}
	
	private List<String> addQuotationMarks(List<String> keywords) {
		
		for (int i=0; i<keywords.size();i++){
			String s = keywords.get(i);
			if (s.split(" ").length>1){
				keywords.set(i,"\""+s+"\"");
			}
		}
		
		return keywords;
	}
	
	public List<String> encodeSpacebar(List<String> keywords) {
		for (int i=0;i<keywords.size();i++){
			keywords.set(i,keywords.get(i).replace(" ","%20"));
		}
		return keywords;
	}
	
	private void saveSearchResult(List<String> keywords, String searchResult, String outputFile) throws IOException {
		FileWriter fw = new FileWriter(outputFile);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write("The keywords of this search were:");
		bw.newLine();
		for (String s : keywords){
			bw.write(s);
			bw.newLine();
		}
		
		bw.write("The unparsed search result is shown below:");
		bw.newLine();
		bw.write(searchResult);
		bw.close();
	}

	private List<String> parseUrlResults(String searchResult) {
		Elements urlNodes = Jsoup.parseBodyFragment(searchResult).select("url");
		List<String> urls = new ArrayList<String>();
		for (Element urlNode : urlNodes){
			urls.add(urlNode.text());
		}
		return urls;
	}
	
	
	private String saveUrls(List<String> urls, String outputFile) throws IOException {
		FileWriter fw = new FileWriter(outputFile);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for (String url : urls){
			bw.write(url);
			bw.newLine();
		}
		bw.close();
		return outputFile;
	}
	
	
}
