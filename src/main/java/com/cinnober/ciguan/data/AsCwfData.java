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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.CwfFilterIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.util.StringEscape;


/**
 * This class holds data which can either be a complete model, or a part of a more
 * complex model. It is the basic client side building block for models.
 *
 * Note that the setProperty and getProperty methods can take attribute names
 * on the form "foo.bar.attribute". In this case, the methods will automatically
 * handle the potentially missing sublevels. In case of setProperty, the missing levels
 * are automatically created, while getProperty will return null if a level is missing.
 *
 */
@SuppressWarnings("serial")
class AsCwfData implements CwfDataIf, Serializable, MvcModelAttributesIf {

    private HashMap<String, String> mProperties = new HashMap<String, String>();
    private HashMap<String, CwfDataIf> mObjects;
    private HashMap<String, List<CwfDataIf>> mObjectLists;
    private List<CwfDataIf> mAllChildNodes;

    public AsCwfData() {
    }

    /**
     * Full recursive copy constructor
     * @param pSource
     */
    protected AsCwfData(CwfDataIf pSource) {
        // Properties
        Map<String, String> tProperties = pSource.getProperties();
        for (Entry<String, String> tEntry : tProperties.entrySet()) {
            setProperty(tEntry.getKey(), tEntry.getValue());
        }
        // Objects
        Map<String, CwfDataIf> tObjects = pSource.getObjects();
        if (tObjects != null) {
            for (Entry<String, CwfDataIf> tEntry : tObjects.entrySet()) {
                setObject(tEntry.getKey(), CwfDataFactory.copy(tEntry.getValue()));
            }
        }
        // Object lists
        Map<String, List<CwfDataIf>> tObjectLists = pSource.getObjectListMap();
        if (tObjectLists != null) {
            for (Entry<String, List<CwfDataIf>> tEntry : tObjectLists.entrySet()) {
                for (CwfDataIf tListData : tEntry.getValue()) {
                    addObject(tEntry.getKey(), CwfDataFactory.copy(tListData));
                }
            }
        }
    }

    @Override
    public final String getProperty(String pAttributeName) {
        int tPos = pAttributeName.indexOf(".");
        if (tPos == -1) {
            return mProperties.get(pAttributeName);
        }
        String tChildName = pAttributeName.substring(0, tPos);
        if (tChildName.matches("^.*\\[\\d+\\]$")) {
            int tRowNumber = Integer.valueOf(tChildName.substring(tChildName.lastIndexOf('[') + 1,
                tChildName.lastIndexOf(']')));
            tChildName = tChildName.substring(0, tChildName.length() - Integer.toString(tRowNumber).length() - 2);
            List<CwfDataIf> tChild = getObjectList(tChildName);
            if (tChild == null || tRowNumber >= tChild.size()) {
                return null;
            }
            return tChild.get(tRowNumber).getProperty(pAttributeName.substring(tPos + 1));
        }
        else {
            CwfDataIf tChild = getObject(tChildName);
            if (tChild == null) {
                return null;
            }
            return tChild.getProperty(pAttributeName.substring(tPos + 1));
        }
    }

    public final Integer getIntProperty(String pAttributeName) {
        String tValue = getProperty(pAttributeName);
        return tValue == null || tValue.length() == 0 ? null : Integer.valueOf(tValue);
    }

    @Override
    public Long getLongProperty(String pAttributeName) {
        String tValue = getProperty(pAttributeName);
        return tValue == null || tValue.length() == 0 ? null : Long.valueOf(tValue);
    }

    public final Boolean getBooleanProperty(String pAttributeName) {
        String tValue = getProperty(pAttributeName);
        return tValue == null || tValue.length() == 0 ? null : Boolean.valueOf(tValue);
    }

