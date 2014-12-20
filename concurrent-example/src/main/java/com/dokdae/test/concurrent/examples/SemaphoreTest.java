package com.dokdae.test.concurrent.examples;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <b>Semaphore 테스트</b><br/>
 * 정의된 갯수 만큼 유지하는 풀에서 사용될 수 있다. 
 * acquire 메소드는 넘겨줘야 할 갯수가 없을 때 타임아웃까지 대기하고 반환(release)되면 객체를 반환한다.
 * 
 * com.dokdae.test.concurrent.examples.SemaphoreTest - Creation date: 2014. 11. 16. <br/>
 * @author jinylee
 *
 */
public class SemaphoreTest {
	
	final static Logger LOG = LoggerFactory.getLogger(SemaphoreTest.class);
	
	final static BoundedHashSet<CustomConnection> usedConnection = new BoundedHashSet<CustomConnection>(10);
	
	public static void main(String[] args){
		
		for(int i=0 ; i<100; i++){
    		new Thread(){
    			public void run(){
					CustomConnection con = null; 
					try {
						con = createConnection();
						sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}finally{
						if(con!=null) releaseConnection(con);
					}
    			}
    		}.start();
		}
	}
	
	private static CustomConnection createConnection() throws InterruptedException{
		CustomConnection con = new CustomConnection(Thread.currentThread().getName()+"-"+System.currentTimeMillis());
		if(usedConnection.add(con)){
			return con;
		}else{
			return createConnection();
		}
	}
	private static void releaseConnection(CustomConnection o){
		usedConnection.remove(o);
	}
	
	public static class CustomConnection{
		final String name;
		public CustomConnection(String name){
			this.name = name;
		}
	}

}

class BoundedHashSet<T>{
	
	final Logger LOG = LoggerFactory.getLogger(BoundedHashSet.class);
	
	private final Set<T> set;
	private final Semaphore sem;
	
	public BoundedHashSet(int bound){
		set = Collections.synchronizedSet(new HashSet<T>());
		sem = new Semaphore(bound);		
	}
	
	public boolean add(T o) throws InterruptedException{
		LOG.debug("{} - checking available slots.... ", Thread.currentThread().getName());
		sem.acquire();
		LOG.debug("{} - have a empty slot! You can add it to hashset!", Thread.currentThread().getName());
		boolean wasAdded = false;
		try{
			wasAdded = set.add(o);
			return wasAdded;
		}finally{
			if(!wasAdded)
				sem.release();
		}
	}
	
	public boolean remove(Object o) {
		boolean wasRemoved = set.remove(o);
		if(wasRemoved){
			sem.release();
			LOG.debug("{} - released a slot.... ", Thread.currentThread().getName());
		}
		return wasRemoved;
	}
}
