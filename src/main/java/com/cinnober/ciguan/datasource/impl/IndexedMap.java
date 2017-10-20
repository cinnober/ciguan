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

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;

/**
 * Defines operations for an indexed map. 
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface IndexedMap<K,V> extends SortedMap<K,V>
{
  
  /**
   * Entry iterator.
   *
   * @return the iterator
   */
  Iterator<Map.Entry<K,V>> entryIterator();
  
  /**
   * Value iterator.
   *
   * @return the iterator
   */
  Iterator<V> valueIterator();
  
  /**
   * Key iterator.
   *
   * @return the iterator
   */
  Iterator<K> keyIterator();
  
  /**
   * Entry list iterator.
   *
   * @return the list iterator
   */
  ListIterator<Map.Entry<K,V>> entryListIterator();
  
  /**
   * Value list iterator.
   *
   * @return the list iterator
   */
  ListIterator<V> valueListIterator();
  
  /**
   * Key list iterator.
   *
   * @return the list iterator
   */
  ListIterator<K> keyListIterator();
  
  /**
   * Entry list iterator.
   *
   * @param index the index
   * @return the list iterator
   */
  ListIterator<Map.Entry<K,V>> entryListIterator(int index);
  
  /**
   * Value list iterator.
   *
   * @param index the index
   * @return the list iterator
   */
  ListIterator<V> valueListIterator(int index);
  
  /**
   * Key list iterator.
   *
   * @param index the index
   * @return the list iterator
   */
  ListIterator<K> keyListIterator(int index);
  
  /**
   * Gets the value at that index.
   *
   * @param index the index
   * @return the v
   */
  V get(int index);
  
  /**
   * Sets the value at the specified index in the map.
   *
   * @param index the index
   * @param value the value
   * @return the value
   */
  V set(int index, V value);
  
  /**
   * Removes the value at the specified index from the map.
   *
   * @param index the index
   * @return the value
   */
  V remove(int index);
  
  /**
   * Returns the index of the specified key in the map.
   *
   * @param key the key
   * @return the index
   */
  int indexOfKey(K key);
  
  /**
   * Index of value.
   *
   * @param value the value
   * @return the index
   */
  int indexOfValue(V value);
  
  /**
   * Last index of key.
   *
   * @param key the key
   * @return the last index of this key
   */
  int lastIndexOfKey(K key);
  
  /**
   * Last index of value.
   *
   * @param value the value
   * @return the last index for this value
   */
  int lastIndexOfValue(V value);
  
  /* (non-Javadoc)
   * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
   */
  IndexedMap<K,V> subMap(K fromKey, K toKey);
  
  /**
   * Sub map.
   *
   * @param fromIndex the from index
   * @param toIndex the to index
   * @return the indexed map
   */
  IndexedMap<K,V> subMap(int fromIndex, int toIndex);
  
  /**
   * Sub map.
   *
   * @param fromKey the from key
   * @param toIndex the to index
   * @return the indexed map
   */
  IndexedMap<K,V> subMap(K fromKey, int toIndex);
  
  /**
   * Sub map.
   *
   * @param fromIndex the from index
   * @param toKey the to key
   * @return the indexed map
   */
  IndexedMap<K,V> subMap(int fromIndex, K toKey);
  
  /**
   * {@inheritDoc}
   */
  IndexedMap<K,V> headMap(K toKey);
  
  /**
   * Head map.
   *
   * @param toIndex the to index
   * @return the indexed map
   */
  IndexedMap<K,V> headMap(int toIndex);
  
  /**
   * {@inheritDoc}
   */
  IndexedMap<K,V> tailMap(K fromKey);
  
  /**
   * Tail map.
   *
   * @param fromIndex the from index
   * @return the indexed map
   */
  IndexedMap<K,V> tailMap(int fromIndex);
  
  /**
   * The Interface Entry.
   *
   * @param <K> the key type
   * @param <V> the value type
   */
  interface Entry<K,V> extends Map.Entry<K,V>
  {
    
    /**
     * {@inheritDoc}
     */
    K getKey();
    
    /**
     * {@inheritDoc}
     */
    V getValue();
    
    /**
     * {@inheritDoc}
     */
    V setValue(V value);
    
    /**
     * {@inheritDoc}
     */
    boolean equals(Object o);
    
    /**
     * {@inheritDoc}
     */
    int hashCode();
    
    /**
     * Gets the index.
     *
     * @return the index
     */
    int getIndex();
  }
}
