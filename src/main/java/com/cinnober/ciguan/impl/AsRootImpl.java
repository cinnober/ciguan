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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cinnober.ciguan.AsBdxHandlerIf;
import com.cinnober.ciguan.AsBeanFactoryIf;
import com.cinnober.ciguan.AsConfigXmlParserIf;
import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.AsRootIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.data.AsClientSession;
import com.cinnober.ciguan.datasource.AsDataSourceOwnerIf;
import com.cinnober.ciguan.datasource.AsGetMethodFactoryIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.getter.AsGetMethodFromExpression;
import com.cinnober.ciguan.datasource.getter.AsListGetMethod;
import com.cinnober.ciguan.datasource.owner.AsGlobalDataSources;
import com.cinnober.ciguan.datasource.owner.AsMemberDataSources;
import com.cinnober.ciguan.datasource.owner.AsUserDataSources;
import com.cinnober.ciguan.plugin.AsServicePluginIf;

/**
 * Implementation of the application server root
 *
 * This class constitutes the root of the application server. It holds references to global components
 * such as the instrument service, the locale service etc.
 *
 */
public class AsRootImpl extends AsComponent implements AsRootIf, MvcModelAttributesIf {

    protected AsDataSourceOwnerIf mGlobalDataSources;
    protected final Map<String, AsDataSourceOwnerIf> mMemberDataSources = new ConcurrentHashMap<String, AsDataSourceOwnerIf>();
    protected final Map<String, AsDataSourceOwnerIf> mUserDataSources = new ConcurrentHashMap<String, AsDataSourceOwnerIf>();
    protected final AsPreRegisteredGetters mGetters = new AsPreRegisteredGetters();
    protected final List<AsServicePluginIf> mServicePlugins = new ArrayList<AsServicePluginIf>();
    protected Map<String, AsConnectionIf> mConnectionsMap = new ConcurrentHashMap<String, AsConnectionIf>();
    protected SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public void startComponent() throws AsInitializationException {
        mGlobalDataSources = As.getBeanFactory().create(AsGlobalDataSources.class);
        registerConfiguredGetters();
        createServicePlugins();
    }

    @Override
    public void synchronizeExternalData() {
        startServicePlugins();
    }

    @Override
    public void stopComponent() throws AsInitializationException {
        // Stop all service plug-ins
        stopServicePlugins();

        // Destroy all data source containers, thus terminating their cleanup threads
        for (AsDataSourceOwnerIf tDs : new ArrayList<AsDataSourceOwnerIf>(mUserDataSources.values())) {
            tDs.destroy();
        }
        for (AsDataSourceOwnerIf tDs : new ArrayList<AsDataSourceOwnerIf>(mMemberDataSources.values())) {
            tDs.destroy();
        }
        mGlobalDataSources.destroy();
    }

    /**
     * Create the service plugins
     */
    protected void createServicePlugins() {
        try {
            Element tConfigDoc = As.getConfigXmlParser().getConfigurationDocument();
            NodeList tNodeList = XPathAPI.selectNodeList(tConfigDoc, "//AsPlugins/Plugin");
            for (int i = 0; i < tNodeList.getLength(); i++) {
                Node tNode = tNodeList.item(i);
                CwfDataIf tTree = AsUtil.parse((Element) tNode);
                String tPluginClass = tTree.getProperty("pluginClass");
                String tRemove = tTree.getProperty("remove");

                if (tRemove != null && !tRemove.isEmpty()) {
                    Iterator<AsServicePluginIf> tIterator = mServicePlugins.iterator();
                    while (tIterator.hasNext()) {
                        AsServicePluginIf tPlugin = tIterator.next();
                        if (tPlugin.getClass().getName().equals(tRemove)) {
                            tIterator.remove();
                        }
                    }
                }
                else {
                    Class<?> tClass = Class.forName(tPluginClass);
                    if (AsServicePluginIf.class.isAssignableFrom(tClass)) {
                        AsServicePluginIf tPlugin = (AsServicePluginIf) tClass.newInstance();
                        tPlugin.setParameters(tTree.getProperty(ATTR_PARAMETERS));
                        mServicePlugins.add(tPlugin);
                    }
                }
            }
        }
        catch (TransformerException e) {
            throw new RuntimeException("Error selecting plugin source node", e);
        }
        catch (Exception e) {
            throw new RuntimeException("Error instantiating service plugin", e);
        }
    }

