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
package com.cinnober.ciguan.datasource.tree;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Wrapper around a field definition (in a view).
 */
@JsonInclude(Include.NON_NULL)
public class AsViewField implements MvcModelAttributesIf {

    /** The path. */
    private final String mPath;

    /** The data. */
    private final CwfDataIf mData;

    /**
     * Instantiates a new as view field.
     *
     * @param pPath the path
     * @param pFieldData the field data
     */
    public AsViewField(String pPath, CwfDataIf pFieldData) {
        mPath = pPath == null || pPath.length() == 0 ? "" : pPath + ".";
        mData = pFieldData;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return mData.getProperty(ATTR_NAME);
    }

    /**
     * Gets the full name.
     *
     * @return the full name
     */
    @JsonIgnore
    public String getFullName() {
        return mPath + getName();
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
	@JsonInclude(Include.NON_EMPTY)
    public String getPath() {
        return mPath;
    }

    /**
     * Gets the context.
     *
     * @return the context
     */
    public String getContext() {
        return mData.getProperty(ATTR_CONTEXT);
    }

    /**
     * Gets the data source id.
     *
     * @return the data source id
     */
    public String getDataSourceId() {
        return mData.getProperty(ATTR_DATASOURCE_ID);
    }

    /**
     * Gets the summary.
     *
     * @return the summary
     */
    public String getSummary() {
        return mData.getProperty(ATTR_SUMMARY);
    }

}
