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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceListenerIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.AsTreeNodeIf;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;
import com.cinnober.ciguan.datasource.impl.AsDataSourceServiceImpl;
import com.cinnober.ciguan.datasource.impl.AsEmapiTreeMapList;

/**
 * Implementation of a server side tree list. This list will contain all items
 * in the tree since the child nodes always contribute their content to the top level
 * as nodes are expanded or collapsed.
 * 
 * FIXME: This implementation should be split in two, where the expand/collapse
 * functionality is kept separate from the tree.
 * 
 * @param <T> The type of object in the source list
 */
public class AsListTreeList<T> extends AsEmapiTreeMapList<AsListTreeNode> {

    /** The source list. */
    private final AsListIf<T> mSourceList;
    
    /** The service. */
    private final AsDataSourceServiceIf mService;
    
    /** The tree definition. */
    private final AsListTreeDefinition mTreeDefinition;
    
    /** The root. */
    private final AsListTreeNode mRoot = new AsListTreeNode(this);
    
    /** The node data sources. */
    private AsListIf<?>[] mNodeDataSources;
    
    /** The node get methods. */
    private AsGetMethodIf<T>[] mNodeGetMethods;
    
    /** The group by listeners. */
    private List<AsDataSourceListenerIf<T>> mGroupByListeners =
        new ArrayList<AsDataSourceListenerIf<T>>();
    
    /** The source listener. */
    private AsDataSourceListenerIf<T> mSourceListener = new AsDataSourceListenerIf<T>() {

        @Override
        public AsDataSourceIf<T> getDataSource() {
            return null;
        }

        @Override
        public void onDataSourceEvent(AsDataSourceEventIf<T> pEvent) {
            switch (pEvent.getType()) {
                case ADD:
                    addItem(pEvent.getNewValue());
                    break;
                    
                case UPDATE:
                    // TODO: This might be expensive, but for now it's the easiest way
                    removeItem(pEvent.getOldValue());
                    addItem(pEvent.getNewValue());
                    break;
                    
                case REMOVE:
                    removeItem(pEvent.getOldValue());
                    break;
                    
                case SNAPSHOT:
                    for (T tItem : pEvent.getSnapshot()) {
                        addItem(tItem);
                    }
                    break;
                    
                default:;
            }
        }

    };
    
    
    /**
     * Instantiates a new as list tree list.
     *
     * @param pService the service
     * @param pTreeData the tree data
     */
    @SuppressWarnings("unchecked")
    public AsListTreeList(AsDataSourceServiceIf pService, AsListTreeDefinition pTreeData) {
        super(pTreeData.getId(), AsListTreeNode.class, "path", "label");
        mService = pService;
        mTreeDefinition = pTreeData;
        String tTreeSource = mTreeDefinition.getSource();
        mSourceList = getDataSource(tTreeSource, mTreeDefinition.getFilter(), 0);
        mNodeGetMethods = new AsGetMethodIf[mTreeDefinition.getDepth()];
        mNodeDataSources = new AsListIf<?>[mTreeDefinition.getDepth()];
        for (int i = 0; i < mTreeDefinition.getDepth(); i++) {
            mNodeGetMethods[i] = AsGetMethod.create(mSourceList.getItemClass(), mTreeDefinition.getNodeKey(i));
            mNodeDataSources[i] = getDataSource(mTreeDefinition.getNodeDataSourceId(i), null, i);
        }
        mSourceList.addListener(mSourceListener);
    }
    
    /**
     * Adds the item.
     *
     * @param pItem the item
     */
    private void addItem(T pItem) {
        AsListTreeNode tCurrentNode = mRoot;
        for (int i = 0; i < mNodeGetMethods.length; i++) {
            String tKey = mNodeGetMethods[i].getValue(pItem);
            AsListTreeNode tNode = tCurrentNode.getChildren().get(tKey);
            if (tNode == null) {
                Object tContextItem = getContextItem(mNodeDataSources[i], tKey);
                tNode = new AsListTreeNode(this, tCurrentNode, tContextItem, mNodeDataSources[i], false);
                tCurrentNode.addChild(tKey, tNode);
            }
            tCurrentNode = tNode;
        }
        tCurrentNode.addChild(mSourceList.getKey(pItem),
            new AsListTreeNode(this, tCurrentNode, pItem, mSourceList, null));
    }

