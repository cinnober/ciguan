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

import java.util.HashMap;
import java.util.Map;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.impl.MvcEventEnum;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.RpcHasDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceEventIf.Type;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;

/**
 *
 * Data subscription listener
 *
 * @param <T> the type in the data source being subscribed to
 */
public class AsDataSourceDataListenerImpl<T> extends AsDataSourceListenerImpl<T> implements RpcHasDataSourceEventIf {

    /**
     * Changed items since last poll, key is object key, value is the latest event type
     */
    protected Map<String, Type> mChangedItems;

    /**
     * The get methods, one for each value
     */
    protected AsGetMethodIf<T>[] mGetMethods;

    /**
     * Instantiates a new as data source viewport listener impl.
     *
     * @param pList the list
     * @param pAttributes the attributes
     * @param pSubscriptionHandle the subscription handle
     * @param pConnection the connection
     * @param pViewId the view id
     */
    public AsDataSourceDataListenerImpl(AsListIf<T> pList, String[] pAttributes, int pSubscriptionHandle,
            AsDataSourceServiceIf pConnection, String pViewId) {
        super(pList, pAttributes, pSubscriptionHandle, pConnection, pViewId);
    }

    /**
     * Initialize the value getters
     */
    @SuppressWarnings("unchecked")
    protected void init() {
        mChangedItems = new HashMap<String, Type>();
        mGetMethods = new AsGetMethodIf[mAttributes.length];
        for (int i = 0; i < mAttributes.length; i++) {
            mGetMethods[i] = AsGetMethod.create(mList.getItemClass(), mAttributes[i]);
        }
    }

    @Override
    public void onDataSourceEvent(AsDataSourceEventIf<T> pEvent) {
        switch (pEvent.getType()) {
        case ADD:
        case UPDATE:
            change(pEvent.getType(), pEvent.getNewValue());
            mDataSourceService.addPendingDataSourceEvent(this);
            break;

        case REMOVE:
            change(pEvent.getType(), pEvent.getOldValue());
            mDataSourceService.addPendingDataSourceEvent(this);
            break;

        case CLEAR:
            clear();
            break;

        case SNAPSHOT:
            init();
            for (T tItem : pEvent.getSnapshot()) {
                change(Type.ADD, tItem);
            }
            mDataSourceService.addPendingDataSourceEvent(this);
            break;

        default:
            break;
        }
    }

    /**
     * Handle an object change
     * @param pType the type of change
     * @param pItem the item which was changed
     */
    protected void change(Type pType, T pItem) {
        String tKey = mList.getKey(pItem);
        synchronized (mChangedItems) {
            mChangedItems.put(tKey, pType);
        }
    }

    /**
     * The data source has been cleared
     */
    protected void clear() {
        synchronized (mChangedItems) {
            mChangedItems.clear();
        }
        CwfDataIf tEvent = CwfDataFactory.create(MvcEventEnum.DataClearEvent);
        CwfMessage tMessage = new CwfMessage(tEvent, mSubscriptionHandle);
        mDataSourceService.addPendingDataSourceEvent(tMessage);
    }

    @Override
    public String getKey() {
        return Integer.toString(mSubscriptionHandle);
    }

    @Override
    public CwfMessage getEvent() {
        // Copy and clear the changed items
        Map<String, Type> tChangedItems = null;
        synchronized (mChangedItems) {
            tChangedItems = new HashMap<String, Type>(mChangedItems);
            mChangedItems.clear();
        }

        // TODO: Do we need to divide this into chunks?
        Map<String, String[]> tValues = new HashMap<String, String[]>();
        String[] tKeys = new String[tChangedItems.size()];
        String[] tTypes = new String[tChangedItems.size()];
        int tItemCount = 0;

        for (Map.Entry<String, Type> tEntry : tChangedItems.entrySet()) {
            T tItem = null;
            try {
                tItem = mList.get(tEntry.getKey());
            }
            catch (Exception e) {
                // Data was removed while reading, count it and return
                //                AsMetricsIf.Singleton.get().incrementCounter(AsMetricsIf.MX_FAILED_DATA_READ_ATTEMPTS);
                return null;
            }

            tKeys[tItemCount] = tEntry.getKey();
            tTypes[tItemCount] = tEntry.getValue().name();
            String[] tAttributeValues = new String[mGetMethods.length];
            for (int i = 0; i < mGetMethods.length; i++) {
                tAttributeValues[i] = tItem == null ? null : mGetMethods[i].getText(tItem, mDataSourceService);
            }
            tValues.put(tEntry.getKey(), tAttributeValues);

            tItemCount += 1;
        }

        CwfDataIf tData = CwfDataFactory.create(MvcEventEnum.DataUpdateEvent);
        tData.setProperty(ATTR_TOTAL_SIZE, mList.size());

        CwfDataIf tGridData = CwfDataFactory.create();
        tGridData.setProperty(ATTR_KEYS, tKeys);
        tGridData.setProperty(ATTR_TYPE, tTypes);
        tGridData.setProperty(ATTR_VALUES, tValues);
        tData.setObject(ATTR_GRID_DATA, tGridData);

        return new CwfMessage(tData, mSubscriptionHandle);
    }

}
