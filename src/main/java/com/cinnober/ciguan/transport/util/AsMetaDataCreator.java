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
package com.cinnober.ciguan.transport.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cinnober.ciguan.AsMetaDataHandlerIf;
import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.data.AsMetaObject;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.CwfBusinessTypes;
import com.cinnober.ciguan.meta.AsMetaDataHandler;
import com.cinnober.ciguan.transport.AsMetaDataCreatorIf;
import com.cinnober.ciguan.transport.AsSuppressionDictionaryIf;
import com.cinnober.ciguan.transport.impl.AsRequestTransformerConfiguration;
import com.cinnober.ciguan.xml.impl.AsDefMetaData;
import com.cinnober.ciguan.xml.impl.AsDefSuppressAttribute;
import com.cinnober.ciguan.xml.impl.AsDefSuppressClass;
import com.cinnober.ciguan.xml.impl.AsDefSuppressClassAttribute;

/**
 * Class responsible for creating serialized metadata which describes the server side objects.
 */
public class AsMetaDataCreator implements AsMetaDataCreatorIf {

    /** The dictionary. */
    private final AsSuppressionDictionaryIf mDictionary;
    
    /** The processed classes. */
    private Map<String, String> mProcessedClasses = new LinkedHashMap<String, String>();
    
    /** The business type mappings. */
    private Map<String, Map<String, String>> mBusinessTypeMappings = new HashMap<String, Map<String, String>>();
    
    /** The meta data. */
    private AsDefMetaData mMetaData;
    
    /**
     * Default instance.
     */
    public AsMetaDataCreator() {
        this(As.getBeanFactory().create(AsSuppressionDictionaryIf.class));
        initializeSuppressionDictionary();
        initializeBusinessTypeMappings();
    }
    
    /**
     * Instance with a given dictionary.
     *
     * @param pDictionary the dictionary
     */
    public AsMetaDataCreator(AsSuppressionDictionaryIf pDictionary) {
        mDictionary = pDictionary;
    }
    
    /**
     * Create metadata for the given class and its child classes.
     *
     * @param pMetaData the meta data
     */
    @Override
    public void create(AsDefMetaData pMetaData) {
        mMetaData = pMetaData;
        create(As.getType(pMetaData.getClassName()));
    }
    
    /**
     * Creates metadata for this class.
     *
     * @param pClass the class
     */
    protected void create(Class<?> pClass) {
        if (!mProcessedClasses.containsKey(As.getTypeName(pClass))) {
            String tSerialized = serializeObject(null, pClass, false);
            mProcessedClasses.put(getMetadataClassName(pClass), tSerialized);
            AsMetaObject<?> tMeta = ((AsMetaDataHandler) As.getMetaDataHandler()).getMetaData(pClass);
            if (!tMeta.cwfSerializedVersion0().equals(getMetadataClassName(pClass) + tSerialized)) {
                System.out.println(getMetadataClassName(pClass) + tSerialized);
                System.out.println(tMeta.cwfSerializedVersion0());
            }
        }
    }
    
    @Override
    public void finalizeMetaData() {
        for (Map.Entry<String, String> tEntry : mProcessedClasses.entrySet()) {
            expand(tEntry.getKey(), tEntry.getValue());
        }
    }
    
    @Override
    public Map<String, String> getMetaData() {
        return mProcessedClasses;
    }
    
    /**
     * Expand the metadata for this class.
     *
     * @param pClassName the class name
     * @param pMetaData the meta data
     */
    protected void expand(String pClassName, String pMetaData) {
        int tPos = pMetaData.indexOf('@'); 
        if (tPos < 0) {
            return;
        }
        
        int tStartPos = tPos;
        StringBuilder tExpansionClassName = new StringBuilder();
        char tChar = pMetaData.charAt(++tPos); 
        while (Character.isJavaIdentifierPart(tChar) || tChar == AsMetaDataHandler.NS_SEP.charAt(0)) {
            tExpansionClassName.append(tChar);
            tChar = pMetaData.charAt(++tPos);
        }
        
        String tExpanded = null;
        if (pMetaData.charAt(tPos) == '[') {
            // Array replacement
            tExpanded =
                pMetaData.substring(0, tStartPos) +
                tExpansionClassName + "[]" + mProcessedClasses.get(tExpansionClassName.toString()) +
                pMetaData.substring(tPos + 2);
        }
        else {
            // Plain replacement
            tExpanded =
                pMetaData.substring(0, tStartPos) +
                tExpansionClassName + mProcessedClasses.get(tExpansionClassName.toString()) +
                pMetaData.substring(tPos);
        }
        
        // Save the expanded string and end with a recursive call
        mProcessedClasses.put(pClassName, tExpanded);
        expand(pClassName, tExpanded);
    }
    
