package com.dokdae.test.concurrent.examples;

import java.util.concurrent.Exchanger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExchangerTest {
	
	final static Logger LOG= LoggerFactory.getLogger(ExchangerTest.class);
	
	public static void main(String[] args) throws InterruptedException{
		Exchanger<String> exchanger = new Exchanger<String>();
		ExchangerRunnable<String> run = new ExchangerRunnable<String>(exchanger, "A");
		ExchangerRunnable<String> run1 = new ExchangerRunnable<String>(exchanger, "B");
		new Thread(run).start();
		new Thread(run1).start();
	}
	
	public static class ExchangerRunnable<T> implements Runnable{
		
		final Exchanger<T> exchanger;
		private T object;
		
		ExchangerRunnable(Exchanger<T> exchanger, T object){
			this.exchanger = exchanger;
			this.object = object;
		}
		
		public void run(){
			try{
				Object previous = this.object;
				LOG.debug("have a value :  {}", this.object);
				this.object= this.exchanger.exchange(this.object);
				LOG.debug("exchanged {} for {}", previous, this.object);
				
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
}
