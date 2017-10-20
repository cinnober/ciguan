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

import java.util.Locale;

import com.cinnober.ciguan.impl.As;

/**
 * Interface defining server side formatting functionality.
 */
public interface AsFormatIf {

    /**
     * Format a value according to its business type and locale.
     *
     * @param pValue The value to be formated.
     * @param pBusinessType The business type.
     * @param pLocale The locale.
     * @return the formated value.
     */
    String format(Object pValue, CwfBusinessTypeIf pBusinessType, Locale pLocale);
    
    /**
     * Get the divisor for the given business type.
     *
     * @param pBusinessType The business type.
     * @return the divisor for the specified business type.
     */
    int getDivisor(CwfBusinessTypeIf pBusinessType);
    
    /**
     * Singleton instance.
     */
    class Singleton {
        
        /** The Singleton. */
        private static AsFormatIf cSingleton;
        
        /**
         * Gets the singleton instance.
         *
         * @return the instance.
         */
        public static AsFormatIf get() {
            return cSingleton;
        }
        
        /**
         * Creates the singleton instance.
         */
        public static void create() {
            cSingleton = As.getBeanFactory().create(AsFormatIf.class);
        }

    }
    
}