    @Override
    public final void setProperty(String pAttributeName, String pValue) {
        int tPos = pAttributeName.indexOf(".");
        if (tPos == -1) {
            if (pAttributeName.endsWith("[0]")) {
                setProperty(pAttributeName.substring(0, pAttributeName.length() - 3),
                    new String[] { pValue });
                return;
            }
            mProperties.put(pAttributeName, pValue);
            return;
        }
        String tChildName = pAttributeName.substring(0, tPos);
        CwfDataIf tChild;
        if (tChildName.matches("^.*\\[\\d+\\]$")) {
            int tRowNumber = Integer.valueOf(tChildName.substring(tChildName.lastIndexOf('[') + 1,
                tChildName.lastIndexOf(']')));
            tChildName = tChildName.substring(0, tChildName.length() - Integer.toString(tRowNumber).length() - 2);
            if (mObjectLists != null && mObjectLists.containsKey(tChildName)) {
                List<CwfDataIf> tList = mObjectLists.get(tChildName);
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
                addObject(tChildName, tChild);
            }
        }
        else {
            tChild = getObject(tChildName);
            if (tChild == null) {
                tChild = CwfDataFactory.create();
                setObject(tChildName, tChild);
            }
        }
        tChild.setProperty(pAttributeName.substring(tPos + 1), pValue);
    }

    public final void setProperty(String pAttributeName, Integer pValue) {
        set(pAttributeName, pValue);
    }

    public final void setProperty(String pAttributeName, Boolean pValue) {
        set(pAttributeName, pValue);
    }

    @Override
    public void setProperty(String pAttributeName, Long pValue) {
        set(pAttributeName, pValue);
    }

    private String set(String pAttributeName, Object pValue) {
        String tOld = getProperty(pAttributeName);
        if (pValue == null) {
            removeProperty(pAttributeName);
        }
        else {
            setProperty(pAttributeName, pValue.toString());
        }
        return tOld;
    }

    /**
     * Set a named string array value
     * @param pAttributeName
     * @param pValues
     * @return
     */
    public void setProperty(String pAttributeName, String[] pValues) {
        setProperty(pAttributeName, Arrays.toString(escape(pValues)));
    }

    public void setProperty(String pAttributeName, int[] pValues) {
        setProperty(pAttributeName, Arrays.toString(pValues));
    }

    /**
     * Set a named two dimensional string array value
     * @param pAttributeName
     * @param pValues
     * @return
     */
    public void setProperty(String pAttributeName, String[][] pValues) {
        StringBuilder tBuilder = new StringBuilder();
        for (String[] tArr : pValues) {
            tBuilder.append(Arrays.toString(escape(tArr)));
        }
        setProperty(pAttributeName, tBuilder.toString());
    }

    /**
     * Set a named string array map value
     * NOTE: Null arrays are serialized as "[\\null]"
     * @param pAttributeName
     * @param pValues
     * @return
     */
    public void setProperty(String pAttributeName, Map<String, String[]> pValues) {

        StringBuilder tBuilder = new StringBuilder();
        if (pValues != null) {
            // make a copy of the given map and it's values
            Map<String, String[]> tValues = new HashMap<String, String[]>();
            for (String tKey : pValues.keySet()) {
                String[] tArray = pValues.get(tKey);
                String[] tCopy = null;
                if (tArray != null) {
                    tCopy = new String[tArray.length];
                    for (int i = 0; tArray != null && i < tArray.length; i++) {
                        tCopy[i] = StringEscape.escape(tArray[i]);
                    }
                }
                tValues.put(StringEscape.escape(tKey), tCopy);
            }

            // serialize to string
            for (String tKey : tValues.keySet()) {
                tBuilder.append(tKey);
                tBuilder.append(tValues.get(tKey) == null ? "[\\null]" : Arrays.toString(tValues.get(tKey)));
            }
        }
        setProperty(pAttributeName, tBuilder.toString());
    }

    /**
     * Get a named value as a two-dimensional string array
     * @param pAttributeName
     * @return
     */
    public String[][] getStringDoubleArray(String pAttributeName) {
        String tValue = getProperty(pAttributeName);
        if (tValue == null || tValue.length() == 0) {
            return new String[0][];
        }
        String[] tItems = tValue.split("\\]\\[");
        String[][] tArr = new String[tItems.length][];
        for (int i = 0; i < tItems.length; i++) {
            if (i == 0) {
                tItems[i] = tItems[i].substring(1);
            }
            if (i == tItems.length - 1) {
                tItems[i] = tItems[i].substring(0, tItems[i].length() - 1);
            }
            tArr[i] = unescape(tItems[i].split(", ", -1));
            for (int j = 0; tArr[i] != null && j < tArr[i].length; j++) {
                if ("null".equals(tArr[i][j])) {
                    tArr[i][j] = null;
                }
            }
        }
        return tArr;
    }

