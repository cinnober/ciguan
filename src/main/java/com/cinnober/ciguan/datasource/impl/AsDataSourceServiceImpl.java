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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsHandlerRegistrationIf;
import com.cinnober.ciguan.AsMetaDataHandlerIf;
import com.cinnober.ciguan.AsSessionDataIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.CwfRequestNameIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.impl.MvcEventEnum;
import com.cinnober.ciguan.client.impl.MvcModelNames;
import com.cinnober.ciguan.client.impl.MvcRequestEnum;
import com.cinnober.ciguan.data.AsContextMenu;
import com.cinnober.ciguan.data.AsMenuItem;
import com.cinnober.ciguan.data.AsMetaDataData;
import com.cinnober.ciguan.data.AsMetaObject;
import com.cinnober.ciguan.data.AsViewDefinition;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsDataSourceViewportListenerIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.AsSessionDataSourcesIf;
import com.cinnober.ciguan.datasource.AsSortIf;
import com.cinnober.ciguan.datasource.AsTreeNodeIf;
import com.cinnober.ciguan.datasource.CwfGlobalDataSources;
import com.cinnober.ciguan.datasource.RpcHasDataSourceEventIf;
import com.cinnober.ciguan.datasource.RpcSortCriteriaIf;
import com.cinnober.ciguan.datasource.RpcSortCriteriaIf.SortOrder;
import com.cinnober.ciguan.datasource.filter.AsAttributeValueFilter;
import com.cinnober.ciguan.datasource.filter.AsIncludeAllFilter;
import com.cinnober.ciguan.datasource.filter.AsRequestFilter;
import com.cinnober.ciguan.datasource.filter.RpcAttributeSortCriteria;
import com.cinnober.ciguan.datasource.listtree.AsListTreeNode;
import com.cinnober.ciguan.datasource.listtree.AsListTreeRoot;
import com.cinnober.ciguan.datasource.tree.AsTreeNode;
import com.cinnober.ciguan.datasource.tree.AsTreeNodeFolder;
import com.cinnober.ciguan.datasource.tree.AsTreeRoot;
import com.cinnober.ciguan.datasource.tree.AsViewContext;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.subscription.AsDataSourceDataSubscriptionRequestIf;
import com.cinnober.ciguan.subscription.AsDataSourceGetListItemRequestIf;
import com.cinnober.ciguan.subscription.AsDataSourceGetListTextRequestIf;
import com.cinnober.ciguan.subscription.AsDataSourceGetMultipleListItemsRequestIf;
import com.cinnober.ciguan.subscription.AsDataSourceListSubscriptionRequestIf;
import com.cinnober.ciguan.subscription.AsDataSourceUnsubscribeRequestIf;
import com.cinnober.ciguan.subscription.AsDataSourceViewportSubscriptionRequestIf;
import com.cinnober.ciguan.subscription.impl.AsDataSourceDataSubscriptionRequest;
import com.cinnober.ciguan.subscription.impl.AsDataSourceGetListItemRequest;
import com.cinnober.ciguan.subscription.impl.AsDataSourceGetListTextRequest;
import com.cinnober.ciguan.subscription.impl.AsDataSourceGetMultipleListItemsRequest;
import com.cinnober.ciguan.subscription.impl.AsDataSourceListSubscriptionRequest;
import com.cinnober.ciguan.subscription.impl.AsDataSourceUnsubscribeRequest;
import com.cinnober.ciguan.subscription.impl.AsDataSourceViewportSubscriptionRequest;

/**
 * Implementation of the data source service layer.
 */
public class AsDataSourceServiceImpl implements AsDataSourceServiceIf, MvcModelAttributesIf {

    /** The Session data sources. */
    protected final AsSessionDataSourcesIf mSessionDataSources;

    /** The Pending data source events. */
    protected final List<CwfMessage> mPendingDataSourceEvents =
        Collections.synchronizedList(new ArrayList<CwfMessage>());

    /** The Pending has data source events. */
    protected final Map<String, RpcHasDataSourceEventIf> mPendingHasDataSourceEvents =
        Collections.synchronizedMap(new HashMap<String, RpcHasDataSourceEventIf>());

    /** The Data source listeners. */
    protected final Map<Integer, AsHandlerRegistrationIf> mDataSourceListeners =
        Collections.synchronizedMap(new HashMap<Integer, AsHandlerRegistrationIf>());

    /** The Session data. */
    private AsSessionDataIf mSessionData;

