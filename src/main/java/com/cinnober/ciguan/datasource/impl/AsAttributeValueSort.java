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

import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.RpcSortCriteriaIf;
import com.cinnober.ciguan.datasource.RpcSortCriteriaIf.SortOrder;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;

/**
 * Generic attribute value implementation of a server side sort.
 *
 * @param <T> The type of the object being sorted
 */
public class AsAttributeValueSort<T> extends AsSort<T> {

    /** The m method. */
    private final AsGetMethodIf<T>[] mMethod;
    
    /** The m to string. */
    private final String mToString;
    
    /**
     * Instantiates a new as attribute value sort.
     *
     * @param pClass the class
     * @param pSortCriteria the sort criteria
     */
    @SuppressWarnings("unchecked")
    public AsAttributeValueSort(Class<T> pClass, RpcSortCriteriaIf[] pSortCriteria) {
        super(pSortCriteria);
        mMethod = new AsGetMethod[pSortCriteria.length];
        for (int i = 0; i < pSortCriteria.length; i++) {
            mMethod[i] = AsGetMethod.create(pClass, pSortCriteria[i].getAttributeName());
        }
        mToString = str(pSortCriteria);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public int compare(T pThis, T pThat) {
        for (int i = 0; i < mSortCriteria.length; i++) {
            Object tThisObject = mMethod[i].getObject(pThis);
            Object tThatObject = mMethod[i].getObject(pThat);
            if ((tThisObject == null || tThisObject instanceof Comparable<?>) &&
                (tThatObject == null || tThatObject instanceof Comparable<?>)) {
                Comparable tThisValue = tThisObject == null ? null : (Comparable) tThisObject;
                Comparable tThatValue = tThatObject == null ? null : (Comparable) tThatObject;
                if (tThisObject != tThatObject) {
                    int tDiff = tThisValue == null ? -1 : tThatValue == null ? 1 : tThisValue.compareTo(tThatValue);
                    if (tDiff != 0) {
                        return (mSortCriteria[i].getSortOrder() == SortOrder.ASCENDING) ? tDiff : -tDiff;
                    }
                }
            }
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return mToString;
    }
    
    /**
     * Returns the sort criteria string value.
     *
     * @param pSortCriteria the sort criteria
     * @return the string
     */
    private String str(RpcSortCriteriaIf[] pSortCriteria) {
        String tRet = "";
        for (RpcSortCriteriaIf tCrit : pSortCriteria) {
            tRet += tCrit;
        }
        return tRet;
    }
    
    @Override
    public int getPriority(String pAttributeName) {
        for (int i = 0; i < mSortCriteria.length; i++) {
            if (mSortCriteria[i].getAttributeName().equals(pAttributeName)) {
                return i + 1;
            }
        }
        return 0;
    }
    
    @Override
    public SortOrder getSortOrder(String pAttributeName) {
        for (int i = 0; i < mSortCriteria.length; i++) {
            if (mSortCriteria[i].getAttributeName().equals(pAttributeName)) {
                return mSortCriteria[i].getSortOrder();
            }
        }
        return SortOrder.UNSORTED;
    }
    
}
