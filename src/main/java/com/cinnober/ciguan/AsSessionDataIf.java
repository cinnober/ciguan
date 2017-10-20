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
package com.cinnober.ciguan;

import java.util.Locale;

import com.cinnober.ciguan.client.MvcDeviceTypeIf;

/**
 * Interface defining functionality for the application server session data.
 *
 * @author jorgen.ekroth, Cinnober Financial Technology
 */
public interface AsSessionDataIf {

    /**
     * Get the user.
     *
     * @return the user
     */
    String getUser();

    /**
     * Get the member.
     *
     * @return the member
     */
    String getMember();

    /**
     * Set the locale for the session.
     *
     * @param pLocale the new locale
     */
    void setLocale(Locale pLocale);

    /**
     * Get the locale for the session.
     *
     * @return the locale
     */
    Locale getLocale();

    /**
     * If logged in.
     *
     * @return true, if is logged in
     */
    boolean isLoggedIn();

    /**
     * If user is acting on behalf.
     *
     * @return true, if is act on behalf
     */
    boolean isActOnBehalf();

    /**
     * Get current perspective.
     *
     * @return the perspective id
     */
    String getPerspectiveId();

    /**
     * Get the logged in user.
     *
     * @return the logged in user
     */
    String getLoggedInUser();

    /**
     * Get the logged in member.
     *
     * @return the logged in member
     */
    String getLoggedInMember();

    /**
     * Set act on behalf information.
     *
     * @param pMember the member
     * @param pUser the user
     */
    void setActOnBehalf(String pMember, String pUser);

    /**
     * Set the request token.
     *
     * @param pRequestToken the new request token
     */
    void setRequestToken(String pRequestToken);

    /**
     * Get the request token.
     *
     * @return the request token
     */
    String getRequestToken();

    /**
     * Get the device type that the session is used for.
     *
     * @return the device type
     */
    MvcDeviceTypeIf getDeviceType();

    /**
     * Set the client session.
     *
     * @param pData the new client session
     */
    void setClientSession(CwfDataIf pData);

    /**
     * Get the client session.
     *
     * @return the client session
     */
    CwfDataIf getClientSession();

}
