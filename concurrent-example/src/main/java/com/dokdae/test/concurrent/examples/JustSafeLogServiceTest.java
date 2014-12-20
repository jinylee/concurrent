package com.dokdae.test.concurrent.examples;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <b>쓰레드 안전하게 로그를 기록하는 테스트 </b><br/>
 * 안정적이지만 성능이 안좋을 것으로 예상
 * com.dokdae.test.concurrent.examples.SafeLogServiceTest - Creation date: 2014. 12. 20. <br/>
 * @author jinylee
 *
 */
public class JustSafeLogServiceTest {
	
	final static Logger LOG= LoggerFactory.getLogger(JustSafeLogServiceTest.class);
	
	public static void main(String[] args){
		
		final LogService logService = new LogService();
		logService.start();
		
		int i = 0;
		try{
    		while(true){
 				logService.log("logging "+i++);
    		}
		}catch(InterruptedException e){
			//ignore
			e.printStackTrace();
		}
	}
	
	/**
	 * <b>너무 안정성만 생각하는 로그 서비스</b><br/>
	 * com.dokdae.test.concurrent.examples.LogService - Creation date: 2014. 12. 20. <br/>
	 * @author jinylee
	 *
	 */
	public static class LogService{
		private final BlockingQueue<String> queue;
		private final LoggerThread loggerThread;
		private final PrintWriter writer;
		
		@GuardedBy("this")
		private boolean isShutdown;
		@GuardedBy("this")
		private int reservations;
		
		
		public LogService(){
			this.queue = new LinkedBlockingQueue<String>();
			this.loggerThread = new LoggerThread();
			this.writer = new PrintWriter(System.out);
		}
		
		public void start(){
			this.loggerThread.start();
			Runtime.getRuntime().addShutdownHook(new Thread(){
    			public void run(){
    				LogService.this.stop();
    			}
    		});
		}
		
		public void stop(){
			synchronized(this){	this.isShutdown = true;	}
			loggerThread.interrupt();
		}
		
		public void log(String msg) throws InterruptedException{
			synchronized(this){
				if(this.isShutdown){
					throw new IllegalStateException("Log Service was shutdowned!!");
				}
			}
			queue.put(msg);
			
			//put에서 interrupted가 발생할 수 있으므로...
			synchronized(this){
				++reservations;
			}
		}
		
		private class LoggerThread extends Thread{
			
			public void run(){
				try{
					while(true){
						synchronized(LogService.this){
							if(isShutdown && reservations==0){
								break;
							}
						}
						String msg;
						try {
							msg = queue.take();
							synchronized (LogService.this) {
								--reservations;
    						}
    						writer.println(msg);
						} catch (InterruptedException e) {
							/*retry*/
						}
					}
				}finally{
					writer.close();
				}
			}
		}
	}
}
