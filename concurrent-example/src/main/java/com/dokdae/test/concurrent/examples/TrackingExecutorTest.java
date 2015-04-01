package com.dokdae.test.concurrent.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <b>종료 후 Cancel된 테스크를 조회할 수 있는 Executor</b><br/>
 * shutdownNow() 메소드를 실행하면 큐에 등록되었지만 실행되지 않았던 작업을 캡슐화하여 리턴한다. 
 * 이것은 캡슐화되어 태스크에 대한 정확한 내용을 모를뿐더 작업은 시작했지만 취소된 작업을 알 수 없다.
 * 이런 태스크를 반환해주는 TrackingExecutor이다
 * com.dokdae.test.concurrent.examples.TrackingExecutorTest - Creation date: 2014. 12. 21. <br/>
 * 
 * @author jinylee
 * 
 */
public class TrackingExecutorTest {

	final static Logger LOG = LoggerFactory.getLogger(TrackingExecutorTest.class);

	public static void main(String[] args) {

		LOG.info("================================================");
		ExecutorService exec = Executors.newFixedThreadPool(10);

		for (int delay = 1; delay <= 10; delay++) {
			exec.execute(new TestTask(delay));
		}

		try {
			Thread.sleep(5000);
			List<Runnable> drainList = exec.shutdownNow();
			exec.awaitTermination(2, TimeUnit.SECONDS);
			
			for(Runnable task:drainList){
				LOG.info("drained task : {}", task);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		LOG.info("================================================");
		
		ExecutorService exec1 = Executors.newFixedThreadPool(10);
		final TrackingExecutor texec = new TrackingExecutor(exec1);

		for (int delay = 1; delay <= 10; delay++) {
			texec.execute(new TestTask(delay));
		}

		try {
			Thread.sleep(5000);
			List<Runnable> drainList = texec.shutdownNow();
			texec.awaitTermination(2, TimeUnit.SECONDS);
			for(Runnable task:drainList){
				LOG.info("drained task : {}", task);
			}
			List<Runnable> cancelledList = texec.getCancelledTasks();
			for(Runnable task:cancelledList){
				LOG.info("cancelled task : {}", task);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		LOG.info("================================================");

	}

	public static class TestTask extends Thread {
		final long delay;

		public TestTask(long delay) {
			this.delay = delay;
		}

		@Override
		public void run() {
			try {
				LOG.info("delay task : {}", "testtask-" +delay);
				Thread.sleep(delay*1000);
				LOG.info("start task : {} ", "testtask-" +delay);
				Thread.sleep(2000);
				LOG.info("end task : {}", "testtask-" +delay);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOG.info("interrupted task : {}", "testtask-" +delay);
			} finally {
				LOG.info("clean up task : {}", "testtask-" +delay);
			}
		}
		
		public String toString(){
			return "testtask-" +delay;
		}
	}

	public static class TrackingExecutor extends AbstractExecutorService {

		private final ExecutorService exec;
		private final Set<Runnable> tasksCancelledAndShutdown = Collections.synchronizedSet(new HashSet<Runnable>());

		public TrackingExecutor(ExecutorService exec) {
			this.exec = exec;
		}

		@Override
		public void shutdown() {
			exec.shutdown();
		}

		@Override
		public List<Runnable> shutdownNow() {
			return exec.shutdownNow();
		}

		@Override
		public boolean isShutdown() {
			return exec.isShutdown();
		}

		@Override
		public boolean isTerminated() {
			return exec.isTerminated();
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			return exec.awaitTermination(timeout, unit);
		}

		@Override
		public void execute(final Runnable command) {
			exec.execute(new Runnable() {

				@Override
				public void run() {
					try {
						command.run();
					} finally {
						if (exec.isShutdown() && Thread.currentThread().isInterrupted()) {
							LOG.info("add interruped task : {}", command);
							tasksCancelledAndShutdown.add(command);
						}
					}
				}
			});
		}

		// new methods
		/**
		 * shutdown/shutdownNow 메소드에 의해 Cancel된 태스크를 리스트로 반환한다.
		 * 
		 * @return
		 */
		public List<Runnable> getCancelledTasks() {
			while (!exec.isTerminated()) {
				try{
					Thread.sleep(100);
				}catch(InterruptedException ignore){}
			}
			return new ArrayList<Runnable>(tasksCancelledAndShutdown);
		}
	}
}
