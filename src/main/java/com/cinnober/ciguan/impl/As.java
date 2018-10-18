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

import com.cinnober.ciguan.AsAuthorizationHandlerIf;
import com.cinnober.ciguan.AsBdxHandlerIf;
import com.cinnober.ciguan.AsBeanFactoryIf;
import com.cinnober.ciguan.AsConfigXmlParserIf;
import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsDictionaryHandlerIf;
import com.cinnober.ciguan.AsFormatIf;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.AsMetaDataHandlerIf;
import com.cinnober.ciguan.AsRootIf;
import com.cinnober.ciguan.AsTransportConfigurationIf;
import com.cinnober.ciguan.AsUserPropertyPersisterIf;
import com.cinnober.ciguan.datasource.AsDataSourceOwnerIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.owner.AsGlobalDataSources;

/**
 * Easy access helper class which assists in hiding the (evil) singleton pattern
 */
public class As {

    public static final int STATUS_CODE_OK = 3001; // TODO
    public static final int STATUS_CODE_NOK = -1; // TODO

    public static AsGlobalDataSources getGlobalDataSources() {
        return (AsGlobalDataSources) AsRootIf.Singleton.get().getGlobalDataSources();
    }

    public static AsDataSourceOwnerIf getMemberDataSources(String pMemberId) {
        return AsRootIf.Singleton.get().getMemberDataSources(pMemberId);
    }

    public static AsDataSourceOwnerIf getUserDataSources(String pMemberId, String pUserId) {
        return AsRootIf.Singleton.get().getUserDataSources(pMemberId, pUserId);
    }

    public static AsDataSourceOwnerIf removeMemberDataSources(String pMemberId) {
        return AsRootIf.Singleton.get().removeMemberDataSources(pMemberId);
    }

    public static AsDataSourceOwnerIf removeUserDataSources(String pMemberId, String pUserId) {
        return AsRootIf.Singleton.get().removeUserDataSources(pMemberId, pUserId);
    }

    public static AsLoggerIf getLogger() {
        return AsLoggerIf.Singleton.get();
    }

    public static AsBeanFactoryIf getBeanFactory() {
        return AsBeanFactoryIf.Singleton.get();
    }

    public static AsBdxHandlerIf getBdxHandler() {
        return AsBdxHandlerIf.Singleton.get();
    }

    public static AsTransportConfigurationIf getTransportConfiguration() {
        return AsTransportConfigurationIf.SINGLETON.get();
    }

    public static AsAuthorizationHandlerIf getAuthorizationHandler() {
        return AsAuthorizationHandlerIf.Singleton.get();
    }

    public static AsUserPropertyPersisterIf getUserPropertyPersister() {
        return AsUserPropertyPersisterIf.Singleton.get();
    }

    public static AsConfigXmlParserIf getConfigXmlParser() {
        return AsConfigXmlParserIf.SINGLETON.get();
    }

    public static AsDictionaryHandlerIf getDictionaryHandler() {
        return AsDictionaryHandlerIf.SINGLETON.get();
    }

    public static <T> Class<T> getType(String pObjectType) {
        return AsMetaDataHandlerIf.SINGLETON.get().getType(pObjectType);
    }

    public static <T> String getTypeName(Class<T> pClass) {
        return AsMetaDataHandlerIf.SINGLETON.get().getTypeName(pClass);
    }

    public static AsFormatIf getFormatter() {
        return AsFormatIf.Singleton.get();
    }

    public static <T> AsGetMethodIf<T> getPreRegisteredGetter(Class<T> pClass, String pAttributeName) {
        return AsRootIf.Singleton.get().getPreRegisteredGetter(pClass, pAttributeName);
    }

    public static AsGetMethodIf<?>[] getPreRegisteredGetters(Class<?> pClass) {
        return AsRootIf.Singleton.get().getPreRegisteredGetters(pClass);
    }

    public static void reloadConfiguration() {
        for (AsComponent tComponent : AsComponent.getAllComponents()) {
            tComponent.reloadConfiguration();
        }
    }

    public static AsConnectionIf createAsConnection(String pSessionId, Locale locale) {
        return AsRootIf.Singleton.get().createAsConnection(pSessionId, locale);
    }

    public static void systemExit(String pString, Throwable pE) {
        AsRootIf.Singleton.get().systemExit(pString, pE);
    }

    public static AsMetaDataHandlerIf getMetaDataHandler() {
        return AsMetaDataHandlerIf.SINGLETON.get();
    }

    public static String now() {
        return AsRootIf.Singleton.get().now();
    }

}
