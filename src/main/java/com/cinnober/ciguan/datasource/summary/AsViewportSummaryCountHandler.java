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

import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.impl.CwfBusinessTypes;

/**
 *
 * Standard count handler
 *
 * @param <T> the type of object in the data source
 */
public class AsViewportSummaryCountHandler<T> extends AsViewportSummaryHandler<T> {

    /** The value. */
    protected long mValue;
    
    /**
     * Instantiates a new as viewport summary count handler.
     *
     * @param pValueGetter the value getter
     * @param pService the service
     */
    public AsViewportSummaryCountHandler(AsGetMethodIf<T> pValueGetter, AsDataSourceServiceIf pService) {
        super(pValueGetter, pService);
    }

    @Override
    public Object getValue() {
        return mValue;
    }

    @Override
    public HandlerType getHandlerType() {
        return HandlerType.count;
    }
    
    @Override
    public CwfBusinessTypeIf getBusinessType() {
        return CwfBusinessTypes.Long;
    }
 
    @Override
    public void handleViewportEvent(AsDataSourceEventIf<T> pEvent) {
        switch (pEvent.getType()) {
            case ADD:
                mValue += 1;
                break;
                
            case UPDATE:
                // No action, count is still the same
                break;
                
            case REMOVE:
                mValue -= 1;
                break;
                
            case CLEAR:
            case DESTROY:
                mValue = 0;
                break;
                
            case SNAPSHOT:
                mValue = pEvent.getSnapshot().size();
                break;
                
            default:
                break;
        }
    }
    
}
