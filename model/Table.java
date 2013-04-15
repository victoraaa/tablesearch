package model;

import java.util.ArrayList;
import java.util.List;

import exceptions.UnexpectedArgumentException;

public class Table {
	
	public static void main(String[] args) {
		List<List<String>> exampleRows = new ArrayList<List<String>>();
		List<String> row1 = new ArrayList<String>();
		List<String> row2 = new ArrayList<String>();
		row1.add("key1");
		row1.add("attr1");
		row2.add("key2");
		row2.add("key2");
		exampleRows.add(row1);
		exampleRows.add(row2);
		int[] maxSizes = new int[2];
		boolean[] mayBeEqual = new boolean[2];
		//Table table = new Table(inputTable, maxSizes, mayBeEqual, mainColumn, columnTypes, columnNames, originalSites);
	}
	
	//This is used just to keep the initial information. We transform this into listOfRows, changing numeric ranges to regex expressions.
	List<List<String>> input;
	
	List<List<String>> listOfRows;
	List<List<String>> listOfColumns;
	//For each row, we keep the original site from where we got the information.
	//For the user examples, we keep the value "user input"
	List<String> originalSites;
	
	int nColumns;
	int nRows;
	
	int[] maxSizes;
	boolean[] mayBeEqual;
	//The index of the column with the 'differentiating' column of the table. However, it is not necessarily the key.
	int mainColumnIndex;
	
	//An attribute type may be STRING or NUMBER.
	AttributeType[] columnTypes;
	List<String> columnNames;
	
	public Table(List<List<String>> inputTable, int[] maxSizes, boolean[] mayBeEqual, 
			int mainColumn, AttributeType[] columnTypes, List<String> columnNames, List<String> originalSites) throws UnexpectedArgumentException{
		
		//These may throw RuntimeExceptions
		checkSizes(inputTable,maxSizes,mayBeEqual,columnTypes,columnNames,mainColumn,originalSites);
		checkInputValues(inputTable,columnTypes);
		
		//If they don't throw exceptions, the input is valid and we fill the table.
		this.input = inputTable;
		this.nColumns = maxSizes.length;
		this.maxSizes = maxSizes;
		this.mayBeEqual = mayBeEqual;
		this.mainColumnIndex = mainColumn;
		this.columnTypes = columnTypes;
		this.columnNames = columnNames;
		this.originalSites = new ArrayList<String>();
		this.listOfRows = new ArrayList<List<String>>();
		this.listOfColumns = initializeListOfColumns();
		this.addAllRows(convertInputToListOfRows(inputTable,columnTypes),originalSites);
		
		
	}
	
	private List<List<String>> initializeListOfColumns() {
		List<List<String>> columns = new ArrayList<List<String>>();
		for (int i=0; i<this.maxSizes.length;i++){
			columns.add(new ArrayList<String>());
		}
		return columns;
	}

	public void addAllRows(List<List<String>> rows, List<String> originalSites) throws UnexpectedArgumentException {
		if (rows.size()!=originalSites.size()) throw new UnexpectedArgumentException("There is a different number " +
				"of rows and sites");
		
		for (int i=0; i<rows.size();i++){
			this.addRow(rows.get(i),originalSites.get(i));
		}
		
	}

	//This is here in case some of the types need some type of conversion.
	private List<List<String>> convertInputToListOfRows(List<List<String>> inputTable,
			AttributeType[] columnTypes) {
		
		List<List<String>> convertedRows = new ArrayList<List<String>>();
		
		for (List<String> row : inputTable){
			List<String> newRow = new ArrayList<String>();
			for (int i=0; i<columnTypes.length;i++){
				switch (columnTypes[i]){
					case STRING:
						newRow.add(row.get(i));
						break;
					case NUMBER:
						newRow.add(row.get(i));
						break;
				}
			
			}
			convertedRows.add(newRow);
		}
		return convertedRows;
	}

