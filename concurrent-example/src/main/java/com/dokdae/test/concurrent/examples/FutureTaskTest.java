package com.dokdae.test.concurrent.examples;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 
 * <b>Future Task 예제</b><br/>
 * 퓨처 테스크는 시간이 걸리는 작업에 대해서 미리 실행 시키고 다른 일을 진행처리 후 
 * 완료될 쯤에 결과를 기다리는 데 사용된다. 
 * 
 * 활용예) 
 * 레거시 연동 시 다른 외부 서버와의 인터페이스를 통해 데이터를 가져와야 하는 경우가 많다.
 * 인터페이스 연동이 트랜잭션 처리에 독립적이라면, 즉 트랜잭션 처리에 별다른 영향을 끼치지 않는다면,
 * 미리 실행시켜놓고 마지막에 확인하여 결과를 조합한다.
 * 
 * 이런 경우, 트랜잭션 처리 시간이 줄어들어 쓰루풋이 향상된다.
 * 
 * com.dokdae.test.concurrent.examples.FutureTaskTest - Creation date: 2014. 11. 16. <br/>
 * @author jinylee
 *
 */
public class FutureTaskTest {
	
	private final FutureTask<IFResult> task = 
			new FutureTask<IFResult>(new Callable<IFResult>(){

				@Override
				public IFResult call() throws Exception {
					return connectAndGetResult();
				}
			});
	
	private final Thread thread = new Thread(task);
	
	public void start(){	thread.start();		}
	
	public IFResult get() throws Exception{
		try{
			return task.get();
		}catch(InterruptedException e){
			return get();
		}catch(ExecutionException e){
			throw new Exception(e);
		}catch(CancellationException e){
			throw new Exception(e);
		}
	}
	
	private IFResult connectAndGetResult(){
		IFResult result = new IFResult();
		try{	Thread.sleep(1000);	}catch(InterruptedException ignored){}
		result.request="key";
		result.response="value";
		return result;
	}
	
	public static class IFResult{
		String request;
		String response;
	}

}
