package tableExtender;

import helpers.TableHelper;
import helpers.TimeoutInvoker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import model.AttributeType;
import model.SpecifiedRow;
import model.Table;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import exceptions.BadSiteException;
import exceptions.UnexpectedArgumentException;
import extractors.RRTableExtractor;

import parsers.RRInterface;


public class Shell {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Shell shell = new Shell();
		Document doc;
		
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
		
		/*
		//this one is wikipedia
		//File wikipedia = new File("SiteExamples/wikiCities.html");
		//doc = Jsoup.parse(wikipedia, "iso-8859-1");
		//This one also has cities
		//doc = Jsoup.connect("http://www.factmonster.com/ipka/A0763765.html").get();
		//POPULOUS CITY EXAMPLES
		row1.add("Chicago");
		row1.add("Illinois");
		row1.add("2500000-3000000");
		row2.add("Atlanta");
		row2.add("Georgia");
		row2.add("400000-500000");
		row3.add("Baltimore");
		row3.add("Maryland");
		row3.add("600000-650000");
		maxSizes = new int[3];
		maxSizes[0]=3;
		maxSizes[1]=3;
		maxSizes[2]=3;
		mayBeEqual = new boolean[3];
		mayBeEqual[0]=true;
		mayBeEqual[1]=true;
		mayBeEqual[2]=false;
		columnTypes = new AttributeType[3];
		columnTypes[0] = AttributeType.STRING;
		columnTypes[1] = AttributeType.STRING;
		columnTypes[2] = AttributeType.NUMBER;
		columnNames.add("City");
		columnNames.add("State");
		columnNames.add("Population");
		originalSites.add("www1");
		originalSites.add("www2");
		originalSites.add("www3");
		//*/
		/*
		//This one is GAMESPOT
		//File gamespot = new File("SiteExamples/gamespot.html");
		//doc = Jsoup.parse(gamespot, "iso-8859-1");
		row1.add("Diablo III");
		row1.add("May 15, 2012");
		//row1.add("8.5");
		row1.add("8.4-8.6");
		row1.add("PC");
		row2.add("The Sims 3");
		row2.add("Jun 2, 2009");
		//row2.add("9.0");
		row2.add("8.9-9.1");
		row2.add("PC");
		row3.add("Medieval II: Total War");
		row3.add("Nov 13, 2006");
		//row3.add("8.8");
		row3.add("8.8-8.8");
		row3.add("PC");
		maxSizes = new int[4];
		maxSizes[0]=6;
		maxSizes[1]=5;
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
		//columnTypes[2] = AttributeType.STRING;
		columnTypes[2] = AttributeType.NUMBER;
		columnTypes[3] = AttributeType.STRING;
		columnNames.add("Game");
		columnNames.add("Launch Date");
		columnNames.add("Grade");
		columnNames.add("Platform");
		originalSites.add("www1");
		originalSites.add("www2");
		originalSites.add("www3");
		//*/
		/*
		//this one is related to basketball
		doc = Jsoup.connect("http://www.basketball-reference.com/leaders/pts_per_g_career.html").get();
		row1.add("Michael Jordan");
		row1.add("30.12");
		row2.add("Lebron James");
		row2.add("27.64");
		row3.add("Kobe Bryant");
		row3.add("25.40");
		//*/
		/*
		//Ebay selling toy story stuff
		//File file = new File("SiteExamples/ebayTS.html");
		//doc = Jsoup.parse(file, "iso-8859-1");
		row1.add("NWT Toy Story 3 Buzz Lightyear");
		row1.add("$19.99");
		row1.add("Accepted within 14 days");
		row2.add("TOY STORY 2 STINKY PETE THE PROSPECTOR");
		row2.add("$19.99");
		row2.add("Not accepted");
		row3.add("DISNEY Pixar TOY STORY Plush STUFFED Animal PEAS in a POD DOLL Green 8 in PLAY");
		row3.add("$7.99");
		row3.add("Not accepted");
		maxSizes = new int[3];
		maxSizes[0]=20;
		maxSizes[1]=2;
		maxSizes[2]=8;
		mayBeEqual = new boolean[3];
		mayBeEqual[0]=false;
		mayBeEqual[1]=false;
		mayBeEqual[2]=false;
		columnTypes = new AttributeType[3];
		columnTypes[0] = AttributeType.STRING;
		columnTypes[1] = AttributeType.STRING;
		columnTypes[2] = AttributeType.STRING;
		columnNames.add("Product");
		columnNames.add("Price");
		columnNames.add("Returns");
		originalSites.add("www1");
		originalSites.add("www2");
		originalSites.add("www3");
		//*/
		//This one is a simple html POKEMON table
		//
		/*
		//Examples of the table
		row1.add("pikachu");
		row1.add("electric");
		row2.add("bulbasaur");
		row2.add("grass");
		maxSizes = new int[2];
		maxSizes[0]=3;
		maxSizes[1]=3;
		mayBeEqual = new boolean[2];
		mayBeEqual[0]=false;
		mayBeEqual[1]=false;
		columnTypes = new AttributeType[2];
		columnTypes[0] = AttributeType.STRING;
		columnTypes[1] = AttributeType.STRING;
		columnNames.add("Pokemon");
		columnNames.add("Type");
		originalSites.add("www1");
		originalSites.add("www2");
		exampleRows.remove(row3);
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
		exampleRows.remove(row3);
		//*/
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
		exampleRows.remove(row3);
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
		row3.add("real madrid");
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
		//exampleRows.remove(row3);
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
		
		Table exampleTable = new Table(exampleRows, maxSizes, mayBeEqual, 0, columnTypes, columnNames, originalSites);
		
