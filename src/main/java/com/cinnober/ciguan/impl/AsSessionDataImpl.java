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

import java.util.Locale;

import com.cinnober.ciguan.AsSessionDataIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcDeviceTypeIf;

/**
 *
 * Implementation of the application server session data
 *
 */
public class AsSessionDataImpl implements AsSessionDataIf {

    protected final String mLoggedInUser;
    protected final String mLoggedInMember;
    protected String mMember;
    protected String mUser;
    protected Locale mLocale;
    protected String mPerspective;
    protected String mRequestToken;
    protected MvcDeviceTypeIf mDeviceType;
    protected CwfDataIf mClientSession;

    public AsSessionDataImpl() {
        this(null, null);
    }

    public AsSessionDataImpl(String... pCredentials) {
        mMember = pCredentials[0];
        mUser = pCredentials[1];
        mLoggedInUser = mUser;
        mLoggedInMember = mMember;
    }

    @Override
    public void setActOnBehalf(String pMember, String pUser) {
        mMember = pMember;
        mUser = pUser;
    }

    @Override
    public String getMember() {
        return mMember;
    }

    @Override
    public String getUser() {
        return mUser;
    }

    public String getLoggedInUser() {
        return mLoggedInUser;
    }

    public String getLoggedInMember() {
        return mLoggedInMember;
    }

    @Override
    public String getPerspectiveId() {
        return mPerspective;
    }

    @Override
    public boolean isLoggedIn() {
        return mUser != null;
    }

    @Override
    public boolean isActOnBehalf() {
        return isLoggedIn() && !mUser.equals(mLoggedInUser);
    }

    @Override
    public void setLocale(Locale pLocale) {
        mLocale = pLocale;
    }

    @Override
    public Locale getLocale() {
        return mLocale;
    }

    @Override
    public void setRequestToken(String pRequestToken) {
        mRequestToken = pRequestToken;
    }

    @Override
    public String getRequestToken() {
        return mRequestToken;
    }

    @Override
    public MvcDeviceTypeIf getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceType(MvcDeviceTypeIf pDeviceType) {
        mDeviceType = pDeviceType;
    }

    @Override
    public void setClientSession(CwfDataIf pData) {
        mClientSession = pData;
    }

    @Override
    public CwfDataIf getClientSession() {
        return mClientSession;
    }

}
