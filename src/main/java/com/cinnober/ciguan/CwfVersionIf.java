/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Cinnober Financial Technology AB (cinnober.com)
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
 * Interface defining the CIGUAN version
 * 
 */
public interface CwfVersionIf {
    
    /**
     * The current version number using "Semantic Versioning" notation.
     */
    String NAME = "CIGUAN";
    String VERSION = "0.2.0";
    String CODENAME = "Greenleaf";
    String COPYRIGHT = "Copyright (c) 2018 Cinnober Financial Technology AB (cinnober.com). ";
    String LICENSE = "The MIT License (MIT)";
    
    /**
     * @return the major version
     */
    int getMajorVersion();
    
    /**
     * @return the minor version
     */
    int getMinorVersion();
    
    /**
     * @return the patch version
     */
    int getPatchVersion();

    /**
     *
     * Singleton version instance
     * 
     */
    static class Singleton implements CwfVersionIf {
        
        static CwfVersionIf cInstance;

        int mMajorVersion;
        int mMinorVersion;
        int mPatchVersion;
        
        private Singleton() {
            String[] tTokens = VERSION.split("\\.");
            mMajorVersion = Integer.parseInt(tTokens[0]);
            mMinorVersion = Integer.parseInt(tTokens[1]);
            mPatchVersion = Integer.parseInt(tTokens[2]);
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
        public int getPatchVersion() {
            return mPatchVersion;
        }
        
        @Override
        public String toString() {
            return NAME + System.lineSeparator() 
                 + VERSION + " (" + CODENAME + ")" + System.lineSeparator() 
                 + COPYRIGHT + System.lineSeparator() 
                 + LICENSE;
        }
    }
    
}
