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
package com.cinnober.ciguan.datasource;

import com.cinnober.ciguan.CwfBusinessTypeIf;

/**
 * The Interface AsGetMethodIf.
 *
 * @param <T> The object type
 */
public interface AsGetMethodIf<T> {

    /**
     * Get the real value from the object.
     *
     * @param pItem the item
     * @return the object
     */
    Object getObject(T pItem);

    /**
     * Gets the raw unformatted value.
     *
     * @param pItem the item
     * @return the value
     */
    String getValue(T pItem);

    /**
     * Get the formatted value from the object.
     *
     * @param pItem the item
     * @param pService the service
     * @return the text
     */
    String getText(T pItem, AsDataSourceServiceIf pService);

    /**
     * Get the name of the attribute from which the value is retrieved.
     *
     * @return the attribute name
     */
    String getAttributeName();
    
    /**
     * Get the class of the object on which to invoke the method.
     *
     * @return the item class
     */
    Class<T> getItemClass();

    /**
     * Get the business type of the attribute.
     *
     * @return the business type
     */
    CwfBusinessTypeIf getBusinessType();

    /**
     * Get the business sub type of the attribute.
     *
     * @return the business subtype
     */
    String getBusinessSubtype();
    
}
