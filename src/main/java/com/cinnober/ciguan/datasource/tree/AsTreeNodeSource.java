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

import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceListenerIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.AsTreeNodeIf;
import com.cinnober.ciguan.datasource.AsDataSourceEventIf.Type;

/**
 * Listener to populate children for tree nodes.
 *
 * @param <T> the generic type
 */
class AsTreeNodeSource<T> implements AsDataSourceListenerIf<T> {
    
    /** The root. */
    private final AsTreeList mRoot;
    
    /** The path. */
    private final String mPath;
    
    /** The data source. */
    private final AsListIf<T> mDataSource;
    
    /** The index. */
    private final int mIndex;
    
    /** The parent. */
    private final AsTreeNode<?> mParent;
    
    /**
     * Instantiates a new as tree node source.
     *
     * @param pRoot the root
     * @param pPath the path
     * @param pSource the source
     * @param pChildIndex the child index
     * @param pFolder the folder
     */
    AsTreeNodeSource(AsTreeList pRoot, String pPath, AsListIf<T> pSource, int pChildIndex, AsTreeNode<?> pFolder) {
        mRoot = pRoot;
        mPath = pPath;
        mDataSource = pSource;
        mIndex = pChildIndex;
        mParent = pFolder;
        pSource.addListener(this);
    }
    
    /**
     * Stop.
     */
    public void stop() {
        mDataSource.removeListener(this);
        // Remove all items in the list
        if (mParent == null || Boolean.TRUE.equals(mParent.getExpanded())) {
            for (T tItem : mDataSource.values()) {
                AsTreeNode<?> tNode = mRoot.get(getKey(tItem, mDataSource));
                tNode.onRemove();
                mRoot.remove(tNode);
            }
        }
    }

    @Override
    public AsDataSourceIf<T> getDataSource() {
        throw new UnsupportedOperationException("This method is invalid for a tree node");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onDataSourceEvent(AsDataSourceEventIf<T> pEvent) {
        if (pEvent.getType() == Type.DESTROY) {
            return;
        }
        else if (mParent == null || Boolean.TRUE.equals(mParent.getExpanded())) {
            switch (pEvent.getType()) {
                case ADD:
                    mRoot.add(new AsTreeNodeWithObject(mRoot, pEvent.getNewValue(), pEvent.getSource(), mPath, mIndex));
                    updateFolder();
                    break;
                case UPDATE: {
                    String tKey = getKey(pEvent.getNewValue(), pEvent.getSource());
                    AsTreeNode<T> tNode = mRoot.get(tKey);
                    tNode.setItem(pEvent.getNewValue());
                    mRoot.update(tNode);
                    break;
                }
                case REMOVE:
                    AsTreeNode<?> tNode = mRoot.get(getKey(pEvent.getOldValue(), pEvent.getSource()));
                    tNode.onRemove();
                    mRoot.remove(tNode);
                    updateFolder();
                    break;
                case SNAPSHOT:
                    for (Object tItem : pEvent.getSnapshot()) {
                        mRoot.add(new AsTreeNodeWithObject(mRoot, tItem, pEvent.getSource(), mPath, mIndex));
                    }
                    updateFolder();
                    break;
                case CLEAR:
                    // TODO: Fold and delete all nodes
                    break;
                default:;
            }
        }
        else {
            updateFolder();
        }
    }
    
    /**
     * Update folder.
     */
    private void updateFolder() {
        if (mParent != null) {
            mRoot.update(mParent);
        }
    }
    
    /**
     * Assemble a complete key for an item in the root data source
     * FIXME: Place this somewhere else?.
     *
     * @param pItem the item
     * @param pSource the source
     * @return the key
     */
    private String getKey(T pItem, AsListIf<T> pSource) {
        return mPath + AsTreeNodeIf.PATH_SEPARATOR + mIndex + ":" + pSource.getKey(pItem);
    }

    /**
     * Expand.
     */
    public void expand() {
        // force a new snapshot
        mDataSource.removeListener(this);
        mDataSource.addListener(this);
    }
    
}