    /**
     * Start and launch the service plugins
     */
    protected void startServicePlugins() {
        for (AsServicePluginIf tPlugin : mServicePlugins) {
            tPlugin.start(AsBdxHandlerIf.Singleton.get());
            tPlugin.launch();
        }
    }

    /**
     * Stop the service plugins
     */
    protected void stopServicePlugins() {
        for (AsServicePluginIf tPlugin : mServicePlugins) {
            tPlugin.stop();
        }
    }

    @Override
    public AsDataSourceOwnerIf getGlobalDataSources() {
        return mGlobalDataSources;
    }

    /**
     * TODO: Remove the member models object when the last user has logged out
     * (after a configurable period of time)
     */
    @Override
    public AsDataSourceOwnerIf getMemberDataSources(String pMemberId) {
    	final String tMemberId = pMemberId != null ? pMemberId : "";
        AsDataSourceOwnerIf tModels = mMemberDataSources.get(tMemberId);
        if (tModels != null) {
            return tModels;
        }
        tModels = As.getBeanFactory().create(AsMemberDataSources.class, tMemberId);
        mMemberDataSources.put(tMemberId, tModels);
        return tModels;
    }

    @Override
    public AsDataSourceOwnerIf removeMemberDataSources(String pMemberId) {
        return mMemberDataSources.remove(pMemberId);
    }

    /**
     * TODO: Remove the user models object when the user has logged out
     * (after a configurable period of time)
     */
    @Override
    public AsDataSourceOwnerIf getUserDataSources(String pMemberId, String pUserId) {
    	final String tUserId = pUserId != null ? pUserId : "";
        AsDataSourceOwnerIf tModels = mUserDataSources.get(tUserId);
        if (tModels != null) {
            return tModels;
        }
        tModels = As.getBeanFactory().create(AsUserDataSources.class, pMemberId, tUserId);
        mUserDataSources.put(tUserId, tModels);
        return tModels;
    }

    @Override
    public AsDataSourceOwnerIf removeUserDataSources(String pMemberId, String pUserId) {
        return mUserDataSources.remove(pUserId);
    }

    @Override
    public int getAsConnectionCount() {
        return mConnectionsMap.size();
    }

    @Override
    public AsConnectionIf createAsConnection(String pSessionId, Locale pLocale) {
        AsConnectionIf tConnection = mConnectionsMap.get(pSessionId);
        if (tConnection == null) {
            tConnection = AsBeanFactoryIf.Singleton.get().create(AsConnectionIf.class, pSessionId);
            tConnection.init(pSessionId, pLocale);
            AsClientSession tSession = new AsClientSession(pSessionId);
            // tSession.browserDetails = pRequest.getHeader("User-Agent");
            As.getBdxHandler().broadcast(tSession);
            mConnectionsMap.put(pSessionId, tConnection);
        }

        // Register a session invalidator if configured
        try {
            // AsConnectionInvalidatorIf tInvalidator =
            //     AsBeanFactoryIf.Singleton.get().create(AsConnectionInvalidatorIf.class);
            // pRequest.getSession().setAttribute(AsConnectionInvalidatorIf.class.getSimpleName(), tInvalidator);
        }
        catch (IllegalArgumentException e) {
            // Bean not configured, ignore
        }

        return tConnection;
    }

    @Override
    public AsConnectionIf getAsConnection(String pSessionId) {
    	return mConnectionsMap.get(pSessionId);
    }
    
    @Override
    public void invalidateAsConnection(AsConnectionIf pConnection) {
        AsConnectionIf tConnection = mConnectionsMap.remove(pConnection.getSessionId());
        if (tConnection != null) {
            tConnection.invalidate();

            AsClientSession tSession = new AsClientSession();
            tSession.sessionId = tConnection.getSessionId();
            tSession.deleted = true;
            As.getBdxHandler().broadcast(tSession);
        }
    }

    @Override
    public AsClientSession getClientSession(String pSessionId) {
        AsListIf<AsClientSession> tList =
                ((AsGlobalDataSources) getGlobalDataSources()).getDataSource(AsClientSession.class);
        AsClientSession tSession = tList.get(pSessionId);
        if (tSession == null) {
            return new AsClientSession(pSessionId);
        }
        return new AsClientSession(tSession);
    }

    @Override
    public <T> AsGetMethodIf<T> getPreRegisteredGetter(Class<T> pClass, String pAttributeName) {
        return mGetters.get(pClass, pAttributeName);
    }

    @Override
    public AsGetMethodIf<?>[] getPreRegisteredGetters(Class<?> pClass) {
        return mGetters.get(pClass);
    }

