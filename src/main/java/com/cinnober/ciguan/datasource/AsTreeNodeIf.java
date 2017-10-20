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
package com.cinnober.ciguan.datasource;

/**
 * Defines a tree datasource node.
 *
 * @param <T> The type of the contained object
 */
public interface AsTreeNodeIf<T> {

    /** The path separator. */
    char PATH_SEPARATOR = 31; // "US", unit separator
    
    /**
     * Get the path of the node.
     *
     * @return the path
     */
    String getPath();
    
    /**
     * Get the level of the node.
     *
     * @return the level
     */
    String getLevel();
    
    /**
     * Get the contained item.
     *
     * @return the item
     */
    T getItem();
    
    /**
     * Set the contained item.
     *
     * @param pItem the new item
     */
    void setItem(T pItem);
    
    /**
     * Check if the node is expanded.
     *
     * @return null if the node is a leaf, otherwise true or false
     */
    Boolean getExpanded();

    /**
     * Get the type of the underlying object, if its derived from XML, return the 
     * corresponding tag name, otherwise return the class name (without package).
     *
     * @return the type
     */
    public String getType();
    
    /**
     * Get the text to display as the label.
     *
     * @return the label
     */
    String getLabel();

    /**
     * Get the state of the current node (for CSS purposes).
     *
     * @return the state
     */
    String getState();
    
    /**
     * Change expanded state.
     *
     * @param {@code true} if expanded
     */
    void setExpanded(boolean pExpanded);
    
    /**
     * Get the parent item.
     *
     * @return the parent
     */
    AsTreeNodeIf<?> getParent();
    
}
