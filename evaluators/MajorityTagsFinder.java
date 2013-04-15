package evaluators;

import helpers.ElementSelector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import exceptions.BadSiteException;
import exceptions.UnexpectedArgumentException;

import extractors.RRTableExtractor;

import model.AttributeType;
import model.Tag;

public class MajorityTagsFinder {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		MajorityTagsFinder mtfinder = new MajorityTagsFinder();
		//File file = new File("C:/Users/Victor/workspace/InfoSearcher/Avaliator/input/games/gamespot.html");
		File file = new File("C:/Users/Victor/workspace/InfoSearcher/Avaliator/input/cars/httpwwwiseecarscomused_cars-t11658-used-chevrolet-camaro-rhode-island.html");
		Document doc = Jsoup.parse(file, "iso-8859-1");
		//Elements rows = mtfinder.getMajorityTagRows(doc.select("ul").first().children());
		RRTableExtractor tableExtractor = new RRTableExtractor();
		List<List<String>> exampleRows = new ArrayList<List<String>>();
		List<String> row1 = new ArrayList<String>();
		List<String> row2 = new ArrayList<String>();
		exampleRows.add(row1);
		exampleRows.add(row2);
		row1.add("Camaro");
		row1.add("westerly");
		row2.add("camaro 2ss");
		row2.add("Providence");
		/*
		Elements tables = tableExtractor.getDeepestTablesWithExamples(doc.getAllElements(),exampleRows);
		for (Element table : tables){
			try{
				Elements rows = mtfinder.getMajorityTagRows(table.children(),exampleRows);
				for (Element row : rows){
					System.out.println(row.text());
				}
			}
			catch(Exception e){
				System.err.println("Não foi possível achar majority rows para esta tabela.");
			}
			
		}
		*/
		
	}

	//returns null if there is not a majority tag.
	public Elements getMajorityTagRows(Elements rowCandidates,List<String> keyExamples, AttributeType keyAttrType) 
			throws UnexpectedArgumentException{
		if (rowCandidates.isEmpty()) throw new UnexpectedArgumentException ("There are no row candidates");
		
		//First, we transform the rows to a format with tagName and a map of attributes and its values.
		List<Tag> tags = constructTags(rowCandidates);
		
		//Then we search for a majority tag
		Tag majorityTagType;
		try{
			majorityTagType = findMajorityTagCharacteristics(tags,keyExamples, keyAttrType);
		}
		catch(BadSiteException e){
			System.err.println(e.getMessage());
			return null;
		}
		
		if (majorityTagType==null) return null;
		//Finally, if we find a majority tag, we filter the rows to return just those that are majority tags.
		Elements rows = filterMajorityTagRows(rowCandidates,majorityTagType);
		
		return rows;
	}

	//returns only the rowCandidates that have AT LEAST the tag/attributes that the majorityTagType has.
	private Elements filterMajorityTagRows(Elements rowCandidates,Tag majorityTagType){
		
		Elements rows = new Elements();
		String majorTagName = majorityTagType.tagName;
		for (Element rowCandidate : rowCandidates){
			if (rowCandidate.tagName().equals(majorTagName)){
				rows.add(rowCandidate);
			}
		}
		
		if (majorityTagType.attributes==null || majorityTagType.attributes.isEmpty()){
			return rows;
		}
		
		Iterator<Element> iterator = rows.iterator();
		while (iterator.hasNext()){
			Element row = iterator.next();
			for (String attrKey : majorityTagType.attributes.keySet()){
				if (!row.attributes().hasKey(attrKey)){
					iterator.remove();
					break;
				}
			}
		}
		
		return rows;
	}

	//Returns null if we can't find any major tag 
	private Tag findMajorityTagCharacteristics(List<Tag> tags, List<String> keyExamples, AttributeType keyAttrType) throws BadSiteException {
		
		Tag majorityTagType = new Tag(null,null,null);
		
		//First we look for the tags with the examples
		List<Tag> exampleTags = getExampleTags(tags, keyExamples, keyAttrType);
		
		//If we don't find the examples in the the tags, something is wrong.
		if (exampleTags.isEmpty()){
			throw new BadSiteException("It is not possible to find the examples in the tags.");
		}
		
		//Now we find the common characteristics between the tags.
		
		//1) The common tagName.
		String commonTag = exampleTags.get(0).tagName;
		for (Tag tag : exampleTags){
			if (!tag.tagName.toLowerCase().equals(commonTag.toLowerCase())){ 
				return null;
			}
		}
		majorityTagType.tagName=commonTag;
		
		//2)The common attribute keys. These are the ones that appear in all of the example tags.
		List<String> attrKeys = new ArrayList<String>();
		
		//First we add all of the ones that are in one of the exampleTags.
		for (String attrKey : exampleTags.get(0).attributes.keySet()){
			attrKeys.add(attrKey);
		}
		
		//Then, we remove all of those that are not in the others.
		for (Tag tag : exampleTags){
			Iterator<String> iterator = attrKeys.iterator();
			while (iterator.hasNext()){
				String attrKey = iterator.next();
				if (!tag.attributes.containsKey(attrKey)){
					iterator.remove();
				}
			}
		}
		
		//Finally, we put the attribute keys on the tag that we will return
		for (String attrKey : attrKeys){
			if (majorityTagType.attributes==null){
				majorityTagType.attributes=new HashMap<String, String>();
			}
			majorityTagType.attributes.put(attrKey, null);
		}
		
		return majorityTagType;
	}

	private List<Tag> getExampleTags(List<Tag> tags, List<String> keyExamples, AttributeType keyAttrType) {
		List<Tag> exampleTags = new ArrayList<Tag>();
		for (Tag tag : tags){
			boolean hasExample=false;
			for (String key : keyExamples){
				ElementSelector.contains(tag.text.toLowerCase(),key.toLowerCase(), keyAttrType);
				if (tag.text.toLowerCase().contains(key.toLowerCase())){
					hasExample=true;
					break;
				}
			}
			if (hasExample){
				exampleTags.add(tag);
			}
		}
		return exampleTags;
	}

	private String findMajorAttributeValue(List<Tag> tags,
			String majorTagLabel, String majorAttributeKey) {
		
		Map<String,Integer> valueCounter = new HashMap<String,Integer>();
		for (Tag tag : tags){
			String value = tag.attributes.get(majorAttributeKey);
			if (valueCounter.containsKey(value)) {
				valueCounter.put(value,valueCounter.get(value)+1);
			}
			else {
				valueCounter.put(value,1);
			}
		}
		int majorValueCount=0;
		String majorValueName="";
		for (String valueName : valueCounter.keySet()){
			if (valueCounter.get(valueName)>majorValueCount){
				majorValueCount=valueCounter.get(valueName);
				majorValueName=valueName;
			}
		}
		double nTagsWithMajorValue = 0;
		for (Tag tag : tags){
			if (tag.attributes.containsValue(majorValueName)){
				nTagsWithMajorValue++;
			}
		}
		double majorValuePercentage = (double) nTagsWithMajorValue/tags.size();
		if (majorValuePercentage<=0.5){
			return null;
		}
		else {
			return majorValueName;
		}
	}

	private String findMajorAttributeKey(List<Tag> tags, String majorTagLabel) {
		Map<String,Integer> attrCounter = new HashMap<String,Integer>();
		for (Tag tag : tags){
			for (String attr : tag.attributes.keySet()){
				if (attrCounter.containsKey(attr)) {
					attrCounter.put(attr,attrCounter.get(attr)+1);
				}
				else {
					attrCounter.put(attr,1);
				}
			}
			
		}
		int majorAttrCount=0;
		String majorAttrName="";
		for (String attrName : attrCounter.keySet()){
			if (attrCounter.get(attrName)>majorAttrCount){
				majorAttrCount=attrCounter.get(attrName);
				majorAttrName=attrName;
			}
		}
		double nTagsWithMajorAttr = 0;
		for (Tag tag : tags){
			if (tag.attributes.containsKey(majorAttrName)){
				nTagsWithMajorAttr++;
			}
		}
		double majorAttrPercentage = (double) nTagsWithMajorAttr/tags.size();
		if (majorAttrPercentage<=0.5){
			return null;
		}
		else {
			return majorAttrName;
		}
	}

	private String findMajorTagLabel(List<Tag> tags){
		
		Map<String,Integer> tagCounter = new HashMap<String,Integer>();
		for (Tag tag : tags){
			if (tagCounter.containsKey(tag.tagName)) {
				tagCounter.put(tag.tagName,tagCounter.get(tag.tagName)+1);
			}
			else {
				tagCounter.put(tag.tagName,1);
			}
		}
		int majorTagCount=0;
		String majorTagName="";
		for (String tagName : tagCounter.keySet()){
			if (tagCounter.get(tagName)>majorTagCount){
				majorTagCount=tagCounter.get(tagName);
				majorTagName=tagName;
			}
		}
		double majorTagPercentage = (double) tagCounter.get(majorTagName)/tagCounter.keySet().size();
		if (majorTagPercentage<=0.5){
			return null;
		}
		else {
			return majorTagName;
		}
	}

	private List<Tag> constructTags(Elements rowCandidates) {
		List<Tag> tags = new ArrayList<Tag>();
		for (Element row : rowCandidates){
			Map<String,String> attributesMap = new HashMap<String, String>();
			for (Attribute attr : row.attributes()){
				attributesMap.put(attr.getKey(), attr.getValue());
			}
			Tag tag = new Tag(row.tagName(),attributesMap,row.text());
			tags.add(tag);
		}
		return tags;
	}
	
}
