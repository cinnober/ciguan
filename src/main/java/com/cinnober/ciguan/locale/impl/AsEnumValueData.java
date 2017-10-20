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

import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.data.AsDictionaryWord;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;

/**
 * Class holding an enum element value
 */
public class AsEnumValueData implements MvcModelAttributesIf {

    public String mEnum;
    public String mEnumName;
    public String mEnumValue;
    public AsDictionaryWord mWord;

    public AsEnumValueData(String pEnum, String pEnumName, String pEnumValue, AsDictionaryWord pTranslation) {
        mEnum = pEnum;
        mEnumName = pEnumName;
        mEnumValue = pEnumValue;
        mWord = pTranslation;
    }
    
    public String getEnumValue(Locale pLocale) {
        if (mWord == null) {
            return mEnumValue;
        }
        return mWord.getText(pLocale);
    }

    public static AsGetMethodIf<AsEnumValueData> getMethod(String pAttributeName) {
        return new AsGetMethod<AsEnumValueData>(AsEnumValueData.class, pAttributeName) {
            @Override
            public Object getObject(AsEnumValueData pItem) {
                return pItem.mEnumValue;
            }
            
            @Override
            public String getText(AsEnumValueData pItem, AsDataSourceServiceIf pService) {
                return pItem.getEnumValue(pService.getLocale());
            }
        };
    }

    public String getEnum() {
        return mEnum;
    }

    public String getEnumName() {
        return mEnumName;
    }

    public String getEnumValue() {
        return mEnumValue;
    }

}
