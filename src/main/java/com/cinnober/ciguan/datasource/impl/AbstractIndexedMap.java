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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Abstract indexed map.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public abstract class AbstractIndexedMap<K,V>
extends AbstractMap<K,V>
implements IndexedMap<K,V>
{
  
  /** The key set. */
  transient volatile Set<K> keySet = null;
  
  /** The values. */
  transient volatile Collection<V> values = null;
  
  /**
   * Instantiates a new abstract indexed map.
   */
  protected AbstractIndexedMap()
  {
  }
  
  /**
   * {@inheritDoc}
   */
  abstract public V get(int index);
  
  /**
   * {@inheritDoc}
   * 
   * @throws UnsupportedOperationException
   */
  public V set(int index, V value)
  {
    throw new UnsupportedOperationException();
  }
  
  /**
   * {@inheritDoc}
   * 
   * @throws UnsupportedOperationException
   */
  public V remove(int index)
  {
    throw new UnsupportedOperationException();
  }
  
  /**
   * {@inheritDoc}
   */
  public int indexOfKey(K key)
  {
    ListIterator<Map.Entry<K,V>> i = entryListIterator();
    if (key == null)
    {
      while (i.hasNext())
      {
        if (i.next() == null) return i.previousIndex();
      }
    }
    else
    {
      while (i.hasNext())
      {
        if (key.equals(i.next())) return i.previousIndex();
      }
    }
    return -1;
  }
  
  /**
   * {@inheritDoc}
   */
  public int lastIndexOfKey(K key)
  {
    ListIterator<Map.Entry<K,V>> i = entryListIterator(size());
    if (key == null)
    {
      while (i.hasPrevious())
      {
        if (i.previous() == null) return i.nextIndex();
      }
    }
    else
    {
      while (i.hasPrevious())
      {
        if (key.equals(i.previous())) return i.nextIndex();
      }
    }
    return -1;
  }
  
  /**
   * {@inheritDoc}
   */
  public ListIterator<Map.Entry<K,V>> entryListIterator()
  {
    return entryListIterator(0);
  }
  
  /**
   * {@inheritDoc}
   */
  public ListIterator<K> keyListIterator()
  {
    return keyListIterator(0);
  }
  
  /**
   * {@inheritDoc}
   */
  public ListIterator<V> valueListIterator()
  {
    return valueListIterator(0);
  }
}
