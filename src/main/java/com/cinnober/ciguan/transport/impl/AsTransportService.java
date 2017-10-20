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
package com.cinnober.ciguan.transport.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.transport.AsTransportServiceIf;
import com.cinnober.ciguan.transport.AsTransportServicePluginIf;

/**
 * Implementation of the application server transport layer.
 */
public class AsTransportService implements AsTransportServiceIf {

    /** The transport plugins. */
    private final List<AsTransportServicePluginIf> mTransportPlugins =
        new ArrayList<AsTransportServicePluginIf>();
    
    /** The client messages. */
    private final List<CwfMessage> mClientMessages =
        new ArrayList<CwfMessage>();
    
    /** The connection. */
    private AsConnectionIf mConnection;

    @Override
    public void setConnection(AsConnectionIf pConnection) {
        mConnection = pConnection;
    }
    
    @Override
    public void createPlugins() {
        for (String tPluginClass : As.getTransportConfiguration().getPluginClasses()) {
            addPlugin(createPlugin(tPluginClass));
        }
    }
    
    @Override
    public void addPlugin(AsTransportServicePluginIf pPlugin) {
        mTransportPlugins.add(pPlugin);
    }

    @Override
    public void addClientMessage(CwfMessage pMessage) {
        synchronized (mClientMessages) {
            mClientMessages.add(pMessage);
        }
    }
    
    @Override
    public ArrayList<CwfMessage> getPendingClientMessages() {
        synchronized (mClientMessages) {
            ArrayList<CwfMessage> tList = new ArrayList<CwfMessage>(mClientMessages);
            mClientMessages.clear();
            return tList;
        }
    }

    @Override
    public List<AsTransportServicePluginIf> getPlugins() {
        return Collections.unmodifiableList(mTransportPlugins);
    }

    @Override
    public void receive(List<CwfMessage> pMessages) {
        for (CwfMessage tMessage : pMessages) {
            for (AsTransportServicePluginIf tPlugin : new ArrayList<AsTransportServicePluginIf>(mTransportPlugins)) {
                if (tPlugin.isEnabled()) {
                    try {
                        tPlugin.onMessage(mConnection, tMessage);
                    }
                    catch (Throwable e) {
                        As.getLogger().logThrowable(
                            "Exception during message processing in transport plugin " + tPlugin.getPluginId(), e);
                        sendPluginExceptionEvent(tPlugin, e);
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        for (AsTransportServicePluginIf tPlugin : new ArrayList<AsTransportServicePluginIf>(mTransportPlugins)) {
            if (tPlugin.isEnabled()) {
                try {
                    tPlugin.reset(mConnection);
                }
                catch (Throwable e) {
                    As.getLogger().logThrowable(
                        "Exception while resetting transport plugin " + tPlugin.getPluginId(), e);
                    sendPluginExceptionEvent(tPlugin, e);
                }
            }
        }
    }
    
    /**
     * Send an error event related to a plug-in exception to the client. Default implementation is not provided. 
     * Override this when needed. 
     *
     * @param pPlugin the plugin
     * @param pError the error to send
     */
    protected void sendPluginExceptionEvent(AsTransportServicePluginIf pPlugin, Throwable pError) {
        // No action, override when needed
    }
    
    /**
     * Instantiate a plugin of the given class. Note that we do not do any type checking
     * here as this is done when the configuration is loaded. See {@link AsTransportConfiguration} for
     * details.
     *
     * @param pPluginClass the plugin class
     * @return a newly created plug-in
     * @throws RuntimeException if it cannot create an instance of this type
     */
    protected AsTransportServicePluginIf createPlugin(String pPluginClass) {
        try {
            Class<?> tClass = Class.forName(pPluginClass);
            try {
                // Look for a constructor that takes an AS connection first
                Constructor<?> tConstructor = tClass.getConstructor(AsConnectionIf.class);
                return (AsTransportServicePluginIf) tConstructor.newInstance(mConnection);
            }
            catch (NoSuchMethodException e) {
                // Assume there is always a default constructor
                Constructor<?> tConstructor = tClass.getConstructor();
                return (AsTransportServicePluginIf) tConstructor.newInstance();
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not instantiate plugin of type " + pPluginClass, e);
        }
    }
    
}
