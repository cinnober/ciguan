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
package com.cinnober.ciguan.transport.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.AsTransportConfigurationIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.AsComponent;
import com.cinnober.ciguan.impl.AsUtil;
import com.cinnober.ciguan.xml.impl.AsDef;
import com.cinnober.ciguan.xml.impl.AsDefBdxProcessor;
import com.cinnober.ciguan.xml.impl.AsDefCopyAttribute;
import com.cinnober.ciguan.xml.impl.AsDefGenerateUniqueId;
import com.cinnober.ciguan.xml.impl.AsDefSuppressAttribute;
import com.cinnober.ciguan.xml.impl.AsDefSuppressClass;
import com.cinnober.ciguan.xml.impl.AsDefSuppressClassAttribute;

/**
 *
 * Class holding the transport configuration.
 *
 */
public class AsTransportConfiguration extends AsComponent implements AsTransportConfigurationIf {

    /** The plugin classes. */
    protected final List<String> mPluginClasses = new ArrayList<String>();

    /** The bdx classes. */
    protected final List<String> mBdxClasses = new ArrayList<String>();

    /** The bdx processors. */
    protected final List<AsDefBdxProcessor> mBdxProcessors = new ArrayList<AsDefBdxProcessor>();

    /** The request transformer configuration. */
    protected AsRequestTransformerConfiguration mRequestTransformerConfiguration =
        new AsRequestTransformerConfiguration();

    @Override
    public void startComponent() throws AsInitializationException {
        parse(As.getConfigXmlParser().getConfigurationDocument());
    }

    /**
     * Parses the transport configuration.
     *
     * @param pNode the node
     */
    protected void parse(Element pNode) {
        parsePlugin(pNode);
        parseRequestTransformerConfiguration(pNode);
        parseSuppressionConfiguration(pNode);
        parseBdxClasses(pNode);
        parseBdxProcessors(pNode);
    }

    /**
     * Adds the request transformer configuration.
     *
     * @param pConfig the config
     */
    public void addRequestTransformerConfiguration(AsRequestTransformerConfiguration pConfig) {
        mRequestTransformerConfiguration = pConfig;
    }

    @Override
    public String[] getPluginClasses() {
        return mPluginClasses.toArray(new String[mPluginClasses.size()]);
    }

    @Override
    public AsRequestTransformerConfiguration getRequestTransformerConfiguration() {
        return mRequestTransformerConfiguration;
    }

    @Override
    public String[] getBdxClasses() {
        return mBdxClasses.toArray(new String[mBdxClasses.size()]);
    }

    @Override
    public AsDefBdxProcessor[] getBdxProcessors() {
        return mBdxProcessors.toArray(new AsDefBdxProcessor[mBdxProcessors.size()]);
    }

    /**
     * Parses the transport plugins configuration.
     *
     * @param pNode the node
     * @throws RuntimeException if an error occurred while parsing the transport plugins configuration.
     */
    protected void parsePlugin(Node pNode) {
        try {
            NodeList tNodeList = XPathAPI.selectNodeList(pNode,
                "//AsTransportConfiguration/Plugin");
            for (int i = 0; i < tNodeList.getLength(); i++) {
                Node tNode = tNodeList.item(i);
                CwfDataIf tTree = AsUtil.parse((Element) tNode);
                String tPluginClass = tTree.getProperty("pluginClass");
                String tRemove = tTree.getProperty("remove");

                if (tRemove != null && !tRemove.isEmpty()) {
                    assert mPluginClasses.contains(tRemove);
                    mPluginClasses.remove(tRemove);
                }
                else {
                    AsDef.ensureClassName(tPluginClass);
                    mPluginClasses.add(tPluginClass);
                }
            }
        }
        catch (TransformerException e) {
            throw new RuntimeException("Error selecting plugin source node", e);
        }
    }

    /**
     * Parses the request transformer configuration.
     *
     * @param pNode the node
     * @throws RuntimeException if an error occurred while parsing the request transformer configuration.
     */
    protected void parseRequestTransformerConfiguration(Node pNode) {
        try {
            NodeList tNodeList = XPathAPI.selectNodeList(pNode,
                "//AsTransportConfiguration/RequestTransformerConfiguration");
            for (int i = 0; i < tNodeList.getLength(); i++) {
                Node tNode = tNodeList.item(i);
                CwfDataIf tTree = AsUtil.parse((Element) tNode);
                for (CwfDataIf tGenerateUniqueId : tTree.getObjectList("GenerateUniqueId")) {
                    AsDefGenerateUniqueId tData = new AsDefGenerateUniqueId();
                    tData.setClassName(tGenerateUniqueId.getProperty("className"));
                    tData.setAttributeName(tGenerateUniqueId.getProperty("attributeName"));
                    mRequestTransformerConfiguration.addGenerateUniqueId(tData);
                }
                for (CwfDataIf tCopyAttribute : tTree.getObjectList("CopyAttribute")) {
                    AsDefCopyAttribute tData = new AsDefCopyAttribute();
                    tData.setClassName(tCopyAttribute.getProperty("className"));
                    tData.setFromAttributeName(tCopyAttribute.getProperty("fromAttributeName"));
                    tData.setToAttributeName(tCopyAttribute.getProperty("toAttributeName"));
                    mRequestTransformerConfiguration.addCopyAttribute(tData);
                }
            }
        }
        catch (TransformerException e) {
            throw new RuntimeException("Error selecting request transformer source node", e);
        }
    }

