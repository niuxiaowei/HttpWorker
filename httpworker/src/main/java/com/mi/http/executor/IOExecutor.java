package com.mi.http.executor;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 *
 * @author niuxiaowei
 * @date 2022/2/13.
 */
public class IOExecutor {

    private static RejectedExecutionHandler sRejectedHandler = new RejectedExecutionHandler() {

        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
            // Some bad situation occurred: sPool is now full, so we need queue subsequent Runnable.
            // But 'put(r)' in executor.getQueue().put(r) might be locked and take too many
            // delayTime, we need
            // post the 'queue' action into a single thread FIFO queue first, and then dispatched to
            // multiple thread pool

            Log.e("http-IOExecutor", "err ThreadPool is now full, need further dispatch ... " + r);
        }
    };

    private final static int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static final int MAX_THREADS = CPU_CORES * 3;

    public final static ExecutorService IO_EXECUTOR = new ThreadPoolExecutor(CPU_CORES,
            Math.max(MAX_THREADS, 20), 15L,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(CPU_CORES),
            new NameThreadFactory("generic-io-pool-"), sRejectedHandler);

    private IOExecutor() {
    }


    private static class NameThreadFactory implements ThreadFactory {
        private int count;
        private String threadNamePrefix;

        public NameThreadFactory(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            count++;
            Thread thr = new Thread(r, threadNamePrefix + count);
            thr.setDaemon(false);
            thr.setPriority((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2);
            return thr;
        }
    }
}
