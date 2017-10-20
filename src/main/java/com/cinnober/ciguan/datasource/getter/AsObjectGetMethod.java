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
import com.cinnober.ciguan.impl.As;

/**
 * Reflection implementation of the value getter. This class should never be
 * instantiated directly. Use {@link AsGetMethod#create(Class, String)} to
 * retrieve an instance.
 * 
 * Note that the attribute name can use the form "foo.bar.value". This will
 * yield a lookup of attribute "foo", followed by a lookup of attribute "bar",
 * and finally a lookup of value "value". If any of the objects being traversed
 * are {@code null}, the retrieved value will be {@code null}. In this case
 * there is no way to determine whether the actual value itself or any of the
 * parent objects were {@code null}.
 * 
 * @param <T>
 *            The top level class from which to retrieve the value
 */
public class AsObjectGetMethod<T> extends AsGetMethod<T> {

    /** The getter. */
    protected AsReflectionMethodOrField<?> mGetter;

    /**
     * Instantiates a new as object get method.
     *
     * @param pClass
     *            the class
     * @param pAttributeName
     *            the attribute name
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public AsObjectGetMethod(Class<T> pClass, String pAttributeName) {
        super(pClass, pAttributeName);
        try {
            mGetter = AsReflectionMethodOrField.getGetter(pClass, pAttributeName);
        }
        catch (AsSuppressedException e) {
            mGetter = new AsNullMethodOrField(pClass, pAttributeName, e.getMethodOrField());
        }
    }

    @Override
    public CwfBusinessTypeIf getBusinessType() {
        return mGetter.getBusinessType();
    }

    @Override
    public String getBusinessSubtype() {
        return mGetter.getBusinessSubtype();
    }

    @Override
    public Object getObject(T pItem) {
        return mGetter.getAttributeObject(pItem);
    }

    @Override
    public Method getConstantGroupMethod() {
        return mGetter.getConstantGroup();
    }

    /**
     * Gets the attribute class.
     *
     * @return the attribute class
     */
    public Class<?> getAttributeClass() {
        return mGetter.getAttributeClass();
    }

    @Override
    public String toString() {
        return getBusinessType() + " " + getAttributeName() + " "
                + As.getTypeName(getAttributeClass());
    }

}
