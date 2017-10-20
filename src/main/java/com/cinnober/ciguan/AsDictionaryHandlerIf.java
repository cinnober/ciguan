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

import java.util.Collection;
import java.util.Locale;

import com.cinnober.ciguan.data.AsDictionaryLanguage;
import com.cinnober.ciguan.data.AsDictionaryWord;

/**
 * Dictionary handler.
 */
public interface AsDictionaryHandlerIf {

    /** Singleton instance of the dictionary handler. */
    AsSingletonBean<AsDictionaryHandlerIf> SINGLETON = AsSingletonBean.create(AsDictionaryHandlerIf.class);
 
    /**
     * Return all words.
     *
     * @return the dictionary words
     */
    Collection<AsDictionaryWord> getDictionaryWords();
    
    /**
     * Return all languages.
     *
     * @return the dictionary languages
     */
    Collection<AsDictionaryLanguage> getDictionaryLanguages();
    
    /**
     * Get the language identified by the given code.
     *
     * @param pLanguageCode the language code
     * @return the dictionary language
     */
    AsDictionaryLanguage getDictionaryLanguage(String pLanguageCode);    
    
    /**
     * Get the default language.
     *
     * @return the default language
     */
    AsDictionaryLanguage getDefaultLanguage();
    
    /**
     * Get the translation for the given key.
     *
     * @param pKey the key
     * @param pLocale the locale
     * @return the translation
     */
    String getTranslation(String pKey, Locale pLocale);
    
}
