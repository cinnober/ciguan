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
import java.util.Formatter;

import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.folder.AsTreeFolder;

/**
 * Tree Node as a folder and reference to a data source to populate child nodes.
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class AsTreeNodeFolder extends AsTreeNode<AsTreeFolder> {

    /**
     * TODO: Pad to 4 digits as a trade-off to save bandwidth. Trees with 10000 folders will break this,
     * but it's not the end of the world.
     */
    private static final String FORMAT = "%04d";

    /** The label. */
    private final String mLabel;

    /** The listener. */
	private AsTreeNodeSource mListener;

    /** The filter. */
    private String mFilter;

    /** The list. */
    private AsListIf mList;

    /**
     * Default instance.
     *
     * @param pRoot the root
     * @param pFolder the folder
     * @param pParentPath the parent path
     */
    public AsTreeNodeFolder(AsTreeList pRoot, AsTreeFolder pFolder, String pParentPath) {
        super(pRoot, pParentPath, format(pFolder.getIndex()) + ":" +
            pFolder.getFolderName(), pFolder.getLabel(), pFolder);
        mLabel = mRoot.getLabel(getItem());
        setExpandable(true);
        mFilter = AsTreeFilterTool.expandFilterExpression(getItem().getParent(), getItem().getFilter());
        mList = mRoot.getDataSource(getItem().getFolderName(), mFilter);
        mListener = new AsTreeNodeSource(mRoot, mPath, mList, 0, this);
    }

    /**
     * Formats the string at the specified index.
     *
     * @param pIndex the index
     * @return the string
     */
    @SuppressWarnings("resource")
	private static String format(int pIndex) {
        StringBuilder tBuilder = new StringBuilder();
        Formatter tFormatter = new Formatter(tBuilder);
        tFormatter.format(FORMAT, pIndex);
        return tBuilder.toString();
    }

    @Override
    public String getLabel() {
        return mLabel + " (" + mList.size() + ")";
    }

	@Override
    protected void collapseNode() {

        if (mListener == null) {
            return;
        }
        ArrayList<AsTreeNode> tChildren = new ArrayList<AsTreeNode>();
        for (int tIndex = mRoot.indexOf(this) + 1; tIndex < mRoot.size(); tIndex++) {
            AsTreeNode tNode = mRoot.get(tIndex);
            if (!tNode.getPath().startsWith(getPath())) {
                break;
            }
            else {
                tChildren.add(tNode);
            }
        }
        for (AsTreeNode tNode : tChildren) {
            if (Boolean.TRUE.equals(tNode.getExpanded())) {
                tNode.setExpanded(false);
            }
            mRoot.remove(tNode);
            tNode.onRemove();
        }
        // Remove listener
    }

    @Override
    public void onRemove() {
        mListener.stop();
    }

    @Override
    protected void expandNode() {
        mListener.expand();
    }

    /**
     * Gets the item type.
     *
     * @return the item type
     */
    public String getItemType() {
        return getItem().getLabel();
    }

}
