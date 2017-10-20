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
package com.cinnober.ciguan.bdx.processor;

import java.util.HashMap;
import java.util.Map;

import com.cinnober.ciguan.AsBdxProcessorIf;

/**
 * Generic BDX processor base class.
 */
public class AsBdxProcessor implements AsBdxProcessorIf {

    /** The Parameters. */
    private final Map<String, String> mParameters = new HashMap<String, String>();
    
    /**
     * {@inheritDoc}
     * 
     * No action on broadcast of the specified message.
     */
    @Override
    public void onBroadcast(Object pMessage) {
        // No action
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void setParameters(String pParameters) {
        if (pParameters == null || pParameters.isEmpty()) {
            return;
        }
        String[] tParts = pParameters.split("[,;]");
        for (String tPart : tParts) {
            String[] tValues = tPart.split("=");
            if (tValues.length == 2) {
                mParameters.put(tValues[0], tValues[1]);
            }
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public String getParameter(String pName) {
        return mParameters.get(pName);
    }
    
    /**
     * {@inheritDoc}
     * 
     *  No action on start.
     */
    @Override
    public void start() {
        // No action by default
    }
    
}
