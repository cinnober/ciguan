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
 * Interface defining the server side data source operations.
 *
 * @param <T> the generic type
 */
public interface AsDataSourceIf<T> {

    /**
     * Get the ID of the data source.
     *
     * @return the data source id
     */
    String getDataSourceId();
    
    /**
     * Get the owner of this data source instance.
     *
     * @return the owner
     */
    AsDataSourceOwnerIf getOwner();
    
    /**
     * Set the owner of this data source inctance.
     *
     * @param pOwner the new owner
     */
    void setOwner(AsDataSourceOwnerIf pOwner);
    
    /**
     * Add a listener to the data source.
     *
     * @param pListener the listener
     */
    void addListener(AsDataSourceListenerIf<T> pListener);
    
    /**
     * Remove a listener from the data source.
     *
     * @param pListener the listener
     */
    void removeListener(AsDataSourceListenerIf<T> pListener);

    /**
     * Get the source data source, or null if this data source is a root data source.
     *
     * @return the source
     */
    AsDataSourceIf<T> getSource();
    
    /**
     * Get the data source's filter.
     *
     * @return the filter
     */
    AsFilterIf<T> getFilter();

    /**
     * Get the data source's comparator.
     *
     * @return the sort
     */
    AsSortIf<T> getSort();

    /**
     * Create a new data source based on the current data source using a specified filter.
     *
     * @param pFilter the filter
     * @return the as data source if
     */
    AsDataSourceIf<T> createDataSource(AsFilterIf<T> pFilter);
    
    /**
     * Create a new data source based on the current data source using a specified sort.
     *
     * @param pSort the sort
     * @return the as data source if
     */
    AsDataSourceIf<T> createDataSource(AsSortIf<T> pSort);
    
    /**
     * Get the class of the contained items.
     *
     * @return the item class
     */
    Class<T> getItemClass();
    
    /**
     * Does this data source have any listeners attached to it?.
     *
     * @return {@code true}, if successful
     */
    boolean hasListeners();
    
    /**
     * Get the time that this data source has had no listeners.
     *
     * @return the childless time
     */
    long getChildlessTime();
    
    /**
     * Destroy this data source.
     */
    void destroy();
    
    /**
     * Flag that this data source cannot be removed.
     */
    void setPermanent();
    
    /**
     * Is this data source removable or not?.
     *
     * @return {@code true}, if is permanent
     */
    boolean isPermanent();
    
}
