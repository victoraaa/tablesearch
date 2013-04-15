package parsers;

import helpers.ElementSelector;
import helpers.RowExporter;
import helpers.TimeoutInvoker;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import model.AttributeType;
import model.SpecifiedRow;
import model.Table;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import exceptions.UnexpectedArgumentException;


public class RRInterface {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	final String MODIFIER = "!@#AXZKY";

	//Receives the example table and a list of rows to parse using roadrunner.
	//Returns a List<Elements> object. Each member of the list is a group of rows that were 
	//parsed in the same way by roadrunner. At least one of the elements of each member is one 
	//example of the exampleTable.
	public List<Elements> getParsedRowsGroups(Elements rows,Table exampleTable) {
		
		List<List<String>> examples = exampleTable.getRowsList(); 
		int keyIndex = exampleTable.getMainColumnIndex(); 
		AttributeType keyAttrType = exampleTable.getKeyAttrType();
		AttributeType[] columnTypes = exampleTable.getColumnTypes();
		
		List<SpecifiedRow> exampleRows = getExampleRows(rows, examples, keyIndex, keyAttrType);
		//exampleRows = SpecifiedRow.getGoodExampleRows(exampleRows,examples);
		//This exists to solve the problem of rows that have the same attribute value; if this was not done, 
		//RoadRunner would not find these attributes when the example had the same value of the new row.
		exampleRows = modifyExampleRows(exampleRows,columnTypes,keyAttrType);

		//Method that makes the parsing
		List<Elements> parsedGroups;
		try {
			parsedGroups = getParsedRowsGroups_exampleAlg(rows, exampleRows,keyAttrType,columnTypes);
		}
		catch (InterruptedException e) {
			parsedGroups = new ArrayList<Elements>();
			System.err.println(e.getMessage());
		}
	
		//In the end, we remove the strings we had put to make attributes unique
		removeModifierString(parsedGroups);

		return parsedGroups;
	}

	//returns the parsed rows as a collection of Elements, selected by the tag "instance"
	private Elements parseRows (Elements rows) throws IOException{
		
		List<String> rowUrls = RowExporter.saveRowsToUrls(rows);
		
		StringBuilder sb = new StringBuilder(); 
		sb.append("-NrowExtractor ");
		for (String row : rowUrls){
			sb.append(row+" ");
		}
		System.out.println(sb.toString());
		File results=null;
		
	
		//OLD WAY, WITHOUT EXECUTOR
		//roadrunner.Shell.main(sb.toString().split(" "));
		//NEW WAY, WITH EXECUTOR
		try{
			rrWithTimeout(sb.toString().split(" "), 5);
		}
		catch (TimeoutException e){
			//If RoadRunner times out, we return an empty Elements object.
			System.err.println(e.getMessage());
			return (new Elements());
		}
	
		results = new File("files/output/rowExtractor/rowExtractor0_DataSet.xml");
		
		
		Elements parsedRows;
		if (results.isFile()){
			Document doc = Jsoup.parse(results, "iso-8859-1");
			parsedRows = doc.select("instance");
		}
		else {
			parsedRows = new Elements();
		}
		
		return parsedRows;
	}
	
