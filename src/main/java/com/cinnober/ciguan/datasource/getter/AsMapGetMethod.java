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

import com.cinnober.ciguan.datasource.impl.AsMapRefData;

/**
 * Implementation of the value getter where the item is an instance
 * of AsMapRefData or a subclass to it. This class should never be instantiated directly.
 * Use {@link AsGetMethod#create(Class, String)} to retrieve an instance.
 */
public class AsMapGetMethod extends AsGetMethod<AsMapRefData> {

    /**
     * Instantiates a new as map get method.
     *
     * @param pAttributeName the attribute name
     */
    private AsMapGetMethod(String pAttributeName) {
        super(AsMapRefData.class, pAttributeName);
    }
    
    @Override
    public String getObject(AsMapRefData pItem) {
        if (pItem == null) {
            return null;
        }
        return pItem.getValues().getProperty(getAttributeName());
    }

    /**
     * Creates the.
     *
     * @param pAttributeName the attribute name
     * @return the as map get method
     */
    public static AsMapGetMethod create(String pAttributeName) {
        return new AsMapGetMethod(pAttributeName);
    }
    
}
