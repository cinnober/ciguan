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
import java.util.Collections;
import java.util.List;

import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.data.AsMetaObject;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.filter.AsAttributeValueFilter;
import com.cinnober.ciguan.datasource.folder.AsTreeFolder;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;
import com.cinnober.ciguan.datasource.impl.AsXmlRefData;
import com.cinnober.ciguan.datasource.tree.AsTreeData.Child;
import com.cinnober.ciguan.datasource.tree.AsTreeData.Node;
import com.cinnober.ciguan.impl.As;

/**
 * Tree node containing an object.
 */
@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
public class AsTreeNodeWithObject extends AsTreeNode<Object> {

    /** The item source. */
    private final AsListIf mItemSource;
    
    /** The link source. */
    private AsListIf mLinkSource;
    
    /** The link key. */
    private String mLinkKey;
    
    /** The children. */
    private List<Child> mChildren;
    
    /** The listeners. */
    private final List<AsTreeNodeSource> mListeners = new ArrayList<AsTreeNodeSource>();
    
    /** The folders. */
    private List<AsTreeNodeFolder> mFolders;
    
    /** The state method. */
    private AsGetMethodIf<Object> mStateMethod;
    
    /** The text method. */
    private AsGetMethodIf<Object> mTextMethod;
    
    /** The is link flag. */
    private boolean mIsLink;
    
    /**
     * Default instance.
     *
     * @param pRoot the root
     * @param pItem the item
     * @param pItemSource the item source
     * @param pParentPath the parent path
     * @param pChildIndex the child index
     */
    public AsTreeNodeWithObject(AsTreeList pRoot, Object pItem, 
        AsListIf pItemSource, String pParentPath, int pChildIndex) {
        super(pRoot, pParentPath, pChildIndex + ":" + pItemSource.getKey(pItem), 
            pItem instanceof AsXmlRefData ? ((AsXmlRefData) pItem).getTagName() : As.getTypeName(pItem.getClass()),
                pItem);
        mItemSource = pItemSource;
        Node tNode = pRoot.getMappings(this);
        if (tNode != null) {
            mChildren = new ArrayList<Child>();
            for (Child tChild : tNode.getChildren()) {
                String tCondition = tChild.getCondition();
                AsFilterIf<Object> tFilter = null;
                if (tCondition != null) {
                    tFilter = (AsFilterIf<Object>) AsAttributeValueFilter.create(pItem.getClass(), tCondition); 
                }
                if (tFilter == null || tFilter.include(pItem)) {
                    mChildren.add(tChild);
                }
            }
            if (tNode.isLink() && !mChildren.isEmpty()) {
                mIsLink = true;
                Child tChild = mChildren.get(0);
                String tKeyAttr = tNode.getKeyRefAttribute();
                AsGetMethodIf tGetMethod = AsGetMethod.create(pItem.getClass(), tKeyAttr);
                mLinkKey = tGetMethod.getValue(pItem);
                mLinkSource = mRoot.getDataSource(tChild.getSource(), (AsFilterIf) null);
                setItem(mLinkSource.get(mLinkKey));
                mType = As.getTypeName(getItem().getClass());
                pRoot.addLinkListener(mLinkSource, mLinkKey, this);

                // Get the "new" children
                tNode = pRoot.getMappings(this);
                mChildren = tNode == null ? Collections.EMPTY_LIST : tNode.getChildren();
            }
            setExpandable(!mChildren.isEmpty());
        }
        AsMetaObject tMeta = As.getMetaDataHandler().getMetaData(pItem.getClass());
        if (tMeta != null && tMeta.getStateField() != null && !tMeta.getStateField().isEmpty()) {
            mStateMethod = tMeta.getGetMethod(tMeta.getStateField());
        }
        // Create the text getter if necessary
        String tTextAttribute = pRoot.getItemText(As.getTypeName(pItem.getClass()));
        if (tTextAttribute != null) {
            mTextMethod = (AsGetMethodIf<Object>) AsGetMethod.create(pItem.getClass(), tTextAttribute);
        }
    }
    
