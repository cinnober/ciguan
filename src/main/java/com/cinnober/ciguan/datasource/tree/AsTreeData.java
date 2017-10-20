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
package com.cinnober.ciguan.datasource.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.data.AsDataSourceDef;
import com.cinnober.ciguan.datasource.impl.AsXmlRefData;
import com.cinnober.ciguan.impl.As;

/**
 * Structured wrapper around a CwfDataIf object in order to interpret it as
 * a data source tree configuration
 */
@SuppressWarnings("deprecation")
public class AsTreeData extends AsXmlRefData implements MvcModelAttributesIf {

    /** The root. */
    private Root mRoot;

    /** The nodes. */
    private final Map<String, Node> mNodes = new HashMap<String, Node>();

    /** The text. */
    private final Map<String, String> mText = new HashMap<String, String>();

    /**
     * Instantiates a new as tree data.
     *
     * @param pData the data
     */
    public AsTreeData(CwfDataIf pData) {
        super(pData);
        for (CwfDataIf tData : getValues().getAllChildNodes()) {
            if (TAG_ROOT.equals(tData.getProperty(ATTR_TAG_NAME))) {
                mRoot = new Root(tData);
            }
            else {
                Node tNode = new Node(tData);
                mNodes.put(tNode.getType(), tNode);
            }
        }
        mapTextAttributes();
    }

    /**
     * Map type/source to text.
     */
    protected void mapTextAttributes() {
        for (Child tChild : mRoot.getChildren()) {
            if (tChild.getText() != null) {
                if (tChild.getType() != null) {
                    mText.put(tChild.getType(), tChild.getText());
                }
                else if (tChild.getSource() != null) {
                    AsDataSourceDef<?> tDef = AsDataSourceDef.getDataSourceDef(tChild.getSource());
                    if (tDef.getType() != null) {
                        mText.put(tDef.getType(), tChild.getText());
                    }
                }
            }
        }
        for (Node tNode : mNodes.values()) {
            for (Child tChild : tNode.getChildren()) {
                if (tChild.getText() != null) {
                    if (tChild.getType() != null) {
                        mText.put(tChild.getType(), tChild.getText());
                    }
                    else if (tChild.getSource() != null) {
                        AsDataSourceDef<?> tDef = AsDataSourceDef.getDataSourceDef(tChild.getSource());
                        if (tDef.getType() != null) {
                            mText.put(tDef.getType(), tChild.getText());
                        }
                    }
                }
            }
        }
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
     * Gets the root.
     *
     * @return the root
     */
    public Root getRoot() {
        return mRoot;
    }

    /**
     * Gets the node.
     *
     * @param pItem the item
     * @return the node
     */
    public Node getNode(Object pItem) {
        if (pItem instanceof AsXmlRefData) {
            return mNodes.get(((AsXmlRefData) pItem).getTagName());
        }
        return mNodes.get(As.getTypeName(pItem.getClass()));
    }

    /**
     * Gets the item text.
     *
     * @param pItemType the item type
     * @return the item text
     */
    public String getItemText(String pItemType) {
        return mText.get(pItemType);
    }

    /**
     * The Class Root.
     */
    public class Root extends Node {

        /**
         * Instantiates a new root.
         *
         * @param pRoot the root
         */
        public Root(CwfDataIf pRoot) {
            super(pRoot);
        }

    }

    /**
     * The Class Node.
     */
    public class Node {

        /** The node data. */
        private final CwfDataIf mNodeData;

        /** The children. */
        private final List<Child> mChildren;

        /** The is link flag. */
        private final boolean mIsLink;

        /** The key ref attribute. */
        private final String mKeyRefAttribute;

        /**
         * Instantiates a new node.
         *
         * @param pNodeData the node data
         */
        public Node(CwfDataIf pNodeData) {
            mNodeData = pNodeData;
            mChildren = new ArrayList<Child>();
            mIsLink = TAG_LINK.equals(pNodeData.getProperty(ATTR_TAG_NAME));
            mKeyRefAttribute = pNodeData.getProperty(ATTR_KEY);
            int tIndex = 0;
            for (CwfDataIf tData : mNodeData.getAllChildNodes()) {
                mChildren.add(new Child(tData, tIndex++));
            }
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public String getType() {
            return mNodeData.getProperty(ATTR_TYPE);
        }

        /**
         * Checks if is link.
         *
         * @return {@code true}, if is link
         */
        public boolean isLink() {
            return mIsLink;
        }

        /**
         * Gets the key ref attribute.
         *
         * @return the key ref attribute
         */
        public String getKeyRefAttribute() {
            return mKeyRefAttribute;
        }

        /**
         * Gets the children.
         *
         * @return the children
         */
        public List<Child> getChildren() {
            return mChildren;
        }

    }

    /**
     * The Class Child.
     */
    public class Child {

        /** The is folder flag. */
        private final boolean mIsFolder;

        /** The source. */
        private String mSource;

        /** The type. */
        private final String mType;

        /** The filter. */
        private final String mFilter;

        /** The condition. */
        private final String mCondition;

        /** The index. */
        private final int mIndex;

        /** The label. */
        private final String mLabel;

        /** The text. */
        private final String mText;

        /**
         * Instantiates a new child.
         *
         * @param pData the data
         * @param pIndex the index
         */
        public Child(CwfDataIf pData, int pIndex) {
            mSource = pData.getProperty(ATTR_SOURCE);
            mType = pData.getProperty(ATTR_TYPE);
            mFilter = pData.getProperty(ATTR_FILTER);
            mCondition = pData.getProperty(ATTR_CONDITION);
            mText = pData.getProperty(ATTR_TEXT);
            mIsFolder = TAG_FOLDER.equals(pData.getProperty(ATTR_TAG_NAME));
            mIndex = pIndex;
            mLabel = (mType != null ? mType : mSource) + "_list";
        }

        /**
         * Checks if is folder.
         *
         * @return {@code true}, if is folder
         */
        public boolean isFolder() {
            return mIsFolder;
        }

        /**
         * Gets the source.
         *
         * @return the source
         */
        public String getSource() {
            if (mSource == null) {
                mSource = As.getGlobalDataSources().getDataSourceId(mType);
            }
            return mSource;
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public String getType() {
            return mType;
        }

        /**
         * Gets the filter.
         *
         * @return the filter
         */
        public String getFilter() {
            return mFilter;
        }

        /**
         * Gets the condition.
         *
         * @return the condition
         */
        public String getCondition() {
            return mCondition;
        }

        /**
         * Gets the text.
         *
         * @return the text
         */
        public String getText() {
            return mText;
        }

        /**
         * Gets the index.
         *
         * @return the index
         */
        public int getIndex() {
            return mIndex;
        }

        /**
         * Gets the label.
         *
         * @return the label
         */
        public String getLabel() {
            return mLabel;
        }

    }

}
