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

import java.util.Collection;

/**
 * Interface defining a server side data source event.
 *
 * @param <T> The type of the item which the event relates to
 */
public interface AsDataSourceEventIf<T> {

    /**
     * Type of event.
     */
    public enum Type {
        
        /** The add. */
        ADD,
        
        /** The clear. */
        CLEAR,
        
        /** The destroy. */
        DESTROY,
        
        /** The remove. */
        REMOVE,
        
        /** The snapshot. */
        SNAPSHOT,
        
        /** The update. */
        UPDATE
    }

    /**
     * Get the event type.
     *
     * @return the event type
     */
    public Type getType();

    /**
     * Get the index of the item in the list which the event concerns.
     *
     * @return the index
     */
    public int getIndex();
    
    /**
     * Get the new value.
     *
     * @return the new value
     */
    public T getNewValue();
    
    /**
     * Get the old value.
     *
     * @return the old value
     */
    public T getOldValue();
    
    /**
     * Get all values, only applicable for events of type SNAPSHOT.
     *
     * @return the snapshot
     */
    public Collection<T> getSnapshot();
    
    /**
     * Get the source list that generated this event.
     *
     * @return the source
     */
    public AsListIf<T> getSource();
}
