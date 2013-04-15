package helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AsynchronousPageDownloader implements Runnable {

	String url;
	String outputfolder;
	String outputfilename;
	public AsynchronousPageDownloader (String url,String outputfolder,String outputfilename){
		this.url=url;
		this.outputfolder=outputfolder;
		this.outputfilename=outputfilename;
	}
	
	@Override
	public void run() {
		PageDownloader pd = new PageDownloader();
		FileWriter fw=null;
		BufferedWriter bw=null;
		try {
			fw = new FileWriter("files/SiteExamples/"+outputfolder+"/"+outputfilename+"/");
			bw = new BufferedWriter(fw);
			String page = pd.getPage(url);
			bw.write(page);
			
		}
		catch (IOException e){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		try {
			if (bw!=null){
				bw.close();
			}
			if (fw!=null){
				fw.close();
			}
		}
		catch(IOException e){
			System.err.println("It was not possible to close the writers: "+e.getMessage());
		}
		System.out.println(url);
		
	}
	
}
