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
package com.cinnober.ciguan.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.AsMetaDataHandlerIf;
import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.annotation.CwfBusinessType;
import com.cinnober.ciguan.annotation.CwfDisplayName;
import com.cinnober.ciguan.annotation.CwfIdField;
import com.cinnober.ciguan.annotation.CwfReference;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.data.AsDataSourceDef;
import com.cinnober.ciguan.data.AsMetaField;
import com.cinnober.ciguan.data.AsMetaObject;
import com.cinnober.ciguan.data.AsSearchPackage;
import com.cinnober.ciguan.data.AsViewDefinition;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.AsComponent;
import com.cinnober.ciguan.impl.CwfBusinessTypes;
import com.cinnober.ciguan.xml.impl.AsDefMetaData;

/**
 * The Class AsMetaDataHandler.
 */

public class AsMetaDataHandler extends AsComponent implements AsMetaDataHandlerIf, MvcModelAttributesIf {

    /** The separator between a namespace prefix and a class name */
    public static final String NS_SEP = ":";

    /** The object type to class. */
    protected Map<String, Class<?>> mObjectTypeToClass = new HashMap<String, Class<?>>();

    /** The meta objects. */
    protected Map<Class<?>, AsMetaObject<?>> mMetaObjects = new HashMap<Class<?>, AsMetaObject<?>>();

    protected Map<Class<?>, String> mTypeNames = new HashMap<Class<?>, String>();

    /** The search packages. */
    private final Map<String, AsSearchPackage> mSearchPackages = new HashMap<String, AsSearchPackage>();

    /** The meta data classes. */
    private final Map<String, AsDefMetaData> mMetaDataClasses = new HashMap<String, AsDefMetaData>();

    /** The suppressed classes. */
    private final Set<String> mSuppressedClasses = new HashSet<String>();

    /** The suppressed attributes. */
    private final Set<String> mSuppressedAttributes = new HashSet<String>();

    @Override
    public void startComponent() throws AsInitializationException {
        parse(As.getConfigXmlParser().getConfigurationDocument());
    }

    @Override
    public void synchronizeExternalData() {

        Set<String> tMetaSet = new HashSet<String>();
        // Populate from view models
        for (AsViewDefinition tViewDefinition : As.getConfigXmlParser().getViewDefinitions().values()) {
            String tModelClass = tViewDefinition.getModel();
            tMetaSet.add(tModelClass);
            // Add BLOB models too
            for (CwfDataIf tBlobDef : tViewDefinition.getValues().getObjectList(TAG_BLOB)) {
                tMetaSet.add(tBlobDef.getProperty(ATTR_MODEL));
            }
            // Add context object types
            for (CwfDataIf tContextDef : tViewDefinition.getValues().getObjectList(TAG_CONTEXT)) {
                tMetaSet.add(tContextDef.getProperty(ATTR_TYPE));
            }
        }
        // Populate all data source item types (except for map based items)
        for (AsDataSourceDef<?> tDataSourceDef : AsDataSourceDef.getAll()) {
            tMetaSet.add(tDataSourceDef.getType());
        }
        tMetaSet.remove(null);
        tMetaSet.remove("");
        for (String tType : tMetaSet) {
            getMetaData(tType);
        }
        for (AsMetaObject<?> tMeta : new ArrayList<AsMetaObject<?>>(mMetaObjects.values())) {
            As.getBdxHandler().broadcast(tMeta);
            for (AsMetaField tField : tMeta.getFields()) {
                As.getBdxHandler().broadcast(tField);
            }
        }
    }