    /**
     * Initialize the suppression dictionary
     * (Only use suppressed items where no direction is set).
     */
    protected void initializeSuppressionDictionary() {
        
        AsRequestTransformerConfiguration tConfig =
            As.getTransportConfiguration().getRequestTransformerConfiguration();
        
        // Attributes
        for (AsDefSuppressAttribute tAttr : tConfig.getSuppressAttributes()) {
            if (isEmpty(tAttr.getDirection())) {
                mDictionary.addAttribute(tAttr.getAttributeName());
            }
        }
        
        // Classes
        for (AsDefSuppressClass tClass : tConfig.getSuppressClasses()) {
            if (isEmpty(tClass.getDirection())) {
                mDictionary.addClass(tClass.getClassName());
            }
        }
        
        // Class attributes
        for (AsDefSuppressClassAttribute tClassAttribute : tConfig.getSuppressClassAttributes()) {
            if (isEmpty(tClassAttribute.getDirection())) {
                mDictionary.addClassAttribute(tClassAttribute.getClassName(), tClassAttribute.getAttributeName());
            }
        }
    }
    
    /**
     * Checks if is empty.
     *
     * @param pString the string
     * @return {@code true}, if is empty
     */
    private boolean isEmpty(String pString) {
        return pString == null || pString.isEmpty();
    }
        
    /**
     * Initialize business type mappings.
     */
    protected void initializeBusinessTypeMappings() {
        for (AsDefMetaData tMetaData : As.getMetaDataHandler().getMetaDataClasses()) {
            if (!tMetaData.getAttributes().isEmpty()) {
                Map<String, String> tAttributes = mBusinessTypeMappings.get(tMetaData.getClassName());
                if (tAttributes == null) {
                    tAttributes = new HashMap<String, String>();
                    mBusinessTypeMappings.put(tMetaData.getClassName(), tAttributes);
                }
                tAttributes.putAll(tMetaData.getAttributes());
            }
        }
    }
    
    /**
     * Get the class name for use on the client.
     *
     * @param pClass the class
     * @return the metadata class name
     */
    private String getMetadataClassName(Class<?> pClass) {
        // If the class is the top level class and it has an explicit request name defined,
        // combine the two names
        if (As.getTypeName(pClass).equals(mMetaData.getClassName()) &&
            mMetaData.getServerRequestName() != null) {
            return As.getTypeName(pClass) + "/" + mMetaData.getServerRequestName(); 
        }
        return As.getTypeName(pClass);
    }
    
