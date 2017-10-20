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
package com.cinnober.ciguan.datasource.summary;

import java.util.NoSuchElementException;
import java.util.TreeMap;

import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;

/**
 *
 * Standard max handler
 * 
 * @param <T> the type of object in the data source
 */
public class AsViewportSummaryMaxHandler<T> extends AsViewportSummaryHandler<T> {

    /** The value. */
    protected Long mValue;
    
    /** The distinct values. */
    protected TreeMap<Long, Integer> mDistinctValues = new TreeMap<Long, Integer>();
    
    /**
     * Instantiates a new as viewport summary max handler.
     *
     * @param pValueGetter the value getter
     * @param pService the service
     */
    public AsViewportSummaryMaxHandler(AsGetMethodIf<T> pValueGetter, AsDataSourceServiceIf pService) {
        super(pValueGetter, pService);
    }

    @Override
    public Object getValue() {
        return mValue;
    }

    @Override
    public HandlerType getHandlerType() {
        return HandlerType.max;
    }
     
    @Override
    public void handleViewportEvent(AsDataSourceEventIf<T> pEvent) {        
        
        switch (pEvent.getType()) {
            case ADD:
                add(pEvent.getNewValue());
                break;
                
            case UPDATE:
                update(pEvent.getNewValue(), pEvent.getOldValue());
                break;
                
            case REMOVE:
                remove(pEvent.getOldValue());
                break;
                
            case CLEAR:
            case DESTROY:
                mValue = null;
                break;
                
            case SNAPSHOT:
                mValue = null;
                for (T tItem : pEvent.getSnapshot()) {
                    add(tItem);
                }
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Adds the item.
     *
     * @param pItem the item
     */
    private void add(T pItem) {
        Object tValue = mValueGetter.getObject(pItem);
        if (tValue instanceof Number) {
            addDistinctValue(((Number) tValue).longValue());
            try {
                mValue = mDistinctValues.lastKey();
            }
            catch (NoSuchElementException e) {
                mValue = null;
            }
        }
    }

    /**
     * Removes the item.
     *
     * @param pItem the item
     */
    private void remove(T pItem) {
        Object tValue = mValueGetter.getObject(pItem);
        if (tValue instanceof Number) {
            removeDistinctValue(((Number) tValue).longValue());
            try {
                mValue = mDistinctValues.lastKey();
            }
            catch (NoSuchElementException e) {
                mValue = null;
            }
        }
    }
    
    /**
     * Updates the item.
     *
     * @param pNewItem the new item
     * @param pOldItem the old item
     */
    private void update(T pNewItem, T pOldItem) {
        remove(pOldItem);
        add(pNewItem);
    }

    /**
     * Adds the distinct value.
     *
     * @param pLongValue the long value
     */
    private void addDistinctValue(long pLongValue) {
        Integer tInteger = mDistinctValues.get(pLongValue);
        mDistinctValues.put(pLongValue, tInteger == null ? 1 : tInteger + 1);
    }
    
    /**
     * Removes the distinct value.
     *
     * @param pLongValue the long value
     */
    private void removeDistinctValue(long pLongValue) {
        Integer tInteger = mDistinctValues.get(pLongValue);
        if (tInteger != null && tInteger == 1) {
            mDistinctValues.remove(pLongValue);
        }
        else {
            mDistinctValues.put(pLongValue, tInteger - 1);
        }
    }
    
}
