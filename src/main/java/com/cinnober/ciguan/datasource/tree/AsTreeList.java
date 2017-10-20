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

import java.util.HashMap;
import java.util.Map;

import com.cinnober.ciguan.data.AsDictionaryWord;
import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceListenerIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.AsTreeNodeIf;
import com.cinnober.ciguan.datasource.folder.AsTreeFolder;
import com.cinnober.ciguan.datasource.impl.AsDataSourceServiceImpl;
import com.cinnober.ciguan.datasource.impl.AsEmapiTreeMapList;
import com.cinnober.ciguan.datasource.tree.AsTreeData.Child;
import com.cinnober.ciguan.datasource.tree.AsTreeData.Node;
import com.cinnober.ciguan.impl.As;

/**
 * Implementation of a server side tree list. This list will contain all items
 * in the tree since the child nodes always contribute their content to the top level
 * as nodes are expanded or collapsed.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AsTreeList extends AsEmapiTreeMapList<AsTreeNode> {

    /** The service. */
    private final AsDataSourceServiceIf mService;

    /** The tree data. */
    private final AsTreeData mTreeData;

    /** The link listeners. */
    private final Map<AsListIf, LinkListener> mLinkListeners =
        new HashMap<AsListIf, LinkListener>();

    /**
     * Instantiates a new as tree list.
     *
     * @param pService the service
     * @param pTreeData the tree data
     */
    public AsTreeList(AsDataSourceServiceIf pService, AsTreeData pTreeData) {
        super(pTreeData.getId(), AsTreeNode.class, "path", "label");
        mService = pService;
        mTreeData = pTreeData;

        for (Child tChild : mTreeData.getRoot().getChildren()) {
            // TODO: test for condition on session object
            if (tChild.isFolder()) {
                AsTreeFolder tFolder = new AsTreeFolder(tChild, null);
                add(new AsTreeNodeFolder(this, tFolder, ""));
            }
            else {
                AsListIf tSourceList = getDataSource(tChild.getSource(), tChild.getFilter());
                tSourceList.addListener(new AsTreeNodeSource(this, "", tSourceList, tChild.getIndex(), null));
            }
        }
    }

    /**
     * Adds the link listener.
     *
     * @param pList the list
     * @param pKey the key
     * @param pNode the node
     */
    public void addLinkListener(AsListIf pList, String pKey, AsTreeNodeIf pNode) {
        LinkListener tListener = mLinkListeners.get(pList);
        if (tListener == null) {
            tListener = new LinkListener(pList);
            mLinkListeners.put(pList, tListener);
        }
        tListener.addNode(pKey, pNode);
    }

    /**
     * Removes the link listener.
     *
     * @param pList the list
     * @param pKey the key
     * @param pNode the node
     */
	@SuppressWarnings("unlikely-arg-type")
	public void removeLinkListener(AsListIf pList, String pKey, AsTreeNodeIf pNode) {
        LinkListener tListener = mLinkListeners.get(pList);
        if (tListener != null) {
            tListener.removeNode(pKey, pNode);
            if (tListener.size() == 0) {
                mLinkListeners.remove(tListener);
            }
        }
    }

    /**
     * Get the data source mappings for the given node.
     *
     * @param pNode the node
     * @return the mappings
     */
    public Node getMappings(AsTreeNode pNode) {
        return mTreeData.getNode(pNode.getItem());
    }

    /**
     * Get the text attribute for a given list item type.
     *
     * @param pItemType the item type
     * @return the item text
     */
    public String getItemText(String pItemType) {
        return mTreeData.getItemText(pItemType);
    }

    /**
     * Helper datasource lookup.
     *
     * @param pDataSourceId the data source id
     * @param pFilterExpression the filter expression
     * @return the data source
     */
    public AsListIf getDataSource(String pDataSourceId, String pFilterExpression) {
        return (AsListIf) ((AsDataSourceServiceImpl) mService).getDataSource(pDataSourceId, pFilterExpression, null);
    }

    /**
     * Helper datasource lookup.
     *
     * @param pDataSourceId the data source id
     * @param pFilter the filter
     * @return the data source
     */
    public AsListIf getDataSource(String pDataSourceId, AsFilterIf pFilter) {
        return (AsListIf) ((AsDataSourceServiceImpl) mService).getDataSource(pDataSourceId, pFilter);
    }

    /**
     * Helper for tree nodes.
     *
     * @return the data source service
     */
    public AsDataSourceServiceIf getDataSourceService() {
        return mService;
    }

    /**
     * Gets the label.
     *
     * @param pItem the item
     * @return the label
     */
    public String getLabel(AsTreeFolder pItem) {
        AsListIf<AsDictionaryWord> tList = As.getGlobalDataSources().getDataSource(AsDictionaryWord.class);
        AsDictionaryWord tWord = (AsDictionaryWord) tList.get(".folder." + pItem.getLabel());
        if (tWord == null) {
            return pItem.getLabel();
        }
        return tWord.getText(mService.getLocale());
    }

    /**
     * Class listening to linked object updates.
     *
     * @see LinkEvent
     */
	private class LinkListener implements AsDataSourceListenerIf {

        /** The list. */
        private final AsListIf mList;

        /** The nodes. */
        private final Map<String, AsTreeNodeIf> mNodes = new HashMap<String, AsTreeNodeIf>();

        /**
         * Instantiates a new link listener.
         *
         * @param pList the list
         */
        public LinkListener(AsListIf pList) {
            mList = pList;
        }

		@Override
        public AsDataSourceIf getDataSource() {
            return mList;
        }

		@Override
        public void onDataSourceEvent(AsDataSourceEventIf pEvent) {
            switch (pEvent.getType()) {
                case UPDATE:
                    String tKey = mList.getKey(pEvent.getNewValue());
                    AsTreeNodeIf tNode = mNodes.get(tKey);
                    if (tNode != null) {
                        tNode.setItem(pEvent.getNewValue());
                        update((AsTreeNode) tNode);
                    }
                    break;

                default:
                    break;
            }
        }

        /**
         * Adds the node.
         *
         * @param pKey the key
         * @param pNode the node
         */
        public void addNode(String pKey, AsTreeNodeIf pNode) {
            mNodes.put(pKey, pNode);
            if (size() == 1) {
                mList.addListener(this);
            }
        }

        /**
         * Removes the node.
         *
         * @param pKey the key
         * @param pNode the node
         */
        public void removeNode(String pKey, AsTreeNodeIf pNode) {
            mNodes.remove(pKey);
            if (size() == 0) {
                mList.removeListener(this);
            }
        }

        /**
         * Returns the size.
         *
         * @return the size
         */
        public int size() {
            return mNodes.size();
        }

    }

}