    /**
     * Registration of all configured value getters. These getters will be used instead of the standard
     * getters which generally use reflection to retrieve the value. A pre-registered getter is typically
     * used to emulate attributes that do not exist in the object itself, but you wish to display it where
     * other data from the object is displayed.
     * @throws AsInitializationException
     */
    protected void registerConfiguredGetters() throws AsInitializationException {
        // TODO move to meta data component
        AsConfigXmlParserIf tXmlConf = As.getConfigXmlParser();
        for (CwfDataIf tGetter : tXmlConf.getAsGetMethods()) {
            if (tGetter.getProperty(ATTR_TAG_NAME).equals("AsGetMethod")) {
                String tType = tGetter.getProperty(ATTR_TYPE);
                String tName = tGetter.getProperty(ATTR_NAME);
                String tExpression = tGetter.getProperty(ATTR_EXPRESSION);
                if (tExpression != null) {
                    registerGetter(tName, tType, tExpression);
                }
                else {
                    registerGetter(tName, tType);
                }
            }
            else if (tGetter.getProperty(ATTR_TAG_NAME).equals("AsGetMethodSource")) {
                String tSourceType = tGetter.getProperty(ATTR_SOURCE);
                String tSourceField = tGetter.getProperty(ATTR_FIELD);
                String tGetterName = tGetter.getProperty(ATTR_NAME);
                for (CwfDataIf tData : tGetter.getObjectList("AsGetMethod")) {
                    registerGetter(tSourceType, tSourceField, tGetterName, tData);
                }
            }
        }
    }

    protected <T> void registerGetter(String pName, String pType, String pExpression) throws AsInitializationException {
        Class<T> tSourceClass = As.getType(pType);
        AsGetMethodIf<T> tMethod = new AsGetMethodFromExpression<T>(tSourceClass, pName, pExpression);
        registerGetter(tMethod);
    }

    protected <S, T> void registerGetter(String pSourceType, String pSourceField, String pGetterName, CwfDataIf pData)
            throws AsInitializationException {

        Class<S> tSourceClass = As.getType(pSourceType);
        Class<T> tItemClass = As.getType(pData.getProperty(ATTR_TYPE));
        String tItemRefField = pData.getProperty(ATTR_FIELD);
        String tGetterName = pData.getProperty(ATTR_NAME);
        if (tGetterName == null) {
            tGetterName = pGetterName;
        }
        AsListGetMethod<T, S> tMethod = new AsListGetMethod<T, S>(
                tSourceClass, pSourceField, tGetterName, tItemClass, tItemRefField);
        registerGetter(tMethod);
    }

    protected void registerGetter(String pGetterName, String pGetterClass) throws AsInitializationException {
        try {
            Class<?> tClass = Class.forName(pGetterClass);
            if (AsGetMethodFactoryIf.class.isAssignableFrom(tClass)) {
                AsGetMethodFactoryIf tFactory = (AsGetMethodFactoryIf) tClass.newInstance();
                for (AsGetMethodIf<?> tMethod : tFactory.getGetMethods()) {
                    registerGetter(tMethod);
                }
            }
            else {
                AsGetMethodIf<?> tGetMethod = (AsGetMethodIf<?>) tClass.newInstance();
                if (!pGetterName.equals(tGetMethod.getAttributeName())) {
                    throw new IllegalArgumentException("Getter attribute name " +
                            tGetMethod.getAttributeName() + " does not match its configured name " + pGetterName);
                }
                registerGetter(tGetMethod);
                // All ok, register the instance
            }
        }
        catch (ClassNotFoundException e) {
            throw new AsInitializationException("Class for getter with name " + pGetterName + " not found", e);
        }
        catch (Exception e) {
            throw new AsInitializationException("Could not instantiate getter with name " + pGetterName, e);
        }
    }

    /**
     * Register a custom get method
     * @param pGetMethod The getter
     * @throws AsInitializationException
     */
    protected <T> void registerGetter(AsGetMethodIf<T> pGetMethod) throws AsInitializationException {
        mGetters.put(pGetMethod);
    }

    @Override
    public void reloadConfiguration() {
        try {
            mGetters.clear();
            registerConfiguredGetters();
        }
        catch (Exception e) {
            As.getLogger().logThrowable("Exception during configuration reload", e);
        }
    }

    @Override
    public void systemExit(String pString, Throwable pE) {
        As.getLogger().logThrowable(pString, pE);
        System.exit(1);
    }

    @Override
    public String now() {
        synchronized (mFormat) {
            return mFormat.format(new Date());
        }
    }

}