	//The method ends after the timeout, but as roadrunner does not accept interruptions,
	//the program won't end until roadrunner actually stops.
	private void rrWithTimeout(String[] argv,int timeout) throws TimeoutException{
		ExecutorService executor = Executors.newCachedThreadPool();
		Runnable task = new RoadRunnerCaller(argv);
		Future<?> future = executor.submit(task);
		
		try {
		   future.get(4, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
		   // handle other exceptions
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		} catch (TimeoutException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			future.cancel(true);
			executor.shutdownNow();
			throw new TimeoutException ("It was not possible to complete the parsing in 4 seconds.");
		} catch (RuntimeException e){
			e.printStackTrace();
			System.err.println(e.getMessage());
			future.cancel(true);
			executor.shutdownNow();
			throw new TimeoutException ("It was not possible to complete the parsing in 4 seconds.");
		}
		finally {
		   future.cancel(true); // may or may not desire this
		}
		executor.shutdownNow();
	}
	

	//Receives rows that will be parsed in a way that the attributes that we want to find will be under attribute-nodes
	//Also receives a list of examples of rows. At least one of the rows must be equivalent to one of the examples
	private List<Elements> getParsedRowsGroups_exampleAlg (Elements rows,List<SpecifiedRow> exampleRows,
			AttributeType keyAttrType, AttributeType[] columnTypes) throws InterruptedException {
		
		List<Elements> parsedGroups = new ArrayList<Elements>();
		
		//We make a copy of rows because we're going to modify it
		Set<Element> elements = new HashSet<Element>(rows);
		Elements copyOfRows = new Elements(elements);
																						
		//For each exRow, tries to parse every row. 
		for (SpecifiedRow exRow : exampleRows){
			List<Elements> newGroups = parseUsingExample(exRow,copyOfRows,keyAttrType,columnTypes);
			if (!newGroups.isEmpty()){
				parsedGroups.addAll(newGroups);
			}
		}	
		
		return parsedGroups;
	}


	//Receives a lot of rows and the user input keyExamples, and returns only the ones that have the keys present in the examples
	private List<SpecifiedRow> getExampleRows(Elements rows, List<List<String>> examples, 
			int keyIndex, AttributeType keyAttrType) {
		List<SpecifiedRow> exampleRows = new ArrayList<SpecifiedRow>();
		
		for (List<String> example : examples){
			Set<Element> elements = new HashSet<Element>(rows);
			Elements candidateRows = new Elements(elements);
			candidateRows.retainAll(ElementSelector.select(rows, example.get(keyIndex), keyAttrType, ":matches((?ui)",")"));
			
			if (!candidateRows.isEmpty()){
				for (Element row : candidateRows){
					exampleRows.add(new SpecifiedRow(row, example, example.get(keyIndex)));
				}
			}
		}
		
		return exampleRows;
	}
	
	//We just make the changes if the type is STRING
	private List<SpecifiedRow> modifyExampleRows(List<SpecifiedRow> exampleRows, 
			AttributeType[] columnTypes, AttributeType keyAttrType){
		
		for (int i=0; i<exampleRows.size();i++){
			SpecifiedRow sr = exampleRows.get(i);
			for (int j=0; j<sr.attributes.size();j++){
				sr.row.html(ElementSelector.addModifierToAll(sr.row.html(),sr.attributes.get(j),
						this.MODIFIER, columnTypes[j]));

			}			
		}
		return exampleRows;
	}
	
	//After making the parsing, we remove the MODIFIER string from our rows and examples
	private void removeModifierString(List<Elements> parsedGroups) {
		
		for (Elements groups : parsedGroups){
			for (Element row : groups){
				row.html(row.html().replaceAll("(?ui)"+this.MODIFIER, ""));
			}
		}
		
	}

	
	//THIS MODIFIES THE 'rows' ARGUMENT. - NOT ANYMORE
	//THIS SHOULD NOT BE USED BY ANY OTHER THAN THE EXAMPLE_ALG - NOT ANYMORE
	//Tries to parse every row from 'rows' comparing to a SpecifiedRow
	private List<Elements> parseUsingExample(SpecifiedRow exRow, Elements rows,
			AttributeType keyAttrType, AttributeType[] columnTypes) throws InterruptedException  {
		List<Elements> parsedGroups = new ArrayList<Elements>();
		
		Iterator<Element> iterator = rows.iterator();
		while(iterator.hasNext()){
			if (Thread.currentThread().isInterrupted()){
				throw new InterruptedException ("The thread was interrupted and parsing of this table will not continue.");
			}
			Element row = iterator.next();
			Elements groupOfRows = new Elements();
			groupOfRows.add(row);

			Elements parsedPair;

			try {
				parsedPair = tryParsingGroupUsingExample(exRow,groupOfRows,keyAttrType,columnTypes);
				if (!parsedPair.isEmpty()){
					parsedGroups.add(parsedPair);
				}	
			} catch (UnexpectedArgumentException e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
			} 
		}
		
		return parsedGroups;
	}

	
	
	//Gets a group of rows and tries to parse it, using an example row to make it possible to check if all attributes 
	//may be found under attribute nodes.
	//Returns, in the end, the parsed rows, or an empty Elements if we can't parse.
	private Elements tryParsingGroupUsingExample(SpecifiedRow exRow, Elements group,
			AttributeType keyAttrType, AttributeType[] columnTypes) 
			throws UnexpectedArgumentException{
		
		if (exRow==null || group.isEmpty()) throw new UnexpectedArgumentException("The parameters should not be empty or null.");
		
		group.add(0,exRow.row);
		
		//Now we try to parse them
		Elements parsedRows = new Elements();
		boolean isParsed;
		try {
			parsedRows = parseRows(group);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		isParsed = hasKeyAndAnotherAttributeUnderAttributeNodes(parsedRows, exRow.attributes, exRow.key, 
				keyAttrType, columnTypes);
		
		if (isParsed) {
			return parsedRows;
		}
		else {
			return new Elements();
		}
		
	}
	
	//Verify if we can find all example attributes under attribute nodes
	private boolean hasKeyAndAnotherAttributeUnderAttributeNodes(Elements rows,
			List<String> attributes,String key, AttributeType keyAttrType, AttributeType[] columnTypes){
		
		Elements elts = ElementSelector.select(rows, key, keyAttrType, ":has(attribute:matches((?ui)",")");
		if (elts.isEmpty()) return false;
		for (int i=0;i<attributes.size();i++){
			Elements elementsWithCurrentAttribute = ElementSelector.select(elts, attributes.get(i), 
					columnTypes[i],":has(attribute:matches((?ui)", ")");
			if (!attributes.get(i).equals(key) && !elementsWithCurrentAttribute.isEmpty()){
				return true;
			}
		}
		return false;	
	}
	
	//Right now, not used.
	//THE SELECT METHODS HAVE TO BE CHANGES TO THE NEW ONE.
	//receives a pair of rows and the attributes of the first one. 
	//Returns the equivalent attributes for the second one
	private List<String> getAllAttributes(Elements rows,List<String> attributes,AttributeType[] columnTypes){
		
		Element specifiedRow = rows.first();
		
		List<String> attrs = new ArrayList<String>();
		List<String> labels = new ArrayList<String>();
		
		for (int i=0;i<attributes.size();i++){
			Elements elts = ElementSelector.select(specifiedRow.getAllElements(), attributes.get(i), columnTypes[i],
					"attribute:matches((?ui)", ")");
			labels.add(elts.first().attr("label"));	
		}
		for (String label : labels){
			attrs.add(rows.get(1).select("attribute[label="+label+"]").first().text());
		}
		
		return attrs;
	}
	
	

}
