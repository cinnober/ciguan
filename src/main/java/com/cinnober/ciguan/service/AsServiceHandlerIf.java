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
package com.cinnober.ciguan.service;

import com.cinnober.ciguan.AsBeanFactoryIf;
import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.service.impl.AsServiceInvocationException;

/**
 *
 * Defines service handler functionality
 * 
 */
public interface AsServiceHandlerIf {

    /**
     * Look up and execute a service for the given request
     * @param pConnection the application server connection
     * @param pRequest the request to process
     * @return null if no service was found, otherwise the service response
     * @throws AsServiceInvocationException when an exception occurs in the service
     */
    Object service(AsConnectionIf pConnection, Object pRequest) throws AsServiceInvocationException;
    
    /**
     * Singleton instance of the interface.
     */
    public static class Singleton {

        /** The Instance. */
        static AsServiceHandlerIf cInstance;

        /**
         * Retrieves the singleton instance.
         *
         * @return The service handler.
         */
        public static AsServiceHandlerIf get() {
            return cInstance;
        }

        /**
         * Creates the service handler.
         */
        public static void create() {
            cInstance = AsBeanFactoryIf.Singleton.get().create(AsServiceHandlerIf.class);
        }
    }
    
}
