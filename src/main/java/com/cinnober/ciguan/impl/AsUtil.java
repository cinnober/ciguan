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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.impl.MvcModelNames;
import com.cinnober.ciguan.data.CwfDataFactory;

/**
 * Application server utility methods. Should never be instantiated.
 */
public abstract class AsUtil implements MvcModelAttributesIf {

    public static final String ARRAY_SUFFIX = "[]";

    public static CwfDataIf parse(Element pElement) {
        CwfDataIf tData = CwfDataFactory.create();
        tData.setProperty(ATTR_TAG_NAME, pElement.getNodeName());
        NamedNodeMap tAttributes = pElement.getAttributes();
        for (int i = 0; i < tAttributes.getLength(); i++) {
            Node tNode = tAttributes.item(i);
            String tValue = tNode.getNodeValue();
            if (tValue != null && tValue.equals("null")) {
                tValue = null;
            }
            tData.setProperty(tNode.getNodeName(), tValue);
        }
        // Check if the first child is a text or CDATA with content, then save it as
        // a text attribute
        Node tChild = pElement.getFirstChild();
        if (tChild != null) {
            if (tChild.getNodeType() == Node.TEXT_NODE) {
                String tTextContent = tChild.getTextContent().trim();
                if (!tTextContent.isEmpty()) {
                    tData.setProperty(ATTR_XML_TEXT, tTextContent);
                }
            }
            else if (tChild.getNodeType() == Node.CDATA_SECTION_NODE) {
                CDATASection tCdata = (CDATASection) tChild;
                String tTextContent = tCdata.getTextContent().trim();
                if (!tTextContent.isEmpty()) {
                    tData.setProperty(ATTR_XML_TEXT, tTextContent);
                }
            }
        }
        NodeList tNodes = pElement.getChildNodes();
        for (int i = 0; i < tNodes.getLength(); i++) {
            Node tNode = tNodes.item(i);
            if (tNode.getNodeType() == Node.ELEMENT_NODE) {
                Element tElement = (Element) tNode;
                tData.addObject(tElement.getNodeName(), parse(tElement));
            }
        }
        return tData;
    }

    /**
     * Method removed when ref removed from configuration
     */
    @Deprecated
    public static CwfDataIf parseAndReplaceRef(Element pElement, Map<String, CwfDataIf> pIdMap) {
        String tRef = pElement.getAttribute("ref");
        if (tRef != null && tRef.length() > 0) {
            CwfDataIf tId = pIdMap.get(tRef);
            if (tId == null) {
                return null;
            }
            if (pElement.hasChildNodes()) {
                for (int i = 0; i < pElement.getChildNodes().getLength(); i++) {
                    Node tNode = pElement.getChildNodes().item(i);
                    if (tNode instanceof Element) {
                        CwfDataIf tItem = parseAndReplaceRef((Element) pElement.getChildNodes().item(i), pIdMap);
                        tId.addObject(tItem.getProperty(ATTR_TAG_NAME), tItem);
                    }
                }
                return null;
            }
            else {
                CwfDataIf tData = CwfDataFactory.copy(pIdMap.get(tRef));
                NamedNodeMap tAttributes = pElement.getAttributes();
                for (int i = 0; i < tAttributes.getLength(); i++) {
                    Node tNode = tAttributes.item(i);
                    tData.setProperty(tNode.getNodeName(), tNode.getNodeValue());
                }
                return tData;
            }
        }
        CwfDataIf tData = CwfDataFactory.create();
        tData.setProperty(ATTR_TAG_NAME, pElement.getNodeName());
        NamedNodeMap tAttributes = pElement.getAttributes();
        for (int i = 0; i < tAttributes.getLength(); i++) {
            Node tNode = tAttributes.item(i);
            tData.setProperty(tNode.getNodeName(), tNode.getNodeValue());
        }
        NodeList tNodes = pElement.getChildNodes();
        for (int i = 0; i < tNodes.getLength(); i++) {
            Node tNode = tNodes.item(i);
            if (tNode.getNodeType() == Node.ELEMENT_NODE) {
                Element tElement = (Element) tNode;
                CwfDataIf tMenu = parseAndReplaceRef(tElement, pIdMap);
                if (tMenu != null) {
                    tData.addObject(tElement.getNodeName(), tMenu);
                }
            }
        }
        return tData;
    }

