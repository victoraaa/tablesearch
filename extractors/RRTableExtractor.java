package extractors;

import helpers.ElementSelector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import model.AttributeType;
import model.Table;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import evaluators.MajorityTagsFinder;
import exceptions.BadParseException;
import exceptions.BadSiteException;
import exceptions.UnexpectedArgumentException;


public class RRTableExtractor {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws IOException {
		File wikipedia = new File("files/SiteExamples/wikiCities.html");
		Document doc = Jsoup.parse(wikipedia, "iso-8859-1");
		RRTableExtractor te = new RRTableExtractor();
		//for (Element table : te.getDeepestTablesWithExamples(doc.getAllElements(), exampleTable));
	}
	
	//Receives WELL-FORMATTED groups of rows and lists of attributes, separated by type, in the order 
	//that they appear in the user input example.
	//Calls the method that extracts the desired attributes for each group.
	//Each group must be parsed in the same way by RoadRunner. Also, each group must have at least one row that 
	//is equivalent to one of the exampleRows.
	//Returns: Table (List<List<String>>) with the information we got from the rows.
	public List<List<String>> extractDesiredAttributes (List<Elements> rowGroups, List<List<String>> attributes,
			AttributeType[] columnTypes) 
			throws UnexpectedArgumentException {
		List<List<String>> extractedTable = new ArrayList<List<String>>();
		
		for (Elements rows : rowGroups){
			try {
				extractedTable.addAll(extractDesiredAttributes(rows, attributes,columnTypes));
			}
			catch(BadParseException e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
			}
		}
		
		return extractedTable;
	}
	
	
	//Receives WELL-FORMATTED rows that have been parsed in the same way and list of the example attributes.  
	//Well-formatted row is the one that has been parsed by RoadRunner
	//Only the attributes with examples that appear in the rows will be extracted. The others will be left as null. 
	//Returns: Table (List<List<String>>) with the information we got from the pair of rows.
	private List<List<String>> extractDesiredAttributes (Elements rows, List<List<String>> attributes, 
			AttributeType[] columnTypes) 
			throws UnexpectedArgumentException, BadParseException {
		List<List<String>> extractedTable = new ArrayList<List<String>>();
		
		List<String> attrLabels = getRelevantLabels(rows, attributes,columnTypes);
		for (Element row : rows){
			List<String> newRow = new ArrayList<String>();
			for (int i=0;i<attrLabels.size();i++){
				Elements query = row.select("attribute[label="+attrLabels.get(i)+"]");
				if (!query.isEmpty() && attrLabels.get(i)!=null && query.size()<=1){
					//Gets the first because it should have only one
					newRow.add(query.first().text());
				}
				else {
					//newRow.add("|no info found|");
					newRow.add(null);
				}
			}
			extractedTable.add(newRow);
		}
		
		return extractedTable;
	}
	
	
	//Uses the majority tags technique. It looks for rows that are equivalent to the exampleRows and then gets the children 
	//of the table that have the same characteristics.
	public Elements getRows(Element table, List<String> keyExamples,AttributeType keyAttrType) 
			throws BadSiteException, UnexpectedArgumentException{

		MajorityTagsFinder mtf = new MajorityTagsFinder();
		Elements rows = mtf.getMajorityTagRows(table.children(),keyExamples,keyAttrType);
		if (rows==null || rows.isEmpty()){
			throw new BadSiteException ("It was not possible to identify rows like the ones from the example in this table");
		}
		return rows;
	}
	
	
	//Returns the List of labels from which we can extract the attributes equivalent to the ones in exampleRows. 
	//The list of labels is in the order correspondent to the order of the attributes in the exampleRows.
	//If one of the attributes is not present, we return the null object as its label
	//The exampleRows must be all of the same size.
	private List<String> getRelevantLabels(Elements rows, List<List<String>> attributes, 
			AttributeType[] columnTypes) throws UnexpectedArgumentException{
		
		List<String> labels = new ArrayList<String>();
		
		//Finds the most probable label for each attribute
		for (int i=0; i<attributes.size(); i++){
			labels.add(getAttributeLabel(rows, attributes.get(i),columnTypes[i]));
		}
		
		return labels;
	}
	

	
	//Given rows found and parsed by the algorithm (at least one must be equivalent to one of the example rows), 
	//and a list of 'examples of one of the attributes' extracted from the example rows, 
	//returns the most probable label for that attribute. With this label we can get the information from rows that were 
	//parsed in the same way.
	private String getAttributeLabel(Elements rows, List<String> attrExamples, 
			AttributeType attrType) throws UnexpectedArgumentException{
		
		if (rows.isEmpty() || attrExamples.isEmpty()){
			throw new UnexpectedArgumentException("It is not possible to find the label: there are no rows or attribute examples");
		}
		
		Map<String,Integer> labelCounter = new HashMap<String, Integer>();
		for (int i=0; i<rows.size(); i++){
			Element row = rows.get(i);
			if (row!=null){
				for (String attrExample : attrExamples){
					//For each row, if the attrExample appears in more than one label, we select the smaller one
					//This is an heuristics made to select the most probable attribute label: the larger the content,
					//the higher the chance it has some word that we're looking for. Our "match" is better when it is of the same
					//size of what we're looking for, and since we're never getting anything smaller than it, then the smaller 
					//the match, the better.
					int smallerSize = 99999;
					String bestLabelForRowAndAttr = null;
					Elements eltsWithAttrExample = ElementSelector.select(row.getAllElements(), attrExample, 
							attrType, ":matchesOwn((?ui)", ")");
					for (Element attrElement : eltsWithAttrExample){	
						if (attrElement.text().length()<smallerSize && attrElement.hasAttr("label")){
							smallerSize = attrElement.text().length();
							bestLabelForRowAndAttr = attrElement.attr("label");
						}
					}
					if (bestLabelForRowAndAttr!=null){
						if (labelCounter.containsKey(bestLabelForRowAndAttr)){
							labelCounter.put(bestLabelForRowAndAttr, labelCounter.get(bestLabelForRowAndAttr)+1);
						}
						else{
							labelCounter.put(bestLabelForRowAndAttr, 1);
						}
					}
				}
				
			}
			
		}
		String bestLabel = null;
		int bestCounter = 0;
		for (String currentLabel: labelCounter.keySet()){
			int currentCounter = labelCounter.get(currentLabel); 
			if (currentCounter>bestCounter) {
				bestLabel = currentLabel;
				bestCounter = currentCounter;
			}
		}
		
		return bestLabel;
	}

