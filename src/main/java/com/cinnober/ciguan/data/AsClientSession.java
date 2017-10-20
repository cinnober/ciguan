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
package com.cinnober.ciguan.data;

import com.cinnober.ciguan.annotation.CwfDateTime;
import com.cinnober.ciguan.annotation.CwfIdField;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.AsRefDataObject;

/**
 * Client session.
 */
public class AsClientSession extends AsRefDataObject {

    /** The session id. */
    @CwfIdField
    public String sessionId;
    
    /** The user id. */
    public String userId;
    
    /** The created at. */
    @CwfDateTime
    public String createdAt;
    
    /** The browser details. */
    public String browserDetails;

    /**
     * Default instance.
     */
    public AsClientSession() {
    }
    
    /**
     * Session ID instance.
     *
     * @param pSessionId the session id
     */
    public AsClientSession(String pSessionId) {
        sessionId = pSessionId;
        createdAt = As.now();
    }
    
    /**
     * Copy constructor.
     *
     * @param pSession the session
     */
    public AsClientSession(AsClientSession pSession) {
        sessionId = pSession.sessionId;
        userId = pSession.userId;
        createdAt = pSession.createdAt;
        browserDetails = pSession.browserDetails;
    }
    
}
