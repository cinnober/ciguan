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
package com.cinnober.ciguan.transport.plugin;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.impl.MvcModelNames;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.plugin.impl.AsPluginBase;
import com.cinnober.ciguan.transport.AsTransportServicePluginIf;

/**
 * Base class for application server transport plugins.
 */
public abstract class AsTransportPlugin extends AsPluginBase
    implements AsTransportServicePluginIf, MvcModelAttributesIf {

    /**
     * {@inheritDoc}
     *
     * Never called, transport plugins are not threads.
     */
    @Override
    public final void run() {
        // Never called, transport plugins are not threads
    }

    /**
     * {@inheritDoc}
     *
     * Never called, transport plugins are not threads.
     */
    @Override
    public final void launch() {
        // Never called, transport plugins are not threads
    }

    /**
     * {@inheritDoc}
     *
     * No implementation is provided by default, override if needed.
     */
    @Override
    public void reset(AsConnectionIf pConnection) {
        // No action, override if needed
    }

    /**
     * Send an Ok response to the client.
     *
     * @param pConnection the connection
     * @param pHandle the handle
     */
    protected void sendOkResponse(AsConnectionIf pConnection, int pHandle) {
        CwfDataIf tRsp = CwfDataFactory.create(MvcModelNames.ServerResponse);
        tRsp.setProperty(ATTR_STATUS_CODE, As.STATUS_CODE_OK);
        CwfMessage tResponse = new CwfMessage(tRsp, pHandle);
        pConnection.getTransportService().addClientMessage(tResponse);
    }

    /**
     * Send an error response to the client.
     *
     * @param pConnection the connection
     * @param pHandle the handle
     * @param pErrorCode the error code
     * @param pErrorMessage the error message
     */
    protected void sendErrorResponse(AsConnectionIf pConnection, int pHandle,
        int pErrorCode, String pErrorMessage) {
        CwfDataIf tRsp = CwfDataFactory.create(MvcModelNames.ServerResponse);
        tRsp.setProperty(ATTR_STATUS_CODE, pErrorCode);
        tRsp.setProperty(ATTR_STATUS_MESSAGE, pErrorMessage);
        CwfMessage tResponse = new CwfMessage(tRsp, pHandle);
        pConnection.getTransportService().addClientMessage(tResponse);
    }

}
