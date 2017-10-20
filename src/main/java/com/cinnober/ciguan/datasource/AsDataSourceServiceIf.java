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

import java.util.List;
import java.util.Locale;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsHandlerRegistrationIf;
import com.cinnober.ciguan.AsSessionDataIf;
import com.cinnober.ciguan.data.CwfMessage;

/**
 * Interface defining services related to data sources.
 *
 * @author jorgen.ekroth, Cinnober Financial Technology
 */
public interface AsDataSourceServiceIf {

    /**
     * Dequeue all pending client data source events.
     *
     * @return the list
     */
    List<CwfMessage> dequeuePendingDataSourceEvents();
    
    /**
     * Reset all data source subscriptions.
     */
    public void resetDataSourceSubscriptions();
    
    /**
     * Set member and user details.
     *
     * @param pSessionData the new session data
     */
    void setSessionData(AsSessionDataIf pSessionData);    
    
    /**
     * Get the session data.
     *
     * @return the session data
     */
    AsSessionDataIf getSessionData();

    /**
     * Add a client data source event to the queue.
     *
     * @param pEvent the event
     */
    void addPendingDataSourceEvent(CwfMessage pEvent);
    
    /**
     * Add a contained client data source event to the queue.
     *
     * @param pEvent the event
     */
    void addPendingDataSourceEvent(RpcHasDataSourceEventIf pEvent);

    /**
     * Gets the data source.
     *
     * @param <T> the type
     * @param pSource the source
     * @param pSort the sort
     * @return new source with sorting
     */
    <T> AsDataSourceIf<T> getDataSource(AsDataSourceIf<T> pSource, RpcSortCriteriaIf[] pSort);

    /**
     * Gets the data source.
     *
     * @param <T> the type
     * @param pDataSourceId the data source id
     * @param pFilter the filter
     * @return new source with sorting
     */
    <T> AsDataSourceIf<T> getDataSource(String pDataSourceId, AsFilterIf<T> pFilter);

    /**
     * Get the current locale.
     *
     * @return the locale
     */
    Locale getLocale();

    /**
     * Process an incoming client request.
     *
     * @param pConnection the connection
     * @param pRequest the request
     */
    void request(AsConnectionIf pConnection, CwfMessage pRequest);

    /**
     * Get menu items.
     *
     * @param pConnection the connection
     * @param pMessage the message
     * @return the menu
     */ 
    void getMenu(AsConnectionIf pConnection, CwfMessage pMessage);

    /**
     * Get handler registration for subscription handle.
     *
     * @param pHandle the subscription handle
     * @return the handler registration
     */
    AsHandlerRegistrationIf getHandlerRegistration(Integer pHandle);

    /**
     * Destroys all data sources.
     */
    void destroy();
    
}