    /**
     * Method removed when ref removed from configuration, only used for menu... separator, languages
     */
    @Deprecated
    public static void makeIdMap(Node pElement, Map<String, CwfDataIf> pIdMap) {
        if (pElement.getNodeType() == Node.ELEMENT_NODE) {
            String tTagName = ((Element) pElement).getTagName();
            String tId = ((Element) pElement).getAttribute("id");
            if (!tId.isEmpty() && tTagName.equals(TAG_MENUITEM)) {
                CwfDataIf tData = CwfDataFactory.create();
                tData.setProperty(ATTR_TAG_NAME, TAG_MENUITEM);
                for (int i = 0; i < pElement.getAttributes().getLength(); i++) {
                    Attr tAttr = (Attr) pElement.getAttributes().item(i);
                    tData.setProperty(tAttr.getName(), tAttr.getValue());
                }
                pIdMap.put(tId, tData);

            }
            NodeList tNodes = pElement.getChildNodes();
            for (int i = 0; i < tNodes.getLength(); i++) {
                makeIdMap(tNodes.item(i), pIdMap);
            }
        }
    }

    /**
     * Create the possibly missing bridging objects and return the destination field
     * @param pObj
     * @param pPath
     * @return
     */
    public static Field getFieldByPath(Object pObj, String pPath) {
        int tPos = pPath.indexOf('.');
        if (tPos < 0) {
            return getField(pObj, pPath);
        }

        String tFieldName = pPath.substring(0, tPos);
        String tRemainingPath = pPath.substring(tPos + 1);
        Field tField = getField(pObj, tFieldName);
        Object tObj = getOrCreateFromField(tField, pObj);

        return getFieldByPath(tObj, tRemainingPath);
    }

    /**
     * Create the possibly missing bridging objects and return the destination object
     * @param pObj
     * @param pPath
     * @return
     */
    public static Object getObjectByPath(Object pObj, String pPath) {
        int tPos = pPath.indexOf('.');
        if (tPos < 0) {
            return pObj;
        }

        String tFieldName = pPath.substring(0, tPos);
        String tRemainingPath = pPath.substring(tPos + 1);
        Field tField = getField(pObj, tFieldName);
        Object tObj = getOrCreateFromField(tField, pObj);

        return getObjectByPath(tObj, tRemainingPath);
    }

    /**
     * Wrapper to get a field
     * @param pObject
     * @param pFieldName
     * @return
     */
    public static Field getField(Object pObject, String pFieldName) {
        Field tField = null;
        try {
            tField = pObject.getClass().getField(pFieldName);
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return tField;
    }

    /**
     * Try to retrieve the object indicated by the field and create it if it is null
     * @param pField
     * @param pObj
     * @return
     */
    public static Object getOrCreateFromField(Field pField, Object pObj) {
        try {
            Object tObj = pField.get(pObj);
            if (tObj != null) {
                return tObj;
            }
            Class<?> tClass = pField.getType();
            tObj = tClass.newInstance();
            pField.set(pObj, tObj);
            return tObj;
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert an object array to a client array suitable for use as a context.
     * Note that the array must contain at least one non-null entry in order to determine the component type.
     *
     * @param pConnection the application server connection
     * @param pArray the source array
     * @return a client format array that can be used as a context
     * @throws IllegalArgumentException if the input array is empty or contains only null entries
     */
    @SuppressWarnings("deprecation")
    public static CwfDataIf toArrayContext(AsConnectionIf pConnection, Object pArray) {
        CwfDataIf tArray = CwfDataFactory.create();
        int tLength = Array.getLength(pArray);
        if (tLength > 0) {
            for (int i = 0; i < tLength; i++) {
                Object tItem = Array.get(pArray, i);
                if (tItem != null) {
                    if (tArray.type() == null) {
                        String tTypeName = As.getTypeName(tItem.getClass());
                        tArray.setProperty(ATTR_MODEL_NAME, MvcModelNames.ServerResponse.name());
                        tArray.setProperty(ATTR_OBJECT_NAME, tTypeName + ARRAY_SUFFIX);
                    }
                    tArray.addObject(ATTR_ARRAY_ITEMS,
                        pConnection.getRequestService().transform(pConnection, tItem));
                }
                else {
                    // null element, add an empty CwfDataIf
                    tArray.addObject(ATTR_ARRAY_ITEMS, CwfDataFactory.create());
                }
            }
        }
        if (tArray.type() == null) {
            throw new IllegalArgumentException("Input array is either empty or contains only null entries");
        }
        return tArray;
    }

}
