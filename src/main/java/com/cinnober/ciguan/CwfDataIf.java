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
package com.cinnober.ciguan;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Base data object for client models and communication with server.
 */
public interface CwfDataIf {
    
    /**
     * Deprecated, use CwfDataTool.type instead
     */
    @Deprecated
    String type();
    
    /**
     * Get the named property
     * @param pPropertyName
     * @return
     */
    String getProperty(String pPropertyName);

    Boolean getBooleanProperty(String pPropertyName);
    
    Byte getByte(String pName);

    Character getChar(String pName);
    
    Integer getIntProperty(String pAttributeName);
    
    Long getLongProperty(String pAttributeName);
    
    BigDecimal getBigDecimal(String pName);
    
    BigInteger getBigInteger(String pName);

    String[] getStringArray(String pAttributeName);

    /**
     * Get a named value as a two-dimensional string array
     * @param pAttributeName
     * @return
     */
    String[][] getStringDoubleArray(String pAttributeName);

    /**
     * Get a named value as a map holding string arrays
     * @param pAttributeName
     * @return
     */
    Map<String, String[]> getStringArrayMap(String pAttributeName);

    int[] getIntArray(String pAttributeName);
    
    /**
     * Get a named object
     * @param pObjectName
     * @return
     */
    CwfDataIf getObject(String pObjectName);
    
    /**
     * Set a named property
     * @param pPropertyName
     * @param pValue
     * @return old value
     */
    void setProperty(String pPropertyName, String pValue);
    
    void setProperty(String pAttrStatusCode, Integer pValue);
    
    void setProperty(String pAttributeName, int[] pValues);

    void setProperty(String pAttributeName, Boolean pValue);
    
    void setProperty(String pAttributeName, Long pValue);
    
    /**
     * Set a named two dimensional string array value
     * @param pAttributeName
     * @param pValues
     * @return 
     */
    void setProperty(String pAttributeName, String[][] pValues);
    
    /**
     * Set a named string array value
     * @param pAttributeName
     * @param pValues
     * @return 
     */
    void setProperty(String pAttributeName, String[] pValues);

    /**
     * Set a named string array map value
     * NOTE: Null arrays are serialized as "[\\null]"
     * @param pAttributeName
     * @param pValues
     * @return 
     */
    void setProperty(String pAttributeName, Map<String, String[]> pValues);
    
    /**
     * Set a named object
     * @param pObjectName
     * @param pValue
     */
    void setObject(String pObjectName, CwfDataIf pValue);
    
    /**
     * Add an object to a named object list
     * @param pName
     * @param pObject
     * @param pPosition
     */
    void addObject(String pName, CwfDataIf pObject, int... pPosition);
    
    /**
     * Remove an object from a named object list
     * @param pName
     * @param pIndex
     */
    void removeObject(String pName, int pIndex);
    
    /**
     * Get all properties
     * @return
     */
    Map<String, String> getProperties();
    
    /**
     * Get all objects
     * @return
     */
    Map<String, CwfDataIf> getObjects();

    /**
     * Get all object lists
     * @return
     */
    Map<String, List<CwfDataIf>> getObjectListMap();

    /**
     * Get a list of named objects
     * @param pAttrItems
     * @return
     */
    List<CwfDataIf> getObjectList(String pAttrItems);

    /**
     * Test if a filter expression is true or not
     * @param pCondition
     * @return
     */
    boolean test(CwfFilterIf pCondition);
 
    
    @Deprecated
    List<CwfDataIf> getObjects(String pName);
    
    void setObject(String pAttrName, Collection<CwfDataIf> pData);

    void addObject(String pAttrName, Collection<CwfDataIf> pData);

    String removeProperty(String pProperty);

    CwfDataIf removeObject(String pName);

    /**
     * Remove an object from the named object list
     * @param pTagField
     * @param pData
     */
    void removeObject(String pListName, CwfDataIf pData);
    /**
     * Replace an object from the named object list
     * @param pTagField
     * @param pData
     */
    void replaceObject(String pTagField, CwfDataIf pOldData, CwfDataIf pNewData);
    
    List<CwfDataIf> removeObjectList(String pName);
    
    List<CwfDataIf> getAllChildNodes();

}
