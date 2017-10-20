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

import java.util.Collection;

import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsListIf;

/**
 * Class representing a model event.
 *
 * @param <T> the generic type
 */
public class AsDataSourceEvent<T> implements AsDataSourceEventIf<T> {
    
    /** The type. */
    private final Type mType;
    
    /** The index. */
    private final int mIndex;
    
    /** The value. */
    private final T mValue;
    
    /** The old value. */
    private final T mOldValue;
    
    /** The source. */
    private final AsListIf<T> mSource;
    
    /** The snapshot. */
    private Collection<T> mSnapshot;
     
    /**
     * Instantiates a new as data source event.
     *
     * @param pSource the source
     * @param pType the type
     * @param pIndex the index
     * @param pItem the item
     * @param pOldItem the old item
     */
    private AsDataSourceEvent(AsListIf<T> pSource, Type pType, int pIndex, T pItem, T pOldItem) {
        mSource = pSource;
        mType = pType;
        mIndex = pIndex;
        mValue = pItem;
        mOldValue = pOldItem;
    }

    /**
     * Instantiates a new as data source event.
     *
     * @param pSource the source
     * @param pSnapshot the snapshot
     */
    private AsDataSourceEvent(AsListIf<T> pSource, Collection<T> pSnapshot) {
        this(pSource, Type.SNAPSHOT, 0, null, null);
        mSnapshot = pSnapshot;
    }
    
    /**
     * Instantiates a new as data source event.
     *
     * @param pSource the source
     */
    private AsDataSourceEvent(AsListIf<T> pSource) {
        this(pSource, Type.CLEAR, 0, null, null);
    }
    
    @Override
    public T getNewValue() {
        return mValue;
    }
    
    @Override
    public Type getType() {
        return mType;
    }
    
    @Override
    public int getIndex() {
        return mIndex;
    }
    
    @Override
    public T getOldValue() {
        return mOldValue;
    }
    
    @Override
    public Collection<T> getSnapshot() {
        return mSnapshot;
    }

    /**
     * Static factory method for a list model event.
     *
     * @param <T> the generic type
     * @param pSource the source
     * @param pType the type
     * @param pIndex the index
     * @param pItem the item
     * @param pOldItem the old item
     * @return the as data source event
     */
    public static <T> AsDataSourceEvent<T> create(AsListIf<T> pSource, Type pType, int pIndex, T pItem, T pOldItem) {
        return new AsDataSourceEvent<T>(pSource, pType, pIndex, pItem, pOldItem);
    }

    /**
     * Static factory method for a snapshot list model event.
     *
     * @param <T> the generic type
     * @param pSource the source
     * @param pSnapshot the snapshot
     * @return the as data source event
     */
    public static <T> AsDataSourceEvent<T> createSnapshot(AsListIf<T> pSource, final Collection<T> pSnapshot) {
        return new AsDataSourceEvent<T>(pSource, pSnapshot);
    }
    
    /**
     * Static factory method for a list model event of type clear.
     *
     * @param <T> the generic type
     * @param pSource the source
     * @return the as data source event
     */
    public static <T> AsDataSourceEvent<T> createClear(AsListIf<T> pSource) {
        return new AsDataSourceEvent<T>(pSource);
    }
    
    /**
     * Static factory method for a list model event of type destroy.
     *
     * @param <T> the generic type
     * @param pSource the source
     * @return the as data source event
     */
    public static <T> AsDataSourceEvent<T> createDestroy(AsListIf<T> pSource) {
        return new AsDataSourceEvent<T>(pSource, Type.DESTROY, 0, null, null);
    }
    
    @Override
    public AsListIf<T> getSource() {
        return mSource;
    }
}
