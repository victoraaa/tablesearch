package helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import parsers.RoadRunnerCaller;

public class TimeoutInvoker {

	/**
	 * @param args
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	public static void main(String[] args) throws SecurityException, NoSuchMethodException {
		// TODO Auto-generated method stub
		TimeoutInvoker ex = new TimeoutInvoker();
		Method m = ex.getClass().getMethod("neverEnds", String.class);
		Object[] argv = new Object[1];
		argv[0] = new String("i=");
		int returned=1;
		try {
			returned= (Integer) TimeoutInvoker.callMethodWithTimeout(ex, m, argv, 5);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			System.err.println("passou o tempo");
			ex.message = "deu excecao";
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		System.out.println(ex.teste+" "+returned);
	}
	
	String message = "nada";
	String teste="nao entrou";
	
	public int neverEnds (String s) throws Exception{
		int i=0;
		this.teste="entrou";
		while(i++!=2000000 ) {
			if (Thread.currentThread().isInterrupted()){
				throw new Exception("interrompido, logo saiu.");
			}
			System.out.print(s+" "+i);
		}
		System.out.println(this.message);
		return i;
		
	}

	
	public static Object callMethodWithTimeout(final Object instance,final Method m,final Object[] argv,int timeout) 
			throws TimeoutException{
		
		class RunnableClone implements Callable{
			@Override
			public Object call() throws IllegalArgumentException,IllegalAccessException,InvocationTargetException   {
				Object result;
				result = m.invoke(instance, argv);
				return result;
			}
		}
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<Object> task = (Callable<Object>) new RunnableClone();
		Future<Object> future = executor.submit(task);
		Object result = null;
		try {
		   result = future.get(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		   // handle the interrupts
			e.printStackTrace();
			System.err.println(e.getMessage());
		} catch (ExecutionException e) {
		   // handle other exceptions
			e.printStackTrace();
			System.err.println(e.getMessage());
		} catch (TimeoutException e) {
			future.cancel(true); // may or may not desire this
			executor.shutdownNow();
			throw new TimeoutException("The execution of the method "+m.getName()+" has been stopped " +
					"because it was not possible to execute it within the desired time " +
					"of "+timeout+" seconds");
		} finally {
		   future.cancel(true); // may or may not desire this
		   executor.shutdownNow();
		}
		
		return result;
	}
	
}
