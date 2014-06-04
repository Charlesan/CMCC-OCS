package com.ocs.cf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CF工作线程池实现类
 * @author Wang Chao
 *
 */
public class CFWorkerThreadPool {

	private ExecutorService executor;
	private static final int POOL_MAX_THREAD_NUM = 1;
	
	public void initPool() {
		this.executor = Executors.newFixedThreadPool(POOL_MAX_THREAD_NUM);
	}
	
	public void shutdownPool() {
		this.executor.shutdown();
        while (!this.executor.isTerminated()) {
        }
	}
	
	public ExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}
}
