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
package com.cinnober.ciguan.plugin.impl;

/**
 *
 * Base class for application server plug-ins
 * 
 * By default, all plug-ins are run as threads. However, if your plug-in is only
 * intended to carry out a one time job, there is no need to implement anything
 * in the {@link Thread#run()} method.
 * 
 * If your plug-in is doing repetitive work in the {@link Thread#run()} method,
 * you should always surround the entire code by a
 * {@code try - catch (Throwable e)} block in order to avoid letting exceptions
 * bubble up to the uncaught exception handler. Before every iteration, you
 * should also check that {@link #isStopOrdered()} returns {@code false} before starting the
 * work. When the application server is shut down, all service plug-ins will be
 * stopped, which causes an interrupt to be sent to every running plug-in
 * thread. When you receive this interrupt you should abort the work and exit
 * the {@link Thread#run()} method as soon as possible.
 * 
 */
public abstract class AsPlugin extends AsPluginBase {

    /** The thread. */
    protected Thread mThread;

    /** Flag to mark if a stop was ordered. */
    protected boolean mStopOrdered;

    /**
     * Default launch, create a thread named after the plugin ID and start it.
     */
    @Override
    public final void launch() {
        mThread = new Thread(this);
        mThread.setName(getPluginId());
        mThread.start();
    }

    @Override
    public void stop() {
        super.stop();
        mStopOrdered = true;
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
        }
    }

    /**
     * Check whether the plugin has been ordered to stop.
     *
     * @return {@code true} if stop has been ordered, otherwise {@code false}
     */
    protected boolean isStopOrdered() {
        return mStopOrdered;
    }

}