    @Override
    public void setObject(String pObjectName, CwfDataIf pValue) {
        int tPos = pObjectName.indexOf(".");
        if (tPos == -1) {
            if (mObjects == null) {
                mObjects = new HashMap<String, CwfDataIf>();
            }
            mObjects.put(pObjectName, pValue);
            return;
        }
        String tChildName = pObjectName.substring(0, tPos);
        CwfDataIf tChild = getObject(tChildName);
        if (tChild == null) {
            tChild = CwfDataFactory.create();
            setObject(tChildName, tChild);
        }
        tChild.setObject(pObjectName.substring(tPos + 1), pValue);
    }

    public void addObject(String pAttrName, Collection<CwfDataIf> pData) {
        for (CwfDataIf tData : pData) {
            addObject(pAttrName, tData);
        }
    }

    public void setObject(String pAttrName, Collection<CwfDataIf> pData) {
        List<CwfDataIf> tList = getObjects(pAttrName);
        if (tList != null) {
            tList.clear();
        }
        addObject(pAttrName, pData);
    }

    @Override
    public void addObject(String pName, CwfDataIf pObject, int... pPosition) {
        int tPos = pName.indexOf(".");
        if (tPos == -1) {
            if (mObjectLists == null) {
                mObjectLists = new HashMap<String, List<CwfDataIf>>();
                mAllChildNodes = new ArrayList<CwfDataIf>();
            }
            List<CwfDataIf> tList = mObjectLists.get(pName);
            if (tList == null) {
                mObjectLists.put(pName, tList = new ArrayList<CwfDataIf>());
            }
            if (pPosition.length == 0) {
                tList.add(pObject);
                mAllChildNodes.add(pObject);
            }
            else {
                tList.add(pPosition[0], pObject);
            }
            return;
        }
        String tChildName = pName.substring(0, tPos);
        CwfDataIf tChild = getObject(tChildName);
        if (tChild == null) {
            tChild = CwfDataFactory.create();
            setObject(tChildName, tChild);
        }
        tChild.addObject(pName.substring(tPos + 1), pObject);
    }

    @Override
    public void removeObject(String pName, int pIndex) {
        int tPos = pName.indexOf(".");
        if (tPos == -1) {
            if (mObjectLists != null) {
                List<CwfDataIf> tList = mObjectLists.get(pName);
                if (tList != null) {
                    tList.remove(pIndex);
                }
            }
            return;
        }
        String tChildName = pName.substring(0, tPos);
        CwfDataIf tChild = getObject(tChildName);
        if (tChild == null) {
            tChild = CwfDataFactory.create();
            setObject(tChildName, tChild);
        }
        tChild.removeObject(pName.substring(tPos + 1), pIndex);
    }

    @Override
    public Map<String, String> getProperties() {
        return mProperties;
    }

    @Override
    public CwfDataIf getObject(String pObjectName) {
        int tPos = pObjectName.indexOf(".");
        if (tPos == -1) {
            return mObjects == null ? null : mObjects.get(pObjectName);
        }
        String tChildName = pObjectName.substring(0, tPos);
        CwfDataIf tChild = getObject(tChildName);
        if (tChild == null) {
            return null;
        }
        return tChild.getObject(pObjectName.substring(tPos + 1));
    }

    @Deprecated
    public List<CwfDataIf> getObjects(String pName) {
        int tPos = pName.indexOf(".");
        if (tPos == -1) {
            return mObjectLists == null ? null : mObjectLists.get(pName);
        }
        String tChildName = pName.substring(0, tPos);
        CwfDataIf tChild = getObject(tChildName);
        if (tChild == null) {
            return null;
        }
        return tChild.getObjects(pName.substring(tPos + 1));
    }

    @Override
    public List<CwfDataIf> getObjectList(String pName) {
        List<CwfDataIf> tList = getObjects(pName);
        if (tList == null) {
            tList = Collections.emptyList();
        }
        return tList;
    }

    @SuppressWarnings("unchecked")
    public List<CwfDataIf> getAllChildNodes() {
        return mAllChildNodes != null ? mAllChildNodes : Collections.EMPTY_LIST;
    }

    @Override
    public Map<String, CwfDataIf> getObjects() {
        return mObjects;
    }

