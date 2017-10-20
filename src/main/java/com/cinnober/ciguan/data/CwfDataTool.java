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
import java.util.List;
import java.util.Map;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.CwfFilterIf;
import com.cinnober.ciguan.CwfModelNameIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.util.StringEscape;

/**
 * 
 */

public class CwfDataTool implements MvcModelAttributesIf {

    public static String type(CwfDataIf pData) {
        // Try object name first, then model name
        String tName = pData.getProperty(ATTR_OBJECT_NAME);
        if (tName == null) {
            tName = pData.getProperty(ATTR_MODEL_NAME);
        }
        return tName;
    }
    
    public static boolean isType(CwfDataIf pData, CwfModelNameIf pType) {
        return isType(pData, pType == null ? null : pType.name());
    }

    public static boolean isType(CwfDataIf pData, String pType) {
        return pData != null && pType != null && pType.equals(type(pData));
    }
    
    public static String getObjectText(CwfDataIf pData, String pName) {
        CwfDataIf tData = pData.getObject(pName);
        if (tData == null) {
            List<CwfDataIf> tObjects = pData.getObjectList(pName);
            if (tObjects != null && !tObjects.isEmpty()) {
                tData = tObjects.get(0);
            }
        }
        return tData == null ? null : tData.getProperty(ATTR_XML_TEXT);
    }
    
    public static String[] getStringArray(CwfDataIf pData, String pAttributeName) {
        String tKeys = pData.getProperty(pAttributeName);
        String[] tRetValue = null;
        if (tKeys != null && !tKeys.equals("null")) {
            if (tKeys.equals("[]")) {
                tRetValue = new String[0];
            }
            else if (tKeys.startsWith("[")) {
                tRetValue = tKeys.substring(1, tKeys.length() - 1).split(", ", -1);
            }
            else {
                tRetValue = tKeys.split(",", -1);
            }
            for (int i = 0; i < tRetValue.length; i++) {
                if ("null".equals(tRetValue[i])) {
                    tRetValue[i] = null;
                }
                tRetValue[i] = StringEscape.unescape(tRetValue[i]);
            }
        }
        return tRetValue; 
    }

    public static int[] getIntArray(CwfDataIf pData, String pAttributeName) {
        String tArr = pData.getProperty(pAttributeName);
        String[] tRetValue = null;
        if (tArr == null) {
            return null;
        }
        if (tArr.length() == 0 || tArr.equals("[]")) {
            return new int[0];
        }
        else if (tArr.startsWith("[")) {
            tRetValue = tArr.substring(1, tArr.length() - 1).split(", ", -1);
        }
        else {
            tRetValue = tArr.split(",", -1);
        }
        int[] tArray = new int[tRetValue.length];
        for (int i = 0; i < tArray.length; i++) {
            tArray[i] = Integer.valueOf(tRetValue[i].trim());
        }
        return tArray;
    }

    public static Map<String, String[]> getStringArrayMap(CwfDataIf pData, String pAttributeName) {
        HashMap<String, String[]> tMap = new HashMap<String, String[]>();
        String tValue = pData.getProperty(pAttributeName);
        if (tValue != null && tValue.length() > 0) {
            String[] tRows = tValue.split("\\]");
            for (String tRow : tRows) {
                int tPos = tRow.indexOf("[");
                String tKey = tRow.substring(0, tPos);
                String tValues = tRow.substring(tPos + 1);
                
                String[] tArray = null;
                if (!tValues.equals("\\null")) {
                    tArray = tValues.split(", ", -1);
                    for (int i = 0; i < tArray.length; i++) {
                        tArray[i] = StringEscape.unescape(tArray[i]).trim();
                    }
                }
                tMap.put(StringEscape.unescape(tKey), tArray);
            }
        }
        return tMap;
    }

    public static String getPropertyByPath(CwfDataIf pData, String pPath) {
        int tPos = pPath.indexOf(".");
        String tChildName = pPath.substring(0, tPos);
        if (tChildName.matches("^.*\\[\\d+\\]$")) {
            int tRowNumber = Integer.valueOf(tChildName.substring(tChildName.lastIndexOf('[') + 1, 
                tChildName.lastIndexOf(']')));
            tChildName = tChildName.substring(0, tChildName.length() - Integer.toString(tRowNumber).length() - 2);
            List<CwfDataIf> tChild = pData.getObjectList(tChildName);
            if (tChild == null || tRowNumber >= tChild.size()) {
                return null;
            }
            return tChild.get(tRowNumber).getProperty(pPath.substring(tPos + 1));
        }

        CwfDataIf tChild = pData.getObject(tChildName);
        if (tChild == null) {
            return null;
        }
        return tChild.getProperty(pPath.substring(tPos + 1));
    }

    public static void setPropertyByPath(CwfDataIf pData, String pAttributeName, String pValue) {
        int tPos = pAttributeName.indexOf(".");
        String tChildName = pAttributeName.substring(0, tPos);
        CwfDataIf tChild;
        if (tChildName.matches("^.*\\[\\d+\\]$")) {
            int tRowNumber = Integer.valueOf(tChildName.substring(tChildName.lastIndexOf('[') + 1, 
                tChildName.lastIndexOf(']')));
            tChildName = tChildName.substring(0, tChildName.length() - Integer.toString(tRowNumber).length() - 2);
            if (pData.getObjectListMap() != null && pData.getObjectListMap().containsKey(tChildName)) {
                List<CwfDataIf> tList = pData.getObjectListMap().get(tChildName);
                if (tRowNumber > tList.size()) {
                    throw new ArrayIndexOutOfBoundsException(tRowNumber);
                } 
                if (tRowNumber == tList.size()) {
                    tList.add(CwfDataFactory.create());
                } 
                tChild = tList.get(tRowNumber);
            }
            else {
                if (tRowNumber != 0) {
                    throw new ArrayIndexOutOfBoundsException(tRowNumber);
                }
                tChild = CwfDataFactory.create();
                pData.addObject(tChildName, tChild);
            }
        }
        else {
            tChild = pData.getObject(tChildName);
            if (tChild == null) {
                tChild = CwfDataFactory.create();
                pData.setObject(tChildName, tChild);
            }
        }
        tChild.setProperty(pAttributeName.substring(tPos + 1), pValue);
    }

    public static boolean test(CwfDataIf pData, CwfFilterIf pCondition) {
        if (pCondition == null) {
            return true;
        }
        String tCurrentValue = pData.getProperty(pCondition.getName());
        String tCompareValue = pCondition.getValue();
        switch (pCondition.getOperator()) {
            case Equals:
                return tCompareValue.equals(tCurrentValue);
            case GreaterThan:
                return tCompareValue.compareTo(tCurrentValue) < 0;
            case GreaterThanOrEqual:
                return tCompareValue.compareTo(tCurrentValue) <= 0;
            case IsNull:
                return tCurrentValue == null;
            case IsNotNull:
                return tCurrentValue != null;
            case LessThan:
                return tCompareValue.compareTo(tCurrentValue) > 0;
            case LessThanOrEqual:
                return tCompareValue.compareTo(tCurrentValue) >= 0;
            case NotEquals:
                return !tCompareValue.equals(tCurrentValue);
            case StartsWith:
                return tCurrentValue != null ? tCurrentValue.startsWith(tCompareValue) : false;
            case Contains:
                return tCurrentValue != null && tCompareValue != null && tCurrentValue.contains(tCompareValue);
            default:
                throw new IllegalArgumentException("Operator " +
                    pCondition.getOperator().getExpression() + " not supported");
        }
    
    }

}