		List<List<List<String>>> extractedTables = shell.extractRowsFromFiles("files/SiteExamples/countryPopulation/potentialSites/", 
						exampleTable,"countryPopulation");
		for(List<List<String>> table : extractedTables){
			TableHelper.printTable(table);
		}
		System.out.println("END OF EXTRACTION");
		
		List<List<List<String>>> correctedTables = ErrorChecker.deleteErrors(extractedTables,maxSizes,mayBeEqual);
		List<List<String>> fullTable = TableHelper.appendTablesAndInfo(correctedTables);
		TableHelper.sortRowsByNonNullInfo(fullTable);
		TableHelper.writeTable(fullTable,"files/output/countryPopulation/fullTable/");
		
	}

	RRTableExtractor gte;
	
	public Shell(){
		gte = new RRTableExtractor();
	}
	
	
	//Method used just to make calls easier for me while in development
	//It may be changed to be the public method that is going to be available for other classes.
	public List<List<String>> tableExtractorShell (Document doc, Table exampleTable) throws UnexpectedArgumentException {
		RRInterface rr = new RRInterface();
		List<List<String>> exampleRows = exampleTable.getRowsList();
		//Table that will be printed as result
		List<List<String>> extractedTable = new ArrayList<List<String>>();
		
		//Given a Document, we get every element that may be a table and has some of the information we're looking for.
		Elements tablesWithContent = gte.getDeepestTablesWithExamples(doc.getAllElements(), 
				exampleTable.getMainColumn(),exampleTable.getKeyAttrType());
		for (Element table : tablesWithContent){
			try {
				//RoadRunner evaluates the rows and parses them. RRInterface returns each row as an Element.
				Elements rows = gte.getRows(table, exampleTable.getMainColumn(),exampleTable.getKeyAttrType());
				//Call of getParsedRows, which uses RoadRunner
				//Due to the fact that RoadRunner may take too long to parse a table, we call it
				//with a timeout.
				Class[] argumentClasses = {Elements.class,Table.class};
				Method m;
				try {
					m = RRInterface.class.getMethod("getParsedRowsGroups", argumentClasses);
				} catch (SecurityException e) {
					e.printStackTrace();
					continue;
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					continue;
				}
				Object[] argv = {rows,exampleTable};
				List<Elements> parsedRows = (ArrayList<Elements>) TimeoutInvoker.callMethodWithTimeout(rr, m, argv, 25);
				
				List<List<String>> parsedInformation = gte.extractDesiredAttributes(parsedRows, 
						exampleTable.getAttributesLists(), exampleTable.getColumnTypes());
				//We try to not add repeated rows. If we're using a key, this may be the place to implement the code that checks if
				//our key will continue being unique.
				for (List<String> newRow : parsedInformation){
					if (!extractedTable.contains(newRow)){
						extractedTable.add(newRow);
					}
				}
			} 
			catch (BadSiteException e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
			} 
			catch (TimeoutException e) {
				//If we can't parse the table in the expected time, we go to the next one.
				System.err.println(e.getMessage());
				continue;
			}

		}
		//for testing, we print the table after we extract it
		TableHelper.printTable(extractedTable);
		return extractedTable;
	}
	
	public List<List<String>> downloadAndExtractRows (List<String> urls, Table exampleTable) throws UnexpectedArgumentException {
		List<List<String>> extractedInformation = new ArrayList<List<String>>();
		for (String url : urls){
			try{
				Document doc = Jsoup.connect(url).get();
				List<List<String>> newInformation = tableExtractorShell(doc, exampleTable);
				extractedInformation.addAll(newInformation);
			}
			catch (IOException e){
				System.err.println(e.getMessage());
			}
		}
		return extractedInformation;
	}
	
	public List<List<String>> downloadAndExtractRows(String urlsFile, Table exampleTable) throws IOException, UnexpectedArgumentException{
		
		List<String> urls = new ArrayList<String>();
		FileReader fr = new FileReader(urlsFile);
		BufferedReader br = new BufferedReader(fr);
		
		String line;
		while ((line=br.readLine())!=null){
			urls.add(line);
		}
		br.close();
		
		List<List<String>> results = downloadAndExtractRows(urls, exampleTable); 
		return results;
	}
	
	//would have to escape the input and output folders if we were going to make this public
	//Receives a folder of files with the HTML of the pages from which we are going to extract information. Also receives the
	//rows that the user uses as example.
	//Returns the extracted information as rows.
	public List<List<List<String>>> extractRowsFromFiles (String inputFolder, Table exampleTable,String outputFolder) throws IOException {
		
		List<List<List<String>>> tableList = new ArrayList<List<List<String>>>();
		
		File folder = new File("files/output/"+outputFolder+"/");
		folder.mkdirs();
		int i=1;
		File directory = new File(inputFolder);
		for (File f : directory.listFiles()){
			if (f.isDirectory()) {
				continue;
			}
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line=br.readLine())!=null){
				sb.append(line);
			}
			br.close();
			fr.close();
			try{
				List<List<String>> newInformation = tableExtractorShell(Jsoup.parseBodyFragment(sb.toString()), exampleTable);
				if (!newInformation.isEmpty()){
					tableList.add(newInformation);
				}
				//REMOVER ABAIXO
				//APENAS PARA TESTE DURANTE DEVELOPMENT
				System.out.println("------------------------------------------------------------------------------");
				TableHelper.writeTable(newInformation, "files/output/"+outputFolder+"/"+i+"/");
				//REMOVER ACIMA
			} catch (UnexpectedArgumentException e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
			}
			i++;
		}

		return tableList;
	}
	
	
}