    /**
     * Parses the specified node element.
     *
     * @param pNode the node
     * @throws AsInitializationException the as initialization exception
     */
    protected void parse(Element pNode) throws AsInitializationException {

        String tXpathPackage = "//AsMeta/SearchPackage";
        String tXpathMetaData = "//AsMeta/MetaData";
        String tXpathSupressClass = "//AsMeta/SuppressClass[not(@direction)]";
        String tXpathSupressedAttr = "//AsMeta/SuppressAttribute[not(@direction)]";
        String tXpathSupressedClassAttr = "//AsMeta/SuppressClassAttribute[not(@direction)]";
        try {

            // find search packages
            NodeList tNodeList = XPathAPI.selectNodeList(pNode, tXpathPackage);
            for (int i = 0; i < tNodeList.getLength(); i++) {
                String tSearchPackage = ((Element) tNodeList.item(i)).getAttribute("packageName");
                String tNamespace = ((Element) tNodeList.item(i)).getAttribute("namespace");
                mSearchPackages.put(tSearchPackage, new AsSearchPackage(tSearchPackage, tNamespace));
            }

            // find classes to make meta data for
            tNodeList = XPathAPI.selectNodeList(pNode, tXpathMetaData);
            for (int i = 0; i < tNodeList.getLength(); i++) {
                Element tElement = (Element) tNodeList.item(i);
                String tClassName = tElement.getAttribute("className");
                AsDefMetaData tDef = null;
                AsDefMetaData tExistingMeta = mMetaDataClasses.get(tClassName);
                if (tExistingMeta == null) {
                    // New definition
                    tDef = new AsDefMetaData(tElement);
                }
                else {
                    // Merge with old definition
                    tDef = new AsDefMetaData(tElement, tExistingMeta);
                }
                mMetaDataClasses.put(tDef.getClassName(), tDef);
            }

            tNodeList = XPathAPI.selectNodeList(pNode, tXpathSupressClass);
            for (int i = 0; i < tNodeList.getLength(); i++) {
                mSuppressedClasses.add(((Element) tNodeList.item(i)).getAttribute("className"));
            }
            tNodeList = XPathAPI.selectNodeList(pNode, tXpathSupressedAttr);
            for (int i = 0; i < tNodeList.getLength(); i++) {
                mSuppressedAttributes.add(((Element) tNodeList.item(i)).getAttribute("attributeName"));
            }
            tNodeList = XPathAPI.selectNodeList(pNode, tXpathSupressedClassAttr);
            for (int i = 0; i < tNodeList.getLength(); i++) {
                String tClass = ((Element) tNodeList.item(i)).getAttribute("className");
                String tAttr = ((Element) tNodeList.item(i)).getAttribute("attributeName");
                mSuppressedAttributes.add(tClass + "." + tAttr);
            }

        }
        catch (TransformerException e) {
            throw new AsInitializationException("Error selecting metadata source node", e);
        }
    }

