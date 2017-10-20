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
package com.cinnober.ciguan.conf.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cinnober.ciguan.client.MvcModelAttributesIf;


/**
 *
 *
 * @author magnus.lenti, Cinnober Financial Technology
 * @version $Revision: 1.7 $
 */
public class AsConfigurationTestBase implements MvcModelAttributesIf {

    protected enum Result {
        error,
        warning,
        ignore
    }

    protected static Element cRoot;
    protected static CachedXPathAPI cCachedXPathAPI = new CachedXPathAPI();
    protected static List<String> cErrors = new ArrayList<String>();
    protected static List<String> cWarnings = new ArrayList<String>();
    protected static boolean cLoadedOk;

    protected static Result cReportDuplicateDefinition = Result.warning;

    protected static void error(String pMessage, Element pNode) {
        String tPath = getPath(pNode);
        cErrors.add(tPath + ": " + pMessage);
    }

    protected static void warning(String pMessage, Element pNode) {
        String tPath = getPath(pNode);
        cWarnings.add(tPath + ": " + pMessage);
    }

    static String getPath(Element pNode) {
        String tModule = pNode.getAttribute("module");
        String tId = pNode.getAttribute("id");
        if (tModule.isEmpty()) {
            String tText = pNode.getTagName();
            if (!tId.isEmpty()) {
                tText = tText + "[" + tId + "]";
            }
            return getPath((Element) pNode.getParentNode()) + "/" + tText;
        }
        return tModule.substring(tModule.lastIndexOf('.') + 1) + ":/";
    }

    protected static void setRoot(Element pRoot) {
        cRoot = pRoot;
    }

    static List<Element> createList(String pObject, String pAttr) {
        return createList("//" + pObject + "[@" + pAttr + "]");
    }
    protected static List<Element> createList(String... pXPath) {
        List<Element> tList = new ArrayList<Element>();
        for (String tXPath : pXPath) {
            tList.addAll(createList(tXPath, cRoot));
        }
        return tList;
    }

    protected static List<Element> createList(String pXPath, Node pNode) {
        List<Element> tList = new ArrayList<Element>();
        try {
            NodeList tNodes = cCachedXPathAPI.selectNodeList(pNode, pXPath);
            for (int i = 0; i < tNodes.getLength(); i++) {
                tList.add((Element) tNodes.item(i));
            }
        }
        catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        return tList;
    }
    protected static List<Attr> createAttrList(String pXPath) {
        return createAttrList(pXPath, cRoot);
    }

    protected static List<Attr> createAttrList(String pXPath, Node pNode) {
        List<Attr> tList = new ArrayList<Attr>();
        try {
            NodeList tNodes = cCachedXPathAPI.selectNodeList(pNode, pXPath);
            for (int i = 0; i < tNodes.getLength(); i++) {
                tList.add((Attr) tNodes.item(i));
            }
        }
        catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        return tList;
    }

    protected static Set<String> createSet(String pXPath) {
        return createSet(pXPath, true);
    }

    protected static Set<String> createSet(String pXPath, boolean pUniqueValue) {
        Set<String> tList = new HashSet<String>();
        for (Attr tAttr : createAttrList(pXPath)) {
            if (!tList.add(tAttr.getNodeValue()) && pUniqueValue) {
                switch (cReportDuplicateDefinition) {
                    case error:
                        error(pXPath + ", already defined " + tAttr.getNodeValue(), tAttr.getOwnerElement());
                        break;
                    case warning:
                        warning(pXPath + ", already defined " + tAttr.getNodeValue(), tAttr.getOwnerElement());
                        break;
                    case ignore:
                    default:;
                }
            }
        }
        return tList;
    }

    protected static Set<String> createSet(String pXPath, Node pNode) {
        return createSet(pXPath, pNode, true);
    }

    protected static Set<String> createSet(String pXPath, Node pNode, boolean pUniqueValue) {
        Set<String> tList = new HashSet<String>();
        for (Attr tAttr : createAttrList(pXPath, pNode)) {
            if (!tList.add(tAttr.getNodeValue()) && pUniqueValue) {
                switch (cReportDuplicateDefinition) {
                    case error:
                        error(pXPath + ", already defined " + tAttr.getNodeValue(), tAttr.getOwnerElement());
                        break;
                    case warning:
                        warning(pXPath + ", already defined " + tAttr.getNodeValue(), tAttr.getOwnerElement());
                        break;
                    case ignore:
                    default:;
                }
            }
        }
        return tList;
    }

    protected static Map<String, Element> createIdMap(String pXPath) {
        return createIdMap(pXPath, true);
    }
    protected static Map<String, Element> createIdMap(String pXPath, boolean pUniqueId) {
        return createIdMap(pXPath, pUniqueId, "id");
    }
    protected static Map<String, Element> createIdMap(String pXPath, boolean pUniqueId, String pId) {
        Map<String, Element> tMap = new HashMap<String, Element>();
        for (Element tElement : createList(pXPath)) {
            String tId = tElement.getAttribute(pId);
            if (pUniqueId && tMap.containsKey(tId)) {
                switch (cReportDuplicateDefinition) {
                    case error:
                        error(tElement.getTagName() + "[" + pId + "=" + tId + "], Duplicate definition", tElement);
                        break;
                    case warning:
                        warning(tElement.getTagName() + "[" + pId + "=" + tId + "], Duplicate definition", tElement);
                        break;
                    case ignore:
                    default:;
                }
            }
            tMap.put(tId, tElement);
        }
        return tMap;
    }

    protected static Map<String, String> createSearchPackageMap(String pXPath) {
        Map<String, String> tMap = new HashMap<>();
        for (Element tElement : createList(pXPath)) {
            String tPackage = tElement.getAttribute("packageName");
            String tNamespace = tElement.getAttribute("namespace");
            String tOld = tMap.put(tPackage, tNamespace);
            if (tOld != null && !tOld.equals(tNamespace)) {
                throw new RuntimeException("Package already defined with different namespace: " + tPackage);
            }
        }
        return tMap;
    }

    protected static List<Element> getViewInheritanceHierarchy(Element pView) {
        try {
            List<Element> tViews = new ArrayList<Element>();
            Element tView = pView;
            tViews.add(tView);
            String tExtends = tView.getAttribute(ATTR_EXTENDS);
            while (!tExtends.isEmpty()) {
                String tXPath = "/Configuration/AsMvc/view[@id='" + tExtends + "']";
                Element tBaseView = ((Element) cCachedXPathAPI.selectSingleNode(cRoot, tXPath));
                if (tBaseView == null) {
                    error("Base view " + tExtends + " does not exist", tView);
                    return tViews;
                }
                tViews.add(tBaseView);
                tView = tBaseView;
                tExtends = tView.getAttribute(ATTR_EXTENDS);
            }
            return tViews;
        }
        catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

}
