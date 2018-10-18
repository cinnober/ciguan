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
package com.cinnober.ciguan;

import java.util.Locale;

import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.request.AsRequestServiceIf;
import com.cinnober.ciguan.transport.AsTransportServiceIf;

/**
 * Interface defining functionality associated with an application server connection.
 *
 * @author jorgen.ekroth, Cinnober Financial Technology
 */
public interface AsConnectionIf {

    /**
     * Get the model service.
     *
     * @return the data source service
     */
    AsDataSourceServiceIf getDataSourceService();

    /**
     * Get the request service.
     *
     * @return the request service
     */
    AsRequestServiceIf getRequestService();

    /**
     * Get the transport service.
     *
     * @return the transport service
     */
    AsTransportServiceIf getTransportService();

    /**
     * Set member and user details.
     *
     * @param pSessionData the new session data
     */
    void setSessionData(AsSessionDataIf pSessionData);

    /**
     * Gets the locale.
     *
     * @return the locale
     */
    Locale getLocale();

    /**
     * Change language.
     *
     * @param pLocale the locale
     * @return true if locale changed
     */
    boolean setLocale(String pLocale);

    /**
     * Gets the session data.
     *
     * @return the session data
     */
    AsSessionDataIf getSessionData();

    /**
     * Sets the connection data.
     *
     * @param <T> the generic type
     * @param pType the class
     * @param pObject the object
     */
    <T> void setConnectionData(Class<T> pType, T pObject);

    /**
     * Gets the connection data.
     *
     * @param <T> the type
     * @param pType the class
     * @return the connection data
     */
    <T> T getConnectionData(Class<T> pType);

    /**
     * Gets the session id.
     *
     * @return the session id
     */
    String getSessionId();

    /**
     * Initializes the connection.
     *
     * @param pSessionId the session id
     * @param pLocale the session locale
     */
    void init(String pSessionId, Locale pLocale);

    /**
     * Invalidate the connection, called when the HTTP session is invalidated.
     */
    void invalidate();

}
