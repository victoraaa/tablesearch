package evaluators;

import helpers.BulkDownloader;
import helpers.ElementSelector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.AttributeType;
import model.Table;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import exceptions.UnexpectedArgumentException;
import extractors.RRTableExtractor;


public class StrictTableFinder {

	/**
	 * @param args
	 * @throws UnexpectedArgumentException 
	 */
	public static void main(String[] args) throws UnexpectedArgumentException {
		
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
		row1.add("Argentina");
		row2.add("Shevchenko");
		row2.add("ukraine");
		maxSizes = new int[2];
		maxSizes[0]=3;
		maxSizes[1]=3;
		mayBeEqual = new boolean[2];
		mayBeEqual[0]=false;
		mayBeEqual[1]=false;
		columnTypes = new AttributeType[2];
		columnTypes[0] = AttributeType.STRING;
		columnTypes[1] = AttributeType.STRING;
		columnNames.add("Player Name");
		columnNames.add("Nationality");
		originalSites.add("www1");
		originalSites.add("www2");
		//*/
		/*
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
		row1.add("Chicago");
		row1.add("2500000-3000000");
		row2.add("Atlanta");
		row2.add("400000-500000");
		row3.add("Baltimore");
		row3.add("600000-650000");
		maxSizes = new int[2];
		maxSizes[0]=3;
		maxSizes[1]=3;
		mayBeEqual = new boolean[2];
		mayBeEqual[0]=false;
		mayBeEqual[1]=false;
		columnTypes = new AttributeType[2];
		columnTypes[0] = AttributeType.STRING;
		columnTypes[1] = AttributeType.NUMBER;
		columnNames.add("City");
		columnNames.add("Population");
		originalSites.add("www1");
		originalSites.add("www2");
		originalSites.add("www3");
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
		StrictTableFinder evaluator = new StrictTableFinder();
		evaluator.evaluatePotentialSites("files/SiteExamples/countryPopulation/",exampleTable,null);
	}
	
	
	
	//Receives the folder with all the sites found at the query and copies the potential good sites to a folder under it called
	//'potentialSites'
	//If filenames!=null, it will only consider the files with their names in the list.
	public void evaluatePotentialSites(String inputPath,Table exampleTable, List<String> filenames){
		File folder = new File(inputPath);
		
		String outputpath;
		if (inputPath.endsWith("/")){
			outputpath=inputPath+"potentialSites/";
		}
		else{
			outputpath=inputPath+"/potentialSites/";
		}
		File outputFolder = new File(outputpath);
		outputFolder.mkdir();
		
		int nFile = 0;
		for (File f : folder.listFiles()){
			if (f.isDirectory() || (filenames!=null && !filenames.contains(f.getName())) ) continue;
			String html = getFileAsString(f);
			nFile++;
			Elements potentialTables = getPotentialTables(html,exampleTable);
			if (!potentialTables.isEmpty()){
				//Copies the potential tables to a folder called "potentialSites"
				try {
					copyToPotentialSites(f.getName(),potentialTables,outputpath);
				}
				catch (IOException e){
					System.err.println("Not possible to write file "+f.getName()+" "+nFile);
					e.printStackTrace();
				}
			
			}
			else {
				System.out.println(nFile +" is not a potential Site");
			}
		}
	}


	//Receives the whole HTML and saves to a file
	//Not being used in this version
	private void copyToPotentialSites(String outputname,String html, String outputFolder) throws IOException {
		FileWriter fw = new FileWriter(outputFolder+outputname);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(html);
		bw.close();
		
	}
	
	//Receives tables and saves them to a file
	private void copyToPotentialSites(String outputname,Elements tables, String outputFolder) throws IOException {
		FileWriter fw = new FileWriter(outputFolder+outputname);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write("<html> <head></head> <body> ");
		bw.newLine();
		for (Element table : tables){
			bw.write("<table> "+table.html()+" </table>");
			bw.newLine();
		}
		bw.write("</body> </html>");
		bw.close();
		
	}

	//Checks if the site has tables with certain conditions that will mark them as 'potentially good'
	public Elements getPotentialTables(String html, Table exampleTable) {
		Elements potentialTables = new Elements();
		//We get all the rows that have keys that appear in the html.
		//Also, we make sure that at least two keys are present in the html.
		List<List<String>> occurringExamples = getPartialyOrCompletelyOccurringExamples(html,exampleTable);
		if (occurringExamples.size()<2) return potentialTables;
		//We check if the occurring examples have keys within the expected size.
		//If they don't, this probably means that the examples are not in a table, but in text, and therefore we discard it.
		List<List<String>> examplesOfExpectedSize = getExamplesWithKeysOfExpectedSize(html,occurringExamples,exampleTable);
		if (examplesOfExpectedSize.size()<2) return potentialTables;
		//If there are no tables with more than 3 majority tags and with variance smaller than 10, we return the empty list.
		potentialTables = filterTablesBySizeAndVariance(html,examplesOfExpectedSize,exampleTable);
		if (potentialTables.isEmpty()) return potentialTables;
		//Now we check whether the majority rows of our potentialTables that have a key of an example has at least one more of
		//its attributes.
		potentialTables = filterNotOnlyKeys(potentialTables,examplesOfExpectedSize,exampleTable);
		if (potentialTables.isEmpty()) return potentialTables;
		
		potentialTables = removeNonMajorityTags(potentialTables, examplesOfExpectedSize, exampleTable);
		
		return potentialTables;
	}
	
	private Elements removeNonMajorityTags(Elements tables, List<List<String>> examples, Table exampleTable) {
		
		Elements cleanTables = new Elements();
		MajorityTagsFinder mtfinder = new MajorityTagsFinder();
		//We use keysFromExamples to get the majority tags of the tables we 
		List<String> keysFromExamples = new ArrayList<String>();
		for (List<String> example : examples){
			keysFromExamples.add(example.get(exampleTable.getMainColumnIndex()));
		}
		//We loop through every table and, if possible, we find and add only the majority tag rows.
		for (Element table : tables){
			//We try to find and add only the majority tags.
			Elements rows = null;
			Element newTable = null;
			try{
				rows = mtfinder.getMajorityTagRows(table.children(),keysFromExamples,exampleTable.getKeyAttrType());
				newTable = new Element(table.tag(), table.baseUri());
				for (Element row : rows){
					newTable.appendChild(row);
				}
			}
			catch(Exception e){
				System.err.println("Cannot find majority tags at this table");
			}
			
			if (newTable!=null){
				cleanTables.add(newTable);
			}
			//if adding only the majority tags is not possible, we add the entire table.
			else {
				cleanTables.add(table);
			}
		}
		return cleanTables;
	}

	//Receives some tables and return only the ones that have at least one example with, 
	//besides the key, at least one more of its attributes.
	//This attribute must not be in the exact same tag as the key.
	private Elements filterNotOnlyKeys(Elements tables,
			List<List<String>> examples, Table exampleTable) {

		//We filter the tables to keep only the ones we want.
		Elements potentialTables = new Elements();
		for (Element table : tables){
			for (Element row : table.children()){
				if (hasKeyAndAttributeAtDifferentTags(row, examples, exampleTable)){
					potentialTables.add(table);
					break;
				}
			}
		}
		return potentialTables;
	}
	
	private boolean hasKeyAndAttributeAtDifferentTags (Element row, List<List<String>> examples, Table exampleTable){
		for (List<String> exampleRow : examples){
			Elements tagsWithKey = ElementSelector.select(row.getAllElements(), 
					exampleRow.get(exampleTable.getMainColumnIndex()), exampleTable.getKeyAttrType(), ":matchesOwn((?ui)", ")");
			if (tagsWithKey.isEmpty()) continue;
			
			Elements tagsWithAttributes = new Elements();
			for (int i=0; i<exampleRow.size();i++){
				if (i==exampleTable.getMainColumnIndex()) continue;
				
				tagsWithAttributes.addAll(ElementSelector.select(row.getAllElements(),exampleRow.get(i), 
						exampleTable.getColumnTypes()[i], ":matchesOwn((?ui)",")"));
			}
			for (Element tagWithAttribute : tagsWithAttributes){
				if (!tagsWithKey.contains(tagWithAttribute)) return true;
			}
		}
		return false;
	}

	//We check for two things: first, if there are more than 3 majority tags. If there's 3 or less, it is not a potential site,
	//as tables usually have more than 3 rows.
	//The other thing is to check the variance of the number of elements of each row. If it's higher than 10 (magic number), 
	//we discard the site. The highest variance on the sites that had good results was 4.37, while the smaller variance in bad 
	//sites was 16, with the majority of them being higher than a hundred.
	private Elements filterTablesBySizeAndVariance(String html,
			List<List<String>> examples,
			Table exampleTable) {
		
		Elements potentialTables = new Elements();
		MajorityTagsFinder mtfinder = new MajorityTagsFinder();
		RRTableExtractor tableExtractor = new RRTableExtractor();
		List<String> keysFromExamples = new ArrayList<String>();
		for (List<String> example : examples){
			keysFromExamples.add(example.get(exampleTable.getMainColumnIndex()));
		}
		Elements tables = tableExtractor.getDeepestTablesWithExamples(Jsoup.parse(html).getAllElements(),keysFromExamples,
				exampleTable.getKeyAttrType());
		for (Element table : tables){
			Elements rows = null;
			try{
				rows = mtfinder.getMajorityTagRows(table.children(),keysFromExamples,exampleTable.getKeyAttrType());
			}
			catch(UnexpectedArgumentException e){
				e.printStackTrace();
				System.err.println(e.getMessage());
			}
			
			if (rows!=null && rows.size()>3){
				double mean=0;
				for (Element row : rows){
					mean+=row.getAllElements().size();
				}
				mean= (double) mean/rows.size();
				double variance =0;
				for (Element row : rows){
					variance += (row.getAllElements().size()-mean)*(row.getAllElements().size()-mean);
				}
				variance = (double) variance/rows.size();
				variance = Math.sqrt(variance);
				
				if (variance<=10){
					potentialTables.add(table);
				}
				else {
					System.err.println("variance too high: "+variance);
				}
			}
		}
		
		return potentialTables;
		
	}

	private List<List<String>> getExamplesWithKeysOfExpectedSize(String html,
			List<List<String>> occurringExamples, Table exampleTable) {
		
		List<List<String>> examplesOfExpectedSize = new ArrayList<List<String>>();
		
		Document doc = Jsoup.parse(html);
		int indexOfKeys = exampleTable.getMainColumnIndex();
		int [] maxSizes = exampleTable.getMaxSizes();
		for (List<String> exampleRow : occurringExamples){
			boolean keyHasExpectedSize = false;
			Elements occurrences = ElementSelector.select(doc.getAllElements(), exampleRow.get(indexOfKeys), 
					exampleTable.getKeyAttrType(), ":matchesOwn((?ui)",")");
			for (Element e : occurrences){
				if (e.text().split(" ").length<=2*maxSizes[indexOfKeys]){
					keyHasExpectedSize=true;
					break;
				}
			}
			if (keyHasExpectedSize){
				examplesOfExpectedSize.add(exampleRow);
			}
		}
		
		return examplesOfExpectedSize;
	}

	private List<List<String>> getPartialyOrCompletelyOccurringExamples(String html,
			Table exampleTable) {
		
		List<List<String>> occurringExamples = new ArrayList<List<String>>();
		int mainColumnIndex = exampleTable.getMainColumnIndex();
		for (List<String> row : exampleTable.getRowsList()){
			if (ElementSelector.contains(html, row.get(mainColumnIndex),exampleTable.getKeyAttrType())){
				occurringExamples.add(row);
			}
		}
		Iterator<List<String>> it = occurringExamples.iterator();
		while (it.hasNext()){
			List<String> row = it.next();
			boolean oneOtherAttributeIsPresent = false;
			for (int i=0; i<row.size(); i++){
				if (i!=mainColumnIndex && ElementSelector.contains(html, row.get(i),exampleTable.getColumnTypes()[i])){
					oneOtherAttributeIsPresent = true;
					break;
				}
			}
			if (!oneOtherAttributeIsPresent){
				it.remove();
			}
		}

		
		return occurringExamples;
	}

	
	private String getFileAsString(File f) {
		FileReader fr;
		BufferedReader br;
		StringBuilder sb = new StringBuilder();
		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			sb = new StringBuilder();
			String line="";
			while ((line=br.readLine())!=null){
				sb.append(line);
			}
			br.close();
			fr.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("It was not possible to find the file "+f.getName());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("It was not possible to read the file "+f.getName());
		}
		return sb.toString();
	}

	
	
}
