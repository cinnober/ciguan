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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;
import com.cinnober.ciguan.impl.As;

/**
 * The Class AsDictionaryWord.
 *
 * @author magnus.lenti, Cinnober Financial Technology
 */
public class AsDictionaryWord {
    
    /** The m translations. */
    public Map<String, String> mTranslations = new HashMap<String, String>();
    
    /** The key. */
    public String key;
    
    /** The m view. */
    public String mView;
    
    /** The m context. */
    public String mContext;
    
    /** The m word. */
    public String mWord;
    
    /**
     * Instantiates a new as dictionary word.
     *
     * @param pKey the key
     */
    public AsDictionaryWord(String pKey) {
        key = pKey.trim();
        String[] tArr = key.split("\\.");
        mView = tArr[0];
        mContext = tArr[1];
        mWord = tArr.length == 3 ? tArr[2] : "";
    }
    

    /**
     * Gets the method.
     *
     * @return the method
     */
    public static AsGetMethodIf<AsDictionaryWord> getMethod() {
        return new AsGetMethod<AsDictionaryWord>(AsDictionaryWord.class, "text") {
            @Override
            public Object getObject(AsDictionaryWord pItem) {
                return this;
            }

            @Override
            public String getText(AsDictionaryWord pItem, AsDataSourceServiceIf pService) {
                return pItem.getText(pService.getLocale());
            }
        };
    }

    /**
     * Adds the translation.
     *
     * @param pLanguageCode the language code
     * @param pTranslation the translation
     */
    public void addTranslation(String pLanguageCode, String pTranslation) {
        mTranslations.put(pLanguageCode, pTranslation);
    }
    
    /**
     * Gets the text.
     *
     * @param pLocale the locale
     * @return the text
     */
    public String getText(Locale pLocale) {
        if (pLocale == null) {
            return key;
        }
        String tText = mTranslations.get(pLocale.toString());
        if (tText == null) {
            tText = mTranslations.get(As.getDictionaryHandler().getDefaultLanguage().getText());
        }
        return tText != null ? tText : key;
    }
    
    /**
     * Gets the word.
     *
     * @return the word
     */
    public String getWord() {
        return mWord;
    }


    /**
     * Gets the view.
     *
     * @return the view
     */
    public String getView() {
        return mView;
    }


    /**
     * Gets the context.
     *
     * @return the context
     */
    public String getContext() {
        return mContext;
    }
    
}
