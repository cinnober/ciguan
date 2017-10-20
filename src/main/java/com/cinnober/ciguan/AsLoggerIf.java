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

import com.cinnober.ciguan.impl.As;

/**
 *
 * The AsLoggerIf is used by the application for logging to file (and console). 
 * To obtain a AsLoggerIf instance, please call {@link AsLoggerIf.Singleton#get()}.
 *
 */

public interface AsLoggerIf {

    /**
     * This method is used to log a Throwable with log level NORMAL.
     *
     * @param pMessage The message.
     * @param pThrowable The throwable.
     */
    void logThrowable(String pMessage, Throwable pThrowable);
    
    /**
     * This method is used to report trace with trace level NORMAL. 
     * This method should be used instead of System.out.println().
     *
     * @param pMessage The message.
     */

    void log(String pMessage);
    
    /**
     * This method is used to report trace with trace level TRACE. 
     * This method should be used for tracing/debugging.
     *
     * @param pMessage The message.
     */
    void logTrace(String pMessage);

    /**
     * Singleton instance of AsLoggerIf.
     */
    class Singleton {
        
        /** The Singleton. */
        private static AsLoggerIf cSingleton;
        
        /**
         * Retrieves the singleton instance.
         *
         * @return the instance.
         */
        public static AsLoggerIf get() {
            if (cSingleton == null) {
                return new BootLogger();
            }
            return cSingleton;
        }
        
        /**
         * Creates the singleton instance.
         */
        public static void create() {
            cSingleton = As.getBeanFactory().create(AsLoggerIf.class);
        }

    }
    
    /**
     * 
     * Simple standard error/out logger for use during the early bootstrap stages
     * 
     */
    class BootLogger implements AsLoggerIf {

        @Override
        public void logThrowable(String pMessage, Throwable pThrowable) {
            System.err.println(pMessage);
            pThrowable.printStackTrace();
        }

        @Override
        public void log(String pMessage) {
            System.err.println(pMessage);
        }

        @Override
        public void logTrace(String pMessage) {
            System.out.println(pMessage);
        }
        
    }
    
}