    /**
     * Removes the item.
     *
     * @param pItem the item
     */
    private void removeItem(T pItem) {
        AsListTreeNode tCurrentNode = mRoot;
        for (int i = 0; i < mNodeGetMethods.length; i++) {
            String tKey = mNodeGetMethods[i].getValue(pItem);
            AsListTreeNode tNode = tCurrentNode.getChildren().get(tKey);
            if (tNode == null) {
                return;
            }
            tCurrentNode = tNode;
        }
        tCurrentNode.removeChild(mSourceList.getKey(pItem));
    }
    
    /**
     * Gets the context item.
     *
     * @param pDataSource the data source
     * @param pKey the key
     * @return the context item
     */
    private Object getContextItem(AsListIf<?> pDataSource, String pKey) {
        Object tContextItem = pDataSource.get(pKey);
        if (tContextItem == null) {
            try {
                Class<?> tClass = pDataSource.getItemClass();
                tContextItem = tClass.newInstance();
                Field tField = tClass.getField(pDataSource.getIdAttribute());
                tField.set(tContextItem, pKey);
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to create parent item", e);
            }
        }
        return tContextItem;
    }
    
    /**
     * Gets the tree definition.
     *
     * @return the tree definition
     */
    public AsListTreeDefinition getTreeDefinition() {
        return mTreeDefinition;
    }
    
    /**
     * Helper datasource lookup.
     *
     * @param pDataSourceId the data source id
     * @param pFilterExpression the filter expression
     * @param pLevel the level
     * @return the data source
     */
    @SuppressWarnings("unchecked")
    public AsListIf<T> getDataSource(String pDataSourceId, String pFilterExpression, int pLevel) {
        // No source means use a group-by parent
        if (pDataSourceId == null) {
            AsListIf<GroupByTextNode> tParent = new AsEmapiTreeMapList<GroupByTextNode>(
                mSourceList.getDataSourceId() + "-GroupBy-" + mTreeDefinition.getNodeKey(pLevel),
                GroupByTextNode.class,
                "key", "text");
            new GroupByListener(tParent, pLevel);
            return (AsListIf<T>) tParent;
        }
        
        return (AsListIf<T>) ((AsDataSourceServiceImpl) mService).getDataSource(pDataSourceId, pFilterExpression, null);
    }

