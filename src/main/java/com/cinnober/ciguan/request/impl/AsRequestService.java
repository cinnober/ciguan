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
package com.cinnober.ciguan.request.impl;

import java.util.Locale;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.AsSessionDataIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.CwfMessageIf;
import com.cinnober.ciguan.client.impl.MvcModelNames;
import com.cinnober.ciguan.data.AsResponse;
import com.cinnober.ciguan.data.AsStatus;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.request.AsRequestServiceIf;
import com.cinnober.ciguan.request.AsResponseCallbackIf;
import com.cinnober.ciguan.service.AsServiceHandlerIf;
import com.cinnober.ciguan.service.impl.AsServiceInvocationException;
import com.cinnober.ciguan.transport.AsRequestTransformerIf;

/**
 * Implementation of the request service layer.
 */
public class AsRequestService implements AsRequestServiceIf {

    /** The transformer. */
    protected final AsRequestTransformerIf mTransformer;

    /**
     * Instantiates a new request service.
     */
    public AsRequestService() {
        mTransformer = As.getBeanFactory().create(AsRequestTransformerIf.class);
    }

    @Override
    public CwfMessage send(AsConnectionIf pConnection, CwfMessageIf pMessage) {
        // Send the request and get the TEAI response
        Object tResponse = sendLocal(pConnection, pMessage);

        if (tResponse != null) {
            // Transform and return the response to the client
            CwfDataIf tData = (CwfDataIf) mTransformer.transform(pConnection, tResponse);
            CwfMessage tResponseMessage = new CwfMessage(MvcModelNames.ServerResponse, tData, pMessage.getHandle());
            return tResponseMessage;
        }
        return null;
    }

    @Override
    public Object sendLocal(AsConnectionIf pConnection, CwfMessageIf pMessage) {
        // Create the request message
        Object tRequest = mTransformer.transform(pConnection, pMessage.getData());
        return sendLocal(pConnection, pMessage.getHandle(), tRequest);
    }

    @Override
    public Object sendLocal(AsConnectionIf pConnection, int pHandle, Object pMessage) {
        // Attempt to execute a local service if one exists
        try {
            Object tResponse = AsServiceHandlerIf.Singleton.get().service(pConnection, pMessage);
            if (tResponse != null) {
                return tResponse;
            }
        }
        catch (AsServiceInvocationException e) {
            AsLoggerIf.Singleton.get().logThrowable("Exception in the service", e);
            return new AsResponse("Exception", new AsStatus(As.STATUS_CODE_NOK, "Exception in the service"));
        }
        // No matching service
        return null;
    }

    @Override
    public void send(final AsConnectionIf pConnection, final CwfMessageIf pMessage,
        final AsResponseCallbackIf<CwfMessageIf> pCallback) {
        // Send the request and get the TEAI response
        sendLocal(pConnection, pMessage, new AsResponseCallbackIf<Object>() {
            @Override
            public void onResponse(Object pResponse) {
                if (pResponse != null) {
                    // Transform and return the response to the client
                    CwfDataIf tData = mTransformer.transform(pConnection, pResponse);
                    CwfMessage tResponseMessage = new CwfMessage(
                        MvcModelNames.ServerResponse, tData, pMessage.getHandle());
                    pCallback.onResponse(tResponseMessage);
                }
            }
        });
    }

    @Override
    public void sendLocal(AsConnectionIf pConnection, CwfMessageIf pMessage,
        final AsResponseCallbackIf<Object> pCallback) {
        // Create the request message
        Object tRequest = mTransformer.transform(pConnection, pMessage.getData());
        sendLocal(pConnection, pMessage.getHandle(), tRequest, pCallback);
    }

    @Override
    public void sendLocal(AsConnectionIf pConnection, int pHandle, Object pMessage,
        final AsResponseCallbackIf<Object> pCallback) {
        // Attempt to execute a local service if one exists
        try {
            Object tResponse = AsServiceHandlerIf.Singleton.get().service(pConnection, pMessage);
            if (tResponse != null) {
                pCallback.onResponse(tResponse);
            }
        }
        catch (AsServiceInvocationException e) {
            AsLoggerIf.Singleton.get().logThrowable("Exception in the service", e);
            pCallback.onResponse(new AsResponse("Exception",
                new AsStatus(As.STATUS_CODE_NOK, "Exception in the service")));
        }
        // No matching service
        pCallback.onResponse(null);
    }

    @Override
    public void setSessionData(AsSessionDataIf pSessionData) {
        // No action
    }

    @Override
    public CwfDataIf transform(AsConnectionIf pConnection, Object pMessage) {
        return mTransformer.transform(pConnection, pMessage);
    }

    @Override
    public Object transform(AsConnectionIf pConnection, CwfDataIf pMessage, Locale pLocale) {
        return mTransformer.transform(pConnection, pMessage);
    }

}
