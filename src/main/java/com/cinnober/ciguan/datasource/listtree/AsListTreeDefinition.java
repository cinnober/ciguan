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
package com.cinnober.ciguan.datasource.listtree;

import java.util.List;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.datasource.impl.AsXmlRefData;

/**
 * Structured wrapper around a CwfDataIf object in order to interpret it as
 * a data source list tree configuration.
 */
@SuppressWarnings("deprecation")
public class AsListTreeDefinition extends AsXmlRefData implements MvcModelAttributesIf {

    /**
     * Instantiates a new as list tree definition.
     *
     * @param pData the data
     */
    public AsListTreeDefinition(CwfDataIf pData) {
        super(pData);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return getValues().getProperty(ATTR_ID);
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return getValues().getProperty(ATTR_SOURCE);
    }

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public String getFilter() {
        return getValues().getProperty(ATTR_FILTER);
    }

    /**
     * Gets the node key.
     *
     * @param pLevel the level
     * @return the node key
     */
    public String getNodeKey(int pLevel) {
        return getNodes().get(pLevel).getProperty(ATTR_KEY);
    }

    /**
     * Gets the depth.
     *
     * @return the depth
     */
    public int getDepth() {
        return getNodes().size();
    }

    /**
     * Gets the node data source id.
     *
     * @param pLevel the level
     * @return the node data source id
     */
    public String getNodeDataSourceId(int pLevel) {
        return getNodes().get(pLevel).getProperty(ATTR_SOURCE);
    }

    /**
     * Gets the node text.
     *
     * @param pLevel the level
     * @return the node text
     */
    public String getNodeText(int pLevel) {
        if (pLevel == 0) {
            return getValues().getProperty(ATTR_TEXT);
        }
        return getNodes().get(pLevel - 1).getProperty(ATTR_TEXT);
    }

    /**
     * Gets the nodes.
     *
     * @return the nodes
     */
    private List<CwfDataIf> getNodes() {
        return getValues().getObjectList(TAG_NODE);
    }

}
