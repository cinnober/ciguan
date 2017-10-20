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

import com.cinnober.ciguan.CwfDataFactoryIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.CwfModelNameIf;

/**
 * 
 * Quick access to CwfDataFactory tools
 * 
 */

public abstract class CwfDataFactory {

    private static CwfDataFactoryIf cInstance;
    
    public static CwfDataIf create() {
        return cInstance.create();
    }
    
    public static CwfDataIf create(CwfModelNameIf pModelName) {
        return cInstance.create(pModelName);
    }
    
    public static CwfDataIf copy(CwfDataIf pData) {
        return cInstance.copy(pData);
    }

    public static CwfDataIf findObject(CwfDataIf pData, String pTagName, 
        String pAttributeName, String pAttributeValue) {
        
        return cInstance.findObject(pData, pTagName, pAttributeName, pAttributeValue);
    }

    public static CwfDataIf fromParameters(String pParameters) {
        return cInstance.fromParameters(pParameters);
    }

    public static void set(CwfDataFactoryIf pCwfDataFactory) {
        cInstance = pCwfDataFactory;
    }

}
