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
package com.cinnober.ciguan.client.impl;

import java.util.HashMap;
import java.util.Map;

import com.cinnober.ciguan.CwfRequestNameIf;

public enum MvcRequestEnum implements CwfRequestNameIf {

    DataSubscriptionRequest,
    DataUnsubscribeRequest,
    ListItemRequest,
    ListMultipleItemsRequest,
    ListSubscriptionRequest,
    ListTextRequest,
    MenuRequest,
    ReloadConfigurationRequest,
    ResetPerspectiveRequest,
    SessionModel,
    UserPreferenceRequest,
    ViewportAddObjectRequest,
    ViewportDeleteObjectRequest,
    ViewportFilterRequest,
    ViewportGetContextMenuRequest,
    ViewportGetContextRequest,
    ViewportMovePositionRequest,
    ViewportMoveSelectionRequest,
    ViewportSetExpandedRequest,
    ViewportSetPositionRequest,
    ViewportSetQueryRequest,
    ViewportSetSelectionRequest,    
    ViewportSetSizeRequest,
    ViewportSortRequest,
    ViewportSubscriptionRequest,
    ViewportUpdateObjectRequest,
    ViewportUnsubscribeRequest;

    private static Map<String, CwfRequestNameIf> cMap;

    private static Map<String, CwfRequestNameIf> getMap() {
        if (cMap == null) {
            cMap = new HashMap<String, CwfRequestNameIf>();
            addRequests(values());
        }
        return cMap;
    }
    
    public static void addRequests(CwfRequestNameIf... pRequests) {
        for (CwfRequestNameIf tRequest : pRequests) {
            getMap().put(tRequest.name(), tRequest);
        }
    }
    
    public static CwfRequestNameIf get(String pName) {
        return getMap().get(pName);
    }


}
