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

import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsSortIf;
import com.cinnober.ciguan.datasource.impl.AsDataSource;

/**
 * This class is in reality a placeholder for the list tree. It does not contain any items. Instead it serves
 * only as a means to register a list tree datasource name in advance. Tree datasources are never pre-created in
 * the same way as lists. Instead they are instantiated when needed by the client.
 * 
 * @param <T> The type of object in the source list
 */
public class AsListTreeRoot<T> extends AsDataSource<AsListTreeNode> {

    /** The tree data. */
    private final AsListTreeDefinition mTreeData;
    
    /**
     * Instantiates a new as list tree root.
     *
     * @param pTreeData the tree data
     */
    public AsListTreeRoot(AsListTreeDefinition pTreeData) {
        super(pTreeData.getId(), null, null, null, AsListTreeNode.class);
        mTreeData = pTreeData;
    }
    
    /**
     * Creates the tree.
     *
     * @param pService the service
     * @return the as list tree list
     */
    public AsListTreeList<T> createTree(AsDataSourceServiceIf pService) {
        return new AsListTreeList<T>(pService, mTreeData);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public AsDataSourceIf<AsListTreeNode> createDataSource(AsFilterIf<AsListTreeNode> pFilter) {
        throw new UnsupportedOperationException();
    }
    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public AsDataSourceIf<AsListTreeNode> createDataSource(AsSortIf<AsListTreeNode> pSort) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * {@inheritDoc}
     * Not implemented.
     */
    @Override
    public void destroy() {
        
    }
}
