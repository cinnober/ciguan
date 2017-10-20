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

import java.util.List;
import java.util.Set;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.datasource.RpcSortCriteriaIf.SortOrder;

/**
 * Interface defining functionality for viewport data sources.
 *
 * @param <T> the generic type
 */
public interface AsDataSourceViewportListenerIf<T> extends AsDataSourceListenerIf<T> {

    /**
     * Set the size of the current viewport.
     *
     * @param pViewportSize the new viewport size
     */
    void setViewportSize(int pViewportSize);

    /**
     * Set the selection of the current viewport.
     *
     * @param pSelectedObjectKey the selected object key
     * @param pSelectedObjectViewportIndex the selected object viewport index
     * @param pControlKeyDown {@code true} if the control key was pressed
     * @param pShiftKeyDown {@code true} if the shift key was pressed
     */
    void setViewportSelection(String pSelectedObjectKey, Integer pSelectedObjectViewportIndex,
        boolean pControlKeyDown, boolean pShiftKeyDown);

    /**
     * Move the selection of the current viewport.
     *
     * @param pSelectedObjectMovement the selected object movement
     * @param pControlKeyDown {@code true} if the control key was pressed
     * @param pShiftKeyDown {@code true} if the shift key was pressed
     */
    void moveViewportSelection(Integer pSelectedObjectMovement, boolean pControlKeyDown, boolean pShiftKeyDown);

    /**
     * Set the position of the current viewport.
     *
     * @param pOffset the offset
     */
    void moveViewportPosition(int pOffset);

    /**
     * Set the position of the current viewport.
     *
     * @param pPosition the position
     * @param pSnapToBottom {@code true} if is a snap to bottom
     */
    void setViewportPosition(int pPosition, boolean pSnapToBottom);

    /**
     * Set the filter.
     *
     * @param pFilterExpression the new viewport filter
     */
    void setViewportFilter(String pFilterExpression);

    /**
     * set sorting of the viewport.
     *
     * @param pAttributeIndex the attribute index
     * @param pSortOrder the sort order
     */
    void setViewportSortCriteria(int pAttributeIndex, SortOrder pSortOrder);

    /**
     * Replace the existing data source with a new one.
     *
     * @param pModel the new data source
     */
    void setDataSource(AsListIf<T> pModel);

    /**
     * Expand or collapse the item (Only valid for tree data sources).
     *
     * @param pObjectKey the object key
     * @param pExpanded {@code true} if is expanded
     */
    void setExpanded(String pObjectKey, boolean pExpanded);

    /**
     * Create a set of possible filter expressions based on the given criteria.
     *
     * @param pKey the key
     * @param pColumnIndex the column index
     * @return the list
     */
    List<CwfDataIf> createFilterExpressions(String pKey, int pColumnIndex);

    /**
     * Add a data source object.
     *
     * @param pAddedObject the added object
     */
    void addObject(T pAddedObject);

    /**
     * Update a data source object.
     *
     * @param pUpdatedObject the updated object
     */
    void updateObject(T pUpdatedObject);

    /**
     * Delete a data source object.
     *
     * @param pDeletedObject the deleted object
     */
    void deleteObject(T pDeletedObject);

    /**
     * Get the set of currently selected keys.
     *
     * @return the selected keys
     */
    Set<String> getSelectedKeys();

    /**
     * Retrieve the get methods used to obtain the attribute data.
     *
     * @return the gets the methods
     */
    AsGetMethodIf<T>[] getGetMethods();

}
