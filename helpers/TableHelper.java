package helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import exceptions.UnexpectedArgumentException;

import tableExtender.ErrorChecker;

public class TableHelper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

	
	public static void writeFormattedTable (Element table, String outputPath) throws IOException{
		FileWriter fw = new FileWriter(outputPath);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(Jsoup.parseBodyFragment(table.toString()).toString());
		bw.close();
	}
	
	public static void writeTable (List<List<String>> table, String outputPath) throws IOException{
		FileWriter fw = new FileWriter(outputPath);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for (List<String> row : table){
			Iterator<String> iterator = row.iterator();
			while(iterator.hasNext()){
				String toWrite = iterator.next(); 
				if (toWrite!=null){
					bw.write(toWrite);
				}
				else {
					bw.write("|no info found|");
				}
				
				if (iterator.hasNext()) bw.write(" -- ");
			}
			bw.newLine();
		}
		bw.close();
	}


	public static void printTables(Elements tables) {
		for (Element table : tables){
			System.out.println(table.toString());
		}
	}

	public static void printTable(List<List<String>> table) {
		for (List<String> row : table){
			for (String s : row){
				System.out.print(s + " - ");
			}
			System.out.println("");
		}
	}
	
	public static List<List<String>> attachTables(List<List<List<String>>> tables){
		List<List<String>> bigTable = new ArrayList<List<String>>();
		for (List<List<String>> table : tables){
			bigTable.addAll(table);
		}
		return bigTable;
	}
	
	//I don't know yet if this is a good idea. Making two rows one by making information may "create" unreal information.
	//RIGHT NOW, this only appends and deletes the duplicates.
	public static List<List<String>> appendTablesAndInfo(List<List<List<String>>> tablesList){
		//First we deal with each table: they may have rows that have the same information but one
		//is missing some attribute and the other isn't. We remove the ones that are subsets of the other.
		tablesList = removeSubsetRowsFromAllTables(tablesList);
		//This table is transitory. It keeps all the rows while we haven't appended their info yet.
		List<List<String>> bigTable = attachTables(tablesList);
		bigTable = ErrorChecker.deleteDuplicates(bigTable);
		return bigTable;
		/*
		//This is the final table. The rows are added here after we append their information with others.
		List<List<String>> appendedInfoTable = new ArrayList<List<String>>();
		Iterator<List<String>> it = bigTable.iterator();
		while (it.hasNext()){
			//This table keeps rows that have the same key so we can append their information together, and then add to the final table.
			List<List<String>> rowsWithSameKey = new ArrayList<List<String>>();
			List<String> currentRow = it.next();
			for (List<String> row : bigTable){
				if (row.get(keyIndex).toLowerCase().equals(currentRow.get(keyIndex).toLowerCase())){
					rowsWithSameKey.add(row);
				}
			}
			it.remove();
			appendedInfoTable.addAll(appendInfo(rowsWithSameKey));
		}
		appendedInfoTable = sortRowsByNonNullInfo(appendedInfoTable);
		return appendedInfoTable;
		*/
	}
	
	private static List<List<List<String>>> removeSubsetRowsFromAllTables(
			List<List<List<String>>> tablesList) {
		List<List<List<String>>> correctedTableList = new ArrayList<List<List<String>>>();
		for (List<List<String>> table : tablesList){
			correctedTableList.add(removeSubsetRows(table));
		}
		return correctedTableList;
	}


	private static List<List<String>> removeSubsetRows(List<List<String>> table) {
		
		//Makes an array that is a copy of the table. Each position has a row of the table.
		List<String>[] copyOfTable = (ArrayList<String>[])new ArrayList[table.size()];
		for (int i=0;i<table.size();i++){
			copyOfTable[i]=new ArrayList<String>(table.get(i));
		}
		//Compares each element in the array. If the second is a subset of the first, we make it null.
		for (int i=0;i<table.size();i++){
			for (int j=0;j<table.size();j++){
				if (j!=i && copyOfTable[i]!=null && copyOfTable[j]!=null && rowBIsSubsetOfRowA(table.get(i), table.get(j))){
					copyOfTable[j]=null;
				}
			}
		}
		//We return a new table with the positions of the array that have not been made null.
		List<List<String>> correctedTable = new ArrayList<List<String>>();
		for (int i=0;i<copyOfTable.length;i++){
			if (copyOfTable[i]!=null){
				correctedTable.add(new ArrayList<String>(copyOfTable[i]));
			}
		}
		return correctedTable;
	}

	//returns true if rowB is equal to A or has the same information in the columns that both of them have and has
	//no information in some column that rowA has.
	private static boolean rowBIsSubsetOfRowA(List<String> rowA,
			List<String> rowB) {
		
		for (int i=0;i<rowA.size();i++){
			if (rowB.get(i)!=null && !rowB.get(i).equals(rowA.get(i))){
				return false;
			}
		}
		return true;
	}


	public static void sortRowsByNonNullInfo (
			List<List<String>> table) {
		
		Comparator<List<String>> comparator = new Comparator<List<String>>() {
			@Override
			public int compare(List<String> a, List<String> b) {
				int result=0;
				try {
					if (a.size()!=b.size()) throw new UnexpectedArgumentException("The two lists should have the same size.");
					for (int i=0;i<a.size();i++){
						if (a.get(i)!=null) result++;
						if (b.get(i)!=null) result--;
					}
				}
				catch(UnexpectedArgumentException e){
					System.err.println(e.getMessage());
				}
				
				return (result);
			}
		
		};
		
		Collections.sort(table, comparator);
		Collections.reverse(table);
	}


	//YET TO DO
	/*
	private static List<List<String>> appendInfo (List<List<String>> rowsWithSameKey) {
		//We create an array os List<String>. Each position will have the options for each attribute.
		List<String>[] attrOptions = (ArrayList<String>[])new ArrayList[rowsWithSameKey.get(0).size()];
		return null;
	}
	*/
	
}
