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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import com.cinnober.ciguan.data.AsMetaField;
import com.cinnober.ciguan.data.AsMetaObject;
import com.cinnober.ciguan.xml.impl.AsDefMetaData;

/**
 * Handling of meta data.
 */
public interface AsMetaDataHandlerIf {
    
    /** Singleton instance of the meta data handler. */
    AsSingletonBean<AsMetaDataHandlerIf> SINGLETON = AsSingletonBean.create(AsMetaDataHandlerIf.class);

    /**
     * Gets the type from the given type name.
     * @param pObjectType the object type
     * @return the type
     */
    <T> Class<T> getType(String pObjectType);

    /**
     * Gets the type name from the given type
     * @param pClass
     * @return the full name of the class, including a possible namespace prefix
     */
    <T> String getTypeName(Class<T> pClass);
    
    /**
     * Gets the id field.
     *
     * @param pItemClass the item class
     * @return the id field
     */
    String getIdField(Class<?> pItemClass);

    /**
     * Gets the display field.
     *
     * @param pItemClass the item class
     * @return the display field
     */
    String getDisplayField(Class<?> pItemClass);

    /**
     * Gets the business type.
     *
     * @param pField the field
     * @return the business type
     */
    CwfBusinessTypeIf getBusinessType(Field pField);

    /**
     * Gets the constant type.
     *
     * @param pField the field
     * @return the constant type
     */
    Class<?> getConstantType(Field pField);

    /**
     * Gets the referenced type.
     *
     * @param pField the field
     * @return the referenced type
     */
    Class<?> getReferencedType(Field pField);

    /**
     * Gets the constant group getter.
     *
     * @param pConstantClass the constant class
     * @return the constant group getter
     */
    Method getConstantGroupGetter(Class<?> pConstantClass);

    /**
     * Checks if is field suppressed.
     *
     * @param pField the field
     * @return {@code true}, if the field is suppressed
     */
    boolean isFieldSuppressed(Field pField);

    /**
     * Get the configured metadata classes.
     *
     * @return the meta data classes
     */
    /**
     * @return a list of the meta data.
     */
    List<AsDefMetaData> getMetaDataClasses();

    /**
     * Gets the meta data.
     *
     * @param <T> the type
     * @param pClass the class
     * @return the meta data
     */
    <T> AsMetaObject<T> getMetaData(Class<T> pClass);
 
    /**
     * Gets the field.
     *
     * @param pClass the class
     * @param pColumnName the column name
     * @return the field
     */
    AsMetaField getField(Class<?> pClass, String pColumnName);

    /**
     * Gets the field.
     *
     * @param pMeta the meta
     * @param pColumnName the column name
     * @return the field
     */
    AsMetaField getField(AsMetaObject<?> pMeta, String pColumnName);
    
    /**
     * Gets the configured search packages.
     * 
     * @return the configured search packages
     */
    Set<String> getSearchPackages();
    
}
