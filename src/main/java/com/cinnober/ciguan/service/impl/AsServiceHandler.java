/*
 * 
 *
 * 
 * 
 *
 * This software is the confidential and proprietary information of
 * Cinnober Financial Technology AB, Stockholm, Sweden. You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Cinnober.
 *
 * Cinnober makes no representations or warranties about the suitability
 * of the software, either expressed or implied, including, but not limited
 * to, the implied warranties of merchantibility, fitness for a particular
 * purpose, or non-infringement. Cinnober shall not be liable for any
 * damages suffered by licensee as a result of using, modifying, or
 * distributing this software or its derivatives.
 */
package com.cinnober.ciguan.service.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.AsMetaDataHandlerIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.AsComponent;
import com.cinnober.ciguan.impl.AsUtil;
import com.cinnober.ciguan.service.AsServiceHandlerIf;
import com.cinnober.ciguan.service.AsServiceIf;

/**
 *
 * Service handler implementation
 *
 */
public class AsServiceHandler extends AsComponent implements AsServiceHandlerIf {

    protected Map<Class<?>, Service> mServices = new HashMap<Class<?>, Service>();
    protected Map<Class<?>, AsServiceIf> mServiceInstances = new HashMap<Class<?>, AsServiceIf>();

    @Override
    public void startComponent() throws AsInitializationException {
        parse(As.getConfigXmlParser().getConfigurationDocument());
    }

    /**
     * Configuration parse entry
     * @param pNode the node to parse from
     * @throws AsInitializationException
     */
    protected void parse(Element pNode) throws AsInitializationException {
        parseServices(pNode);
    }

    /**
     * Parses the service configuration
     * @param pNode the node to parse from
     * @throws AsInitializationException
     */
    protected void parseServices(Element pNode) throws AsInitializationException {
        try {
            NodeList tNodeList = XPathAPI.selectNodeList(pNode, "//AsServices/service");
            for (int i = 0; i < tNodeList.getLength(); i++) {
                Node tNode = tNodeList.item(i);
                CwfDataIf tTree = AsUtil.parse((Element) tNode);
                String tServiceClass = tTree.getProperty("serviceClass");
                String tRequestClass = tTree.getProperty("requestClass");
                createAndRegisterService(tRequestClass, tServiceClass);
            }
        }
        catch (TransformerException e) {
            throw new RuntimeException("Error selecting service source node", e);
        }
    }

    /**
     * Attempt to create and register a service for the given request and implementation
     * @param pRequestClass
     * @param pServiceClass
     * @throws AsInitializationException
     */
    protected void createAndRegisterService(String pRequestClass, String pServiceClass)
        throws AsInitializationException {
        // Register this instance
        new Service(pRequestClass, pServiceClass);
    }

    @Override
    public Object service(AsConnectionIf pConnection, Object pRequest) throws AsServiceInvocationException {
        Service tService = mServices.get(pRequest.getClass());
        if (tService != null) {
            return tService.service(pConnection, pRequest);
        }
        return null;
    }

    /**
     *
     * Service wrapper
     *
     */
    protected class Service {

        private AsServiceIf mService;
        private Method mMethod;

        public Service(String pRequestClass, String pServiceClass) throws AsInitializationException {
            // Look up the request type
            Class<?> tRequestType = AsMetaDataHandlerIf.SINGLETON.get().getType(pRequestClass);
            if (tRequestType == null) {
                throw new AsInitializationException("Service request type " + pRequestClass + " not found");
            }
            // Check that the request type is not already registered
            Service tService = mServices.get(tRequestType);
            if (tService != null) {
                throw new AsInitializationException("Service request type " + pRequestClass + " already registered");
            }
            // Create and register the service
            try {
                // Validate the service implementation type
                Class<?> tServiceType = Class.forName(pServiceClass);
                if (!AsServiceIf.class.isAssignableFrom(tServiceType)) {
                    throw new AsInitializationException("Service implementation is not a valid service");
                }
                // Check for service method
                mMethod = tServiceType.getMethod("service", AsConnectionIf.class, tRequestType);

                // Check if an instance already exists
                mService = mServiceInstances.get(tServiceType);
                if (mService == null) {
                    // Create and register the service
                    mService = (AsServiceIf) tServiceType.newInstance();
                    mServiceInstances.put(tServiceType, mService);
                }
                // Register this instance
                mServices.put(tRequestType, this);
            }
            catch (Exception e) {
                throw new AsInitializationException("Service initialization error", e);
            }
        }

        /**
         * Execute the contained service
         * @param pConnection the application server connection
         * @param pRequest the request to process
         * @return the service response
         * @throws AsServiceInvocationException
         */
        public Object service(AsConnectionIf pConnection, Object pRequest) throws AsServiceInvocationException {
            try {
                return mMethod.invoke(mService, pConnection, pRequest);
            }
            catch (Exception e) {
                throw new AsServiceInvocationException(
                    "Exception during execution of service " + mService.getClass().getName() +
                    " and parameter type " + pRequest.getClass().getName(), e);
            }
        }

    }

}
