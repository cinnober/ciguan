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
package com.cinnober.ciguan.xml.impl;

/**
 * Class storing attribute value copy information
 * <pre>
 * Example:
 * 
 * {@code
 *  <CopyAttribute className="com.cinnober.rtc.generated.te.tap.tc.TcTradeReportInsertReq"
 *     fromAttributeName="matchedTrade.buyTrade.tradingMemberId"
 *     toAttributeName="matchedTrade.sellTrade.counterpartyTradingMemberId"/>
 *  }
 * </pre>
 */
public class AsDefCopyAttribute extends AsDef {

    /** The class name. */
    private String mClassName;
    
    /** The from attribute name. */
    private String mFromAttributeName;
    
    /** The to attribute name. */
    private String mToAttributeName;

    /**
     * Sets the class name.
     *
     * @param pClassName the new class name
     */
    public void setClassName(String pClassName) {
        ensureClassName(pClassName);
        mClassName = pClassName;
    }

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return mClassName;
    }
    
    /**
     * Sets the from attribute name.
     *
     * @param pFromAttributeName the new from attribute name
     */
    public void setFromAttributeName(String pFromAttributeName) {
        mFromAttributeName = pFromAttributeName;
    }

    /**
     * Gets the from attribute name.
     *
     * @return the from attribute name
     */
    public String getFromAttributeName() {
        return mFromAttributeName;
    }
    
    /**
     * Sets the to attribute name.
     *
     * @param pToAttributeName the new to attribute name
     */
    public void setToAttributeName(String pToAttributeName) {
        mToAttributeName = pToAttributeName;
    }

    /**
     * Gets the to attribute name.
     *
     * @return the to attribute name
     */
    public String getToAttributeName() {
        return mToAttributeName;
    }
    
}