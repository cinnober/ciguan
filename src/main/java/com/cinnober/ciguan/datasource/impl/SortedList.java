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
import java.util.Comparator;
import java.util.List;

/**
 * The Interface SortedList.
 *
 * @param <E> the element type
 */
public interface SortedList<E> extends List<E>
{
  /**
   * Returns the comparator associated with this sorted list, or
   * <tt>null</tt> if it uses its elements' natural ordering.
   *
   * @return the comparator associated with this sorted list, or
   *         <tt>null</tt> if it uses its elements' natural ordering.
   */
  Comparator<? super E> comparator();
  
  /**
   * Returns a view of the portion of this sorted list whose elements range
   * from <tt>fromElement</tt>, inclusive, to <tt>toElement</tt>, exclusive.
   * (If <tt>fromElement</tt> and <tt>toElement</tt> are equal, the returned
   * sorted list is empty.)  The returned sorted list is backed by this sorted
   * list, so changes in the returned sorted list are reflected in this sorted
   * list, and vice-versa.  The returned sorted list supports all optional list
   * operations that this sorted list supports.<p>
   * 
   * The sorted list returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert a
   * element outside the specified range.<p>
   * 
   * Note: this method always returns a <i>half-open range</i> (which
   * includes its low endpoint but not its high endpoint).  If you need a
   * <i>closed range</i> (which includes both endpoints), and the element
   * type allows for calculation of the successor a given value, merely
   * request the subrange from <tt>lowEndpoint</tt> to
   * <tt>successor(highEndpoint)</tt>.  For example, suppose that <tt>s</tt>
   * is a sorted list of strings.  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> from <tt>low</tt> to
   * <tt>high</tt>, inclusive: <pre>
   * SortedList sub = s.subList(low, high+"\0");
   * </pre>
   * 
   * A similar technique can be used to generate an <i>open range</i> (which
   * contains neither endpoint).  The following idiom obtains a view
   * containing all of the Strings in <tt>s</tt> from <tt>low</tt> to
   * <tt>high</tt>, exclusive: <pre>
   * SortedList sub = s.subList(low+"\0", high);
   * </pre>
   *
   * @param fromElement low endpoint (inclusive) of the subList.
   * @param toElement high endpoint (exclusive) of the subList.
   * @return a view of the specified range within this sorted list.
   */
  SortedList<E> subList(E fromElement, E toElement);
  
  /* (non-Javadoc)
   * @see java.util.List#subList(int, int)
   */
  SortedList<E> subList(int fromIndex, int toIndex);
  
  /**
   * Sub list.
   *
   * @param fromElement the from element
   * @param toIndex the to index
   * @return the sorted list
   */
  SortedList<E> subList(E fromElement, int toIndex);
  
  /**
   * Sub list.
   *
   * @param fromIndex the from index
   * @param toElement the to element
   * @return the sorted list
   */
  SortedList<E> subList(int fromIndex, E toElement);
  
  /**
   * Returns a view of the portion of this sorted list whose elements are
   * strictly less than <tt>toElement</tt>.  The returned sorted list is
   * backed by this sorted list, so changes in the returned sorted list are
   * reflected in this sorted list, and vice-versa.  The returned sorted list
   * supports all optional list operations.<p>
   * 
   * The sorted list returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert a
   * element outside the specified range.<p>
   * 
   * Note: this method always returns a view that does not contain its
   * (high) endpoint.  If you need a view that does contain this endpoint,
   * and the element type allows for calculation of the successor a given
   * value, merely request a headList bounded by
   * <tt>successor(highEndpoint)</tt>.  For example, suppose that <tt>s</tt>
   * is a sorted list of strings.  The following idiom obtains a view
   * containing all of the strings in <tt>s</tt> that are less than or equal
   * to <tt>high</tt>:
   *      <pre>    SortedList head = s.headList(high+"\0");</pre>
   *
   * @param toElement high endpoint (exclusive) of the headList.
   * @return a view of the specified initial range of this sorted list.
   */
  SortedList<E> headList(E toElement);
  
  /**
   * Head list.
   *
   * @param toIndex the to index
   * @return the sorted list
   */
  SortedList<E> headList(int toIndex);
  
  /**
   * Returns a view of the portion of this sorted list whose elements are
   * greater than or equal to <tt>fromElement</tt>.  The returned sorted list
   * is backed by this sorted list, so changes in the returned sorted list are
   * reflected in this sorted list, and vice-versa.  The returned sorted list
   * supports all optional list operations.<p>
   * 
   * The sorted list returned by this method will throw an
   * <tt>IllegalArgumentException</tt> if the user attempts to insert a
   * element outside the specified range.<p>
   * 
   * Note: this method always returns a view that contains its (low)
   * endpoint.  If you need a view that does not contain this endpoint, and
   * the element type allows for calculation of the successor a given value,
   * merely request a tailList bounded by <tt>successor(lowEndpoint)</tt>.
   * For example, suppose that <tt>s</tt> is a sorted list of strings.  The
   * following idiom obtains a view containing all of the strings in
   * <tt>s</tt> that are strictly greater than <tt>low</tt>:
   * 
   *      <pre>    SortedList tail = s.tailList(low+"\0");</pre>
   *
   * @param fromElement low endpoint (inclusive) of the tailList.
   * @return a view of the specified final range of this sorted list.
   */
  SortedList<E> tailList(E fromElement);
  
  /**
   * Tail list.
   *
   * @param fromIndex the from index
   * @return the sorted list
   */
  SortedList<E> tailList(int fromIndex);
  
  /**
   * Returns the first (lowest) element currently in this sorted list.
   *
   * @return the first (lowest) element currently in this sorted list.
   */
  E first();
  
  /**
   * Returns the last (highest) element currently in this sorted list.
   *
   * @return the last (highest) element currently in this sorted list.
   */
  E last();
}
