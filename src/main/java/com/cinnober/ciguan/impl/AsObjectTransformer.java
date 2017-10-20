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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Arrays;

import com.cinnober.ciguan.AsModelExtensionIf;
import com.cinnober.ciguan.transport.AsSuppressionDictionaryIf;
import com.cinnober.ciguan.transport.impl.AsRequestTransformerConfiguration;
import com.cinnober.ciguan.xml.impl.AsDefSuppressAttribute;
import com.cinnober.ciguan.xml.impl.AsDefSuppressClass;
import com.cinnober.ciguan.xml.impl.AsDefSuppressClassAttribute;

/**
 *
 * Base class for object transformations
 * 
 */
public abstract class AsObjectTransformer {

    protected static final String IN = "in";
    protected static final String OUT = "out";
    
    protected final AsSuppressionDictionaryIf mIn;
    protected final AsSuppressionDictionaryIf mOut;
    protected final AsRequestTransformerConfiguration mConfiguration;
    
    public AsObjectTransformer() {
        mIn = As.getBeanFactory().create(AsSuppressionDictionaryIf.class);
        mOut = As.getBeanFactory().create(AsSuppressionDictionaryIf.class);
        mConfiguration = As.getTransportConfiguration().getRequestTransformerConfiguration();
        initializeSuppressionDictionary();
    }

    /**
     * Set the message part of a TapStatus object if the message is not already set
     * @param pMessage
     */
    protected void setTapStatusMessage(Object pMessage) {
    }
    
