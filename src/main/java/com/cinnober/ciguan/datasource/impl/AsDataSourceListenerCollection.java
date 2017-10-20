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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceListenerIf;

/**
 * Class holding data source listeners.
 *
 * @param <T> The type that the data source contains
 */
@SuppressWarnings("serial")
public class AsDataSourceListenerCollection<T> extends LinkedHashSet<AsDataSourceListenerIf<T>> {

    protected final List<AsDataSourceListenerIf<T>> mPendingAdds = new ArrayList<AsDataSourceListenerIf<T>>();
    protected final List<AsDataSourceListenerIf<T>> mPendingRemoves = new ArrayList<AsDataSourceListenerIf<T>>();
    protected boolean mNotifying;
    
    /**
     * Notify listeners.
     *
     * @param pEvent the event
     */
    public void notifyListeners(AsDataSourceEventIf<T> pEvent) {
        mNotifying = true;
        for (AsDataSourceListenerIf<T> tListener : this) {
            tListener.onDataSourceEvent(pEvent);
        }
        mNotifying = false;
        handlePendingChanges();
    }

    /**
     * Handle pending changes during the notification
     */
    protected void handlePendingChanges() {
        if (!mPendingRemoves.isEmpty()) {
            for (AsDataSourceListenerIf<T> tListener : mPendingRemoves) {
                remove(tListener);
            }
            mPendingRemoves.clear();
        }
        if (!mPendingAdds.isEmpty()) {
            for (AsDataSourceListenerIf<T> tListener : mPendingAdds) {
                add(tListener);
            }
            mPendingAdds.clear();
        }
    }
    
    @Override
    public boolean add(AsDataSourceListenerIf<T> pListener) {
        if (mNotifying) {
            mPendingAdds.add(pListener);
            return true;
        }
        return super.add(pListener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object pListener) {
        if (mNotifying) {
            mPendingRemoves.add((AsDataSourceListenerIf<T>) pListener);
            return true;
        }
        return super.remove(pListener);
    }
    
}
