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
package com.cinnober.ciguan.datasource.impl;

import com.cinnober.ciguan.datasource.AsDataSourceListenerIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.AsListMetaDataIf;
import com.cinnober.ciguan.datasource.AsSortIf;

/**
 * Base class for the server side list implementations.
 *
 * @param <T> The type of the contained object
 */
public abstract class AsList<T> extends AsDataSource<T> implements AsListIf<T> {

    /** The mutex. */
    protected Object mMutex = new Object();

    /** The list meta data. */
    private AsListMetaDataIf mListMetaData;

    /**
     * Instantiates a new as list.
     *
     * @param pModelId the model id
     * @param pSource the source
     * @param pFilter the filter
     * @param pSort the sort
     * @param pClass the class
     */
    public AsList(String pModelId, AsListIf<T> pSource, AsFilterIf<T> pFilter, AsSortIf<T> pSort, Class<T> pClass) {
        super(pModelId, pSource, pFilter, pSort, pClass);
        mListMetaData = pSource == null ? null : pSource.getListMetaData();
    }

    @Override
    public void addListener(AsDataSourceListenerIf<T> pListener) {
        synchronized (mMutex) {
            pListener.onDataSourceEvent(AsDataSourceEvent.createSnapshot(this, values()));
        }
        super.addListener(pListener);
    }

    @Override
    public void setListMetaData(AsListMetaDataIf pListMetaData) {
        mListMetaData = pListMetaData;
    }

    @Override
    public AsListMetaDataIf getListMetaData() {
        return mListMetaData;
    }

    /**
     * {@inheritDoc}
     * Notifies and clears the listeners.
     * Updates the management information base.
     */
    @Override
    public void destroy() {
        mListeners.notifyListeners(AsDataSourceEvent.createDestroy(this));
        mListeners.clear();
        mOwner = null;
        //        updateMib();
    }

}
