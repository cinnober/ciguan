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

import java.util.HashMap;
import java.util.Map;

import com.cinnober.ciguan.data.AsMetaObject;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.AsTreeNodeIf;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;
import com.cinnober.ciguan.datasource.impl.AsXmlRefData;
import com.cinnober.ciguan.impl.As;

/**
 * Implementation of a list tree node.
 */
@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
public class AsListTreeNode implements AsTreeNodeIf {

    /** The item source. */
    private final AsListIf mItemSource;

    /** The list. */
    private final AsListTreeList mList;

    /** The parent. */
    private final AsListTreeNode mParent;

    /** The children. */
    private final Map<String, AsListTreeNode> mChildren = new HashMap<String, AsListTreeNode>();

    /** The key. */
    private final String mKey;

    /** The path. */
    private final String mPath;

    /** The type. */
    private final String mType;

    /** The level. */
    private final String mLevel;

    /** The item. */
    private Object mItem;

    /** The expanded. */
    private Boolean mExpanded;

    /** The state method. */
    private AsGetMethodIf<Object> mStateMethod;

    /** The text method. */
    private AsGetMethodIf<Object> mTextMethod;

    /**
     * Default instance.
     *
     * @param pList the list
     * @param pParent the parent
     * @param pItem the item
     * @param pItemSource the item source
     * @param pExpanded the expanded
     */
    public AsListTreeNode(
        AsListTreeList pList, AsListTreeNode pParent, Object pItem, AsListIf pItemSource, Boolean pExpanded) {
        mItemSource = pItemSource;
        mList = pList;
        mParent = pParent;
        mItem = pItem;
        if (pParent == null) {
            mKey = "";
            mPath = "";
            mType = "";
            mLevel = "";
        }
        else {
            mKey = pItemSource.getKey(pItem);
            mPath = pParent.getPath() + PATH_SEPARATOR + mKey;
            mType = pItem instanceof AsXmlRefData ? ((AsXmlRefData) pItem).getTagName() :
                As.getTypeName(pItem.getClass());
            mLevel = getLevel(mPath);
        }
        mExpanded = pExpanded;
        if (pItem != null) {
            AsMetaObject tMeta = As.getMetaDataHandler().getMetaData(pItem.getClass());
            if (tMeta != null && tMeta.getStateField() != null && !tMeta.getStateField().isEmpty()) {
                mStateMethod = tMeta.getGetMethod(tMeta.getStateField());
            }
            // Initiate the text getter if needed
            String tItemText = pList.getNodeText(pList.getTreeDefinition().getDepth() - mLevel.length());
            if (tItemText != null) {
                mTextMethod = AsGetMethod.create(pItemSource.getItemClass(), tItemText);
            }
        }
    }

    /**
     * Instantiates a new as list tree node.
     *
     * @param pAsListTreeList the as list tree list
     */
    public AsListTreeNode(AsListTreeList pAsListTreeList) {
        this(pAsListTreeList, null, null, null, Boolean.TRUE);
    }

    /**
     * Gets the children.
     *
     * @return the children
     */
    public Map<String, AsListTreeNode> getChildren() {
        return mChildren;
    }

    @Override
    public Object getItem() {
        return mItem;
    }

    public void setItem(Object pItem) {
        mItem = pItem;
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
    public String getState() {
        return mStateMethod != null ? (String) mStateMethod.getObject(getItem()) : "";
    }

    @Override
    public void setExpanded(boolean pExpanded) {
        if (mExpanded == null || mExpanded == pExpanded) {
            return;
        }

        mExpanded = pExpanded;
        if (mExpanded) {
            for (AsListTreeNode tChild : mChildren.values()) {
                mList.add(tChild);
            }
        }
        else {
            for (AsListTreeNode tChild : mChildren.values()) {
                tChild.setExpanded(false);
                mList.remove(tChild);
            }
        }
        mList.update(this);
    }

    @Override
    public String getLabel() {
        if (mParent == null) {
            return "";
        }
        if (mTextMethod != null) {
            return mTextMethod.getText(mItem, mList.getDataSourceService());
        }
        return mItemSource.getText(mItem, mList.getDataSourceService());
    }

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

    /**
     * Adds the child.
     *
     * @param pKey the key
     * @param pNode the node
     */
    public void addChild(String pKey, AsListTreeNode pNode) {
        mChildren.put(pKey, pNode);
        if (mExpanded) {
            mList.add(pNode);
        }
    }

    /**
     * Update child.
     *
     * @param pKey the key
     * @param pItem the item
     */
    public void updateChild(String pKey, Object pItem) {
        AsListTreeNode tNode = mChildren.get(pKey);
        if (tNode != null) {
            tNode.setItem(pItem);
            if (mExpanded) {
                mList.update(tNode);
            }
        }
    }

    /**
     * Removes the child.
     *
     * @param pKey the key
     */
    public void removeChild(String pKey) {
        AsListTreeNode tNode = mChildren.remove(pKey);
        if (mExpanded) {
            mList.remove(tNode);
        }
        // Remove self when no children remain
        if (mParent != null && mChildren.size() == 0) {
            mParent.removeChild(mKey);
        }
    }

    @Override
    public AsTreeNodeIf<?> getParent() {
        return mParent;
    }

    /**
     * Gets the state get method.
     *
     * @return the state get method
     */
    public static AsGetMethod<AsListTreeNode> getStateGetMethod() {
        AsGetMethod<AsListTreeNode> tMethod = new AsGetMethod<AsListTreeNode>(AsListTreeNode.class, "_state") {
            @Override
            public Object getObject(AsListTreeNode pItem) {
                return pItem.getState();
            }
        };
        return tMethod;
    }

}
