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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsHandlerRegistrationIf;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.impl.MvcModelNames;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.datasource.AsDataSourceListenerIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.AsObjectTransformer;
import com.cinnober.ciguan.impl.CwfBusinessTypes;
import com.cinnober.ciguan.locale.impl.AsLocalizedString;
import com.cinnober.ciguan.transport.AsRequestTransformerIf;
import com.cinnober.ciguan.xml.impl.AsDefCopyAttribute;
import com.cinnober.ciguan.xml.impl.AsDefGenerateUniqueId;

/**
 * Standard implementation of the application server request transformer. This class handles transformations
 * between the generic CwfDataIf hashmap objects and the server side MessageIf objects. The class also handles
 * generation uf unique identifiers, and performs copying of attribute values.
 *
 * The behaviour of this class is highly configurable. See AsTransportConfig.xml for details on how to
 * configure it.
 */
public class AsRequestTransformer extends AsObjectTransformer implements AsRequestTransformerIf, MvcModelAttributesIf {

    /**
     * Outbound (client to server) transformation.
     *
     * @param pConnection the connection
     * @param pData the data
     * @return the object
     */
    @Override
    public Object transform(AsConnectionIf pConnection, CwfDataIf pData) {
        Object tCurrentMessage = createObject(pData.getProperty(ATTR_OBJECT_NAME));
        processUuidGeneration(tCurrentMessage, pData);
        processValueCopying(tCurrentMessage, pData);
        populateMessage(tCurrentMessage, pData);
        populateMessageMappings(pConnection, tCurrentMessage,
            pData.getObjectList(MvcModelNames.ViewportHandleMappingModel.name()));
        return tCurrentMessage;
    }

    /**
     * Inbound (server to client) transformation.
     *
     * @param pConnection the connection
     * @param pMessage the message
     * @return the cwf data object
     */
    @Override
    public CwfDataIf transform(AsConnectionIf pConnection, Object pMessage) {
        CwfDataIf tCurrentData = CwfDataFactory.create();
        tCurrentData.setProperty(ATTR_OBJECT_NAME, As.getTypeName(pMessage.getClass()));
        tCurrentData.setProperty(ATTR_MODEL_NAME, MvcModelNames.ServerResponse.name());
        setTapStatusMessage(pMessage);
        populateData(tCurrentData, "", "", pMessage, pConnection.getLocale());
        return tCurrentData;
    }

    /**
     * Recursively populate the server message from the supplied model data.
     *
     * @param pMessage the message
     * @param pData the data
     */
    protected void populateMessage(Object pMessage, CwfDataIf pData) {
        // Handle null properly
        if (pData == null) {
            return;
        }

        // First do the regular properties
        for (String tKey : pData.getProperties().keySet()) {
            try {
                Field tField = pMessage.getClass().getField(tKey);
                if (!mOut.isFieldSuppressed(tField)) {
                    switch (mOut.getFieldCategory(tField.getType())) {
                        case SIMPLE:
                            setField(tField, pMessage, pData.getProperty(tKey));
                            break;

                        case ARRAY:
                            setArrayField(tField, pMessage, pData, tKey);
                            break;

                        case COMPLEX:
                            AsLoggerIf.Singleton.get().log("Found an object when a simple type was expected " +
                                "for attribute " + tKey + " in type " + pMessage.getClass().getName());
                            break;

                        default:;
                    }
                }
            }
            catch (Exception e) {
                // Skip the field
            }
        }
        // Now convert the objects
        if (pData.getObjects() != null) {
            for (String tKey : pData.getObjects().keySet()) {
                try {
                    Field tField = pMessage.getClass().getField(tKey);
                    if (!mOut.isFieldSuppressed(tField)) {
                        switch (mOut.getFieldCategory(tField.getType())) {
                            case SIMPLE:
                                AsLoggerIf.Singleton.get().log("Found a simple type when an object was expected " +
                                    "for attribute " + tKey + " in type " + pMessage.getClass().getName());
                                break;

                            case COMPLEX:
                                Object tObject = createObject(As.getTypeName(tField.getType()));
                                tField.set(pMessage, tObject);
                                populateMessage(tObject, pData.getObject(tKey));
                                break;

                            default:;
                        }
                    }
                }
                catch (Exception e) {
                    // Skip the field
                }
            }
        }
        // Finally convert the object lists (arrays)
        if (pData.getObjectListMap() != null) {
            for (String tKey : pData.getObjectListMap().keySet()) {
                try {
                    Field tField = pMessage.getClass().getField(tKey);
                    if (!mOut.isFieldSuppressed(tField)) {
                        switch (mOut.getFieldCategory(tField.getType())) {
                            case ARRAY:
                                int tIndex = 0;
                                String tTypeName = As.getTypeName(tField.getType().getComponentType());
                                List<CwfDataIf> tComponents = pData.getObjectListMap().get(tKey);
                                Object[] tArray = createObjectArray(tTypeName, tComponents.size());
                                for (CwfDataIf tComponent : tComponents) {
                                    Object tMessage = createObject(tTypeName);
                                    populateMessage(tMessage, tComponent);
                                    Array.set(tArray, tIndex++, tMessage);
                                }
                                tField.set(pMessage, tArray);

                            default:;
                        }
                    }
                }
                catch (Exception e) {
                    // Skip the field
                }
            }
        }
    }

