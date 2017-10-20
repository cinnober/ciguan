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
package com.cinnober.ciguan.datasource.owner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cinnober.ciguan.AsDataSourceNotFoundException;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.data.AsDataSourceDef;
import com.cinnober.ciguan.datasource.AsDataSourceFactoryIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.filter.AsFilter;
import com.cinnober.ciguan.impl.As;

/**
 * Class holding member level data sources
 * The intention here is to hide the original data sources by registering filtered data sources using the same name
 * 
 * TODO: Make the code in this class automated through configuration.
 */
public class AsMemberDataSources extends AsDataSources {

    /**
     * Maximum attempts to retry a data source factory creation
     */
    private static final int MAX_RETRIES = 3;
    
    /** The member id. */
    private final String mMemberId;
    
    /**
     * Instantiates a new as member data sources.
     *
     * @param pParameters the parameters
     */
    public AsMemberDataSources(String... pParameters) {
        mMemberId = pParameters[0];
        
        List<AsDataSourceDef<?>> tDataSources = new ArrayList<AsDataSourceDef<?>>();
        tDataSources.addAll(AsDataSourceDef.getAll());
        for (int i = 0; i < MAX_RETRIES && !tDataSources.isEmpty(); i++) {
            createMemberDataSources(tDataSources);
        }
        if (!tDataSources.isEmpty()) {
            logAndDie(tDataSources);
        }
    }

    /**
     * Creates the member data sources.
     *
     * @param pDataSourceDefinitions the data source definitions
     */
    protected void createMemberDataSources(List<AsDataSourceDef<?>> pDataSourceDefinitions) {
        Iterator<AsDataSourceDef<?>> tIterator = pDataSourceDefinitions.iterator();
        while (tIterator.hasNext()) {
            AsDataSourceDef<?> tDataSourceDef = tIterator.next();
            AsDataSourceFactoryIf<?> tFactory = tDataSourceDef.getFactoryImpl();
            if (tFactory != null) {
                try {
                    AsListIf<?> tList = tFactory.createMemberList(tDataSourceDef.getId(), this);
                    if (tList != null) {
                        tList.setPermanent();
                        putDataSource(tList);
                        tIterator.remove();
                        continue;
                    }
                }
                catch (AsDataSourceNotFoundException e) {
                    // Lookup failed, leave the definition in the list
                    AsLoggerIf.Singleton.get().log(getClass().getSimpleName() + "(" + getName() +
                        "): Creation of data source " + tDataSourceDef.getId() +
                        " was delayed due to a data source lookup failure: " + e.getMessage());
                    continue;
                }
            }
            applyMemberFilter(tDataSourceDef);
            tIterator.remove();
        }
    }

    /**
     * Apply member filter.
     *
     * @param <T> the generic type
     * @param pDataSourceDef the data source def
     */
    private <T> void applyMemberFilter(AsDataSourceDef<T> pDataSourceDef) {
        AsFilter<T> tFilter = pDataSourceDef.createMemberFilter(getMemberId());
        applyMemberFilter(pDataSourceDef.getId(), tFilter);
    }

    /**
     * Apply member filter.
     *
     * @param <T> the generic type
     * @param pDataSourceId the data source id
     * @param pFilter the filter
     */
    protected <T> void applyMemberFilter(String pDataSourceId, AsFilterIf<T> pFilter) {
        createMemberDataSource(pDataSourceId, pDataSourceId, pFilter);
    }
    
    /**
     * Creates the member data source.
     *
     * @param <T> the generic type
     * @param pNewDataSource the new data source
     * @param pOriginalSourceId the original source id
     * @param pFilter the filter
     */
    protected <T> void createMemberDataSource(String pNewDataSource, String pOriginalSourceId, AsFilterIf<T> pFilter) {
        AsDataSourceIf<T> tOriginalDataSource = As.getGlobalDataSources().getDataSource(
            pOriginalSourceId, null, null);
        if (pFilter == null) {
            putDataSourceAs(tOriginalDataSource, pNewDataSource);
        }
        else {
            AsDataSourceIf<T> tDataSource = tOriginalDataSource.createDataSource(pFilter);
            tDataSource.setPermanent();
            putDataSourceAs(tDataSource, pNewDataSource);
        }
    }

    @Override
    public String getName() {
        return mMemberId;
    }
    
    /**
     * Gets the member id.
     *
     * @return the member id
     */
    public String getMemberId() {
        return mMemberId;
    }
    
}
