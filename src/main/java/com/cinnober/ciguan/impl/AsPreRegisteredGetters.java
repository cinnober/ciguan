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

import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
/**
 * 
 * Mapping of custom getters
 *  
 */
public class AsPreRegisteredGetters {
    
    private final Map<Class<?>, Map<String, AsGetMethodIf<?>>> mClassMap;
    
    public AsPreRegisteredGetters() {
        mClassMap = new HashMap<Class<?>, Map<String, AsGetMethodIf<?>>>();
    }
    
    @SuppressWarnings("unchecked")
    <T> AsGetMethodIf<T> get(Class<T> pClass, String pName) {
        return (AsGetMethodIf<T>) get0(pClass).get(pName);
    }
    
    @SuppressWarnings("unchecked")
    <T> AsGetMethodIf<T>[] get(Class<T> pClass) {
        return get0(pClass).values().toArray(new AsGetMethodIf[0]);
    }
    
    void put(AsGetMethodIf<?> pMethod) throws AsInitializationException {
        if (get(pMethod.getItemClass(), pMethod.getAttributeName()) != null) {
            throw new AsInitializationException(
                "Attempting to register a custom getter on a name that is already used: " +
                pMethod.getItemClass() + " " + pMethod.getAttributeName());
        }
        Map<String, AsGetMethodIf<?>> tMap = mClassMap.get(pMethod.getItemClass());
        if (tMap == null) {
            tMap = new HashMap<String, AsGetMethodIf<?>>();
            mClassMap.put(pMethod.getItemClass(), tMap); 
        }
        tMap.put(pMethod.getAttributeName(), pMethod);
    }
    
    public void clear() {
        mClassMap.clear();
    }
    
    /**
     * Collects defined get methods for the given class and all its superclasses. Note that
     * a get method on a subclass should override a get method with the same name on a superclass.
     * @param pClass the class to collect getters for
     * @return a map of all getters valid for the given class
     */
    protected <T> Map<String, AsGetMethodIf<?>> get0(Class<T> pClass) {
        Class<?> tClass = pClass;
        Map<String, AsGetMethodIf<?>> tGetters = new HashMap<String, AsGetMethodIf<?>>();
        while (tClass != null && tClass != Object.class) {
            Map<String, AsGetMethodIf<?>> tMap = mClassMap.get(tClass);
            if (tMap != null) {
                for (Map.Entry<String, AsGetMethodIf<?>> tEntry : tMap.entrySet()) {
                    // Subclass methods override superclass methods
                    if (!tGetters.containsKey(tEntry.getKey())) {
                        tGetters.put(tEntry.getKey(), tEntry.getValue());
                    }
                }
            }
            tClass = tClass.getSuperclass();
        }
        return tGetters;
    }
    
}
