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

import com.cinnober.ciguan.CwfEventNameIf;

/**
 * Enumeration for MVC events 
 */
public enum MvcEventEnum implements CwfEventNameIf {

    ChangeLogLevelEvent,
    ContextMenuResponse,
    ContextObjectResponse,
    DataClearEvent,
    DataUpdateEvent,
    FormContextEvent,
    FormContextLookupEvent,
    FormDestroyEvent,
    FormStartEvent,
    FormSubmitEvent,
    ListAddEvent,
    ListClearEvent,
    ListErrorEvent,
    ListInitEvent,
    ListItemResponse,
    ListMultipleItemsResponse, 
    ListRemoveEvent,
    ListTextResponse,
    ListUpdateEvent,
    PageReloadEvent,
    UpdateRequestTokenEvent,
    ViewportArrangeColumnsMvcEvent,
    ViewportErrorEvent,
    ViewportInitEvent,
    ViewportQueryInitEvent,
    ViewportQueryCompleteEvent,
    ViewportSetFilterMvcEvent,
    ViewportUpdateEvent;
    
    private static Map<String, CwfEventNameIf> cMap;

    private static Map<String, CwfEventNameIf> getMap() {
        if (cMap == null) {
            cMap = new HashMap<String, CwfEventNameIf>();
            addEvents(values());
        }
        return cMap;
    }
    
    public static void addEvents(CwfEventNameIf... pEvents) {
        for (CwfEventNameIf tEvent : pEvents) {
            getMap().put(tEvent.name(), tEvent);
        }
    }
    
    public static CwfEventNameIf get(String pName) {
        return getMap().get(pName);
    }

}
