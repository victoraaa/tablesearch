package parsers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.jsoup.Jsoup;


public class RoadRunnerCaller implements Runnable {

	public static void main(String[] args) {
		

	}
	
	String argv[];
	public RoadRunnerCaller(String[] argv){
		this.argv= new String[argv.length];
		for (int i=0;i<argv.length;i++){
			this.argv[i]= new String(argv[i]);
		}
	}
	
	@Override
	public void run() {
		try {
			roadrunner.Shell.main(argv);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		
	}
	
	
	
	
}
