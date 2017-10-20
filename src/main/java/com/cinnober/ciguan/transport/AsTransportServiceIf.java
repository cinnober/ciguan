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

import java.util.ArrayList;
import java.util.List;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.data.CwfMessage;

/**
 * Interface defining the application server transport service layer.
 */
public interface AsTransportServiceIf {

    /**
     * Set the AS connection used by this instance of the transport service.
     *
     * @param pConnection the new connection
     */
    void setConnection(AsConnectionIf pConnection);
    
    /**
     * Process a list of received messages.
     *
     * @param pMessages a list of messages (requests)
     */
    void receive(List<CwfMessage> pMessages);

    /**
     * Transport service reset, called when the underlying session is invalidated.
     */
    void reset();
    
    /**
     * Dequeue and retrieve all pending client messages
     * (responses and broadcasts).
     *
     * @return the pending client messages
     */
    ArrayList<CwfMessage> getPendingClientMessages();

    /**
     * Add a client message.
     *
     * @param pMessage the message
     */
    void addClientMessage(CwfMessage pMessage);
    
    /**
     * Add a transport plugin to the service.
     *
     * @param pPlugin the plugin
     */
    void addPlugin(AsTransportServicePluginIf pPlugin);
    
    /**
     * Get the currently known plugins.
     *
     * @return the plugins
     */
    List<AsTransportServicePluginIf> getPlugins();
 
    /**
     * Create all configured transport plug-ins.
     */
    void createPlugins();
    
}
