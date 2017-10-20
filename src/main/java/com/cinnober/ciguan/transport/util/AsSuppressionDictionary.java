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
package com.cinnober.ciguan.transport.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.transport.AsSuppressionDictionaryIf;

/**
 * Class that handles lookup of suppressed items.
 */
public class AsSuppressionDictionary implements AsSuppressionDictionaryIf {

    /** The suppressed attributes. */
    public Set<String> mSuppressedAttributes = new HashSet<String>();    
    
    /** The suppressed classes. */
    public Set<String> mSuppressedClasses = new HashSet<String>();
    
    /** The suppressed class attributes. */
    public Map<String, Set<String>> mSuppressedClassAttributes = new HashMap<String, Set<String>>();

    @Override
    public void addAttribute(String pAttribute) {
        mSuppressedAttributes.add(pAttribute);
    }

    @Override
    public void addClass(String pClass) {
        mSuppressedClasses.add(pClass);
    }
    
    @Override
    public void addClassAttribute(String pClass, String pAttribute) {
        Set<String> tAttributes = mSuppressedClassAttributes.get(pClass);
        if (tAttributes == null) {
            tAttributes = new HashSet<String>();
            mSuppressedClassAttributes.put(pClass, tAttributes);
        }
        tAttributes.add(pAttribute);
    }
    
    /**
     * Checks if is attribute suppressed.
     *
     * @param pAttribute the attribute
     * @return {@code true}, if is attribute suppressed
     */
    protected boolean isAttributeSuppressed(String pAttribute) {
        return mSuppressedAttributes.contains(pAttribute);
    }

    /**
     * Checks if is class suppressed.
     *
     * @param pClass the class
     * @return {@code true}, if is class suppressed
     */
    protected boolean isClassSuppressed(String pClass) {
        return mSuppressedClasses.contains(pClass);
    }
    
    /**
     * Checks if is class attribute suppressed.
     *
     * @param pClass the class
     * @param pAttribute the attribute
     * @return {@code true}, if is class attribute suppressed
     */
    protected boolean isClassAttributeSuppressed(String pClass, String pAttribute) {
        Set<String> tAttributes = mSuppressedClassAttributes.get(pClass);
        if (tAttributes == null) {
            return false;
        }
        return tAttributes.contains(pAttribute);
    }
    
    @Override
    public FieldCategory getFieldCategory(Class<?> pClass) {
        if (pClass.isArray()) {
            return FieldCategory.ARRAY;
        }
        if (pClass.isEnum()) {
            return FieldCategory.SIMPLE;
        }
        Package tPackage = pClass.getPackage();
        if (tPackage == null || tPackage.getName().startsWith("java")) {
            return FieldCategory.SIMPLE;
        }
        return FieldCategory.COMPLEX;
    }

    @Override
    public boolean isFieldSuppressed(Field pField) {
        if (As.getMetaDataHandler().isFieldSuppressed(pField)) {
            return true;
        }
        if (
            isClassSuppressed(As.getTypeName(pField.getDeclaringClass())) ||
            isClassSuppressed(As.getTypeName(pField.getType())) ||
            isAttributeSuppressed(pField.getName()) ||
            isClassAttributeSuppressed(As.getTypeName(pField.getDeclaringClass()), pField.getName()) ||
            isClassAttributeSuppressed(As.getTypeName(pField.getType()), pField.getName())) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isObjectSuppressed(String pName, Class<?> pClass) {
        if (isClassSuppressed(As.getTypeName(pClass)) ||
            isAttributeSuppressed(pName) ||
            isClassAttributeSuppressed(As.getTypeName(pClass), pName)) {
            return true;
        }
        return false;
    }
    
}