    /**
     * Populate mapped data from viewports.
     *
     * @param pConnection the AS connection
     * @param pCurrentMessage the current message
     * @param pMappings the mappings
     */
    protected void populateMessageMappings(
        AsConnectionIf pConnection, Object pCurrentMessage, List<? extends CwfDataIf> pMappings) {
        try {
            // FIXME: This code needs to cope with x.y.z paths
            for (CwfDataIf tMapping : pMappings) {
                int tHandle = ((CwfDataIf) tMapping).getIntProperty(ATTR_HANDLE);
                String tPath = tMapping.getProperty(ATTR_PATH);
                AsHandlerRegistrationIf tHandler =
                    pConnection.getDataSourceService().getHandlerRegistration(tHandle);
                AsDataSourceListenerIf<?> tListener = (AsDataSourceListenerIf<?>) tHandler;
                AsListIf<?> tList = (AsListIf<?>) tListener.getDataSource();
                Object[] tArray = createObjectArray(As.getTypeName(tList.getItemClass()), tList.size());
                int tIndex = 0;
                for (Object tItem : tList.values()) {
                    Array.set(tArray, tIndex++, tItem);
                }
                Field tField = pCurrentMessage.getClass().getField(tPath);
                tField.set(pCurrentMessage, tArray);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the UUID generation by inserting placeholders at the configured locations.
     *
     * @param pMessage the message
     * @param pData the data
     */
    protected void processUuidGeneration(Object pMessage, CwfDataIf pData) {
        for (AsDefGenerateUniqueId tGenerate : mConfiguration.getGenerateUniqueIds()) {
            if (pMessage.getClass().getName().equals(tGenerate.getClassName()) ||
                As.getTypeName(pMessage.getClass()).equals(tGenerate.getClassName())) {
                pData.setProperty(tGenerate.getAttributeName(), createUUID());
            }
        }
    }

    /**
     * Copy configured values from source to target locations.
     *
     * @param pMessage the message
     * @param pData the data
     */
    protected void processValueCopying(Object pMessage, CwfDataIf pData) {
        for (AsDefCopyAttribute tCopy : mConfiguration.getCopyAttributes()) {
            if (pMessage.getClass().getName().equals(tCopy.getClassName()) ||
                As.getTypeName(pMessage.getClass()).equals(tCopy.getClassName())) {
                pData.setProperty(tCopy.getToAttributeName(), pData.getProperty(tCopy.getFromAttributeName()));
            }
        }
    }

    /**
     * Create a unique ID, by default a UUID.
     *
     * @return a unique ID
     */
    protected String createUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Recursively populate the client model data from the supplied server message.
     *
     * @param <T> the generic type
     * @param pData the data
     * @param pName the name
     * @param pPath the path
     * @param pMessage the message
     * @param pLocale the locale
     */
    @SuppressWarnings("unchecked")
    protected <T> void populateData(CwfDataIf pData, String pName, String pPath, T pMessage, Locale pLocale) {
        if ((pMessage == null) ||
            (pName.length() > 0 && mIn.isObjectSuppressed(pName, pMessage.getClass()))) {
            return;
        }
        for (Field tField : pMessage.getClass().getFields()) {
            if (!mIn.isFieldSuppressed(tField)) {
                switch (mIn.getFieldCategory(tField.getType())) {
                    case ARRAY:
                        String tName = pPath + (pPath.length() > 0 ? "." : "") + tField.getName();
                        populateArray(pData, pName, tName, pMessage, getObject(tField, pMessage), pLocale);
                        break;

                    case SIMPLE:
                        tName = pPath + (pPath.length() > 0 ? "." : "") + tField.getName();
                        pData.setProperty(tName, getValue(tField, pMessage));
                        break;

                    case COMPLEX:
                        Object tChild = getObject(tField, pMessage);
                        String tPath = pPath + (pPath.length() > 0 ? "." : "") + tField.getName();
                        populateData(pData, tField.getName(), tPath, tChild, pLocale);
                        break;

                    default:;
                }
            }
        }
        if (!pPath.isEmpty()) {
            pPath = pPath + ".";
        }
        for (AsGetMethodIf<?> tGetter : As.getPreRegisteredGetters(pMessage.getClass())) {
            AsGetMethodIf<T> tMethod = (AsGetMethodIf<T>) tGetter;
            Object tValue = tMethod.getObject(pMessage);
            if (tGetter.getBusinessType() == CwfBusinessTypes.Object && tValue != null) {
                CwfDataIf tObject = CwfDataFactory.create();
                populateData(tObject, "", "", tValue, pLocale);
                pData.setObject(pPath + tGetter.getAttributeName(), tObject);
            }
            else if (tValue instanceof AsLocalizedString) {
                pData.setProperty(pPath + tGetter.getAttributeName(),
                    tValue == null ? "" : ((AsLocalizedString) tValue).toString(pLocale));
            }
            else {
                pData.setProperty(pPath + tGetter.getAttributeName(), tValue == null ? "" : tValue.toString());
            }
        }
    }

    /**
     * Populate array.
     *
     * @param pData the data
     * @param pName the name
     * @param pPath the path
     * @param pMessage the message
     * @param pArray the array
     * @param pLocale the locale
     */
    protected void populateArray(CwfDataIf pData, String pName, String pPath,
            Object pMessage, Object pArray, Locale pLocale) {
        if (pArray == null) {
            return;
        }
        switch (mIn.getFieldCategory(pArray.getClass().getComponentType())) {
            case SIMPLE:
                pData.setProperty(pPath, primitiveArrayToString(pArray));
                break;

            case COMPLEX:
                for (int i = 0; i < Array.getLength(pArray); i++) {
                    Object tElement = Array.get(pArray, i);
                    CwfDataIf tElementData = CwfDataFactory.create();
                    populateData(tElementData, "", "", tElement, pLocale);
                    pData.addObject(pPath, tElementData);
                }
                break;

            default:;
        }
    }

    /**
     * Set the array value of the given field.
     *
     * @param pField the field
     * @param pObject the object
     * @param pData the data
     * @param pKey the key
     * @throws RuntimeException if the type is not handled or if another problem occurred while setting the array value.
     */
    protected void setArrayField(Field pField, Object pObject, CwfDataIf pData, String pKey) {
        try {
            Class<?> tClass = pField.getType().getComponentType();
            if (tClass == int.class) {
                pField.set(pObject, ((CwfDataIf) pData).getIntArray(pKey));
            }
            else if (tClass == Integer.class) {
                int[] tInts = ((CwfDataIf) pData).getIntArray(pKey);
                Integer[] tIntegers = new Integer[tInts.length];
                for (int i = 0; i < tInts.length; i++) {
                    tIntegers[i] = Integer.valueOf(tInts[i]);
                }
                pField.set(pObject, tIntegers);
            }
            else if (tClass == long.class) {
                String[] tValues = ((CwfDataIf) pData).getStringArray(pKey);
                long[] tLongs = new long[tValues.length];
                for (int i = 0; i < tValues.length; i++) {
                    tLongs[i] = Long.valueOf(tValues[i]).longValue();
                }
                pField.set(pObject, tLongs);
            }
            else if (tClass == Long.class) {
                String[] tValues = ((CwfDataIf) pData).getStringArray(pKey);
                Long[] tLongs = new Long[tValues.length];
                for (int i = 0; i < tValues.length; i++) {
                    tLongs[i] = Long.valueOf(tValues[i]);
                }
                pField.set(pObject, tLongs);
            }
            else if (tClass == float.class) {
                String[] tValues = ((CwfDataIf) pData).getStringArray(pKey);
                float[] tFloats = new float[tValues.length];
                for (int i = 0; i < tValues.length; i++) {
                    tFloats[i] = Float.valueOf(tValues[i]).floatValue();
                }
                pField.set(pObject, tFloats);
            }
            else if (tClass == Float.class) {
                String[] tValues = ((CwfDataIf) pData).getStringArray(pKey);
                Float[] tFloats = new Float[tValues.length];
                for (int i = 0; i < tValues.length; i++) {
                    tFloats[i] = Float.valueOf(tValues[i]);
                }
                pField.set(pObject, tFloats);
            }
            else if (tClass == double.class) {
                String[] tValues = ((CwfDataIf) pData).getStringArray(pKey);
                double[] tDoubles = new double[tValues.length];
                for (int i = 0; i < tValues.length; i++) {
                    tDoubles[i] = Double.valueOf(tValues[i]).doubleValue();
                }
                pField.set(pObject, tDoubles);
            }
            else if (tClass == Double.class) {
                String[] tValues = ((CwfDataIf) pData).getStringArray(pKey);
                Double[] tDoubles = new Double[tValues.length];
                for (int i = 0; i < tValues.length; i++) {
                    tDoubles[i] = Double.valueOf(tValues[i]);
                }
                pField.set(pObject, tDoubles);
            }
            else if (tClass == short.class) {
                String[] tValues = ((CwfDataIf) pData).getStringArray(pKey);
                short[] tShorts = new short[tValues.length];
                for (int i = 0; i < tValues.length; i++) {
                    tShorts[i] = Short.valueOf(tValues[i]).shortValue();
                }
                pField.set(pObject, tShorts);
            }
            else if (tClass == Short.class) {
                String[] tValues = ((CwfDataIf) pData).getStringArray(pKey);
                Short[] tShorts = new Short[tValues.length];
                for (int i = 0; i < tValues.length; i++) {
                    tShorts[i] = Short.valueOf(tValues[i]);
                }
                pField.set(pObject, tShorts);
            }
            else if (tClass == byte.class) {
                String[] tValues = ((CwfDataIf) pData).getStringArray(pKey);
                byte[] tBytes = new byte[tValues.length];
                for (int i = 0; i < tValues.length; i++) {
                    tBytes[i] = Byte.valueOf(tValues[i]).byteValue();
                }
                pField.set(pObject, tBytes);
            }
            else if (tClass == Byte.class) {
                String[] tValues = ((CwfDataIf) pData).getStringArray(pKey);
                Byte[] tBytes = new Byte[tValues.length];
                for (int i = 0; i < tValues.length; i++) {
                    tBytes[i] = Byte.valueOf(tValues[i]);
                }
                pField.set(pObject, tBytes);
            }
            else if (tClass == String.class) {
                pField.set(pObject, ((CwfDataIf) pData).getStringArray(pKey));
            }
            else {
                throw new RuntimeException("Simple type array not handled: " + tClass.getName());
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
