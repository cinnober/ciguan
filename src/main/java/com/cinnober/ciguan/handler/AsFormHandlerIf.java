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
package com.cinnober.ciguan.handler;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.CwfMessageIf;

/**
 * Interface defining server side support during the MVC form life cycle.
 */
public interface AsFormHandlerIf {

    /**
     * Called upon form startup.
     *
     * @param pConnection the connection
     * @param pModel the model
     * @return an object to be passed back to the view as a context object, or null if
     * no context object should be sent to the view.
     */
    CwfDataIf onFormStart(AsConnectionIf pConnection, CwfMessageIf pModel);
    
    /**
     * Called upon form context lookup.
     *
     * @param pConnection the connection
     * @param pModel the model. To find out what to look up, do a <code>getObject(TAG_CONTEXT_LOOKUP)</code>
     * followed by <code>getProperty(ATTR_DATASOURCE_ID)</code> and <code>getProperty(ATTR_KEY)</code>.
     */
    void onFormContextLookup(AsConnectionIf pConnection, CwfMessageIf pModel);
    
    /**
     * Called just before the form model is submitted to the server.
     *
     * @param pConnection the connection
     * @param pModel the model
     */
    void onFormSubmit(AsConnectionIf pConnection, CwfMessageIf pModel);
    
    /**
     * Called upon form destruction (close).
     *
     * @param pConnection the connection
     * @param pModel the model
     */
    void onFormDestroy(AsConnectionIf pConnection, CwfMessageIf pModel);
    
    /**
     * Called when a form handler is discarded, normally immediately after onFormDestroy,
     * but also as a last resort when the client page has been reloaded without closing a form
     * properly. Think of this as a finally-block in a try/catch. If you allocate something you MUST
     * deallocate, you need to implement code here.
     */
    void destroy();
    
}
