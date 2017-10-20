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
package com.cinnober.ciguan.datasource.filter;

import com.cinnober.ciguan.datasource.RpcFilterCriteriaIf;

/**
 * Attribute version of a filter passed from the client to the server
 */
@SuppressWarnings("serial")
public class RpcAttributeFilterCriteria implements RpcFilterCriteriaIf {

    private final String mAttributeName;
    private final String mValue;
    private final RpcFilterOperator mOperator;
    
    public <T> RpcAttributeFilterCriteria(String pAttributeName, String pValue, String pOperatorExpression) {
        mAttributeName = pAttributeName;
        mValue = pValue;
        mOperator = RpcFilterOperator.get(pOperatorExpression);
        assert mOperator != null;
    }
    
    @Override
    public String getAttributeName() {
        return mAttributeName;
    }

    @Override
    public String getValue() {
        return mValue;
    }
    
    @Override
    public RpcFilterOperator getOperator() {
        return mOperator;
    }
    
    @Override
    public String toString() {
        return mAttributeName + mOperator.getExpression() + mValue;
    }

    /**
     * Factory method from a full expression of type "attribute(operator)value"
     * @param pString
     * @return
     */
    public static RpcFilterCriteriaIf fromString(String pString) {
        if (pString == null || pString.length() == 0 || pString.equals("null")) {
            return null;
        }
        String[] tParts = RpcFilterOperator.split(pString);
        if (tParts != null) {
            return new RpcAttributeFilterCriteria(tParts[0], tParts[2], tParts[1]);
        }
        throw new RuntimeException("Invalid filter criteria '" + pString + "'");
    }
    
}
