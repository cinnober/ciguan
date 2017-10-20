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

import com.cinnober.ciguan.datasource.AsTreeNodeIf;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;

/**
 * Implementation of a tree node. Note that this tree node is not a conventional tree node in the sense
 * that it does not hold its children. Instead, the tree node is similar to an adapter that knows how to retrieve
 * the relevant children when the node itself is expanded. The child nodes are always added to and removed from
 * the top level list in order to obtain the normal viewport and client event handling. The node also listens to
 * events in the list it has attached itself to.
 *
 * @param <T> The type of the contained item
 */
public abstract class AsTreeNode<T> implements AsTreeNodeIf<T> {

    /** The root. */
    protected final AsTreeList mRoot;

    /** The path. */
    protected final String mPath;

    /** The parent path. */
    protected final String mParentPath;

    /** The type. */
    protected String mType;

    /** The level. */
    private final String mLevel;

    /** The expanded flag. */
    private Boolean mExpanded;

    /** The item. */
    private T mItem;

    /**
     * Instantiates a new as tree node.
     *
     * @param pRoot the root
     * @param pParentPath the parent path
     * @param pKey the key
     * @param pType the type
     * @param pItem the item
     */
    public AsTreeNode(AsTreeList pRoot, String pParentPath, String pKey, String pType, T pItem) {
        mRoot = pRoot;
        mPath = pParentPath + PATH_SEPARATOR + pKey;
        mParentPath = pParentPath;
        mType = pType;
        mLevel = getLevel(mPath);
        mItem = pItem;
    }

    /**
     * Sets the expandable flag.
     *
     * @param pExpandable the new expandable
     */
    protected void setExpandable(boolean pExpandable) {
        mExpanded = pExpandable ? Boolean.FALSE : null;
    }

    @Override
    public String getLevel() {
        return mLevel;
    }

    @Override
    public String getPath() {
        return mPath;
    }

    @Override
    public Boolean getExpanded() {
        return mExpanded;
    }

    @Override
    public String getType() {
        return mType;
    }

    @Override
    public T getItem() {
        return mItem;
    }

    public AsTreeNodeIf<?> getParent() {
        return mRoot.get(mParentPath);
    }

    public void setItem(T pItem) {
        mItem = pItem;
    }

    @Override
    public final void setExpanded(boolean pExpanded) {
        if (mExpanded != null && mExpanded.booleanValue() != pExpanded) {
            if (pExpanded) {
                mExpanded = true;
                expandNode();
            }
            else {
                collapseNode();
                mExpanded = false;
            }
            mRoot.update(this);
        }
    }

    /**
     * Expand node.
     */
    protected abstract void expandNode();

    /**
     * Collapse node.
     */
    protected abstract void collapseNode();


    /**
     * Get the level of this node by calculating the number of path separators.
     *
     * @param pPath the path
     * @return the level
     */
    protected String getLevel(String pPath) {
        String tLevel = "";
        for (int i = 1; i < pPath.length(); i++) {
            if (pPath.charAt(i) == PATH_SEPARATOR) {
                tLevel = tLevel + ".";
            }
        }
        return tLevel;
    }

    @Override
    public String getState() {
        return null;
    }

    /**
     * Gets the state get method.
     *
     * @return the state get method
     */
    @SuppressWarnings("rawtypes")
	public static AsGetMethod<AsTreeNode> getStateGetMethod() {
        AsGetMethod<AsTreeNode> tMethod = new AsGetMethod<AsTreeNode>(AsTreeNode.class, "_state") {
            @Override
            public Object getObject(AsTreeNode pItem) {
                return pItem.getState();
            }
        };
        return tMethod;
    }

    /**
     * Executed on remove.
     */
    public void onRemove() {
    }

}