    /**
     * Serialize an arbitrary object recursively.
     *
     * @param pName the name
     * @param pClass the class
     * @param pIncludeClassName the include class name
     * @return the string
     */
    private String serializeObject(String pName, Class<?> pClass, boolean pIncludeClassName) {
        
        if (mDictionary.isObjectSuppressed(pName, pClass)) {
            return "";
        }
        StringBuilder tBuilder = new StringBuilder();
        if (pName != null) {
            tBuilder.append(pName).append("=");
        }
        int tFieldIndex = 0;
        tBuilder.append((pIncludeClassName ? As.getTypeName(pClass) : "")).append("{");
        for (Field tField : pClass.getFields()) {
            if (!mDictionary.isFieldSuppressed(tField)) {
                if (tFieldIndex > 0) {
                    tBuilder.append(",");
                }
                switch (mDictionary.getFieldCategory(tField.getType())) {
                    case ARRAY:
                        switch (mDictionary.getFieldCategory(tField.getType().getComponentType())) {
                            case SIMPLE:
                                tBuilder.append(tField.getName()).append("=").append(getMetaDataType(tField));
                                break;
                            case COMPLEX:
                                tBuilder.append(tField.getName()).append("=@").append(getMetaDataType(tField));
                                create(tField.getType().getComponentType());
                            default:;
                        }
                        break;
                    case SIMPLE:
                        tBuilder.append(tField.getName()).append("=").append(getMetaDataType(tField));
                        break;
                    case COMPLEX:
                        tBuilder.append(tField.getName()).append("=@").append(getMetaDataType(tField));
                        create(tField.getType());
                        break;
                    default:;
                }
                tFieldIndex++;
            }
        }
        AsGetMethodIf<?>[] tGetters = As.getPreRegisteredGetters(pClass);
        if (tGetters != null) {
            for (AsGetMethodIf<?> tGetter : tGetters) {
                if (tFieldIndex > 0) {
                    tBuilder.append(",");
                }
                CwfBusinessTypeIf tBusinessType = tGetter.getBusinessType();
                String tSubtype = tGetter.getBusinessSubtype();
                if (tBusinessType == CwfBusinessTypes.Object && tSubtype != null && !tSubtype.isEmpty()) {
                    tBuilder.append(tGetter.getAttributeName()).append("=@").append(tSubtype);
                }
                else if ((tBusinessType == CwfBusinessTypes.Constant ||
                          tBusinessType == CwfBusinessTypes.Reference) &&
                          tSubtype != null && !tSubtype.isEmpty()) {
                    tBuilder.append(tGetter.getAttributeName()).append("=").append(tBusinessType)
                        .append(":").append(tSubtype);
                }
                else {
                    tBuilder.append(tGetter.getAttributeName()).append("=").append(tBusinessType);
                }
                tFieldIndex++;
            }
        }
        tBuilder.append("}");
        return tBuilder.toString();
    }

    /**
     * Get the type name to use in metadata.
     *
     * @param pField the field
     * @return the meta data type
     */
    private String getMetaDataType(Field pField) {
        
        // 1. Check for explicit mapping
        Map<String, String> tAttributes = mBusinessTypeMappings.get(pField.getDeclaringClass().getName());
        if (tAttributes == null) {
            tAttributes = mBusinessTypeMappings.get(As.getTypeName(pField.getDeclaringClass()));
        }
        if (tAttributes != null) {
            String tMappedType = tAttributes.get(pField.getName());
            if (tMappedType != null) {
                if (tMappedType.startsWith(CwfBusinessTypes.Constant.name())) {
                    String tConstantGroupName = tMappedType.substring(tMappedType.indexOf(":") + 1);
                    Class<?> tConstantGroupClass = AsMetaDataHandlerIf.SINGLETON.get().getType(tConstantGroupName);
                    As.getGlobalDataSources().createConstantGroupList(tConstantGroupClass);
                }
                return tMappedType;
            }
        }
        
        // 2. Check for reference, business type or constant group annotation
        Class<?> tReferencedType = As.getMetaDataHandler().getReferencedType(pField);
        if (tReferencedType != null) {
            return "Reference:" + As.getTypeName(tReferencedType);
        }
        Class<?> tConstantType = As.getMetaDataHandler().getConstantType(pField);
        if (tConstantType != null) {
            As.getGlobalDataSources().createConstantGroupList(tConstantType);
            return "Constant:" + As.getTypeName(tConstantType);
        }
        CwfBusinessTypeIf tBusinessType = As.getMetaDataHandler().getBusinessType(pField);
        if (tBusinessType != null) {
            return tBusinessType.name();
        }
        
        // 3. Check for enum
        if (Enum.class.isAssignableFrom(pField.getType())) {
            As.getGlobalDataSources().createEnumList(pField.getType());
            return "Constant:" + As.getTypeName(pField.getType());
        }
        
        
        // 4. Use own type
        return As.getTypeName(pField.getType());
    }
    
}
