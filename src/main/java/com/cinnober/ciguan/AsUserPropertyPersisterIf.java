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
 * Interface defining user property persistance functionality.
 */
public interface AsUserPropertyPersisterIf {

    /**
     * Persist user properties for the given user.
     *
     * @param pUserId the user id
     * @param pTaskNotifier the task notifier
     */
    void saveUserProperties(String pUserId, AsTaskCompletionIf pTaskNotifier);

    /**
     * Reset all properties for the given user.
     *
     * @param pUserId the user id
     */
    void resetUserProperties(String pUserId);
    

    /**
     * Singleton instance.
     */
    public static class Singleton {

        /** The Instance. */
        static AsUserPropertyPersisterIf cInstance;

        /**
         * Gets the instance.
         *
         * @return the user property persister instance
         */
        public static AsUserPropertyPersisterIf get() {
            return cInstance;
        }
        
        /**
         * Creates the instance.
         */
        public static void create() {
            cInstance = As.getBeanFactory().create(AsUserPropertyPersisterIf.class);
        }
        
    }
}
