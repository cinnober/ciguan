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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cinnober.ciguan.AsBeanFactoryIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.impl.AsBeanConfiguration.Entry;

/**
 * Implementation of the application server bean factory
 * @author jorgen.ekroth, Cinnober Financial Technology
 */
public class AsBeanFactoryImpl implements AsBeanFactoryIf {

    private AsBeanConfiguration mBeanConfiguration = new AsBeanConfiguration();
    private final Map<Class<?>, Object> mSingletonInstances = new HashMap<Class<?>, Object>();


    public AsBeanFactoryImpl() {
        parse(As.getConfigXmlParser().getConfigurationDocument());
    }

    protected void parse(Node pNode) {
        try {
            NodeList tNodeList = XPathAPI.selectNodeList(pNode, "//AsBeanFactory/bean");
            for (int i = 0; i < tNodeList.getLength(); i++) {
                Node tNode = tNodeList.item(i);
                CwfDataIf tTree = AsUtil.parse((Element) tNode);

                String tInterface = tTree.getProperty("interface");
                String tClass = tTree.getProperty("class");
                boolean tSingleton = tTree.getBooleanProperty("singleton") == Boolean.TRUE;
                boolean tParameters = tTree.getBooleanProperty("parameters") == Boolean.TRUE;

                Class<?> tInterfaceClass = Class.forName(tInterface);
                Class<?> tImplementationClass = Class.forName(tClass);

                mBeanConfiguration.addConfiguration(tInterfaceClass, tImplementationClass, tSingleton, tParameters);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Error selecting bean source node", e);
        }
    }

    @Override
    public <T> T create(Class<T> pInterfaceClass, String... pParameters) {

        Entry<T> tConfig = mBeanConfiguration.getConfiguration(pInterfaceClass);
        if ((tConfig == null)) {
            throw new IllegalArgumentException("Attempt to create instance of unknown interface " +
                pInterfaceClass.getSimpleName());
        }

        if (tConfig.isSingleton()) {
            @SuppressWarnings("unchecked")
            T tBean = (T) mSingletonInstances.get(pInterfaceClass);
            if (tBean == null) {
                tBean = createBean(tConfig, pParameters);
                mSingletonInstances.put(pInterfaceClass, tBean);
            }
            return tBean;
        }

        return createBean(tConfig, pParameters);
    }

    /**
     * @param pConfig
     * @param pParameters
     * @return
     */
    protected <T> T createBean(Entry<T> pConfig, String... pParameters) {
        Constructor<? extends T> tConstructor = null;
        T tInstance = null;
        try {
            if (!pConfig.wantsParameters()) {
                tConstructor = pConfig.getImplementationClass().getConstructor();
                tInstance = tConstructor.newInstance();
            }
            else {
                if (pParameters.length == 0) {
                    throw new IllegalArgumentException(
                        "The bean class requires constructor parameters, but the supplied parameter set is empty");
                }

                tConstructor = pConfig.getImplementationClass().getConstructor(String[].class);
                tInstance = tConstructor.newInstance(new Object[] {pParameters});
            }
        }
        catch (Exception e) {
            throw new RuntimeException(
                "Could not instantiate bean of type " + pConfig.getImplementationClass().getSimpleName(), e);
        }
        return tInstance;
    }

}
