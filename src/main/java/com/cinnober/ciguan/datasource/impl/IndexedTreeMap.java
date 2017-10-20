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

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Red-Black tree based implementation of the <tt>IndexedMap</tt> interface.
 * This class guarantees that the map will be in ascending key order, sorted
 * according to the <i>natural order</i> for the key's class (see
 * <tt>Comparable</tt>), or by the comparator provided at creation time,
 * depending on which constructor is used.<p>
 *
 * This implementation provides guaranteed log(n) time cost for the
 * <tt>containsKey</tt>, <tt>get</tt>, <tt>put</tt> and <tt>remove</tt>
 * operations.  Algorithms are adaptations of those in Cormen, Leiserson, and
 * Rivest's <I>Introduction to Algorithms</I>.<p>
 *
 * Note that the ordering maintained by an indexed map (whether or not an
 * explicit comparator is provided) must be <i>consistent with equals</i> if
 * this indexed map is to correctly implement the <tt>Map</tt> interface.  (See
 * <tt>Comparable</tt> or <tt>Comparator</tt> for a precise definition of
 * <i>consistent with equals</i>.)  This is so because the <tt>Map</tt>
 * interface is defined in terms of the equals operation, but a map performs
 * all key comparisons using its <tt>compareTo</tt> (or <tt>compare</tt>)
 * method, so two keys that are deemed equal by this method are, from the
 * standpoint of the indexed map, equal.  The behavior of an indexed map
 * <i>is</i> well-defined even if its ordering is inconsistent with equals; it
 * just fails to obey the general contract of the <tt>Map</tt> interface.<p>
 *
 * <b>Note that this implementation is not synchronized.</b> If multiple
 * threads access a map concurrently, and at least one of the threads modifies
 * the map structurally, it <i>must</i> be synchronized externally.  (A
 * structural modification is any operation that adds or deletes one or more
 * mappings; merely changing the value associated with an existing key is not
 * a structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the map.  If no
 * such object exists, the map should be "wrapped" using the
 * <tt>Collections.synchronizedMap</tt> method.  This is best done at creation
 * time, to prevent accidental unsynchronized access to the map:
 * <pre>
 *     Map m = Collections.synchronizedMap(new IndexedTreeMap(...));
 * </pre><p>
 *
 * The iterators returned by all of this class's "collection view methods" are
 * <i>fail-fast</i>: if the map is structurally modified at any time after the
 * iterator is created, in any way except through the iterator's own
 * <tt>remove</tt> or <tt>add</tt> methods, the iterator throws a
 * <tt>ConcurrentModificationException</tt>.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:   <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i><p>
 *
 * This class is a member of the
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Matthew Wilson
 * @version 1.0, 16/11/09
 * @param <K> the key type
 * @param <V> the value type
 * @see Map
 * @see Comparable
 * @see Comparator
 * @see Collection
 * @see java.util.Collections#synchronizedMap(Map)
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class IndexedTreeMap<K,V>
extends AbstractIndexedMap<K,V>
implements IndexedMap<K,V>, Cloneable, java.io.Serializable
{

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 4465798410966014686L;

  /** The Constant RED. */
  private static final boolean RED   = false;

  /** The Constant BLACK. */
  private static final boolean BLACK = true;

  /**
   * The Comparator used to maintain order in this IndexedTreeMap, or
   * {@code null} if this IndexedTreeMap uses its elements natural ordering.
   */
  private Comparator<? super K> comparator = null;

  /** The root. */
  private transient Entry<K,V> root = null;

  /**
   * This field is initialized to contain an instance of the entry set
   * view the first time this view is requested.  The view is stateless,
   * so there's no reason to create more than one.
   */
  private transient volatile Set<Map.Entry<K,V>> entrySet = null;

  /** The number of entries in the tree. */
  private transient int size;

  /**
   * The number of structural modifications to the tree.
   */
  private transient int modCount = 0;

  /**
   * Increment size.
   */
  private void incrementSize() { modCount++; size++; }

  /**
   * Decrement size.
   */
  private void decrementSize() { modCount++; size--; }

  /**
   * Constructs a new, empty map, sorted according to the keys' natural
   * order.  All keys inserted into the map must implement the
   * <tt>Comparable</tt> interface.  Furthermore, all such keys must be
   * <i>mutually comparable</i>: <tt>k1.compareTo(k2)</tt> must not throw a
   * ClassCastException for any elements <tt>k1</tt> and <tt>k2</tt> in the
   * map.  If the user attempts to put a key into the map that violates this
   * constraint (for example, the user attempts to put a string key into a
   * map whose keys are integers), the <tt>put(Object key, Object
   * value)</tt> call will throw a <tt>ClassCastException</tt>.
   *
   * @see Comparable
   */
  public IndexedTreeMap()
  {
  }

  /**
   * Constructs a new, empty map, sorted according to the given comparator.
   * All keys inserted into the map must be <i>mutually comparable</i> by
   * the given comparator: <tt>comparator.compare(k1, k2)</tt> must not
   * throw a <tt>ClassCastException</tt> for any keys <tt>k1</tt> and
   * <tt>k2</tt> in the map.  If the user attempts to put a key into the
   * map that violates this constraint, the <tt>put(Object key, Object
   * value)</tt> call will throw a <tt>ClassCastException</tt>.
   *
   * @param c the comparator that will be used to sort this map.  A
   *        <tt>null</tt> value indicates that the keys' <i>natural
   *        ordering</i> should be used.
   */
  public IndexedTreeMap(Comparator<? super K> c)
  {
    this.comparator = c;
  }

  /**
   * Constructs a new map containing the same mappings as the given map,
   * sorted according to the keys' <i>natural order</i>.  All keys inserted
   * into the new map must implement the <tt>Comparable</tt> interface.
   * Furthermore, all such keys must be <i>mutually comparable</i>:
   * <tt>k1.compareTo(k2)</tt> must not throw a <tt>ClassCastException</tt>
   * for any elements <tt>k1</tt> and <tt>k2</tt> in the map.  This method
   * runs in n*log(n) time.
   *
   * @param m the
   */
  public IndexedTreeMap(Map<? extends K, ? extends V> m)
  {
    putAll(m);
  }

  /**
   * Constructs a new map containing the same mappings as the given
   * <tt>IndexedMap</tt>, sorted according to the same ordering.  This method
   * runs in linear time.
   *
   * @param m the
   */
  public IndexedTreeMap(IndexedMap<K, ? extends V> m)
  {
    comparator = m.comparator();
    try
    {
      buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
    }
    catch (java.io.IOException cannotHappen)
    {
    }
    catch (ClassNotFoundException cannotHappen)
    {
    }
  }

  /**
   * Entry iterator.
   *
   * @return the iterator
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#entryIterator()
   */
  public Iterator<Map.Entry<K,V>> entryIterator()
  {
    return new EntryIterator();
  }

  /**
   * Key iterator.
   *
   * @return the iterator
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#keyIterator()
   */
  public Iterator<K> keyIterator()
  {
    return new KeyIterator();
  }

  /**
   * Value iterator.
   *
   * @return the iterator
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#valueIterator()
   */
  public Iterator<V> valueIterator()
  {
    return new ValueIterator();
  }

  /**
   * Entry list iterator.
   *
   * @param index the index
   * @return the list iterator
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#entryListIterator(int)
   */
  public ListIterator<Map.Entry<K,V>> entryListIterator(final int index)
  {
    if (index < 0 || index > size) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    return new EntryIterator(index);
  }

  /**
   * Key list iterator.
   *
   * @param index the index
   * @return the list iterator
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#keyListIterator(int)
   */
  public ListIterator<K> keyListIterator(final int index)
  {
    if (index < 0 || index > size) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    return new KeyIterator(index);
  }

  /**
   * Value list iterator.
   *
   * @param index the index
   * @return the list iterator
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#valueListIterator(int)
   */
  public ListIterator<V> valueListIterator(final int index)
  {
    if (index < 0 || index > size) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    return new ValueIterator(index);
  }

  /**
   * Returns the number of key-value mappings in this map.
   *
   * @return the number of key-value mappings in this map.
   */
  public int size()
  {
    return size;
  }

  /**
   * Returns <tt>true</tt> if this map contains a mapping for the specified
   * key.
   *
   * @param key key whose presence in this map is to be tested.
   * @return <tt>true</tt> if this map contains a mapping for the
   *            specified key.
   */
  public boolean containsKey(Object key)
  {
    return getEntry(key) != null;
  }

  /**
   * Returns <tt>true</tt> if this map maps one or more keys to the
   * specified value.  More formally, returns <tt>true</tt> if and only if
   * this map contains at least one mapping to a value <tt>v</tt> such
   * that <tt>(value==null ? v==null : value.equals(v))</tt>.  This
   * operation will probably require time linear in the Map size for most
   * implementations of Map.
   *
   * @param value value whose presence in this Map is to be tested.
   * @return  <tt>true</tt> if a mapping to <tt>value</tt> exists;
   *          <tt>false</tt> otherwise.
   */
  public boolean containsValue(Object value)
  {
    return (root == null ? false :
      (value == null ? valueSearchNull(root) : valueSearchNonNull(root, value)));
  }

  /**
   * Returns the comparator used to order this map, or <tt>null</tt> if this
   * map uses its keys' natural order.
   *
   * @return the comparator associated with this sorted map, or
   *                <tt>null</tt> if it uses its keys' natural sort method.
   */
  public Comparator<? super K> comparator()
  {
    return comparator;
  }

  /**
   * Returns the first (lowest) key currently in this sorted map.
   *
   * @return the first (lowest) key currently in this sorted map.
   */
  public K firstKey()
  {
    return key(firstEntry());
  }

  /**
   * Returns the index of the specified key.
   *
   * @param key the key
   * @return the index
   * @see com.cinnober.ciguan.datasource.impl.AbstractIndexedMap#indexOfKey(java.lang.Object)
   */
  public int indexOfKey(K key)
  {
    Entry<K,V> p = getEntry(key);
    return p == null ? -1 : p.getIndex();
  }

  /**
   * Returns the index of the specified value.
   *
   * @param value the value
   * @return the index
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#indexOfValue(java.lang.Object)
   */
  public int indexOfValue(V value)
  {
    ListIterator<Map.Entry<K,V>> i = entryListIterator();
    if (value == null)
    {
      while (i.hasNext())
      {
        Map.Entry<K,V> entry = i.next();
        if (entry.getValue() == null) return i.previousIndex();
      }
    }
    else
    {
      while (i.hasNext())
      {
        Map.Entry<K,V> entry = i.next();
        if (value.equals(entry.getValue())) return i.previousIndex();
      }
    }
    return -1;
  }

  /**
   * Returns the last (highest) key currently in this sorted map.
   *
   * @return the last (highest) key currently in this sorted map.
   */
  public K lastKey()
  {
    return key(lastEntry());
  }

  /**
   * Last index of key.
   *
   * @param key the key
   * @return the index
   * @see com.cinnober.ciguan.datasource.impl.AbstractIndexedMap#lastIndexOfKey(java.lang.Object)
   */
  public int lastIndexOfKey(K key)
  {
    return indexOfKey(key);
  }

  /**
   * Last index of value.
   *
   * @param value the value
   * @return the int
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#lastIndexOfValue(java.lang.Object)
   */
  public int lastIndexOfValue(V value)
  {
    ListIterator<Map.Entry<K,V>> i = entryListIterator(size());
    if (value == null)
    {
      while (i.hasPrevious())
      {
        Map.Entry<K,V> entry = i.previous();
        if (entry.getValue() == null) return i.nextIndex();
      }
    }
    else
    {
      while (i.hasPrevious())
      {
        Map.Entry<K,V> entry = i.previous();
        if (value.equals(entry.getValue())) return i.nextIndex();
      }
    }
    return -1;
  }

  /**
   * Returns the value to which this map maps the specified key.  Returns
   * <tt>null</tt> if the map contains no mapping for this key.  A return
   * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
   * map contains no mapping for the key; it's also possible that the map
   * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
   * operation may be used to distinguish these two cases.
   *
   * @param key key whose associated value is to be returned.
   * @return the value to which this map maps the specified key, or
   *               <tt>null</tt> if the map contains no mapping for the key.
   * @see #containsKey(Object)
   */
  public V get(Object key)
  {
    Entry<K,V> p = getEntry(key);
    return (p == null ? null : p.value);
  }

  /**
   * Gets the value from that index.
   *
   * @param index the index
   * @return the v
   * @see com.cinnober.ciguan.datasource.impl.AbstractIndexedMap#get(int)
   */
  public V get(int index)
  {
    rangeCheck(index);
    Entry<K,V> p = getEntry(index);
    return p.value;
  }

  /**
   * Associates the specified value with the specified key in this map.
   * If the map previously contained a mapping for this key, the old
   * value is replaced.
   *
   * @param key key with which the specified value is to be associated.
   * @param value value to be associated with the specified key.
   * @return previous value associated with specified key, or <tt>null</tt>
   *         if there was no mapping for key.  A <tt>null</tt> return can
   *         also indicate that the map previously associated <tt>null</tt>
   *         with the specified key.
   */
  public V put(K key, V value)
  {
    Entry<K,V> t = root;
    if (t == null)
    {
      incrementSize();
      root = new Entry<K,V>(key, value, null);
      return null;
    }

    while (true)
    {
      int cmp = compare(key, t.key);
      if (cmp == 0)
      {
        Entry<K,V> parent = t.parent;
        while (parent != null)
        {
          int cmp2 = compare(key, parent.key);
          if (cmp2 < 0) parent.leftNodes--;
          else parent.rightNodes--;
          parent = parent.parent;
        }
        return t.setValue(value);
      }
      else if (cmp < 0)
      {
        t.leftNodes++;
        if (t.left != null) t = t.left;
        else
        {
          incrementSize();
          t.left = new Entry<K,V>(key, value, t);
          fixAfterInsertion(t.left);
          return null;
        }
      }
      else
      {
        t.rightNodes++;
        if (t.right != null) t = t.right;
        else
        {
          incrementSize();
          t.right = new Entry<K,V>(key, value, t);
          fixAfterInsertion(t.right);
          return null;
        }
      }
    }
  }

  /**
   * Copies all of the mappings from the specified map to this map.  These
   * mappings replace any mappings that this map had for any of the keys
   * currently in the specified map.
   *
   * @param map the map
   */
public void putAll(Map<? extends K, ? extends V> map)
  {
    int mapSize = map.size();
    if (size == 0 && mapSize != 0 && map instanceof IndexedMap)
    {
      Comparator c = ((IndexedMap)map).comparator();
      if (c == comparator || (c != null && c.equals(comparator)))
      {
        ++modCount;
        try
        {
          buildFromSorted(mapSize, map.entrySet().iterator(), null, null);
        }
        catch (java.io.IOException cannotHappen)
        {
        }
        catch (ClassNotFoundException cannotHappen)
        {
        }
        return;
      }
    }
    super.putAll(map);
  }

  /**
   * Sets the value at the specified index.
   *
   * @param index the index
   * @param value the value
   * @return the v
   * @see com.cinnober.ciguan.datasource.impl.AbstractIndexedMap#set(int, java.lang.Object)
   */
  public V set(int index, V value)
  {
    rangeCheck(index);
    Entry<K,V> p = getEntry(index);
    V oldValue = p.getValue();
    p.setValue(value);
    return oldValue;
  }

  /**
   * Removes the mapping for this key from this IndexedTreeMap if present.
   *
   * @param key the key
   * @return previous value associated with specified key, or <tt>null</tt>
   *         if there was no mapping for key.  A <tt>null</tt> return can
   *         also indicate that the map previously associated
   *         <tt>null</tt> with the specified key.
   */
  public V remove(Object key)
  {
    Entry<K,V> p = getEntry(key);
    if (p == null) return null;

    V oldValue = p.value;
    deleteEntry(p);
    return oldValue;
  }

  /**
   * Removes the value from that index.
   *
   * @param index the index
   * @return the v
   * @see com.cinnober.ciguan.datasource.impl.AbstractIndexedMap#remove(int)
   */
  public V remove(int index)
  {
    rangeCheck(index);
    Entry<K,V> p = getEntry(index);
    V value = p.getValue();
    deleteEntry(p);

    return value;
  }

  /**
   * Removes all mappings from this IndexedTreeMap.
   */
  public void clear()
  {
    modCount++;
    size = 0;
    root = null;
  }

  /**
   * Returns a shallow copy of this <tt>IndexedTreeMap</tt> instance. (The keys and
   * values themselves are not cloned.)
   *
   * @return a shallow copy of this Map.
   */
  public Object clone()
  {
    IndexedTreeMap<K,V> clone = null;
    try
    {
      clone = (IndexedTreeMap<K,V>)super.clone();
    }
    catch (CloneNotSupportedException e)
    {
      throw new InternalError();
    }

    clone.root = null;
    clone.size = 0;
    clone.modCount = 0;
    clone.entrySet = null;

    try
    {
      clone.buildFromSorted(size, entrySet().iterator(), null, null);
    }
    catch (java.io.IOException cannotHappen)
    {
    }
    catch (ClassNotFoundException cannotHappen)
    {
    }

    return clone;
  }

  /**
   * Returns the key corresponding to the specified Entry.  Throw
   * NoSuchElementException if the Entry is <tt>null</tt>.
   *
   * @param <K> the key type
   * @param e the e
   * @return the k
   */
  private static <K> K key(Entry<K,?> e)
  {
    if (e == null) throw new NoSuchElementException();
    return e.key;
  }

  /**
   * Range check.
   *
   * @param index the index
   */
  private void rangeCheck(int index)
  {
    if (index < 0 || index >= size)
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
  }

  /**
   * Compare.
   *
   * @param key1 the key1
   * @param key2 the key2
   * @return the int
   */
  private int compare(K key1, K key2)
  {
    return (comparator == null ?
      ((Comparable<K>)key1).compareTo(key2) :
      comparator.compare(key1, key2));
  }

  /**
   * Checks if the two objects are equal.
   *
   * @param o1 the first object
   * @param o2 the second object
   * @return {@code true}, if successful
   */
  private static boolean valEquals(Object o1, Object o2)
  {
    return (o1 == null ? o2 == null : o1.equals(o2));
  }

  /**
   * Checks for a null entry starting from the specified entry
   *
   * @param n the entry
   * @return {@code true}, if successful
   */
  private boolean valueSearchNull(Entry n)
  {
    if (n.value == null) return true;

    return (n.left != null && valueSearchNull(n.left)) ||
      (n.right != null && valueSearchNull(n.right));
  }

  /**
   * Checks for a non null entry equal to {@code value} starting from the specified entry.
   *
   * @param n the entry
   * @param value the value
   * @return {@code true}, if successful
   */
  private boolean valueSearchNonNull(Entry n, Object value)
  {
    if (value.equals(n.value)) return true;

    return (n.left != null && valueSearchNonNull(n.left, value)) ||
      (n.right != null && valueSearchNonNull(n.right, value));
  }

  /**
   * Returns a Set view of the keys contained in this map.  The set's
   * iterator will return the keys in ascending order.  The map is backed by
   * this <tt>IndexedTreeMap</tt> instance, so changes to this map are reflected in
   * the Set, and vice-versa.  The Set supports element removal, which
   * removes the corresponding mapping from the map, via the
   * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
   * <tt>retainAll</tt>, and <tt>clear</tt> operations.  It does not support
   * the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a set view of the keys contained in this IndexedTreeMap.
   */
  public Set<K> keySet()
  {
    if (keySet == null)
    {
      keySet = new AbstractSet<K>()
      {
        public Iterator<K> iterator()
        {
          return new KeyIterator();
        }

        public int size()
        {
          return IndexedTreeMap.this.size();
        }

        public boolean contains(Object o)
        {
          return containsKey(o);
        }

        public boolean remove(Object o)
        {
          int oldSize = size;
          IndexedTreeMap.this.remove(o);
          return size != oldSize;
        }

        public void clear()
        {
          IndexedTreeMap.this.clear();
        }
      };
    }
    return keySet;
  }

  /**
   * Returns a collection view of the values contained in this map.  The
   * collection's iterator will return the values in the order that their
   * corresponding keys appear in the tree.  The collection is backed by
   * this <tt>IndexedTreeMap</tt> instance, so changes to this map are reflected in
   * the collection, and vice-versa.  The collection supports element
   * removal, which removes the corresponding mapping from the map through
   * the <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the values contained in this map.
   */
  public Collection<V> values()
  {
    if (values == null)
    {
      values = new AbstractCollection<V>()
      {
        public Iterator<V> iterator()
        {
          return new ValueIterator();
        }

        public int size()
        {
          return IndexedTreeMap.this.size();
        }

        public boolean contains(Object o)
        {
          for (Entry<K,V> e = firstEntry(); e != null; e = successor(e))
          {
            if (valEquals(e.getValue(), o)) return true;
          }
          return false;
        }

        public boolean remove(Object o)
        {
          for (Entry<K,V> e = firstEntry(); e != null; e = successor(e))
          {
            if (valEquals(e.getValue(), o))
            {
              deleteEntry(e);
              return true;
            }
          }
          return false;
        }

        public void clear()
        {
          IndexedTreeMap.this.clear();
        }
      };
    }
    return values;
  }

  /**
   * Returns a set view of the mappings contained in this map.  The set's
   * iterator returns the mappings in ascending key order.  Each element in
   * the returned set is a <tt>Map.Entry</tt>.  The set is backed by this
   * map, so changes to this map are reflected in the set, and vice-versa.
   * The set supports element removal, which removes the corresponding
   * mapping from the IndexedTreeMap, through the <tt>Iterator.remove</tt>,
   * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
   * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
   * <tt>addAll</tt> operations.
   *
   * @return a set view of the mappings contained in this map.
   * @see Entry
   */
  public Set<Map.Entry<K,V>> entrySet()
  {
    if (entrySet == null)
    {
      entrySet = new AbstractSet<Map.Entry<K,V>>()
      {
        public Iterator<Map.Entry<K,V>> iterator()
        {
          return new EntryIterator();
        }

        public boolean contains(Object o)
        {
          if (!(o instanceof Map.Entry)) return false;
          Map.Entry<K,V> entry = (Map.Entry<K,V>)o;
          V value = entry.getValue();
          Entry<K,V> p = getEntry(entry.getKey());
          return p != null && valEquals(p.getValue(), value);
        }

        public boolean remove(Object o)
        {
          if (!(o instanceof Map.Entry)) return false;
          Map.Entry<K,V> entry = (Map.Entry<K,V>)o;
          V value = entry.getValue();
          Entry<K,V> p = getEntry(entry.getKey());
          if (p != null && valEquals(p.getValue(), value))
          {
            deleteEntry(p);
            return true;
          }
          return false;
        }

        public int size()
        {
          return IndexedTreeMap.this.size();
        }

        public void clear()
        {
          IndexedTreeMap.this.clear();
        }
      };
    }
    return entrySet;
  }

  /**
   * Returns a view of the portion of this map whose keys range from
   * <tt>fromKey</tt>, inclusive, to <tt>toKey</tt>, exclusive.  (If
   * <tt>fromKey</tt> and <tt>toKey</tt> are equal, the returned sorted map
   * is empty.)  The returned sorted map is backed by this map, so changes
   * in the returned sorted map are reflected in this map, and vice-versa.
   * The returned sorted map supports all optional map operations.<p>
   *
   * The sorted map returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert a key
   * less than <tt>fromKey</tt> or greater than or equal to
   * <tt>toKey</tt>.<p>
   *
   * Note: this method always returns a <i>half-open range</i> (which
   * includes its low endpoint but not its high endpoint).  If you need a
   * <i>closed range</i> (which includes both endpoints), and the key type
   * allows for calculation of the successor a given key, merely request the
   * subrange from <tt>lowEndpoint</tt> to <tt>successor(highEndpoint)</tt>.
   * For example, suppose that <tt>m</tt> is a sorted map whose keys are
   * strings.  The following idiom obtains a view containing all of the
   * key-value mappings in <tt>m</tt> whose keys are between <tt>low</tt>
   * and <tt>high</tt>, inclusive:
   *             <pre>    SortedMap sub = m.submap(low, high+"\0");</pre>
   * A similar technique can be used to generate an <i>open range</i> (which
   * contains neither endpoint).  The following idiom obtains a view
   * containing all of the key-value mappings in <tt>m</tt> whose keys are
   * between <tt>low</tt> and <tt>high</tt>, exclusive:
   *             <pre>    SortedMap sub = m.subMap(low+"\0", high);</pre>
   *
   * @param fromKey low endpoint (inclusive) of the subMap.
   * @param toKey high endpoint (exclusive) of the subMap.
   * @return a view of the portion of this map whose keys range from
   *                <tt>fromKey</tt>, inclusive, to <tt>toKey</tt>, exclusive.
   */
  public IndexedMap<K,V> subMap(K fromKey, K toKey)
  {
    return new SubMap(fromKey, toKey);
  }

  /**
   * Sub map.
   *
   * @param fromIndex the from index
   * @param toIndex the to index
   * @return the indexed map
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#subMap(int, int)
   */
  public IndexedMap<K,V> subMap(int fromIndex, int toIndex)
  {
    return new SubMap(fromIndex, toIndex);
  }

  /**
   * Sub map.
   *
   * @param fromKey the from key
   * @param toIndex the to index
   * @return the indexed map
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#subMap(java.lang.Object, int)
   */
  public IndexedMap<K,V> subMap(K fromKey, int toIndex)
  {
    return new SubMap(fromKey, toIndex);
  }

  /**
   * Sub map.
   *
   * @param fromIndex the from index
   * @param toKey the to key
   * @return the indexed map
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#subMap(int, java.lang.Object)
   */
  public IndexedMap<K,V> subMap(int fromIndex, K toKey)
  {
    return new SubMap(fromIndex, toKey);
  }

  /**
   * Returns a view of the portion of this map whose keys are strictly less
   * than <tt>toKey</tt>.  The returned sorted map is backed by this map, so
   * changes in the returned sorted map are reflected in this map, and
   * vice-versa.  The returned sorted map supports all optional map
   * operations.<p>
   *
   * The sorted map returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert a key
   * greater than or equal to <tt>toKey</tt>.<p>
   *
   * Note: this method always returns a view that does not contain its
   * (high) endpoint.  If you need a view that does contain this endpoint,
   * and the key type allows for calculation of the successor a given key,
   * merely request a headMap bounded by <tt>successor(highEndpoint)</tt>.
   * For example, suppose that suppose that <tt>m</tt> is a sorted map whose
   * keys are strings.  The following idiom obtains a view containing all of
   * the key-value mappings in <tt>m</tt> whose keys are less than or equal
   * to <tt>high</tt>:
   * <pre>
   *     SortedMap head = m.headMap(high+"\0");
   * </pre>
   *
   * @param toKey high endpoint (exclusive) of the headMap.
   * @return a view of the portion of this map whose keys are strictly
   *                less than <tt>toKey</tt>.
   */
  public IndexedMap<K,V> headMap(K toKey)
  {
    return new SubMap(toKey, true);
  }

  /**
   * Head map.
   *
   * @param toIndex the to index
   * @return the indexed map
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#headMap(int)
   */
  public IndexedMap<K,V> headMap(int toIndex)
  {
    return new SubMap(toIndex, true);
  }

  /**
   * Returns a view of the portion of this map whose keys are greater than
   * or equal to <tt>fromKey</tt>.  The returned sorted map is backed by
   * this map, so changes in the returned sorted map are reflected in this
   * map, and vice-versa.  The returned sorted map supports all optional map
   * operations.<p>
   *
   * The sorted map returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert a key
   * less than <tt>fromKey</tt>.<p>
   *
   * Note: this method always returns a view that contains its (low)
   * endpoint.  If you need a view that does not contain this endpoint, and
   * the element type allows for calculation of the successor a given value,
   * merely request a tailMap bounded by <tt>successor(lowEndpoint)</tt>.
   * For example, suppose that <tt>m</tt> is a sorted map whose keys
   * are strings.  The following idiom obtains a view containing
   * all of the key-value mappings in <tt>m</tt> whose keys are strictly
   * greater than <tt>low</tt>: <pre>
   *     SortedMap tail = m.tailMap(low+"\0");
   * </pre>
   *
   * @param fromKey low endpoint (inclusive) of the tailMap.
   * @return a view of the portion of this map whose keys are greater
   *                than or equal to <tt>fromKey</tt>.
   */
  public IndexedMap<K,V> tailMap(K fromKey)
  {
    return new SubMap(fromKey, false);
  }

  /**
   * Tail map.
   *
   * @param fromIndex the from index
   * @return the indexed map
   * @see com.cinnober.ciguan.datasource.impl.IndexedMap#tailMap(int)
   */
  public IndexedMap<K,V> tailMap(int fromIndex)
  {
    return new SubMap(fromIndex, false);
  }

  /**
   * The Class SubMap.
   */
  private class SubMap
  extends AbstractIndexedMap<K,V>
  implements IndexedMap<K,V>, java.io.Serializable
  {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -348770434460450477L;

    /** The entry set. */
    private transient Set<Map.Entry<K,V>> entrySet = new EntrySetView();

    /**
     * fromKey and fromIndex are significant only if fromStart is false.  Similarly,
     * toKey and toIndex are significant only if toStart is false.
     */
    private boolean fromStart = false, toEnd = false;

    /** The to index based. */
    private boolean fromIndexBased = false, toIndexBased = false;

    /** The to key. */
    private K fromKey, toKey;

    /** The to index. */
    private int fromIndex, toIndex;

    /**
     * Instantiates a new sub map.
     *
     * @param fromKey the from key
     * @param toKey the to key
     */
    SubMap(K fromKey, K toKey)
    {
      if (compare(fromKey, toKey) > 0) throw new IllegalArgumentException("fromKey > toKey");
      this.fromKey = fromKey;
      this.toKey = toKey;
    }

    /**
     * Instantiates a new sub map.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     */
    SubMap(int fromIndex, int toIndex)
    {
      if (fromIndex > toIndex) throw new IllegalArgumentException("fromIndex > toIndex");
      this.fromIndexBased = true;
      this.fromIndex = fromIndex;
      this.toIndexBased = true;
      this.toIndex = toIndex;
    }

    /**
     * Instantiates a new sub map.
     *
     * @param fromKey the from key
     * @param toIndex the to index
     */
    SubMap(K fromKey, int toIndex)
    {
      this.fromKey = fromKey;
      this.toIndexBased = true;
      this.toIndex = toIndex;
    }

    /**
     * Instantiates a new sub map.
     *
     * @param fromIndex the from index
     * @param toKey the to key
     */
    SubMap(int fromIndex, K toKey)
    {
      this.fromIndexBased = true;
      this.fromIndex = fromIndex;
      this.toKey = toKey;
    }

    /**
     * Instantiates a new sub map.
     *
     * @param key the key
     * @param headMap the head map
     */
    SubMap(K key, boolean headMap)
    {
      compare(key, key); // Type-check key

      if (headMap)
      {
        fromStart = true;
        toKey = key;
      }
      else
      {
        toEnd = true;
        fromKey = key;
      }
    }

    /**
     * Instantiates a new sub map.
     *
     * @param index the index
     * @param headMap the head map
     */
    SubMap(int index, boolean headMap)
    {
      if (headMap)
      {
        fromStart = true;
        toIndexBased = true;
        toIndex = index;
      }
      else
      {
        toEnd = true;
        fromIndexBased = true;
        fromIndex = index;
      }
    }

    /**
     * Instantiates a new sub map.
     *
     * @param fromStart the from start
     * @param fromKey the from key
     * @param toEnd the to end
     * @param toKey the to key
     */
    SubMap(boolean fromStart, K fromKey, boolean toEnd, K toKey)
    {
      this.fromStart = fromStart;
      this.fromKey = fromKey;
      this.toEnd = toEnd;
      this.toKey = toKey;
    }

    /**
     * Instantiates a new sub map.
     *
     * @param fromStart the from start
     * @param fromIndex the from index
     * @param toEnd the to end
     * @param toIndex the to index
     */
    SubMap(boolean fromStart, int fromIndex, boolean toEnd, int toIndex)
    {
      this.fromStart = fromStart;
      this.fromIndexBased = true;
      this.fromIndex = fromIndex;
      this.toEnd = toEnd;
      this.toIndexBased = true;
      this.toIndex = toIndex;
    }

    /**
     * Instantiates a new sub map.
     *
     * @param fromStart the from start
     * @param fromKey the from key
     * @param toEnd the to end
     * @param toIndex the to index
     */
    SubMap(boolean fromStart, K fromKey, boolean toEnd, int toIndex)
    {
      this.fromStart = fromStart;
      this.fromKey = fromKey;
      this.toEnd = toEnd;
      this.toIndexBased = true;
      this.toIndex = toIndex;
    }

    /**
     * Instantiates a new sub map.
     *
     * @param fromStart the from start
     * @param fromIndex the from index
     * @param toEnd the to end
     * @param toKey the to key
     */
    SubMap(boolean fromStart, int fromIndex, boolean toEnd, K toKey)
    {
      this.fromStart = fromStart;
      this.fromIndexBased = true;
      this.fromIndex = fromIndex;
      this.toEnd = toEnd;
      this.toKey = toKey;
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     * @see java.util.AbstractMap#isEmpty()
     */
    public boolean isEmpty()
    {
      return entrySet.isEmpty();
    }

    /**
     * Contains key.
     *
     * @param key the key
     * @return true, if successful
     * @see java.util.AbstractMap#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key)
    {
      return keyInRange((K)key) && IndexedTreeMap.this.containsKey(key);
    }

    /**
     * Index of key.
     *
     * @param key the key
     * @return the int
     * @see com.cinnober.ciguan.datasource.impl.AbstractIndexedMap#indexOfKey(java.lang.Object)
     */
    public int indexOfKey(K key)
    {
      return IndexedTreeMap.this.indexOfKey(key);
    }

    /**
     * Index of value.
     *
     * @param value the value
     * @return the int
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#indexOfValue(java.lang.Object)
     */
    public int indexOfValue(V value)
    {
      return IndexedTreeMap.this.indexOfValue(value);
    }

    /**
     * Last index of key.
     *
     * @param key the key
     * @return the int
     * @see com.cinnober.ciguan.datasource.impl.AbstractIndexedMap#lastIndexOfKey(java.lang.Object)
     */
    public int lastIndexOfKey(K key)
    {
      return IndexedTreeMap.this.lastIndexOfKey(key);
    }

    /**
     * Last index of value.
     *
     * @param value the value
     * @return the int
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#lastIndexOfValue(java.lang.Object)
     */
    public int lastIndexOfValue(V value)
    {
      return IndexedTreeMap.this.lastIndexOfValue(value);
    }

    /**
     * Gets the value mapped with that key.
     *
     * @param key the key
     * @return the v
     * @see java.util.AbstractMap#get(java.lang.Object)
     */
    public V get(Object key)
    {
      if (!keyInRange((K)key)) return null;
      return IndexedTreeMap.this.get(key);
    }

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @return the v
     * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
     */
    public V put(K key, V value)
    {
      if (!keyInRange(key)) throw new IllegalArgumentException("key out of range");
      return IndexedTreeMap.this.put(key, value);
    }

    /**
     * Gets the value mapped at that index.
     *
     * @param index the index
     * @return the v
     * @see com.cinnober.ciguan.datasource.impl.AbstractIndexedMap#get(int)
     */
    public V get(int index)
    {
      if (!indexInRange(index)) throw new IllegalArgumentException("index out of range");
      return IndexedTreeMap.this.get(index);
    }

    /**
     * Sets the value at the specified index.
     *
     * @param index the index
     * @param value the value
     * @return the v
     * @see com.cinnober.ciguan.datasource.impl.AbstractIndexedMap#set(int, java.lang.Object)
     */
    public V set(int index, V value)
    {
      if (!indexInRange(index)) throw new IllegalArgumentException("index out of range");
      return IndexedTreeMap.this.set(index, value);
    }

    /**
     * Removes the value from the specified index.
     *
     * @param index the index
     * @return the value
     * @see com.cinnober.ciguan.datasource.impl.AbstractIndexedMap#remove(int)
     */
    public V remove(int index)
    {
      if (!indexInRange(index)) throw new IllegalArgumentException("index out of range");
      return IndexedTreeMap.this.remove(index);
    }

    /**
     * Comparator.
     *
     * @return the comparator<? super k>
     * @see java.util.SortedMap#comparator()
     */
    public Comparator<? super K> comparator()
    {
      return comparator;
    }

    /**
     * First key.
     *
     * @return the k
     * @see java.util.SortedMap#firstKey()
     */
    public K firstKey()
    {
      IndexedTreeMap.Entry<K,V> e = fromStart ? firstEntry() : getCeilEntry(fromKey);
      K first = key(e);
      if (!toEnd && compare(first, toKey) < 0)
        throw new NoSuchElementException();
      return first;
    }

    /**
     * Last key.
     *
     * @return the k
     * @see java.util.SortedMap#lastKey()
     */
    public K lastKey()
    {
      IndexedTreeMap.Entry<K,V> e = toEnd ? lastEntry() : getCeilEntry(toKey);
      K last = key(e);
      if (!fromStart && compare(last, fromKey) < 0)
        throw new NoSuchElementException();
      return last;
    }

    /**
     * Entry iterator.
     *
     * @return the iterator
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#entryIterator()
     */
    public Iterator<Map.Entry<K,V>> entryIterator()
    {
      return entryListIterator();
    }

    /**
     * Key iterator.
     *
     * @return the iterator
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#keyIterator()
     */
    public Iterator<K> keyIterator()
    {
      return keyListIterator();
    }

    /**
     * Value iterator.
     *
     * @return the iterator
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#valueIterator()
     */
    public Iterator<V> valueIterator()
    {
      return valueListIterator();
    }

    /**
     * Entry list iterator.
     *
     * @param index the index
     * @return the list iterator
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#entryListIterator(int)
     */
    public ListIterator<Map.Entry<K,V>> entryListIterator(final int index)
    {
      if (!indexInRange(index)) throw new IllegalArgumentException("index out of range");
      return new ListIterator<Map.Entry<K,V>>()
      {
        private ListIterator<Map.Entry<K,V>> i = IndexedTreeMap.this.entryListIterator(index + fromIndex);

        public boolean hasNext()
        {
          return nextIndex() < size;
        }

        public boolean hasPrevious()
        {
          return previousIndex() >= 0;
        }

        public Map.Entry<K,V> next()
        {
          if (hasNext()) return i.next();
          else throw new NoSuchElementException();
        }

        public Map.Entry<K,V> previous()
        {
          if (hasPrevious()) return i.previous();
          else throw new NoSuchElementException();
        }

        public int nextIndex()
        {
          return i.nextIndex() - fromIndex;
        }

        public int previousIndex()
        {
          return i.previousIndex() - fromIndex;
        }

        public void remove()
        {
          i.remove();
          size--;
        }

        public void set(Map.Entry<K,V> entry)
        {
          i.set(entry);
        }

        public void add(Map.Entry<K,V> entry)
        {
          i.add(entry);
          size++;
        }
      };
    }

    /**
     * Key list iterator.
     *
     * @param index the index
     * @return the list iterator
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#keyListIterator(int)
     */
    public ListIterator<K> keyListIterator(final int index)
    {
      if (!indexInRange(index)) throw new IllegalArgumentException("index out of range");
      return new ListIterator<K>()
      {
        private ListIterator<K> i = IndexedTreeMap.this.keyListIterator(index + fromIndex);

        public boolean hasNext()
        {
          return nextIndex() < size;
        }

        public boolean hasPrevious()
        {
          return previousIndex() >= 0;
        }

        public K next()
        {
          if (hasNext()) return i.next();
          else throw new NoSuchElementException();
        }

        public K previous()
        {
          if (hasPrevious()) return i.previous();
          else throw new NoSuchElementException();
        }

        public int nextIndex()
        {
          return i.nextIndex() - fromIndex;
        }

        public int previousIndex()
        {
          return i.previousIndex() - fromIndex;
        }

        public void remove()
        {
          i.remove();
          size--;
        }

        public void set(K key)
        {
          i.set(key);
        }

        public void add(K key)
        {
          i.add(key);
          size++;
        }
      };
    }

    /**
     * Value list iterator.
     *
     * @param index the index
     * @return the list iterator
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#valueListIterator(int)
     */
    public ListIterator<V> valueListIterator(final int index)
    {
      if (!indexInRange(index)) throw new IllegalArgumentException("index out of range");
      return new ListIterator<V>()
      {
        private ListIterator<V> i = IndexedTreeMap.this.valueListIterator(index + fromIndex);

        public boolean hasNext()
        {
          return nextIndex() < size;
        }

        public boolean hasPrevious()
        {
          return previousIndex() >= 0;
        }

        public V next()
        {
          if (hasNext()) return i.next();
          else throw new NoSuchElementException();
        }

        public V previous()
        {
          if (hasPrevious()) return i.previous();
          else throw new NoSuchElementException();
        }

        public int nextIndex()
        {
          return i.nextIndex() - fromIndex;
        }

        public int previousIndex()
        {
          return i.previousIndex() - fromIndex;
        }

        public void remove()
        {
          i.remove();
          size--;
        }

        public void set(V value)
        {
          i.set(value);
        }

        public void add(V value)
        {
          i.add(value);
          size++;
        }
      };
    }

    /**
     * Entry set.
     *
     * @return the sets the
     * @see java.util.AbstractMap#entrySet()
     */
    public Set<Map.Entry<K,V>> entrySet()
    {
      return entrySet;
    }

    /**
     * Sub map.
     *
     * @param fromKey the from key
     * @param toKey the to key
     * @return the indexed map
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#subMap(java.lang.Object, java.lang.Object)
     */
    public IndexedMap<K,V> subMap(K fromKey, K toKey)
    {
      if (!keyInRange2(fromKey)) throw new IllegalArgumentException("fromKey out of range");
      if (!keyInRange2(toKey)) throw new IllegalArgumentException("toKey out of range");
      return new SubMap(fromKey, toKey);
    }

    /**
     * Sub map.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the indexed map
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#subMap(int, int)
     */
    public IndexedMap<K,V> subMap(int fromIndex, int toIndex)
    {
      if (!indexInRange2(fromIndex)) throw new IllegalArgumentException("fromIndex out of range");
      if (!indexInRange2(toIndex)) throw new IllegalArgumentException("toIndex out of range");
      return new SubMap(fromIndex, toIndex);
    }

    /**
     * Sub map.
     *
     * @param fromKey the from key
     * @param toIndex the to index
     * @return the indexed map
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#subMap(java.lang.Object, int)
     */
    public IndexedMap<K,V> subMap(K fromKey, int toIndex)
    {
      if (!keyInRange2(fromKey)) throw new IllegalArgumentException("fromKey out of range");
      if (!indexInRange2(toIndex)) throw new IllegalArgumentException("toIndex out of range");
      return new SubMap(fromKey, toIndex);
    }

    /**
     * Sub map.
     *
     * @param fromIndex the from index
     * @param toKey the to key
     * @return the indexed map
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#subMap(int, java.lang.Object)
     */
    public IndexedMap<K,V> subMap(int fromIndex, K toKey)
    {
      if (!indexInRange2(fromIndex)) throw new IllegalArgumentException("fromIndex out of range");
      if (!keyInRange2(toKey)) throw new IllegalArgumentException("toKey out of range");
      return new SubMap(fromIndex, toKey);
    }

    /**
     * Head map.
     *
     * @param toKey the to key
     * @return the indexed map
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#headMap(java.lang.Object)
     */
    public IndexedMap<K,V> headMap(K toKey)
    {
      if (!keyInRange2(toKey)) throw new IllegalArgumentException("toKey out of range");
      if (fromIndexBased)
      {
        return new SubMap(fromStart, fromIndex, false, toKey);
      }
      else
      {
        return new SubMap(fromStart, fromKey, false, toKey);
      }
    }

    /**
     * Head map.
     *
     * @param toIndex the to index
     * @return the indexed map
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#headMap(int)
     */
    public IndexedMap<K,V> headMap(int toIndex)
    {
      if (!indexInRange2(toIndex)) throw new IllegalArgumentException("toIndex out of range");
      if (fromIndexBased)
      {
        return new SubMap(fromStart, fromIndex, false, toIndex);
      }
      else
      {
        return new SubMap(fromStart, fromKey, false, toIndex);
      }
    }

    /**
     * Tail map.
     *
     * @param fromKey the from key
     * @return the indexed map
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#tailMap(java.lang.Object)
     */
    public IndexedMap<K,V> tailMap(K fromKey)
    {
      if (!keyInRange2(fromKey)) throw new IllegalArgumentException("fromKey out of range");
      if (toIndexBased)
      {
        return new SubMap(false, fromKey, toEnd, toIndex);
      }
      else
      {
        return new SubMap(false, fromKey, toEnd, toKey);
      }
    }

    /**
     * Tail map.
     *
     * @param fromIndex the from index
     * @return the indexed map
     * @see com.cinnober.ciguan.datasource.impl.IndexedMap#tailMap(int)
     */
    public IndexedMap<K,V> tailMap(int fromIndex)
    {
      if (!indexInRange2(fromIndex)) throw new IllegalArgumentException("fromIndex out of range");
      if (toIndexBased)
      {
        return new SubMap(false, fromIndex, toEnd, toIndex);
      }
      else
      {
        return new SubMap(false, fromIndex, toEnd, toKey);
      }
    }

    /**
     * Key in range.
     *
     * @param key the key
     * @return true, if successful
     */
    private boolean keyInRange(K key)
    {
      return (fromStart || compare(key, fromKey) >= 0) &&
        (toEnd || compare(key, toKey) <  0);
    }

    // This form allows the high endpoint (as well as all legit keys)
    /**
     * Key in range2.
     *
     * @param key the key
     * @return true, if successful
     */
    private boolean keyInRange2(K key)
    {
      return (fromStart || compare(key, fromKey) >= 0) &&
        (toEnd || compare(key, toKey) <= 0);
    }

    /**
     * Index in range.
     *
     * @param index the index
     * @return true, if successful
     */
    private boolean indexInRange(int index)
    {
      return (fromStart || index >= fromIndex) &&
        (toEnd || index < toIndex);
    }

    /**
     * Index in range2.
     *
     * @param index the index
     * @return true, if successful
     */
    private boolean indexInRange2(int index)
    {
      return (fromStart || index >= fromIndex) &&
        (toEnd || index <= toIndex);
    }

    /**
     * The Class EntrySetView.
     */
    private class EntrySetView extends AbstractSet<Map.Entry<K,V>>
    {

      /** The size mod count. */
      private transient int size = -1, sizeModCount;

      /**
       * Size.
       *
       * @return the int
       * @see java.util.AbstractCollection#size()
       */
      public int size()
      {
        if (size == -1 || sizeModCount != IndexedTreeMap.this.modCount)
        {
          size = 0;
          sizeModCount = IndexedTreeMap.this.modCount;
          Iterator i = iterator();
          while (i.hasNext())
          {
            size++;
            i.next();
          }
        }
        return size;
      }

      /**
       * Checks if is empty.
       *
       * @return true, if is empty
       * @see java.util.AbstractCollection#isEmpty()
       */
      public boolean isEmpty()
      {
        return !iterator().hasNext();
      }

      /**
       * Contains.
       *
       * @param o the o
       * @return true, if successful
       * @see java.util.AbstractCollection#contains(java.lang.Object)
       */
      public boolean contains(Object o)
      {
        if (!(o instanceof Map.Entry)) return false;
        Map.Entry<K,V> entry = (Map.Entry<K,V>)o;
        K key = entry.getKey();
        if (!keyInRange(key)) return false;
        IndexedTreeMap.Entry node = getEntry(key);
        return node != null && valEquals(node.getValue(), entry.getValue());
      }

      /**
       * Removes the.
       *
       * @param o the o
       * @return true, if successful
       * @see java.util.AbstractCollection#remove(java.lang.Object)
       */
      public boolean remove(Object o)
      {
        if (!(o instanceof Map.Entry)) return false;
        Map.Entry<K,V> entry = (Map.Entry<K,V>)o;
        K key = entry.getKey();
        if (!keyInRange(key)) return false;
        IndexedTreeMap.Entry<K,V> node = getEntry(key);
        if (node != null && valEquals(node.getValue(), entry.getValue()))
        {
          deleteEntry(node);
          return true;
        }
        return false;
      }

      /**
       * Iterator.
       *
       * @return the iterator
       * @see java.util.AbstractCollection#iterator()
       */
      public Iterator<Map.Entry<K,V>> iterator()
      {
        return new SubMapEntryIterator(
          (fromStart ? firstEntry() : getCeilEntry(fromKey)),
          (toEnd     ? null         : getCeilEntry(toKey)));
      }
    }
  }

  /**
   * Returns the first Entry in the IndexedTreeMap (according to the IndexedTreeMap's
   * key-sort function).  Returns null if the IndexedTreeMap is empty.
   *
   * @return the entry
   */
  private Entry<K,V> firstEntry()
  {
    Entry<K,V> p = root;
    if (p != null) while (p.left != null) p = p.left;
    return p;
  }

  /**
   * Returns the last Entry in the IndexedTreeMap (according to the IndexedTreeMap's
   * key-sort function).  Returns null if the IndexedTreeMap is empty.
   *
   * @return the entry
   */
  private Entry<K,V> lastEntry()
  {
    Entry<K,V> p = root;
    if (p != null) while (p.right != null) p = p.right;
    return p;
  }

  /**
   * Returns the successor of the specified Entry, or null if no such.
   *
   * @param t the t
   * @return the entry
   */
  private Entry<K,V> successor(Entry<K,V> t)
  {
    if (t == null) return null;
    else if (t.right != null)
    {
      Entry<K,V> p = t.right;
      while (p.left != null) p = p.left;
      return p;
    }
    else
    {
      Entry<K,V> p = t.parent;
      Entry<K,V> ch = t;
      while (p != null && ch == p.right)
      {
        ch = p;
        p = p.parent;
      }
      return p;
    }
  }

  /**
   * Predecessor.
   *
   * @param t the t
   * @return the entry
   */
  private Entry<K,V> predecessor(Entry<K,V> t)
  {
    if (t == null) return null;
    else if (t.left != null)
    {
      Entry<K,V> p = t.left;
      while (p.right != null) p = p.right;
      return p;
    }
    else
    {
      Entry<K,V> p = t.parent;
      Entry<K,V> ch = t;
      while (p != null && ch == p.left)
      {
        ch = p;
        p = p.parent;
      }
      return p;
    }
  }

  /**
   * Balancing operations.
   *
   * Implementations of rebalancings during insertion and deletion are
   * slightly different than the CLR version.  Rather than using dummy
   * nilnodes, we use a set of accessors that deal properly with null.  They
   * are used to avoid messiness surrounding nullness checks in the main
   * algorithms.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param p the
   * @return true, if successful
   */
  private static <K,V> boolean colourOf(Entry<K,V> p)
  {
    return (p == null ? BLACK : p.colour);
  }

  /**
   * Parent of.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param p the
   * @return the entry
   */
  private static <K,V> Entry<K,V> parentOf(Entry<K,V> p)
  {
    return (p == null ? null : p.parent);
  }

  /**
   * Sets the colour.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param p the
   * @param c the
   */
  private static <K,V> void setColour(Entry<K,V> p, boolean c)
  {
    if (p != null) p.colour = c;
  }

  /**
   * Left of.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param p the
   * @return the entry
   */
  private static <K,V> Entry<K,V> leftOf(Entry<K,V> p)
  {
    return (p == null ? null : p.left);
  }

  /**
   * Right of.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param p the
   * @return the entry
   */
  private static <K,V> Entry<K,V> rightOf(Entry<K,V> p)
  {
    return (p == null ? null : p.right);
  }

  /**
   *  From CLR *.
   *
   * @param p the
   */
  private void rotateLeft(Entry<K,V> p)
  {
    Entry<K,V> r = p.right;
    p.right = r.left;
    p.rightNodes = r.leftNodes;

    if (r.left != null) r.left.parent = p;
    r.parent = p.parent;

    if (p.parent == null) root = r;
    else if (p.parent.left == p) p.parent.left = r;
    else p.parent.right = r;

    r.left = p;
    r.leftNodes = p.leftNodes + p.rightNodes + 1;
    p.parent = r;
  }

  /**
   *  From CLR *.
   *
   * @param p the
   */
  private void rotateRight(Entry<K,V> p)
  {
    Entry<K,V> l = p.left;
    p.left = l.right;
    p.leftNodes = l.rightNodes;

    if (l.right != null) l.right.parent = p;
    l.parent = p.parent;

    if (p.parent == null) root = l;
    else if (p.parent.right == p) p.parent.right = l;
    else p.parent.left = l;

    l.right = p;
    l.rightNodes = p.leftNodes + p.rightNodes + 1;
    p.parent = l;
  }

  /**
   * Fix after insertion.
   *
   * @param x the x
   */
  private void fixAfterInsertion(Entry<K,V> x)
  {
    x.colour = RED;

    while (x != null && x != root && x.parent.colour == RED)
    {
      if (parentOf(x) == leftOf(parentOf(parentOf(x))))
      {
        Entry<K,V> y = rightOf(parentOf(parentOf(x)));
        if (colourOf(y) == RED)
        {
          setColour(parentOf(x), BLACK);
          setColour(y, BLACK);
          setColour(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        }
        else
        {
          if (x == rightOf(parentOf(x)))
          {
            x = parentOf(x);
            rotateLeft(x);
          }
          setColour(parentOf(x), BLACK);
          setColour(parentOf(parentOf(x)), RED);
          if (parentOf(parentOf(x)) != null) rotateRight(parentOf(parentOf(x)));
        }
      }
      else
      {
        Entry<K,V> y = leftOf(parentOf(parentOf(x)));
        if (colourOf(y) == RED)
        {
          setColour(parentOf(x), BLACK);
          setColour(y, BLACK);
          setColour(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        }
        else
        {
          if (x == leftOf(parentOf(x)))
          {
            x = parentOf(x);
            rotateRight(x);
          }
          setColour(parentOf(x),  BLACK);
          setColour(parentOf(parentOf(x)), RED);
          if (parentOf(parentOf(x)) != null) rotateLeft(parentOf(parentOf(x)));
        }
      }
    }
    root.colour = BLACK;
  }

  /**
   * Returns this map's entry for the given key, or <tt>null</tt> if the map
   * does not contain an entry for the key.
   *
   * @param key the key
   * @return this map's entry for the given key, or <tt>null</tt> if the map
   *                does not contain an entry for the key.
   */
  private Entry<K,V> getEntry(Object key)
  {
    Entry<K,V> p = root;
    K k = (K)key;
    while (p != null)
    {
      int cmp = compare(k, p.key);
      if (cmp == 0) return p;
      else if (cmp < 0) p = p.left;
      else p = p.right;
    }
    return null;
  }

  /**
   * Gets the entry.
   *
   * @param index the index
   * @return the entry
   */
  private Entry<K,V> getEntry(int index)
  {
    Entry<K,V> p = root;
    int pIndex = p.leftNodes;
    while (pIndex != index)
    {
      if (index < pIndex)
      {
        p = p.left;
        pIndex -= p.rightNodes + 1;
      }
      else
      {
        p = p.right;
        pIndex += p.leftNodes + 1;
      }
    }
    return p;
  }

  /**
   * Gets the entry corresponding to the specified key; if no such entry
   * exists, returns the entry for the least key greater than the specified
   * key; if no such entry exists (i.e., the greatest key in the Tree is less
   * than the specified key), returns <tt>null</tt>.
   *
   * @param key the key
   * @return the ceil entry
   */
  private Entry<K,V> getCeilEntry(K key)
  {
    Entry<K,V> p = root;
    if (p == null) return null;

    while (true)
    {
      int cmp = compare(key, p.key);
      if (cmp == 0) return p;
      else if (cmp < 0)
      {
        if (p.left != null) p = p.left;
        else return p;
      }
      else
      {
        if (p.right != null) p = p.right;
        else
        {
          Entry<K,V> parent = p.parent;
          Entry<K,V> ch = p;
          while (parent != null && ch == parent.right)
          {
            ch = parent;
            parent = parent.parent;
          }
          return parent;
        }
      }
    }
  }

  /**
   * Returns the entry for the greatest key less than the specified key; if
   * no such entry exists (i.e., the least key in the Tree is greater than
   * the specified key), returns <tt>null</tt>.
   *
   * @param key the key
   * @return the preceding entry
   */
  @SuppressWarnings("unused")
private Entry<K,V> getPrecedingEntry(K key)
  {
    Entry<K,V> p = root;
    if (p == null) return null;

    while (true)
    {
      int cmp = compare(key, p.key);
      if (cmp > 0)
      {
        if (p.right != null) p = p.right;
        else return p;
      }
      else
      {
        if (p.left != null) p = p.left;
        else
        {
          Entry<K,V> parent = p.parent;
          Entry<K,V> ch = p;
          while (parent != null && ch == parent.left)
          {
            ch = parent;
            parent = parent.parent;
          }
          return parent;
        }
      }
    }
  }

  /**
   * Delete node p, and then rebalance the tree.
   *
   * @param p the
   */
  private void deleteEntry(Entry<K,V> p)
  {
    decrementSize();

    if (p.left != null && p.right != null)
    {
      Entry<K,V> s = successor(p);
      p.key = s.key;
      p.value = s.value;
      p = s;
    }

    Entry<K,V> replacement = (p.left != null ? p.left : p.right);
    if (replacement != null)
    {
      replacement.parent = p.parent;
      if (p.parent == null) root = replacement;
      else if (p == p.parent.left) p.parent.left = replacement;
      else p.parent.right = replacement;

      // Decrement the node counts in the tree.
      Entry<K,V> q = replacement;
      while (q.parent != null)
      {
        if (q == q.parent.left) q.parent.leftNodes--;
        else if (q == q.parent.right) q.parent.rightNodes--;

        q = q.parent;
      }

      p.left = p.right = p.parent = null;

      if (p.colour == BLACK) fixAfterDeletion(replacement);
    }
    else if (p.parent == null) root = null;
    else
    {
      if (p.colour == BLACK) fixAfterDeletion(p);

      if (p.parent != null)
      {
        Entry<K,V> q = p;
        while (q.parent != null)
        {
          if (q == q.parent.left) q.parent.leftNodes--;
          else if (q == q.parent.right) q.parent.rightNodes--;

          q = q.parent;
        }

        if (p == p.parent.left) p.parent.left = null;
        else if (p == p.parent.right) p.parent.right = null;

        p.parent = null;
      }
    }
  }

  /**
   *  From CLR *.
   *
   * @param x the x
   */
  private void fixAfterDeletion(Entry<K,V> x)
  {
    while (x != root && colourOf(x) == BLACK)
    {
      if (x == leftOf(parentOf(x)))
      {
        Entry<K,V> sib = rightOf(parentOf(x));

        if (colourOf(sib) == RED)
        {
          setColour(sib, BLACK);
          setColour(parentOf(x), RED);
          rotateLeft(parentOf(x));
          sib = rightOf(parentOf(x));
        }

        if (colourOf(leftOf(sib))  == BLACK && colourOf(rightOf(sib)) == BLACK)
        {
          setColour(sib,  RED);
          x = parentOf(x);
        }
        else
        {
          if (colourOf(rightOf(sib)) == BLACK)
          {
            setColour(leftOf(sib), BLACK);
            setColour(sib, RED);
            rotateRight(sib);
            sib = rightOf(parentOf(x));
          }
          setColour(sib, colourOf(parentOf(x)));
          setColour(parentOf(x), BLACK);
          setColour(rightOf(sib), BLACK);
          rotateLeft(parentOf(x));
          x = root;
        }
      }
      else
      {
        Entry<K,V> sib = leftOf(parentOf(x));

        if (colourOf(sib) == RED)
        {
          setColour(sib, BLACK);
          setColour(parentOf(x), RED);
          rotateRight(parentOf(x));
          sib = leftOf(parentOf(x));
        }

        if (colourOf(rightOf(sib)) == BLACK && colourOf(leftOf(sib)) == BLACK)
        {
          setColour(sib,  RED);
          x = parentOf(x);
        }
        else
        {
          if (colourOf(leftOf(sib)) == BLACK)
          {
            setColour(rightOf(sib), BLACK);
            setColour(sib, RED);
            rotateLeft(sib);
            sib = leftOf(parentOf(x));
          }
          setColour(sib, colourOf(parentOf(x)));
          setColour(parentOf(x), BLACK);
          setColour(leftOf(sib), BLACK);
          rotateRight(parentOf(x));
          x = root;
        }
      }
    }
    setColour(x, BLACK);
  }

  /**
   * Save the state of the <tt>IndexedTreeMap</tt> instance to a stream (i.e.,
   * serialize it).
   *
   * @param s the s
   * @throws IOException Signals that an I/O exception has occurred.
   * @serialData The <i>size</i> of the IndexedTreeMap (the number of key-value
   *             mappings) is emitted (int), followed by the key (Object)
   *             and value (Object) for each key-value mapping represented
   *             by the IndexedTreeMap. The key-value mappings are emitted in
   *             key-order (as determined by the IndexedTreeMap's Comparator,
   *             or by the keys' natural ordering if the IndexedTreeMap has no
   *             Comparator).
   */
  private void writeObject(java.io.ObjectOutputStream s)
  throws java.io.IOException
  {
    s.defaultWriteObject();
    s.writeInt(size);
    for (Iterator<Map.Entry<K,V>> i = entrySet().iterator(); i.hasNext(); )
    {
      Map.Entry<K,V> e = i.next();
      s.writeObject(e.getKey());
      s.writeObject(e.getValue());
    }
  }

  /**
   * Reconstitute the <tt>IndexedTreeMap</tt> instance from a stream (i.e.,
   * deserialize it).
   *
   * @param s the s
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ClassNotFoundException the class not found exception
   */
  private void readObject(final java.io.ObjectInputStream s)
  throws java.io.IOException, ClassNotFoundException
  {
    s.defaultReadObject();
    int size = s.readInt();
    buildFromSorted(size, null, s, null);
  }

  /**
   *  Intended to be called only from TreeSet.readObject *
   *
   * @param size the size
   * @param s the s
   * @param defaultVal the default value
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ClassNotFoundException the class not found exception
   */
  void readTreeList(int size, java.io.ObjectInputStream s, V defaultVal)
  throws java.io.IOException, ClassNotFoundException
  {
    buildFromSorted(size, null, s, defaultVal);
  }

  /**
   *  Intended to be called only from TreeSet.addAll *
   *
   * @param list the list
   * @param defaultVal the default value
   */
  void addAllForTreeList(SortedList<Map.Entry<K,V>> list, V defaultVal)
  {
    try
    {
      buildFromSorted(list.size(), list.iterator(), null, defaultVal);
    }
    catch (java.io.IOException cannotHappen)
    {
    }
    catch (ClassNotFoundException cannotHappen)
    {
    }
  }

  /**
   * Linear time tree building algorithm from sorted data.  Can accept keys
   * and/or values from iterator or stream. This leads to too many
   * parameters, but seems better than alternatives.  The four formats
   * that this method accepts are:
   *
   *    1) An iterator of Map.Entries.  (it != null, defaultVal == null).
   *    2) An iterator of keys.         (it != null, defaultVal != null).
   *    3) A stream of alternating serialized keys and values.
   *                                   (it == null, defaultVal == null).
   *    4) A stream of serialized keys. (it == null, defaultVal != null).
   *
   * It is assumed that the comparator of the IndexedTreeMap is already set prior
   * to calling this method.
   *
   * @param size the number of keys (or key-value pairs) to be read from
   *        the iterator or stream.
   * @param it If non-null, new entries are created from entries
   *        or keys read from this iterator.
   * @param str If non-null, new entries are created from keys and
   *        possibly values read from this stream in serialized form.
   *        Exactly one of it and str should be non-null.
   * @param defaultVal if non-null, this default value is used for
   *        each value in the map.  If null, each value is read from
   *        iterator or stream, as described above.
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ClassNotFoundException propagated from readObject.
   *         This cannot occur if str is null.
   */
  private void buildFromSorted(int size, Iterator it,
    java.io.ObjectInputStream str, V defaultVal)
  throws java.io.IOException, ClassNotFoundException
  {
    this.size = size;
    root = buildFromSorted(0, 0, size - 1, computeRedLevel(size),
      it, str, defaultVal);
  }

  /**
   * Recursive "helper method" that does the real work of the
   * of the previous method.  Identically named parameters have
   * identical definitions.  Additional parameters are documented below.
   * It is assumed that the comparator and size fields of the IndexedTreeMap are
   * already set prior to calling this method.  (It ignores both fields.)
   *
   * @param level the current level of tree. Initial call should be 0.
   * @param lo the first element index of this subtree. Initial should be 0.
   * @param hi the last element index of this subtree.  Initial should be
   *              size-1.
   * @param redLevel the level at which nodes should be red.
   *        Must be equal to computeRedLevel for tree of this size.
   * @param it the it
   * @param str the str
   * @param defaultVal the default val
   * @return the entry
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ClassNotFoundException the class not found exception
   */
  private final Entry<K,V> buildFromSorted(int level, int lo, int hi,
    int redLevel, Iterator it, java.io.ObjectInputStream str, V defaultVal)
  throws java.io.IOException, ClassNotFoundException
  {
    if (hi < lo) return null;
    int mid = (lo + hi) / 2;

    Entry<K,V> left = null;
    if (lo < mid) left = buildFromSorted(level + 1, lo, mid - 1, redLevel,
      it, str, defaultVal);

    K key;
    V value;
    if (it != null)
    {
      if (defaultVal == null)
      {
        IndexedMap.Entry<K,V> entry = (IndexedMap.Entry<K,V>)it.next();
        key = entry.getKey();
        value = entry.getValue();
      }
      else
      {
        key = (K)it.next();
        value = defaultVal;
      }
    }
    else
    {
      key = (K)str.readObject();
      value = (defaultVal != null ? defaultVal : (V)str.readObject());
    }

    Entry<K,V> middle = new Entry<K,V>(key, value, null);

    if (level == redLevel) middle.colour = RED;

    if (left != null)
    {
      middle.left = left;
      left.parent = middle;
    }

    if (mid < hi)
    {
      Entry<K,V> right = buildFromSorted(level + 1, mid + 1, hi, redLevel,
        it, str, defaultVal);
      middle.right = right;
      right.parent = middle;
    }

    return middle;
  }

  /**
   * Find the level down to which to assign all nodes BLACK.  This is the
   * last `full' level of the complete binary tree produced by
   * buildTree. The remaining nodes are colored RED. (This makes a `nice'
   * set of color assignments wrt future insertions.) This level number is
   * computed by finding the number of splits needed to reach the zeroeth
   * node.  (The answer is ~lg(N), but in any case must be computed by same
   * quick O(lg(N)) loop.)
   *
   * @param sz the sz
   * @return the int
   */
  private static int computeRedLevel(int sz)
  {
    int level = 0;
    for (int m = sz - 1; m >= 0; m = m / 2 - 1) level++;
    return level;
  }

  /**
   * IndexedTreeMap Iterator.
   *
   * @param <T> the generic type
   */
  private abstract class PrivateEntryIterator<T>
  implements Iterator<T>, ListIterator<T>
  {

    /** The last. */
    final Entry<K,V> last = new Entry<K,V>(null, null, null);

    /** The expected mod count. */
    private int expectedModCount = IndexedTreeMap.this.modCount;

    /** The last returned. */
    private Entry<K,V> lastReturned = null;

    /** The cursor. */
    Entry<K,V> cursor = firstEntry();

    /**
     * Instantiates a new private entry iterator.
     */
    PrivateEntryIterator()
    {
      cursor = firstEntry();
    }

    /**
     * Instantiates a new private entry iterator.
     *
     * @param first the first
     */
    PrivateEntryIterator(Entry<K,V> first)
    {
      cursor = first;
    }

    /**
     * Instantiates a new private entry iterator.
     *
     * @param index the index
     */
    PrivateEntryIterator(int index)
    {
      cursor = getEntry(index);
    }

    /**
     * Checks for next.
     *
     * @return {@code true} if successful
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext()
    {
      return (cursor != null && cursor != last);
    }

    /**
     * Checks for previous.
     *
     * @return {@code true} if successful
     * @see java.util.ListIterator#hasPrevious()
     */
    public boolean hasPrevious()
    {
      return (cursor != null && cursor != firstEntry());
    }

    /**
     * Returns the next entry.
     *
     * @return the entry
     */
    final Entry<K,V> nextEntry()
    {
      checkForComodification();

      if (cursor == null || cursor == last) throw new NoSuchElementException();
      lastReturned = cursor;
      cursor = successor(cursor);
      if (cursor == null) cursor = last;
      return lastReturned;
    }

    /**
     * Returns the previous entry.
     *
     * @return the entry
     */
    final Entry<K,V> previousEntry()
    {
      checkForComodification();

      if (cursor == null || cursor == firstEntry()) throw new NoSuchElementException();
      if (cursor == last) lastReturned = cursor = lastEntry();
      else lastReturned = cursor = predecessor(cursor);
      return lastReturned;
    }

    /**
     * Returns the next index.
     *
     * @return the index
     * @see java.util.ListIterator#nextIndex()
     */
    public int nextIndex()
    {
      if (cursor == null) return 0;
      else if (cursor == last) return size;
      return cursor.getIndex();
    }

    /**
     * Returns the previous index.
     *
     * @return the index
     * @see java.util.ListIterator#previousIndex()
     */
    public int previousIndex()
    {
      if (cursor == null) return -1;
      else if (cursor == last) return size - 1;
      return cursor.getIndex() - 1;
    }

    /**
     * Puts the key and value in the map.
     *
     * @param key the key
     * @param value the value
     */
    final void put(K key, V value)
    {
      checkForComodification();

      IndexedTreeMap.this.put(key, value);
      lastReturned = null;
      expectedModCount = modCount;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    final void setValue(V value)
    {
      checkForComodification();

      if (lastReturned == null) throw new IllegalStateException();
      lastReturned.setValue(value);
      lastReturned = null;
      expectedModCount = modCount;
    }

    /**
     * {@inheritDoc}
     */
    public void remove()
    {
      checkForComodification();

      if (lastReturned == null) throw new IllegalStateException();
      if (lastReturned.left != null && lastReturned.right != null) cursor = lastReturned;
      deleteEntry(lastReturned);
      expectedModCount++;
      lastReturned = null;
    }

    /**
     * Check for comodification.
     */
    final void checkForComodification()
    {
      if (modCount != expectedModCount) throw new ConcurrentModificationException();
    }
  }

  /**
   * A simple EntryIterator implementation.
   */
  private class EntryIterator extends PrivateEntryIterator<Map.Entry<K,V>>
  {

    /**
     * Instantiates a new entry iterator.
     */
    public EntryIterator()
    {
      super();
    }

    /**
     * Instantiates a new entry iterator.
     *
     * @param index the index
     */
    public EntryIterator(int index)
    {
      super(index);
    }

    /**
     * Next entry.
     *
     * @return the map. entry
     * @see java.util.Iterator#next()
     */
    public Map.Entry<K,V> next()
    {
      return nextEntry();
    }

    /**
     * Previous entry.
     *
     * @return the map. entry
     * @see java.util.ListIterator#previous()
     */
    public Map.Entry<K,V> previous()
    {
      return previousEntry();
    }

    /**
     * Adds the entry.
     *
     * @param entry the entry
     * @see java.util.ListIterator#add(java.lang.Object)
     */
    public void add(Map.Entry<K,V> entry)
    {
      put(entry.getKey(), entry.getValue());
    }

    /**
     * Sets the entry.
     *
     * @param entry the entry
     * @see java.util.ListIterator#set(java.lang.Object)
     */
    public void set(Map.Entry<K,V> entry)
    {
      put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * A simple KeyIterator implementation.
   */
  private class KeyIterator extends PrivateEntryIterator<K>
  {

    /**
     * Instantiates a new key iterator.
     */
    public KeyIterator()
    {
      super();
    }

    /**
     * Instantiates a new key iterator.
     *
     * @param index the index
     */
    public KeyIterator(int index)
    {
      super(index);
    }

    /**
     * Returns the next key.
     *
     * @return the k
     * @see java.util.Iterator#next()
     */
    public K next()
    {
      return nextEntry().key;
    }

    /**
     * Returns the previous key.
     *
     * @return the k
     * @see java.util.ListIterator#previous()
     */
    public K previous()
    {
      return previousEntry().key;
    }

    /**
     * Adds the key.
     *
     * @param key the key
     * @see java.util.ListIterator#add(java.lang.Object)
     * @throws UnsupportedOperationException
     */
    public void add(K key)
    {
      throw new UnsupportedOperationException();
    }

    /**
     * Sets the key.
     *
     * @param key the key
     * @see java.util.ListIterator#set(java.lang.Object)
     * @throws UnsupportedOperationException
     */
    public void set(K key)
    {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * The Class ValueIterator.
   */
  private class ValueIterator extends PrivateEntryIterator<V>
  {

    /**
     * Instantiates a new value iterator.
     */
    public ValueIterator()
    {
      super();
    }

    /**
     * Instantiates a new value iterator.
     *
     * @param index the index
     */
    public ValueIterator(int index)
    {
      super(index);
    }

    /**
     * Returns the next value.
     *
     * @return the v
     * @see java.util.Iterator#next()
     */
    public V next()
    {
      return nextEntry().value;
    }

    /**
     * Returns the previous value.
     *
     * @return the v
     * @see java.util.ListIterator#previous()
     */
    public V previous()
    {
      return previousEntry().value;
    }

    /**
     * Adds the value.
     *
     * @param value the value
     * @see java.util.ListIterator#add(java.lang.Object)
     */
    public void add(V value)
    {
      throw new UnsupportedOperationException();
    }

    /**
     * Sets the value.
     *
     * @param value the value
     * @see java.util.ListIterator#set(java.lang.Object)
     */
    public void set(V value)
    {
      setValue(value);
    }
  }

  /**
   * The Class SubMapEntryIterator.
   */
  private class SubMapEntryIterator extends PrivateEntryIterator<Map.Entry<K,V>>
  {

    /** The first excluded key. */
    private final K firstExcludedKey;

    /** The first entry. */
    private final Entry<K,V> first;

    /**
     * Instantiates a new sub map entry iterator.
     *
     * @param first the first entry
     * @param firstExcluded the first excluded entry
     */
    SubMapEntryIterator(Entry<K,V> first, Entry<K,V> firstExcluded)
    {
      super(first);
      this.first = first;
      firstExcludedKey = (firstExcluded == null ? null : firstExcluded.key);
    }

    /**
     * Checks for next map entry.
     *
     * @return {@code true} if successful
     * @see com.cinnober.ciguan.datasource.impl.IndexedTreeMap.PrivateEntryIterator#hasNext()
     */
    public boolean hasNext()
    {
      return cursor != null && cursor != last && cursor.key != firstExcludedKey;
    }

    /**
     * Checks for previous map entry.
     *
     * @return {@code true} if successful
     * @see com.cinnober.ciguan.datasource.impl.IndexedTreeMap.PrivateEntryIterator#hasPrevious()
     */
    public boolean hasPrevious()
    {
      return cursor != null && cursor != first;
    }

    /**
     * Returns the next map entry.
     *
     * @return the map. entry
     * @see java.util.Iterator#next()
     */
    public Map.Entry<K,V> next()
    {
      if (cursor == null || cursor == last || cursor.key == firstExcludedKey)
        throw new NoSuchElementException();
      return nextEntry();
    }

    /**
     * Returns the previous map entry.
     *
     * @return the map. entry
     * @see java.util.ListIterator#previous()
     */
    public Map.Entry<K,V> previous()
    {
      if (cursor == null || cursor == first)
        throw new NoSuchElementException();
      return nextEntry();
    }

    /**
     * Adds the specified map entry.
     *
     * @param entry the entry
     * @see java.util.ListIterator#add(java.lang.Object)
     */
    public void add(Map.Entry<K,V> entry)
    {
      put(entry.getKey(), entry.getValue());
    }

    /**
     * Sets the map entry.
     *
     * @param entry the entry
     * @see java.util.ListIterator#set(java.lang.Object)
     */
    public void set(Map.Entry<K,V> entry)
    {
      put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Node in the Tree.  Doubles as a means to pass key-value pairs back to
   * user (see IndexedMap.Entry).
   *
   * @param <K> the key type
   * @param <V> the value type
   */
  static class Entry<K,V> implements IndexedMap.Entry<K,V>
  {

    /** The key. */
    K key;

    /** The value. */
    V value;

    /** The parent. */
    Entry<K,V> parent;

    /** The left. */
    Entry<K,V> left = null;

    /** The right. */
    Entry<K,V> right = null;

    /** The left nodes. */
    int leftNodes = 0;

    /** The right nodes. */
    int rightNodes = 0;

    /** The colour. */
    boolean colour = BLACK;

    /**
     * Make a new cell with given key, value, and parent, and with
     * <tt>null</tt> child links, and BLACK color.
     *
     * @param key the key
     * @param value the value
     * @param parent the parent
     */
    Entry(K key, V value, Entry<K,V> parent)
    {
      this.key = key;
      this.value = value;
      this.parent = parent;
    }

    /**
     * Returns the key.
     * @return the key.
     */
    public K getKey()
    {
      return key;
    }

    /**
     * Returns the value associated with the key.
     * @return the value associated with the key.
     */
    public V getValue()
    {
      return value;
    }

    /**
     * Replaces the value currently associated with the key with the given
     * value.
     *
     * @param value the value
     * @return the value associated with the key before this method was
     * called.
     */
    public V setValue(V value)
    {
      V oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o)
    {
      if (!(o instanceof Map.Entry)) return false;
      Map.Entry e = (Map.Entry)o;

      return valEquals(key, e.getKey()) && valEquals(value, e.getValue());
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
      int keyHash = (key == null ? 0 : key.hashCode());
      int valueHash = (value == null ? 0 : value.hashCode());
      return keyHash ^ valueHash;
    }

    /**
     * {@inheritDoc}
     */
    public int getIndex()
    {
      int tIndex = leftNodes;
      Entry<K,V> p = parent;
      Entry<K,V> ch = this;
      while (true)
      {
        if (p == null) return tIndex;

        if (p.left == ch)
        {
          ch = p;
          p = p.parent;
        }
        else
        {
          tIndex += p.leftNodes + 1;
          ch = p;
          p = p.parent;
        }
      }
    }

    /**
     * {@inheritDoc}
     *
     * Returns the key + "=" + value string.
     */
    public String toString()
    {
      return key + "=" + value;
    }
  }
}
