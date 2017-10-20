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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.cinnober.ciguan.AsConfigXmlParserIf;
import com.cinnober.ciguan.AsInitializationException;

/**
 *  Helper class for loading as XML configuration.
 */
public class AsXmlTool {

    /**
     * Load XML resource.
     *
     * @param pResource the resource
     * @return the document
     * @throws Exception in case a problem occurred
     */
    public static Document loadXmlResource(String pResource) throws Exception {

        String tResource = (pResource.startsWith("/") ? pResource.substring(1) : pResource);
        InputStream tStream = null;

        /**
         * Try own class loader first, then the system class loader
         */
        tStream = AsXmlTool.class.getClassLoader().getResourceAsStream(tResource);
        if (tStream == null) {
            tStream = ClassLoader.getSystemResourceAsStream(tResource);
        }
        if (tStream == null) {
            tStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(tResource);
        }        
        if (tStream == null) {
            throw new RuntimeException("Failed to locate module '" + pResource + "'");
        }
        
        DocumentBuilderFactory tFactory = DocumentBuilderFactory.newInstance();
        tFactory.setIgnoringComments(true);
        tFactory.setNamespaceAware(false); // No namespaces: this is default
        tFactory.setValidating(false); // Don't validate DTD: also default
        
        DocumentBuilder tParser = tFactory.newDocumentBuilder();
        
        return tParser.parse(tStream);
    }
    
    /**
     * Load cwf module document.
     *
     * @param pModule the module
     * @return the document
     * @throws Exception the exception
     */
    public static Document loadCwfModuleDocument(String pModule) throws Exception {
        return loadXmlResource(pModule.replaceAll("\\.", "/") + ".cwf.xml");
    }
    
    /**
     * Load cwf module.
     *
     * @param pModule the module
     * @return the document
     * @throws Exception the exception
     */
    public static Document loadCwfModule(String pModule) throws Exception {
        return new InheritanceMap(pModule).assembleModule();
    }
    
    /**
     * Gets the inherited modules.
     *
     * @param pDocument the document
     * @return the inherited modules
     * @throws TransformerException the transformer exception
     */
    public static List<String> getInheritedModules(Document pDocument) throws TransformerException {
        List<String> tModules = new ArrayList<String>();
        NodeList tElem = XPathAPI.selectNodeList(pDocument.getDocumentElement(), "/Configuration/inherits");
        for (int i = 0; i < tElem.getLength(); i++) {
            tModules.add(((Element) tElem.item(i)).getAttribute("name"));
        }
        return tModules;
    }
    
    
    /**
     * The Class InheritanceMap.
     *
     * @author magnus.lenti, Cinnober Financial Technology
     */
    public static class InheritanceMap {
        
        /** The documents. */
        Map<String, Document> mDocuments = new HashMap<String, Document>();
        
        /** The inheritance. */
        Map<String, List<String>> mInheritance = new LinkedHashMap<String, List<String>>();
        
        /** The module. */
        String mModule;

        /**
         * Instantiates a new inheritance map.
         *
         * @param pModule the module
         * @throws Exception in case a problem occurred
         */
        public InheritanceMap(String pModule) throws Exception {
            mModule = pModule;
            loadRecursive(pModule);
        }

        /**
         * Load recursive.
         *
         * @param pModule the module
         * @throws Exception in case a problem occurred
         */
        private void loadRecursive(String pModule) throws Exception {
            if (!mDocuments.containsKey(pModule)) {
                mDocuments.put(pModule, null);
                mInheritance.put(pModule, new ArrayList<String>());
                Document tDoc = loadCwfModuleDocument(pModule);
                mDocuments.put(pModule, tDoc);
                checkVersion(tDoc, pModule);
                for (String tInherits : getInheritedModules(tDoc)) {
                    mInheritance.get(pModule).add(tInherits);
                    loadRecursive(tInherits);
                }
            }
        }
        
