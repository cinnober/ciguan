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
package com.cinnober.ciguan.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.cinnober.ciguan.AsBdxHandlerIf;
import com.cinnober.ciguan.AsBdxListenerIf;
import com.cinnober.ciguan.AsBdxProcessorIf;
import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.data.AsClockPulse;
import com.cinnober.ciguan.plugin.AsServicePluginMessageHandlerIf;
import com.cinnober.ciguan.xml.impl.AsDefBdxProcessor;

/**
 * Implementation of the application server broadcast handler. This class receives broadcast objects,
 * and notifying listeners about new messages.
 * 
 * FIXME: At the moment, only one broadcast reception queue is present, but in order to distribute the work
 * across multiple processors better, we need to introduce multiple dequeuers.
 * 
 */
public class AsBdxHandlerImpl extends AsComponent
    implements AsBdxHandlerIf, AsServicePluginMessageHandlerIf {

    /**
     * Catch-all listeners
     */
    protected final Set<AsBdxListenerIf> mListeners = new LinkedHashSet<AsBdxListenerIf>();
    
    /**
     * Mapping of incoming message type to dedicated listeners
     */
    protected final Map<Class<?>, Set<AsBdxListenerIf>> mListenersByClass =
        new HashMap<Class<?>, Set<AsBdxListenerIf>>();
    
    protected final Set<Class<?>> mBdxClasses = new HashSet<Class<?>>();
    protected BlockingQueue<Object> mBdxQueue = new LinkedBlockingDeque<Object>();

    protected BdxDispatcher mBdxDispatcher;
    protected ClockPulseGenerator mClockPulseGenerator;
    
    @Override
    public void startComponent() throws AsInitializationException {
        super.startComponent();
        
        // Create and start the broadcast dispatcher
        Thread tBdxDispatcher = new Thread(mBdxDispatcher = new BdxDispatcher());
        tBdxDispatcher.setName(BdxDispatcher.class.getSimpleName());
        tBdxDispatcher.start();
    }

    @Override
    public void allComponentsStarted() {
        
        // Register BDX class filters
        registerBdxClasses();
        
        // Register BDX processors
        registerBdxProcessors();
        
    }
    
    @Override
    public void stopComponent() throws AsInitializationException {
        super.stopComponent();
        
        // Stop the internal threads
        mBdxDispatcher.stop();
        mClockPulseGenerator.stop();
        
        // Submit one last dummy object to make sure the dispatcher wakes up and exits
        broadcast(new Object());
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            // Ignore
        }
    }

    @Override
    public void addBdxListener(AsBdxListenerIf pListener) {
        synchronized (mListeners) {
            if (!mListeners.contains(pListener)) {
                mListeners.add(pListener);
            }
        }
    }

    @Override
    public void addBdxListener(AsBdxListenerIf pListener, Class<?>... pClasses) {
        synchronized (mListenersByClass) {
            for (Class<?> tClass : pClasses) {
                Set<AsBdxListenerIf> tListeners = mListenersByClass.get(tClass);
                if (tListeners == null) {
                    tListeners = new LinkedHashSet<AsBdxListenerIf>();
                    mListenersByClass.put(tClass, tListeners);
                }
                tListeners.add(pListener);
            }
        }
    }
    
    @Override
    public void removeBdxListener(AsBdxListenerIf pListener) {
        synchronized (mListenersByClass) {
            Iterator<Map.Entry<Class<?>, Set<AsBdxListenerIf>>> tIterator = mListenersByClass.entrySet().iterator();
            while (tIterator.hasNext()) {
                Map.Entry<Class<?>, Set<AsBdxListenerIf>> tEntry = tIterator.next();
                tEntry.getValue().remove(pListener);
                if (tEntry.getValue().isEmpty()) {
                    tIterator.remove();
                }
            }
        }
        synchronized (mListeners) {
            mListeners.remove(pListener);
        }
    }
    
    /**
     * Add a broadcast class to receive
     * 
     * TODO: This mechanism is not used yet, but it is added as a means to prevent the broadcast reception
     * from having to call the listeners for *every* broadcast. Only classes registered here should yield a
     * call to notifyListeners(). 
     * 
     * @param pBdxClass
     */
    protected void addBdxClass(Class<?> pBdxClass) {
        mBdxClasses.add(pBdxClass);
    }
    
    @Override
    public void broadcast(Object pMessage) {
        enqueue(pMessage);
    }
    
    @Override
    public int getQueueLength() {
        return mBdxQueue.size();
    }
    
    /**
     * Enqueue an incoming broadcast
     * @param pMessage
     */
    protected void enqueue(Object pMessage) {
        mBdxQueue.add(pMessage);
    }

    /**
     * Dequeue the oldest message
     * @return the oldest message
     * @throws InterruptedException 
     */
    protected Object dequeue() throws InterruptedException {
        return mBdxQueue.take();
    }
    
    /**
     * Notify all reference data listeners about the arrival of a message
     * @param pMessage
     */
    protected void notifyListeners(Object pMessage) {
        Object tMessage = pMessage instanceof AsHasBdxValue ? ((AsHasBdxValue) pMessage).getBdxValue() : pMessage;
        synchronized (mListenersByClass) {
            Set<AsBdxListenerIf> tListeners = mListenersByClass.get(tMessage.getClass());
            if (tListeners != null) {
                for (AsBdxListenerIf tListener : tListeners) {
                    try {
                        tListener.onBroadcast(tMessage);
                    }
                    catch (Throwable e) {
                        AsLoggerIf.Singleton.get().logThrowable("Exception while dispatching broadcast to " +
                            tListener.getClass().getSimpleName(), e);                    
                    }
                }
            }
        }
        synchronized (mListeners) {
            for (AsBdxListenerIf tListener : mListeners) {
                try {
                    tListener.onBroadcast(tMessage);
                }
                catch (Throwable e) {
                    AsLoggerIf.Singleton.get().logThrowable("Exception while dispatching broadcast to " +
                        tListener.getClass().getSimpleName(), e);                    
                }
            }
        }
    }

    @Override
    public void synchronizeExternalData() {
        // Start the BDX processors
        startBdxProcessors();
        // Create and start the clock pulse generator
        Thread tClockPulseGenerator = new Thread(mClockPulseGenerator = new ClockPulseGenerator());
        tClockPulseGenerator.setName(ClockPulseGenerator.class.getSimpleName());
        tClockPulseGenerator.start();
    }

    /**
     * Start the broadcast processors
     */
    protected void startBdxProcessors() {
        for (AsBdxListenerIf tListener : mListeners) {
            if (tListener instanceof AsBdxProcessorIf) {
                ((AsBdxProcessorIf) tListener).start();
            }
        }
    }
    
    /**
     * Register BDX class filters
     */
    protected void registerBdxClasses() {
        for (String tBdxClass : As.getTransportConfiguration().getBdxClasses()) {
            try {
                Class<?> tClass = Class.forName(tBdxClass);
                addBdxClass(tClass);
            }
            catch (ClassNotFoundException e) {
                // Should not happen since class names are validated when parsing
            }
        }
    }
    
    protected void registerBdxProcessors() {
        // TODO: map listeners on class they are interested in
        for (AsDefBdxProcessor tBdxProcessor : As.getTransportConfiguration().getBdxProcessors()) {
            try {
                Class<?> tClass = Class.forName(tBdxProcessor.getClassName());
                AsBdxProcessorIf tHandler = (AsBdxProcessorIf) tClass.newInstance();
                addBdxListener(tHandler);
                tHandler.setParameters(tBdxProcessor.getParameters());
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to register BDX processor", e);
            }
        }
    }
    
    /**
     * Simple dequeue thread
     * TODO: Decide when to exit the loop due to too many exceptions
     */
    protected class BdxDispatcher implements Runnable {
        
        protected boolean mStopOrdered;
        
        @Override
        public void run() {
            AsLoggerIf.Singleton.get().log("Starting broadcast dispatcher thread");
            while (!mStopOrdered) {
                try {
                    Object tBdx = dequeue();
                    if (tBdx != null) {
                        notifyListeners(tBdx);
                    }
                }
                catch (Throwable e) {
                    AsLoggerIf.Singleton.get().logThrowable("Exception during broadcast dispatching", e);
                }
            }
        }

        public void stop() {
            mStopOrdered = true;
        }
    }
    
    /**
     * Clock pulse generator thread.
     * Clock pulse interval is one second.
     */
    protected class ClockPulseGenerator implements Runnable {
        
        protected final AsClockPulse mClockPulse = new AsClockPulse();
        protected Calendar mCalendar = Calendar.getInstance();
        protected int mCurrentSecond;
        protected boolean mStopOrdered;
        
        @Override
        public void run() {
            AsLoggerIf.Singleton.get().log("Starting clock pulse generator thread");
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            mCurrentSecond = mCalendar.get(Calendar.SECOND);
            while (!mStopOrdered) {
                try {
                    while (!mStopOrdered && mCalendar.get(Calendar.SECOND) == mCurrentSecond) {
                        Thread.sleep(10);
                        mCalendar.setTimeInMillis(System.currentTimeMillis());
                    }
                    mCurrentSecond = mCalendar.get(Calendar.SECOND);
                    mClockPulse.setTimestamp(mCalendar.getTimeInMillis() + "");
                    enqueue(mClockPulse);
                }
                catch (InterruptedException e) {
                    // No action
                }
            }
        }

        public void stop() {
            mStopOrdered = true;
        }
    }
    
}
