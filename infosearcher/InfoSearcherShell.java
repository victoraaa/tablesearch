package infosearcher;

import helpers.BulkDownloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.AttributeType;
import model.Table;

import evaluators.StrictTableFinder;
import exceptions.NoSearchResultsException;
import exceptions.UnexpectedArgumentException;

import searchEngines.QueryMaker;

public class InfoSearcherShell {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NoSearchResultsException 
	 * @throws UnexpectedArgumentException 
	 */
	public static void main(String[] args) throws NoSearchResultsException, IOException, UnexpectedArgumentException {
		List<List<String>> exampleRows = new ArrayList<List<String>>();
		List<String> row1 = new ArrayList<String>();
		List<String> row2 = new ArrayList<String>();
		List<String> row3 = new ArrayList<String>();
		exampleRows.add(row1);
		exampleRows.add(row2);
		exampleRows.add(row3);
		int[] maxSizes;
		boolean[] mayBeEqual;
		AttributeType[] columnTypes;
		List<String> columnNames = new ArrayList<String>();
		List<String> originalSites = new ArrayList<String>();
		int mainColumn = 0;
		/*
		//Examples of the table
		row1.add("Civilization V");
		row1.add("2K games");
		row1.add("Strategy");
		row1.add("03/19/12");
		row2.add("Mass Effect 3");
		row2.add("Electronic Arts");
		row2.add("action");
		row2.add("10/27/11");
		maxSizes = new int[4];
		maxSizes[0]=4;
		maxSizes[1]=4;
		maxSizes[2]=3;
		maxSizes[3]=3;
		mayBeEqual = new boolean[4];
		mayBeEqual[0]=false;
		mayBeEqual[1]=false;
		mayBeEqual[2]=false;
		mayBeEqual[3]=false;
		columnTypes = new AttributeType[4];
		columnTypes[0] = AttributeType.STRING;
		columnTypes[1] = AttributeType.STRING;
		columnTypes[2] = AttributeType.STRING;
		columnTypes[3] = AttributeType.STRING;
		columnNames.add("Name of Game");
		columnNames.add("Publisher");
		columnNames.add("Genre");
		columnNames.add("Launch Date");
		originalSites.add("www1");
		originalSites.add("www2");
		//*/
		/*
		row1.add("Messi");
		row1.add("argentina");
		row1.add("Barcelona");
		row2.add("Rooney");
		row2.add("england");
		row2.add("manchester");
		row3.add("Ronaldo");
		row3.add("portugal");
		row3.add("Real Madrid");
		maxSizes = new int[3];
		maxSizes[0]=3;
		maxSizes[1]=3;
		maxSizes[2]=3;
		mayBeEqual = new boolean[3];
		mayBeEqual[0]=false;
		mayBeEqual[1]=false;
		mayBeEqual[2]=false;
		columnTypes = new AttributeType[3];
		columnTypes[0] = AttributeType.STRING;
		columnTypes[1] = AttributeType.STRING;
		columnTypes[2] = AttributeType.STRING;
		columnNames.add("Player");
		columnNames.add("Nation");
		columnNames.add("Club");
		originalSites.add("www1");
		originalSites.add("www2");
		originalSites.add("www3");
		//*/
		/*
		row1.add("Corolla");
		row1.add("boston");
		row2.add("camaro");
		row2.add("Providence");
		maxSizes = new int[2];
		maxSizes[0]=12;
		maxSizes[1]=5;
		mayBeEqual = new boolean[2];
		mayBeEqual[0]=false;
		mayBeEqual[1]=false;
		columnTypes = new AttributeType[2];
		columnTypes[0] = AttributeType.STRING;
		columnTypes[1] = AttributeType.STRING;
		columnNames.add("Car");
		columnNames.add("City");
		originalSites.add("www1");
		originalSites.add("www2");
		//*/
		//*
		row1.add("United States");
		row1.add("290000000-330000000");
		row2.add("Canada");
		row2.add("25000000-50000000");
		row3.add("england");
		row3.add("45000000-55000000");
		maxSizes = new int[2];
		maxSizes[0]=4;
		maxSizes[1]=4;
		mayBeEqual = new boolean[2];
		mayBeEqual[0]=false;
		mayBeEqual[1]=false;
		columnTypes = new AttributeType[2];
		columnTypes[0] = AttributeType.STRING;
		columnTypes[1] = AttributeType.NUMBER;
		columnNames.add("Country");
		columnNames.add("Population");
		originalSites.add("www1");
		originalSites.add("www2");
		originalSites.add("www3");
		//*/
		Table exampleTable = new Table(exampleRows, maxSizes, mayBeEqual, mainColumn, columnTypes, columnNames, originalSites);
		String searchName = "countryPopulation";
		InfoSearcherShell shell = new InfoSearcherShell();
		//shell.search(exampleTable,searchName,400);
		shell.dynamicSearch(exampleTable,searchName);
	}
	
