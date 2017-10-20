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
package com.cinnober.ciguan.datasource;

import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.datasource.summary.AsViewportSummaryAvgHandler;
import com.cinnober.ciguan.datasource.summary.AsViewportSummaryCountHandler;
import com.cinnober.ciguan.datasource.summary.AsViewportSummaryMaxHandler;
import com.cinnober.ciguan.datasource.summary.AsViewportSummaryMinHandler;
import com.cinnober.ciguan.datasource.summary.AsViewportSummarySumHandler;

/**
 * Interface defining viewport summary functionality.
 *
 * @param <T> type type of object in the underlying data source
 */
public interface AsViewportSummaryHandlerIf<T> {

    /**
     * Enumeration for standard handlers
     * NOTE: The custom type is always used in conjunction with a specific implementation class.
     */
    enum HandlerType {
        
        /** The avg. */
        avg(AsViewportSummaryAvgHandler.class),
        
        /** The count. */
        count(AsViewportSummaryCountHandler.class),
        
        /** The custom. */
        custom(null),
        
        /** The max. */
        max(AsViewportSummaryMaxHandler.class),
        
        /** The min. */
        min(AsViewportSummaryMinHandler.class),
        
        /** The sum. */
        sum(AsViewportSummarySumHandler.class);
        
        /** The m handler type. */
        Class<?> mHandlerType;
        
        /**
         * Instantiates a new handler type.
         *
         * @param pHandlerType the handler type
         */
        HandlerType(Class<?> pHandlerType) {
            mHandlerType = pHandlerType;
        }
        
        /**
         * Gets the handler type.
         *
         * @return the handler type
         */
        public Class<?> getHandlerType() {
            return mHandlerType;
        }
        
    }
    
    /**
     * Handle a data source event.
     *
     * @param pEvent the event
     */
    void handleViewportEvent(AsDataSourceEventIf<T> pEvent);
    
    /**
     * Get the current summary value.
     *
     * @return the text
     */
    String getText();
    
    /**
     * Get the business type of the attribute.
     *
     * @return the business type
     */
    CwfBusinessTypeIf getBusinessType();

    /**
     * Get the business sub type of the attribute.
     *
     * @return the business subtype
     */
    String getBusinessSubtype();

    /**
     * Get the type of the handler.
     *
     * @return the handler type
     */
    HandlerType getHandlerType();
    
}
