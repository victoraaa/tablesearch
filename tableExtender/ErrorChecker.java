package tableExtender;

import helpers.TableHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import exceptions.UnexpectedArgumentException;


public class ErrorChecker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	//Gets a list of tables (table => List<List<String>>'s) and remove the ones that seem to have too many errors.
	public static List<List<List<String>>> deleteErrors(List<List<List<String>>> tableList, 
			int[] expectedMaxSizes,boolean[] mayBeEqual) throws UnexpectedArgumentException {
		
		//Let's verify if every array has the size equal to the number of attributes
		int nAttributes = expectedMaxSizes.length;
		if (expectedMaxSizes.length!=nAttributes || mayBeEqual.length!=nAttributes) {
			throw new UnexpectedArgumentException ("Size of the row is different from what was expected");
		}
		for (List<List<String>> table : tableList){
			for (List<String> row : table){
				if (row.size()!=nAttributes) throw new UnexpectedArgumentException ("Size of the row is different from what was expected");
			}
		}
		
		List<List<List<String>>> currentTableList;
		Iterator<List<List<String>>> it = tableList.iterator();
		while (it.hasNext()){
			List<List<String>> table = it.next();
			table.retainAll(deleteDuplicates(table));
		}
		
		currentTableList=filterBasedOnSize(tableList,expectedMaxSizes);
		currentTableList=filterRowsWithRepeatedColumns(currentTableList,mayBeEqual);
		currentTableList=filterRowsWithBlankColumns(currentTableList);
		
		currentTableList=filterBasedOnTableRelevance(tableList,currentTableList);	
		
		return currentTableList;
	}

	private static List<List<List<String>>> filterBasedOnTableRelevance(List<List<List<String>>> originalTableList, 
			List<List<List<String>>> currentTableList) {
		
		List<List<List<String>>> newTableList = new ArrayList<List<List<String>>>();
		
		for (int i=0; i<originalTableList.size();i++){
			double correctnessRate = (double) currentTableList.get(i).size()/originalTableList.get(i).size();
			if (correctnessRate>0.5){
				newTableList.add(new ArrayList<List<String>>(originalTableList.get(i)));
			}
		}
		
		return newTableList;
	}

	private static List<List<List<String>>> filterRowsWithRepeatedColumns(List<List<List<String>>> tableList, 
			boolean[] mayBeEqual) {
		
		List<List<List<String>>> newTableList = new ArrayList<List<List<String>>>();
		
		for (List<List<String>> table : tableList){
			List<List<String>> newTable = new ArrayList<List<String>>();
			for (List<String> row : table){
				boolean hasUnexpectedRepeatedColumns=false;
				for (int i=0; i<row.size();i++){
					if (!mayBeEqual[i] && row.get(i)!=null){
						for (int j=0; j<row.size();j++){
							if (j!=i && row.get(j)!=null && row.get(i).toLowerCase().equals(row.get(j).toLowerCase())){
								hasUnexpectedRepeatedColumns=true;
							}
						}
					}
				}
				if (!hasUnexpectedRepeatedColumns) {
					newTable.add(new ArrayList<String>(row));
				}
			}
			//We add even if it is empty, because then we will check for the number of rows on each table before and after filters)
			newTableList.add(newTable);
		}
		
		return newTableList;
	}

	
	private static List<List<List<String>>> filterRowsWithBlankColumns(List<List<List<String>>> tableList) {
		List<List<List<String>>> newTableList = new ArrayList<List<List<String>>>();
		
		for (List<List<String>> table : tableList){
			List<List<String>> newTable = new ArrayList<List<String>>();
			for (List<String> row : table){
				boolean attrsNotBlank=true;
				for (int i=0; i<row.size(); i++){
					if (row.get(i)!=null && (row.get(i).split(" ").length==0 || row.get(i).equals(""))){
						//We don't add this row to the new table if one of its attributes has no information on it
						attrsNotBlank = false;
						break;
					}
				}
				if (attrsNotBlank) {
					newTable.add(new ArrayList<String>(row));
				}
			}
			newTableList.add(newTable);
		}
		
		return newTableList;
	}

	private static List<List<List<String>>> filterBasedOnSize( List<List<List<String>>> tableList, int[] maxSizes) {
		List<List<List<String>>> newTableList = new ArrayList<List<List<String>>>();
		int rowLength = maxSizes.length;
		
		for (List<List<String>> table : tableList){
			List<List<String>> newTable = new ArrayList<List<String>>();
			for (List<String> row : table){
				boolean attrsWithinMaxSize=true;
				for (int i=0; i<rowLength; i++){
					if (row.get(i)!=null && row.get(i).split(" ").length>maxSizes[i]){
						//we don't add this row to the new table if one of its attributes is bigger than the maxSize for it.
						attrsWithinMaxSize=false;
						break;
					}
				}
				if (attrsWithinMaxSize) {
					newTable.add(new ArrayList<String>(row));
				}
			}
			newTableList.add(newTable);
		}
		return newTableList;
	}

	public static List<List<String>> deleteDuplicates(List<List<String>> table) {
		//Makes an array that is a copy of the table. Each position has a row of the table.
		List<String>[] copyOfTable = (ArrayList<String>[])new ArrayList[table.size()];
		for (int i=0;i<table.size();i++){
			copyOfTable[i]=new ArrayList<String>(table.get(i));
		}
		//Compares each element in the array. If two elements are equal, we make one of them null.
		for (int i=0;i<table.size()-1;i++){
			for (int j=i+1;j<table.size();j++){
				if (copyOfTable[j]!=null && compareListsIgnoresCase(table.get(i), table.get(j))){
					copyOfTable[j]=null;
				}
			}
		}
		//We return a new table with the positions of the array that have not been made null.
		List<List<String>> newTable = new ArrayList<List<String>>();
		for (int i=0;i<copyOfTable.length;i++){
			if (copyOfTable[i]!=null){
				newTable.add(new ArrayList<String>(copyOfTable[i]));
			}
		}
		return newTable;
	}
	
	//Returns true if each string of a list is equal to the equivalent of the other one, ignoring case.
	//Lists should have same size
	private static boolean compareListsIgnoresCase(List<String> a, List<String> b){
		
		for (int i=0; i<a.size();i++){
			if (a.get(i)==null || b.get(i)==null){
				if (a.get(i)!=b.get(i)) return false;
				else continue;
			}
			if (!a.get(i).equalsIgnoreCase(b.get(i))) return false;
		}
		return true;
	}

}
