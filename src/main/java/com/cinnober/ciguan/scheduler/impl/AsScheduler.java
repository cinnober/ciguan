/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Cinnober Financial Technology AB (cinnober.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cinnober.ciguan.scheduler.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.impl.AsComponent;
import com.cinnober.ciguan.scheduler.AsScheduledTaskHandleIf;
import com.cinnober.ciguan.scheduler.AsScheduledTaskIf;
import com.cinnober.ciguan.scheduler.AsSchedulerIf;

/**
 *
 * Standard application server task scheduler implementation
 * 
 * Implementation notes:
 * Every task which is executed spawns a new thread and executes the real task in that thread,
 * to avoid blocking the executor thread if the job takes a long time or gets stuck for some reason.
 * The scheduler itself uses a single thread worker, which should be sufficient for almost all situations.
 * If you need something different, extend this class and override the createScheduledExecutorService method.
 * 
 */
public class AsScheduler extends AsComponent implements AsSchedulerIf {

    protected ScheduledExecutorService mExecutor;
    
    public AsScheduler() {
        mExecutor = createScheduledExecutorService();
    }
    
    /**
     * Factory method for the scheduled execuror service. Override this if you need to use
     * something other than a single-threaded executor.
     * @return a suitable scheduled executor service.
     */
    protected ScheduledExecutorService createScheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor(new SchedulerThreadFactory());
    }
    
    @Override
    public AsScheduledTaskHandleIf schedule(AsScheduledTaskIf pTask) {
        if (pTask.getIntervalMs() <= 0) {
            // Single execution
            ScheduledFuture<?> tFuture = mExecutor.schedule(
                new ThreadTask(pTask), pTask.getDelayMs(), TimeUnit.MILLISECONDS);
            return new AsScheduledTaskHandle(tFuture);
        }
        // Repeating execution
        ScheduledFuture<?> tFuture = mExecutor.scheduleAtFixedRate(
            new ThreadTask(pTask), pTask.getDelayMs(), pTask.getIntervalMs(), TimeUnit.MILLISECONDS);
        return new AsScheduledTaskHandle(tFuture);
    }

    /* 
     * Make sure to shut down the executor service (at once)
     */
    @Override
    public void stopComponent() throws AsInitializationException {
        mExecutor.shutdownNow();
    }
    
    /**
     * Class which creates the scheduler threads. Needed only to get more readable thread names.
     */
    protected static class SchedulerThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable pRunnable) {
            return new Thread(pRunnable, "TaskScheduler");
        }
    }
    
    /**
     * Task wrapper which runs the contained task in a separate thread
     */
    protected static class ThreadTask implements Runnable {

        private final AsScheduledTaskIf mTask;
        
        public ThreadTask(AsScheduledTaskIf pTask) {
            mTask = pTask;
        }

        @Override
        public void run() {
            Thread tThread = new Thread(mTask, mTask.getName());
            tThread.start();
        }
        
    }
    
}
