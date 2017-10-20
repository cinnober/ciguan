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
package com.cinnober.ciguan.datasource.tree;

import java.lang.reflect.Constructor;

import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;
import com.cinnober.ciguan.datasource.getter.AsObjectGetMethod;

/**
 *
 * Helper class to parse filter expressions for tree definitions
 *
 */
public abstract class AsTreeFilterTool {

    /**
     * Creates the filter.
     *
     * @param pExpression the expression
     * @param pItem the item
     * @return the as filter if
     */
    public static AsFilterIf<?> createFilter(String pExpression, Object pItem) {
        if (pExpression.startsWith("class:")) {
            AsFilterIf<?> tFilter = null;
            String tClassName = pExpression.substring(6);
            try {
                Class<?> tClass = Class.forName(tClassName);
                Constructor<?> tConstructor = tClass.getConstructor(pItem.getClass());
                tFilter = (AsFilterIf<?>) tConstructor.newInstance(pItem);
                return tFilter;
            }
            catch (Exception e) {
                throw new RuntimeException("Could not instantiate filter of type " + tClassName, e);
            }
        }
        return null;
    }

    /**
     * Expand expressions of type "../" and replace it with the value of the contained item's
     * corresponding attribute
     *
     * @param pItem the item
     * @param pFilterExpression the filter expression
     * @return the string
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String expandFilterExpression(Object pItem, String pFilterExpression) {
        if (pFilterExpression != null) {
            int tIndex = pFilterExpression.indexOf("../");
            if (tIndex >= 0) {
                String tAttributeName = pFilterExpression.substring(tIndex + 3);
                AsObjectGetMethod tGetter = (AsObjectGetMethod) AsGetMethod.create(pItem.getClass(), tAttributeName);
                return pFilterExpression.substring(0, tIndex) + tGetter.getValue(pItem);
            }
        }
        return pFilterExpression;
    }

    /**
     * Expand expressions of type "../" and replace it with the value of the contained item's
     * corresponding attribute
     *
     * @param pItem the item
     * @param pFilterExpression the filter expression
     * @return the string
     */
    public static String expandFilterExpression(Object pItem, String[] pFilterExpression) {
        StringBuilder tBuilder = new StringBuilder(expandFilterExpression(pItem, pFilterExpression[0]));
        for (int i = 1; i < pFilterExpression.length; i++) {
            tBuilder.append(",");
            tBuilder.append(expandFilterExpression(pItem, pFilterExpression[i]));
        }
        return tBuilder.toString();
    }

}
