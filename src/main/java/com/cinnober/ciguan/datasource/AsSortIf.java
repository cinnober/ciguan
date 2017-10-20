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

import java.util.Comparator;

import com.cinnober.ciguan.datasource.RpcSortCriteriaIf.SortOrder;

/**
 * The Interface AsSortIf.
 *
 * @param <T> the generic type
 */
public interface AsSortIf<T> extends Comparator<T> {

    /**
     * Get the contained sort criteria.
     *
     * @return the sort criteria
     */
    RpcSortCriteriaIf[] getSortCriteria();

    /**
     * If not sorted by this attribute, return 0.
     *
     * @param pAttr the attr
     * @return the priority
     */
    int getPriority(String pAttr);
    
    /**
     * Return the sort order.
     *
     * @param pAttr the attr
     * @return the sort order
     */
    SortOrder getSortOrder(String pAttr);

}
