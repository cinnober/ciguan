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
 * Interface defining bean factory functionality.
 */
public interface AsBeanFactoryIf {

    /**
     * Generic object factory method.
     *
     * @param <T> The type of the object being created.
     * @param pInterfaceClass The interface class of the object to be created.
     * @param pParameters An optional array of parameters.
     * @return an object of this type.
     * @throws IllegalArgumentException When the interface class is not among the known set of
     * object types.
     */
    public <T> T create(Class<T> pInterfaceClass, String... pParameters) throws IllegalArgumentException;
    

    /**
     * Singleton instance of the bean factory.
     */
    public static class Singleton {

        /** The Instance. */
        static AsBeanFactoryIf cInstance;

        /**
         * Gets the singleton instance.
         *
         * @return the instance.
         */
        public static AsBeanFactoryIf get() {
            return cInstance;
        }

        /**
         * Sets the bean factory instance.
         *
         * @param pFactory The factory instance.
         */
        public static void set(AsBeanFactoryIf pFactory) {
            cInstance = pFactory;
        }
    }
    
}
