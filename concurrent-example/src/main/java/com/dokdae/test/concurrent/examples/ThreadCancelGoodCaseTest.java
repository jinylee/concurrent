package com.dokdae.test.concurrent.examples;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadCancelGoodCaseTest {
	
	final static Logger LOG= LoggerFactory.getLogger(ThreadCancelGoodCaseTest.class);
	
	public static void main(String[] args){
		try{
			consumerPrimes();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	public static void consumerPrimes() throws InterruptedException{
		BlockingQueue<BigInteger> primeQueue = new ArrayBlockingQueue<BigInteger>(100);
		SafePrimeProducer p = new SafePrimeProducer(primeQueue);
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
	 * 
	 * <b>쓰레드 중단 및 종료 처리에 대한 올바른 Procuder 구현체</b><br/>
	 * queue에 데이터 full 상태에서 queue.put을 호출하면 이 쓰레드는 queue.put 메소드에서 wait가 
	 * 걸려서 대기상태로 빠진다. 작업을 취소하거나 종료하기 위해 interrupt을 호출하면, 
	 * queue.put 메소드에서 interruptedException이 발생하고 producer를 catch문에서 처리한다. 
	 * catch문에는 이것이 실제 종료(cancelled) 인지 확인하기 위해 다시 loop을 수행하게 처리하고, 
	 * 만일, interrupted가 되었고, cancelled 필드가 true라면 작업취소 및 종료임을 확인하고 loop를 빠져나온다. 
	 * 
	 * 여기서, 잠시 고려할 사항은 interrup exception에 대한 처리 문제이다.
	 * BlockingQueue가 자체 구현한 큐이고 쓰레드를 waiting하는 동기화 컬렉션이라면 Interrupt Exception을 
	 * 전파하여야 한다.만일, 범용 모듈인데 전파하지 않고 자체에서 해결한다면 쓰레드는 해당 interrupt에 대한 제어권을 가지지 못하기 때문에
	 * 종료 및 취소에 대한 작업이 불가능할 수 도 있다. 
	 *   
	 * 즉, 중요한것은 Interrupt Exception을 무시하지 말고, 전파하든 자체 처리하든 처리를 해주는 것이 좋다는 얘기다. 
	 * com.dokdae.test.concurrent.examples.SafePrimeProducer - Creation date: 2014. 12. 20. <br/>
	 * @author jinylee
	 *
	 */
     public  static class SafePrimeProducer extends Thread{
    	
    	private final BlockingQueue<BigInteger> queue;
    	private volatile boolean cancelled = false;
    	
    	public SafePrimeProducer(BlockingQueue<BigInteger> queue) {
    		this.queue = queue;
    	}
    	
    	@Override
    	public void run(){
    		try{
    			BigInteger p  = BigInteger.ONE;
    			while(!Thread.currentThread().isInterrupted() && !cancelled){
    				try{
        				LOG.info("producer put a data : {}", p);
        				queue.put(p = p.nextProbablePrime());
    				}catch(InterruptedException e){
    					LOG.info("producer was interrupted : cancelled={} - {}", cancelled, e.toString());
    				}
    			}
    		}finally{
    			//ignore
    			LOG.info("producer was terminated");
    		}
    	}
    	
    	public void cancel(){	
    		cancelled = true;	
    		interrupt();
    	}
    }
	
}