    /**
     * Get the full path of the item.
     *
     * @param pItem the item
     * @return the full path
     */
    protected String getFullPath(T pItem) {
        return AsTreeNodeIf.PATH_SEPARATOR + mSourceList.getKey(pItem);
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
     * Helper for tree nodes.
     *
     * @param pLevel the level
     * @return the node text
     */
    public String getNodeText(int pLevel) {
        return mTreeDefinition.getNodeText(pLevel);
    }
    
    @Override
    public void removeListener(AsDataSourceListenerIf<AsListTreeNode> pListener) {
        // Unregister the listeners to the root list
        mSourceList.removeListener(mSourceListener);
        for (AsDataSourceListenerIf<T> tListener : mGroupByListeners) {
            mSourceList.removeListener(tListener);
        }
        super.removeListener(pListener);
    }
    
    /**
     * Simple group-by class.
     */
    public static class GroupByTextNode {
        
        /** The key. */
        public String key;
        
        /** The text. */
        public String text;
        
        /** The count. */
        public int count;
        
    }
    
    /**
     * Listener performing the group-by aggregation.
     *
     * @see GroupByEvent
     */
    private class GroupByListener implements AsDataSourceListenerIf<T> {

        /** The list. */
        private final AsListIf<GroupByTextNode> mList;
        
        /** The key get method. */
        private final AsGetMethodIf<T> mKeyGetMethod;
        
        /** The text get method. */
        private final AsGetMethodIf<T> mTextGetMethod;
        
        /**
         * Instantiates a new group by listener.
         *
         * @param pList the list
         * @param pLevel the level
         */
        public GroupByListener(AsListIf<GroupByTextNode> pList, int pLevel) {
            mList = pList;
            mKeyGetMethod = mNodeGetMethods[pLevel];
            String tKeyAttribute = mKeyGetMethod.getAttributeName();
            String tTextAttribute = tKeyAttribute.substring(tKeyAttribute.indexOf(',') + 1);
            mTextGetMethod = AsGetMethod.create(mKeyGetMethod.getItemClass(), tTextAttribute);
            mSourceList.addListener(this);
            mGroupByListeners.add(this);
        }
 
        @Override
        public AsDataSourceIf<T> getDataSource() {
            return mSourceList;
        }

        @Override
        public void onDataSourceEvent(AsDataSourceEventIf<T> pEvent) {
            switch (pEvent.getType()) {
                case ADD:
                    add(pEvent);
                    break;

                case UPDATE:
                    update(pEvent);
                    break;
                    
                case REMOVE:
                    remove(pEvent);
                    break;

                case CLEAR:
                    mList.clear();
                    break;

                case DESTROY:
                    mSourceList.removeListener(this);
                    break;

                case SNAPSHOT:
                    snapshot(pEvent);
                    break;
                    
                default:;
            }
        }

        /**
         * Adds the item.
         *
         * @param pItem the item
         */
        private void add(T pItem) {
            GroupByTextNode tNew = new GroupByTextNode();
            tNew.key = mKeyGetMethod.getObject(pItem).toString();
            tNew.text = mTextGetMethod.getText(pItem, getDataSourceService());
            tNew.count = 1;
            
            GroupByTextNode tCurrent = mList.get(tNew.key);
            if (tCurrent == null) {
                mList.add(tNew);
            }
            else {
                tNew.count += tCurrent.count;
                mList.update(tNew);
            }
        }
        
        /**
         * Adds the value.
         *
         * @param pEvent the event
         */
        private void add(AsDataSourceEventIf<T> pEvent) {
            add(pEvent.getNewValue());
        }

        /**
         * Updates the value.
         *
         * @param pEvent the event
         */
        private void update(AsDataSourceEventIf<T> pEvent) {
            String tNewKey = mKeyGetMethod.getObject(pEvent.getNewValue()).toString();
            String tOldKey = mKeyGetMethod.getObject(pEvent.getOldValue()).toString();

            // No change of the key, nothing to do
            if (tNewKey.equals(tOldKey)) {
                return;
            }
            
            // Remove the old and add the new
            remove(pEvent.getOldValue());
            add(pEvent.getNewValue());
        }

        /**
         * Removes the item.
         *
         * @param pItem the item
         */
        private void remove(T pItem) {
            GroupByTextNode tNew = new GroupByTextNode();
            tNew.key = mKeyGetMethod.getObject(pItem).toString();
            tNew.text = mTextGetMethod.getText(pItem, getDataSourceService());
            tNew.count = 1;
            
            GroupByTextNode tCurrent = mList.get(tNew.key);
            if (tCurrent != null) {
                tNew.count = tCurrent.count - 1;
                if (tNew.count == 0) {
                    mList.remove(tNew);
                }
                else {
                    mList.update(tNew);
                }
            }
        }
        
        /**
         * Removes the value.
         *
         * @param pEvent the event
         */
        private void remove(AsDataSourceEventIf<T> pEvent) {
            remove(pEvent.getOldValue());
        }
        
        /**
         * Snapshot.
         *
         * @param pEvent the event
         */
        private void snapshot(AsDataSourceEventIf<T> pEvent) {
            for (T tItem : pEvent.getSnapshot()) {
                add(tItem);
            }
        }

    }
    
}
