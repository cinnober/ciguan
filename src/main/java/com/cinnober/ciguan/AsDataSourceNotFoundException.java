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
package com.cinnober.ciguan;

/**
 *
 * Exception indicating that a referenced data source was not found during
 * creation of a data source in a factory. Only intended to be used by data source factories.
 * Throwing this exception in createGlobalList, createMemberList or createUserList
 * will result in a renewed attempt after all non-factory based data sources have been
 * created.
 * 
 */
@SuppressWarnings("serial")
public class AsDataSourceNotFoundException extends RuntimeException {

    /**
     * Instantiates an exception exception of this type.
     *
     * @param pReferencedDataSourceId the referenced data source id
     */
    public AsDataSourceNotFoundException(String pReferencedDataSourceId) {
        super(pReferencedDataSourceId);
    }
    
}
