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

import java.util.ArrayList;
import java.util.List;

import com.cinnober.ciguan.CwfDataFactoryIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.CwfModelNameIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.impl.MvcModelNames;


/**
 *
 * Server side implementation of CwfDataFactoryIf
 *
 */
public class AsCwfDataFactoryImpl implements CwfDataFactoryIf {

    @Override
    public CwfDataIf create() {
        return new AsCwfData();
    }


    @Override
    public CwfDataIf create(CwfModelNameIf pModelName) {
        CwfDataIf tData = create();
        tData.setProperty(MvcModelAttributesIf.ATTR_MODEL_NAME, pModelName.name());
        return tData;
    }

    @Override
    public CwfDataIf copy(CwfDataIf pData) {
        return pData == null ? null : new AsCwfData(pData);
    }

    /**
     * Find an object in an object that matches the given tag name and where the named attribute
     * matches the given value. The object itself is also subject to matching.
     * @param pData
     * @param pTagName
     * @param pAttributeName
     * @param pAttributeValue
     * @return
     */
    @Override
    public CwfDataIf findObject(
        CwfDataIf pData, String pTagName, String pAttributeName, String pAttributeValue) {
        if (pData == null) {
            return null;
        }
        if (pData.getProperty(MvcModelAttributesIf.ATTR_TAG_NAME) != null &&
            pData.getProperty(MvcModelAttributesIf.ATTR_TAG_NAME).equals(pTagName) &&
            pData.getProperty(pAttributeName) != null &&
            pData.getProperty(pAttributeName).equals(pAttributeValue)) {
            return pData;
        }
        CwfDataIf tData = findObject(pData.getObject(pTagName), pTagName, pAttributeName, pAttributeValue);
        if (tData != null) {
            return tData;
        }
        for (CwfDataIf tObject : pData.getObjectList(pTagName)) {
            tData = findObject(tObject, pTagName, pAttributeName, pAttributeValue);
            if (tData != null) {
                return tData;
            }
        }
        // Add all objects and object lists to the search list
        List<CwfDataIf> tSearchList = new ArrayList<CwfDataIf>();
        if (pData.getObjects() != null) {
            tSearchList.addAll(pData.getObjects().values());
        }
        if (pData.getObjectListMap() != null) {
            for (List<CwfDataIf> tList :pData.getObjectListMap().values()) {
                tSearchList.addAll(tList);
            }
        }
        // Now do the final search
        for (CwfDataIf tChild : tSearchList) {
            tData = findObject(tChild, pTagName, pAttributeName, pAttributeValue);
            if (tData != null) {
                return tData;
            }
        }
        return null;
    }

    /**
     * Construct a CwfDataIf from a comma separated list of attributes and values
     * @param pParameters
     * @return
     */
    @Override
    public CwfDataIf fromParameters(String pParameters) {
        CwfDataIf tData = create();
        tData.setProperty(MvcModelAttributesIf.ATTR_OBJECT_NAME, MvcModelNames.MenuParameters.name());
        if (pParameters != null) {
            for (String tParam : pParameters.split(",", -1)) {
                String tName = tParam.substring(0, tParam.indexOf("="));
                String tValue = tParam.substring(tParam.indexOf("=") + 1);
                tData.setProperty(tName, tValue);
            }
        }
        return tData;
    }


}
