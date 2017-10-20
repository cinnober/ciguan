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

import com.cinnober.ciguan.AsHandlerRegistrationIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.impl.MvcEventEnum;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceListenerIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.subscription.impl.AsClientSubscriptionImpl;

/**
 * Implementation of the data source listener interface. The implementation creates client side events and
 * enqueues them to the pending events queue in the user connection
 *
 * @param <T> the generic type
 */
public class AsDataSourceListenerImpl<T> extends AsClientSubscriptionImpl
    implements AsDataSourceListenerIf<T>, AsHandlerRegistrationIf, MvcModelAttributesIf {

    /** Snapshot size restriction, if exceeded, the snapshot is sent in chunks rather than as
     * one large initiation event. */
    protected static final int SNAPSHOT_SEGMENTATION_THRESHOLD = 100;

    /** The data source service. */
    protected final AsDataSourceServiceIf mDataSourceService;

    /** The attributes. */
    protected final String[] mAttributes;

    /** The list. */
    protected AsListIf<T> mList;

    /** The view id. */
    protected final String mViewId;

    /**
     * Instantiates a new as data source listener impl.
     *
     * @param pList the list
     * @param pAttributes the attributes
     * @param pSubscriptionHandle the subscription handle
     * @param pDataSourceService the data source service
     * @param pViewId the view id
     */
    public AsDataSourceListenerImpl(
        AsListIf<T> pList, String[] pAttributes, int pSubscriptionHandle, AsDataSourceServiceIf pDataSourceService,
        String pViewId) {
        super(pSubscriptionHandle);
        mList = pList;
        mViewId = pViewId;
        mAttributes = pAttributes;
        mDataSourceService = pDataSourceService;
        pList.addListener(this);
    }

    /**
     * Instantiates a new as data source listener implementation.
     *
     * @param pList the list
     * @param pSubscriptionHandle the subscription handle
     * @param pDataSourceService the data source service
     */
    public AsDataSourceListenerImpl(
        AsListIf<T> pList, int pSubscriptionHandle, AsDataSourceServiceIf pDataSourceService) {
        this(pList, null, pSubscriptionHandle, pDataSourceService, null);
    }


    @Override
    public AsDataSourceIf<T> getDataSource() {
        return mList;
    }

    @Override
    public void onDataSourceEvent(AsDataSourceEventIf<T> pEvent) {
        CwfDataIf tData = null;
        switch (pEvent.getType()) {
            case ADD:
                tData = CwfDataFactory.create(MvcEventEnum.ListAddEvent);
                tData.setProperty(ATTR_INDEX, pEvent.getIndex());
                tData.setProperty(ATTR_KEY, mList.getKey(pEvent.getNewValue()));
                tData.setProperty(ATTR_TEXT, mList.getText(pEvent.getNewValue(), mDataSourceService));
                break;

            case UPDATE:
                tData = CwfDataFactory.create(MvcEventEnum.ListUpdateEvent);
                tData.setProperty(ATTR_INDEX, pEvent.getIndex());
                tData.setProperty(ATTR_KEY, mList.getKey(pEvent.getNewValue()));
                tData.setProperty(ATTR_TEXT, mList.getText(pEvent.getNewValue(), mDataSourceService));
                break;

            case REMOVE:
                tData = CwfDataFactory.create(MvcEventEnum.ListRemoveEvent);
                tData.setProperty(ATTR_INDEX, pEvent.getIndex());
                break;

            case SNAPSHOT:
                sendSnapshot();
                break;

            case CLEAR:
                tData = CwfDataFactory.create(MvcEventEnum.ListClearEvent);
                break;

            default:;
        }
        if (tData != null) {
            CwfMessage tMessage = new CwfMessage(tData, mSubscriptionHandle);
            mDataSourceService.addPendingDataSourceEvent(tMessage);
        }
    }

    /**
     * Send a snapshot, break up into segments if we exceed the threshold
     * TODO: Do we need to synchronize this?.
     */
    protected void sendSnapshot() {
        int tListSize = mList.size();
        if (tListSize >= SNAPSHOT_SEGMENTATION_THRESHOLD) {
            int tOverflow = tListSize % SNAPSHOT_SEGMENTATION_THRESHOLD;
            int tLastSegmentSize = tOverflow == 0 ? SNAPSHOT_SEGMENTATION_THRESHOLD : tOverflow;
            int tNoOfSegments = tListSize / SNAPSHOT_SEGMENTATION_THRESHOLD + (tOverflow > 0 ? 1 : 0);

            int i = 0;
            for (int tSegmentNo = 0; tSegmentNo < tNoOfSegments; tSegmentNo++) {
                int tSegmentSize = (tSegmentNo < tNoOfSegments - 1 ?
                    SNAPSHOT_SEGMENTATION_THRESHOLD : tLastSegmentSize);
                String[][] tItems = new String[tSegmentSize][];
                for (int j = 0; j < tSegmentSize; j++) {
                    T tItem = mList.get(i++);
                    tItems[j] = new String[] {
                        mList.getKey(tItem), mList.getText(tItem, mDataSourceService)};
                }

                CwfDataIf tData = CwfDataFactory.create(MvcEventEnum.ListInitEvent);
                tData.setProperty(ATTR_SEGMENT_COUNT, tNoOfSegments);
                tData.setProperty(ATTR_SEGMENT_NO, tSegmentNo);
                tData.setProperty(ATTR_VALUES, tItems);
                if (tSegmentNo == 0) {
                    tData.setObject(ATTR_METADATA,
                        mList.getListMetaData() != null ? mList.getListMetaData().values() : null);
                }

                mDataSourceService.addPendingDataSourceEvent(new CwfMessage(tData, mSubscriptionHandle));
            }
        }
        else {
            CwfDataIf tData = CwfDataFactory.create(MvcEventEnum.ListInitEvent);
            String[][] tItems = new String[mList.size()][];
            int i = 0;
            for (T tItem : mList.values()) {
                tItems[i++] = new String[] {
                    mList.getKey(tItem), mList.getText(tItem, mDataSourceService)};
            }
            tData.setProperty(ATTR_SEGMENT_COUNT, 1);
            tData.setProperty(ATTR_SEGMENT_NO, 0);
            tData.setProperty(ATTR_VALUES, tItems);
            tData.setObject(ATTR_METADATA,
                mList.getListMetaData() != null ? mList.getListMetaData().values() : null);

            mDataSourceService.addPendingDataSourceEvent(new CwfMessage(tData, mSubscriptionHandle));
        }

    }

    @Override
    public void removeHandler() {
        mList.removeListener(this);
    }

}