    /**
     * Parses the class and attribute suppression configuration.
     *
     * @param pNode the node
     * @throws RuntimeException if an error occurred while parsing the class and attribute suppression configuration.
     */
    protected void parseSuppressionConfiguration(Node pNode) {
        try {
            NodeList tNodeList = XPathAPI.selectNodeList(pNode, "//AsMeta");
            for (int i = 0; i < tNodeList.getLength(); i++) {
                Node tNode = tNodeList.item(i);
                CwfDataIf tTree = AsUtil.parse((Element) tNode);
                for (CwfDataIf tSuppressAttribute : tTree.getObjectList("SuppressAttribute")) {
                    AsDefSuppressAttribute tData = new AsDefSuppressAttribute();
                    tData.setDirection(tSuppressAttribute.getProperty("direction"));
                    tData.setAttributeName(tSuppressAttribute.getProperty("attributeName"));
                    mRequestTransformerConfiguration.addSuppressAttribute(tData);
                }
                for (CwfDataIf tSuppressClass : tTree.getObjectList("SuppressClass")) {
                    AsDefSuppressClass tData = new AsDefSuppressClass();
                    tData.setDirection(tSuppressClass.getProperty("direction"));
                    tData.setClassName(tSuppressClass.getProperty("className"));
                    mRequestTransformerConfiguration.addSuppressClass(tData);
                }
                for (CwfDataIf tSuppressClassAttribute : tTree.getObjectList("SuppressClassAttribute")) {
                    AsDefSuppressClassAttribute tData = new AsDefSuppressClassAttribute();
                    tData.setDirection(tSuppressClassAttribute.getProperty("direction"));
                    tData.setClassName(tSuppressClassAttribute.getProperty("className"));
                    tData.setAttributeName(tSuppressClassAttribute.getProperty("attributeName"));
                    mRequestTransformerConfiguration.addSuppressClassAttribute(tData);
                }
            }
        }
        catch (TransformerException e) {
            throw new RuntimeException("Error selecting meta source node", e);
        }
    }

    /**
     * Parses the BDX classes configuration.
     *
     * @param pNode the node
     * @throws RuntimeException if an error occurred while parsing the BDX classes configuration.
     */
    protected void parseBdxClasses(Node pNode) {
        try {
            NodeList tNodeList = XPathAPI.selectNodeList(pNode,
                "//AsTransportConfiguration/BdxClass");
            for (int i = 0; i < tNodeList.getLength(); i++) {
                Node tNode = tNodeList.item(i);
                CwfDataIf tTree = AsUtil.parse((Element) tNode);
                String tClassName = tTree.getProperty("className");
                if (!mBdxClasses.contains(tClassName)) {
                    AsDef.ensureClassName(tClassName);
                    mBdxClasses.add(tClassName);
                }
            }
        }
        catch (TransformerException e) {
            throw new RuntimeException("Error selecting BDX class source node", e);
        }
    }

    /**
     * Parses the BDX processors configuration.
     *
     * @param pNode the node
     * @throws RuntimeException if an error occurred while parsing the BDX processors configuration.
     */
    protected void parseBdxProcessors(Node pNode) {
        try {
            NodeList tNodeList = XPathAPI.selectNodeList(pNode,
                "//AsTransportConfiguration/BdxProcessor");
            for (int i = 0; i < tNodeList.getLength(); i++) {
                Node tNode = tNodeList.item(i);
                CwfDataIf tTree = AsUtil.parse((Element) tNode);
                String tClassName = tTree.getProperty("className");
                String tParameters = tTree.getProperty("parameters");
                String tRemove = tTree.getProperty("remove");

                if (tRemove != null && !tRemove.isEmpty()) {
                    AsDefBdxProcessor tProcessor = new AsDefBdxProcessor();
                    tProcessor.setClassName(tRemove);
                    assert mBdxProcessors.contains(tProcessor);
                    mBdxProcessors.remove(tProcessor);
                }
                else {
                    AsDef.ensureClassName(tClassName);
                    AsDefBdxProcessor tProcessor = new AsDefBdxProcessor();
                    tProcessor.setClassName(tClassName);
                    tProcessor.setParameters(tParameters);
                    mBdxProcessors.add(tProcessor);
                }
            }
        }
        catch (TransformerException e) {
            throw new RuntimeException("Error selecting BDX processor source node", e);
        }
    }

}
