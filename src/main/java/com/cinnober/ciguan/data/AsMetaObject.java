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
package com.cinnober.ciguan.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cinnober.ciguan.AsMetaDataHandlerIf;
import com.cinnober.ciguan.annotation.CwfDisplayName;
import com.cinnober.ciguan.annotation.CwfIdField;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.xml.impl.AsDefMetaData;

/**
 * Meta data describing the object from AS perspective.
 *
 * @param <T> the generic type
 */
public class AsMetaObject<T> {

    /** The name. */
    @CwfDisplayName
    @CwfIdField
    public String name;

    /** The id field. */
    public String idField;
    
    /** The display field. */
    public String displayField;
    
    /** The state field. */
    public String stateField;

    /** The server request name. */
    public String serverRequestName;
    
    /** The fields. */
    private List<AsMetaField> mFields;

    /** The item class. */
    private final Class<T> mItemClass;
    
    /** The get method map. */
    private final Map<String, AsGetMethodIf<T>> mGetMethodMap = new HashMap<String, AsGetMethodIf<T>>(); 
    
    /**
     * Instantiates a new as meta object.
     *
     * @param pClass the class
     * @param pXmlDef the xml def
     * @param pMetaDataHandler the meta data handler
     */
    public AsMetaObject(Class<T> pClass, AsDefMetaData pXmlDef, AsMetaDataHandlerIf pMetaDataHandler) {
        mItemClass = pClass;
        name = pMetaDataHandler.getTypeName(pClass);
        idField = pMetaDataHandler.getIdField(pClass);
        displayField = pMetaDataHandler.getDisplayField(pClass);
        if (pXmlDef != null) {
            stateField = pXmlDef.getState();
            serverRequestName = pXmlDef.getServerRequestName();
        }
    }
    
    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the id field.
     *
     * @return the id field
     */
    public String getIdField() {
        return idField;
    }
    
    /**
     * Gets the display field.
     *
     * @return the display field
     */
    public String getDisplayField() {
        return displayField;
    }
    
    /**
     * Gets the state field.
     *
     * @return the state field
     */
    public String getStateField() {
        return stateField;
    }

    /**
     * Gets the fields.
     *
     * @return the fields
     */
    public List<AsMetaField> getFields() {
        if (mFields == null) {
            initFields();
        }
        return mFields;
    }
    
    /**
     * Gets the server request name.
     *
     * @return the server request name
     */
    public String getServerRequestName() {
        return serverRequestName;
    }
    
    /**
     * Gets the item class.
     *
     * @return the item class
     */
    public Class<T> getItemClass() {
        return mItemClass;
    }

    /**
     * Cwf serialized version.
     *
     * @return the string
     */
    public String cwfSerializedVersion0() {
        StringBuffer tBuf = new StringBuffer();
        tBuf.append(name);
        if (serverRequestName != null) {
            tBuf.append("/");
            tBuf.append(serverRequestName);
        }
        tBuf.append("{");
        if (!getFields().isEmpty()) {
            tBuf.append(mFields.get(0));
            for (int i = 1; i < mFields.size(); i++) {
                tBuf.append(",");
                tBuf.append(mFields.get(i));
            }
        }
        tBuf.append("}");
        return tBuf.toString();
    }

    /**
     * Inits the fields.
     */
    @SuppressWarnings("unchecked")
    private void initFields() {
        mFields = new ArrayList<AsMetaField>();
        for (Field tField : mItemClass.getFields()) {
            if (!As.getMetaDataHandler().isFieldSuppressed(tField)) {
                mFields.add(new AsMetaField(name, tField));
            }
        }
        for (AsGetMethodIf<?> tGetter : As.getPreRegisteredGetters(this.mItemClass)) {
            mFields.add(new AsMetaField(tGetter));
            mGetMethodMap.put(tGetter.getAttributeName(), (AsGetMethodIf<T>) tGetter);
        }
    }
    
    /**
     * Gets the gets the method.
     *
     * @param pField the field
     * @return the gets the method
     */
    public AsGetMethodIf<T> getGetMethod(String pField) {
        AsGetMethodIf<T> tStateGetMethod = mGetMethodMap.get(pField);
        if (tStateGetMethod == null) {
            tStateGetMethod = AsGetMethod.create(mItemClass, pField);
            mGetMethodMap.put(pField, tStateGetMethod);
        }
        return tStateGetMethod;
    }

    /**
     * Gets the field.
     *
     * @param pField the field
     * @return the field
     */
    public AsMetaField getField(String pField) {
        for (AsMetaField tField : mFields) {
            if (tField.name.equals(pField)) {
                return tField;
            }
        }
        return null;
    }
    
}
