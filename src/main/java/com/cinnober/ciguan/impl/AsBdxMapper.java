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
package com.cinnober.ciguan.impl;

import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.AsBdxMapperIf;
import com.cinnober.ciguan.datasource.impl.AsEmapiTreeMapList;

/**
 *
 * Standard application server broadcast mapper.
 * 
 * Custom implementations may want to traverse up in the class hierarchy a number of levels and
 * check for a matching entry in the class to list map
 * 
 */
public class AsBdxMapper implements AsBdxMapperIf {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean onBroadcast(Object pMessage, Map<Class<?>, Set<AsEmapiTreeMapList<?>>> pClassToListMap) {
        Set<AsEmapiTreeMapList<?>> tLists = pClassToListMap.get(pMessage.getClass());
        if (tLists != null && !tLists.isEmpty()) {
            for (AsEmapiTreeMapList tList : tLists) {
                tList.messageReceived(pMessage);
            }
            return true;
        }
        return false;
    }

}
