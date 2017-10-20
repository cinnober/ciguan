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
 * Interface defining operations on a data source owner.
 */
public interface AsDataSourceOwnerIf {
    
    /**
     * Get the data source identified by the given ID.
     *
     * @param <T> the generic type
     * @param pDataSourceId the data source id
     * @param pFilter the filter description
     * @param pSort the sort description
     * @return the data source
     */
    <T> AsDataSourceIf<T> getDataSource(String pDataSourceId, AsFilterIf<T> pFilter, AsSortIf<T> pSort);

    /**
     * Return the name of the given data source owner.
     *
     * @return the name
     */
    String getName();
    
    /**
     * Destroy this data source owner.
     */
    void destroy();
    
    /**
     * Mark this data source owner as in use.
     *
     * @param pUser the user
     * @param pUsed {@code true} if is used, {@code false} otherwise.
     */
    void setUsed(Object pUser, boolean pUsed);
    
    /**
     * Check if there is a registered data source with the given ID.
     *
     * @param pId the id
     * @return {@code true}, if is registered data source
     */
    boolean isRegisteredDataSourceId(String pId);
    
}
