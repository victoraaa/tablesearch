package helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.AttributeType;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import exceptions.UnexpectedArgumentException;

public class ElementSelector {
							//FAZER O selectOWN!!!!!!!! 
	final static String NUMBERREGEX = "(?ui)(\\d+([\\.,\\s]\\d)*)+";
	
	//Receives all the necessary information to make a query on the elements (elts), looking for the String attr of the type attrType.
	//The query should be of the format queryStart+attr+queryEnd, and it should use the matches or matchesOwn argument.
	public static Elements select(Elements elts, String attr, AttributeType attrType,String queryStart,String queryEnd){
		Elements result = new Elements();
		try{
			switch (attrType){
				case STRING:
					result = elts.select(queryStart+attr+queryEnd);
					break;
				case NUMBER:
					result = selectForNumbers(elts, attr, queryStart, queryEnd); 
					break;
			}
		}
		catch(UnexpectedArgumentException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		
		return result;
	}
	
	//replaces all the occurrences of 'toReplace', of type 'attrType', by 'replacement', in the string 'text'
	//If the attrType is NUMBER, to 'replace' actually means to add a modifier
	public static String addModifierToAll(String text,String toReplace,String modifier,AttributeType attrType){
		String newText=text;
		try {
			switch (attrType){
				case STRING:
					newText = text.replaceAll("((?ui)"+toReplace+")", toReplace+modifier);
					break;
				case NUMBER:
					newText = addModifierToAllForNumericRanges(text, toReplace, modifier);
					break;
			}
		}
		catch (UnexpectedArgumentException e){
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		return newText;
	}
	
	public static boolean contains(String text, String keyword, AttributeType keywordType) {
		boolean contains = false;
		try {
			switch (keywordType){
				case STRING:
					contains = text.toLowerCase().contains(keyword.toLowerCase());
					break;
				case NUMBER:
					contains = containsForNumericRanges (text,keyword);
					break;
			}
		}
		catch (UnexpectedArgumentException e){
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
			
		return contains;
	}
	
	private static String addModifierToAllForNumericRanges(String text, String range, String modifier) throws UnexpectedArgumentException {
		String newText = text;
		String[] rangeArray = range.split("-");
		if (rangeArray.length!=2) throw new UnexpectedArgumentException("The string is not in the expected format. Should be: 'double-double' ");
		double min,max;
		try {
			min = Double.parseDouble(rangeArray[0]);
			max = Double.parseDouble(rangeArray[1]);
		}
		catch(NumberFormatException e){
			throw new UnexpectedArgumentException("The string does not contain numbers in Double format.");
		}
		
		for (String numberString : findStringsOfNumbers(text)){
			double number = transformIntoDouble(numberString);
			if (number>=min && number<=max){
				newText = newText.replaceAll(numberString, numberString+modifier);
			}
		}
		return newText;
	}
	
	private static boolean containsForNumericRanges(String text, String range) throws UnexpectedArgumentException {
		
		String[] rangeArray = range.split("-");
		if (rangeArray.length!=2) throw new UnexpectedArgumentException("The string is not in the expected format. Should be: 'double-double' ");
		double min,max;
		try {
			min = Double.parseDouble(rangeArray[0]);
			max = Double.parseDouble(rangeArray[1]);
		}
		catch(NumberFormatException e){
			throw new UnexpectedArgumentException("The string does not contain numbers in Double format.");
		}
		if (min>max) throw new UnexpectedArgumentException("The min value of the range is greater than the max value.");
		
		for (double number : findNumbers(text)){
			if (min<=number && number<=max) return true;
		}
		return false;
	}

	
	private static Elements selectForNumbers (Elements elts, String attr, String queryStart,String queryEnd) throws UnexpectedArgumentException{
		String[] range = attr.split("-");
		if (range.length!=2) throw new UnexpectedArgumentException("The string is not in the expected format. Should be: 'double-double' ");
		double min,max;
		try {
			min = Double.parseDouble(range[0]);
			max = Double.parseDouble(range[1]);
		}
		catch(NumberFormatException e){
			throw new UnexpectedArgumentException("The string does not contain numbers in Double format.");
		}
		if (min>max) throw new UnexpectedArgumentException("The min value of the range is greater than the max value.");
		
		
		Elements candidateElements = elts.select(queryStart+NUMBERREGEX+queryEnd);
		Elements results = new Elements();
		for (Element candidate : candidateElements){
			if (queryStart.contains("Own")){
				for (double number : findNumbers(candidate.ownText())){
					if (number>=min && number<=max){
						results.add(candidate);
						break;
					}
				}
			}
			else {
				for (double number : findNumbers(candidate.html())){
					if (number>=min && number<=max){
						results.add(candidate);
						break;
					}
				}
			}
			
			
		}
		return results;
	}
	
	private static List<String> findStringsOfNumbers(String text){
		Pattern pattern = Pattern.compile(NUMBERREGEX);
        Matcher matcher = pattern.matcher(text);

        List<String> numberStrings = new ArrayList<String>();
        while (matcher.find()) {
            numberStrings.add(matcher.group());
        }
        return numberStrings;
	}
	
	public static List<Double> findNumbers(String text){
		
		Pattern pattern = Pattern.compile(NUMBERREGEX);
        Matcher matcher = pattern.matcher(text);

        List<String> numberStrings = new ArrayList<String>();
        while (matcher.find()) {
            numberStrings.add(matcher.group());
        }
        List<Double> results = new ArrayList<Double>();
        
        for (int i=0;i<numberStrings.size();i++){
        	String modifiedResult = numberStrings.get(i);
        	results.add(transformIntoDouble(modifiedResult));
        }
        return results;
	}

	private static Double transformIntoDouble(String number) {
		number = number.replaceAll(" ", "");
		String[] splittedNumber = number.split("[,\\.]");
		StringBuilder sb = new StringBuilder();
		if (splittedNumber[splittedNumber.length-1].length()<=2){
			for (int i=0;i<splittedNumber.length-1;i++){
				sb.append(splittedNumber[i]);
			}
			sb.append(".");
			sb.append(splittedNumber[splittedNumber.length-1]);
		}
		else {
			for (int i=0;i<splittedNumber.length;i++){
				sb.append(splittedNumber[i]);
			}
		}
		return Double.parseDouble(sb.toString());
	}
	
}
