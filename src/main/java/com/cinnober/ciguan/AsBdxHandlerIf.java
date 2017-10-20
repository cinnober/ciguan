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
package com.cinnober.ciguan;

import com.cinnober.ciguan.plugin.AsServicePluginMessageHandlerIf;

/**
 * Interface defining broadcast handling functionality.
 */
public interface AsBdxHandlerIf extends AsServicePluginMessageHandlerIf {

    /** The user preferences id. */
    String USER_PREFERENCES_ID = "cwf.user-preferences";
    
    /**
     * Add a catch-all broadcast listener. A catch-all listener will receive all
     * incoming messages.
     *
     * @param pListener The listener.
     */
    void addBdxListener(AsBdxListenerIf pListener);

    /**
     * Add a specified broadcast listener. A specified listener will only receive messages
     * matching the specified class(es).
     *
     * @param pListener The listener.
     * @param pClasses The class(es) of interest to listen for.
     */
    void addBdxListener(AsBdxListenerIf pListener, Class<?>... pClasses);
    
    /**
     * Remove a broadcast listener.
     *
     * @param pListener The listener.
     */
    void removeBdxListener(AsBdxListenerIf pListener);
    
    /**
     * @return the current broadcast queue length
     */
    int getQueueLength();
    
    /**
     * Singleton instance of the broadcast handler.
     */
    public static class Singleton {

        /** The Instance. */
        static AsBdxHandlerIf cInstance;

        /**
         * Retrieves the singleton instance.
         *
         * @return The broadcast handler.
         */
        public static AsBdxHandlerIf get() {
            return cInstance;
        }

        /**
         * Creates the broadcast handler.
         */
        public static void create() {
            cInstance = AsBeanFactoryIf.Singleton.get().create(AsBdxHandlerIf.class);
        }
    }
    
}
