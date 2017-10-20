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
import java.util.Map;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.locale.impl.AsLocalePattern;

/**
 * Locale impl.
 */
public class AsLocale implements MvcModelAttributesIf {

    /** The id. */
    public String id;

    /** The group separator. */
    public String groupSeparator;

    /** The decimal separator. */
    public String decimalSeparator;

    /** The patterns. */
    public AsLocalePattern[] patterns;

    /**
     * Instantiates a new as locale.
     *
     * @param pData the data
     * @param pDefault the default
     */
    public AsLocale(CwfDataIf pData, AsLocalePattern... pDefault) {
        id = pData.getProperty(ATTR_ID);
        groupSeparator = pData.getProperty(ATTR_GROUP);
        decimalSeparator = pData.getProperty(ATTR_DECIMAL);

        Map<String, AsLocalePattern> tPatternMap = new HashMap<String, AsLocalePattern>();
        for (AsLocalePattern tPattern : pDefault) {
            tPatternMap.put(tPattern.type, tPattern);
        }

        for (CwfDataIf tData : pData.getObjectList(TAG_PATTERN)) {
            AsLocalePattern tPattern = new AsLocalePattern();
            tPattern.type = tData.getProperty(ATTR_TYPE);
            tPattern.value = tData.getProperty(ATTR_VALUE);
            tPatternMap.put(tPattern.type, tPattern);
        }

        patterns = tPatternMap.values().toArray(new AsLocalePattern[tPatternMap.size()]);
    }

}
