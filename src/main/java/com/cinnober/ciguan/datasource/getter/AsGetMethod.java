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

import java.lang.reflect.Method;

import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.data.AsDictionaryWord;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.impl.AsMapRefData;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.CwfBusinessTypes;
import com.cinnober.ciguan.locale.impl.AsConstantGroupValueData;
import com.cinnober.ciguan.locale.impl.AsEnumValueData;
import com.cinnober.ciguan.locale.impl.AsLocalizedString;

/**
 * Base class for attribute value getters.
 *
 * @param <T> The type of object that the value is retrieved from
 */
public abstract class AsGetMethod<T> implements AsGetMethodIf<T> {

    /** The attribute name. */
    private final String mAttributeName;
    
    /** The item class. */
    private final Class<T> mItemClass;
    
    /**
     * Instantiates a new get method.
     *
     * @param pClass the class
     * @param pAttributeName the attribute name
     */
    protected AsGetMethod(Class<T> pClass, String pAttributeName) {
        mItemClass = pClass;
        mAttributeName = pAttributeName;
    }
    
    @Override
    public String getAttributeName() {
        return mAttributeName;
    }
    
    @Override
    public Class<T> getItemClass() {
        return mItemClass;
    }
    
    @Override
    public String getText(T pItem, AsDataSourceServiceIf pService) {
        Object tValue = getObject(pItem);
        if (tValue == null) {
            return "";
        }
        if (pService == null) {
            return tValue.toString();
        }
        Method tMethod = getConstantGroupMethod();
        if (tMethod != null) {
            try {
                String tText = (String) tMethod.invoke(null, tValue);
                String tTranslation = As.getDictionaryHandler().getTranslation(
                    ".constant." + As.getTypeName(tMethod.getDeclaringClass().getDeclaringClass()) +
                    "." + tText, pService.getLocale());
                if (tTranslation.startsWith(".constant.")) {
                    tTranslation = As.getDictionaryHandler().getTranslation(".constant." + tText, pService.getLocale());
                }
                return tTranslation;
            }
            catch (Exception e) {
                return "?";
            }
        }
        if (getBusinessType() == CwfBusinessTypes.Enum) {
            String tText = tValue.toString();
            String tTranslation = As.getDictionaryHandler().getTranslation(
                ".enum." + As.getTypeName(tValue.getClass()) + "." + tText, pService.getLocale());
            if (tTranslation.startsWith(".enum.")) {
                tTranslation = As.getDictionaryHandler().getTranslation(".enum." + tText, pService.getLocale());
            }
            return tTranslation;
        }
        if (tValue instanceof AsLocalizedString) {
            return ((AsLocalizedString) tValue).toString(pService.getLocale());
        }
        return As.getFormatter().format(tValue, getBusinessType(), pService.getLocale());
    }
    
    @Override
    public final String getValue(T pItem) {
        Object tValue = getObject(pItem);
        return tValue == null ? "" : tValue.toString();
    }

    @Override
    public CwfBusinessTypeIf getBusinessType() {
        return CwfBusinessTypes.Text;
    }
    
    @Override
    public String getBusinessSubtype() {
        return null;
    }

    /**
     * Gets the constant group method.
     *
     * @return the constant group method
     */
    public Method getConstantGroupMethod() {
        return null;
    }

    @Override
    public String toString() {
        return getAttributeName();
    }
    
    /**
     * Factory method for value getters.
     *
     * @param <T> the generic type
     * @param pClass the class
     * @param pAttributeName the attribute name
     * @return the getter method
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> AsGetMethodIf<T> create(Class<T> pClass, String pAttributeName) {
        // Look up pre-registered getters and return them before attempting to create
        // a new instance
        AsGetMethodIf<T> tGetter = As.getPreRegisteredGetter(pClass, pAttributeName);
        if (tGetter != null) {
            return tGetter;
        }
        if (pClass == AsDictionaryWord.class && pAttributeName.equals("text")) {
            return (AsGetMethodIf<T>) AsDictionaryWord.getMethod();
        }
        if (pClass == AsConstantGroupValueData.class && 
            pAttributeName.equals(MvcModelAttributesIf.ATTR_CONSTANT_NAME)) {
            return (AsGetMethodIf<T>) AsConstantGroupValueData.getMethod(pAttributeName);
        }
        if (pClass == AsEnumValueData.class && 
            pAttributeName.equals(MvcModelAttributesIf.ATTR_ENUM_VALUE)) {
            return (AsGetMethodIf<T>) AsEnumValueData.getMethod(pAttributeName);
        }
        // No pre-registered getter exists, create one
        String[] tAttributeNames = pAttributeName.split(",");
        switch (tAttributeNames.length) {
            case 1:
                if (AsMapRefData.class.isAssignableFrom(pClass)) {
                    try {
                        return new AsObjectGetMethod(pClass, pAttributeName);
                    }
                    catch (Exception e) {
                        return (AsGetMethodIf<T>) AsMapGetMethod.create(pAttributeName);
                    }
                }
                return new AsObjectGetMethod(pClass, pAttributeName);
            case 2:
                return new GetterWithTwoFields<T>(pClass, pAttributeName, tAttributeNames);
            case 3:
                return new GetterWithThreeFields<T>(pClass, pAttributeName, tAttributeNames);
            default:
                throw new RuntimeException("Getter with more than 3 fields not implemented");
        }
    }

    /**
     * The Class GetterWithTwoFields.
     *
     * @param <T> the generic type
     */
    private static class GetterWithTwoFields<T> extends AsGetMethod<T> {

        /** The getter methods. */
        AsGetMethodIf<T>[] mAttr;
        
        /**
         * Instantiates a new getter with two fields.
         *
         * @param pClass the class
         * @param pName the name
         * @param pAttr the attr
         */
        @SuppressWarnings("unchecked")
        public GetterWithTwoFields(Class<T> pClass, String pName, String[] pAttr) {
            super(pClass, pName);
            mAttr = new AsGetMethodIf[pAttr.length];
            for (int i = 0; i < pAttr.length; i++) {
                mAttr[i] = create(pClass, pAttr[i]);
            }
        }
        
        @Override
        public Object getObject(T pItem) {
            return mAttr[0].getValue(pItem) + "-" + mAttr[1].getValue(pItem);
        }
        
        @Override
        public String getText(T pItem, AsDataSourceServiceIf pService) {
            return mAttr[0].getObject(pItem) + "-" + mAttr[1].getText(pItem, pService);
        }
    }
    
    /**
     * The Class GetterWithThreeFields.
     *
     * @param <T> the generic type
     */
    private static class GetterWithThreeFields<T> extends GetterWithTwoFields<T> {
        
        /**
         * Instantiates a new getter with three fields.
         *
         * @param pClass the class
         * @param pName the name
         * @param pAttr the attr
         */
        public GetterWithThreeFields(Class<T> pClass, String pName, String[] pAttr) {
            super(pClass, pName, pAttr);
        }
        
        @Override
        public Object getObject(T pItem) {
            return super.getObject(pItem) + "-" + mAttr[2].getValue(pItem);
        }
        
        @Override
        public String getText(T pItem, AsDataSourceServiceIf pService) {
            return super.getText(pItem, pService) + "-" + mAttr[2].getText(pItem, pService);
        }
    }
    
}
