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

import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.datasource.impl.AsEmapiTreeMapList;

/**
 *
 * Interface defining broadcast to list mapping functionality 
 * 
 * The sole purpose of a class implementing this interface is to examine an incoming message,
 * attempt to look up a set of lists to add it to, and if the message was added to at least one list,
 * return true. If no matching list(s) were found, the implementation should return false.
 * 
 * By default, the application server looks in the class to list map using the exact class of the
 * incoming message. This results in a set of lists that matches the class of the message and all
 * of its superclasses which have a configured data source. While this gives a certain level of flexibility,
 * there are situations where that is not enough. For example, if you have a base class A with subclasses
 * A1 and A2, but only have a data source where the type is A, any messages of type A1 or A2 will not match
 * anything. However, if you have data sources where the type is A1 and A, an A1 message will be added both
 * to the list of type A1 and the list of type A, while an A2 message will not match anything.
 * 
 * Recommendation:
 * If you know that you use subclassing and that you do NOT have data sources for all subclasses which you
 * use for messages, you likely need to write a custom implementation of this interface and configure it
 * in the bean factory.
 * 
 * Some final pointers:
 * 1) It is strictly forbidden to modify the message in any way.
 * 2) It is strictly forbidden to add, modify or remove entries in the map or any of its sets.
 * 3) It is strictly forbidden to insert the message in lists not being part of the sets in the map.
 *    Please use broadcast processors or broadcast listeners attached to root lists for that purpose in
 *    order to keep the code in the mapper as clean as possible.
 * 4) Please keep the code as fast as possible. For example, if you traverse up the class hierarchy
 *    when looking for an entry, you only need to process the lists in the first matching entry.
 * 
 */
public interface AsBdxMapperIf {

    /**
     * Process an incoming message and try to add it to matching data sources.
     *
     * @param pMessage The received message.
     * @param pClassToListMap The map containing lists mapped to all known data source item classes.
     * @return {@code true} if the broadcast was added to at least one list, otherwise {@code false}.
     */
    boolean onBroadcast(final Object pMessage, final Map<Class<?>, Set<AsEmapiTreeMapList<?>>> pClassToListMap);
    
}
