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

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

/**
 * This class implements the <tt>OrderedList</tt> interface, backed by an
 * <tt>IndexedTreeMap</tt> instance.  This class guarantees that the sorted list
 * will be in ascending element order, sorted according to the <i>natural
 * order</i> of the elements (see <tt>Comparable</tt>), or by the comparator
 * provided at list creation time, depending on which constructor is used.<p>
 *
 * This implementation provides guaranteed log(n) time cost for the basic
 * operations (<tt>add</tt>, <tt>remove</tt> and <tt>contains</tt>).<p>
 *
 * Note that the ordering maintained by a list (whether or not an explicit
 * comparator is provided) must be <i>consistent with equals</i> if it is to
 * correctly implement the <tt>List</tt> interface.  (See <tt>Comparable</tt>
 * or <tt>Comparator</tt> for a precise definition of <i>consistent with
 * equals</i>.)  This is so because the <tt>List</tt> interface is defined in
 * terms of the <tt>equals</tt> operation, but a <tt>TreeList</tt> instance
 * performs all key comparisons using its <tt>compareTo</tt> (or
 * <tt>compare</tt>) method, so two keys that are deemed equal by this method
 * are, from the standpoint of the list, equal.  The behavior of a list
 * <i>is</i> well-defined even if its ordering is inconsistent with equals; it
 * just fails to obey the general contract of the <tt>List</tt> interface.<p>
 *
 * <b>Note that this implementation is not synchronized.</b> If multiple
 * threads access a list concurrently, and at least one of the threads modifies
 * the list, it <i>must</i> be synchronized externally.  This is typically
 * accomplished by synchronizing on some object that naturally encapsulates the
 * list.  If no such object exists, the list should be "wrapped" using the
 * <tt>Collections.synchronizedList</tt> method.  This is best done at creation
 * time, to prevent accidental unsynchronized access to the list:
 * <pre>
 *     List list = Collections.synchronizedList(new ArrayList(...));
 * </pre><p>
 *
 * The Iterators returned by this class's <tt>iterator</tt> method are
 * <i>fail-fast</i>: if the list is modified at any time after the iterator is
 * created, in any way except through the iterator's own <tt>remove</tt>
 * method, the iterator will throw a <tt>ConcurrentModificationException</tt>.
 * Thus, in the face of concurrent modification, the iterator fails quickly
 * and cleanly, rather than risking arbitrary, non-deterministic behavior at
 * an undetermined time in the future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:   <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i><p>
 *
 * @author  Matthew Wilson
 * @version 1.0, 16/11/09
 * @param <E> the element type
 * @see     java.util.Collection
 * @see     java.util.List
 * @see     Comparable
 * @see     Comparator
 * @see     IndexedTreeMap
 * @see     java.util.Collections#synchronizedList(List)
 */
