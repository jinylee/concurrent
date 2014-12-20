package com.dokdae.test.concurrent.examples;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadCancelBadCaseTest {
	
	final static Logger LOG= LoggerFactory.getLogger(ThreadCancelBadCaseTest.class);
	
	public static void main(String[] args){
		try{
			consumerPrimes();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	public static void consumerPrimes() throws InterruptedException{
		BlockingQueue<BigInteger> primeQueue = new ArrayBlockingQueue<BigInteger>(100);
		BrokenPrimeProducer p = new BrokenPrimeProducer(primeQueue);
		p.start();
		
		//queue에 100개까지 찰때까지 잠시 대기 .
		Thread.sleep(1000);
		
		try{
			while(needMorePrimes()){
				consume(primeQueue.take());
			}
		}finally{
			p.cancel();
			LOG.info("producer was cancelled!!");
		}
	}

	private static void consume(BigInteger data) {
		LOG.info("consume a data : {}", data);
	}

	/**
	 * 명시적으로 필요없을 설정하여 에러를 유도함.
	 * @return
	 */
	private static boolean needMorePrimes() {
		LOG.info("no needMorePrimes");
		return false;
	}
	
	
    /**
     * <b>잘못된 데이터 제공자(Producer) 구현체 </b><br/>
     * 아래의 코드와 같이 queue.put 호출 시 이미 큐의 데이터가 Full상태라면, 
     * Consumer가 소비할때까지 해당 쓰레드는 waiting 할 것이고 이럴 경우, while(!cancelled) 조건 문을 
     * 만나지 못하기 때문에 Consumer가 소비 속도가 느리거나 block되어 있을 경우, 영원히 종료되지 않는다.
     * 따라서 나쁜 구현이다. 
     * com.dokdae.test.concurrent.examples.BrokenPrimeProducer - Creation date: 2014. 12. 20. <br/>
     * @author jinylee
     *
     */
    public  static class BrokenPrimeProducer extends Thread{
    	
    	private final BlockingQueue<BigInteger> queue;
    	private volatile boolean cancelled = false;
    	
    	public BrokenPrimeProducer(BlockingQueue<BigInteger> queue) {
    		this.queue = queue;
    	}
    	
    	@Override
    	public void run(){
    		try{
    			BigInteger p  = BigInteger.ONE;
    			while(!cancelled){
    				LOG.info("producer put a data : {}", p);
    				queue.put(p = p.nextProbablePrime());
    			}
    		}catch(InterruptedException e){
    			//ignore
    			LOG.info("producer was interrupted");
    		}
    	}
    	
    	public void cancel(){	
    		cancelled = true;		
    	}
    }
}



