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

import java.util.List;

import com.cinnober.ciguan.CwfDataIf;

/**
 * Interface defining the transport layer service functionality
 */
public interface TpServiceIf {

    /**
     * Send a request and receive the response through a callback
     * @param pRequest
     * @param pCallback
     * @return the request handle
     */
    int send(CwfDataIf pRequest, TpServiceCallbackIf<?> pCallback);
    
    /**
     * Add a transport plugin to the service
     * @param pPlugin
     */
    void addPlugin(TpServicePluginIf pPlugin);
    
    /**
     * Get the currently known plugins
     * @return the currently known plugins
     */
    List<TpServicePluginIf> getPlugins();
    
    /**
     * Singleton instance of the interface
     */
    public static class Singleton {
        
        private static TpServiceIf cInstance;
        
        public static TpServiceIf get() {
            return cInstance;
        }
        
        public static void set(TpServiceIf pTransportService) {
            cInstance = pTransportService;
        }
        
    }
}
