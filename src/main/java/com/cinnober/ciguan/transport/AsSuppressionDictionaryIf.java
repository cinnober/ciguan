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

import java.lang.reflect.Field;

/**
 * Functions related to suppression of items being transformed between server side
 * objects and client side map structures.
 */
public interface AsSuppressionDictionaryIf {

    /**
     * Enumeration of field categories.
     */
    enum FieldCategory {
        
        /** The simple. */
        SIMPLE,
        
        /** The complex. */
        COMPLEX,
        
        /** The array. */
        ARRAY, 
        
        /** The collection. */
        COLLECTION,
        
        /** The map. */
        MAP
    }
    
    /**
     * Add an attribute that should be suppressed.
     *
     * @param pAttribute the name of the attribute
     */
    void addAttribute(String pAttribute);

    /**
     * Add a class that should be suppressed.
     *
     * @param pClass the simple name of the class
     */
    void addClass(String pClass);

    /**
     * Add an attribute in a class that should be suppressed.
     *
     * @param pClass the simple name of the class
     * @param pAttribute the name of the attribute
     */
    void addClassAttribute(String pClass, String pAttribute);
    
    /**
     * Get the category of a given class.
     *
     * @param pClass the class
     * @return the field category
     */
    FieldCategory getFieldCategory(Class<?> pClass);
    
    /**
     * Check if a field is suppressed.
     *
     * @param pField the field
     * @return {@code true}, if is field suppressed
     */
    boolean isFieldSuppressed(Field pField);

    /**
     * Check if an object is suppressed.
     *
     * @param pName the name
     * @param pClass the class
     * @return {@code true}, if is object suppressed
     */
    boolean isObjectSuppressed(String pName, Class<?> pClass);
    
}
