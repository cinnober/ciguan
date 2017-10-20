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
package com.cinnober.ciguan.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.cinnober.ciguan.CwfBusinessTypeIf;

/**
 * 
 *  Business types
 */
public enum CwfBusinessTypes implements CwfBusinessTypeIf {

    Amount(Long.class, true),
    BasisPoint(Long.class, true),
    Constant(String.class, false),
    Date(String.class, true),
    DateTime(String.class, true),
    Decimal(Long.class, true),
    InterestRate(Long.class, true),
    MultiLineText(String.class, false),
    Number(Long.class, true),
    Percent(Long.class, true),
    Price(Long.class, true),
    SecondsSinceTime(Integer.class, true),
    Text(String.class, false),
    Time(String.class, true),
    Url(String.class, false),
    Volume(Long.class, true),
    //
    Boolean(Boolean.class, false),
    Password(String.class, false),
    Button(String.class, false),
    Enum(String.class, false),
    Object(Object.class, false),
    Integer(Long.class, true),
    Long(Long.class, true),
    Double(Long.class, true),
    Reference(String.class, false);

    
    private static Map<String, CwfBusinessTypeIf> cMap;

    private Class<?> mClass;
    private boolean mOrdinal;

    private CwfBusinessTypes(Class<?> pClass, boolean pOrdinal) {
        mClass = pClass;
        mOrdinal = pOrdinal;
    }

    @Override
    public Class<?> getUnderlyingType() {
        return mClass;
    }

    @Override
    public boolean isOrdinal() {
        return mOrdinal;
    }
    
    private static Map<String, CwfBusinessTypeIf> getMap() {
        if (cMap == null) {
            cMap = new HashMap<String, CwfBusinessTypeIf>();
            addBusinessTypes(values());
        }
        return cMap;
    }
    
    public static Collection<CwfBusinessTypeIf> getBusinessTypes() {
        return Collections.unmodifiableCollection(getMap().values());
    }
    
    public static void addBusinessTypes(CwfBusinessTypeIf... pBusinessTypes) {
        for (CwfBusinessTypeIf tBusinessType : pBusinessTypes) {
            getMap().put(tBusinessType.name(), tBusinessType);
        }
    }
    
    public static CwfBusinessTypeIf get(String pName) {
        if (pName != null && pName.startsWith("Constant:")) {
            return Constant;
        }
        if (pName != null && pName.startsWith("Object:")) {
            return Object;
        }
        return getMap().get(pName);
    }
    
    
    
}
