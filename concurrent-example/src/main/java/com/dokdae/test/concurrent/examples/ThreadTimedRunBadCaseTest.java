package com.dokdae.test.concurrent.examples;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <b>특정 timeout 시간까지 처리가 완료되지 않은 경우 InterruptedException을 발생시키는 테스트</b><br/>
 * com.dokdae.test.concurrent.examples.ThreadTimedRunBadCaseTest - Creation date: 2014. 12. 20. <br/>
 * @author jinylee
 *
 */
public class ThreadTimedRunBadCaseTest {

	final static Logger LOG= LoggerFactory.getLogger(ThreadTimedRunBadCaseTest.class);
	
	final static ScheduledThreadPoolExecutor cancelExec = new ScheduledThreadPoolExecutor(10);
	
	public static void main(String[] args){
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try{
					LOG.info("start task working...");
					Thread.sleep(10000);
					LOG.info("end task working...");
				}catch(InterruptedException e){
					//ignore
					LOG.info("interrupted task working...");
					throw launderThrowable(e);
				}
			}
		};
		try{
			timedRun(task, 5000);
		}catch(Throwable e){
			LOG.info("interrupted : {}", e.getCause());
		}
		
		try{
			timedRun(task, 15000);
		}catch(Throwable e){
			LOG.info("interrupted : {}", e.getCause());
		}finally{
			cancelExec.shutdown();
		}
	}
	
	
	/**
	 * InterruptedException을 재발생시키게 한다. 
	 * 이 방법의 문제는 join 메소드에 있는데, 만일 외부에서 해당 쓰레드에 대해 interrupt 호출하면, join은 interruptedException을 발생시킨다. 
	 * 즉, timedRun에서 발생하는 InterruptedException이 join에서 발생한 것인지, timeout되어 발생한 것인지 알수 가 없다는 것이다. 
	 * @param r
	 * @param timeout
	 * @throws InterruptedException
	 */
	public static void timedRun(final Runnable r, long timeout) throws InterruptedException{
		
		class RethrowableTask implements Runnable{
			private volatile Throwable t;

			@Override
			public void run() {
				try{
					r.run();
				}catch(Throwable t){
					LOG.info("throwable : {}", t.getMessage());
					this.t = t;
					LOG.info("set throwable data : {}", t.getMessage());
				}
			}
			void rethrow(){
				LOG.info("rethrow - {}", (this.t!=null?t.getMessage():"null"));
				if(this.t!=null) throw launderThrowable(this.t);
				
			}
		}
		
		RethrowableTask task = new RethrowableTask();
		final Thread taskThread= new Thread(task);
		taskThread.start();
		
		cancelExec.schedule(new Runnable() {
			@Override
			public void run() {
				LOG.info("call interrupt method!");
				taskThread.interrupt();
				
			}
		}, timeout, TimeUnit.MILLISECONDS);
		
		LOG.info("join timeout : {}", timeout);
		taskThread.join(timeout+100);
		LOG.info("rethrow!!");
		task.rethrow();
	}
	
	public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else if (t instanceof InterruptedException)
        	throw new RuntimeException(t);
        else
            throw new IllegalStateException("Not unchecked", t);
    }
	
}
