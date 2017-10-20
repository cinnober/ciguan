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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Enum describing all possible filter operators, plus some logic attached to it
 */
public enum RpcFilterOperator {
    
    Contains("|="),
    Equals("="),
    GreaterThan(">"),
    GreaterThanOrEqual(">="),
    IsNull("=null"),
    IsNotNull("!=null"),
    LessThan("<"),
    LessThanOrEqual("<="),
    NotEquals("!="),
    StartsWith("~=");
    
    private static Map<String, RpcFilterOperator> cOperators = new HashMap<String, RpcFilterOperator>();
    private static String[] cPatterns;
    
    static {
        init();
    }
    
    private final String mExpression;
    
    /**
     * Default instance
     * @param pExpression
     */
    private RpcFilterOperator(String pExpression) {
        mExpression = pExpression;
    }
    
    /**
     * Get the expression in string format representation
     * @return the expression in string format representation
     */
    public String getExpression() {
        return mExpression;
    }
    
    /**
     * Lazy initialization of static components if necessary
     */
    private static void init() {
        for (RpcFilterOperator tOperator : RpcFilterOperator.values()) {
            cOperators.put(tOperator.getExpression(), tOperator);
        }
        // Regex pattern - sort on operator expression length descending to avoid splitting
        // into parts of a longer operator expression
        RpcFilterOperator[] tOperators = RpcFilterOperator.values();
        Arrays.sort(tOperators, new Comparator<RpcFilterOperator>() {
            @Override
            public int compare(RpcFilterOperator pThis, RpcFilterOperator pThat) {
                return -(pThis.getExpression().length() - pThat.getExpression().length());
            }
        });
        cPatterns = new String[tOperators.length];
        for (int i = 0; i < tOperators.length; i++) {
            cPatterns[i] = tOperators[i].getExpression();
        }
    }
    
    /**
     * Get the operator that corresponds to the given expression
     * @param pExpression
     * @return the operator that corresponds to the given expression
     */
    public static RpcFilterOperator get(String pExpression) {
        return cOperators.get(pExpression);
    }
    
    /**
     * Evaluate two strings
     * @param pLeft
     * @param pRight
     * @return the evaluated operator expression for the two strings
     */
    public boolean eval(String pLeft, String pRight) {
        switch (this) {
            case Contains:
                String tLeft = pLeft != null ? pLeft : "";
                String tRight = pRight != null ? pRight : "";
                Set<String> tParts = new HashSet<String>(Arrays.asList(tRight.split(",")));
                return tParts.contains(tLeft);
            case Equals:
                return
                    (pLeft == null && pRight == null) ||
                    (pLeft != null && pLeft.equals(pRight));
            case NotEquals:
                return
                    (pLeft == null && pRight != null) ||
                    (pLeft != null && pRight == null) ||
                    (!pLeft.equals(pRight));
            case IsNotNull:
                assert pRight == null;
                return pLeft != null;
            case IsNull:
                assert pRight == null;
                return pLeft == null;
            case StartsWith:
                return pLeft != null && pLeft.startsWith(pRight);
            default:
                throw new RuntimeException(this + ": Not implemented");
        }
    }
    
    /**
     * Split an expression into three parts
     * @param pExpression
     * @return an array of attribute, operator and value, or null if an operator cannot be found
     * in the expression
     */
    public static String[] split(String pExpression) {
        for (String tEx : cPatterns) {
            int tSt = pExpression.indexOf(tEx);
            if (tSt >= 0) {
                String[] tParts = new String[3];
                tParts[0] = pExpression.substring(0, tSt);
                tParts[1] = tEx;
                tParts[2] = pExpression.substring(tParts[0].length() + tEx.length());
                return tParts;
            }
        }
        return null;
    }
    
}
