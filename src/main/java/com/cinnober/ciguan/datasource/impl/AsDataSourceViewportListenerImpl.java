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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.impl.MvcEventEnum;
import com.cinnober.ciguan.data.AsMetaObject;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsDataSourceViewportListenerIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.AsTreeNodeIf;
import com.cinnober.ciguan.datasource.AsViewportSummaryHandlerIf;
import com.cinnober.ciguan.datasource.RpcHasDataSourceEventIf;
import com.cinnober.ciguan.datasource.RpcSortCriteriaIf;
import com.cinnober.ciguan.datasource.RpcSortCriteriaIf.SortOrder;
import com.cinnober.ciguan.datasource.filter.AsAttributeValueFilter;
import com.cinnober.ciguan.datasource.filter.AsRequestFilter;
import com.cinnober.ciguan.datasource.filter.RpcAttributeSortCriteria;
import com.cinnober.ciguan.datasource.filter.RpcFilterOperator;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;
import com.cinnober.ciguan.datasource.listtree.AsListTreeList;
import com.cinnober.ciguan.datasource.listtree.AsListTreeNode;
import com.cinnober.ciguan.datasource.summary.AsViewportSummaryHandler;
import com.cinnober.ciguan.datasource.tree.AsTreeList;
import com.cinnober.ciguan.datasource.tree.AsTreeNode;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.CwfBusinessTypes;

/**
 * Implementation of the data source viewport listener interface. The implementation creates client side events and
 * enqueues them to the pending events queue in the user connection. This specific implementation is used
 * for viewports, and adds some logic related to that. For example we need to keep track of whether an update
 * in the list is visible in the client or not, and we also want to decrease the number of generated events
 * to the client to one and only one per poll.
 *
 * TODO: Non-updated rows in the viewport should not be sent to the client.
 *
 * @param <T> the type of object contained in the underlying list
 */
