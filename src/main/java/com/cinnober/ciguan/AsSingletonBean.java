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

import com.cinnober.ciguan.impl.As;

/**
 * Generic singleton base class.
 *
 * @param <I> the generic type
 */
public class AsSingletonBean<I> extends AsSingleton<I> {

    /** The Interface class. */
    private final Class<I> mInterfaceClass;
    
    /**
     * Instantiates a new as singleton bean.
     *
     * @param pClass the class
     */
    public AsSingletonBean(Class<I> pClass) {
        mInterfaceClass = pClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(I pI) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates the instance.
     */
    public void create() {
        super.set(As.getBeanFactory().create(mInterfaceClass));
    }

    /**
     * Creates the singleton bean.
     *
     * @param <I> the generic type
     * @param pClass the class
     * @return the singleton bean
     */
    public static <I> AsSingletonBean<I> create(Class<I> pClass) {
        return new AsSingletonBean<I>(pClass);
    }
    
}
