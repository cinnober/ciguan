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
package com.cinnober.ciguan.request;

import java.util.Locale;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsSessionDataIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.data.CwfMessage;

/**
 * Interface defining request/response related functionality.
 *
 * @author jorgen.ekroth, Cinnober Financial Technology
 */
public interface AsRequestServiceIf {
    
    /**
     * Send a request.
     *
     * @param pConnection the connection
     * @param pMessage the message
     * @return the cwf message
     */
    CwfMessage send(AsConnectionIf pConnection, CwfMessage pMessage);

    /**
     * Send a request and return the response via a callback.
     *
     * @param pConnection the connection
     * @param pMessage the message
     * @param pCallback the callback to use for passing the response
     */
    void send(AsConnectionIf pConnection, CwfMessage pMessage, AsResponseCallbackIf<CwfMessage> pCallback);
    
    /**
     * Send a CWF request that should not have its response sent back to the client.
     *
     * @param pConnection the connection
     * @param pMessage the message
     * @return the object
     */
    Object sendLocal(AsConnectionIf pConnection, CwfMessage pMessage);
    
    /**
     * Send a CWF request that should not have its response sent back to the client.
     *
     * @param pConnection the connection
     * @param pMessage the message
     * @param pCallback the callback to use for passing the response
     */
    void sendLocal(AsConnectionIf pConnection, CwfMessage pMessage, AsResponseCallbackIf<Object> pCallback);
    
    /**
     * Send a server request that should not have its response sent back to the client.
     *
     * @param pConnection the connection
     * @param pHandle the client request handle
     * @param pMessage the message
     * @return the object
     */
    Object sendLocal(AsConnectionIf pConnection, int pHandle, Object pMessage);
    
    /**
     * Send a server request that should not have its response sent back to the client.
     *
     * @param pConnection the connection
     * @param pHandle the client request handle
     * @param pMessage the message
     * @param pCallback the callback to use for passing the response
     */
    void sendLocal(AsConnectionIf pConnection, int pHandle, Object pMessage, AsResponseCallbackIf<Object> pCallback);
    
    /**
     * Set member and user details.
     *
     * @param pSessionData the new session data
     */
    void setSessionData(AsSessionDataIf pSessionData);

    /**
     * Transform a server side object into client format.
     *
     * @param pConnection the connection
     * @param pObject the object
     * @return the cwf data if
     */
    CwfDataIf transform(AsConnectionIf pConnection, Object pObject);
    
    /**
     * Transform a client side object into server format.
     *
     * @param pConnection the connection
     * @param pMessage the message
     * @param pLocale the locale
     * @return the object
     */
    Object transform(AsConnectionIf pConnection, CwfDataIf pMessage, Locale pLocale);
    
}
