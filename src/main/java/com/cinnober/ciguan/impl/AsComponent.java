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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cinnober.ciguan.AsComponentIf;
import com.cinnober.ciguan.AsInitializationException;

/**
 *
 * Base class for application server components
 *
 */
public abstract class AsComponent implements AsComponentIf {

    private static final List<AsComponent> cAllComponents = new ArrayList<AsComponent>();

    protected long mStartTime;
    protected long mAllStartTime;
    protected long mSynchExtTime;

    public AsComponent() {
        cAllComponents.add(this);
    }

    public void doStartComponent() throws AsInitializationException {
        mStartTime = System.currentTimeMillis();
        startComponent();
        mStartTime = System.currentTimeMillis() - mStartTime;
    }

    public final void doAllComponentsStarted() throws AsInitializationException {
        mAllStartTime = System.currentTimeMillis();
        allComponentsStarted();
        mAllStartTime = System.currentTimeMillis() - mAllStartTime;
    }

    public void doSynchronizeExternalData() {
        mSynchExtTime = System.currentTimeMillis();
        synchronizeExternalData();
        mSynchExtTime = System.currentTimeMillis() - mSynchExtTime;
        //        As.getBdxHandler().broadcast(new MibAsComponent(this, mStartTime, mAllStartTime, mSynchExtTime));
    }

    public void doStopComponent() throws AsInitializationException {
        stopComponent();
    }

    public final void doAllComponentsStopped() throws AsInitializationException {
        allComponentsStopped();
    }

    public static Collection<AsComponent> getAllComponents() {
        return cAllComponents;
    }

    @Override
    public void allComponentsStarted() throws AsInitializationException {
        // No action by default
    }

    @Override
    public void startComponent() throws AsInitializationException {
        // No action by default
    }

    @Override
    public void synchronizeExternalData() {
        // No action by default
    }

    @Override
    public void reloadConfiguration() {
        // No action by default
    }

    @Override
    public void stopComponent() throws AsInitializationException {
        // No action by default
    }

    @Override
    public void allComponentsStopped() throws AsInitializationException {
        // No action by default
    }

}
