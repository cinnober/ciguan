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
package com.cinnober.ciguan.datasource.impl;

import java.util.Collection;
import java.util.Comparator;

import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsSortIf;
import com.cinnober.ciguan.datasource.RpcSortCriteriaIf;
import com.cinnober.ciguan.datasource.AsDataSourceEventIf.Type;
import com.cinnober.ciguan.datasource.RpcSortCriteriaIf.SortOrder;
import com.cinnober.ciguan.datasource.filter.RpcAttributeSortCriteria;

/**
 * Sortable tree map list containing arbitrary EMAPI messages
 * 
 * This implementation extends a conventional tree map where the ID attribute is unique,
 * and adds a second map which is sorted on whatever the user has decided to sort on.
 * Both maps are maintained in sort order at all times. The add, update, remove, snapshot and get(i)
 * methods are overridden to manage both maps at the same time.
 * 
 * The sorted map uses a custom comparator which compares on all attributes being sorted
 * as well as on the ID attribute, in order to allow multiple entries with the same values.
 * 
 * FIXME: We need to avoid running the filters twice. The double synchronization is also unfortunate but
 * most likely marginal since we have already obtained the lock on the mutex when the superclass method
 * is called.
 * 
 * @param <E> The type of EMAPI object that the list contains
 */
public class AsEmapiSortableTreeMapList<E extends Object> extends AsEmapiTreeMapList<E> {

    /** The sorted items. */
    private final IndexedTreeMap<E, E> mSortedItems;
    
    /** The value attribute sorter. */
    private final AsAttributeValueSort<E> mValueAttributeSorter;
    
    /** The id attribute sorter. */
    private final AsAttributeValueSort<E> mIdAttributeSorter;
    
    /** The snapshot. */
    private Collection<E> mSnapshot;
    
    /**
     * Instantiates a new as emapi sortable tree map list.
     *
     * @param pModelId the model id
     * @param pSource the source
     * @param pFilter the filter
     * @param pSort the sort
     */
    public AsEmapiSortableTreeMapList(String pModelId, AsEmapiTreeMapList<E> pSource,
        AsFilterIf<E> pFilter, AsSortIf<E> pSort) {
        super(pModelId, pSource, pFilter, pSort);
        if (pSort == null) {
            throw new IllegalArgumentException("Sort criteria must not be null");
        }
        mSortedItems = new IndexedTreeMap<E, E>(new SortComparator());
        mValueAttributeSorter = new AsAttributeValueSort<E>(pSource.getItemClass(), pSort.getSortCriteria());
        mIdAttributeSorter = new AsAttributeValueSort<E>(pSource.getItemClass(),
            new RpcSortCriteriaIf[] {
                new RpcAttributeSortCriteria(getIdAttribute(), SortOrder.ASCENDING)
            });
        doSnapshot();
    }

    @Override
    public void add(E pItem) {
        synchronized (mMutex) {
            if (include(pItem)) {
                // Add to the sorted map too
                mSortedItems.put(pItem, pItem);
                int tIndex = mSortedItems.indexOfKey(pItem);
                notifyListeners(AsDataSourceEvent.create(this, Type.ADD, tIndex, pItem, null));
                
                // Add quietly to the ID map
                super.add(pItem, false);
            }
            assert mSortedItems.size() == size();
        }
    };

    @Override
    public void update(E pNewValue) {
        synchronized (mMutex) {
            if (include(pNewValue)) {
                // Remove from and add to sorted map too
                E tOldValue = get(getKey(pNewValue));
                if (tOldValue != null) {
                    E tOldSortedValue = mSortedItems.remove(tOldValue);
                    assert tOldSortedValue != null;
                }
                mSortedItems.put(pNewValue, pNewValue);
                int tIndex = mSortedItems.indexOfKey(pNewValue);
                if (tOldValue == null) {
                    notifyListeners(AsDataSourceEvent.create(this, Type.ADD, tIndex, pNewValue, null));
                }
                else {
                    notifyListeners(AsDataSourceEvent.create(this, Type.UPDATE, tIndex, pNewValue, tOldValue));
                }
                
                // Update quietly to the ID map
                super.update(pNewValue, false);
            }
            else {
                remove(pNewValue);
            }
            assert mSortedItems.size() == size();
        }
    };

    @Override
    public void remove(E pItem) {
        synchronized (mMutex) {
            // Remove from the sorted map
            E tOldValue = get(getKey(pItem));
            if (tOldValue != null) {
                int tIndex = mSortedItems.indexOfKey(tOldValue);
                E tOldSortedValue = mSortedItems.remove(tOldValue);
                assert tOldSortedValue != null;
                notifyListeners(AsDataSourceEvent.create(this, Type.REMOVE, tIndex, pItem, tOldValue));
                
                // Remove quietly from the ID map
                super.remove(pItem, false);
            }
            assert mSortedItems.size() == size();
        }
    };
    
    /**
     * Store the snapshot until everything is initialized properly.
     *
     * @param pSnapshot the snapshot
     */
    @Override
    public void snapshot(Collection<E> pSnapshot) {
        mSnapshot = pSnapshot;
    }    

    @Override
    public void clear() {
        mSortedItems.clear();
        super.clear();
    }
    
    /**
     * Do the actual snapshot processing.
     */
    protected void doSnapshot() {
        // Populate the sorted map
        for (E tItem : mSnapshot) {
            if (include(tItem)) {
                mSortedItems.put(tItem, tItem);
            }
        }
        
        // Let the ID map process the snapshot and notify its listeners
        super.snapshot(mSnapshot);
        mSnapshot = null;
    }

    @Override
    public E get(int pIndex) {
        return mSortedItems.get(pIndex);
    }

    @Override
    public int indexOf(String pKey) {
        E tItem = super.get(pKey);
        if (tItem != null) {
            return mSortedItems.indexOfKey(tItem);
        }
        return -1;
    }

    @Override
    public void destroy() {
        super.destroy();
        mSortedItems.clear();
        mSnapshot = null;
    }

    
    /**
     * Comparator used to sort the map. Note that if all sorted attributes have the same values,
     * an extra compare is done using the ID attribute in order to never consider two entries
     * equal, which would result in objects considered equal being overwritten.
     */
    protected class SortComparator implements Comparator<E> {
        
        @Override
        public int compare(E pThis, E pThat) {
            // First, compare the values being sorted on
            int tDiff = mValueAttributeSorter.compare(pThis, pThat);
            if (tDiff != 0) {
                return tDiff;
            }
            // All the same so far, use the unique ID attribute as the final decider
            return mIdAttributeSorter.compare(pThis, pThat);
        }
    }
    
}
