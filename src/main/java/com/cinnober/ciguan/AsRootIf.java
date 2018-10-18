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

import com.cinnober.ciguan.data.AsClientSession;
import com.cinnober.ciguan.datasource.AsDataSourceOwnerIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;

/**
 * Interface defining functionality in the application server root handle.
 */
public interface AsRootIf {

    /**
     * Convenience singleton returning the Compis instance.
     */
    public static class Singleton {

        /** The root. */
        static AsRootIf cAsRoot;

        /**
         * Gets the root.
         *
         * @return the root singleton instance
         */
        public static AsRootIf get() {
            return cAsRoot;
        }

        /**
         * Creates the root instance.
         */
        public static void create() {
            cAsRoot = AsBeanFactoryIf.Singleton.get().create(AsRootIf.class);
        }

    }

    /**
     * Get the global data sources.
     *
     * @return the global data sources
     */
    AsDataSourceOwnerIf getGlobalDataSources();

    /**
     * Get the member data sources.
     *
     * @param pMemberId the member id
     * @return the member data sources
     */
    AsDataSourceOwnerIf getMemberDataSources(String pMemberId);

    /**
     * Get the user data sources.
     *
     * @param pMemberId the member id
     * @param pUserId the user id
     * @return the user data sources
     */
    AsDataSourceOwnerIf getUserDataSources(String pMemberId, String pUserId);

    /**
     * Remove the member data sources.
     *
     * @param pMemberId the member id
     * @return the data source owner
     */
    AsDataSourceOwnerIf removeMemberDataSources(String pMemberId);

    /**
     * Remove the user data sources.
     *
     * @param pMemberId the member id
     * @param pUserId the user id
     * @return the data source owner
     */
    AsDataSourceOwnerIf removeUserDataSources(String pMemberId, String pUserId);

    //    /**
    //     * Get the AS connection associated with the HTTP request.
    //     *
    //     * @param pRequest the request
    //     * @return the connection
    //     */
    //    AsConnectionIf getAsConnection(HttpServletRequest pRequest);

    /**
     * @return the number of current AS connections
     */
    int getAsConnectionCount();

    /**
     * Fetch a pre-registered value retrieval method.
     *
     * @param <T> The type of the object from which the value will be fetched
     * @param pClass The class of the object from which the value will be fetched
     * @param pAttributeName The name of the attribute to fetch the value of
     * @return the pre registered getter
     */
    <T> AsGetMethodIf<T> getPreRegisteredGetter(Class<T> pClass, String pAttributeName);

    /**
     * Fetch all pre-registered getters for the given class.
     *
     * @param pClass the class
     * @return the pre registered getters
     */
    AsGetMethodIf<?>[] getPreRegisteredGetters(Class<?> pClass);

    /**
     * Exit gracefully.
     *
     * @param pString the string
     * @param pE the exception
     */
    void systemExit(String pString, Throwable pE);

    /**
     * Get the current time in ISO8601 format.
     *
     * @return the string
     */
    String now();

    /**
     * Invalidate the given AS connection.
     *
     * @param pConnection the connection
     */
    void invalidateAsConnection(AsConnectionIf pConnection);

    /**
     * Get the client session with the given session ID.
     *
     * @param pSessionId the session id
     * @return the client session
     */
    AsClientSession getClientSession(String pSessionId);

    /**
     * Get the AS connection associated with the given session ID.
     *
     * @param pSessionId the session id
     * @return the as connection
     */
    AsConnectionIf getAsConnection(String pSessionId);

    /**
     * Create an AS connection and associate it with the given session ID.
     *
     * @param pSessionId the session id
     * @param locale the session locale
     * @return the as connection
     */
    AsConnectionIf createAsConnection(String pSessionId, Locale locale);
    
}
