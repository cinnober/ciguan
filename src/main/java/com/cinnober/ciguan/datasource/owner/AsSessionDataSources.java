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

import com.cinnober.ciguan.AsSessionDataIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceOwnerIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsSessionDataSourcesIf;
import com.cinnober.ciguan.datasource.AsSortIf;
import com.cinnober.ciguan.impl.As;

/**
 * Class holding all data sources for a user session.
 */
public class AsSessionDataSources implements AsSessionDataSourcesIf  {

    /** The global data sources. */
    private final AsDataSourceOwnerIf mGlobalDataSources;
    
    /** The member data sources. */
    private AsDataSourceOwnerIf mMemberDataSources;
    
    /** The user data sources. */
    private AsDataSourceOwnerIf mUserDataSources;
    
    /**
     * Instantiates a new as session data sources.
     */
    public AsSessionDataSources() {
        mGlobalDataSources = As.getGlobalDataSources();
        mUserDataSources = As.getUserDataSources(null, null);
    }
    
    @Override
    public <T> AsDataSourceIf<T> getDataSource(String pModelId, AsFilterIf<T> pFilter, AsSortIf<T> pSort) {

        // Try user data sources first
        if (mUserDataSources != null) {
            AsDataSourceIf<T> tModel = mUserDataSources.getDataSource(pModelId, pFilter, pSort);
            if (tModel != null) {
                return tModel;
            }
        }

        // Then member data sources
        if (mMemberDataSources != null) {
            AsDataSourceIf<T> tModel = mMemberDataSources.getDataSource(pModelId, pFilter, pSort);
            if (tModel != null) {
                return tModel;
            }
        }

        // Last resort, look among the global data sources
        return mGlobalDataSources.getDataSource(pModelId, pFilter, pSort);
    }

    @Override
    public void setSessionData(AsSessionDataIf pSessionData) {
        mMemberDataSources = As.getMemberDataSources(pSessionData.getMember());
        mUserDataSources = As.getUserDataSources(pSessionData.getMember(), pSessionData.getUser());
        mUserDataSources.setUsed(this, true);
    }

    @Override
    public String getName() {
        return null;
    }
    
    @Override
    public void destroy() {
        mUserDataSources.setUsed(this, false);
    }

    @Override
    public void setUsed(Object pUser, boolean pUsed) {
        // do nothing
    }
    
    @Override
    public boolean isRegisteredDataSourceId(String pId) {
        // Try user data sources first
        if (mUserDataSources != null) {
            if (mUserDataSources.isRegisteredDataSourceId(pId)) {
                return true;
            }
        }

        // Then member data sources
        if (mMemberDataSources != null) {
            if (mMemberDataSources.isRegisteredDataSourceId(pId)) {
                return true;
            }
        }

        // Last resort, look among the global data sources
        return mGlobalDataSources.isRegisteredDataSourceId(pId);
    }
    
}
