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

import java.util.List;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.impl.MvcModelNames;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.handler.AsFileUploadHandlerIf;

/**
 *
 * Base class for file upload handlers
 *
 */
public abstract class AsFileUploadHandler implements AsFileUploadHandlerIf, MvcModelAttributesIf {

    private int mStatusCode;
    private String mStatusMessage;
    private String mResponseName;

    @Override
    public CwfDataIf handleFileUploadData(AsConnectionIf pConnection, AsFileUploadParam pFile,
        List<AsFileUploadParam> pFormFields) {

        doProcess(pConnection, pFile, pFormFields);
        assert mStatusCode != 0 && mStatusMessage != null;
        CwfDataIf tResponse = CwfDataFactory.create(MvcModelNames.FileUploadResult);
        tResponse.setProperty(ATTR_STATUS_CODE, mStatusCode);
        tResponse.setProperty(ATTR_STATUS_MESSAGE, mStatusMessage);
        if (mResponseName != null) {
            tResponse.setProperty(ATTR_OBJECT_NAME, mResponseName);
        }
        tResponse.setProperty(ATTR_NAME, getClass().getSimpleName());
        return tResponse;
    }

    /**
     * Override this method to add specific handler functionality. The resulting status
     * code and message must be set by the implementing class.
     * @param pConnection the application server connection
     * @param pFile the file data
     * @param pFormFields data related to the other form fields
     */
    public abstract void doProcess(AsConnectionIf pConnection, AsFileUploadParam pFile,
        List<AsFileUploadParam> pFormFields);

    /**
     * Set the status code of the processing
     * @param pStatusCode the status code of the processing
     */
    protected void setStatusCode(int pStatusCode) {
        mStatusCode = pStatusCode;
    }

    /**
     * Set the status message of the processing
     * @param pStatusMessage the status message of the processing
     */
    protected void setStatusMessage(String pStatusMessage) {
        mStatusMessage = pStatusMessage;
    }

    /**
     * Set the name of the response sent to the client
     * @param pResponseName the name of the response sent to the client
     */
    public void setResponseName(String pResponseName) {
        mResponseName = pResponseName;
    }

}
