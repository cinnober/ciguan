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
package com.cinnober.ciguan.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * Class holding configuration for on-the-fly created beans
 */
public class AsBeanConfiguration {

    private final Map<Class<?>, Entry<?>> mClassMappings = new HashMap<Class<?>, Entry<?>>();

    /**
     * Add configuration for an interface class
     * @param pInterfaceClass
     * @param pImplementationClass
     * @param pSingleton
     * @param pParameters
     */
    @SuppressWarnings("unchecked")
    public <T> void addConfiguration(Class<T> pInterfaceClass, Class<?> pImplementationClass,
        boolean pSingleton, boolean pParameters) {
        assert pImplementationClass.isAssignableFrom(pImplementationClass);
        Entry<T> tEntry = new Entry<T>((Class<? extends T>) pImplementationClass, pSingleton, pParameters);
        mClassMappings.put(pInterfaceClass, tEntry);
    }
    
    /**
     * Get the configuration for an interface class
     * @param pInterfaceClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> Entry<T> getConfiguration(Class<T> pInterfaceClass) {
        return (Entry<T>) mClassMappings.get(pInterfaceClass);
    }
    
    /**
     * Class representing an individual configuration entry
     */
    public static class Entry<T> {
        
        private final Class<? extends T> mImplementationClass;
        private final boolean mSingleton;
        private final boolean mParameters;
        
        public Entry(Class<? extends T> pImplementationClass, boolean pSingleton, boolean pParameters) {
            mImplementationClass = pImplementationClass;
            mSingleton = pSingleton;
            mParameters = pParameters;
        }
        
        public Class<? extends T> getImplementationClass() {
            return mImplementationClass;
        }
        
        public boolean isSingleton() {
            return mSingleton;
        }
        
        public boolean wantsParameters() {
            return mParameters;
        }
        
    }
    
}
