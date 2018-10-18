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
package com.cinnober.ciguan.handler.impl;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.CwfMessageIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.impl.MvcEventEnum;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.handler.AsFormHandlerIf;

/**
 *
 * Base class for form handlers.
 *
 * IMPORTANT: The start, submit and destroy events use a handle which is subscription based, which
 * means that you can pass asynchronous messages back to the form at any time. The context lookup
 * event is however NOT a subscription handle, which means that there can be one and only one
 * response to a context lookup, which should always be the context object that was looked up.
 * If no matching object was found during lookup, a null object should be passed as the response.
 *
 */
public abstract class AsFormHandler implements AsFormHandlerIf, MvcModelAttributesIf {

    /**
     * Default form start-up action, override if needed
     */
    @Override
    public CwfDataIf onFormStart(AsConnectionIf pConnection, CwfMessageIf pModel) {
        // No action by default
        return null;
    }

    /**
     * Default form context lookup action, override if needed
     */
    @Override
    public void onFormContextLookup(AsConnectionIf pConnection, CwfMessageIf pModel) {
        // No action by default
    }

    /**
     * Default form submit action, override if needed
     */
    @Override
    public void onFormSubmit(AsConnectionIf pConnection, CwfMessageIf pModel) {
        // No action by default
    }

    /**
     * Default form destruction action, override if needed
     */
    @Override
    public void onFormDestroy(AsConnectionIf pConnection, CwfMessageIf pModel) {
        // No action by default
    }

    /**
     * Default form handler destruction action, override if needed
     */
    @Override
    public void destroy() {
        // No action by default
    }

    /**
     * Send the given object as a context to the client.
     *
     * @param pConnection the application server connection
     * @param pHandle the client handle
     * @param pObject the object to send
     */
    protected void sendClientContext(AsConnectionIf pConnection, int pHandle, Object pObject) {
        CwfDataIf tContext = null;
        if (pObject instanceof CwfDataIf) {
            tContext = (CwfDataIf) pObject;
        }
        else if (pObject != null) {
            tContext = pConnection.getRequestService().transform(pConnection, pObject);
        }
        tContext.setProperty(ATTR_MODEL_NAME, MvcEventEnum.FormContextEvent.name());
        pConnection.getTransportService().addClientMessage(new CwfMessage((CwfDataIf) tContext, pHandle));
    }

}
