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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Wrapper around a context declaration (in a view xml definition).
 */
@JsonInclude(Include.NON_NULL)
public class AsViewContext implements MvcModelAttributesIf {

    /** The data. */
    private final CwfDataIf mData;

    /** The model. */
    private final String mModel;

    /**
     * Instantiates a new as view context.
     *
     * @param pData the data
     * @param pModel the model
     */
    public AsViewContext(CwfDataIf pData, String pModel) {
        mData = pData;
        mModel = pModel;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return mData.getProperty(ATTR_TYPE);
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return mData.getProperty(ATTR_SOURCE);
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {
        return mData.getProperty(ATTR_TARGET);
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    public String getModel() {
        return mModel;
    }

}
