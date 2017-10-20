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
package com.cinnober.ciguan.datasource.getter;

import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.impl.As;

/**
 * Get method to get attribute value from a referenced object.
 *
 * @param <T> the generic type
 * @param <L> the generic type
 */
public class AsListGetMethod<T, L> extends AsGetMethod<T> {

    /** The list. */
    private AsListIf<L> mList;
    
    /** The getter method. */
    private final AsGetMethodIf<T> mMethod;
    
    /** The value getter. */
    private final AsGetMethodIf<L> mValueMethod;
    
    /** The data source class. */
    private final Class<L> mDataSourceClass;

    /**
     * Instantiates a new as list get method.
     *
     * @param pSourceType the source type
     * @param pSourceField the source field
     * @param pName the name
     * @param pClass the class
     * @param pField the field
     */
    public AsListGetMethod(Class<L> pSourceType, String pSourceField, String pName, Class<T> pClass, String pField) {
        super(pClass, pName);
        mDataSourceClass = pSourceType;
        mMethod = AsGetMethod.create(pClass, pField);
        mValueMethod = pSourceField == null ? null : AsGetMethod.create(pSourceType, pSourceField);
    }

    @Override
    public Object getObject(T pItem) {
        if (mList == null) {
            mList = As.getGlobalDataSources().getDataSource(mDataSourceClass);
        }
        // Always use a non-formatted key
        String tKey = mMethod.getValue(pItem);
        L tItem = mList.get(tKey);
        if (mValueMethod == null) {
            return mList.getText(tItem, null);
        }
        return mValueMethod.getObject(tItem);
    }
    
    @Override
    public String getText(T pItem, AsDataSourceServiceIf pService) {
        if (mList == null) {
            mList = As.getGlobalDataSources().getDataSource(mDataSourceClass);
        }
        // Always use a non-formatted key
        String tKey = mMethod.getValue(pItem);
        L tItem = mList.get(tKey);
        if (mValueMethod == null) {
            return mList.getText(tItem, pService);
        }
        return mValueMethod.getText(tItem, pService);
    };
    
    @Override
    public CwfBusinessTypeIf getBusinessType() {
        return mValueMethod != null ? mValueMethod.getBusinessType() : super.getBusinessType();
    }

    @Override
    public String getBusinessSubtype() {
        return mValueMethod != null ? mValueMethod.getBusinessSubtype() : super.getBusinessSubtype();
    }

}