    @Override
    public List<AsDefMetaData> getMetaDataClasses() {
        return new ArrayList<AsDefMetaData>(mMetaDataClasses.values());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Class<T> getType(String pObjectType) {
        Class<T> tClass = (Class<T>) mObjectTypeToClass.get(pObjectType);
        // test with contains key, they value can be null
        if (mObjectTypeToClass.containsKey(pObjectType)) {
            return tClass;
        }

        String tObjectType = pObjectType;
        String tNamespace = "";
        int tNamespaceIx = pObjectType.indexOf(NS_SEP);
        if (tNamespaceIx != -1) {
            tNamespace = pObjectType.substring(0, tNamespaceIx);
            tObjectType = pObjectType.substring(tNamespaceIx + 1);
        }

        if (tObjectType.indexOf('.') != -1) {
            try {
                return (Class<T>) Class.forName(tObjectType);
            }
            catch (ClassNotFoundException e) {
                return null;
            }
        }
        if (tClass != null || (tNamespaceIx == -1 && mObjectTypeToClass.containsKey(tObjectType))) {
            // test with contains key, they value can be null, then no need to do this again
            return tClass;
        }
        for (String tPackage : mSearchPackages.keySet()) {
            try {
                tClass = (Class<T>) Class.forName(tPackage + "." + tObjectType);
                AsSearchPackage tSearchPackage = mSearchPackages.get(tPackage);
                if ((tNamespaceIx == -1 && tSearchPackage.namespace.isEmpty()) ||
                    (tNamespaceIx != -1 && tSearchPackage.namespace.equals(tNamespace))) {
                    break;
                }
            }
            catch (Exception e) {
                // No action, proceed with next package
            }
        }
        synchronized (mObjectTypeToClass) {
            mObjectTypeToClass.put(pObjectType, tClass);
        }
        return tClass;
    }

    @Override
    public <T> String getTypeName(Class<T> pClass) {
        String tClassName = mTypeNames.get(pClass);
        if (tClassName == null) {
            String tPackage = pClass.getPackage() != null ? pClass.getPackage().getName() : null;
            if (pClass.isArray()) {
                Class<?> tClass = pClass.getComponentType();
                tPackage = tClass.getPackage() != null ? tClass.getPackage().getName() : null;
            }

            // Try search packages for a suitable namespace
            AsSearchPackage tSearchPackage = tPackage != null ? mSearchPackages.get(tPackage) : null;
            if (tSearchPackage != null) {
                // Package found, use namespace if available
                tClassName = (tSearchPackage.namespace.isEmpty() ?
                    "" : tSearchPackage.namespace + NS_SEP) + pClass.getSimpleName();
            }
            else {
                // No matching package
                tClassName = pClass.getSimpleName();
            }
            synchronized (mTypeNames) {
                mTypeNames.put(pClass, tClassName);
            }
        }
        return tClassName;
    }

    @Override
    public CwfBusinessTypeIf getBusinessType(Field pField) {
        AsDefMetaData tDef = mMetaDataClasses.get(As.getTypeName(pField.getDeclaringClass()));
        if (tDef != null) {
            String tBusType = tDef.getAttributes().get(pField.getName());
            if (tBusType != null) {
                return CwfBusinessTypes.get(tBusType);
            }
        }
        for (Annotation tAnnotation : pField.getAnnotations()) {
            CwfBusinessType tType = tAnnotation.annotationType().getAnnotation(CwfBusinessType.class);
            if (tType != null) {
                assert tType.value() != null;
                return tType.value();
            }
        }
        return null;
    }

    @Override
    public String getIdField(Class<?> pItemClass) {
        for (Field tField : pItemClass.getFields()) {
            if (tField.getAnnotation(CwfIdField.class) != null) {
                return tField.getName();
            }
        }
        for (Method tMethod : pItemClass.getMethods()) {
            if (tMethod.getAnnotation(CwfIdField.class) != null) {
                assert tMethod.getName().startsWith("get");
                return tMethod.getName().substring(3, 4).toLowerCase() + tMethod.getName().substring(4);
            }
        }
        return null;
    }

    @Override
    public String getDisplayField(Class<?> pItemClass) {
        for (Field tField : pItemClass.getFields()) {
            if (tField.getAnnotation(CwfDisplayName.class) != null) {
                return tField.getName();
            }
        }
        for (Method tMethod : pItemClass.getMethods()) {
            if (tMethod.getAnnotation(CwfDisplayName.class) != null) {
                assert tMethod.getName().startsWith("get");
                return tMethod.getName().substring(3, 4).toLowerCase() + tMethod.getName().substring(4);
            }
        }
        return null;
    }

    @Override
    public Class<?> getConstantType(Field pField) {
        // Enum
        if (Enumeration.class.isAssignableFrom(pField.getType())) {
            return pField.getType();
        }
        // Constant group
        AsDefMetaData tDef = mMetaDataClasses.get(As.getTypeName(pField.getDeclaringClass()));
        if (tDef != null) {
            String tBusType = tDef.getAttributes().get(pField.getName());
            if (tBusType != null && tBusType.startsWith("Constant:")) {
                String tConstantType = tBusType.substring(9);
                return getType(tConstantType);
            }
        }
        return null;
    }

    @Override
    public Class<?> getReferencedType(Field pField) {
        CwfReference tAnnotation = pField.getAnnotation(CwfReference.class);
        return tAnnotation != null ? tAnnotation.value() : null;
    }

    @Override
    public Method getConstantGroupGetter(Class<?> pConstantClass) {
        return null;
    }

    @Override
    public boolean isFieldSuppressed(Field pField) {
        return
            Modifier.isStatic(pField.getModifiers()) ||
            Modifier.isTransient(pField.getModifiers()) ||
            Modifier.isFinal(pField.getModifiers()) ||
            mSuppressedAttributes.contains(pField.getName()) ||
            mSuppressedAttributes.contains(As.getTypeName(pField.getDeclaringClass()) + "." + pField.getName()) ||
            mSuppressedClasses.contains(As.getTypeName(pField.getType())) ||
            mSuppressedClasses.contains(As.getTypeName(pField.getDeclaringClass()));
    }

    /**
     * Gets the meta data for the specified type.
     *
     * @param <T> the generic type
     * @param pType the type
     * @return the meta data
     */
    public <T> AsMetaObject<T> getMetaData(String pType) {
        Class<T> tType = getType(pType);
        return tType == null ? null : getMetaData(tType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> AsMetaObject<T> getMetaData(Class<T> pClass) {
        AsMetaObject<?> tMeta = mMetaObjects.get(pClass);
        if (tMeta == null) {
            AsDefMetaData tDef = mMetaDataClasses.get(As.getTypeName(pClass));
            tMeta = new AsMetaObject<T>(pClass, tDef, this);
            mMetaObjects.put(pClass, tMeta);
        }
        return (AsMetaObject<T>) tMeta;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.cinnober.ciguan.data.AsMetaObject#getField(String)
     */
    @Override
    public AsMetaField getField(AsMetaObject<?> pMeta, String pColumnName) {
        int tPathPos = pColumnName.indexOf('.');
        if (tPathPos >= 0) {
            String tObjectPath = pColumnName.substring(0, tPathPos);
            AsMetaField tObject = getField(pMeta, tObjectPath);
            if (tObject != null && tObject.owner != null) {
                return getField(getType(tObject.owner), pColumnName.substring(tPathPos + 1));
            }
            return null;
        }
        if (pMeta != null) {
            return pMeta.getField(pColumnName);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.cinnober.ciguan.data.AsMetaObject#getField(String)
     */
    @Override
    public AsMetaField getField(Class<?> pClass, String pColumnName) {
        return getField(getMetaData(pClass), pColumnName);
    }

    @Override
    public Set<String> getSearchPackages() {
        return mSearchPackages.keySet();
    }

}