	//Gets the elements that have, in its children, at least two elements with keys from the example table
	//Returns an empty list if we can't find any.
	public Elements getDeepestTablesWithExamples(Elements elements, List<String> keyExamples, AttributeType attrType) {
		
		//We add each element that has at least two keys to a list.
		Elements candidateTables = new Elements();
		for (int i=0; i<keyExamples.size();i++){
			for (int j=i+1;j<keyExamples.size();j++){
				Elements candidates = 
						getIntersection(ElementSelector.select(elements, keyExamples.get(i), attrType, ":matches((?ui)",")"), 
								ElementSelector.select(elements, keyExamples.get(j), attrType, ":matches((?ui)",")"));
				//Elements candidates = getIntersection(elements.select(":matches((?ui)"+keyExamples.get(i)+")"), 
				//		elements.select(":matches((?ui)"+keyExamples.get(j)+")"));
				for (Element candidate : candidates){
					if (!candidateTables.contains(candidate)){
						candidateTables.add(candidate);
					}
				}
			}
		}
		//If we find no elements with at least two key examples, we return an empty list.
		if(candidateTables.isEmpty()) return candidateTables;
		
		//Now, we keep only the elements that not only have at least two key examples, but that are also not a superset of others that have.
		Elements desiredTables = new Elements();
		for (Element table : candidateTables){
			boolean isNotSuperset = true;
			for (Element otherTable : candidateTables){
				if (!table.equals(otherTable) && table.getAllElements().contains(otherTable)) {
					isNotSuperset=false;
					break;
				}
			}
			if (isNotSuperset){
				desiredTables.add(table);
			}
		}
		
		return desiredTables;
	}
	
	private Elements getIntersection(Elements a, Elements b){
		Elements intersection = new Elements();
		for (Element e: a){
			if (b.contains(e)){
				intersection.add(e);
			}
		}
		return intersection;
	}
	
	
	
	
}
