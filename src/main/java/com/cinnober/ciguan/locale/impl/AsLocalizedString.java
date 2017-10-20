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
package com.cinnober.ciguan.locale.impl;

import java.util.Locale;

import com.cinnober.ciguan.impl.As;

/**
 *
 * Class holding a localized string, which is actually a key into the dictionary
 *
 */
public class AsLocalizedString implements Comparable<AsLocalizedString> {
    
    private final String mValue;
    private String mLocalizedValue;
    private String mDefaultValue = "?";
    
    public AsLocalizedString(String pValue) {
        mValue = pValue;
        mLocalizedValue = pValue;
    }
    public AsLocalizedString(String pValue, String pDefaultValue) {
        this(pValue);
        mDefaultValue = pDefaultValue;
    }

    @Override
    public String toString() {
        return mValue;
    }

    public String toString(Locale pLocale) {
        String tValue = As.getDictionaryHandler().getTranslation(mValue, pLocale);
        mLocalizedValue = mValue == tValue ? mDefaultValue : tValue;
        return mLocalizedValue;
    }

    @Override
    public int compareTo(AsLocalizedString pO) {
        return mLocalizedValue.compareTo(pO.mLocalizedValue);
    }
}
