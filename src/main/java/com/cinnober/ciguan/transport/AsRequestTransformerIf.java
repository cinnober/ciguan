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
package com.cinnober.ciguan.transport;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.CwfDataIf;

/**
 * Interface defining request transformation functionality, in essence the way to transform from
 * a raw model to a TEAI message or the other way around.
 */
public interface AsRequestTransformerIf {

    /**
     * Transform a raw data map to a server side message.
     *
     * @param pConnection the connection
     * @param pData the data
     * @return the object
     */
    Object transform(AsConnectionIf pConnection, CwfDataIf pData);

    /**
     * Transform a server side message to a raw data map.
     *
     * @param pConnection the connection
     * @param pMessage the message
     * @return the CwfDataIf object.
     */
    CwfDataIf transform(AsConnectionIf pConnection, Object pMessage);
    
}
