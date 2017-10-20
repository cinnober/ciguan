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
package com.cinnober.ciguan.data;

import java.util.Locale;

/**
 * Dictionary language.
 */
public class AsDictionaryLanguage {

    /** The key. */
    public String key;
    
    /** The locale. */
    private Locale mLocale;
    
    /**
     * Instantiates a new dictionary language.
     *
     * @param pLanguageCode the language code
     */
    public AsDictionaryLanguage(String pLanguageCode) {
        String[] tParts = pLanguageCode.split("_");
        if (tParts.length == 2) {
            mLocale = new Locale(tParts[0], tParts[1]);
        }
        else {
            mLocale = new Locale(tParts[0], tParts[1], tParts[2]);
        }
        key = pLanguageCode;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
        return mLocale.toString();
    }
    
    /**
     * Gets the locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return mLocale;
    }
    
}
