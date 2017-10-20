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

import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsSortIf;

/**
 * Class responsible for creating the identifying key for a data source.
 * The key is built up from three components:
 * <ul>
 * <li>Its list ID</li>
 * <li>Its filter</li>
 * <li>Its sorting (not yet implemented)</li>
 * </ul>
 */
public abstract class AsDataSourceKeyGenerator {

    /**
     * Data source key creation.
     *
     * @param pListId the list id
     * @param pFilter the filter
     * @param pSort the sort
     * @return the string
     */
    public static String createKey(String pListId, AsFilterIf<?> pFilter, AsSortIf<?> pSort) {
        if ((pFilter == null || pFilter.toString().length() == 0) && pSort == null) {
            return pListId;
        }
        if (pSort == null) {
            return pListId + "|" + pFilter;
        }
        if (pFilter == null) {
            return pListId + "||" + pSort;
        }
        
        return pListId + "|" + pFilter + "|" + pSort;
    }
}
