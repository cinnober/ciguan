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
 * Factory for data sources.
 *
 * @param <T> The type of object in the data source
 */
public interface AsDataSourceFactoryIf<T> {

    /**
     * Create a global data source with the given ID.
     *
     * @param pId the id
     * @return the {@link AsListIf}
     */
    AsListIf<T> createGlobalList(String pId);

    /**
     * Create a member level data source with the given ID.
     *
     * @param pId the id
     * @param pMemberDataSources the member data sources
     * @return the {@link AsListIf}
     */
    AsListIf<T> createMemberList(String pId, AsDataSourceOwnerIf pMemberDataSources);
    
    /**
     * Create a user level data source with the given ID.
     *
     * @param pId the id
     * @param pUserDataSources the user data sources
     * @return the {@link AsListIf}
     */
    AsListIf<T> createUserList(String pId, AsDataSourceOwnerIf pUserDataSources);

    /**
     * Get the type of object stored in the data source.
     *
     * @return the item class
     */
    Class<T> getItemClass();
    
    /**
     * Is this list a root list which receives broadcasts, or is it a child list which receives notifications
     * from its parent?.
     *
     * @return {@code true}, if is root list
     */
    boolean isRootList();

}
