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

import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.impl.As;

/**
 * Getter from expressions defined in configuration.
 *
 * @param <T> the generic type
 */
public class AsGetMethodFromExpression<T> extends AsGetMethod<T> {

    /** The method. */
    protected AsGetMethodIf<T> mMethod;
    
    /** The true value. */
    private String mTrueValue;
    
    /** The false value. */
    private String mFalseValue;

    /**
     * Instantiates a new as get method from expression.
     *
     * @param pClass the class
     * @param pName the name
     * @param pExpression the expression
     */
    public AsGetMethodFromExpression(Class<T> pClass, String pName, String pExpression) {
        super(pClass, pName);
        if (pExpression.contains("?")) {
            String[] tParts = pExpression.split("[?:]");
            mMethod = create(pClass, tParts[0]);
            mTrueValue = tParts[1];
            mFalseValue = tParts[2];
        }
    }
    
    @Override
    public String getObject(T pItem) {
        if (pItem == null) {
            return null;
        }
        Boolean tValue = (Boolean) mMethod.getObject(pItem);
        return tValue ? mTrueValue : mFalseValue;
    }

    @Override
    public String getText(T pItem, AsDataSourceServiceIf pService) {
        String tText = getObject(pItem);
        if (tText.startsWith(".")) {
            String tTranslation = As.getDictionaryHandler().getTranslation(tText, pService.getLocale());
            if (!tTranslation.startsWith(".")) {
                return tTranslation;
            }
        }
        return tText;
    }
    
}
