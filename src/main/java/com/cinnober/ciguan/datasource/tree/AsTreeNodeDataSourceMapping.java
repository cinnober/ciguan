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
package com.cinnober.ciguan.datasource.tree;

/**
 * Class that holds a data source mapping which a tree node will use to retrieve its
 * children.
 */
public class AsTreeNodeDataSourceMapping {

    /** The data source id. */
    private final String mDataSourceId;
    
    /** The filter expression. */
    private final String mFilterExpression;
    
    /**
     * Instantiates a new as tree node data source mapping.
     *
     * @param pDataSourceId the data source id
     * @param pFilterExpression the filter expression
     */
    public AsTreeNodeDataSourceMapping(String pDataSourceId, String pFilterExpression) {
        mDataSourceId = pDataSourceId;
        mFilterExpression = pFilterExpression;
    }

    /**
     * Gets the data source id.
     *
     * @return the data source id
     */
    public String getDataSourceId() {
        return mDataSourceId;
    }

    /**
     * Gets the filter expression.
     *
     * @return the filter expression
     */
    public String getFilterExpression() {
        return mFilterExpression;
    }
    
}
