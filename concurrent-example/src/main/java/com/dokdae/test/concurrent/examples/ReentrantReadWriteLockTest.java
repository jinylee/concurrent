package com.dokdae.test.concurrent.examples;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReentrantReadWriteLockTest {
	
	final static Logger LOG = LoggerFactory.getLogger(ReentrantReadWriteLockTest.class);
	
	public static void main(String[] args){
		
		final ReadWriteMap<String, String> cacheMap 
				= new ReadWriteMap<String, String>(
						new Callable<String>() {
							@Override
							public String call() throws Exception {
								try{
									Thread.sleep(10);
								}catch(InterruptedException e){
									e.printStackTrace();
								}
								String data  = "DATA - "+System.currentTimeMillis();
								LOG.info("Create DATA : {}", data);
								return data;
							}
						}
				);
		int nThreads=100;
		for( int i = 0 ; i < nThreads ; i++){
			final String key = "key - "+(i%3);
			new Thread(key){
				public void run(){
					String data;
					try {
						LOG.info("TRY GET DATA : {}", this.getName());
						data = cacheMap.get(this.getName());
						if(data==null) cacheMap.make(this.getName());
						data = cacheMap.get(this.getName());
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
						return;
					}
					LOG.info("GET DATA : {} - {}", this.getName(), data);
				}
			}.start();
		}
		
	}
	
	public static class ReadWriteMap<K,V>{
		
		private final Map<K,Future<V>> map;
		private final Callable<V> task;
		private final ReadWriteLock lock = new ReentrantReadWriteLock();
		private final Lock rlock = lock.readLock();
		private final Lock wlock = lock.writeLock();
		
		
		public ReadWriteMap(Callable<V> task){
			this.map = new ConcurrentHashMap<K, Future<V>>();
			this.task = task;
		}
		
		public Future<V> make(K key) throws InterruptedException, ExecutionException{
			wlock.lock();
			LOG.info("============ Write LOCK ============");
			if(map.get(key)!=null) {
				LOG.info("============ Write LOCK AND READ ============");
				wlock.unlock();
				return map.get(key);
			}
			FutureTask<V> futureTask =null;
			try{
				futureTask = new FutureTask<V>(task);
				new Thread(futureTask).start();
				map.put(key, futureTask);
			}finally{
				LOG.info("============ Write UNLOCK ============");
				wlock.unlock();
			}
			return futureTask;
		}
		
		public V get(K key) throws InterruptedException, ExecutionException{
			LOG.info("============ READ LOCK ============");
			rlock.lock();
			try{
				Future<V> value = map.get(key);
				return value!=null?value.get():null;
			}finally{
				rlock.unlock();
				LOG.info("============ READ LOCK ============");
			}
		}
	}
}
