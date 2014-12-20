package com.dokdae.test.concurrent.examples;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <b>CompleteFutureService에 대한 예제</b><br/>
 * CompleteService는 처리가 완료된 FutureTask의 결과를 BlockingQueue에 저장하기 때문에 
 * 처리된 결과의 값을 polling할 필요없이 가져와 사용하면 되기 때문에 좀 더 performance 좋아질 수 있다 
 * com.dokdae.test.concurrent.examples.RenderUsingCompleteFutureServiceTest - Creation date: 2014. 11.25. <br/>
 * 
 * @author jinylee
 * 
 */
public class RenderUsingCompleteFutureServiceTest {

	static final Logger LOG = LoggerFactory.getLogger(RenderUsingCompleteFutureServiceTest.class);

	public static void main(String[] args) throws ExecutionException {

		final ExecutorService executor = Executors.newFixedThreadPool(10);
		Renderer render = new Renderer(executor);
		render.renderPage(null);
		executor.shutdown();
	}

	public static class Renderer {
		// ExecutorService 생성
		private final ExecutorService executor;

		Renderer(ExecutorService executor) {
			this.executor = executor;
		}

		void renderPage(CharSequence source) throws ExecutionException {

			final List<String> imageUrls = scanForImageUrls(source);
			CompletionService<ByteBuffer> completeService = new ExecutorCompletionService<ByteBuffer>(executor);

			for (final String imageUrl : imageUrls) {
				completeService.submit(new Callable<ByteBuffer>() {

					@Override
					public ByteBuffer call() throws Exception {
						LOG.debug("Image loading....");
						ByteBuffer ret = ByteBuffer.wrap(imageUrl.getBytes());
						try {
							Thread.sleep(10);
						} catch (Exception e) {
						}
						LOG.debug("finished Image loading....");
						return ret;
					}
				});
			}
			renderText(source);
			try {
				for (int t = 0, n = imageUrls.size(); t < n; t++) {
					Future<ByteBuffer> f = completeService.take();
					ByteBuffer data = f.get();
					renderImage(data);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (CancellationException e) {
				throw e;
			} catch (ExecutionException e) {
				throw e;
			}
		}

		private void renderImage(ByteBuffer data) {
			LOG.debug("Image rendering....");
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
			LOG.debug("finished Image rendering....");
		}

		private void renderText(CharSequence source) {
			LOG.debug("Text rendering....");
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
			LOG.debug("finished Text rendering....");
		}

		List<String> scanForImageUrls(CharSequence source) {
			return Arrays.asList("http://aaa.bbb.com/img/a.gif", "http://aaa.bbb.com/img/b.gif",
					"http://aaa.bbb.com/img/b.gif", "http://aaa.bbb.com/img/b.gif", "http://aaa.bbb.com/img/b.gif",
					"http://aaa.bbb.com/img/b.gif", "http://aaa.bbb.com/img/b.gif", "http://aaa.bbb.com/img/b.gif",
					"http://aaa.bbb.com/img/b.gif", "http://aaa.bbb.com/img/b.gif", "http://aaa.bbb.com/img/b.gif",
					"http://aaa.bbb.com/img/b.gif", "http://aaa.bbb.com/img/b.gif", "http://aaa.bbb.com/img/b.gif");
		}

	}
}
