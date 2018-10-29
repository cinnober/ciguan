/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Cinnober Financial Technology AB (cinnober.com)
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

import java.io.Serializable;

import org.json.JSONObject;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.CwfMessageIf;
import com.cinnober.ciguan.CwfModelNameIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;

/**
 * Implementation of a message transferred across the wire
 */
@SuppressWarnings("serial")
public class CwfMessage extends JSONObject
    implements CwfMessageIf, Serializable, MvcModelAttributesIf {

    protected CwfMessage() {
    }

    public CwfMessage(CwfModelNameIf pMessageName, CwfDataIf pData, int pHandle) {
        this(pMessageName.name(), pData, pHandle);
    }
    public CwfMessage(CwfDataIf pData, int pHandle) {
        this(pData.getProperty(ATTR_MODEL_NAME), pData, pHandle);
    }
    private CwfMessage(String pMessageName, CwfDataIf pData, int pHandle) {
    	CwfDataIf tData = pData == null ? CwfDataFactory.create() : pData;
    	if (pMessageName != null) {
    		tData.setProperty(ATTR_MODEL_NAME, pMessageName);
    	}
        put(ATTR_DATA, tData);
        put(ATTR_HANDLE, pHandle);
    }

    public CwfDataIf getData() {
        return (CwfDataIf) get(ATTR_DATA);
    }

    public int getHandle() {
        return getInt(ATTR_HANDLE);
    }

    public String getName() {
        return getData().getProperty(ATTR_MODEL_NAME);
    }
    
    public JSONObject getJson() {
        return this;
    }

}
