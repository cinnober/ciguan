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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cinnober.ciguan.AsCacheIf;
import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;

/**
 *
 *
 */
public class AsCache extends AsComponent implements AsCacheIf, MvcModelAttributesIf {

    private Map<String, AsCacheReference<?, ?>> mCacheRefMap = new HashMap<String, AsCacheReference<?, ?>>();

    private <A, B> void addReference(String pType, String pField) {
        AsCacheReference<A, B> tRef = new AsCacheReference<A, B>(pType, pField);
        mCacheRefMap.put(tRef.getName(), tRef);
    }

    private void addReferences() {
        try {
            Node tDoc = As.getConfigXmlParser().getConfigurationDocument();
            NodeList tNodeList = XPathAPI.selectNodeList(tDoc, "//" + TAG_AS_CACHE + "/" + TAG_CACHE_REFERENCE);
            for (int i = 0; i < tNodeList.getLength(); i++) {
                Node tNode = tNodeList.item(i);
                CwfDataIf tTree = AsUtil.parse((Element) tNode);
                String tType = tTree.getProperty(ATTR_TYPE);
                String tField = tTree.getProperty(ATTR_FIELD);
                addReference(tType, tField);
            }
        }
        catch (TransformerException e) {
            throw new RuntimeException("Error selecting cache reference node", e);
        }
    }

    @Override
    public <T> T get(Class<T> pClass, String... pIds) {
        StringBuilder tKey = new StringBuilder();
        for (String tId : pIds) {
            tKey.append(tKey.length() > 0 ? "-" : "").append(tId);
        }
        return As.getGlobalDataSources().getDataSource(pClass).get(tKey.toString());
    }

    @Override
    public <T> Collection<T> get(Class<T> pClass) {
        return As.getGlobalDataSources().getDataSource(pClass).values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <From> Collection<From> getReferencingObjects(Class<From> pFrom, String pFieldName, String pToKey) {
        String tRefName = AsCacheReference.refName(pFrom, pFieldName);
        AsCacheReference<From, ?> tCacheRef = (AsCacheReference<From, ?>) mCacheRefMap.get(tRefName);
        return new ArrayList<From>(tCacheRef.getMapping(pToKey));
    }

    @Override
    public void allComponentsStarted() throws AsInitializationException {
        addReferences();
    }

    @Override
    public void reloadConfiguration() {
        for (AsCacheReference<?, ?> tRef : mCacheRefMap.values()) {
            tRef.destroy();
        }
        mCacheRefMap.clear();
        addReferences();
    }

}
