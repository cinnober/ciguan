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

import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;

/**
 *
 * Standard sum handler
 * 
 * @param <T> the type of object in the data source
 */
public class AsViewportSummarySumHandler<T> extends AsViewportSummaryHandler<T> {

    /** The value. */
    protected long mValue;
    
    /** The count. */
    protected long mCount;
    
    /**
     * Instantiates a new as viewport summary sum handler.
     *
     * @param pValueGetter the value getter
     * @param pService the service
     */
    public AsViewportSummarySumHandler(AsGetMethodIf<T> pValueGetter, AsDataSourceServiceIf pService) {
        super(pValueGetter, pService);
    }

    @Override
    public Object getValue() {
        return mCount == 0 ? null : mValue;
    }

    @Override
    public HandlerType getHandlerType() {
        return HandlerType.sum;
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
                mValue = 0;
                mCount = 0;
                break;
                
            case SNAPSHOT:
                mValue = 0;
                mCount = 0;
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
            mValue += ((Number) tValue).longValue();
            mCount += 1;
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
            mValue -= ((Number) tValue).longValue();
            mCount -= 1;
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

}
