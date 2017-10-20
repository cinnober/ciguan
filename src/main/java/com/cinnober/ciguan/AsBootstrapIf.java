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

/**
 * Defines application server bootstrap functionality.
 */
public interface AsBootstrapIf {

    /**
     * Start the system.
     *
     * @param pStartModuleName The name of the module to load.
     * @param pContextPath The path of the web application context
     * @throws AsInitializationException when a critical error is thrown from a component
     */
    void start(String pStartModuleName, String pContextPath) throws AsInitializationException;
    
    /**
     * @return the path of the web application context
     */
    String getContextPath();
    
    /**
     * All components started.
     *
     * @throws AsInitializationException when a critical error is thrown from a component
     */
    void allComponentsStarted() throws AsInitializationException;
    
    /**
     * External data synchronization.
     *
     * @throws AsInitializationException when a critical error is thrown from a component
     */
    void synchronizeExternalData() throws AsInitializationException;
    
    /**
     * Stop the system.
     *
     * @throws AsInitializationException when a critical error is thrown from a component
     */
    void stop() throws AsInitializationException;
    
    /**
     * All components stopped.
     *
     * @throws AsInitializationException when a critical error is thrown from a component
     */
    void allComponentsStopped() throws AsInitializationException;
    
    /**
     * Singleton instance of this interface.
     */
    public static class Singleton {

        /** The Instance. */
        static AsBootstrapIf cInstance;

        /**
         * Gets the instance.
         *
         * @return The instance.
         */
        public static AsBootstrapIf get() {
            return cInstance;
        }

        /**
         * Sets the instance.
         *
         * @param pInstance The instance.
         */
        public static void set(AsBootstrapIf pInstance) {
            cInstance = pInstance;
        }
    }
    
}
