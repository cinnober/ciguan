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

import java.util.Collection;

/**
 * Interface defining operations on lists.
 *
 * @param <T> The type of the contained object
 */
public interface AsListIf<T> extends AsDataSourceIf<T> {

    /**
     * Add an item to the list.
     *
     * @param pItem the item to add
     */
    void add(T pItem);
    
    /**
     * Update an item in the list.
     *
     * @param pItem the item to update
     */
    void update(T pItem);
    
    /**
     * Remove an item from the list.
     *
     * @param pItem the item to remove
     */
    void remove(T pItem);

    /**
     * Add a snapshot to the list.
     *
     * @param pSnapshot the snapshot
     */
    void snapshot(Collection<T> pSnapshot);
    
    /**
     * Get the n:th item in the list.
     *
     * @param pIndex the index
     * @return the t
     */
    T get(int pIndex);
    
    /**
     * Get the index of the item.
     *
     * @param pItem the item
     * @return the int
     */
    int indexOf(T pItem);
    
    /**
     * Get the index of the object with the given key.
     *
     * @param pKey the key
     * @return the int
     */
    int indexOf(String pKey);
    
    /**
     * Get an item in the list identified by the given key.
     *
     * @param pKey the key
     * @return the t
     */
    T get(String pKey);
    
    /**
     * Get the size of the list.
     *
     * @return the int
     */
    int size();
    
    /**
     * Get a read-only copy of the contained values.
     *
     * @return the collection
     */
    Collection<T> values();
    
    /**
     * Remove all items from the list.
     */
    void clear();
    
    /**
     * Get the identifying key from the given object.
     *
     * @param pItem the item
     * @return the key
     */
    String getKey(T pItem);

    /**
     * Get the text to display in a list widget.
     *
     * @param pItem the item
     * @param pService the service
     * @return the text
     */
    String getText(T pItem, AsDataSourceServiceIf pService);

    /**
     * Get the name of the ID attribute.
     *
     * @return the id attribute
     */
    String getIdAttribute();
    
    /**
     * Get the name of the text attribute.
     *
     * @return the text attribute
     */
    String getTextAttribute();

    /**
     * Set the list metadata.
     *
     * @param pMeta the new list meta data
     */
    void setListMetaData(AsListMetaDataIf pMeta);
    
    /**
     * Get the list metadata.
     *
     * @return the list meta data
     */
    AsListMetaDataIf getListMetaData();
    
}
