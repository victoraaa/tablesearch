package model;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parsers.RRInterface;

//A specified row is a row with its HTML and the attributes that we are looking for.
public class SpecifiedRow {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	public Element row;
	public List<String> attributes;
	public String key;
	
	public SpecifiedRow (Element row, List<String> attributes, String key){
		this.row = new Element(row.tag(),row.baseUri(),row.attributes());
		this.row.html(row.html());
		this.attributes = new ArrayList<String>(attributes);
		this.key = key;
	}
	

}
