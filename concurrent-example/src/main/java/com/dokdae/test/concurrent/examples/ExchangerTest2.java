package com.dokdae.test.concurrent.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * <b>exchangerTest2</b><br/>
 * 아래 예제는 두개의 list 를 이용하여 ,
 * 한쪽 쓰레드에서는 1씩 증가하는 숫자를 5개씩 리스트에 저장하여 exchanger를 통해 보내고
 * 다른 쓰레드에서 교환할 리스트를 줄때까지 waiting한다. 
 * 
 * 다른 쓰레드에서는 5개가 차여있는 리스트를 exchanger를 통해 받고, 
 * 비어있는 리스트를 수신할 때까지 waiting한다. 
 * 
 * 잘못 짜면 둘다 waiting 하는 dead lock이 발생할 수 있다. 주의하자.!!
 * 
 * com.dokdae.test.concurrent.examples.ExchangerTest2 - Creation date: 2014. 11. 24. <br/>
 * @author jinylee
 *
 */
public class ExchangerTest2 {
	
	
	public static void main(String[] args){
		final Exchanger<List<Integer>> exchanger = new Exchanger<List<Integer>>();
		new Thread(new Calculate5ListRunnable(exchanger, new ArrayList<Integer>())).start();
		new Thread(new Add5ListRunnable(exchanger, new ArrayList<Integer>())).start();
	}
	
	final static Logger LOG= LoggerFactory.getLogger(ExchangerTest2.class);
	
	public static class Add5ListRunnable implements Runnable{
		
		final Exchanger<List<Integer>> exchanger;
		List<Integer> pipe;
		
		public Add5ListRunnable(Exchanger<List<Integer>> exchanger, List<Integer> pipe){
			this.exchanger = exchanger;
			this.pipe = pipe;
		}

		@Override
		public void run() {
			int num =1;
			try{
    			while(true){
    				for(int i=0; i < 5; i++){
    					pipe.add(num++);
    				}
    				LOG.debug("pipe send to exchanger");
       				pipe = exchanger.exchange(pipe);
       				LOG.debug("get empty pipe from exchanger");
    			}
			}catch(InterruptedException e){
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static class Calculate5ListRunnable implements Runnable{
		final Exchanger<List<Integer>> exchanger;
		List<Integer> pipe;
		
		public Calculate5ListRunnable(Exchanger<List<Integer>> exchanger, List<Integer> pipe){
			this.exchanger = exchanger;
			this.pipe = pipe;
		}

		@Override
		public void run() {
			int total = 0;
			try{
    			while(true){
    				LOG.debug("waiting for exchanging from non empty pipe");
    				pipe = exchanger.exchange(pipe);
    				LOG.debug("exchanged from pipe : {}", pipe.size());
    				for(int i =0 ; i < pipe.size() ; i++){
    					total+=pipe.get(i);
    				}
    				pipe.clear();
    				LOG.debug("total sum : {}", total);
    				if(total>100){
    					System.exit(0);
    				}
    			}
			}catch(InterruptedException e){
				e.printStackTrace();
			}catch(Exception e){ 
				e.printStackTrace();
			}
		}
	}
}