        /**
         * Check version.
         *
         * @param pDoc the doc
         * @param pModule the module
         * @throws Exception in case a problem occurred while checking the version.
         */
        private void checkVersion(Document pDoc, String pModule) throws Exception {
            NodeList tNodes = pDoc.getElementsByTagName("Version");
            if (tNodes.getLength() == 0) {
                throw new AsInitializationException(pModule + ", version missing");
            }
            Element tVersion = (Element) tNodes.item(0);
            if (AsConfigXmlParserIf.VERSION_MAJOR != Integer.valueOf(tVersion.getAttribute("major")).intValue()) {
                throw new AsInitializationException(pModule + ", wrong major version (" 
                    + AsConfigXmlParserIf.VERSION_MAJOR + " expected)");
            }
            if (AsConfigXmlParserIf.VERSION_MINOR != Integer.valueOf(tVersion.getAttribute("minor")).intValue()) {
                throw new AsInitializationException(pModule + ", wrong minor version (" 
                    + AsConfigXmlParserIf.VERSION_MINOR + " expected)");
            }
        }

        /**
         * Gets the module load order.
         *
         * @return the module load order
         */
        public List<String> getModuleLoadOrder() {
            return getModuleLoadOrderRecursive(mModule, new ArrayList<String>());
        }

        /**
         * Gets the module load order recursive.
         *
         * @param pModule the module
         * @param pList the list
         * @return the module load order recursive
         */
        private List<String> getModuleLoadOrderRecursive(String pModule, List<String> pList) {
            for (String tInheritedModule : mInheritance.get(pModule)) {
                getModuleLoadOrderRecursive(tInheritedModule, pList);
            }
            if (!pList.contains(pModule)) {
                pList.add(pModule);
            }
            return pList;
        }
        
        /**
         * Assemble module.
         *
         * @return the document
         * @throws TransformerException in case an error occurred during the transformation process.
         * @throws DOMException in case of a DOM traversing problem.
         * @throws ParserConfigurationException in case an error occurred during the parsing of the configuration
         */
        private Document assembleModule() 
            throws TransformerException, DOMException, ParserConfigurationException {
            
            // create empty document
            DocumentBuilderFactory tFactory = DocumentBuilderFactory.newInstance();
            Document tDoc = tFactory.newDocumentBuilder().getDOMImplementation().createDocument(null, null, null);
            Element tRoot = tDoc.createElement("Configuration");
            tDoc.appendChild(tRoot);
            System.out.println("AsXmlTool: assemble AS configuration");
            for (String tModule : getModuleLoadOrder()) {
                System.out.println("...module " + tModule);
                Document tC = mDocuments.get(tModule);
                NodeList tElem = XPathAPI.selectNodeList(tC.getDocumentElement(), "/Configuration/*");
                for (int j = 0; j < tElem.getLength(); j++) {
                    Element tNode = (Element) tElem.item(j);
                    if (tNode.getTagName().equals("Version")) {
                        continue;
                    }
                    if (tNode.getTagName().equals("inherits")) {
                        continue;
                    }
                    tRoot.appendChild(tDoc.createTextNode(tNode.getPreviousSibling().getNodeValue()));
                    tNode.setAttribute("module", tModule);
                    tNode.getParentNode().removeChild(tNode);
                    tDoc.adoptNode(tNode);
                    tRoot.appendChild(tNode);
                }
            }
            return tDoc;

        }

    }

    /**
     * Parses the xml stream.
     *
     * @param pInputSource the input source
     * @return the document
     * @throws Exception the in case a problem occurred.
     */
    public static Document parseXmlStream(InputSource pInputSource) throws Exception {
        DocumentBuilderFactory tFactory = DocumentBuilderFactory.newInstance();
        tFactory.setIgnoringComments(true);
        tFactory.setCoalescing(true); // Convert CDATA to Text nodes
        tFactory.setNamespaceAware(false); // No namespaces: this is default
        tFactory.setValidating(false); // Don't validate DTD: also default
        DocumentBuilder tParser = tFactory.newDocumentBuilder();
        return tParser.parse(pInputSource);
    }

}
