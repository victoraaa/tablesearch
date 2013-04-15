package model;

import java.util.HashMap;
import java.util.Map;

public class Tag {

	public String tagName;
	public Map<String,String> attributes = new HashMap<String, String>();
	public String text;
	
	public Tag (String tagName, Map<String,String> attributes,String text){
		this.tagName=null;
		this.attributes=null;
		this.text=null;
		if (tagName!=null){
			this.tagName=new String(tagName);
		}
		if (attributes!=null){
			this.attributes=new HashMap<String,String>(attributes);
		}
		if (text!=null){
			this.text=text;
		}
	}
}
