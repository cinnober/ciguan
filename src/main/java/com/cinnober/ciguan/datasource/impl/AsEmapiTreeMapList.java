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

import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceListenerIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsSortIf;
import com.cinnober.ciguan.datasource.filter.AsIncludeAllFilter;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;

/**
 * Tree map list containing arbitrary EMAPI messages.
 *
 * @param <E> The type of EMAPI object that the list contains
 */
public class AsEmapiTreeMapList<E extends Object> extends AsTreeMapList<E> implements AsDataSourceListenerIf<E> {
    
    /** The id attribute. */
    private final AsGetMethodIf<E> mIdAttribute;
    
    /** The text attribute. */
    private final AsGetMethodIf<E> mTextAttribute;
    
    /**
     * Instantiates a new as EMAPI tree map list.
     *
     * @param pModelId the model id
     * @param pItemClass the item class
     * @param pIdAttribute the id attribute
     * @param pTextAttribute the text attribute
     * @param pFilter the filter
     */
    public AsEmapiTreeMapList(String pModelId, Class<E> pItemClass, String pIdAttribute,
        String pTextAttribute, AsFilterIf<E> pFilter) {
        super(pModelId, null, pFilter, null, pItemClass);
        mIdAttribute = AsGetMethod.create(pItemClass, pIdAttribute);
        mTextAttribute = AsGetMethod.create(pItemClass, pTextAttribute);
    }

    /**
     * Instantiates a new as EMAPI tree map list.
     *
     * @param pModelId the model id
     * @param pItemClass the item class
     * @param pIdAttribute the id attribute
     * @param pTextAttribute the text attribute
     */
    public AsEmapiTreeMapList(String pModelId, Class<E> pItemClass, String pIdAttribute, String pTextAttribute) {
        this(pModelId, pItemClass, pIdAttribute, pTextAttribute, null);
    }
    
    /**
     * Instantiates a new as EMAPI tree map list.
     *
     * @param pModelId the model id
     * @param pType the type
     * @param pIdAttribute the id attribute
     */
    public AsEmapiTreeMapList(String pModelId, Class<E> pType, String pIdAttribute) {
        this(pModelId, pType, pIdAttribute, pIdAttribute);
    }

    /**
     * Instantiates a new as EMAPI tree map list.
     *
     * @param pModelId the model id
     * @param pSource the source
     * @param pFilter the filter
     * @param pSort the sort
     */
    public AsEmapiTreeMapList(
        String pModelId, AsEmapiTreeMapList<E> pSource, AsFilterIf<E> pFilter, AsSortIf<E> pSort) {
        super(pModelId, pSource, pFilter, pSort, pSource.getItemClass());
        mIdAttribute = pSource.mIdAttribute;
        mTextAttribute = pSource.mTextAttribute;
        getSource().addListener(this);
    }

    /**
     * Instantiates a new as EMAPI tree map list.
     *
     * @param pModelId the model id
     * @param pSource the source
     * @param pTextAttribute the attribute to use as text
     * @param pFilter the filter
     * @param pSort the sort
     */
    public AsEmapiTreeMapList(
        String pModelId, AsEmapiTreeMapList<E> pSource, String pTextAttribute,
        AsFilterIf<E> pFilter, AsSortIf<E> pSort) {
        super(pModelId, pSource, pFilter, pSort, pSource.getItemClass());
        mIdAttribute = pSource.mIdAttribute;
        mTextAttribute = AsGetMethod.create(getItemClass(), pTextAttribute);
        getSource().addListener(this);
    }
    
    @Override
    public String getKey(E pItem) {
        return mIdAttribute.getValue(pItem);
    }

    @Override
    public String getText(E pItem, AsDataSourceServiceIf pService) {
        return mTextAttribute.getText(pItem, pService);
    }

    @Override
    public AsDataSourceIf<E> getDataSource() {
        return getSource();
    }

    @Override
    public void onDataSourceEvent(AsDataSourceEventIf<E> pEvent) {
        switch (pEvent.getType()) {
            case ADD:
                add(pEvent.getNewValue());
                break;

            case UPDATE:
                update(pEvent.getNewValue());
                break;

            case REMOVE:
                remove(pEvent.getNewValue());
                break;

            case SNAPSHOT:
                snapshot(pEvent.getSnapshot());
                break;
                
            case CLEAR:
                clear();
                break;
                
            default:;
        }
    }
    
    /**
     * Handle incoming messages.
     *
     * @param pMessage the message
     */
    public void messageReceived(E pMessage) {
        update(pMessage);
    }    
    
    @Override
    public AsDataSourceIf<E> createDataSource(AsFilterIf<E> pFilter) {
        synchronized (mMutex) {
            return new AsEmapiTreeMapList<E>(getDataSourceId(), this, pFilter, null);
        }
    }

    @Override
    public AsDataSourceIf<E> createDataSource(AsSortIf<E> pSort) {
        synchronized (mMutex) {
            return new AsEmapiSortableTreeMapList<E>(getDataSourceId(), this,
                new AsIncludeAllFilter<E>(getFilter()), pSort);
        }
    }

    @Override
    public String getIdAttribute() {
        return mIdAttribute.getAttributeName();
    }

    @Override
    public String getTextAttribute() {
        return mTextAttribute.getAttributeName();
    }

    @Override
    public void destroy() {
        if (getSource() != null) {
            getSource().removeListener(this);
        }
        super.destroy();
    }

}
