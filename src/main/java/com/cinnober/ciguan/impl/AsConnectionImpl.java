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
package com.cinnober.ciguan.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.AsSessionDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.MvcUserPreferenceAttributesIf;
import com.cinnober.ciguan.data.AsDictionaryLanguage;
import com.cinnober.ciguan.data.AsUserPreference;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceOwnerIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.request.AsRequestServiceIf;
import com.cinnober.ciguan.transport.AsTransportServiceIf;

/**
 * Implementation of the application server connection
 * @author jorgen.ekroth, Cinnober Financial Technology
 */
public class AsConnectionImpl implements AsConnectionIf, MvcUserPreferenceAttributesIf, MvcModelAttributesIf {

    private final AsDataSourceServiceIf mDataSourceService;
    private final AsRequestServiceIf mRequestService;
    private final AsTransportServiceIf mTransportService;
    private AsSessionDataIf mSessionData;
    private Map<Class<?>, Object> mConnectionDataMap = new HashMap<Class<?>, Object>();
    private String mSessionId;

    public AsConnectionImpl() {
        mDataSourceService = As.getBeanFactory().create(AsDataSourceServiceIf.class);
        mRequestService = As.getBeanFactory().create(AsRequestServiceIf.class);
        mTransportService = As.getBeanFactory().create(AsTransportServiceIf.class);
        mTransportService.setConnection(this);
        mTransportService.createPlugins();
        mSessionData = As.getBeanFactory().create(AsSessionDataIf.class, null, null);
    }

    @Override
    public AsDataSourceServiceIf getDataSourceService() {
        return mDataSourceService;
    }

    @Override
    public AsRequestServiceIf getRequestService() {
        return mRequestService;
    }

    @Override
    public AsTransportServiceIf getTransportService() {
        return mTransportService;
    }

    @Override
    public AsSessionDataIf getSessionData() {
        return mSessionData;
    }
    @Override
    public void setSessionData(AsSessionDataIf pSessionData) {
        setPreferredLocale(pSessionData);
        if (pSessionData.getLocale() == null) {
            pSessionData.setLocale(mSessionData.getLocale());
        }
        mSessionData = pSessionData;
        mDataSourceService.setSessionData(pSessionData);
        mRequestService.setSessionData(pSessionData);
    }

    @Override
    public Locale getLocale() {
        return mSessionData.getLocale();
    }

    @Override
    public boolean setLocale(String pLocale) {
        for (AsDictionaryLanguage tLanguage : As.getDictionaryHandler().getDictionaryLanguages()) {
            Locale tLocale = tLanguage.getLocale();
            if (tLocale.toString().equals(pLocale) || tLocale.getLanguage().equals(pLocale)) {
                if (mSessionData.getLocale() != tLocale) {
                    mSessionData.setLocale(tLocale);
                    mDataSourceService.setSessionData(mSessionData);
                    return true;
                }
                return false;
            }
        }
        // The browser locale is not among the supported locales, choose the first supported one
        Locale tLocale = As.getDictionaryHandler().getDefaultLanguage().getLocale();
        mSessionData.setLocale(tLocale);
        mDataSourceService.setSessionData(mSessionData);

        return false;
    }

    /**
     * Load the user's preferred locale, if configured
     * @param pSessionData
     */
    protected void setPreferredLocale(AsSessionDataIf pSessionData) {
        if (!pSessionData.isLoggedIn()) {
            return;
        }
        AsDataSourceIf<AsUserPreference> tDs =
                As.getGlobalDataSources().getDataSource("USER_PREFERENCES_ALL", null, null);
        AsListIf<AsUserPreference> tList = (AsListIf<AsUserPreference>) tDs;
        String tPrefKey = AsUserPreference.key(pSessionData.getUser(), ATTR_LANGUAGE_CODE);
        AsUserPreference tUserPref = tList.get(tPrefKey);
        if (tUserPref != null) {
            for (AsDictionaryLanguage tLanguage : As.getDictionaryHandler().getDictionaryLanguages()) {
                Locale tLocale = tLanguage.getLocale();
                if (tLocale.toString().equals(tUserPref.getValue())) {
                    AsLoggerIf.Singleton.get().logTrace("Setting user " + pSessionData.getUser() +
                            "'s locale to preferred: " + tUserPref.getValue());
                    pSessionData.setLocale(tLocale);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getConnectionData(Class<T> pType) {
        return (T) mConnectionDataMap.get(pType);
    }

    @Override
    public <T> void setConnectionData(Class<T> pType, T pObject) {
        mConnectionDataMap.put(pType, pObject);
    }

    @Override
    public String getSessionId() {
        return mSessionId;
    }

    @Override
    public void init(String pSessionId) {
        // set browser locale
        mSessionId = pSessionId;
        //        if (pRequest.getCookies() != null) {
        //            for (Cookie tCookie : pRequest.getCookies()) {
        //                if (tCookie.getName().equals("locale")) {
        //                    setLocale(tCookie.getValue());
        //                    return;
        //                }
        //            }
        //        }
        //        if (getSessionData().getLocale() == null) {
        //            Locale tLocale = pRequest.getLocale();
        //            if (!setLocale(tLocale.toString())) {
        //                // if default locale not ok, look for more
        //                Enumeration<Locale> tLocales = pRequest.getLocales();
        //                while (tLocales.hasMoreElements()) {
        //                    if (setLocale(tLocales.nextElement().toString())) {
        //                        break;
        //                    }
        //                }
        //            }
        //        }
    }

    @Override
    public void invalidate() {
        getDataSourceService().resetDataSourceSubscriptions();
        getTransportService().reset();
        // If this is an ongoing act on behalf, un-use the original user's data sources too
        if (getSessionData().isActOnBehalf()) {
            String tMemberId = getSessionData().getLoggedInMember();
            String tUserId = getSessionData().getLoggedInUser();
            AsDataSourceOwnerIf tUserDataSources = As.getUserDataSources(tMemberId, tUserId);
            tUserDataSources.setUsed(tUserDataSources, false);
        }
    }

}
