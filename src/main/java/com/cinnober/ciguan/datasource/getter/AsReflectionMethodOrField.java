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
package com.cinnober.ciguan.datasource.getter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.CwfBusinessTypes;
import com.cinnober.ciguan.transport.impl.AsRequestTransformerConfiguration;
import com.cinnober.ciguan.transport.util.AsSuppressionDictionary;
import com.cinnober.ciguan.xml.impl.AsDefSuppressAttribute;
import com.cinnober.ciguan.xml.impl.AsDefSuppressClass;
import com.cinnober.ciguan.xml.impl.AsDefSuppressClassAttribute;

/**
 * The Class AsReflectionMethodOrField.
 *
 * @param <T> the generic type
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class AsReflectionMethodOrField<T> {
    
    private static SuppressionDictionary cSuppressionDictionary = new SuppressionDictionary();
    private static final Set<Class<?>> cSuppressionTypeExemptions = new HashSet<Class<?>>();
    private static final Set<Class<?>> cNumberClasses = new HashSet<Class<?>>();
    static {
        cNumberClasses.add(int.class);
        cNumberClasses.add(Integer.class);
        cNumberClasses.add(long.class);
        cNumberClasses.add(Long.class);
        cNumberClasses.add(byte.class);
        cNumberClasses.add(Byte.class);

        // Need to be exempt in order for the set/map filtering mechanisms to work
        cSuppressionTypeExemptions.add(Set.class);
        cSuppressionTypeExemptions.add(Map.class);
        
        initializeSuppressionDictionary();
    }
    
    /** The business type. */
    protected CwfBusinessTypeIf mBusinessType = CwfBusinessTypes.Text;
    
    /** The constant group get method. */
    protected Method mConstantGroupGetMethod;
    
    /** The business subtype. */
    protected String mBusinessSubtype;
    
    /**
     * Gets the attribute object.
     *
     * @param pOwner the owner
     * @return the attribute object
     */
    public abstract Object getAttributeObject(Object pOwner);
    
    /**
     * Gets the attribute class.
     *
     * @return the attribute class
     */
    public abstract Class<?> getAttributeClass();
    
    /**
     * Gets the business type.
     *
     * @return the business type
     */
    public CwfBusinessTypeIf getBusinessType() {
        return mBusinessType;
    }
    
    /**
     * Gets the constant group.
     *
     * @return the constant group
     */
    public Method getConstantGroup() {
        return mConstantGroupGetMethod;
    }
    
    /**
     * Gets the business subtype.
     *
     * @return the business subtype
     */
    public String getBusinessSubtype() {
        return mBusinessSubtype;
    }
    
    /**
     * Gets the getter.
     *
     * @param <T> the generic type
     * @param pClass the class
     * @param pAttributeName the attribute name
     * @return the getter
     */
    private static <T> AsReflectionMethodOrField getGetter0(Class<T> pClass, String pAttributeName) {
        

        if (pAttributeName.indexOf('.') >= 0) {
            return new AsReflectionGetterWithPath(pClass, pAttributeName.split("\\."));
        }
        AsGetMethodIf<T> tRegisteredMethod = As.getPreRegisteredGetter(pClass, pAttributeName);
        if (tRegisteredMethod != null) {
            return new WrappedGetMethod(tRegisteredMethod);
        }
        try {
            return new AsReflectionField(pClass, pAttributeName);
        }
        catch (NoSuchFieldException e) {
            // continue
        }
        try {
            return new AsReflectionMethod(pClass, "get" + initCap(pAttributeName));
        }
        catch (NoSuchMethodException e) {
            // no method and no field, throw exception
            throw new RuntimeException(pAttributeName + ":  no such method or field in class " + pClass.getName(), e);
        }
    }
    
    /**
     * Gets the getter.
     *
     * @param <T> the generic type
     * @param pClass the class
     * @param pAttributeName the attribute name
     * @return the getter
     */
    public static <T> AsReflectionMethodOrField getGetter(Class<T> pClass, String pAttributeName) {
        AsReflectionMethodOrField tGetter = getGetter0(pClass, pAttributeName);
        
        // Check for suppression
        if (isSuppressed(tGetter)) {
            throw new AsSuppressedException(tGetter);
        }
        
        return tGetter;
    }

    private static void initializeSuppressionDictionary() {
        AsRequestTransformerConfiguration tCfg =
            As.getTransportConfiguration().getRequestTransformerConfiguration();
        
        // Attributes
        for (AsDefSuppressAttribute tAttr : tCfg.getSuppressAttributes()) {
            if (isEmpty(tAttr.getDirection())) {
                cSuppressionDictionary.addAttribute(tAttr.getAttributeName());
            }
            else if (tAttr.getDirection().equals("in")) {
                cSuppressionDictionary.addAttribute(tAttr.getAttributeName());
            }
        }
        
        // Classes
        for (AsDefSuppressClass tClass : tCfg.getSuppressClasses()) {
            if (isEmpty(tClass.getDirection())) {
                cSuppressionDictionary.addClass(tClass.getClassName());
            }
            else if (tClass.getDirection().equals("in")) {
                cSuppressionDictionary.addClass(tClass.getClassName());
            }
        }
        
        // Class attributes
        for (AsDefSuppressClassAttribute tClassAttribute : tCfg.getSuppressClassAttributes()) {
            if (isEmpty(tClassAttribute.getDirection())) {
                cSuppressionDictionary.addClassAttribute(
                    tClassAttribute.getClassName(), tClassAttribute.getAttributeName());
            }
            else if (tClassAttribute.getDirection().equals("in")) {
                cSuppressionDictionary.addClassAttribute(
                    tClassAttribute.getClassName(), tClassAttribute.getAttributeName());
            }
        }
    }
    
    private static boolean isEmpty(String pString) {
        return pString == null || pString.isEmpty();
    }
    
    private static boolean isSuppressed(AsReflectionMethodOrField pMethodOrField) {
        if (pMethodOrField instanceof AsReflectionField) {
            return isSuppressed((AsReflectionField) pMethodOrField);
        }
        if (pMethodOrField instanceof AsReflectionMethod) {
            return isSuppressed((AsReflectionMethod) pMethodOrField);
        }
        return false;
    }
    
    private static boolean isSuppressed(AsReflectionMethod pMethod) {
        Method tMethod = pMethod.getMethod();
        if (cSuppressionDictionary.isClassSuppressed(As.getTypeName(tMethod.getDeclaringClass())) ||
            cSuppressionDictionary.isClassSuppressed(As.getTypeName(tMethod.getReturnType())) ||
            cSuppressionDictionary.isAttributeSuppressed(pMethod.getAttributeName()) ||
            cSuppressionDictionary.isClassAttributeSuppressed(
                As.getTypeName(tMethod.getDeclaringClass()), pMethod.getAttributeName()) ||
            cSuppressionDictionary.isClassAttributeSuppressed(
                As.getTypeName(tMethod.getReturnType()), pMethod.getAttributeName())) {
            return !cSuppressionTypeExemptions.contains(tMethod.getReturnType());
        }
        return false;
    }
    
    private static boolean isSuppressed(AsReflectionField pField) {
        Field tField = pField.getField();
        if (Modifier.isStatic(tField.getModifiers()) ||
            Modifier.isTransient(tField.getModifiers()) ||
            Modifier.isFinal(tField.getModifiers()) ||
            cSuppressionDictionary.isClassSuppressed(As.getTypeName(tField.getDeclaringClass())) ||
            cSuppressionDictionary.isClassSuppressed(As.getTypeName(tField.getType())) ||
            cSuppressionDictionary.isAttributeSuppressed(tField.getName()) ||
            cSuppressionDictionary.isClassAttributeSuppressed(
                As.getTypeName(tField.getDeclaringClass()), tField.getName()) ||
            cSuppressionDictionary.isClassAttributeSuppressed(As.getTypeName(tField.getType()), tField.getName())) {
            return !cSuppressionTypeExemptions.contains(tField.getType());
        }
        return false;
    }
    
    /**
     * Converts the first character of the string to upper case
     *
     * @param pAttributeName the attribute name
     * @return the string
     */
    private static String initCap(String pAttributeName) {
        return pAttributeName.substring(0, 1).toUpperCase() + pAttributeName.substring(1);
    }
    
    /**
     * Converts the first character of the string to lower case
     *
     * @param pAttributeName the attribute name
     * @return the string
     */
    private static String initLow(String pAttributeName) {
        return pAttributeName.substring(0, 1).toLowerCase() + pAttributeName.substring(1);
    }
    
    /**
     * The Class AsReflectionGetterWithPath.
     */
    private static class AsReflectionGetterWithPath extends AsReflectionMethodOrField {
        
        /** The getters. */
        AsReflectionMethodOrField[] mGetters;
        
        /**
         * Instantiates a new as reflection getter with path.
         *
         * @param <T> the generic type
         * @param pClass the class
         * @param pPath the path
         */
        public <T> AsReflectionGetterWithPath(Class<T> pClass, String[] pPath) {
            mGetters = new AsReflectionMethodOrField[pPath.length];
            mGetters[0] = getGetter(pClass, pPath[0]);
            for (int i = 1; i < pPath.length; i++) {
                mGetters[i] = getGetter(mGetters[i - 1].getAttributeClass(), pPath[i]);
            }
            AsReflectionMethodOrField tLastGetter = mGetters[mGetters.length - 1];
            mBusinessType = tLastGetter.getBusinessType();
            mBusinessSubtype = tLastGetter.getBusinessSubtype();
            mConstantGroupGetMethod = tLastGetter.getConstantGroup();
        }
        
        @Override
        public Class<?> getAttributeClass() {
            return mGetters[mGetters.length - 1].getAttributeClass();
        }
        
        @Override
        public Object getAttributeObject(Object pOwner) {
            Object tValue = pOwner;
            for (int i = 0; i < mGetters.length && tValue != null; i++) {
                tValue = mGetters[i].getAttributeObject(tValue);
            }
            return tValue;
        }
    }
    
    /**
     * The Class AsReflectionField.
     */
    private static class AsReflectionField extends AsReflectionMethodOrField {
        
        /** The m field. */
        private final Field mField;

        /**
         * Instantiates a new as reflection field.
         *
         * @param pClass the class
         * @param pField the field
         * @throws NoSuchFieldException if such a field does not exist.
         */
        public AsReflectionField(Class<?> pClass, String pField) throws NoSuchFieldException {
            mField = pClass.getField(pField);
            
            //mMetaDataComponent.getBussinessType(mField);
            
            mBusinessType = As.getMetaDataHandler().getBusinessType(mField);
            Class<?> tConstantClass = As.getMetaDataHandler().getConstantType(mField);
            if (tConstantClass != null) {
                try {
                    mConstantGroupGetMethod = As.getMetaDataHandler().getConstantGroupGetter(tConstantClass);
                    mBusinessType = CwfBusinessTypes.Constant;
                    mBusinessSubtype = As.getTypeName(tConstantClass);
                }
                catch (Exception e) {
                    // could not get method
                }
            }
            else if (mBusinessType != null) {
                // already set
            }
            else if (mField.getType().isEnum()) {
                mBusinessType = CwfBusinessTypes.Enum;
                mBusinessSubtype = As.getTypeName(getAttributeClass());
            }
            else if (cNumberClasses.contains(getAttributeClass())) {
                mBusinessType = CwfBusinessTypes.Number;
            }
            else if (getAttributeClass() == Boolean.class || getAttributeClass() == boolean.class) {
                mBusinessType = CwfBusinessTypes.Boolean;
            }
            else if (getAttributeClass() == String.class) {
                mBusinessType = CwfBusinessTypes.Text;
            }
            else {
                mBusinessType = CwfBusinessTypes.Object;
                mBusinessSubtype = As.getTypeName(getAttributeClass());
            }
        }
        
        @Override
        public Object getAttributeObject(Object pItem) {
            try {
                return pItem == null ? null : mField.get(pItem);
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to retrieve value from filter target object", e);
            }
        }

        @Override
        public Class<?> getAttributeClass() {
            return mField.getType();
        }
        
        public Field getField() {
            return mField;
        }
        
    }

    /**
     * The Class AsReflectionMethod.
     */
    private static class AsReflectionMethod extends AsReflectionMethodOrField {
        
        /** The method. */
        private final Method mMethod;

        /**
         * Instantiates a new as reflection method.
         *
         * @param pClass the class
         * @param pMethod the method
         * @throws NoSuchMethodException if such a method does not exist.
         */
        public AsReflectionMethod(Class<?> pClass, String pMethod) throws NoSuchMethodException {
            mMethod = pClass.getMethod(pMethod);
        }
        
        @Override
        public Class<?> getAttributeClass() {
            return mMethod.getReturnType();
        }
        
        @Override
        public Object getAttributeObject(Object pItem) {
            try {
                return pItem == null ? null : mMethod.invoke(pItem);
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to retrieve value from filter target object", e);
            }
        }
        
        public Method getMethod() {
            return mMethod;
        }
        
        public String getAttributeName() {
            return initLow(mMethod.getName().substring(3));
        }
        
    }
    
    /**
     * The Class WrappedGetMethod.
     *
     * @param <T> the generic type
     */
    private static class WrappedGetMethod<T> extends AsReflectionMethodOrField<T> {

        /** The method. */
        AsGetMethodIf<T> mMethod;
        
        /**
         * Instantiates a new wrapped get method.
         *
         * @param pRegisteredMethod the registered method
         */
        public WrappedGetMethod(AsGetMethodIf<T> pRegisteredMethod) {
            mBusinessType = pRegisteredMethod.getBusinessType();
            mBusinessSubtype = pRegisteredMethod.getBusinessSubtype();
            mMethod = pRegisteredMethod;
        }
        
        @Override
        public Class<?> getAttributeClass() {
            try {
                Method tGetter = mMethod.getClass().getMethod("getObject", mMethod.getItemClass());
                return tGetter.getReturnType();
            }
            catch (Exception e) {
                // do nothing
            }
            return Object.class;
        }
        
        @Override
        public Object getAttributeObject(Object pItem) {
            return mMethod.getObject((T) pItem);
        }
        
    }

    /**
     * Extended supression dictionary to make the protected methods accessible
     */
    private static class SuppressionDictionary extends AsSuppressionDictionary {
        
        public boolean isAttributeSuppressed(String pAttribute) {
            return super.isAttributeSuppressed(pAttribute);
        }

        public boolean isClassSuppressed(String pClass) {
            return super.isClassSuppressed(pClass);
        }
        
        public boolean isClassAttributeSuppressed(String pClass, String pAttribute) {
            return super.isClassAttributeSuppressed(pClass, pAttribute);
        }
        
    }
    
}