    /**
     * Get the raw attribute from the given object 
     * @param pField the field from which the value should be retrieved
     * @param pObject the object to retrieve the value from
     * @return the value of the given attribute as an object
     */
    protected Object getObject(Field pField, Object pObject) {
        try {
            return pField.get(pObject);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the value of the given field
     * @param pField the field from which the value should be retrieved
     * @param pObject the object to retrieve the value from
     * @return the value of the given attribute as a string
     */
    protected String getValue(Field pField, Object pObject) {
        try {
            Object tObject = pField.get(pObject);
            return tObject == null ? null : tObject.toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Create an object of the given type
     * @param pTypeName
     * @return a newly created instance of the given type
     */
    protected Object createObject(String pTypeName) {
        try {
            Class<?> tClass = realTypeOf(As.getType(pTypeName));
            if (!As.getMetaDataHandler().getSearchPackages().contains(tClass.getPackage().getName())) {
                throw new IllegalAccessException("No matching search package found");
            }
            Object tObject = tClass.newInstance();
            return tObject;
        }
        catch (Exception e) {
            throw new RuntimeException("Type '" + pTypeName + "' could not be instantiated");
        }
    }
    
    /**
     * Create an object array of the given type and size
     * @param pTypeName
     * @param pSize
     * @return a newly created array instance of the given type
     */
    protected Object[] createObjectArray(String pTypeName, int pSize) {
        try {
            Class<?> tClass = realTypeOf(As.getType(pTypeName));
            Object tArray = Array.newInstance(tClass, pSize);
            return (Object[]) tArray;
        }
        catch (Exception e) {
            throw new RuntimeException("Type '" + pTypeName + "' could not be instantiated");
        }
    }
    
    /**
     * Replace a possible model extension type with its model supertype
     * @param pClass the type to be substituted
     * @return the real type to 
     */
    protected Class<?> realTypeOf(Class<?> pClass) {
        Class<?> tClass = pClass;
        while (AsModelExtensionIf.class.isAssignableFrom(tClass)) {
            tClass = tClass.getSuperclass();
        }
        return tClass;
    }
    
    /**
     * Format a primitive array to a string
     * @param pArray
     * @return a formatted string
     */
    protected String primitiveArrayToString(Object pArray) {
        Class<?> tElementType = pArray.getClass().getComponentType();
        if (tElementType == int.class) {
            return Arrays.toString((int[]) pArray);
        }
        if (tElementType == long.class) {
            return Arrays.toString((long[]) pArray);
        }
        if (tElementType == float.class) {
            return Arrays.toString((float[]) pArray);
        }
        if (tElementType == double.class) {
            return Arrays.toString((double[]) pArray);
        }
        if (tElementType == byte.class) {
            return Arrays.toString((byte[]) pArray);
        }
        if (tElementType == boolean.class) {
            return Arrays.toString((boolean[]) pArray);
        }
        if (tElementType == char.class) {
            return Arrays.toString((char[]) pArray);
        }
        if (tElementType == short.class) {
            return Arrays.toString((short[]) pArray);
        }
        return Arrays.toString((Object[]) pArray);
    }
    
    /**
     * Set the value of the given field
     * @param pField
     * @param pObject
     * @param pValue
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void setField(Field pField, Object pObject, String pValue) {
        try {
            Class<?> tClass = pField.getType();
            if (tClass == int.class || tClass == Integer.class) {
                pField.set(pObject, Integer.valueOf(pValue));
            }
            else if (tClass == long.class || tClass == Long.class) {
                pField.set(pObject, Long.valueOf(pValue));
            }
            else if (tClass == float.class || tClass == Float.class) {
                pField.set(pObject, Float.valueOf(pValue));
            }
            else if (tClass == double.class || tClass == Double.class) {
                pField.set(pObject, Double.valueOf(pValue));
            }
            else if (tClass == byte.class || tClass == Byte.class) {
                pField.set(pObject, Byte.valueOf(pValue));
            }
            else if (tClass == short.class || tClass == Short.class) {
                pField.set(pObject, Short.valueOf(pValue));
            }
            else if (tClass == boolean.class) {
                pField.set(pObject, Boolean.valueOf(pValue));
            }
            else if (tClass == Boolean.class) {
                pField.set(pObject, pValue == null ? null : Boolean.valueOf(pValue));
            }
            else if (tClass == String.class) {
                pField.set(pObject, pValue);
            }
            else if (tClass == BigInteger.class) {
                pField.set(pObject, new BigInteger(pValue));
            }
            else if (tClass.isEnum()) {
                pField.set(pObject, Enum.valueOf((Class<Enum>) tClass, pValue));
            }
            else {
                throw new RuntimeException("Simple type not handled: " + tClass.getName());
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Initialize the inbound and outbound suppression dictionaries
     */
    protected void initializeSuppressionDictionary() {
        
        // Attributes
        for (AsDefSuppressAttribute tAttr : mConfiguration.getSuppressAttributes()) {
            if (isEmpty(tAttr.getDirection())) {
                mIn.addAttribute(tAttr.getAttributeName());
                mOut.addAttribute(tAttr.getAttributeName());
            }
            else if (tAttr.getDirection().equals(IN)) {
                mIn.addAttribute(tAttr.getAttributeName());
            }
            else if (tAttr.getDirection().equals(OUT)) {
                mOut.addAttribute(tAttr.getAttributeName());
            }
        }
        
        // Classes
        for (AsDefSuppressClass tClass : mConfiguration.getSuppressClasses()) {
            if (isEmpty(tClass.getDirection())) {
                mIn.addClass(tClass.getClassName());
                mOut.addClass(tClass.getClassName());
            }
            else if (tClass.getDirection().equals(IN)) {
                mIn.addClass(tClass.getClassName());
            }
            else if (tClass.getDirection().equals(OUT)) {
                mOut.addClass(tClass.getClassName());
            }
        }
        
        // Class attributes
        for (AsDefSuppressClassAttribute tClassAttribute : mConfiguration.getSuppressClassAttributes()) {
            if (isEmpty(tClassAttribute.getDirection())) {
                mIn.addClassAttribute(tClassAttribute.getClassName(), tClassAttribute.getAttributeName());
                mOut.addClassAttribute(tClassAttribute.getClassName(), tClassAttribute.getAttributeName());
            }
            else if (tClassAttribute.getDirection().equals(IN)) {
                mIn.addClassAttribute(tClassAttribute.getClassName(), tClassAttribute.getAttributeName());
            }
            else if (tClassAttribute.getDirection().equals(OUT)) {
                mOut.addClassAttribute(tClassAttribute.getClassName(), tClassAttribute.getAttributeName());
            }
        }
    }

    protected boolean isEmpty(String pString) {
        return pString == null || pString.isEmpty();
    }
    
}
