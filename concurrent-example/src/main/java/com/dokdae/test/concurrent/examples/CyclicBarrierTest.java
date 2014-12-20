package com.dokdae.test.concurrent.examples;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <b>Barrier 테스트</b><br/>
 * Latch의 경우 countDown되어 쓰레드가 풀리면 다시 사용할 수 없다.
 * 그러나, 배리어는 반복적으로 사용할 수 있다 
 * com.dokdae.test.concurrent.examples.CyclicBarrierTest - Creation date: 2014. 11. 16. <br/>
 * @author jinylee
 *
 */
public class CyclicBarrierTest {
	
	public static void main(String[] args){
		final Board mainBoard = new Board();
		CellularAutomata auto = new CellularAutomata(mainBoard);
		auto.start();
	}

}

class Board{
	final String[] data;
	final String[] result;
	
	private Board root;
	private int sindex =-1;
	
	Board(){
		data = new String[1024];
		for(int i=0; i < data.length ; i++){
			this.data[i] = "DATA "+i;
		}
		result = new String[1024];
		this.root = this;
		this.sindex = 0;
		
	}
	
	Board(Board root, int index){
		this.root = root;
		this.data = root.data;
		this.result = new String[256];
		this.sindex = 256 * index;
	}
	
	Board subBoard(int index){
		return new Board(this, index);
	}	
	void setNewValue(int index, String value){
		this.result[index] = value;
	}
	void commitNewValues(){
		if(sindex>=0){
			for(int i=sindex ; i < this.result.length ; i++, sindex++){
				root.result[i] = this.result[sindex];
			}
		}
	}
	String getValue(int index){
		return this.data[sindex+index];
	}
	
	int size(){
		return this.result.length;
	}
	
	void waitForConvergence(){
		
	}
}

class CellularAutomata{

	private final Logger LOG = LoggerFactory.getLogger(CellularAutomata.class);

	private final Board mainBoard;
	private final CyclicBarrier barrier;
	private final Worker[] workers;
	
	public CellularAutomata(Board board) {
		final int divide = 2;
		this.mainBoard = board;
		this.barrier = new CyclicBarrier(divide, new Runnable(){
			@Override
			public void run() {
				mainBoard.commitNewValues();
				LOG.debug("barrier action - commit new values");
			}
		});
		this.workers = new Worker[divide];
		for(int i=0; i < workers.length; i++){
			this.workers[i] = new Worker(mainBoard.subBoard(i));
		}
	}
	
	public void start(){
		for(int i=0; i< workers.length ;i++){
			new Thread(workers[i]).start();
		}
		mainBoard.waitForConvergence();
	}
	
	class Worker implements Runnable{
	
    	private final Board board;
    	
    	public Worker(Board board){	this.board = board;	}
    
    	@Override
    	public void run() {
    		LOG.debug("computing board : {}", board.result.length);
    		for(int i = 0 ; i < board.size() ; i++){
    			board.setNewValue(i , computeValue(i));
    		}
    		LOG.debug("finished compute board : {}", board.result.length);
    
    		try {
    			LOG.debug("barrier await......");
				barrier.await();
				LOG.debug("finished barrier awaiting");
			} catch (InterruptedException e) {
				return;
			} catch (BrokenBarrierException e) {
				return;
			}
    	}
    	
    	private  String computeValue(int i){
    		return String.valueOf("i th"+board.getValue(i));
    	}
	}
	
}


