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
package com.cinnober.ciguan.xml.impl;

/**
 * Class storing a class attribute suppression rule
 * 
 * <pre>
 * Example:
 * 
 * {@code
 * <SuppressClassAttribute className="User" attributeName="hashedPassword" direction="out"/>.
 * }
 * </pre>
 */
public class AsDefSuppressClassAttribute extends AsDef {

    /** The class name. */
    private String mClassName;
    
    /** The attribute name. */
    private String mAttributeName;
    
    /** The direction. */
    private String mDirection;

    /**
     * Sets the class name.
     *
     * @param pAttribute the new class name
     */
    public void setClassName(String pAttribute) {
        mClassName = pAttribute;
    }

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return mClassName;
    }
    
    /**
     * Sets the attribute name.
     *
     * @param pAttribute the new attribute name
     */
    public void setAttributeName(String pAttribute) {
        mAttributeName = pAttribute;
    }

    /**
     * Gets the attribute name.
     *
     * @return the attribute name
     */
    public String getAttributeName() {
        return mAttributeName;
    }
    
    /**
     * Sets the direction.
     *
     * @param pDirection the new direction
     */
    public void setDirection(String pDirection) {
        ensureDirection(pDirection);
        mDirection = pDirection;
    }

    /**
     * Gets the direction.
     *
     * @return the direction
     */
    public String getDirection() {
        return mDirection;
    }
    
}