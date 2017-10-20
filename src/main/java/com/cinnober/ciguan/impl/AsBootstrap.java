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

import com.cinnober.ciguan.AsAuthorizationHandlerIf;
import com.cinnober.ciguan.AsBdxHandlerIf;
import com.cinnober.ciguan.AsBeanFactoryIf;
import com.cinnober.ciguan.AsBootstrapIf;
import com.cinnober.ciguan.AsCacheIf;
import com.cinnober.ciguan.AsConfigXmlParserIf;
import com.cinnober.ciguan.AsDictionaryHandlerIf;
import com.cinnober.ciguan.AsFormatIf;
import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.AsMetaDataFactoryIf;
import com.cinnober.ciguan.AsMetaDataHandlerIf;
import com.cinnober.ciguan.AsRootIf;
import com.cinnober.ciguan.AsTransportConfigurationIf;
import com.cinnober.ciguan.AsUserPropertyPersisterIf;
import com.cinnober.ciguan.CwfVersionIf;
import com.cinnober.ciguan.data.AsCwfDataFactoryImpl;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.scheduler.AsSchedulerIf;
import com.cinnober.ciguan.service.AsServiceHandlerIf;
import com.cinnober.ciguan.xml.impl.AsConfigXmlParser;

/**
 *
 * Standard AS bootstrap
 */
public class AsBootstrap implements AsBootstrapIf {

	public static final String CWF_LOAD_MODULE_PROPERTY = "cwf.loadModule";
	public static final String CWF_BOOTSTRAP_PROPERTY = "cwf.bootstrap";

	public boolean mRunning;

	private String mContextPath;

	@Override
	public void start(String pStartModuleName, String pContextPath) throws AsInitializationException {
		if (mRunning) {
			return;
		}
		mRunning = true;

		As.getLogger().log(CwfVersionIf.Singleton.get().toString());
		mContextPath = pContextPath;
		String tModule = System.getProperty(CWF_LOAD_MODULE_PROPERTY, pStartModuleName);

		// Create all default components
		createDefaultComponents(tModule);

		// Create all configured components
		createConfiguredComponents();

		// components created
		for (AsComponent tComponent : AsComponent.getAllComponents()) {
			As.getLogger().log("ciguan start component: " + tComponent.getClass().getSimpleName());
			tComponent.doStartComponent();
		}
	}

	@Override
	public String getContextPath() {
		return mContextPath;
	}

	/**
	 * Create the configuration XML parser, must be done first.
	 * Override if you want to extend the default parser.
	 * @param pModule the name of the module to parse
	 * @return a configuration XML parser instance
	 * @throws AsInitializationException when the parser fails to parse the configuration
	 */
	protected AsConfigXmlParserIf createConfigXmlParser(String pModule) throws AsInitializationException {
		return new AsConfigXmlParser(pModule);
	}

	/**
	 * Create the bean factory, must be done as second step.
	 * Override if you want to extend the default implementation.
	 * @return a bean factory instance
	 */
	protected AsBeanFactoryIf createBeanFactory() {
		return new AsBeanFactoryImpl();
	}

	/**
	 * Create the default components.
	 *
	 * @param pModule the name of the boot module
	 * @throws AsInitializationException
	 */
	protected void createDefaultComponents(String pModule) throws AsInitializationException {
		CwfDataFactory.set(new AsCwfDataFactoryImpl());

		// Required components
		AsConfigXmlParserIf.SINGLETON.set(createConfigXmlParser(pModule));
		AsBeanFactoryIf.Singleton.set(createBeanFactory());
		//		AsMetricsIf.Singleton.create();
		AsLoggerIf.Singleton.create();
		AsDictionaryHandlerIf.SINGLETON.create();
		AsMetaDataHandlerIf.SINGLETON.create();
		AsMetaDataFactoryIf.SINGLETON.create();
		AsTransportConfigurationIf.SINGLETON.create();
		AsRootIf.Singleton.create();
		AsBdxHandlerIf.Singleton.create();
		AsCacheIf.SINGLETON.create();
		AsAuthorizationHandlerIf.Singleton.create();
		AsUserPropertyPersisterIf.Singleton.create();
		AsFormatIf.Singleton.create();
		AsSchedulerIf.Singleton.create();
		AsServiceHandlerIf.Singleton.create();
		// AsRestConfigurationIf.SINGLETON.create();
	}

	/**
	 * Create all configured components.
	 */
	private final void createConfiguredComponents() {
		for (String tComponentClass : As.getConfigXmlParser().getConfiguredComponents()) {
			try {
				Class<?> tClass = Class.forName(tComponentClass);
				tClass.newInstance();
			}
			catch (Exception e) {
				AsLoggerIf.Singleton.get().logThrowable("Failed to create component of type " + tComponentClass, e);
			}
		}
	}

	@Override
	public void allComponentsStarted() throws AsInitializationException {
		for (AsComponent tComponent : AsComponent.getAllComponents()) {
			System.out.println("AS all components started(): " + tComponent.getClass().getSimpleName());
			tComponent.doAllComponentsStarted();
		}
	}

	@Override
	public void synchronizeExternalData() {
		for (AsComponent tComponent : AsComponent.getAllComponents()) {
			System.out.println("AS Synchronize External Data: " + tComponent.getClass().getSimpleName());
			tComponent.doSynchronizeExternalData();
		}
	}

	@Override
	public void stop() throws AsInitializationException {
		if (!mRunning) {
			return;
		}
		mRunning = false;
		for (AsComponent tComponent : AsComponent.getAllComponents()) {
			System.out.println("AS stop Component: " + tComponent.getClass().getSimpleName());
			tComponent.doStopComponent();
		}
	}

	@Override
	public void allComponentsStopped() throws AsInitializationException {
		for (AsComponent tComponent : AsComponent.getAllComponents()) {
			System.out.println("AS all components stopped(): " + tComponent.getClass().getSimpleName());
			tComponent.doAllComponentsStopped();
		}
	}

	/**
	 * Configure the bootstrap instance
	 * 1) Attempt to use the passed class name
	 * 2) Attempt to read the JVM parameter cwf.bootstrap
	 * @param pBootstrap
	 * @throws AsInitializationException
	 */
	public static void setBootstrap(String pBootstrap) throws AsInitializationException {
		String tBootstrap = pBootstrap;
		if (tBootstrap == null || tBootstrap.isEmpty()) {
			tBootstrap = System.getProperty(CWF_BOOTSTRAP_PROPERTY);
		}
		if (tBootstrap == null || tBootstrap.isEmpty()) {
			throw new AsInitializationException("No bootstrap class configured");
		}
		try {
			Class<?> tClass = Class.forName(tBootstrap);
			Object tInstance = tClass.newInstance();
			AsBootstrapIf.Singleton.set((AsBootstrapIf) tInstance);
		}
		catch (Exception e) {
			throw new AsInitializationException("Bad bootstrap class configured", e);
		}
	}

}