public class TreeList<E>
extends AbstractList<E>
implements SortedList<E>, RandomAccess, Cloneable, java.io.Serializable
{

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -7196319787853289337L;

  // Dummy value to associate with an Object in the backing Map
  /** The Constant PRESENT. */
  private static final Object PRESENT = new Object();

  /** The m. */
  private transient IndexedMap<E,Object> m; // The backing Map

  /** The key set. */
  private transient Set<E> keySet; // The keySet view of the backing Map

  /**
   * Constructs a new, empty list, sorted according to the elements' natural
   * order.  All elements inserted into the list must implement the
   * <tt>Comparable</tt> interface.  Furthermore, all such elements must be
   * <i>mutually comparable</i>: <tt>e1.compareTo(e2)</tt> must not throw a
   * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
   * <tt>e2</tt> in the list.  If the user attempts to add an element to the
   * list that violates this constraint (for example, the user attempts to
   * add a string element to a list whose elements are integers), the
   * <tt>add(Object)</tt> call will throw a <tt>ClassCastException</tt>.
   *
   * @see Comparable
   */
  public TreeList()
  {
    this(new IndexedTreeMap<E,Object>());
  }

  /**
   * Constructs a list backed by the specified indexed map.
   *
   * @param m the m
   */
  public TreeList(IndexedMap<E,Object> m)
  {
    this.m = m;
    keySet = m.keySet();
  }

  /**
   * Constructs a new, empty list, sorted according to the specified
   * comparator.  All elements inserted into the list must be <i>mutually
   * comparable</i> by the specified comparator: <tt>comparator.compare(e1,
   * e2)</tt> must not throw a <tt>ClassCastException</tt> for any elements
   * <tt>e1</tt> and <tt>e2</tt> in the list.  If the user attempts to add
   * an element to the list that violates this constraint, the
   * <tt>add(Object)</tt> call will throw a <tt>ClassCastException</tt>.
   *
   * @param c the comparator that will be used to sort this list.  A
   *        <tt>null</tt> value indicates that the elements' <i>natural
   *        ordering</i> should be used.
   */
  public TreeList(Comparator<? super E> c)
  {
    this(new IndexedTreeMap<E,Object>(c));
  }

  /**
   * Constructs a new list containing the elements in the specified
   * collection, sorted according to the elements' <i>natural order</i>.
   * All keys inserted into the list must implement the <tt>Comparable</tt>
   * interface.  Furthermore, all such keys must be <i>mutually
   * comparable</i>: <tt>k1.compareTo(k2)</tt> must not throw a
   * <tt>ClassCastException</tt> for any elements <tt>k1</tt> and
   * <tt>k2</tt> in the list.
   *
   * @param c The elements that will comprise the new list.
   */
  public TreeList(Collection<? extends E> c)
  {
    this();
    addAll(c);
  }

  /**
   * Constructs a new list containing the same elements as the specified
   * sorted list, sorted according to the same ordering.
   *
   * @param l sorted list whose elements will comprise the new list.
   */
  public TreeList(SortedList<E> l)
  {
    this(l.comparator());
    addAll(l);
  }

  /**
   * Returns an iterator over the elements in this list.  The elements
   * are returned in ascending order.
   *
   * @return an iterator over the elements in this list.
   */
  public Iterator<E> iterator()
  {
    return keySet.iterator();
  }

  /**
   * Returns the number of elements in this list (its cardinality).
   *
   * @return the number of elements in this list (its cardinality).
   */
  public int size()
  {
    return m.size();
  }

  /**
   * Returns <tt>true</tt> if this list contains no elements.
   *
   * @return <tt>true</tt> if this list contains no elements.
   */
  public boolean isEmpty()
  {
    return m.isEmpty();
  }

  /**
   * Returns <tt>true</tt> if this list contains the specified element.
   *
   * @param o the object to be checked for containment in this list.
   * @return <tt>true</tt> if this list contains the specified element.
   */
  public boolean contains(Object o)
  {
    return m.containsKey(o);
  }

  /**
   * Adds the specified element to this list if it is not already present.
   *
   * @param o element to be added to this list.
   * @return <tt>true</tt> if the list did not already contain the specified
   *         element.
   */
  public boolean add(E o)
  {
    return m.put(o, PRESENT) == null;
  }

  /**
   * Removes the specified element from this list if it is present.
   *
   * @param o object to be removed from this list, if present.
   * @return <tt>true</tt> if the list contained the specified element.
   */
  public boolean remove(Object o)
  {
    return m.remove(o) == PRESENT;
  }

  /**
   * Returns the element at the specified position in this list.
   *
   * @param index the index
   * @return the element at the specified position in this list.
   */
  @SuppressWarnings("unchecked")
public E get(int index)
  {
    return (E)m.get(index);
  }

  /**
   * Removes all of the elements from this list.
   */
  public void clear()
  {
    m.clear();
  }

  /**
   * Adds all of the elements in the specified collection to this list.
   *
   * @param c elements to be added
   * @return <tt>true</tt> if this list changed as a result of the call.
   */
  @SuppressWarnings("unchecked")
public boolean addAll(Collection<? extends E> c)
  {
    if (m.size() == 0 && c.size() > 0 && c instanceof SortedList &&
      m instanceof IndexedTreeMap)
    {
      SortedList<Map.Entry<E,Object>> list = (SortedList<Map.Entry<E,Object>>)c;
      IndexedTreeMap<E,Object> map = (IndexedTreeMap<E,Object>)m;
      Comparator<? super E> cc = (Comparator<E>)list.comparator();
      Comparator<? super E> mc = map.comparator();
      if (cc == mc || (cc != null && cc.equals(mc)))
      {
        map.addAllForTreeList(list, PRESENT);
        return true;
      }
    }
    return super.addAll(c);
  }

  /**
   * Returns a view of the portion of this list whose elements range from
   * <tt>fromElement</tt>, inclusive, to <tt>toElement</tt>, exclusive.  (If
   * <tt>fromElement</tt> and <tt>toElement</tt> are equal, the returned
   * sorted list is empty.)  The returned sorted list is backed by this list,
   * so changes in the returned sorted list are reflected in this list, and
   * vice-versa.  The returned sorted list supports all optional List
   * operations.<p>
   *
   * The sorted list returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert an
   * element outside the specified range.<p>
   *
   * Note: this method always returns a <i>half-open range</i> (which
   * includes its low endpoint but not its high endpoint).  If you need a
   * <i>closed range</i> (which includes both endpoints), and the element
   * type allows for calculation of the successor of a specified value,
   * merely request the subrange from <tt>lowEndpoint</tt> to
   * <tt>successor(highEndpoint)</tt>.  For example, suppose that <tt>s</tt>
   * is a sorted list of strings.  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> from <tt>low</tt> to
   * <tt>high</tt>, inclusive: <pre>
   *     SortedList sub = s.subList(low, high+"\0");
   * </pre>
   *
   * A similar technique can be used to generate an <i>open range</i> (which
   * contains neither endpoint).  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> from <tt>low</tt> to
   * <tt>high</tt>, exclusive: <pre>
   *     SortedList sub = s.subList(low+"\0", high);
   * </pre>
   *
   * @param fromElement low endpoint (inclusive) of the subList.
   * @param toElement high endpoint (exclusive) of the subList.
   * @return a view of the portion of this list whose elements range from
   *         <tt>fromElement</tt>, inclusive, to <tt>toElement</tt>,
   *         exclusive.
   */
  public SortedList<E> subList(E fromElement, E toElement)
  {
    return new TreeList<E>(m.subMap(fromElement, toElement));
  }

  /**
   * Returns a view of the portion of this list between <tt>fromIndex</tt>,
   * inclusive, and <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex</tt> and
   * <tt>toIndex</tt> are equal, the returned list is empty.)  The returned
   * list is backed by this list, so changes in the returned list are
   * reflected in this list, and vice-versa.  The returned list supports all
   * of the optional list operations supported by this list.<p>
   *
   * This method eliminates the need for explicit range operations (of the
   * sort that commonly exist for arrays).  Any operation that expects a
   * list can be used as a range operation by operating on a subList view
   * instead of a whole list.  For example, the following idiom removes a
   * range of elements from a list:
   * <pre>
   *     list.subList(from, to).clear();
   * </pre>
   * Similar idioms may be constructed for <tt>indexOf</tt> and
   * <tt>lastIndexOf</tt>, and all of the algorithms in the
   * <tt>Collections</tt> class can be applied to a subList.<p>
   *
   * The semantics of the list returned by this method become undefined if
   * the backing list (i.e., this list) is <i>structurally modified</i> in
   * any way other than via the returned list.  (Structural modifications are
   * those that change the size of the list, or otherwise perturb it in such
   * a fashion that iterations in progress may yield incorrect results.)<p>
   *
   * This implementation returns a list that subclasses
   * <tt>AbstractList</tt>.  The subclass stores, in private fields, the
   * offset of the subList within the backing list, the size of the subList
   * (which can change over its lifetime), and the expected
   * <tt>modCount</tt> value of the backing list.  There are two variants
   * of the subclass, one of which implements <tt>RandomAccess</tt>.
   * If this list implements <tt>RandomAccess</tt> the returned list will
   * be an instance of the subclass that implements <tt>RandomAccess</tt>.<p>
   *
   * The subclass's <tt>set(int, Object)</tt>, <tt>get(int)</tt>,
   * <tt>add(int, Object)</tt>, <tt>remove(int)</tt>, <tt>addAll(int,
   * Collection)</tt> and <tt>removeRange(int, int)</tt> methods all
   * delegate to the corresponding methods on the backing abstract list,
   * after bounds-checking the index and adjusting for the offset.  The
   * <tt>addAll(Collection c)</tt> method merely returns <tt>addAll(size,
   * c)</tt>.<p>
   *
   * The <tt>listIterator(int)</tt> method returns a "wrapper object" over a
   * list iterator on the backing list, which is created with the
   * corresponding method on the backing list.  The <tt>iterator</tt> method
   * merely returns <tt>listIterator()</tt>, and the <tt>size</tt> method
   * merely returns the subclass's <tt>size</tt> field.<p>
   *
   * All methods first check to see if the actual <tt>modCount</tt> of the
   * backing list is equal to its expected value, and throw a
   * <tt>ConcurrentModificationException</tt> if it is not.
   *
   * @param fromIndex low endpoint (inclusive) of the subList.
   * @param toIndex high endpoint (exclusive) of the subList.
   * @return a view of the specified range within this list.
   */
  public SortedList<E> subList(int fromIndex, int toIndex)
  {
    return new TreeList<E>(m.subMap(fromIndex, toIndex));
  }

  /**
   * Returns a view of the portion of this list whose elements range from
   * <tt>fromElement</tt>, inclusive, to <tt>toIndex</tt>, exclusive.  (If
   * <tt>fromElement</tt> and <tt>toIndex</tt> are equal, the returned
   * sorted list is empty.)  The returned sorted list is backed by this list,
   * so changes in the returned sorted list are reflected in this list, and
   * vice-versa.  The returned sorted list supports all optional List
   * operations.<p>
   *
   * The sorted list returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert an
   * element outside the specified range.<p>
   *
   * Note: this method always returns a <i>half-open range</i> (which
   * includes its low endpoint but not its high endpoint).  If you need a
   * <i>closed range</i> (which includes both endpoints), and the element
   * type allows for calculation of the successor of a specified value,
   * merely request the subrange from <tt>lowEndpoint</tt> to
   * <tt>successor(highEndpoint)</tt>.  For example, suppose that <tt>s</tt>
   * is a sorted list of strings.  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> from <tt>low</tt> to
   * <tt>high</tt>, inclusive: <pre>
   *     SortedList sub = s.subList(low, high+"\0");
   * </pre>
   *
   * A similar technique can be used to generate an <i>open range</i> (which
   * contains neither endpoint).  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> from <tt>low</tt> to
   * <tt>high</tt>, exclusive: <pre>
   *     SortedList sub = s.subList(low+"\0", high);
   * </pre>
   *
   * @param fromElement low endpoint (inclusive) of the subList.
   * @param toIndex high endpoint (exclusive) of the subList.
   * @return a view of the portion of this list whose elements range from
   *         <tt>fromElement</tt>, inclusive, to <tt>toElement</tt>,
   *         exclusive.
   */
  public SortedList<E> subList(E fromElement, int toIndex)
  {
    return new TreeList<E>(m.subMap(fromElement, toIndex));
  }

  /**
   * Returns a view of the portion of this list whose elements range from
   * <tt>fromIndex</tt>, inclusive, to <tt>toElement</tt>, exclusive.  (If
   * <tt>fromIndex</tt> and <tt>toElement</tt> are equal, the returned
   * sorted list is empty.)  The returned sorted list is backed by this list,
   * so changes in the returned sorted list are reflected in this list, and
   * vice-versa.  The returned sorted list supports all optional List
   * operations.<p>
   *
   * The sorted list returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert an
   * element outside the specified range.<p>
   *
   * Note: this method always returns a <i>half-open range</i> (which
   * includes its low endpoint but not its high endpoint).  If you need a
   * <i>closed range</i> (which includes both endpoints), and the element
   * type allows for calculation of the successor of a specified value,
   * merely request the subrange from <tt>lowEndpoint</tt> to
   * <tt>successor(highEndpoint)</tt>.  For example, suppose that <tt>s</tt>
   * is a sorted list of strings.  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> from <tt>low</tt> to
   * <tt>high</tt>, inclusive: <pre>
   *     SortedList sub = s.subList(low, high+"\0");
   * </pre>
   *
   * A similar technique can be used to generate an <i>open range</i> (which
   * contains neither endpoint).  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> from <tt>low</tt> to
   * <tt>high</tt>, exclusive: <pre>
   *     SortedList sub = s.subList(low+"\0", high);
   * </pre>
   *
   * @param fromIndex low endpoint (inclusive) of the subList.
   * @param toElement high endpoint (exclusive) of the subList.
   * @return a view of the portion of this list whose elements range from
   *         <tt>fromElement</tt>, inclusive, to <tt>toElement</tt>,
   *         exclusive.
   */
  public SortedList<E> subList(int fromIndex, E toElement)
  {
    return new TreeList<E>(m.subMap(fromIndex, toElement));
  }

  /**
   * Returns a view of the portion of this list whose elements are strictly
   * less than <tt>toElement</tt>.  The returned sorted list is backed by
   * this list, so changes in the returned sorted list are reflected in this
   * list, and vice-versa.  The returned sorted list supports all optional list
   * operations.<p>
   *
   * The sorted list returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert an
   * element greater than or equal to <tt>toElement</tt>.<p>
   *
   * Note: this method always returns a view that does not contain its
   * (high) endpoint.  If you need a view that does contain this endpoint,
   * and the element type allows for calculation of the successor of a
   * specified value, merely request a headList bounded by
   * <tt>successor(highEndpoint)</tt>.  For example, suppose that <tt>s</tt>
   * is a sorted list of strings.  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> that are less than or equal
   * to <tt>high</tt>: <pre> SortedList head = s.headList(high+"\0");</pre>
   *
   * @param toElement high endpoint (exclusive) of the headList.
   * @return a view of the portion of this list whose elements are strictly
   *         less than toElement.
   */
  public SortedList<E> headList(E toElement)
  {
    return new TreeList<E>(m.headMap(toElement));
  }

  /**
   * Returns a view of the portion of this list whose elements are strictly
   * less than <tt>toIndex</tt>.  The returned sorted list is backed by
   * this list, so changes in the returned sorted list are reflected in this
   * list, and vice-versa.  The returned sorted list supports all optional list
   * operations.<p>
   *
   * The sorted list returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert an
   * element greater than or equal to <tt>toIndex</tt>.<p>
   *
   * Note: this method always returns a view that does not contain its
   * (high) endpoint.  If you need a view that does contain this endpoint,
   * and the element type allows for calculation of the successor of a
   * specified value, merely request a headList bounded by
   * <tt>successor(highEndpoint)</tt>.  For example, suppose that <tt>s</tt>
   * is a sorted list of strings.  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> that are less than or equal
   * to <tt>high</tt>: <pre> SortedList head = s.headList(high+"\0");</pre>
   *
   * @param toIndex high endpoint (exclusive) of the headList.
   * @return a view of the portion of this list whose elements are strictly
   *         less than toElement.
   */
  public SortedList<E> headList(int toIndex)
  {
    return new TreeList<E>(m.headMap(toIndex));
  }

  /**
   * Returns a view of the portion of this list whose elements are
   * greater than or equal to <tt>fromElement</tt>.  The returned sorted list
   * is backed by this list, so changes in the returned sorted list are
   * reflected in this list, and vice-versa.  The returned sorted list
   * supports all optional list operations.<p>
   *
   * The sorted list returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert an
   * element less than <tt>fromElement</tt>.
   *
   * Note: this method always returns a view that contains its (low)
   * endpoint.  If you need a view that does not contain this endpoint, and
   * the element type allows for calculation of the successor of a specified
   * value, merely request a tailList bounded by
   * <tt>successor(lowEndpoint)</tt>.  For example, suppose that <tt>s</tt>
   * is a sorted list of strings.  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> that are strictly greater
   * than <tt>low</tt>: <pre>
   *     SortedList tail = s.tailList(low+"\0");
   * </pre>
   *
   * @param fromElement low endpoint (inclusive) of the tailList.
   * @return a view of the portion of this list whose elements are
   *         greater than or equal to <tt>fromElement</tt>.
   */
  public SortedList<E> tailList(E fromElement)
  {
    return new TreeList<E>(m.tailMap(fromElement));
  }

  /**
   * Returns a view of the portion of this list whose elements are
   * greater than or equal to <tt>fromIndex</tt>.  The returned sorted list
   * is backed by this list, so changes in the returned sorted list are
   * reflected in this list, and vice-versa.  The returned sorted list
   * supports all optional list operations.<p>
   *
   * The sorted list returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert an
   * element less than <tt>fromIndex</tt>.
   *
   * Note: this method always returns a view that contains its (low)
   * endpoint.  If you need a view that does not contain this endpoint, and
   * the element type allows for calculation of the successor of a specified
   * value, merely request a tailList bounded by
   * <tt>successor(lowEndpoint)</tt>.  For example, suppose that <tt>s</tt>
   * is a sorted list of strings.  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> that are strictly greater
   * than <tt>low</tt>: <pre>
   *     SortedList tail = s.tailList(low+"\0");
   * </pre>
   *
   * @param fromIndex low endpoint (inclusive) of the tailList.
   * @return a view of the portion of this list whose elements are
   *         greater than or equal to <tt>fromElement</tt>.
   */
  public SortedList<E> tailList(int fromIndex)
  {
    return new TreeList<E>(m.tailMap(fromIndex));
  }

  /**
   * Returns the comparator used to order this sorted list, or <tt>null</tt>
   * if this tree list uses its elements natural ordering.
   *
   * @return the comparator used to order this sorted list, or <tt>null</tt>
   * if this tree list uses its elements natural ordering.
   */
  public Comparator<? super E> comparator()
  {
    return m.comparator();
  }

  /**
   * Returns the first (lowest) element currently in this sorted list.
   *
   * @return the first (lowest) element currently in this sorted list.
   */
  public E first()
  {
    return m.firstKey();
  }

  /**
   * Returns the last (highest) element currently in this sorted list.
   *
   * @return the last (highest) element currently in this sorted list.
   */
  public E last()
  {
    return m.lastKey();
  }

  /**
   * Returns a shallow copy of this <tt>TreeList</tt> instance. (The elements
   * themselves are not cloned.)
   *
   * @return a shallow copy of this list.
   */
  @SuppressWarnings("unchecked")
public Object clone()
  {
    TreeList<E> clone = null;
    try
    {
      clone = (TreeList<E>)super.clone();
    }
    catch (CloneNotSupportedException e)
    {
      throw new InternalError();
    }

    clone.m = new IndexedTreeMap<E,Object>(m);
    clone.keySet = clone.m.keySet();

    return clone;
  }

  /**
   * Save the state of the <tt>TreeList</tt> instance to a stream (that is,
   * serialize it).
   *
   * @param s the s
   * @throws IOException Signals that an I/O exception has occurred.
   * @serialData Emits the comparator used to order this list, or
   *             <tt>null</tt> if it obeys its elements' natural ordering
   *             (Object), followed by the size of the list (the number of
   *             elements it contains) (int), followed by all of its
   *             elements (each an Object) in order (as determined by the
   *             list's Comparator, or by the elements' natural ordering if
   *             the list has no Comparator).
   */
  @SuppressWarnings("rawtypes")
private void writeObject(java.io.ObjectOutputStream s)
  throws java.io.IOException
  {
    s.defaultWriteObject();
    s.writeObject(m.comparator());
    s.writeInt(m.size());
    for (Iterator i = m.keySet().iterator(); i.hasNext(); )
    {
      s.writeObject(i.next());
    }
  }

  /**
   * Reconstitute the <tt>TreeList</tt> instance from a stream (that is,
   * deserialize it).
   *
   * @param s the s
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ClassNotFoundException the class not found exception
   */
  @SuppressWarnings("unchecked")
private void readObject(java.io.ObjectInputStream s)
  throws java.io.IOException, ClassNotFoundException
  {
    s.defaultReadObject();
    Comparator<E> c = (Comparator<E>)s.readObject();

    IndexedTreeMap<E,Object> tm;
    if (c == null) tm = new IndexedTreeMap<E,Object>();
    else tm = new IndexedTreeMap<E,Object>(c);
    m = tm;
    keySet = m.keySet();

    int size = s.readInt();
    tm.readTreeList(size, s, PRESENT);
  }
}
