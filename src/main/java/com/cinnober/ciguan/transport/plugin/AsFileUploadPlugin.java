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
import com.cinnober.ciguan.client.impl.MvcModelNames;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.impl.As;

/**
 * A transport plugin for handling processed file upload data.
 */
public class AsFileUploadPlugin extends AsTransportPlugin {

    /** The m status code. */
    private int mStatusCode = As.STATUS_CODE_OK;

    /** The m status message. */
    private String mStatusMessage = "";

    /** The m response name. */
    private String mResponseName;

    /**
     * Handle additional processing of uploaded data.
     * There is no default implementation provided, extend this when needed.
     *
     * @param pConnection the connection
     * @param pSubCodes an array of result codes, one per upload handler
     * @param pSubMessages an array of result messages, one per upload handler
     * @param pModelName the name of the file upload form model
     */
    public void handleFileUploadResult(AsConnectionIf pConnection,
        int[] pSubCodes, String[] pSubMessages, String pModelName) {
        // No action by default
    }

    @Override
    public void onMessage(AsConnectionIf pConnection, CwfMessage pMessage) {
        if (pMessage.getName().equals(MvcModelNames.FileUploadResult.name())) {

            String tModelName = pMessage.getData().getProperty(ATTR_OBJECT_NAME);
            int[] tSubCodes = new int[pMessage.getData().getObjectList(ATTR_HANDLER_RESULT).size()];
            String[] tSubMessages = new String[pMessage.getData().getObjectList(ATTR_HANDLER_RESULT).size()];
            boolean tHasErrorSubstatusCodes = false;
            int tIndex = 0;
            for (CwfDataIf tSubresult : pMessage.getData().getObjectList(ATTR_HANDLER_RESULT)) {
                int tSubCode = tSubresult.getIntProperty(ATTR_STATUS_CODE);
                tSubMessages[tIndex] = tSubresult.getProperty(ATTR_STATUS_MESSAGE);
                tSubCodes[tIndex++] = tSubCode;
                if (tSubCode != As.STATUS_CODE_OK && tSubCodes.length > 1) {
                    tHasErrorSubstatusCodes = true;
                }
            }

            // Set the default return status and call the extension point
            setDefaultReturnStatus(pMessage);
            handleFileUploadResult(pConnection, tSubCodes, tSubMessages, tModelName);

            CwfDataIf tResponse = CwfDataFactory.create(MvcModelNames.ServerResponse);
            tResponse.setProperty(ATTR_STATUS_CODE, mStatusCode);
            tResponse.setProperty(ATTR_STATUS_MESSAGE, mStatusMessage);
            if (mResponseName != null) {
                tResponse.setProperty(ATTR_OBJECT_NAME, mResponseName);
            }

            // set sub status for each file upload result if at least one of them did not succeed
            if (tHasErrorSubstatusCodes) {
                tResponse.setProperty(ATTR_STATUS_SUBCODE, tSubCodes);
            }

            CwfMessage tMessage = new CwfMessage(tResponse, pMessage.getHandle());
            pConnection.getTransportService().addClientMessage(tMessage);
        }
    }

    /**
     * Set default return status
     * Logic: If there is one and only one upload handler, set the total return status to the return
     * status of the handler.
     *
     * @param pMessage the new default return status
     */
    protected void setDefaultReturnStatus(CwfMessage pMessage) {
        if (pMessage.getData().getObjectList(ATTR_HANDLER_RESULT).size() == 1) {
            CwfDataIf tHandlerResult = pMessage.getData().getObjectList(ATTR_HANDLER_RESULT).get(0);
            setStatusCode(tHandlerResult.getIntProperty(ATTR_STATUS_CODE));
            setStatusMessage(tHandlerResult.getProperty(ATTR_STATUS_MESSAGE));
            setResponseName(tHandlerResult.getProperty(ATTR_OBJECT_NAME));
        }
    }

    /**
     * Set the total return status code.
     *
     * @param pStatusCode the new status code
     */
    protected void setStatusCode(int pStatusCode) {
        mStatusCode = pStatusCode;
    }

    /**
     * Set the total return status message.
     *
     * @param pStatusMessage the new status message
     */
    protected void setStatusMessage(String pStatusMessage) {
        mStatusMessage = pStatusMessage;
    }

    /**
     * Sets the name of the response sent to the client.
     *
     * @param pResponseName the new response name
     */
    protected void setResponseName(String pResponseName) {
        mResponseName = pResponseName;
    }

}
