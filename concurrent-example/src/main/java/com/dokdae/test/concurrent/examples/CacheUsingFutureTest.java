package com.dokdae.test.concurrent.examples;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheUsingFutureTest {
	
	final static Logger LOG = LoggerFactory.getLogger(CacheUsingFutureTest.class);

	public static void main(String[] args) throws InterruptedException{
		Computable<String, Integer> compute = new Computable<String, Integer>(){
			@Override
			public Integer compute(String arg) throws InterruptedException {
				return Integer.parseInt(arg);
			}
		};
		
		final CacheUsingFuture<String, Integer> cache = new CacheUsingFuture<String, Integer>(compute);
		
		for(int i=0 ; i < 10 ; i++){
			new Thread(){
				public void run(){
					try {
						cache.compute("1");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}.start();
			Thread.sleep(1);
		}
		
	}
	
	interface Computable<A, V>{
		V compute(A arg) throws InterruptedException;
	}
	
	public static class CacheUsingFuture<A, V> implements Computable<A, V>{
		
		private final ConcurrentHashMap<A, Future<V>> cache = new ConcurrentHashMap<A, Future<V>>();
		
		private final Computable<A,V> compute;
		
		public CacheUsingFuture(Computable<A,V> compute){
			this.compute = compute;
		}

		@Override
		public V compute(final A arg) throws InterruptedException {
			while(true){
				Future<V> f = cache.get(arg);
				if(f==null){
					LOG.debug("Not Cached!!");
					Callable<V> callable = new Callable<V>() {
						@Override
						public V call() throws Exception {
							V result = compute.compute(arg);
							LOG.debug("Compute : {} - {}!!", arg, result);
							return result;
						}
					};
					
					FutureTask<V> task = new FutureTask<V>(callable);
					f = cache.putIfAbsent(arg, task);
					
					if(f==null) {
						LOG.debug("No Cached Future Task!! It's runing");
						f = task; 
						task.run();
					}
				}else{
					LOG.debug("Cached!!");
				}
				
				try {
					return f.get();
				} catch (CancellationException e){
					cache.remove(arg, f);
				} catch (ExecutionException e) {
					throw new InterruptedException(e.getMessage());
				}
			}
		}
		
	}
}