    @Override
    public void setItem(Object pItem) {
        if (mIsLink && (pItem == null || pItem.getClass() != mLinkSource.getItemClass())) {
            if (pItem == null) {
                // Inconsistent data, log
                AsLoggerIf.Singleton.get().log("ERROR: Tree link child object not found, data source: " +
                    mLinkSource.getDataSourceId() + " child key: " + mLinkKey);
            }
            else if (pItem.getClass() != mLinkSource.getItemClass()) {
                // Ignore updates to the link object, log as trace
                AsLoggerIf.Singleton.get().logTrace(
                    "The tree link object was updated, this is most likely an application error. " +
                    "The update was ignored.");
            }
            return;
        }
        super.setItem(pItem);
    }
    
    @Override
    public void onRemove() {
        collapseNode();
        mRoot.removeLinkListener(mLinkSource, mLinkKey, this);
    }
    
    @Override
    public String getLabel() {
        if (mTextMethod != null) {
            return mTextMethod.getText(getItem(), mRoot.getDataSourceService());
        }
        return mIsLink ?
            mLinkSource.getText(getItem(), mRoot.getDataSourceService()) :
            mItemSource.getText(getItem(), mRoot.getDataSourceService());
    }
    
    /**
     * Adds the folder.
     *
     * @param pFolder the folder
     */
    private void addFolder(AsTreeNodeFolder pFolder) {
        if (mFolders == null) {
            mFolders = new ArrayList<AsTreeNodeFolder>();
        }
        mFolders.add(pFolder);
        mRoot.add(pFolder);
    }
    
    @Override
    protected void expandNode() {
        for (Child tChild : mChildren) {
            if (tChild.isFolder()) {
                AsTreeFolder tFolder = 
                    new AsTreeFolder(tChild, getItem());
                AsTreeNodeFolder tNode = new AsTreeNodeFolder(mRoot, tFolder , mPath);
                addFolder(tNode);
            }
            else {
                // Get datasource
                final String tDataSourceId = tChild.getSource();
                AsListIf tList = null;
                if (tChild.getFilter() == null) {
                    tList = mRoot.getDataSource(tDataSourceId, (String) null);
                }
                else {
                    AsFilterIf tFilter = AsTreeFilterTool.createFilter(tChild.getFilter(), getItem());
                    if (tFilter != null) {
                        tList = mRoot.getDataSource(tDataSourceId, tFilter);
                    }
                    else {
                        String tExpression = AsTreeFilterTool.
                            expandFilterExpression(getItem(), tChild.getFilter().split(","));
                        tList = mRoot.getDataSource(tDataSourceId, tExpression);
                    }
                }
                // listener to data source
                mListeners.add(new AsTreeNodeSource(mRoot, mPath, tList, tChild.getIndex(), this));
            }
        }
    }
    
    @Override
    protected void collapseNode() {
        if (mFolders != null) {
            for (AsTreeNodeFolder tFolder : mFolders) {
                tFolder.collapseNode();
            }
        }
        ArrayList<AsTreeNode> tChildren = new ArrayList<AsTreeNode>();
        for (int tIndex = mRoot.indexOf(this) + 1; tIndex < mRoot.size(); tIndex++) {
            AsTreeNode tNode = mRoot.get(tIndex);
            if (!tNode.getPath().startsWith(getPath())) {
                break;
            }
            else if (tNode.getExpanded() != null) {
                tChildren.add(tNode);
            }
        }
        for (AsTreeNode tNode : tChildren) {
            tNode.setExpanded(false);
        }
        for (AsTreeNodeSource tListener : mListeners) {
            // Remove listener
            tListener.stop();
        }
        mListeners.clear();
        if (mFolders != null) {
            for (AsTreeNodeFolder tFolder : mFolders) {
                mRoot.remove(tFolder);
                tFolder.onRemove();
            }
        }
    }
    
    @Override
    public String getState() {
        return mStateMethod != null ? (String) mStateMethod.getObject(getItem()) : "";
    }
    
}
