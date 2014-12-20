package com.dokdae.test.concurrent.examples;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <b>특정 timeout 시간까지 처리가 완료되지 않은 경우 InterruptedException을 발생시키는 테스트</b><br/>
 * com.dokdae.test.concurrent.examples.ThreadTimedRunBadCaseTest - Creation date: 2014. 12. 20. <br/>
 * @author jinylee
 *
 */
public class ThreadTimedRunGoodCaseTest {

	final static Logger LOG= LoggerFactory.getLogger(ThreadTimedRunGoodCaseTest.class);
	
	final static ExecutorService taskExec = Executors.newFixedThreadPool(10);
	
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
			taskExec.shutdown();
		}
	}
	
	
	/**
	 * Future 클래스를 통한 작업시간 지키기 구현 
	 * @param r
	 * @param timeout
	 * @throws InterruptedException
	 */
	public static void timedRun(final Runnable r, long timeout) throws InterruptedException{
		
		Future<?> task = taskExec.submit(r);
		try {
			task.get(timeout, TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			// 실행 오류가 발생한 경우 finally에서 처리하도록 한다. 
			LOG.info("Execution Exception !!");
		} catch (TimeoutException e) {
			// 시간이 지연되어 발생한 경우는 예외를 다시 던진다.
			LOG.info("Timeout Exception !!");
			throw launderThrowable(e.getCause());
		} finally{
			if(task.isDone()){
				LOG.info("Task Is Done!!");
			}else{
				LOG.info("Task Is Cancelled!!");
				// 실행중이라면 interrupt을 건다.
				task.cancel(true);
			}
			
		}
	}
	
	public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else if (t instanceof InterruptedException)
        	throw new RuntimeException(t);
        else if (t instanceof TimeoutException)
        	throw new RuntimeException(t);
        else
            throw new IllegalStateException("Not unchecked", t);
    }
	
}
