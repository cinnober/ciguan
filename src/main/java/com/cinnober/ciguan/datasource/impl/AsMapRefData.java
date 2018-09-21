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
package com.cinnober.ciguan.datasource.impl;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Hand coded implementation of map based values for use in data sources.
 */
public abstract class AsMapRefData implements MvcModelAttributesIf {

    /** The values. */
    private final CwfDataIf mValues;

    /**
     * Instantiates a new as map reference data.
     *
     * @param pValues the values
     */
    public AsMapRefData(CwfDataIf pValues) {
        mValues = pValues;
    }

    /**
     * Gets the values.
     *
     * @return the values
     */
    @JsonIgnore
    public CwfDataIf getValues() {
        return mValues;
    }

}
