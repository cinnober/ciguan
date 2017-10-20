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

import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.annotation.CwfDisplayName;
import com.cinnober.ciguan.annotation.CwfReference;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.CwfBusinessTypes;

/**
 * Metadata for a field.
 */
public class AsMetaField {

    /** The owner. */
    @CwfReference(AsMetaObject.class)
    public String owner;

    /** The name. */
    @CwfDisplayName
    public String name;
    
    /** The type. */
    public String type;
    
    /** The business type. */
    public String businessType;

    /** The constant. */
    public String constant;
    
    /** The is object. */
    public boolean isObject;
    
    /** The is array. */
    public boolean isArray;

    /** The referenced type. */
    public String referencedType;
    
    /** The business type. */
    private CwfBusinessTypeIf mBusinessType;
    
    /**
     * Instantiates a new as meta field.
     *
     * @param pOwner the owner
     * @param pField the field
     */
    public AsMetaField(String pOwner, Field pField) {
        owner = pOwner;
        name = pField.getName();
        isArray = pField.getType().getComponentType() != null;
        Class<?> tTypeClass = isArray ? pField.getType().getComponentType() : pField.getType();
        type = As.getTypeName(tTypeClass);
        Class<?> tConstantType = As.getMetaDataHandler().getConstantType(pField);
        if (tConstantType != null) {
            constant = As.getTypeName(tConstantType);
        }
        else if (Enum.class.isAssignableFrom(tTypeClass)) {
            constant = type;
        }
        Class<?> tReferencedType = As.getMetaDataHandler().getReferencedType(pField);
        referencedType = tReferencedType != null ? As.getTypeName(tReferencedType) : null;
        mBusinessType = As.getMetaDataHandler().getBusinessType(pField);
        if (mBusinessType != null) {
            businessType = mBusinessType.name();
        }
        isObject = As.getType(type) != null;
    }

    /**
     * Instantiates a new as meta field.
     *
     * @param pGetter the getter
     */
    public AsMetaField(AsGetMethodIf<?> pGetter) {
        owner = As.getTypeName(pGetter.getItemClass());
        name = pGetter.getAttributeName();
        isArray = false;
        mBusinessType = pGetter.getBusinessType();
        if (mBusinessType == CwfBusinessTypes.Object) {
            isObject = true;
            type = pGetter.getBusinessSubtype();
        }
        else if (mBusinessType == CwfBusinessTypes.Constant) {
            constant = pGetter.getBusinessSubtype();
        }
    }
    
    /**
     * Gets the business type.
     *
     * @return the business type
     */
    public String getBusinessType() {
        return mBusinessType == null ? null : mBusinessType.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer tRet = new StringBuffer();
        tRet.append(name);
        tRet.append("=");
        if (isObject && constant == null) {
            tRet.append("@");
            tRet.append(type);
        }
        else if (constant != null) {
            tRet.append("Constant:");
            tRet.append(constant);
        }
        else if (mBusinessType != null) {
            tRet.append(mBusinessType.name());
        }
        else if (referencedType != null) {
            tRet.append("Reference:");
            tRet.append(referencedType);
        }
        else {
            tRet.append(type);
        }
        if (isArray && mBusinessType == null) {
            tRet.append("[]");
        }
        return tRet.toString();
    }
}
