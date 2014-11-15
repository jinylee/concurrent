package com.dokdae.test.concurrent.examples;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>CountDownLatch 샘플</b><br/>
 * Latch는 쓰레드의 입구를 열고 닫는 대문이라 할 수 있다. 즉, 열리면 모든 쓰레드가 모두 시작되고 닫히면 모든 쓰레드가 wait가 된다.
 * 
 * 활용방법) 어플리케이션 종료 혹은 정지 시 처리시간이 긴 작업을 하고 있는 쓰레드들이 모두 잔여작업을 완료시키는 것을 확인하고 종료 및 정지할 필요가 있다. 
 * 이런 상황에서 CountDownLatch 좋은 활용이 될 수 있다. 
 * com.dokdae.test.concurrent.examples.CountDownLatchTest - Creation date: 2014. 11. 16. <br/>
 * 
 * @author jinylee
 * 
 */
public class CountDownLatchTest {
	
	final Logger LOG = LoggerFactory.getLogger(CountDownLatchTest.class);

	final private CountDownLatch startGate;
	final private CountDownLatch endGate;
	final private int nThread;
	final private Runnable task;

	private volatile boolean closed;

	public CountDownLatchTest(final int nThread, Runnable task) {
		this.nThread = nThread;
		this.task = task;
		this.startGate = new CountDownLatch(1); // 시작게이트
		this.endGate = new CountDownLatch(nThread); // 종료게이트
	}

	public void start() {
		_runTask();
		_prepare();
		LOG.debug("{} - start Application!!", Thread.currentThread().getName());
		
	}

	public void stop() throws InterruptedException{
		closed = true;
		endGate.await();
		_release();
		LOG.debug("{} - stop Application!!", Thread.currentThread().getName());
	}

	/**
	 * Task를 실행시키기 위한 준비작업을 수행한다.
	 */
	private void _prepare() {
		new Thread(){
			public void run(){
				LOG.debug("{} - start prepare resources....", Thread.currentThread().getName());
				try{
					Thread.sleep(1000);
				}catch(InterruptedException ie){
					Thread.currentThread().interrupt();
				}finally{
					LOG.debug("{} - .......end prepare resources!", Thread.currentThread().getName());
					startGate.countDown();
				}
			}
		}.start();
	}
	
	/**
	 * Task를 실행시키기 위한 준비한 리소스를 해제한다.
	 */
	private void _release(){
		LOG.debug("{} - release prepared resources", Thread.currentThread().getName());
	}

	/*
	 * 
	 */
	private void _runTask(){
		for(int i =0 ; i < nThread ; i++){
			new Thread(){
				public void run(){
					try{
						LOG.debug("{} - checking initailize.....", getName());
						startGate.await();
						LOG.debug("{} - start task....", getName());
						while(!closed){
							task.run();
						}
					}catch(InterruptedException ignored){
					}finally{
						LOG.debug("{} - ... closed task!!!", getName());
						endGate.countDown();
					}
				}
			}.start();
		}
	}
	
	public static void main(String[] args) throws InterruptedException{
		
		CountDownLatchTest test = new CountDownLatchTest(10, new Runnable() {
			
			@Override
			public void run() {
				System.out.println(Thread.currentThread().getName()+" - running......");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		});
		
		test.start();
		try{
			Thread.sleep(10000);
		}catch(InterruptedException ignored){
			ignored.printStackTrace();
		}
		test.stop();
	}
}
