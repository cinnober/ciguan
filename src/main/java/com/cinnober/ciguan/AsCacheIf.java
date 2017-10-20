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

import java.util.Collection;

/**
 * As Cache search functionality.
 */
public interface AsCacheIf {

    /** Singleton instance. */
    AsSingletonBean<AsCacheIf> SINGLETON = new AsSingletonBean<AsCacheIf>(AsCacheIf.class);

    /**
     * Fetch all objects of type pClass where the key matches pIds.
     * Note that pIds is an array to allow composite keys.
     *
     * @param <T> The generic type.
     * @param pClass The class.
     * @param pIds The ids.
     * @return the objects.
     */
    <T> T get(Class<T> pClass, String... pIds);
    
    /**
     * Fetch all objects of type pClass.
     *
     * @param <T> The generic type.
     * @param pClass The class.
     * @return the collection.
     */
    <T> Collection<T> get(Class<T> pClass);

    /**
     * Fetch all objects of type pFrom where the pFieldName attribute matches the pToKey value
     * Example: getReferencingObjects(User.class, "memberId", "AAA");
     *
     * @param <From> The generic type.
     * @param pFrom The from class.
     * @param pFieldName The field name.
     * @param pToKey The to key value.
     * @return the referencing objects.
     */
    <From> Collection<From> getReferencingObjects(Class<From> pFrom, String pFieldName, String pToKey);

}
