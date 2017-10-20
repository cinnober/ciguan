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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsRootIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.impl.MvcEventEnum;
import com.cinnober.ciguan.client.impl.MvcModelNames;
import com.cinnober.ciguan.data.AsClientLogEntry;
import com.cinnober.ciguan.data.AsClientSession;
import com.cinnober.ciguan.data.AsSetClientLogLevelReq;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.impl.As;

/**
 * Plugin that handles client logging.
 */
public class AsClientLogPlugin extends AsTransportPlugin {

    /** The format. */
    protected SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public void onMessage(AsConnectionIf pConnection, CwfMessage pMessage) {

        // Client logging
        if (MvcModelNames.LogEntry.name().equals(pMessage.getName())) {
            AsClientLogEntry tEntry = new AsClientLogEntry();
            tEntry.sessionId = pConnection.getSessionId();
            tEntry.message = pMessage.getData().getProperty(ATTR_LOG_MESSAGE);
            tEntry.timestamp = mFormat.format(new Date());
            tEntry.level = pMessage.getData().getProperty(ATTR_LOG_LEVEL);
            AsClientSession tClientSession = AsRootIf.Singleton.get()
                .getClientSession(pConnection.getSessionId());
            tEntry.browserDetails = tClientSession != null
                    ? tClientSession.browserDetails
                    : null;
            CwfDataIf tStackTrace = pMessage.getData().getObject(ATTR_LOG_EXCEPTION);
            if (tStackTrace != null) {
                tEntry.stacktrace = tStackTrace.getStringArray(ATTR_LOG_STACKTRACE);
            }
            As.getBdxHandler().broadcast(tEntry);
            sendOkResponse(pConnection, pMessage.getHandle());
            return;
        }

        // Log level request
        if (As.getTypeName(AsSetClientLogLevelReq.class).equals(pMessage.getName())) {
            String tTargetSessionId = pMessage.getData().getProperty("sessionId");
            String tLogLevel = pMessage.getData().getProperty("logLevel");
            CwfDataIf tEvent = CwfDataFactory.create(MvcEventEnum.ChangeLogLevelEvent);
            tEvent.setProperty(ATTR_LOG_LEVEL, tLogLevel);
            AsConnectionIf tTargetConnection = AsRootIf.Singleton.get().getAsConnection(tTargetSessionId);
            synchronized (tTargetConnection) {
                tTargetConnection.getTransportService().addClientMessage(new CwfMessage(tEvent, 0));
            }
            sendOkResponse(pConnection, pMessage.getHandle());
            return;
        }

    }

}
