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
package com.cinnober.ciguan.transport.impl;

import java.util.ArrayList;
import java.util.List;

import com.cinnober.ciguan.xml.impl.AsDefCopyAttribute;
import com.cinnober.ciguan.xml.impl.AsDefGenerateUniqueId;
import com.cinnober.ciguan.xml.impl.AsDefSuppressAttribute;
import com.cinnober.ciguan.xml.impl.AsDefSuppressClass;
import com.cinnober.ciguan.xml.impl.AsDefSuppressClassAttribute;

/**
 * Class holding request transformer configuration items.
 */
public class AsRequestTransformerConfiguration {

    /** The suppress attributes. */
    private final List<AsDefSuppressAttribute> mSuppressAttributes = new ArrayList<AsDefSuppressAttribute>();
    
    /** The suppress classes. */
    private final List<AsDefSuppressClass> mSuppressClasses = new ArrayList<AsDefSuppressClass>();
    
    /** The suppress class attributes. */
    private final List<AsDefSuppressClassAttribute> mSuppressClassAttributes =
        new ArrayList<AsDefSuppressClassAttribute>();
    
    /** The generate unique ids. */
    private final List<AsDefGenerateUniqueId> mGenerateUniqueIds = new ArrayList<AsDefGenerateUniqueId>();
    
    /** The copy attributes. */
    private final List<AsDefCopyAttribute> mCopyAttributes = new ArrayList<AsDefCopyAttribute>();
    
    /**
     * Adds the suppress attribute.
     *
     * @param pAttribute the attribute
     */
    public void addSuppressAttribute(AsDefSuppressAttribute pAttribute) {
        mSuppressAttributes.add(pAttribute);
    }

    /**
     * Gets the suppress attributes.
     *
     * @return the suppress attributes
     */
    public List<AsDefSuppressAttribute> getSuppressAttributes() {
        return mSuppressAttributes;
    }

    /**
     * Adds the suppress class.
     *
     * @param pClass the class
     */
    public void addSuppressClass(AsDefSuppressClass pClass) {
        mSuppressClasses.add(pClass);
    }

    /**
     * Gets the suppress classes.
     *
     * @return the suppress classes
     */
    public List<AsDefSuppressClass> getSuppressClasses() {
        return mSuppressClasses;
    }
    
    /**
     * Adds the suppress class attribute.
     *
     * @param pClassAttribute the class attribute
     */
    public void addSuppressClassAttribute(AsDefSuppressClassAttribute pClassAttribute) {
        mSuppressClassAttributes.add(pClassAttribute);
    }

    /**
     * Gets the suppress class attributes.
     *
     * @return the suppress class attributes
     */
    public List<AsDefSuppressClassAttribute> getSuppressClassAttributes() {
        return mSuppressClassAttributes;
    }
    
    /**
     * Adds the generate unique id.
     *
     * @param pGenerateUniqueId the generate unique id
     */
    public void addGenerateUniqueId(AsDefGenerateUniqueId pGenerateUniqueId) {
        mGenerateUniqueIds.add(pGenerateUniqueId);
    }
    
    /**
     * Gets the generate unique ids.
     *
     * @return the generate unique ids
     */
    public List<AsDefGenerateUniqueId> getGenerateUniqueIds() {
        return mGenerateUniqueIds;
    }
    
    /**
     * Adds the copy attribute.
     *
     * @param pCopyAttribute the copy attribute
     */
    public void addCopyAttribute(AsDefCopyAttribute pCopyAttribute) {
        mCopyAttributes.add(pCopyAttribute);
    }
    
    /**
     * Gets the copy attributes.
     *
     * @return the copy attributes
     */
    public List<AsDefCopyAttribute> getCopyAttributes() {
        return mCopyAttributes;
    }
    
}