    @Override
    public Map<String, List<CwfDataIf>> getObjectListMap() {
        return mObjectLists;
    }

    public String[] getStringArray(String pAttributeName) {
        String tKeys = getProperty(pAttributeName);
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

    public int[] getIntArray(String pAttributeName) {
        String tArr = getProperty(pAttributeName);
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

    /**
     * Get a named value as a map holding string arrays
     * @param pAttributeName
     * @return
     */
    public Map<String, String[]> getStringArrayMap(String pAttributeName) {
        HashMap<String, String[]> tMap = new HashMap<String, String[]>();
        String tValue = getProperty(pAttributeName);
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

    @Override
    public String toString() {
        return mProperties.toString();
    }


    public void copyProperties(CwfDataIf pFrom, String... pProperties) {
        for (String tProperty : pProperties) {
            setProperty(tProperty, pFrom.getProperty(tProperty));
        }
    }

    public String removeProperty(String pKey) {
        return mProperties.remove(pKey);
    }

    /**
     * Remove a named object
     * @param pKey
     * @return
     */
    public CwfDataIf removeObject(String pKey) {
        return mObjects == null ? null : mObjects.remove(pKey);
    }

    /**
     * Remove a named object list
     * @param pKey
     * @return
     */
    public List<CwfDataIf> removeObjectList(String pKey) {
        return mObjectLists == null ? null : mObjectLists.remove(pKey);
    }

    /**
     * Remove an object from the named object list
     * @param pTagField
     * @param pData
     */
    public void removeObject(String pTagField, CwfDataIf pData) {
        int tIndex = getObjectList(pTagField).indexOf(pData);
        if (tIndex >= 0) {
            removeObject(pTagField, tIndex);
        }
    }

    public void replaceObject(String pTagField, CwfDataIf pOldData, CwfDataIf pNewData) {
        List<CwfDataIf> tList = getObjectList(pTagField);
        int tIndex = tList.indexOf(pOldData);
        if (tIndex >= 0) {
            tList.remove(tIndex);
            tList.add(tIndex, pNewData);
        }
        else {
            tList.add(pNewData);
        }
    }

    public int compareProperty(CwfDataIf pThat, String pProperty) {
        String tThisProperty = getProperty(pProperty);
        String tThatProperty = pThat.getProperty(pProperty);
        if (tThisProperty == null && tThatProperty == null) {
            return 0;
        }
        if (tThisProperty != null && tThatProperty == null) {
            return 1;
        }
        if (tThisProperty == null && tThatProperty != null) {
            return -1;
        }
        return tThisProperty.compareTo(tThatProperty);
    }

    @Override
    public boolean test(CwfFilterIf pCondition) {
        if (pCondition == null) {
            return true;
        }
        String tCurrentValue = getProperty(pCondition.getName());
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

    public String[] escape(String[] pArray) {
        if (pArray == null) {
            return null;
        }
        String[] tArray = new String[pArray.length];
        for (int i = 0; i < pArray.length; i++) {
            tArray[i] = StringEscape.escape(pArray[i]);
        }
        return tArray;
    }

    public String[] unescape(String[] pArray) {
        if (pArray == null) {
            return null;
        }
        String[] tArray = new String[pArray.length];
        for (int i = 0; i < pArray.length; i++) {
            tArray[i] = StringEscape.unescape(pArray[i]);
        }
        return tArray;
    }

    @Override
    public String type() {
        return CwfDataTool.type(this);
    }

    @Override
    public Byte getByte(String pName) {
        String tValue = getProperty(pName);
        if (tValue == null || tValue.isEmpty()) {
            return null;
        }
        return Byte.valueOf(tValue);
    }

    @Override
    public Character getChar(String pName) {
        String tValue = getProperty(pName);
        if (tValue == null || tValue.length() != 1) {
            return null;
        }
        return tValue.charAt(0);
    }

    @Override
    public BigDecimal getBigDecimal(String pName) {
        String tValue = getProperty(pName);
        if (tValue == null || tValue.isEmpty()) {
            return null;
        }
        return new BigDecimal(tValue);
    }

    @Override
    public BigInteger getBigInteger(String pName) {
        String tValue = getProperty(pName);
        if (tValue == null || tValue.isEmpty()) {
            return null;
        }
        return new BigInteger(tValue);
    }

}
