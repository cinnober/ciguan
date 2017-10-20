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
import com.cinnober.ciguan.client.impl.MvcRequestEnum;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;

/**
 * Plugin handling data source related requests, for example setting up subscriptions to
 * data sources.
 */
public class AsDataSourcePlugin extends AsTransportPlugin {

    @Override
    public void onMessage(AsConnectionIf pConnection, CwfMessage pMessage) {
        
        try {
            AsDataSourceServiceIf tDataSourceService = pConnection.getDataSourceService();
            MvcRequestEnum tModelName = MvcRequestEnum.valueOf(pMessage.getName());
            switch (tModelName) {
                // Session query, remove all current subscriptions
                case SessionModel :
                    tDataSourceService.resetDataSourceSubscriptions();
                    break;
                case MenuRequest:
                    tDataSourceService.getMenu(pConnection, pMessage);
                    break;
                // Data source related requests
                case DataSubscriptionRequest:
                case DataUnsubscribeRequest:
                case ListItemRequest:
                case ListMultipleItemsRequest:
                case ListSubscriptionRequest:
                case ListTextRequest:
                case ViewportFilterRequest:
                case ViewportMovePositionRequest:
                case ViewportMoveSelectionRequest:
                case ViewportSetSizeRequest:
                case ViewportSetPositionRequest:
                case ViewportSetSelectionRequest:
                case ViewportSetExpandedRequest:
                case ViewportSortRequest:
                case ViewportSubscriptionRequest:
                case ViewportSetQueryRequest:
                case ViewportAddObjectRequest:
                case ViewportUpdateObjectRequest:
                case ViewportUnsubscribeRequest:
                case ViewportDeleteObjectRequest:
                case ViewportGetContextRequest:
                case ViewportGetContextMenuRequest:
                    tDataSourceService.request(pConnection, pMessage);
                    break;
                    
                default :
                    break;
            }
        }
        catch (IllegalArgumentException e) {
            // this message is not handled here!
            return;
        }

    }
    

    @Override
    public void reset(AsConnectionIf pConnection) {
        super.reset(pConnection);
        pConnection.getDataSourceService().destroy();
    }

}
