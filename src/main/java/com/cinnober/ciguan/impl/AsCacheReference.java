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
package com.cinnober.ciguan.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.data.AsMetaObject;
import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceListenerIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsListIf;

/**
 * 
 * @param <A>
 * @param <B>
 */
public class AsCacheReference<A, B> implements AsDataSourceListenerIf<A> {

    private final AsGetMethodIf<A> mReferenceField;
    private final Map<String, Set<A>> mMap = new HashMap<String, Set<A>>();
    private final AsListIf<A> mList;
    private String mName;
    
    public AsCacheReference(String pType, String pField) {
        Class<A> tType = As.getMetaDataHandler().getType(pType);
        mName = refName(tType, pField);
        AsMetaObject<A> tMetaData = As.getMetaDataHandler().getMetaData(tType);
        mReferenceField = tMetaData.getGetMethod(pField);
        mList = As.getGlobalDataSources().getDataSource(tType); 
        mList.addListener(this);
    }

    @Override
    public AsDataSourceIf<A> getDataSource() {
        return null;
    }

    @Override
    public void onDataSourceEvent(AsDataSourceEventIf<A> pEvent) {
        switch (pEvent.getType()) {
            case SNAPSHOT:
                for (A tValue : pEvent.getSnapshot()) {
                    add(tValue);
                }
                break;
            case ADD :
                add(pEvent.getNewValue());
                break;
            case UPDATE:
                update(pEvent.getOldValue(), pEvent.getNewValue());
                break;
            case REMOVE:
                remove(pEvent.getOldValue());
                break;
            default :
                break;
        }
    }

    private void add(A pValue) {
        Object tKey = mReferenceField.getValue(pValue);
        if (tKey != null) {
            getMapping(tKey.toString()).add(pValue);
        }
    }
    
    

    private void remove(A pValue) {
        Object tKey = mReferenceField.getValue(pValue);
        if (tKey != null) {
            Collection<A> tSet = getMapping(tKey.toString());
            tSet.remove(pValue);
            if (tSet.isEmpty()) {
                mMap.remove(tKey.toString());
            }
        }
    }
    private void update(A pOld, A pNew) {
        Object tOldKey = mReferenceField.getValue(pOld);
        if (tOldKey != null) {
            Collection<A> tSet = getMapping(tOldKey.toString());
            tSet.remove(pOld);
            add(pNew);
            if (tSet.isEmpty()) {
                mMap.remove(tOldKey.toString());
            }
        }
    }

    public Collection<A> getMapping(String pKey) {
        Set<A> tSet = mMap.get(pKey);
        if (tSet == null) {
            tSet = new HashSet<A>();
            mMap.put(pKey, tSet);
        }
        return tSet;
    }

    public String getName() {
        return mName;
    }
    
    public void destroy() {
        mList.removeListener(this);
        mMap.clear();
    }
    
    static String refName(Class<?> pType, String pFieldName) {
        return As.getTypeName(pType) + "." + pFieldName;
    }
    
}
