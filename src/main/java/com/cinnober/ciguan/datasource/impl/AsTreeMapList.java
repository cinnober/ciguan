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
import java.util.Collection;

import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.AsSortIf;
import com.cinnober.ciguan.datasource.AsDataSourceEventIf.Type;

/**
 * Tree map implementation of a server side list, typically used where a large number of items are stored.
 *
 * @param <T> The type of the contained object
 */
public abstract class AsTreeMapList<T> extends AsList<T> {

    /** The m items. */
    private IndexedTreeMap<String, T> mItems = new IndexedTreeMap<String, T>();
    
    /**
     * Instantiates a new as tree map list.
     *
     * @param pModelId the model id
     * @param pSource the source
     * @param pFilter the filter
     * @param pSort the sort
     * @param pClass the class
     */
    public AsTreeMapList(String pModelId, AsListIf<T> pSource,
            AsFilterIf<T> pFilter, AsSortIf<T> pSort, Class<T> pClass) {
        super(pModelId, pSource, pFilter, pSort, pClass);
    }

    @Override
    public void add(T pItem) {
        add(pItem, true);
    }

    /**
     * Adds the item and notifies the listeners if {@code pNotify=true}.
     *
     * @param pItem the item
     * @param pNotify the notify flag
     */
    protected void add(T pItem, boolean pNotify) {
        synchronized (mMutex) {
            if (include(pItem)) {
                String tId = getKey(pItem);
                T tOldValue = mItems.put(tId, pItem);
                if (pNotify) {
                    int tIndex = indexOf(tId);
                    if (tOldValue == null) {
                        notifyListeners(AsDataSourceEvent.create(this, Type.ADD, tIndex, pItem, null));
                    }
                    else {
                        notifyListeners(AsDataSourceEvent.create(this, Type.UPDATE, tIndex, pItem, tOldValue));
                    }
                }
            }
        }
    }

    @Override
    public T get(int pIndex) {
        if (pIndex < 0 || pIndex >= mItems.size()) {
            return null;
        }
        return mItems.get(pIndex);
    }

    @Override
    public T get(String pId) {
        return mItems.get(pId);
    }

    @Override
    public int indexOf(T pItem) {
        return mItems.indexOfKey(getKey(pItem));
    }
    
    @Override
    public int indexOf(String pKey) {
        return mItems.indexOfKey(pKey);
    }

    /**
     * {@inheritDoc}
     * Notifies the listeners as well.
     */
    @Override
    public void remove(T pItem) {
        remove(pItem, true);
    }

    /**
     * Removes the item and notifies the listeners if {@code pNotify=true}.
     *
     * @param pItem the item
     * @param pNotify the notify
     */
    protected void remove(T pItem, boolean pNotify) {
        synchronized (mMutex) {
            String tId = getKey(pItem);
            int tIndex = mItems.indexOfKey(tId);
            if (tIndex >= 0) {
                T tOldValue = mItems.remove(tId);
                if (pNotify) {
                    notifyListeners(AsDataSourceEvent.create(this, Type.REMOVE, tIndex, pItem, tOldValue));
                }
            }
        }
    }

    @Override
    public int size() {
        return mItems.size();
    }

    @Override
    public void update(T pNewValue) {
        update(pNewValue, true);
    }
    
    /**
     * Updates the value and notifies the listeners if {@code pNotify=true}.
     *
     * @param pNewValue the new value
     * @param pNotify the notify
     */
    protected void update(T pNewValue, boolean pNotify) {
        synchronized (mMutex) {
            if (include(pNewValue)) {
                String tId = getKey(pNewValue);
                T tOldValue = mItems.put(tId, pNewValue);
                if (pNotify) {
                    int tIndex = indexOf(tId);
                    if (tOldValue == null) {
                        notifyListeners(AsDataSourceEvent.create(this, Type.ADD, tIndex, pNewValue, null));
                    }
                    else {
                        notifyListeners(AsDataSourceEvent.create(this, Type.UPDATE, tIndex, pNewValue, tOldValue));
                    }
                }
            }
            else {
                remove(pNewValue, pNotify);
            }
        }
    }
    
    @Override
    public void snapshot(Collection<T> pSnapshot) {
        for (T tItem : pSnapshot) {
            if (include(tItem)) {
                mItems.put(getKey(tItem), tItem);
            }
        }
        notifyListeners(AsDataSourceEvent.createSnapshot(this, pSnapshot));
    }

    @Override
    public Collection<T> values() {
        synchronized (mMutex) {
            return new ArrayList<T>(mItems.values());
        }
    }

    @Override
    public void clear() {
        synchronized (mMutex) {
            mItems.clear();
            notifyListeners(AsDataSourceEvent.createClear(this));
        }
    }

    @Override
    public void destroy() {
        synchronized (mMutex) {
            super.destroy();
            mItems.clear();
        }
    }
    
}