public class AsDataSourceViewportListenerImpl<T> extends AsDataSourceListenerImpl<T>
implements AsDataSourceViewportListenerIf<T>, RpcHasDataSourceEventIf {

    /** The get methods. */
    protected AsGetMethodIf<T>[] mGetMethods;

    /** The summary handlers. */
    protected AsViewportSummaryHandlerIf<T>[] mSummaryHandlers;

    /** The state method. */
    protected AsGetMethodIf<T> mStateMethod;

    /** The viewport size. */
    protected int mViewportSize;

    /** The first index. */
    protected int mFirstIndex;

    /** The first object key. */
    protected String mFirstObjectKey;

    /** The selected object key. */
    protected String mSelectedObjectKey;

    /** The selected object key set. */
    protected Set<String> mSelectedObjectKeySet = new LinkedHashSet<String>();

    /** The selected object viewport index. */
    protected Integer mSelectedObjectViewportIndex;

    /** The values have changed. */
    protected boolean mValuesHaveChanged;

    /** The list size changed. */
    protected boolean mListSizeChanged;

    /** The snap to bottom. */
    protected boolean mSnapToBottom;

    /** The multi select. */
    protected boolean mMultiSelect;

    /** The has summaries. */
    protected boolean mHasSummaries;

    /**
     * Instantiates a new as data source viewport listener impl.
     *
     * @param pList the list
     * @param pAttributes the attributes
     * @param pSubscriptionHandle the subscription handle
     * @param pMultiSelect the multi select
     * @param pConnection the connection
     * @param pViewId the view id
     */
    public AsDataSourceViewportListenerImpl(AsListIf<T> pList, String[] pAttributes, int pSubscriptionHandle,
            boolean pMultiSelect, AsDataSourceServiceIf pConnection, String pViewId) {
        super(pList, pAttributes, pSubscriptionHandle, pConnection, pViewId);
        mMultiSelect = pMultiSelect;
    }

    /**
     * Initialize the value getters, handlers and the state getter
     * FIXME: Since the mSummaryHandlers array is mostly consisting of null entries, we might need to
     * optimize the storage.
     *
     * @param pClass the class
     * @param pAttributes the attributes
     */
    @SuppressWarnings("unchecked")
    protected void init(Class<T> pClass, String[] pAttributes) {
        mGetMethods = new AsGetMethodIf[pAttributes.length];
        mSummaryHandlers = new AsViewportSummaryHandlerIf[pAttributes.length];
        for (int i = 0; i < pAttributes.length; i++) {
            mGetMethods[i] = AsGetMethod.create(pClass, pAttributes[i]);
            mSummaryHandlers[i] = AsViewportSummaryHandler.create(mViewId, mGetMethods[i], mDataSourceService);
            if (mSummaryHandlers[i] != null) {
                mHasSummaries = true;
            }
        }
        if (AsTreeNode.class.isAssignableFrom(pClass)) {
            mStateMethod = (AsGetMethod<T>) AsTreeNode.getStateGetMethod();
        }
        else if (AsListTreeNode.class.isAssignableFrom(pClass)) {
            mStateMethod = (AsGetMethod<T>) AsListTreeNode.getStateGetMethod();
        }
        else {
            AsMetaObject<T> tMeta = As.getMetaDataHandler().getMetaData(pClass);
            if (tMeta != null && tMeta.getStateField() != null && !tMeta.getStateField().isEmpty()) {
                mStateMethod = tMeta.getGetMethod(tMeta.getStateField());
            }
        }
    }

    @Override
    public synchronized void onDataSourceEvent(AsDataSourceEventIf<T> pEvent) {
        switch (pEvent.getType()) {
        case ADD:
            add(pEvent);
            break;
        case CLEAR:
            clear();
            break;
        case REMOVE:
            remove(pEvent);
            break;
        case UPDATE:
            update(pEvent);
            break;
        case SNAPSHOT:
            snapshot(pEvent);
            break;
        default:
        }
        callViewportHandlers(pEvent);
    }

    /**
     * Call all viewport handlers with the given event.
     *
     * @param pEvent the event
     */
    protected void callViewportHandlers(AsDataSourceEventIf<T> pEvent) {
        if (mHasSummaries) {
            for (AsViewportSummaryHandlerIf<T> tHandler : mSummaryHandlers) {
                if (tHandler != null) {
                    tHandler.handleViewportEvent(pEvent);
                }
            }
        }
    }

    /**
     * Process an add event
     * If an item has been added above the viewport, advance the first row to continue looking at the
     * same rows.
     *
     * @param pEvent the event
     */
    protected void add(AsDataSourceEventIf<T> pEvent) {
        setValuesHaveChanged(true);
        setListSizeChanged();
    }

    /**
     * Process an update event
     * Generate an event if data being viewed was modified.
     *
     * @param pEvent the event
     */
    protected void update(AsDataSourceEventIf<T> pEvent) {
        setValuesHaveChanged(true);
    }

    /**
     * Process a removal event
     * Generate an event if data being viewed was removed
     * If an item has been removed above the viewport, revert the first row to continue looking at the
     * same rows.
     *
     * @param pEvent the event
     */
    protected void remove(AsDataSourceEventIf<T> pEvent) {
        // FIXME: Remove the key from selected keys and unselect if selected?
        setValuesHaveChanged(true);
        setListSizeChanged();
    }

    /**
     * Process a snapshot event.
     *
     * @param pEvent the event
     */
    protected void snapshot(AsDataSourceEventIf<T> pEvent) {
        init(mList.getItemClass(), mAttributes);
        CwfDataIf tData = CwfDataFactory.create(MvcEventEnum.ViewportInitEvent);
        tData.setProperty(ATTR_SIZE, mList.size());
        mDataSourceService.addPendingDataSourceEvent(new CwfMessage(tData, mSubscriptionHandle));
    }

    /**
     * Conditionally set the values changed flag .
     *
     * @param pChanged the new values have changed
     */
    protected void setValuesHaveChanged(boolean pChanged) {
        if (pChanged && !mValuesHaveChanged) {
            mValuesHaveChanged = true;
        }
        mDataSourceService.addPendingDataSourceEvent(this);
    }

    /**
     * Set the first index changed flag.
     */
    protected void setListSizeChanged() {
        if (!mListSizeChanged && !mValuesHaveChanged) {
            // if mValuesHaveChanged, the value of mListSizeChanged is not relevant
            mListSizeChanged = true;
            mDataSourceService.addPendingDataSourceEvent(this);
        }
    }

    @Override
    public String getKey() {
        return Integer.toString(mSubscriptionHandle);
    }

    @Override
    public void moveViewportPosition(int pOffset) {
        // Restrict move to maximum possible
        int tFirstIndex = (mFirstObjectKey == null ? 0 : mList.indexOf(mFirstObjectKey));
        int tLastIndex = tFirstIndex +  mViewportSize - 1;
        if (tFirstIndex + pOffset < 0) {
            pOffset = -tFirstIndex;
        }
        else if (tLastIndex + pOffset > mList.size()) {
            pOffset = mList.size() - tLastIndex - 1;
        }

        // Calculate the first object key
        int tCurrentIndex = (mFirstObjectKey == null ? 0 : mList.indexOf(mFirstObjectKey));
        int tNewIndex = Math.min((tCurrentIndex + pOffset), mList.size() - mViewportSize);
        if (tNewIndex <= 0) {
            mFirstObjectKey = null;
            tNewIndex = 0;
        }
        else {
            mFirstObjectKey = mList.getKey(mList.get(tNewIndex));
        }

        // Move the viewport selection index (in the opposite direction of the scroll)
        if (mSelectedObjectViewportIndex != null && tNewIndex >= 0) {
            tNewIndex = mSelectedObjectViewportIndex.intValue() - pOffset;
            if (tNewIndex >= 0 && tNewIndex < mViewportSize) {
                mSelectedObjectViewportIndex = Integer.valueOf(tNewIndex);
            }
            else {
                mSelectedObjectViewportIndex = null;
            }
        }
        if (pOffset < 0) {
            mSnapToBottom = false;
        }
        setValuesHaveChanged(true);
        mFirstIndex = -1;
    }

    @Override
    public void setViewportPosition(int pPosition, boolean pSnapToBottom) {
        if (pSnapToBottom) {
            mFirstIndex = Math.max(mList.size() - mViewportSize, 0);
        }
        else {
            mFirstIndex = Math.min((pPosition), mList.size() - mViewportSize);
        }
        mSnapToBottom = pSnapToBottom;
        if (mFirstIndex <= 0) {
            mFirstObjectKey = null;
            mFirstIndex = 0;
        }
        else {
            mFirstObjectKey = mList.getKey(mList.get(mFirstIndex));
        }
        mSelectedObjectViewportIndex = null;
        setValuesHaveChanged(true);
    }

    @Override
    public void setViewportFilter(String pFilterExpression) {
        AsFilterIf<T> tFilter = pFilterExpression == null ?
                null : AsAttributeValueFilter.create(mList.getItemClass(), pFilterExpression);

        // Handle query based data sources in a special way

        AsListIf<T> tOriginalQueryList = getOriginalQueryList();
        if (tOriginalQueryList != null) {
            if (tFilter != null) {
                // Request based data source: Switch to a new filtered sub-list
                AsDataSourceIf<T> tNewDataSource = tOriginalQueryList.createDataSource(tFilter);
                setDataSource((AsListIf<T>) tNewDataSource);
            }
            else {
                // Request based data source: Go back to the original list
                setDataSource(tOriginalQueryList);
            }
        }
        else {
            // Normal filtering
            AsDataSourceIf<T> tFilteredList = mDataSourceService.getDataSource(mList.getDataSourceId(), tFilter);
            setDataSource((AsListIf<T>) tFilteredList);
        }
    }

    /**
     * Attempt to back up to the original unfiltered request based data source.
     *
     * @return the original query list
     */
    protected AsListIf<T> getOriginalQueryList() {
        AsListIf<T> tList = mList;
        while (tList.getSource() != null) {
            if (tList.getSort() == null && tList.getFilter() instanceof AsRequestFilter) {
                return tList;
            }
            tList = (AsListIf<T>) tList.getSource();
        }
        return null;
    }

    /**
     * Attempt to back up to the filtered request based data source.
     *
     * @return the filtered query list
     */
    protected AsListIf<T> getFilteredQueryList() {
        AsListIf<T> tOriginalQueryList = getOriginalQueryList();
        if (tOriginalQueryList != null) {
            AsListIf<T> tList = mList;
            while (tList.getSource() != null) {
                if (tList.getSort() == null && tList.getSource() == tOriginalQueryList) {
                    return tList;
                }
                tList = (AsListIf<T>) tList.getSource();
            }
        }
        return null;
    }

    @Override
    public void setViewportSortCriteria(final int pAttributeIndex, final SortOrder pSortOrder) {
        RpcSortCriteriaIf[] tSort = {
                new RpcAttributeSortCriteria(mAttributes[pAttributeIndex], pSortOrder) };
        if (pSortOrder == SortOrder.UNSORTED) {
            tSort = null;
        }

        // Handle query based data sources in a special way

        AsListIf<T> tFilteredQueryList = getFilteredQueryList();
        if (tFilteredQueryList != null) {
            if (tSort != null) {
                // Request based data source: Switch to a new filtered sub-list
                AsAttributeValueSort<T> tS = new AsAttributeValueSort<T>(mList.getItemClass(), tSort);
                AsDataSourceIf<T> tNewDataSource = tFilteredQueryList.createDataSource(tS);
                setDataSource((AsListIf<T>) tNewDataSource);
            }
            else {
                // Request based data source: Go back to the original list
                setDataSource(tFilteredQueryList);
            }
        }
        else {
            // Normal sorting
            AsDataSourceIf<T> tSortedList = mDataSourceService.getDataSource(mList, tSort);
            setDataSource((AsListIf<T>) tSortedList);
        }

    }

    @Override
    public void setViewportSelection(String pSelectedObjectKey, Integer pSelectedObjectViewportIndex,
            boolean pControlKeyDown, boolean pShiftKeyDown) {

        if (!pControlKeyDown && !pShiftKeyDown) {
            select(pSelectedObjectKey, pSelectedObjectViewportIndex);
        }
        else if (pShiftKeyDown && !pControlKeyDown) {
            selectShift(pSelectedObjectKey, pSelectedObjectViewportIndex);
        }
        else if (pControlKeyDown && !pShiftKeyDown) {
            selectControl(pSelectedObjectKey, pSelectedObjectViewportIndex);
        }
        else {
            // Cannot handle both shift and control key down
            return;
        }

        if (mSelectedObjectKey != null && mSelectedObjectViewportIndex != null) {
            int tSelectedIndex = mList.indexOf(mSelectedObjectKey);
            int tFirstIndex = Math.max(tSelectedIndex - mSelectedObjectViewportIndex, 0);
            mFirstObjectKey = mList.getKey(mList.get(tFirstIndex));
        }
        setValuesHaveChanged(true);
    }

    /**
     * Unselect all previously selected rows, and select the given row.
     *
     * @param pSelectedObjectKey the selected object key
     * @param pSelectedObjectViewportIndex the selected object viewport index
     */
    protected void select(String pSelectedObjectKey, Integer pSelectedObjectViewportIndex) {
        mSelectedObjectKeySet.clear();
        mSelectedObjectKey = pSelectedObjectKey;
        mSelectedObjectKeySet.add(pSelectedObjectKey);
        mSelectedObjectViewportIndex = pSelectedObjectViewportIndex;
    }

    /**
     * Unselect all previously selected rows, and select rows from current index to the new index.
     *
     * @param pSelectedObjectKey the selected object key
     * @param pSelectedObjectViewportIndex the selected object viewport index
     */
    protected void selectShift(String pSelectedObjectKey, Integer pSelectedObjectViewportIndex) {
        if (!mMultiSelect) {
            return;
        }
        mSelectedObjectKeySet.clear();
        int tCurrentIndex = mSelectedObjectKey == null ? 0 : mList.indexOf(mSelectedObjectKey);
        int tNewIndex = mList.indexOf(pSelectedObjectKey);
        int tStart = Math.min(tCurrentIndex, tNewIndex);
        int tStop = Math.max(tCurrentIndex, tNewIndex);
        for (int i = tStart; i <= tStop; i++) {
            mSelectedObjectKeySet.add(mList.getKey(mList.get(i)));
        }
    }

    /**
     * Unselect the given row if it is already selected otherwise select it.
     *
     * @param pSelectedObjectKey the selected object key
     * @param pSelectedObjectViewportIndex the selected object viewport index
     */
    protected void selectControl(String pSelectedObjectKey, Integer pSelectedObjectViewportIndex) {
        if ((pSelectedObjectKey != null && pSelectedObjectKey.equals(mSelectedObjectKey))
                || mSelectedObjectKeySet.contains(pSelectedObjectKey)) {
            mSelectedObjectKey = null;
            mSelectedObjectViewportIndex = null;
            mSelectedObjectKeySet.remove(pSelectedObjectKey);
            return;
        }
        if (!mMultiSelect) {
            return;
        }
        mSelectedObjectKeySet.add(pSelectedObjectKey);
        mSelectedObjectKey = pSelectedObjectKey;
        mSelectedObjectViewportIndex = pSelectedObjectViewportIndex;
    }

    @Override
    public void moveViewportSelection(Integer pSelectedObjectMovement, boolean pControlKeyDown,
            boolean pShiftKeyDown) {
        if (mSelectedObjectKey == null) {
            return;
        }
        int tIndex = mList.indexOf(mSelectedObjectKey) + pSelectedObjectMovement;
        if (tIndex < 0 || tIndex >= mList.size()) {
            return;
        }
        mSelectedObjectKey = mList.getKey(mList.get(tIndex));
        mSelectedObjectKeySet.clear();
        mSelectedObjectKeySet.add(mSelectedObjectKey);
        if (mSelectedObjectViewportIndex == null) {
            mSelectedObjectViewportIndex = 0;
        }
        else {
            mSelectedObjectViewportIndex = mSelectedObjectViewportIndex + pSelectedObjectMovement;
            if (mSelectedObjectViewportIndex  <= -1) {
                moveViewportPosition(-3);
            }
            else if (mSelectedObjectViewportIndex >= mViewportSize) {
                moveViewportPosition(3);
            }
        }
        setValuesHaveChanged(true);
    }

    @Override
    public void setViewportSize(int pViewportSize) {
        mViewportSize = pViewportSize < 0 ? 0 : pViewportSize;
        setValuesHaveChanged(true);
    }

    @Override
    public void setDataSource(AsListIf<T> pDataSource) {
        if (mList == pDataSource) {
            return;
        }
        mList.removeListener(this);
        mList = pDataSource;
        setValuesHaveChanged(true);
        mListSizeChanged = false;
        mList.addListener(this);
        // TODO: What to do with the selected object when the data source is replaced?
        //        mFirstObjectKey;
        //        mSelectedObjectKey;
        //        mSelectedObjectViewportIndex;
    }

    /**
     * Clear.
     */
    protected void clear() {
        setValuesHaveChanged(true);
    }

    @Override
    public void setExpanded(String pObjectKey, boolean pExpanded) {
        String tKey = pObjectKey == null ? mSelectedObjectKey : pObjectKey;
        if (tKey != null) {
            int tIndex = mList.indexOf(tKey);
            if (tIndex >= 0) {
                AsTreeNodeIf<?> tItem = (AsTreeNodeIf<?>) mList.get(tIndex);
                tItem.setExpanded(pExpanded);
            }
        }
    }

    @Override
    public List<CwfDataIf> createFilterExpressions(String pKey, int pColumnIndex) {

        if (mList instanceof AsListTreeList<?> || mList instanceof AsTreeList) {
            return null;
        }
        T tItem = mList.get(pKey);
        if (tItem == null) {
            return null;
        }

        CwfBusinessTypeIf tType = mGetMethods[pColumnIndex].getBusinessType();
        String tName = mGetMethods[pColumnIndex].getAttributeName();
        Object tValue = mGetMethods[pColumnIndex].getObject(tItem);
        String tTextValue = mGetMethods[pColumnIndex].getText(tItem, mDataSourceService);

        List<CwfDataIf> tExpressions = new ArrayList<CwfDataIf>();

        // Skip columns of type Object
        if (tType != null && tType == CwfBusinessTypes.Object) {
            return tExpressions;
        }

        if (tType != null && tType.isOrdinal()) {
            String tFromValue = tValue == null ? "" : tValue.toString();
            String tToValue = tValue == null ? "" : tValue.toString();

            // Adjust from/to for date/time filtering to avoid millisecond confusion
            if (tType == CwfBusinessTypes.Time || tType == CwfBusinessTypes.DateTime) {
                tFromValue = tFromValue.substring(0, tFromValue.length() - 3) + "000";
                tToValue = tToValue.substring(0, tToValue.length() - 3) + "999";
            }

            // From
            CwfDataIf tFilter = CwfDataFactory.create();
            tFilter.setProperty(ATTR_NAME, tName);
            tFilter.setProperty(ATTR_VALUE, tFromValue);
            tFilter.setProperty(ATTR_TEXT, tTextValue);
            tFilter.setProperty(ATTR_FILTER_OPERATOR, RpcFilterOperator.GreaterThanOrEqual.name());
            tExpressions.add(tFilter);

            // To
            tFilter = CwfDataFactory.create();
            tFilter.setProperty(ATTR_NAME, tName);
            tFilter.setProperty(ATTR_VALUE, tToValue);
            tFilter.setProperty(ATTR_TEXT, tTextValue);
            tFilter.setProperty(ATTR_FILTER_OPERATOR, RpcFilterOperator.LessThanOrEqual.name());
            tExpressions.add(tFilter);

        }
        else {
            CwfDataIf tFilter = CwfDataFactory.create();
            tFilter.setProperty(ATTR_NAME, tName);
            tFilter.setProperty(ATTR_VALUE, tValue == null ? "" : tValue.toString());
            tFilter.setProperty(ATTR_TEXT, tTextValue);
            tFilter.setProperty(ATTR_FILTER_OPERATOR, RpcFilterOperator.Equals.name());
            tExpressions.add(tFilter);
        }

        return tExpressions;
    }

    @Override
    public void addObject(T pAddedObject) {
        mList.add(pAddedObject);
    }

    @Override
    public void updateObject(T pUpdatedObject) {
        mList.update(pUpdatedObject);
    }

    @Override
    public void deleteObject(T pDeletedObject) {
        mList.remove(pDeletedObject);
    };

    @Override
    public Set<String> getSelectedKeys() {
        return Collections.unmodifiableSet(mSelectedObjectKeySet);
    }

    @Override
    public AsGetMethodIf<T>[] getGetMethods() {
        return mGetMethods;
    }

    @Override
    public synchronized CwfMessage getEvent() {
        try {
            Map<String, String[]> tValues = new HashMap<String, String[]>();
            String[] tKeys = new String[mViewportSize];
            String[] tStates = new String[mViewportSize];
            String[] tSummaries = new String[mSummaryHandlers.length];

            if (mSnapToBottom) {
                mFirstIndex = Math.max(0, mList.size() - mViewportSize);
            }
            else if (mFirstIndex != 0) {
                if (mSelectedObjectViewportIndex != null) {
                    mFirstIndex = mList.indexOf(mSelectedObjectKey) - mSelectedObjectViewportIndex;
                }
                else {
                    mFirstIndex = mFirstObjectKey == null ? 0 : mList.indexOf(mFirstObjectKey);
                }
                if (mFirstIndex < 0 || mViewportSize >= mList.size()) {
                    mFirstIndex = 0;
                }
            }

            // find out which objects to send
            mSelectedObjectViewportIndex = null;
            List<T> tItemsToSend = new ArrayList<T>();
            int tIndex = mFirstIndex;
            for (int tRow = 0; tRow < mViewportSize && tIndex < mList.size(); tRow++) {
                T tItem = null;
                try {
                    tItem = mList.get(tIndex);
                }
                catch (Exception e) {
                    // Data was removed while reading, count it and return
                    //                    AsMetricsIf.Singleton.get().incrementCounter(AsMetricsIf.MX_FAILED_VIEWPORT_READ_ATTEMPTS);
                    return null;
                }
                if (tItem != null && mStateMethod != null) {
                    tStates[tRow] = (String) mStateMethod.getObject(tItem);
                }
                String tKey = mList.getKey(tItem);
                tKeys[tRow] = tKey;
                tItemsToSend.add(tItem);
                if (tKey.equals(mSelectedObjectKey)) {
                    mSelectedObjectViewportIndex = tRow;
                }
                tIndex++;
            }
            if (mSelectedObjectViewportIndex == null) {
                T tSelectedObject = mSelectedObjectKey == null ? null : mList.get(mSelectedObjectKey);
                if (mSelectedObjectKey == null || tSelectedObject == null) {
                    mSelectedObjectKey = null;
                }
                else {
                    tItemsToSend.add(tSelectedObject);
                }
            }
            List<String> tSelectedKeysToSend = new ArrayList<String>();
            for (T tItem : tItemsToSend) {
                String tKey = mList.getKey(tItem);
                String[] tAttributeValues = new String[mGetMethods.length];
                for (int i = 0; i < mGetMethods.length; i++) {
                    tAttributeValues[i] = mGetMethods[i].getText(tItem, mDataSourceService);
                }
                tValues.put(tKey, tAttributeValues);
                if (mSelectedObjectKeySet.contains(tKey) || tKey.equals(mSelectedObjectKey)) {
                    tSelectedKeysToSend.add(tKey);
                }
            }
            if (mHasSummaries) {
                for (int i = 0; i < mSummaryHandlers.length; i++) {
                    if (mSummaryHandlers[i] != null) {
                        tSummaries[i] = mSummaryHandlers[i].getText();
                    }
                    else {
                        tSummaries[i] = "";
                    }
                }
            }

            CwfDataIf tData = CwfDataFactory.create(MvcEventEnum.ViewportUpdateEvent);

            // add grid data
            CwfDataIf tGridData = CwfDataFactory.create();
            tGridData.setProperty(ATTR_KEYS, tKeys);
            tGridData.setProperty(ATTR_STATE, tStates);
            tGridData.setProperty(ATTR_VALUES, tValues);
            if (mHasSummaries) {
                tGridData.setProperty(ATTR_SUMMARIES, tSummaries);
            }
            tGridData.setProperty(ATTR_FIRST_INDEX, mFirstIndex);
            tGridData.setProperty(ATTR_SELECTED_OBJECT, mSelectedObjectKey);
            tGridData.setProperty(ATTR_SELECTED_INDEX, mSelectedObjectViewportIndex);
            tGridData.setProperty(ATTR_SELECTED_KEYS,
                    tSelectedKeysToSend.toArray(new String[tSelectedKeysToSend.size()]));
            tData.setObject(ATTR_GRID_DATA, tGridData);

            // scroll data
            CwfDataIf tScrollData = CwfDataFactory.create();
            tScrollData.setProperty(ATTR_POSITION, mFirstIndex);
            tScrollData.setProperty(ATTR_TOTAL_SIZE, mList.size());
            tScrollData.setProperty(ATTR_SELECTED_COUNT, mSelectedObjectKeySet.size());
            tScrollData.setProperty(ATTR_VISIBLE_SIZE, tKeys.length);
            tData.setObject(ATTR_SCROLL_DATA, tScrollData);

            mValuesHaveChanged = false;
            mListSizeChanged = false;

            return new CwfMessage(tData, mSubscriptionHandle);
        }
        catch (Exception e) {
            AsLoggerIf.Singleton.get().logThrowable(
                    "Exception while retrieving viewport data from " + mList.getDataSourceId(), e);
        }
        return null;
    }

}
