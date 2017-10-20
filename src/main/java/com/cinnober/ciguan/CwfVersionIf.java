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



/**
 *
 * Interface defining the iGUAN version
 * 
 */
public interface CwfVersionIf {
    
    /**
     * The current version number in "Dewey Decimal" notation.
     */
    String VERSION = "10.5.1";
    String NAME = "iGUAN / Cinnober Web Framework";
    String CODENAME = "Greenleaf Service Release 1";
    
    /**
     * @return the major version
     */
    int getMajorVersion();
    
    /**
     * @return the minor version
     */
    int getMinorVersion();
    
    /**
     * @return the micro version
     */
    int getMicroVersion();

    /**
     *
     * Singleton version instance
     * 
     */
    static class Singleton implements CwfVersionIf {
        
        static CwfVersionIf cInstance;

        int mMajorVersion;
        int mMinorVersion;
        int mMicroVersion;
        
        private Singleton() {
            String[] tTokens = VERSION.split("\\.");
            mMajorVersion = Integer.parseInt(tTokens[0]);
            mMinorVersion = Integer.parseInt(tTokens[1]);
            mMicroVersion = Integer.parseInt(tTokens[2]);
        }
        
        public static CwfVersionIf get() {
            if (cInstance == null) {
                cInstance = new Singleton();
            }
            return cInstance;
        }
        
        @Override
        public int getMajorVersion() {
            return mMajorVersion;
        }
        
        @Override
        public int getMinorVersion() {
            return mMinorVersion;
        }
        
        @Override
        public int getMicroVersion() {
            return mMicroVersion;
        }
        
        @Override
        public String toString() {
            return NAME + " - Version " + VERSION + " (" + CODENAME + ")";
        }
        
    }
    
}
