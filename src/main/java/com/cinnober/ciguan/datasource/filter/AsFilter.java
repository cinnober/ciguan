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
package com.cinnober.ciguan.datasource.filter;

import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.RpcFilterCriteriaIf;

/**
 * Base class for server side filter implementation
 * @param <T> The type of the filtered object
 */
public abstract class AsFilter<T> implements AsFilterIf<T> {

    protected final RpcFilterCriteriaIf mFilterCriteria;
    
    public AsFilter() {
        this(null);
    }
    
    public AsFilter(RpcFilterCriteriaIf pFilterCriteria) {
        mFilterCriteria = pFilterCriteria;
    }
    
    @Override
    public String toString() {
        return mFilterCriteria == null ? "" : mFilterCriteria.toString();
    }
    
    public RpcFilterCriteriaIf getFilterCriteria() {
        return mFilterCriteria;
    }
    
}