    /**
     * Instantiates a new as data source service impl.
     */
    public AsDataSourceServiceImpl() {
        mSessionDataSources = As.getBeanFactory().create(AsSessionDataSourcesIf.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetDataSourceSubscriptions() {
        for (AsHandlerRegistrationIf tHandler : mDataSourceListeners.values()) {
            tHandler.removeHandler();
        }
        mDataSourceListeners.clear();
    }

    /**
     * Method to query for an item from a list.
     *
     * @param pConnection the connection
     * @param pRequest the request
     */
    protected void getListItem(AsConnectionIf pConnection, AsDataSourceGetListItemRequestIf pRequest) {
        AsDataSourceIf<?> tDataSource = getDataSource(pRequest.getDataSourceId(), null, null);
        if (!(tDataSource instanceof AsListIf<?>)) {
            return;
        }
        AsListIf<?> tList = (AsListIf<?>) tDataSource;
        String tKey = pRequest.getKey();
        if (tKey == null) {
            return;
        }

        Object tItem = tList.get(tKey);
        if (tItem == null) {
            CwfMessage tMessage = new CwfMessage(MvcEventEnum.ListItemResponse, null, pRequest.getHandle());
            addPendingDataSourceEvent(tMessage);
            return;
        }

        CwfDataIf tData = null;
        if (tItem instanceof AsMapRefData) {
            tData = tItem == null ? null : ((AsMapRefData) tItem).getValues();
        }
        else {
            tData = (CwfDataIf) pConnection.getRequestService().transform(pConnection, tItem);
        }

        // If the fetched item is a view definition, append its associated metadata
        if (tItem instanceof AsViewDefinition) {
            appendMetaData(pConnection, (AsViewDefinition) tItem, tData);
        }

        CwfMessage tMessage = new CwfMessage(MvcEventEnum.ListItemResponse, tData, pRequest.getHandle());
        addPendingDataSourceEvent(tMessage);
    }

    /**
     * Method to query for an item's text from a list.
     *
     * @param pConnection the connection
     * @param pRequest the request
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void getListText(AsConnectionIf pConnection, AsDataSourceGetListTextRequestIf pRequest) {
        AsDataSourceIf<?> tDataSource = getDataSource(pRequest.getDataSourceId(), null, null);
        if (!(tDataSource instanceof AsListIf<?>)) {
            return;
        }
        AsListIf tList = (AsListIf) tDataSource;
        String tKey = pRequest.getKey();
        if (tKey == null) {
            return;
        }

        Object tItem = tList.get(tKey);
        if (tItem == null) {
            CwfMessage tMessage = new CwfMessage(MvcEventEnum.ListTextResponse, null, pRequest.getHandle());
            addPendingDataSourceEvent(tMessage);
            return;
        }

        CwfDataIf tData = CwfDataFactory.create();
        tData.setProperty(ATTR_KEY, pRequest.getKey());
        tData.setProperty(ATTR_TEXT, tList.getText(tItem, this));

        CwfMessage tMessage = new CwfMessage(MvcEventEnum.ListTextResponse, tData, pRequest.getHandle());
        addPendingDataSourceEvent(tMessage);
    }

    /**
     * Append metadata associated with the given view.
     *
     * @param pConnection the connection
     * @param pItem the item
     * @param pData the data
     */
    protected void appendMetaData(AsConnectionIf pConnection, AsViewDefinition pItem, CwfDataIf pData) {

        AsListIf<AsMetaDataData> tMetaList = As.getGlobalDataSources().getDataSource(AsMetaDataData.class);
        Map<String, String> tMetaData = new LinkedHashMap<String, String>();

        // The model
        String tKey = getMetaKey(pItem.getModel());
        if (tKey != null) {
            AsMetaDataData tData = tMetaList.get(tKey);
            tMetaData.put(tData.key, tData.metadata);
        }

        // All contexts
        for (AsViewContext tCtx : pItem.getViewContexts()) {
            String tType = tCtx.getType();
            if (tType.endsWith("[]")) {
                tType = tType.substring(0, tType.length() - 2);
            }
            tKey = getMetaKey(tType);
            if (tKey != null) {
                if (!tMetaData.containsKey(tKey)) {
                    AsMetaDataData tData = tMetaList.get(tKey);
                    tMetaData.put(tData.key, tData.metadata);
                }
            }
        }

        // All blob's
        for (CwfDataIf tBlobDef : pItem.getValues().getObjectList(TAG_BLOB)) {
            tKey = getMetaKey(tBlobDef.getProperty(ATTR_MODEL));
            AsMetaDataData tData = tMetaList.get(tKey);
            if (tData != null) {
                tMetaData.put(tData.key, tData.metadata);
            }
        }

        // The data source type
        if (pItem.getDataSourceId() != null) {
            AsDataSourceIf<Object> tDs =
                pConnection.getDataSourceService().getDataSource(pItem.getDataSourceId(), null);
            if (tDs != null) {
                tKey = As.getTypeName(tDs.getItemClass());
                AsMetaDataData tData = tMetaList.get(tKey);
                if (tData != null) {
                    tMetaData.put(tData.key, tData.metadata);
                }
            }
        }

        // All set, now append the data
        String[][] tData = new String[tMetaData.size()][];
        int tIndex = 0;
        for (Map.Entry<String, String> tEntry : tMetaData.entrySet()) {
            tData[tIndex] = new String[] {tEntry.getKey(), tEntry.getValue()};
            tIndex++;
        }
        pData.setProperty(ATTR_METADATA, tData);
    }

    /**
     * Gets the meta key.
     *
     * @param pKey the key
     * @return the meta key
     */
    protected String getMetaKey(String pKey) {
        if (pKey != null && !pKey.isEmpty()) {
            Class<Object> tType = AsMetaDataHandlerIf.SINGLETON.get().getType(pKey);
            if (tType != null) {
                AsMetaObject<Object> tMetaData = AsMetaDataHandlerIf.SINGLETON.get().getMetaData(tType);
                if (tMetaData.serverRequestName != null && !tMetaData.serverRequestName.isEmpty()) {
                    return pKey + "/" + tMetaData.serverRequestName;
                }
                return pKey;
            }
        }
        return null;
    }

    /**
     * Method to query for multiple items from a list.
     *
     * @param pConnection the connection
     * @param pRequest the request
     */
    protected void getListItems(AsConnectionIf pConnection, AsDataSourceGetMultipleListItemsRequestIf pRequest) {
        AsDataSourceIf<?> tDataSource = getDataSource(pRequest.getDataSourceId(), null, null);
        AsListIf<?> tList = (AsListIf<?>) tDataSource;
        CwfDataIf tContainer = CwfDataFactory.create();

        for (String tKey : pRequest.getKeys()) {
            Object tItem = tList.get(tKey);
            if (tItem != null) {
                CwfDataIf tData = null;
                if (tItem instanceof AsMapRefData) {
                    tData = ((AsMapRefData) tItem).getValues();
                }
                else {
                    tData = (CwfDataIf) pConnection.getRequestService().transform(pConnection, tItem);
                }
                tContainer.addObject(ATTR_ITEMS, tData);
            }
            else {
                tContainer.addObject(ATTR_ITEMS, (CwfDataIf) null);
            }
        }

        CwfMessage tMessage = new CwfMessage(MvcEventEnum.ListMultipleItemsResponse, tContainer, pRequest.getHandle());
        addPendingDataSourceEvent(tMessage);
    }

    /**
     * Subscribe to a list style data source.
     *
     * @param <T> the generic type
     * @param pRequest the request
     */
    protected <T> void subscribe(AsDataSourceListSubscriptionRequestIf pRequest) {
        AsDataSourceIf<T> tDataSource = getDataSource(
            pRequest.getDataSourceId(), pRequest.getFilterCriteria(), pRequest.getSortCriteria());
        if (tDataSource != null) {
            AsDataSourceListenerImpl<T> tListener = new AsDataSourceListenerImpl<T>((AsListIf<T>) tDataSource,
                pRequest.getHandle(), this);
            mDataSourceListeners.put(pRequest.getHandle(), tListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AsHandlerRegistrationIf getHandlerRegistration(Integer pHandle) {
        return mDataSourceListeners.get(pHandle);
    }

    /**
     * Subscribe to data source data changes
     * @param pRequest the request
     */
    protected <T> void subscribe(AsDataSourceDataSubscriptionRequestIf pRequest) {
        AsDataSourceIf<T> tDataSource = getDataSource(
            pRequest.getDataSourceId(), pRequest.getFilterCriteria(), pRequest.getSortCriteria());

        if (tDataSource instanceof AsTreeRoot || tDataSource instanceof AsListTreeRoot) {
            // Not supported
            return;
        }

        AsDataSourceDataListenerImpl<T> tListener = new AsDataSourceDataListenerImpl<T>(
            (AsListIf<T>) tDataSource,
            pRequest.getAttributes(),
            pRequest.getHandle(),
            this,
            pRequest.getViewId());
        mDataSourceListeners.put(pRequest.getHandle(), tListener);
    }

    /**
     * Subscribe to a viewport style data source.
     *
     * @param <T> the generic type
     * @param pRequest the request
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <T> void subscribe(AsDataSourceViewportSubscriptionRequestIf pRequest) {
        AsDataSourceIf<T> tDataSource = getDataSource(
            pRequest.getDataSourceId(), pRequest.getFilterCriteria(), pRequest.getSortCriteria());

        // Create the tree if the data source is a tree root
        // TODO: How/when is the tree discarded???
        if (tDataSource instanceof AsTreeRoot) {
            tDataSource = (AsDataSourceIf<T>) ((AsTreeRoot) tDataSource).createTree(this);
        }

        // Create the tree if the data source is a tree root
        // TODO: How/when is the tree discarded???
        if (tDataSource instanceof AsListTreeRoot) {
            tDataSource = ((AsListTreeRoot) tDataSource).createTree(this);
        }

        AsDataSourceViewportListenerIf<T> tListener = createDataSourceViewportListener(
            (AsListIf<T>) tDataSource, pRequest.getAttributes(), pRequest.getHandle(), pRequest.isMultiSelect(),
            pRequest.getViewId());
        mDataSourceListeners.put(pRequest.getHandle(), (AsHandlerRegistrationIf) tListener);
    }

    /**
     * Create a data source viewport listener. Override if you need an extended version.
     *
     * @param <T> the type
     * @param pList the list
     * @param pAttributes the attributes
     * @param pSubscriptionHandle the subscription handle
     * @param pMultiSelect the multi select
     * @param pViewId the view id
     * @return the as data source viewport listener if
     */
    protected <T> AsDataSourceViewportListenerIf<T> createDataSourceViewportListener(
        AsListIf<T> pList, String[] pAttributes, int pSubscriptionHandle, boolean pMultiSelect, String pViewId) {
        return new AsDataSourceViewportListenerImpl<T>(
            pList, pAttributes, pSubscriptionHandle, pMultiSelect, this, pViewId);
    }

    /**
     * Unsubscribe from a data source.
     *
     * @param pRequest the request
     */
    protected void unsubscribe(AsDataSourceUnsubscribeRequestIf pRequest) {
        AsHandlerRegistrationIf tListener = mDataSourceListeners.remove(pRequest.getHandle());
        if (tListener != null) {
            tListener.removeHandler();
        }
    }

    /**
     * Clear the pending data source events and return them in the shape of a list.
     *
     * @return the list
     */
    @Override
    public List<CwfMessage> dequeuePendingDataSourceEvents() {

        List<CwfMessage> tEvents = new ArrayList<CwfMessage>();
        List<RpcHasDataSourceEventIf> tHasEvents = new ArrayList<RpcHasDataSourceEventIf>();

        if (mPendingDataSourceEvents.size() > 0) {
            synchronized (mPendingDataSourceEvents) {
                tEvents.addAll(mPendingDataSourceEvents);
                mPendingDataSourceEvents.clear();
            }
        }
        if (mPendingHasDataSourceEvents.size() > 0) {
            synchronized (mPendingHasDataSourceEvents) {
                tHasEvents.addAll(mPendingHasDataSourceEvents.values());
                mPendingHasDataSourceEvents.clear();
            }
            for (RpcHasDataSourceEventIf tContainedEvent : tHasEvents) {
                CwfMessage tEvent = tContainedEvent.getEvent();
                if (tEvent != null) {
                    tEvents.add(tEvent);
                }
            }
        }
        return tEvents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSessionData(AsSessionDataIf pSessionData) {
        mSessionData = pSessionData;
        mSessionDataSources.setSessionData(pSessionData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AsSessionDataIf getSessionData() {
        return mSessionData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPendingDataSourceEvent(CwfMessage pEvent) {
        mPendingDataSourceEvents.add(pEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPendingDataSourceEvent(RpcHasDataSourceEventIf pEvent) {
        mPendingHasDataSourceEvents.put(pEvent.getKey(), pEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return mSessionData.getLocale();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void request(AsConnectionIf pConnection, CwfMessage pRequest) {

        CwfRequestNameIf tRequestName = MvcRequestEnum.get(pRequest.getName());
        if (tRequestName instanceof MvcRequestEnum) {
            handleRequest(pConnection, (MvcRequestEnum) tRequestName, pRequest);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    public void getMenu(AsConnectionIf pConnection, CwfMessage pRequest) {

        // create context object from context
        CwfDataIf tContext = CwfDataFactory.create();
        tContext.setProperty(ATTR_TAG_NAME, "Session");
        tContext.setProperty(ATTR_OBJECT_NAME, "Session");

        // Safety measure, set all known parameters first
        tContext.setProperty(ATTR_USER_ID, mSessionData.getUser());
        tContext.setProperty(ATTR_MEMBER_ID , mSessionData.getMember());
        tContext.setProperty(ATTR_ACTING_USER_ID, mSessionData.getLoggedInUser());
        tContext.setProperty(ATTR_LANGUAGE_CODE, mSessionData.getLocale().toString());
        tContext.setProperty(ATTR_PERSPECTIVE, pRequest.getData().getProperty(ATTR_PERSPECTIVE));
        tContext.setProperty(ATTR_IS_ACT_ON_BEHALF, mSessionData.isActOnBehalf());
        tContext.setProperty(ATTR_IS_LOGGED_IN, mSessionData.isLoggedIn());

        // Copy all client session properties
        CwfDataIf tClientSession = pConnection.getSessionData().getClientSession();
        for (Map.Entry<String, String> tEntry : tClientSession.getProperties().entrySet()) {
            tContext.setProperty(tEntry.getKey(), tEntry.getValue());
        }

        AsXmlRefData tSession = new AsXmlRefData(tContext);

        AsDataSourceIf<?> tDataSource = getDataSource(CwfGlobalDataSources.CONTEXTMENUS_ALL, null, null);
        AsContextMenu tItem = ((AsListIf<AsContextMenu>) tDataSource).get("Session");
        CwfDataIf tData = tItem.getMenu(pConnection, tSession);

        CwfMessage tMessage = new CwfMessage(MvcEventEnum.ContextMenuResponse, tData, pRequest.getHandle());
        addPendingDataSourceEvent(tMessage);
    }

    /**
     * Handle a data source request.
     *
     * @param pConnection the connection
     * @param pRequestName the request name
     * @param pRequest the request
     */
    protected void handleRequest(AsConnectionIf pConnection, MvcRequestEnum pRequestName, CwfMessage pRequest) {
        AsDataSourceViewportListenerIf<Object> tListener = getTableDataSourceSubscription(pRequest.getHandle());
        if (tListener != null) {
            handleViewportRequest(pConnection, pRequestName, pRequest, tListener);
            return;
        }
        switch (pRequestName) {
            // Subscribe
            case ListSubscriptionRequest:
                subscribe(new AsDataSourceListSubscriptionRequest(pRequest));
                break;
            case ListItemRequest:
                getListItem(pConnection, new AsDataSourceGetListItemRequest(pRequest));
                break;
            case ListTextRequest:
                getListText(pConnection, new AsDataSourceGetListTextRequest(pRequest));
                break;
            case ListMultipleItemsRequest:
                getListItems(pConnection, new AsDataSourceGetMultipleListItemsRequest(pRequest));
                break;
            case ViewportSubscriptionRequest:
                subscribe(new AsDataSourceViewportSubscriptionRequest(pRequest));
                break;
            case DataSubscriptionRequest:
                subscribe(new AsDataSourceDataSubscriptionRequest(pRequest));
                break;
            case DataUnsubscribeRequest:
                unsubscribe(new AsDataSourceUnsubscribeRequest(pRequest));
                break;
            default :
                break;
        }
    }

    /**
     * Handle a request related to a viewport.
     *
     * @param pConnection the connection
     * @param pRequestName the request name
     * @param pRequest the request
     * @param pListener the listener
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void handleViewportRequest(
        AsConnectionIf pConnection,
        MvcRequestEnum pRequestName, CwfMessage pRequest,
        AsDataSourceViewportListenerIf<Object> pListener) {

        CwfDataIf tData = pRequest.getData();

        if (pListener == null) {
            return;
        }
        switch (pRequestName) {
            case ViewportFilterRequest:
                String tFilterCriteria = pRequest.getData().getProperty(ATTR_FILTER_CRITERIA);
                pListener.setViewportFilter(tFilterCriteria);
                break;

            case ViewportMoveSelectionRequest:
                pListener.moveViewportSelection(tData.getIntProperty(ATTR_MOVE),
                    tData.getBooleanProperty(ATTR_CTRL_KEY_DOWN),
                    tData.getBooleanProperty(ATTR_SHIFT_KEY_DOWN));
                break;

            case ViewportMovePositionRequest:
                pListener.moveViewportPosition(tData.getIntProperty(ATTR_MOVE));
                break;

            case ViewportSetPositionRequest:
                int tPosition = tData.getIntProperty(ATTR_POSITION);
                Boolean tSnapToBottom = tData.getBooleanProperty(ATTR_SNAP_TO_BOTTOM);
                pListener.setViewportPosition(tPosition, tSnapToBottom == null ? false : tSnapToBottom.booleanValue());
                break;

            case ViewportSetSelectionRequest: {
                String tKey = tData.getProperty(ATTR_SELECTED_OBJECT);
                int tIndex = tData.getIntProperty(ATTR_SELECTED_INDEX);
                pListener.setViewportSelection(tKey, tIndex,
                    tData.getBooleanProperty(ATTR_CTRL_KEY_DOWN),
                    tData.getBooleanProperty(ATTR_SHIFT_KEY_DOWN));
                break;
            }

            case ViewportSetExpandedRequest: {
                Boolean tExpanded = tData.getBooleanProperty(ATTR_EXPANDED);
                String tKey = tData.getProperty(ATTR_SELECTED_OBJECT);
                pListener.setExpanded(tKey, tExpanded);
                break;
            }

            case ViewportSetSizeRequest:
                int tSize = tData.getIntProperty(ATTR_SIZE);
                pListener.setViewportSize(tSize);
                break;

            case ViewportSortRequest:
                int tAttributeIndex = tData.getIntProperty(ATTR_INDEX);
                SortOrder tSortOrder = SortOrder.valueOf(tData.getProperty(ATTR_SORT_ORDER));
                pListener.setViewportSortCriteria(tAttributeIndex, tSortOrder);
                break;

            case ViewportSetQueryRequest:
                // Use the handle as filter string to uniquely identify this query
                CwfDataIf tFilterData = CwfDataFactory.create();
                tFilterData.setProperty(ATTR_HANDLE, pRequest.getHandle());
                AsRequestFilter tFilter = new AsRequestFilter(tFilterData);
                String tSortCriteria = tData.getProperty(ATTR_SORT_CRITERIA);
                RpcSortCriteriaIf tSort = tSortCriteria == null ? null :
                    RpcAttributeSortCriteria.fromString(tSortCriteria);
                AsSortIf tQuerySortOrder = tSort == null ? null :
                    new AsAttributeValueSort(
                        pListener.getDataSource().getItemClass(),  new RpcSortCriteriaIf[] {tSort});
                AsListIf tDataSource = (AsListIf) getRequestDataSource(
                    pListener.getDataSource().getDataSourceId(), tFilter, tQuerySortOrder);
                populateDataSourceFromQuery(pConnection, tDataSource,
                    tData.getObject(ATTR_QUERY), pRequest.getHandle());
                pListener.setDataSource(tDataSource);
                break;

            case ViewportGetContextMenuRequest:
                createContextMenu(pConnection, pListener, pRequest);
                break;
            case ViewportGetContextRequest:
                getObject(pConnection, pListener, pRequest);
                break;
            case ViewportAddObjectRequest:
                Object tAddedObject = pConnection.getRequestService().transform(
                    pConnection, tData.getObject(ATTR_VALUE), pConnection.getLocale());
                pListener.addObject(tAddedObject);
                break;
            case ViewportUpdateObjectRequest:
                Object tUpdatedObject = pConnection.getRequestService().transform(
                    pConnection, tData.getObject(ATTR_VALUE), pConnection.getLocale());
                pListener.updateObject(tUpdatedObject);
                break;
            case ViewportDeleteObjectRequest:
                Object tDeletedObject = pConnection.getRequestService().transform(
                    pConnection, tData.getObject(ATTR_VALUE), pConnection.getLocale());
                pListener.deleteObject(tDeletedObject);
                break;
            case ViewportUnsubscribeRequest:
                unsubscribe(new AsDataSourceUnsubscribeRequest(pRequest));
                break;
            default:;
        }

    }

    /**
     * Gets the object.
     *
     * @param pConnection the connection
     * @param pListener the listener
     * @param pRequest the request
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void getObject(AsConnectionIf pConnection, AsDataSourceViewportListenerIf<?> pListener,
            CwfMessage pRequest) {

        String tKey = pRequest.getData().getProperty(ATTR_KEY);
        AsListIf tList = (AsListIf) pListener.getDataSource();
        Object tItem = tList.get(tKey);

        CwfDataIf tResponse = CwfDataFactory.create();

        // Get the item data, run all custom getters on it, and populate it in the response
        CwfDataIf tItemData = getItemData(pConnection, tItem, true);
        if (tItemData != null) {
            tItemData.setProperty(ATTR_MODEL_LABEL, tList.getText(tItem, this));
        }
        tResponse.setObject(ATTR_CONTEXT_OBJECT, tItemData);
        CwfMessage tMessage = new CwfMessage(MvcEventEnum.ContextObjectResponse, tResponse, pRequest.getHandle());
        addPendingDataSourceEvent(tMessage);
    }

    /**
     * Create a context menu for the given request
     * TODO: This should really be recursive, since menus can have an arbitrary depth.
     *
     * @param pConnection the connection
     * @param pListener the listener
     * @param pRequest the request
     */
    protected void createContextMenu(
        AsConnectionIf pConnection, AsDataSourceViewportListenerIf<?> pListener, CwfMessage pRequest) {

        String tPerspective = pRequest.getData().getProperty(ATTR_PERSPECTIVE);
        String tView = pRequest.getData().getProperty(ATTR_VIEW);
        String tKey = pRequest.getData().getProperty(ATTR_KEY);
        Integer tRowIndex = pRequest.getData().getIntProperty(ATTR_ROW_INDEX);
        Integer tColumnIndex = pRequest.getData().getIntProperty(ATTR_COLUMN_INDEX);

        Object tItem = ((AsListIf<?>) pListener.getDataSource()).get(tKey);
        if (tItem == null) {
            return;
        }

        Set<String> tSelectedKeys = pListener.getSelectedKeys();
        CwfDataIf tResponse = CwfDataFactory.create();

        if (tSelectedKeys.contains(tKey) && tSelectedKeys.size() > 1) {
            // TODO: Combined context menu
            // Get the context menu defined for the item's class, plus the current perspective and view
            String tItemClassName = getItemClassName(tItem) + "[]";
            AsContextMenu tMenu = getContextMenu(tPerspective, tView, tItemClassName);

            // If a context menu is found, populate its items in the response
            if (tMenu != null && !tMenu.getMenuItems().isEmpty()) {
                CwfDataIf tArray = CwfDataFactory.create();
                List<Object> tItems = new ArrayList<Object>();
                tArray.setProperty(ATTR_OBJECT_NAME, tItemClassName);

                // Get the item datas, run all custom getters on them, and populate them in the array
                for (String tItemKey : pListener.getSelectedKeys()) {
                    tItem = ((AsListIf<?>) pListener.getDataSource()).get(tItemKey);
                    if (tItem != null) {
                        tItems.add(tItem);

                        CwfDataIf tItemData = getItemData(pConnection, tItem, false);
                        if (tItemData == null) {
                            tItemData = CwfDataFactory.create();
                        }
                        tArray.addObject(ATTR_ARRAY_ITEMS, tItemData);
                    }
                }
                // Set the array in the response
                tResponse.setObject(ATTR_CONTEXT_OBJECT, tArray);

                for (AsMenuItem tMenuItem : tMenu.getMenuItems()) {
                    if (tMenuItem.isForPerspective(tPerspective)) {
                        if (tMenuItem.isAccessAllowed(pConnection) &&
                            tMenuItem.isIncluded(pConnection, tItems.toArray())) {
                            tResponse.addObject(TAG_MENUITEM, tMenuItem.getValues());
                        }
                    }
                }
                for (AsContextMenu tSubMenu : tMenu.getSubMenus(tPerspective)) {
                    // Copy the menu and remove all its items since we need to filter them one by one
                    CwfDataIf tSubMenuData = CwfDataFactory.copy(tSubMenu.getValues());
                    while (tSubMenuData.getObjectList(TAG_MENUITEM).size() > 0) {
                        tSubMenuData.removeObject(TAG_MENUITEM, 0);
                    }
                    tResponse.addObject(TAG_MENU, tSubMenuData);
                    for (AsMenuItem tSubItem : tSubMenu.getMenuItems()) {
                        if (tSubItem.isForPerspective(tPerspective)) {
                            if (tSubItem.isAccessAllowed(pConnection) &&
                                tSubItem.isIncluded(pConnection, tItems.toArray())) {
                                tSubMenuData.addObject(TAG_MENUITEM, tSubItem.getValues());
                            }
                        }
                    }
                }
            }
            else {
                // TODO: Remove
                System.out.println("Context menu not defined for object of type " + tItemClassName);
            }
        }
        else {
            // Select the clicked item
            pListener.setViewportSelection(tKey, tRowIndex, false, false);

            // Get the context menu defined for the item's class, plus the current perspective and view
            String tItemClassName = getItemClassName(tItem);
            AsContextMenu tMenu = getContextMenu(tPerspective, tView, tItemClassName);


            // If a context menu is found, populate its items in the response
            if (tMenu != null && !tMenu.getMenuItems().isEmpty()) {

                // Get the item data, run all custom getters on it, and populate it in the response
                CwfDataIf tItemData = getItemData(pConnection, tItem, false);
                if (tItemData == null) {
                    tItemData = CwfDataFactory.create();
                }
                tResponse.setObject(ATTR_CONTEXT_OBJECT, tItemData);

                for (AsMenuItem tMenuItem : tMenu.getMenuItems()) {
                    if (tMenuItem.isForPerspective(tPerspective)) {
                        if (tMenuItem.isAccessAllowed(pConnection) &&
                            tMenuItem.isIncluded(pConnection, tItem)) {
                            tResponse.addObject(TAG_MENUITEM, tMenuItem.getValues());
                        }
                    }
                }
                for (AsContextMenu tSubMenu : tMenu.getSubMenus(tPerspective)) {
                    // Copy the menu and remove all its items since we need to filter them one by one
                    CwfDataIf tSubMenuData = CwfDataFactory.copy(tSubMenu.getValues());
                    while (tSubMenuData.getObjectList(TAG_MENUITEM).size() > 0) {
                        tSubMenuData.removeObject(TAG_MENUITEM, 0);
                    }
                    tResponse.addObject(TAG_MENU, tSubMenuData);
                    for (AsMenuItem tSubItem : tSubMenu.getMenuItems()) {
                        if (tSubItem.isForPerspective(tPerspective)) {
                            if (tSubItem.isAccessAllowed(pConnection) &&
                                tSubItem.isIncluded(pConnection, tItem)) {
                                tSubMenuData.addObject(TAG_MENUITEM, tSubItem.getValues());
                            }
                        }
                    }
                }
            }
            else {
                // TODO: Remove
                System.out.println("Context menu not defined for object of type " + tItemClassName);
            }

            // Add possible filter expressions (Not applicable for trees)
            if (tColumnIndex >= 0) {
                List<CwfDataIf> tExpressions = pListener.createFilterExpressions(tKey, tColumnIndex);
                if (tExpressions != null) {
                    tResponse.setObject(ATTR_FILTER, tExpressions);
                }
            }
        }

        CwfMessage tMessage = new CwfMessage(MvcEventEnum.ContextMenuResponse, tResponse, pRequest.getHandle());
        addPendingDataSourceEvent(tMessage);
    }

    /**
     * Get the context menu for the given keys.
     *
     * @param pPerspectiveId the perspective id
     * @param pViewId the view id
     * @param pItemClassName the item class name
     * @return the context menu
     */
    protected AsContextMenu getContextMenu(String pPerspectiveId, String pViewId, String pItemClassName) {
        AsDataSourceIf<AsContextMenu> tContextMenusDataSource =
            getDataSource(CwfGlobalDataSources.CONTEXTMENUS_ALL, null, null);
        AsListIf<AsContextMenu> tContextMenus = (AsListIf<AsContextMenu>) tContextMenusDataSource;
        // Use a most -> least significant match scheme
        String[] tKeysToTest = new String[] {
            pPerspectiveId + "." + pViewId + "." + pItemClassName,
            pPerspectiveId + ".*." + pItemClassName,
            "*." + pViewId + "." + pItemClassName,
            "*.*." + pItemClassName,
            pItemClassName
        };
        for (String tKey : tKeysToTest) {
            AsContextMenu tMenu = tContextMenus.get(tKey);
            if (tMenu != null) {
                return tMenu;
            }
        }
        return null;
    }

    /**
     * Assemble the context menu item key for the given criteria.
     *
     * @param pPerspectiveId the perspective id
     * @param pViewId the view id
     * @param pItemClassName the item class name
     * @return the context menu item key
     */
    protected String getContextMenuItemKey(String pPerspectiveId, String pViewId, String pItemClassName) {
        StringBuilder tBuilder = new StringBuilder();
        if (pPerspectiveId != null && !pPerspectiveId.isEmpty()) {
            tBuilder.append(pPerspectiveId).append(".");
        }
        else {
            tBuilder.append("*.");
        }
        if (pViewId != null && !pViewId.isEmpty()) {
            tBuilder.append(pViewId).append(".");
        }
        else {
            tBuilder.append("*.");
        }
        tBuilder.append(pItemClassName);
        return tBuilder.toString();
    }

    /**
     * Get the "real" object from the given object.
     *
     * @param pItem the item
     * @return the item
     */
    protected Object getItem(Object pItem) {
        if (pItem instanceof AsTreeNode<?>) {
            return ((AsTreeNode<?>) pItem).getItem();
        }
        if (pItem instanceof AsListTreeNode) {
            return ((AsListTreeNode) pItem).getItem();
        }
        return pItem;
    }

    /**
     * Get the class of the given object.
     *
     * @param pItem the item
     * @return the item class
     */
    protected Class<?> getItemClass(Object pItem) {
        if (pItem instanceof AsTreeNode<?>) {
            return ((AsTreeNode<?>) pItem).getItem().getClass();
        }
        if (pItem instanceof AsListTreeNode) {
            return ((AsListTreeNode) pItem).getItem().getClass();
        }
        return pItem.getClass();
    }

    /**
     * Get the simple class name of the given object.
     *
     * @param pItem the item
     * @return the item class name
     */
    @SuppressWarnings("deprecation")
	protected String getItemClassName(Object pItem) {
        if (pItem instanceof AsTreeNodeFolder) {
            return ((AsTreeNodeFolder) pItem).getItem().getLabel();
        }
        if (pItem instanceof AsTreeNode<?>) {
            return getItemClassName(((AsTreeNode<?>) pItem).getItem());
        }
        if (pItem instanceof AsListTreeNode) {
            return getItemClassName(((AsListTreeNode) pItem).getItem());
        }
        // TODO: Is this necessary?
        if (pItem instanceof AsXmlRefData &&
            pItem.getClass().getSimpleName().equals(AsXmlRefData.class.getSimpleName())) {
            return ((AsXmlRefData) pItem).getTagName();
        }
        return As.getTypeName(pItem.getClass());
    }

    /**
     * Get the item transformed to client format.
     *
     * @param pConnection the connection
     * @param pItem the item
     * @param pIncludeParent the include parent
     * @return the item data
     */
    protected CwfDataIf getItemData(AsConnectionIf pConnection, Object pItem, boolean pIncludeParent) {
        if (pItem == null) {
            return null;
        }
        if (pItem instanceof AsTreeNodeFolder) {
            return getItemData(pConnection, ((AsTreeNodeFolder) pItem).getItem().getParent(), pIncludeParent);
        }
        if (pItem instanceof AsTreeNode<?>) {
            AsTreeNode<?> tTreeNode = (AsTreeNode<?>) pItem;
            CwfDataIf tNodeData = getItemData(pConnection, tTreeNode.getItem(), false);
            tNodeData.setProperty(ATTR_KEY, tTreeNode.getPath());
            tNodeData.setProperty(ATTR_TEXT, tTreeNode.getLabel());
            if (pIncludeParent) {
                AsTreeNodeIf<?> tNode = tTreeNode.getParent();
                while (tNode != null) {
                    CwfDataIf tParent = getItemData(pConnection, tNode, false);
                    if (tParent != null) {
                        tParent.setProperty(ATTR_KEY, tNode.getPath());
                        tParent.setProperty(ATTR_TEXT, tNode.getLabel());
                        tNodeData.addObject(ATTR_PARENT_CONTEXT_OBJECT, tParent);
                    }
                    tNode = ((AsTreeNodeIf<?>) tNode).getParent();
                }
            }
            return tNodeData;
        }
        if (pItem instanceof AsListTreeNode) {
            AsListTreeNode tTreeNode = (AsListTreeNode) pItem;
            CwfDataIf tNodeData = getItemData(pConnection, tTreeNode.getItem(), false);
            if (tNodeData != null) {
                tNodeData.setProperty(ATTR_KEY, tTreeNode.getPath());
                tNodeData.setProperty(ATTR_TEXT, tTreeNode.getLabel());
                if (pIncludeParent) {
                    AsTreeNodeIf<?> tNode = tTreeNode.getParent();
                    while (tNode != null) {
                        CwfDataIf tParent = getItemData(pConnection, tNode, false);
                        if (tParent != null) {
                            tParent.setProperty(ATTR_KEY, tNode.getPath());
                            tParent.setProperty(ATTR_TEXT, tNode.getLabel());
                            tNodeData.addObject(ATTR_PARENT_CONTEXT_OBJECT, tParent);
                        }
                        tNode = ((AsTreeNodeIf<?>) tNode).getParent();
                    }
                }
            }
            return tNodeData;
        }
        if (pItem instanceof AsMapRefData) {
            return ((AsMapRefData) pItem).getValues();
        }
        return (CwfDataIf) pConnection.getRequestService().transform(pConnection, pItem);
    }

    /**
     * Populate the data source from the given query.
     *
     * @param pConnection the connection
     * @param pDataSource the data source
     * @param pQuery the query
     * @param pRequestHandle the request handle
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void populateDataSourceFromQuery(
        AsConnectionIf pConnection, AsListIf pDataSource, CwfDataIf pQuery, int pRequestHandle) {

        // Inform the client that the search has started
        CwfDataIf tData = CwfDataFactory.create(MvcEventEnum.ViewportQueryInitEvent);
        addPendingDataSourceEvent(new CwfMessage(tData, pRequestHandle));

        pDataSource.clear();
        CwfMessage tRequest = new CwfMessage(MvcModelNames.ServerRequest, (CwfDataIf) pQuery, -1);
        Object tResponse = pConnection.getRequestService().sendLocal(pConnection, tRequest);

        // If the query failed, notify the client
        ClientQueryResponse tClientResponse = createClientQueryResponse(tResponse);
        if (tClientResponse != null && tClientResponse.mStatusCode != As.STATUS_CODE_OK) {
            tData = CwfDataFactory.create(MvcEventEnum.ViewportErrorEvent);
            tData.setProperty(ATTR_STATUS_CODE, tClientResponse.mStatusCode);
            tData.setProperty(ATTR_STATUS_MESSAGE, tClientResponse.mStatusMessage);
            addPendingDataSourceEvent(new CwfMessage(tData, pRequestHandle));
            return;
        }

        // Now populate the response into the data source
        Object tItems = findItems(tResponse, pDataSource.getItemClass());
        if (tItems != null) {
            for (int i = 0; i < Array.getLength(tItems); i++) {
                Object tItem = Array.get(tItems, i);
                if (tItem != null) {
                    pDataSource.add(tItem);
                }
            }
        }

        // Inform the client that the search has completed
        tData = CwfDataFactory.create(MvcEventEnum.ViewportQueryCompleteEvent);
        tData.setProperty(ATTR_SIZE, pDataSource.size());
        addPendingDataSourceEvent(new CwfMessage(tData, pRequestHandle));
    }

    /**
     * Create a simple client response from an actual query response.
     *
     * @param pQueryResponse the actual query response from the back-end
     * @return always returns {@code null}, subclasses must override this
     * method and construct a proper response
     */
    protected ClientQueryResponse createClientQueryResponse(Object pQueryResponse) {
        return null;
    }

    /**
     * Attempt to find the data to populate based on its class.
     *
     * @param pObject the object
     * @param pItemClass the item class
     * @return the object
     */
    protected Object findItems(Object pObject, Class<?> pItemClass) {
        for (Field tField : pObject.getClass().getFields()) {
            // If the field is an array, look at its type
            if (tField.getType().isArray()) {
                if (tField.getType().getComponentType().isAssignableFrom(pItemClass)) {
                    try {
                        return tField.get(pObject);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Could not retrieve array element", e);
                    }
                }
                // Array of wrong type, skip this field
                continue;
            }
            // Skip enumerations as they will lead to infinite recursion
            if (Enum.class.isAssignableFrom(tField.getType())) {
                continue;
            }
            // If the field is an object, try to recursively find an array in it
            Package tPackage = tField.getType().getPackage();
            if (tPackage != null) {
                try {
                    Object tSubObject = tField.get(pObject);
                    if (tSubObject != null) {
                        Object tArray = findItems(tSubObject, pItemClass);
                        if (tArray != null) {
                            return tArray;
                        }
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException("Could not retrieve subobject", e);
                }
            }

        }
        return null;
    }

    /**
     * Gets the table data source subscription.
     *
     * @param <T> the type
     * @param pSubscriptionHandle the subscription handle
     * @return the table data source subscription
     */
    @SuppressWarnings("unchecked")
    protected <T> AsDataSourceViewportListenerIf<T> getTableDataSourceSubscription(int pSubscriptionHandle) {
        AsHandlerRegistrationIf tListener = mDataSourceListeners.get(pSubscriptionHandle);
        if (tListener instanceof AsDataSourceViewportListenerIf) {
            return (AsDataSourceViewportListenerIf<T>) tListener;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> AsDataSourceIf<T> getDataSource(AsDataSourceIf<T> pSource, RpcSortCriteriaIf[] pSort) {
        return getDataSource(
            pSource.getDataSourceId(),
            pSource.getFilter() != null ? pSource.getFilter().toString() : null,
            pSort);
    }

    /**
     * Gets the data source.
     *
     * @param <T> the type
     * @param pDataSourceId the data source id
     * @param pFilterExpression the filter expression
     * @param pSort the sort
     * @return the data source
     */
    public <T> AsDataSourceIf<T> getDataSource(
        String pDataSourceId, String pFilterExpression, RpcSortCriteriaIf[] pSort) {

        // Do the ID vs type trick first, but only if the passed ID is NOT a true data source ID
        String tActualDataSourceId = pDataSourceId;
        if (!mSessionDataSources.isRegisteredDataSourceId(pDataSourceId)) {
            tActualDataSourceId = As.getGlobalDataSources().getDataSourceId(pDataSourceId);
        }

        AsDataSourceIf<T> tDataSource = mSessionDataSources.getDataSource(tActualDataSourceId, null, null);
        if (tDataSource == null) {
            return null;
        }

        AsFilterIf<T> tFilter = null;
        // TODO: Expressions starting with "{" are filters used by query data sources where the filter is
        // only used to identify the data source
        if (pFilterExpression != null && pFilterExpression.startsWith("{")) {
            tFilter = new AsIncludeAllFilter<T>(pFilterExpression);
        }
        if (tFilter == null) {
            tFilter = (pFilterExpression == null || pFilterExpression.length() == 0) ?
                null : AsAttributeValueFilter.create(tDataSource.getItemClass(), pFilterExpression);
        }
        AsAttributeValueSort<T> tSort = pSort == null ?
            null :  new AsAttributeValueSort<T>(tDataSource.getItemClass(), pSort);
        return mSessionDataSources.getDataSource(tActualDataSourceId, tFilter, tSort);
    }

    /**
     * Gets the data source.
     *
     * @param <T> the type
     * @param pDataSourceId the data source id
     * @param pFilter the filter
     * @return the data source
     */
    @Override
    public <T> AsDataSourceIf<T> getDataSource(String pDataSourceId, AsFilterIf<T> pFilter) {
        return mSessionDataSources.getDataSource(pDataSourceId, pFilter, null);
    }

    /**
     * Gets the request data source.
     *
     * @param <T> the type
     * @param pDataSourceId the data source id
     * @param pRequestFilter the request filter
     * @param pSort the sort
     * @return the request data source
     */
    protected <T> AsDataSourceIf<T> getRequestDataSource(
        String pDataSourceId, AsFilterIf<T> pRequestFilter, AsSortIf<T> pSort) {
        AsDataSourceIf<T> tDataSource = mSessionDataSources.getDataSource(pDataSourceId, null, pSort);
        AsSortIf<T> tSort = pSort != null ? pSort : tDataSource.getSort();
        return mSessionDataSources.getDataSource(pDataSourceId, pRequestFilter, tSort);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        mSessionDataSources.destroy();
    }

    /**
     * Simple class holding a status summary of the actual query response.
     */
    protected class ClientQueryResponse {

        /** The Status code. */
        public int mStatusCode;

        /** The Status message. */
        public String mStatusMessage;

    }

}
