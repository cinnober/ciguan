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
package com.cinnober.ciguan.datasource.folder;

import com.cinnober.ciguan.datasource.tree.AsTreeData.Child;

/**
 * Base class for tree folders.
 */
public class AsTreeFolder {

    /** The child. */
    private final Child mChild;
    
    /** The parent. */
    private final Object mParent;
    
    /**
     * Instantiates a new as tree folder.
     *
     * @param pChild the child
     * @param pParent the parent
     */
    public AsTreeFolder(Child pChild, Object pParent) {
        mChild = pChild;
        mParent = pParent;
    }

    /**
     * Gets the folder name.
     *
     * @return the folder name
     */
    public String getFolderName() {
        return mChild.getSource();
    }

    /**
     * Gets the folder type.
     *
     * @return the folder type
     */
    public String getFolderType() {
        return mChild.getType();
    }
    
    /**
     * Gets the parent.
     *
     * @return the parent
     */
    public Object getParent() {
        return mParent;
    }
    
    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public String getFilter() {
        return mChild.getFilter();
    }
    
    /**
     * Gets the index.
     *
     * @return the index
     */
    public int getIndex() {
        return mChild.getIndex();
    }
    
    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return mChild.getLabel();
    }
    
}
