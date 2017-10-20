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

/**
 *
 * Application server "TAP status mimick" class
 * 
 */
public class AsStatus {

    public String message;
    public int code;
    public int[] subCode;
    public String[] subMessage;
    
    public AsStatus(int pCode, String pMessage) {
        this(pCode, pMessage, null);
    }
    
    public AsStatus(int pCode, String pMessage, int[] pSubcodes) {
        code = pCode;
        message = pMessage;
        if (pSubcodes != null) {
            subMessage = new String[pSubcodes.length];
        }
    }
    
    public AsStatus(int pCode, String pMessage, int[] pSubcodes, String[] pSubmessages) {
        this(pCode, pMessage, pSubcodes);
        checkArrays(pSubcodes, pSubmessages);
        subMessage = pSubmessages;
    }

    public void checkArrays(int[] pSubcodes, String[] pSubmessages) {
        if ((pSubcodes == null && pSubmessages != null) ||
            (pSubcodes != null && pSubmessages == null) ||
            (pSubcodes.length != pSubmessages.length)) {
            throw new IllegalArgumentException("Sub codes and sub messages must have the same size");
        }
    }
    
}