	public void dynamicSearch(Table exampleTable, String searchName) {
		
		QueryMaker qm = new QueryMaker();
		//File siteExamplesFolder = new File("files/SiteExamples/"+searchName+"/");
		File potentialSitesFolder = new File("files/SiteExamples/"+searchName+"/potentialSites/");
		potentialSitesFolder.mkdirs();
		StrictTableFinder evaluator = new StrictTableFinder();
		List<List<String>> keywordGroups = tableIntoKeywordsGroups(exampleTable);
		boolean[] stillHasResults = new boolean[keywordGroups.size()];
		for (int i=0;i<stillHasResults.length;i++){
			stillHasResults[i] = true;
		}
		boolean keepSearching = true;
		int nPagesRequested = 0;
		int iterationNumber = 0;
		
		while (keepSearching){
			List<String> urls = new ArrayList<String>();
			for (int i=0;i<keywordGroups.size();i++){
				if (stillHasResults[i]){
					try {
						List<String> newUrls = qm.get50SearchResults(keywordGroups.get(i), iterationNumber*50);
						if (newUrls.size()<25) stillHasResults[i]=false;
						urls.addAll(newUrls);
					}
					catch (NoSearchResultsException e){
						stillHasResults[i] = false;
					}
				}
			}
			//Download the HTML of all the URLs and delete those with size above 750000 bytes
			BulkDownloader.downloadAllUrlsMultithread(urls, searchName);
			BulkDownloader.eliminateBigSearchResults("files/SiteExamples/"+searchName+"/",750000);
			//Evaluates the downloaded sites and copies those who possibly have interesting tables at 
			//"files/SiteExamples/"+searchName+"/potentialSites/"
			evaluator.evaluatePotentialSites("files/SiteExamples/"+searchName+"/", exampleTable,escapeAllForWindows(urls));
			//Now we keep things straight for the next iteration and 
			//check whether we will continue downloading pages
			iterationNumber++;
			nPagesRequested+=urls.size();
			//Numeric limits conditions to stop searching.
			if (nPagesRequested>=1200) keepSearching = false;
			if (potentialSitesFolder.list().length>20) keepSearching = false;
			//if we can't get anymore results from any of the queries, then we stop searching
			boolean someGroupStillHasResults = false;
			for (int j=0;j<stillHasResults.length;j++){
				if (stillHasResults[j]) someGroupStillHasResults=true;
			}
			if (!someGroupStillHasResults) keepSearching = false;
		}
		System.out.println("Search Concluded.");
		
	}
	
	private List<String> escapeAllForWindows(List<String> strings){
		for (int i=0; i<strings.size();i++){
			strings.set(i,BulkDownloader.escapeForWindows(strings.get(i)));
		}
		return strings;
	}
	
	public void search(Table exampleTable, String searchName,int nResults) throws NoSearchResultsException, IOException{
		
		QueryMaker qm = new QueryMaker();
		List<String> keywords = examplesIntoKeywords(exampleTable.getRowsList());
		
		//Makes the search and saves the URLs at "C:/Users/Victor/workspace/InfoSearcher/+"+searchName+"URLs"
		qm.saveUrlsToFile(keywords,"files/urls/"+searchName+"URLs",nResults);
		//Download the HTML of all the URLs and delete those with size above 750000 bytes
		BulkDownloader.downloadAllUrls("files/urls/"+searchName+"URLs", searchName);
		BulkDownloader.eliminateBigSearchResults("files/SiteExamples/"+searchName+"/",750000);
		//Evaluates the downloaded sites and copies those who possibly have interesting tables at 
		//"C:/Users/Victor/workspace/InfoSearcher/SiteExamples/"+searchName+"/potentialSites/"
		StrictTableFinder evaluator = new StrictTableFinder();
		evaluator.evaluatePotentialSites("files/SiteExamples/"+searchName+"/", exampleTable,null);
		
		
	}

	private List<String> examplesIntoKeywords(List<List<String>> examples) {

		List<String> keywords = new ArrayList<String>();
		for (List<String> example : examples){
			for (String s : example){
				keywords.add(s);
			}
		}
		return keywords;
	}
	
	//Receives a table
	//Returns a List of groups of keywords, with one group for each pair of rows in the table.
	private List<List<String>> tableIntoKeywordsGroups(Table table) {
		
		List<List<String>> keywordGroups = new ArrayList<List<String>>();
		List<List<String>> exampleRows = table.getRowsList();
		for (int i=0;i<exampleRows.size();i++){
			for (int j=i+1;j<exampleRows.size();j++){
				List<String> keywords = new ArrayList<String>();
				for (int k=0;k<table.getAttributesLists().size();k++){
					switch (table.getColumnTypes()[k]){
						case STRING:
							keywords.add(exampleRows.get(i).get(k));
							keywords.add(exampleRows.get(j).get(k));
							break;
						case NUMBER:
							keywords.add(table.getColumnNames().get(k));
							break;
					}
				}
				keywordGroups.add(keywords);
			}
		}
		return keywordGroups;
	}

}