	//SHOULD CHECK: if the min value from range is lower than the max value, if the sizes of the cells are lower than maxSizes, 
	//        if they are equal but mayBeEqual is false...
	//This checks whether each cell satisfies the restraints of each type
	private void checkInputValues(List<List<String>> inputTable,
			AttributeType[] columnTypes) 
					throws UnexpectedArgumentException {
		
		//Here we check if each cell satisfies the restraints of each type
		for (int i=0; i<columnTypes.length;i++){
			for (List<String> row: inputTable){
				switch (columnTypes[i]){
					case STRING:
						//We accept anything if it's a string
						break;
					case NUMBER:
						String[] range = row.get(i).split("-");
						if (range.length!=2){
							throw new UnexpectedArgumentException("There is a number input that does not follow the appropriate format.");
						}
						else{
							try {
								Double.parseDouble(range[0]);
								Double.parseDouble(range[1]);
							}
							catch(NumberFormatException e){
								throw new UnexpectedArgumentException("There is a number input that does not follow the appropriate format.");
							}
							break;
						}
				}
			}
		}
		
		
		
	}

	//This checks if the size of all the arguments is the same and, if it is not, throws an exception.
	//We suppose that the table received by the constructor has all rows with the same size.
	private void checkSizes(List<List<String>> inputTable, 
			int[] maxSizes,
			boolean[] mayBeEqual, 
			AttributeType[] columnTypes,
			List<String> columnNames, 
			int mainColumn,
			List<String> originalSites) 
			throws UnexpectedArgumentException{
		
		int expectedSize = inputTable.get(0).size();
		if (expectedSize!=maxSizes.length) {
			throw new UnexpectedArgumentException("The array of maxSizes has wrong size.");
		}
		if (expectedSize!=mayBeEqual.length) {
			throw new UnexpectedArgumentException("The mayBeEqual array has wrong size.");
		}
		if (expectedSize!=columnTypes.length) {
			throw new UnexpectedArgumentException("The array of columnTypes has wrong size.");
		}
		if (expectedSize!=columnNames.size()) {
			throw new UnexpectedArgumentException("The array of columnNames has wrong size.");
		}
		if (expectedSize<=mainColumn) {
			throw new UnexpectedArgumentException("The index of mainColumn is bigger than the size of the examples");
		}
		if (inputTable.size()!=originalSites.size()) {
			throw new UnexpectedArgumentException("The array of original sites has a wrong size");
		}
		for (List<String> row : inputTable){
			if (row.size()!=expectedSize){
				throw new UnexpectedArgumentException("One of the rows has a wrong size");
			}
			for (int i=0;i<expectedSize;i++){
				if (row.get(i).split(" ").length>maxSizes[i]){
					throw new UnexpectedArgumentException("One of the attributes has size larger than the allowed maximum.");
				}
			}
		}

		
	}

	public List<List<String>> getRowsList(){
		return this.listOfRows;
	}
	
	public List<List<String>> getAttributesLists(){
		return this.listOfColumns;
	}
	
	public List<String> getEntireColumn(int index){
		return this.listOfColumns.get(index);
	}
	
	public int getMainColumnIndex(){
		return this.mainColumnIndex;
	}
	
	public int[] getMaxSizes(){
		return this.maxSizes;
	}
	
	public void addRow(List<String> row, String site) throws UnexpectedArgumentException{
		if (row.size()!=columnTypes.length) throw new UnexpectedArgumentException("The row does not have the expected size.");
		
		this.listOfRows.add(row);
		for (int i=0;i<row.size();i++){
			this.listOfColumns.get(i).add(row.get(i));
		}
		this.nRows++;
		this.originalSites.add(site);
	}
	
	public List<String> getMainColumn(){
		return getEntireColumn(mainColumnIndex);
	}
	
	public int size(){
		return this.nRows;
	}
	
	public AttributeType getKeyAttrType(){
		return this.columnTypes[this.getMainColumnIndex()];
	}
	
	public AttributeType[] getColumnTypes(){
		return this.columnTypes;
	}
	
	public List<String> getColumnNames(){
		return this.columnNames;
	}
}
